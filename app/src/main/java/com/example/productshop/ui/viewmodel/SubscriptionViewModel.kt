package com.example.productshop.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productshop.data.model.SubscriptionDto
import com.example.productshop.data.remote.RetrofitManager
import com.example.productshop.security.SessionManager
import kotlinx.coroutines.launch

sealed interface SubscriptionUiState {
    object Idle : SubscriptionUiState
    object Loading : SubscriptionUiState
    data class Success(val subscriptions: List<SubscriptionDto>) : SubscriptionUiState
    data class Error(val message: String) : SubscriptionUiState
}

class SubscriptionViewModel : ViewModel() {
    private val retrofitManager = RetrofitManager()
    
    var uiState: SubscriptionUiState by mutableStateOf(SubscriptionUiState.Idle)
        private set

    fun clearData() {
        uiState = SubscriptionUiState.Idle
    }

    private val authHeader: String
        get() = "Bearer ${SessionManager.bearerToken}"

    fun fetchSubscriptions() {
        val currentProfileId = KycViewModel.profileId // We'll need to share the profile ID
        if (currentProfileId == -1L) return

        viewModelScope.launch {
            uiState = SubscriptionUiState.Loading
            try {
                val response = retrofitManager.service.getCustomerSubscriptions(currentProfileId, authHeader)
                uiState = SubscriptionUiState.Success(response)
            } catch (e: Exception) {
                uiState = SubscriptionUiState.Error("Failed to load subscriptions: ${e.message}")
            }
        }
    }
}
