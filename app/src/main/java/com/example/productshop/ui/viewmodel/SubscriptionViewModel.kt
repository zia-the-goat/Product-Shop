package com.example.productshop.ui.viewmodel

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productshop.data.model.SubscriptionDto
import com.example.productshop.data.remote.RetrofitManager
import com.example.productshop.security.ProfileManager
import com.example.productshop.security.SecurityManager
import com.example.productshop.security.SessionManager
import com.example.productshop.util.AnalyticsManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed interface SubscriptionUiState {
    object Idle : SubscriptionUiState
    object Loading : SubscriptionUiState
    data class Success(val subscriptions: List<SubscriptionDto>) : SubscriptionUiState
    data class Error(val message: String) : SubscriptionUiState
}

class SubscriptionViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    private val retrofitManager = RetrofitManager()
    private val profileManager = ProfileManager(application)
    
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
                AnalyticsManager.logSubscriptionLoaded(response.size)
                
                // Map to inject local dates if backend ones are missing or placeholder
                val username = AuthViewModel.currentCustomer?.email
                val enrichedSubscriptions = response.map { sub ->
                    if (username != null) {
                        val savedDate = profileManager.getSubscriptionDate(username, sub.subscriptionId)
                        if (savedDate != 0L) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            sub.copy(dateSubscribed = sdf.format(Date(savedDate)))
                        } else sub
                    } else sub
                }
                
                uiState = SubscriptionUiState.Success(enrichedSubscriptions)
            } catch (e: Exception) {
                uiState = SubscriptionUiState.Error("Failed to load subscriptions: ${e.message}")
            }
        }
    }

    fun cancelSubscription(subscriptionId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            // We'll use a temporary loading state or just handle it silently then refresh
            try {
                retrofitManager.service.deleteCustomerSubscription(subscriptionId, authHeader)
                fetchSubscriptions() // Refresh list
                onComplete()
            } catch (e: Exception) {
                // For now just log, maybe add a specific cancellation error state later
                AnalyticsManager.logEvent("subscription_cancel_error", android.os.Bundle().apply {
                    putString("error", e.message)
                })
            }
        }
    }
}
