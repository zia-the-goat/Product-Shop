package com.example.productshop.ui.viewmodel

import android.os.Bundle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.productshop.data.model.*
import com.example.productshop.data.remote.RetrofitManager
import com.example.productshop.security.ProfileManager
import com.example.productshop.security.SecurityManager
import com.example.productshop.security.SessionManager
import com.example.productshop.util.AnalyticsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface FulfillmentUiState {
    object Idle : FulfillmentUiState
    object Loading : FulfillmentUiState
    object Verified : FulfillmentUiState
    data class Success(val subscriptionId: Long) : FulfillmentUiState
    data class Error(val message: String, val failedChecks: List<FulfilmentResultDto> = emptyList()) : FulfillmentUiState
}

class ProductViewModel(application: android.app.Application) : androidx.lifecycle.AndroidViewModel(application) {
    var products by mutableStateOf<List<ProductDto>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    private val profileManager = ProfileManager(application)
    private val securityManager = SecurityManager(application)
    
    // Search State
    var searchQuery by mutableStateOf("")
    val filteredProductsList: List<ProductDto>
        get() = if (searchQuery.isEmpty()) products else products.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.description.contains(searchQuery, ignoreCase = true)
        }
    
    // Default to your working ngrok URL
    val retrofitManager = RetrofitManager()

    suspend fun fetchProducts(forceRefresh: Boolean = false) {
        if (!forceRefresh && products.isNotEmpty()) return

        isLoading = true
        try {
            val response = retrofitManager.service.getProducts()
            // Fix image URLs if they contain localhost or are relative
            products = response.map { product ->
                product.copy(imageUrl = fixImageUrl(product.imageUrl))
            }
            AnalyticsManager.logSelectContent("product_list", "all")
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = "Failed to load products: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Fulfillment State
    var fulfillmentUiState: FulfillmentUiState by mutableStateOf(FulfillmentUiState.Idle)
    var selectedProductForFulfillment by mutableStateOf<ProductDto?>(null)
    private var storedSubscriptionId: Long? = null
    
    // Contract Flow Data
    var selectedColor by mutableStateOf("Titanium Gray")
    var selectedStorage by mutableStateOf("256GB")
    var selectedContractTerm by mutableStateOf("24 Months")
    
    // Investment Flow Data
    var investmentGoal by mutableStateOf("Retirement")
    var riskTolerance by mutableStateOf(0.5f) // 0 to 1
    var fundingAccount by mutableStateOf("")

    // Insurance Flow Data
    var assetDescription by mutableStateOf("")
    var selectedExcess by mutableStateOf(500.0)
    var coverageLimit by mutableStateOf(10000.0)

    // Payment Info (Simulated)
    var cardNumber by mutableStateOf("")
    var cardExpiry by mutableStateOf("")
    var cardCvv by mutableStateOf("")
    var isPaymentVerified by mutableStateOf(false)

    fun startFulfillment(product: ProductDto) {
        selectedProductForFulfillment = product
        fulfillmentUiState = FulfillmentUiState.Idle
        isPaymentVerified = false
        
        // Save initial progress if not already set
        AuthViewModel.currentCustomer?.email?.let { username ->
            val accountTypeId = securityManager.getActiveAccountTypeId()
            val savedStep = profileManager.getSubscriptionProgress(username, accountTypeId, product.id)
            // Note: currentStep is handled in the UI
        }
    }

    fun saveProgress(step: Int) {
        val username = AuthViewModel.currentCustomer?.email ?: return
        val productId = selectedProductForFulfillment?.id ?: return
        val accountTypeId = securityManager.getActiveAccountTypeId()
        profileManager.saveSubscriptionProgress(username, accountTypeId, productId, step)
    }

    fun saveDate() {
        val username = AuthViewModel.currentCustomer?.email ?: return
        val subscriptionId = storedSubscriptionId ?: return
        profileManager.saveSubscriptionDate(username, subscriptionId, System.currentTimeMillis())
    }

    fun getSavedProgress(productId: Long): Int {
        val username = AuthViewModel.currentCustomer?.email ?: return 1
        val accountTypeId = securityManager.getActiveAccountTypeId()
        return profileManager.getSubscriptionProgress(username, accountTypeId, productId)
    }

    fun resetFulfillmentState() {
        fulfillmentUiState = FulfillmentUiState.Idle
        cardNumber = ""
        cardExpiry = ""
        cardCvv = ""
        isPaymentVerified = false
        storedSubscriptionId = null
    }

    fun getQualifyingConstraints(productName: String): Pair<List<String>, List<String>> {
        return when {
            productName.contains("Retail", ignoreCase = true) && productName.contains("Insurance", ignoreCase = true) -> 
                listOf("INDIVIDUAL") to listOf("Gold Cheque Account", "Platinum Cheque Account", "Signet Cheque Account", "Islamic Cheque Account")
            
            productName.contains("Commercial", ignoreCase = true) && productName.contains("Insurance", ignoreCase = true) -> 
                listOf("SOLE PROP", "NON-PROFIT", "CIPC") to listOf("SME Checking Account", "Medium Enterprise Checking Account", "Large Enterprise Checking Account")
            
            productName.contains("Device Contract", ignoreCase = true) -> 
                listOf("INDIVIDUAL", "SOLE PROP", "NON-PROFIT", "CIPC") to listOf("Gold Cheque Account", "Platinum Cheque Account", "Signet Cheque Account", "Islamic Cheque Account", "Savings Account")
            
            (productName.contains("Short-Term", ignoreCase = true) || productName.contains("Long-Term", ignoreCase = true)) && productName.contains("Investment", ignoreCase = true) -> 
                listOf("INDIVIDUAL", "SOLE PROP", "NON-PROFIT", "CIPC") to listOf("Gold Cheque Account", "Platinum Cheque Account", "Islamic Cheque Account")
            
            productName.contains("Islamic Investment", ignoreCase = true) -> 
                listOf("INDIVIDUAL", "NON-PROFIT") to listOf("Islamic Cheque Account")
            
            productName.contains("VIP Investment", ignoreCase = true) ->
                listOf("INDIVIDUAL") to listOf("Signet Cheque Account")
                
            else -> emptyList<String>() to emptyList<String>()
        }
    }

    fun validateEligibility(onSuccess: () -> Unit) {
        if (fulfillmentUiState is FulfillmentUiState.Verified) {
            onSuccess()
            return
        }

        val product = selectedProductForFulfillment ?: return
        val customerId = KycViewModel.profileId

        if (customerId == -1L) {
            fulfillmentUiState = FulfillmentUiState.Error("Please log in and complete KYC first.")
            return
        }

        viewModelScope.launch {
            fulfillmentUiState = FulfillmentUiState.Loading
            try {
                // Client-side validation of Customer and Account types
                val (qualifyingCustomerTypes, qualifyingAccountTypes) = getQualifyingConstraints(product.name)
                val userCustomerType = AuthViewModel.currentCustomer?.customerType?.name?.uppercase()

                val activeAccountTypeId = securityManager.getActiveAccountTypeId()
                val userAccountType = AuthViewModel.currentCustomer?.customerAccounts?.find { it.id == activeAccountTypeId }?.name

                val clientFailedChecks = mutableListOf<FulfilmentResultDto>()

                if (userCustomerType != null && qualifyingCustomerTypes.isNotEmpty()) {
                    if (!qualifyingCustomerTypes.any { it.contains(userCustomerType) || userCustomerType.contains(it) }) {
                        clientFailedChecks.add(FulfilmentResultDto("Customer Type", false, "Your customer type ($userCustomerType) is not eligible. Required: ${qualifyingCustomerTypes.joinToString(", ")}"))
                    }
                }

                if (userAccountType != null && qualifyingAccountTypes.isNotEmpty()) {
                    if (!qualifyingAccountTypes.any { it.contains(userAccountType) || userAccountType.contains(it) }) {
                        clientFailedChecks.add(FulfilmentResultDto("Account Type", false, "Your account type ($userAccountType) is not eligible. Required: ${qualifyingAccountTypes.joinToString(", ")}"))
                    }
                }

                if (clientFailedChecks.isNotEmpty()) {
                    fulfillmentUiState = FulfillmentUiState.Error("Verification Incomplete", clientFailedChecks)
                    return@launch
                }

                val authHeader = "Bearer ${SessionManager.bearerToken}"
                val request = ProductRequest(customerId, listOf(product.id))

                // Call actual eligibility check API
                val eligibilityResults = retrofitManager.service.eligibilityCheck(authHeader, request)
                val result = eligibilityResults.find { it.productId == product.id }

                if (result?.isEligible == true) {
                    fulfillmentUiState = FulfillmentUiState.Verified
                    onSuccess()
                } else {
                    val reasons = result?.failureReasons?.joinToString(". ") ?: "Unknown eligibility error"
                    fulfillmentUiState = FulfillmentUiState.Error("Verification Incomplete", listOf(FulfilmentResultDto("General Eligibility", false, reasons)))
                }
            } catch (e: Exception) {
                fulfillmentUiState = FulfillmentUiState.Error("Eligibility check failed: ${e.message}")
            }
        }
    }

    fun completeFulfillment(onComplete: (() -> Unit)? = null) {
        val product = selectedProductForFulfillment ?: return
        val customerId = KycViewModel.profileId

        if (customerId == -1L) {
            fulfillmentUiState = FulfillmentUiState.Error("Please log in and complete KYC first.")
            return
        }

        viewModelScope.launch {
            fulfillmentUiState = FulfillmentUiState.Loading
            try {
                val authHeader = "Bearer ${SessionManager.bearerToken}"
                val request = ProductRequest(customerId, listOf(product.id))

                android.util.Log.d("Fulfillment", "Fulfillment Request: customerId=$customerId, productIds=${request.productIds}")

                // Final step: calling take-up after payment
                val response = retrofitManager.service.productTakeUp(authHeader, request)
                android.util.Log.d("Fulfillment", "Backend Take-up Response: $response")

                val productType = com.example.productshop.ui.screens.ProductType.fromProduct(product)
                val requiredCheckPatterns = when (productType) {
                    com.example.productshop.ui.screens.ProductType.CONTRACT -> listOf("KYC")
                    com.example.productshop.ui.screens.ProductType.INVESTMENT -> listOf("KYC", "Living", "ID", "Fraud")
                    com.example.productshop.ui.screens.ProductType.INSURANCE -> listOf("KYC", "Fraud", "Living", "ID", "Credit", "Marital")
                }

                val failedRequiredResults = response.fulfilmentResultList.filter { result ->
                    val isRequired = requiredCheckPatterns.any { pattern ->
                        result.checkName.contains(pattern, ignoreCase = true)
                    }
                    var shouldFail = isRequired && !result.passed

                    if (SessionManager.isDebugMode) {
                        if (result.checkName.contains("Fraud", ignoreCase = true) ||
                            result.checkName.contains("Credit", ignoreCase = true)) {
                            shouldFail = false
                        }
                    }
                    shouldFail
                }

                if (failedRequiredResults.isEmpty()) {
                    val finalSubscriptionId = response.subscriptionId ?: (1000L..9999L).random()
                    storedSubscriptionId = finalSubscriptionId

                    fulfillmentUiState = FulfillmentUiState.Success(finalSubscriptionId)
                    saveDate()
                    NotificationViewModel.addNotification(
                        "Subscription Successful!",
                        "You have successfully subscribed to ${product.name}. Check 'Subscriptions' for details."
                    )

                    val bundle = Bundle().apply {
                        putLong("product_id", product.id)
                        putLong("subscription_id", finalSubscriptionId)
                    }
                    AnalyticsManager.logEvent("product_take_up_success", bundle)
                    onComplete?.invoke()
                } else {
                    fulfillmentUiState = FulfillmentUiState.Error("Subscription Incomplete", failedRequiredResults)
                }
            } catch (e: retrofit2.HttpException) {
                val errorBody = e.response()?.errorBody()?.string()
                android.util.Log.e("Fulfillment", "Fulfillment Error (HTTP ${e.code()}): $errorBody")
                fulfillmentUiState = FulfillmentUiState.Error("Subscription failed (Server Error). Please contact support.")
            } catch (e: Exception) {
                android.util.Log.e("Fulfillment", "Fulfillment Error: ${e.message}", e)
                fulfillmentUiState = FulfillmentUiState.Error("Take-up failed: ${e.message}")
            }
        }
    }

    private fun fixImageUrl(url: String): String {
        val ngrokBase = "https://boozy-supply-ripping.ngrok-free.dev"
        return when {
            url.startsWith("http://localhost:8080") -> url.replace("http://localhost:8080", ngrokBase)
            url.startsWith("http://10.0.2.2:8080") -> url.replace("http://10.0.2.2:8080", ngrokBase)
            url.startsWith("/") -> "$ngrokBase$url"
            else -> url
        }
    }
}
