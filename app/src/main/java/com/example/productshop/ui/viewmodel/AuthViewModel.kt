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
import com.example.productshop.util.AnalyticsManager
import com.example.productshop.util.EmailService
import com.example.productshop.util.OtpManager
import com.example.productshop.security.ProfileManager
import com.example.productshop.BuildConfig
import androidx.compose.runtime.mutableIntStateOf
import kotlinx.coroutines.delay
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
    private val profileManager = ProfileManager(application)
    
    var isGuest by mutableStateOf(!SessionManager.hasToken())
        private set

    init {
        NotificationViewModel.init(profileManager)
        
        // Restore session state if token is persisted
        if (SessionManager.hasToken()) {
            viewModelScope.launch {
                try {
                    val username = securityManager.getCredentials().first
                    if (username != null) {
                        val profile = retrofitManager.customerService.getCustomerByEmail(username, "Bearer ${SessionManager.bearerToken}")
                        currentCustomer = profile
                        KycViewModel.profileId = profile.id
                        
                        // Set profile pic and notifications
                        profilePicturePath = profileManager.getProfilePicture(username)
                        val savedNotifications = profileManager.getNotifications(username)
                        NotificationViewModel.setNotifications(savedNotifications)
                    }
                } catch (e: Exception) {
                    // Token might be expired or network error
                    // SessionManager.clearSession() // Optional: auto-logout on failure
                }
            }
        }
    }

    private var generatedOtp: String? = null

    companion object {
        var currentCustomer: CustomerDto? = null
    }

    var uiState: AuthUiState by mutableStateOf(AuthUiState.Idle)
        private set

    var isBiometricPromptVisible by mutableStateOf(false)
    
    var profilePicturePath by mutableStateOf<String?>(null)
        private set

    var failedAttempts by mutableIntStateOf(0)
        private set
    var lockoutTimeRemaining by mutableIntStateOf(0)
        private set

    var otpTimer by mutableIntStateOf(0)
        private set
    var isOtpExpired by mutableStateOf(false)
        private set
    private var timerJob: kotlinx.coroutines.Job? = null

    private fun startOtpTimer() {
        timerJob?.cancel()
        otpTimer = 180
        isOtpExpired = false
        timerJob = viewModelScope.launch {
            while (otpTimer > 0) {
                delay(1000)
                otpTimer--
            }
            isOtpExpired = true
            generatedOtp = null
        }
    }

    fun resendOtp(email: String) {
        val currentOtp = generatedOtp
        if (currentOtp == null) {
            // If for some reason we don't have one, generate new
            sendOtp(email, 1L, "") 
            return
        }

        viewModelScope.launch {
            try {
                // Just resend the email with existing OTP
                emailService.sendOtpEmail(email, currentOtp)
                AnalyticsManager.logEvent("otp_resent")
            } catch (e: Exception) {
                uiState = AuthUiState.Error("Failed to resend OTP: ${e.message}", isOtpError = true)
            }
        }
    }

    fun setGuestMode(guest: Boolean) {
        isGuest = guest
    }

    fun clearData() {
        currentCustomer = null
        uiState = AuthUiState.Idle
        isGuest = true
        failedAttempts = 0
        lockoutTimeRemaining = 0
        profilePicturePath = null
    }

    fun canUseBiometric(): Boolean {
        return securityManager.isBiometricAvailable(getApplication()) && securityManager.hasStoredCredentials()
    }

    fun sendOtp(email: String, customerTypeId: Long, systemPassword: String = "") {
        viewModelScope.launch {
            uiState = AuthUiState.Loading

            // System Password Check
            if (customerTypeId == 5L) {
                val expectedPassword = securityManager.getSystemPassword() 
                    ?: OtpManager.getSystemPassword(getApplication())

                if (systemPassword != expectedPassword) {
                    AnalyticsManager.logAuthError("signup_error", "Invalid system password")
                    uiState = AuthUiState.Error("Invalid system password. Access denied.")
                    return@launch
                }
            }

            try {
                // Step 1: Login as "signup" user to get registration token for existence check
                val signupAuth = "Basic " + Base64.encodeToString(
                    "signup:signup".toByteArray(),
                    Base64.NO_WRAP
                )
                val loginResult = retrofitManager.authService.login(signupAuth)
                val token = loginResult.loginAccessKey ?: throw Exception("Failed to get registration token")

                // Step 2: Check if email already exists
                try {
                    retrofitManager.customerService.getCustomerByEmail(email, "Bearer $token")
                    // If the call succeeds, it means the customer exists
                    uiState = AuthUiState.Error("This email address is already registered. Try logging in instead.")
                    return@launch
                } catch (e: HttpException) {
                    // 404 is expected if the customer does not exist, which is what we want for signup
                    if (e.code() != 404) {
                        throw e
                    }
                }

                // Step 3: If email is available, proceed to send OTP
                val otp = OtpManager.generateOtp()
                generatedOtp = otp
                
                // Save to .env as requested
                OtpManager.saveOtpToEnv(getApplication(), otp)
                
                // Send email
                emailService.sendOtpEmail(email, otp)
                
                AnalyticsManager.logEvent("otp_sent")
                uiState = AuthUiState.OtpSent(email)
                startOtpTimer()
            } catch (e: Exception) {
                AnalyticsManager.logAuthError("otp_error", e.localizedMessage ?: "Unknown error")
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
        accountTypeIds: List<Long>,
        otp: String,
        systemPassword: String = "",
        onNavigateBack: () -> Unit
    ) {
        if (isOtpExpired || generatedOtp == null) {
            uiState = AuthUiState.Error("OTP has expired. Please request a new one.", isOtpError = true)
            return
        }

        if (otp != generatedOtp) {
            uiState = AuthUiState.Error("Invalid OTP. Please check your email.", isOtpError = true)
            return
        }

        if (customerTypeId == 5L) {
            val expectedPassword = securityManager.getSystemPassword() 
                ?: OtpManager.getSystemPassword(getApplication())

            if (systemPassword != expectedPassword) {
                AnalyticsManager.logAuthError("signup_error", "Invalid system password")
                uiState = AuthUiState.Error("Invalid system password. Access denied.")
                return
            }
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
                val customer = retrofitManager.customerService.registerCustomer(
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

                // Step 3: Add the selected accounts to the customer
                accountTypeIds.forEach { id ->
                    retrofitManager.customerService.addAccountToCustomer(
                        customerId = customer.id,
                        accountTypeId = id,
                        authHeader = "Bearer $registrationToken"
                    )
                }

                if (accountTypeIds.isNotEmpty()) {
                    securityManager.saveActiveAccountTypeId(accountTypeIds.first())
                }

                AnalyticsManager.logSignUp("email")
                uiState = AuthUiState.Success("Account created successfully!")
                onNavigateBack()
            } catch (e: HttpException) {
                val errorMsg = when (e.code()) {
                    400 -> "This email address is already registered. Try logging in instead."
                    401 -> "Registration failed: Unauthorized. Please contact support."
                    else -> "Something went wrong on our end. Please try again later. (${e.code()})"
                }
                AnalyticsManager.logAuthError("signup_error", errorMsg)
                uiState = AuthUiState.Error(errorMsg)
            } catch (e: IOException) {
                AnalyticsManager.logAuthError("signup_error", "Network error")
                uiState = AuthUiState.Error("We're having trouble reaching our servers. Please check your connection.")
            } catch (e: Exception) {
                AnalyticsManager.logAuthError("signup_error", e.localizedMessage ?: "Unknown error")
                uiState = AuthUiState.Error("An unexpected error occurred. Please try again.")
            }
        }
    }

    fun login(username: String, password: String, onLoginSuccess: () -> Unit) {
        if (lockoutTimeRemaining > 0) {
            uiState = AuthUiState.Error("Too many failed attempts. Please wait $lockoutTimeRemaining seconds.")
            return
        }

        // Check profile limit BEFORE attempting login
        if (!profileManager.canAddProfile(username)) {
            uiState = AuthUiState.Error("Device limit reached. Only 5 profiles allowed per device.")
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

                    // Set default active account if not set
                    if (securityManager.getActiveAccountTypeId() == -1L) {
                        profile.customerAccounts?.firstOrNull()?.id?.let {
                            securityManager.saveActiveAccountTypeId(it)
                        }
                    }

                    // Add to profile manager
                    profileManager.addProfile(username)
                    
                    // Load profile picture
                    profilePicturePath = profileManager.getProfilePicture(username)

                    // Load notifications
                    val savedNotifications = profileManager.getNotifications(username)
                    NotificationViewModel.setNotifications(savedNotifications)

                    // Save credentials for biometric login if successful
                    securityManager.saveCredentials(username, password)
                    
                    AnalyticsManager.logLogin("email")
                    uiState = AuthUiState.Success("Welcome back!")
                    
                    com.example.productshop.ui.viewmodel.NotificationViewModel.addNotification(
                        "Welcome Back!",
                        "You have successfully logged in as ${currentCustomer?.firstName ?: "User"}."
                    )

                    onLoginSuccess()
                } else {
                    val message = result.errorMessage ?: "Incorrect email or password. Please try again."
                    AnalyticsManager.logAuthError("login_error", message)
                    handleLoginFailure(message)
                }
            } catch (e: HttpException) {
                val message = when (e.code()) {
                    401 -> "Incorrect email or password. Please try again."
                    403 -> "Your account is not authorized to access this resource."
                    else -> "Something went wrong. Please try again later."
                }
                AnalyticsManager.logAuthError("login_error", message)
                handleLoginFailure(message)
            } catch (e: IOException) {
                AnalyticsManager.logAuthError("login_error", "Network error")
                uiState = AuthUiState.Error("Connection error. Please check your internet and try again.", isOtpError = false)
            } catch (e: Exception) {
                AnalyticsManager.logAuthError("login_error", e.localizedMessage ?: "Unknown error")
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
            AnalyticsManager.logLogin("biometric")
            login(username, password, onLoginSuccess)
        } else {
            AnalyticsManager.logAuthError("biometric_error", "No stored credentials")
            uiState = AuthUiState.Error("No stored credentials found.", isOtpError = false)
        }
    }

    fun resetState() {
        uiState = AuthUiState.Idle
    }

    fun switchAccount(accountTypeId: Long) {
        securityManager.saveActiveAccountTypeId(accountTypeId)
        // Optionally refresh profile if needed, but since active account is local, 
        // we might just need to trigger a UI refresh or a profile reload.
        viewModelScope.launch {
            uiState = AuthUiState.Loading
            delay(500) // Simulate switching
            uiState = AuthUiState.Success("Account switched successfully")
            
            com.example.productshop.ui.viewmodel.NotificationViewModel.addNotification(
                "Account Switched",
                "You are now using your ${currentCustomer?.customerAccounts?.find { it.id == accountTypeId }?.name ?: "selected"} account."
            )
        }
    }

    fun updateProfilePicture(path: String) {
        val username = currentCustomer?.email ?: return
        profilePicturePath = path
        profileManager.saveProfilePicture(username, path)
    }
}
