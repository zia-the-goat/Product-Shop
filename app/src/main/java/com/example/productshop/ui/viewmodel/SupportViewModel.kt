package com.example.productshop.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productshop.util.EmailService
import kotlinx.coroutines.launch

sealed interface SupportUiState {
    object Idle : SupportUiState
    object Loading : SupportUiState
    object Success : SupportUiState
    data class Error(val message: String) : SupportUiState
}

class SupportViewModel : ViewModel() {
    private val emailService = EmailService()
    
    var uiState: SupportUiState by mutableStateOf(SupportUiState.Idle)
        private set

    var subject by mutableStateOf("")
    var message by mutableStateOf("")

    fun sendSupportRequest(onSuccess: () -> Unit) {
        val userEmail = AuthViewModel.currentCustomer?.email ?: "Guest"
        if (subject.isBlank() || message.isBlank()) {
            uiState = SupportUiState.Error("Please fill in both subject and message.")
            return
        }

        viewModelScope.launch {
            uiState = SupportUiState.Loading
            try {
                emailService.sendSupportEmail(userEmail, subject, message)
                uiState = SupportUiState.Success
                subject = ""
                message = ""
                onSuccess()
            } catch (e: Exception) {
                uiState = SupportUiState.Error("Failed to send message: ${e.message}")
            }
        }
    }

    fun resetState() {
        uiState = SupportUiState.Idle
    }
}
