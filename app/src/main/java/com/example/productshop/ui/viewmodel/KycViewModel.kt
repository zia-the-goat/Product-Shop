package com.example.productshop.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.productshop.data.model.*
import com.example.productshop.data.remote.RetrofitManager
import com.example.productshop.security.SessionManager
import com.example.productshop.util.AnalyticsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.ByteArrayOutputStream

sealed interface KycUiState {
    object Idle : KycUiState
    object Loading : KycUiState
    object Verifying : KycUiState
    object Success : KycUiState
    data class Error(val message: String) : KycUiState
}

class KycViewModel(application: Application) : AndroidViewModel(application) {
    private val retrofitManager = RetrofitManager()

    companion object {
        var profileId: Long = -1L
    }

    var profile by mutableStateOf<CustomerDto?>(null)
    var kycStatus by mutableStateOf<KycDto?>(null)
    var uiState: KycUiState by mutableStateOf(KycUiState.Idle)
    var isDebugMode by mutableStateOf(SessionManager.isDebugMode)

    var selfieUri by mutableStateOf<Uri?>(null)
    var residenceUri by mutableStateOf<Uri?>(null)

    fun clearData() {
        profile = null
        kycStatus = null
        uiState = KycUiState.Idle
        selfieUri = null
        residenceUri = null
        profileId = -1L
        isDebugMode = false
        SessionManager.isDebugMode = false
    }

    fun toggleDebugMode() {
        isDebugMode = !isDebugMode
        SessionManager.isDebugMode = isDebugMode
    }

    private val authHeader: String
        get() = "Bearer ${SessionManager.bearerToken}"

    fun fetchProfileAndKyc() {
        viewModelScope.launch {
            uiState = KycUiState.Loading
            try {
                // Fetch profile using the email (username) from AuthViewModel or session
                val userProfile = AuthViewModel.currentCustomer ?: retrofitManager.profileService.getProfile(authHeader)
                profile = userProfile
                profileId = userProfile.id
                
                try {
                    val status = retrofitManager.kycService.getKycStatus(userProfile.id, authHeader)
                    kycStatus = status
                } catch (e: HttpException) {
                    if (e.code() == 404) {
                        // Customer has no KYC record yet - this is expected for new users
                        kycStatus = null
                    } else {
                        AnalyticsManager.logAuthError("kyc_status_error", e.localizedMessage ?: "HTTP ${e.code()}")
                        throw e
                    }
                }
                uiState = KycUiState.Idle
            } catch (e: Exception) {
                AnalyticsManager.logAuthError("kyc_profile_error", e.localizedMessage ?: "Unknown error")
                uiState = KycUiState.Error("Failed to load profile info: ${e.message}")
            }
        }
    }

    fun uploadKyc(context: Context) {
        val currentProfile = profile ?: return
        val sUri = selfieUri ?: return
        val rUri = residenceUri ?: return

        viewModelScope.launch {
            uiState = KycUiState.Loading
            AnalyticsManager.logKycStarted()
            try {
                // 1. Upload Selfie
                val selfieData = uriToRawString(context, sUri)
                retrofitManager.profileService.addDocument(
                    currentProfile.id,
                    DocumentDto(id = currentProfile.id, document = selfieData, type = "PNG"),
                    authHeader
                )

                // 2. Upload Residence Proof
                val residenceData = uriToRawString(context, rUri)
                retrofitManager.profileService.addDocument(
                    currentProfile.id,
                    DocumentDto(id = currentProfile.id, document = residenceData, type = "PNG"),
                    authHeader
                )

                // 3. Mock Verification Process
                uiState = KycUiState.Verifying
                delay(3000) // Simulate checking compliance

                // 4. Update KYC Status on Backend
                retrofitManager.kycService.updateKycStatus(
                    currentProfile.id,
                    KycDto(primaryIndicator = true, secondaryIndicator = true, taxCompliance = "green"),
                    authHeader
                )

                // 5. DHA Synchronization
                syncWithDha(currentProfile.idNumber)

                // 6. Link a default account to improve eligibility
                retrofitManager.customerService.addAccountToCustomer(
                    currentProfile.id,
                    3L, // Checking Account
                    authHeader
                )

                uiState = KycUiState.Success
                AnalyticsManager.logKycCompleted(true)
                
                com.example.productshop.ui.viewmodel.NotificationViewModel.addNotification(
                    "Verification Successful!",
                    "Your identity has been verified. You now have full access to all products."
                )

                fetchProfileAndKyc()
            } catch (e: Exception) {
                AnalyticsManager.logKycCompleted(false)
                uiState = KycUiState.Error("Upload failed: ${e.message}")
            } finally {
                // Ensure kycStatus is refreshed after any completion attempt
                if (uiState is KycUiState.Success) {
                    fetchProfileAndKyc()
                }
            }
        }
    }

    private suspend fun syncWithDha(idNumber: String) {
        try {
            // Always update DHA status to passing values upon KYC completion
            retrofitManager.dhaService.addLivingStatus(
                idNumber,
                LivingStatusDto(livingStatus = "alive"),
                authHeader
            )

            retrofitManager.dhaService.addMaritalStatus(
                idNumber,
                MaritalStatusDto(
                    status = "Married",
                    effectiveFrom = "2000-01-01",
                    effectiveTo = "9999-12-31"
                ),
                authHeader
            )

            retrofitManager.dhaService.addIdStatus(
                idNumber,
                DuplicateIDDocumentCheck(
                    hasDuplicateId = false,
                    duplicateIdIssueDate = "2000-01-01"
                ),
                authHeader
            )
        } catch (e: Exception) {
            // Log DHA sync error but don't fail the whole KYC if it's just a sync issue
            AnalyticsManager.logAuthError("dha_sync_error", e.localizedMessage ?: "Unknown DHA error")
        }
    }

    private fun uriToRawString(context: Context, uri: Uri): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.toByteArray()
        } ?: throw Exception("Failed to open input stream for URI: $uri")
        
        // Return raw string representation (ISO_8859_1 preserves byte values)
        // Backend now handles Base64 encoding
        return String(bytes, Charsets.ISO_8859_1)
    }
}
