package com.example.productshop.ui.viewmodel

import android.app.Application
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.productshop.data.model.SignupRequest
import com.example.productshop.data.remote.RetrofitManager
import com.example.productshop.security.SecurityManager
import com.example.productshop.security.SessionManager
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val retrofitManager = RetrofitManager()
    private val securityManager = SecurityManager(application)
    
    var uiState: AuthUiState by mutableStateOf(AuthUiState.Idle)
        private set

    var isBiometricPromptVisible by mutableStateOf(false)
    
    fun canUseBiometric(): Boolean {
        return securityManager.isBiometricAvailable(getApplication()) && securityManager.hasStoredCredentials()
    }

    fun signup(
        username: String,
        password: String,
        firstName: String,
        lastName: String,
        idNumber: String,
        onNavigateBack: () -> Unit
    ) {
        viewModelScope.launch {
            uiState = AuthUiState.Loading
            try {
                retrofitManager.authService.createUser(
                    SignupRequest(
                        username = username,
                        password = password,
                        firstName = firstName,
                        lastName = lastName,
                        idNumber = idNumber
                    )
                )
                uiState = AuthUiState.Success("Account created successfully!")
                onNavigateBack()
            } catch (e: HttpException) {
                uiState = when (e.code()) {
                    400 -> AuthUiState.Error("This email address is already registered. Try logging in instead.")
                    else -> AuthUiState.Error("Something went wrong on our end. Please try again later.")
                }
            } catch (e: IOException) {
                uiState = AuthUiState.Error("We're having trouble reaching our servers. Please check your connection.")
            } catch (e: Exception) {
                uiState = AuthUiState.Error("An unexpected error occurred. Please try again.")
            }
        }
    }

    fun login(username: String, password: String, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = AuthUiState.Loading
            try {
                val authHeader = "Basic " + Base64.encodeToString(
                    "$username:$password".toByteArray(),
                    Base64.NO_WRAP
                )
                val result = retrofitManager.authService.login(authHeader)
                if (result.success == "true") {
                    // Save credentials for biometric login if successful
                    securityManager.saveCredentials(username, password)
                    // Save volatile token
                    result.loginAccessKey?.let { SessionManager.setToken(it) }
                    
                    uiState = AuthUiState.Success("Welcome back!")
                    onLoginSuccess()
                } else {
                    uiState = AuthUiState.Error(result.errorMessage ?: "Incorrect email or password. Please try again.")
                }
            } catch (e: HttpException) {
                uiState = when (e.code()) {
                    401 -> AuthUiState.Error("Incorrect email or password. Please try again.")
                    403 -> AuthUiState.Error("Your account is not authorized to access this resource.")
                    else -> AuthUiState.Error("Something went wrong. Please try again later.")
                }
            } catch (e: IOException) {
                uiState = AuthUiState.Error("Connection error. Please check your internet and try again.")
            } catch (e: Exception) {
                uiState = AuthUiState.Error("Login failed. Please check your credentials.")
            }
        }
    }

    fun onBiometricSuccess(onLoginSuccess: () -> Unit) {
        val (username, password) = securityManager.getCredentials()
        if (username != null && password != null) {
            login(username, password, onLoginSuccess)
        } else {
            uiState = AuthUiState.Error("No stored credentials found.")
        }
    }
    
    fun resetState() {
        uiState = AuthUiState.Idle
    }
}
