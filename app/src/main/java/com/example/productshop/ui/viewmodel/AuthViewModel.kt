package com.example.productshop.ui.viewmodel

import android.app.Application
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.productshop.data.model.CreateCustomerDto
import com.example.productshop.data.model.CustomerDto
import com.example.productshop.data.remote.RetrofitManager
import com.example.productshop.security.SecurityManager
import com.example.productshop.security.SessionManager
import com.example.productshop.security.facerecog.FaceRecognitionUtils
import com.example.productshop.util.EmailService
import com.example.productshop.util.OtpManager
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class OtpSent(val email: String) : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String, val isOtpError: Boolean = false) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val retrofitManager = RetrofitManager()
    private val securityManager = SecurityManager(application)
    private val emailService = EmailService()
    
    private var generatedOtp: String? = null
    
    companion object {
        var currentCustomer: CustomerDto? = null
    }

    var uiState: AuthUiState by mutableStateOf(AuthUiState.Idle)
        private set

    var isGuest by mutableStateOf(false)
        private set

    var isBiometricPromptVisible by mutableStateOf(false)
    
    var failedAttempts by mutableIntStateOf(0)
        private set
    var lockoutTimeRemaining by mutableIntStateOf(0)
        private set

    fun setGuestMode(guest: Boolean) {
        isGuest = guest
    }

    fun clearData() {
        currentCustomer = null
        uiState = AuthUiState.Idle
        isGuest = false
        failedAttempts = 0
        lockoutTimeRemaining = 0
    }

    fun canUseBiometric(): Boolean {
        return securityManager.isBiometricAvailable(getApplication()) && securityManager.hasStoredCredentials()
    }

    fun canUseFaceAuth(): Boolean {
        return securityManager.isFaceAuthSetup() && securityManager.hasStoredCredentials()
    }

    fun isFaceAuthSetup(): Boolean {
        return securityManager.isFaceAuthSetup()
    }

    fun sendOtp(email: String) {
        viewModelScope.launch {
            uiState = AuthUiState.Loading
            try {
                val otp = OtpManager.generateOtp()
                generatedOtp = otp
                
                // Save to .env as requested
                OtpManager.saveOtpToEnv(otp)
                
                // Send email
                emailService.sendOtpEmail(email, otp)
                
                uiState = AuthUiState.OtpSent(email)
            } catch (e: Exception) {
                uiState = AuthUiState.Error("Failed to send OTP: ${e.localizedMessage}")
            }
        }
    }

    fun signup(
        username: String,
        password: String,
        firstName: String,
        lastName: String,
        idNumber: String,
        customerTypeId: Long,
        otp: String,
        onNavigateBack: () -> Unit
    ) {
        if (otp != generatedOtp) {
            uiState = AuthUiState.Error("Invalid OTP. Please check your email.", isOtpError = true)
            return
        }

        viewModelScope.launch {
            uiState = AuthUiState.Loading
            try {
                // Step 1: Login as "signup" user to get registration token
                val signupAuth = "Basic " + Base64.encodeToString(
                    "signup:signup".toByteArray(),
                    Base64.NO_WRAP
                )
                val loginResult = retrofitManager.authService.login(signupAuth)
                val registrationToken = loginResult.loginAccessKey 
                    ?: throw Exception("Failed to get registration token")

                // Step 2: Register the Customer profile using the registration token
                retrofitManager.customerService.registerCustomer(
                    authHeader = "Bearer $registrationToken",
                    request = CreateCustomerDto(
                        username = username,
                        password = password,
                        firstName = firstName,
                        lastName = lastName,
                        idNumber = idNumber,
                        customerTypeId = customerTypeId
                    )
                )
                uiState = AuthUiState.Success("Account created successfully!")
                onNavigateBack()
            } catch (e: HttpException) {
                uiState = when (e.code()) {
                    400 -> AuthUiState.Error("This email address is already registered. Try logging in instead.")
                    401 -> AuthUiState.Error("Registration failed: Unauthorized. Please contact support.")
                    else -> AuthUiState.Error("Something went wrong on our end. Please try again later. (${e.code()})")
                }
            } catch (e: IOException) {
                uiState = AuthUiState.Error("We're having trouble reaching our servers. Please check your connection.")
            } catch (e: Exception) {
                uiState = AuthUiState.Error("An unexpected error occurred. Please try again.")
            }
        }
    }

    fun login(username: String, password: String, onLoginSuccess: () -> Unit) {
        if (lockoutTimeRemaining > 0) {
            uiState = AuthUiState.Error("Too many failed attempts. Please wait $lockoutTimeRemaining seconds.")
            return
        }

        viewModelScope.launch {
            uiState = AuthUiState.Loading
            try {
                val authHeader = "Basic " + Base64.encodeToString(
                    "$username:$password".toByteArray(),
                    Base64.NO_WRAP
                )
                val result = retrofitManager.authService.login(authHeader)
                if (result.success == "true") {
                    // Save volatile token
                    val token = result.loginAccessKey ?: throw Exception("No access key received")
                    SessionManager.setToken(token)
                    isGuest = false
                    failedAttempts = 0
                    lockoutTimeRemaining = 0
                    
                    // Fetch full customer profile
                    val profile = retrofitManager.customerService.getCustomerByEmail(username, "Bearer $token")
                    currentCustomer = profile
                    KycViewModel.profileId = profile.id

                    // Save credentials for biometric login if successful
                    securityManager.saveCredentials(username, password)
                    
                    uiState = AuthUiState.Success("Welcome back!")
                    onLoginSuccess()
                } else {
                    handleLoginFailure(result.errorMessage ?: "Incorrect email or password. Please try again.")
                }
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401 -> "Incorrect email or password. Please try again."
                    403 -> "Your account is not authorized to access this resource."
                    else -> "Something went wrong. Please try again later."
                }
                handleLoginFailure(message)
            } catch (e: IOException) {
                uiState = AuthUiState.Error("Connection error. Please check your internet and try again.", isOtpError = false)
            } catch (e: Exception) {
                uiState = AuthUiState.Error("Login failed. Please check your credentials.", isOtpError = false)
            }
        }
    }

    private fun handleLoginFailure(message: String) {
        failedAttempts++
        if (failedAttempts >= 5) {
            startLockout()
            uiState = AuthUiState.Error("Too many failed attempts. Account locked for 10 seconds.")
        } else {
            uiState = AuthUiState.Error(message)
        }
    }

    private fun startLockout() {
        lockoutTimeRemaining = 10
        viewModelScope.launch {
            while (lockoutTimeRemaining > 0) {
                kotlinx.coroutines.delay(1000)
                lockoutTimeRemaining--
            }
        }
    }

    fun onBiometricSuccess(onLoginSuccess: () -> Unit) {
        val (username, password) = securityManager.getCredentials()
        if (username != null && password != null) {
            login(username, password, onLoginSuccess)
        } else {
            uiState = AuthUiState.Error("No stored credentials found.", isOtpError = false)
        }
    }

    fun onFaceMatched(capturedEmbedding: FloatArray, onLoginSuccess: () -> Unit) {
        val savedEmbedding = securityManager.getFaceEmbedding()
        if (savedEmbedding != null) {
            if (FaceRecognitionUtils.isMatch(capturedEmbedding, savedEmbedding)) {
                val (username, password) = securityManager.getCredentials()
                if (username != null && password != null) {
                    login(username, password, onLoginSuccess)
                } else {
                    uiState = AuthUiState.Error("No stored credentials found.", isOtpError = false)
                }
            } else {
                uiState = AuthUiState.Error("Face does not match.", isOtpError = false)
            }
        } else {
            uiState = AuthUiState.Error("Face recognition not set up.", isOtpError = false)
        }
    }

    fun saveFaceEmbedding(embedding: FloatArray) {
        securityManager.saveFaceEmbedding(embedding)
    }
    
    fun resetState() {
        uiState = AuthUiState.Idle
    }
}
