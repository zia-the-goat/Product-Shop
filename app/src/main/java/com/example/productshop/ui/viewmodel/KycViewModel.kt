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
import com.example.productshop.data.model.CustomerDto
import com.example.productshop.data.model.DocumentDto
import com.example.productshop.data.model.KycDto
import com.example.productshop.data.remote.RetrofitManager
import com.example.productshop.security.SessionManager
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

    var selfieUri by mutableStateOf<Uri?>(null)
    var residenceUri by mutableStateOf<Uri?>(null)

    fun clearData() {
        profile = null
        kycStatus = null
        uiState = KycUiState.Idle
        selfieUri = null
        residenceUri = null
        profileId = -1L
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
                        throw e
                    }
                }
                uiState = KycUiState.Idle
            } catch (e: Exception) {
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
            try {
                // 1. Upload Selfie
                val selfieBase64 = uriToDataUriBase64(context, sUri)
                retrofitManager.profileService.addDocument(
                    currentProfile.id,
                    DocumentDto(id = currentProfile.id, document = selfieBase64, type = "PNG"),
                    authHeader
                )

                // 2. Upload Residence Proof
                val residenceBase64 = uriToDataUriBase64(context, rUri)
                retrofitManager.profileService.addDocument(
                    currentProfile.id,
                    DocumentDto(id = currentProfile.id, document = residenceBase64, type = "PNG"),
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

                uiState = KycUiState.Success
                fetchProfileAndKyc()
            } catch (e: Exception) {
                uiState = KycUiState.Error("Upload failed: ${e.message}")
            }
        }
    }

    private fun uriToDataUriBase64(context: Context, uri: Uri): String {
        val bytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.toByteArray()
        } ?: throw Exception("Failed to open input stream for URI: $uri")
        
        // Convert image to Base64
        val imageBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        // Create Data URI string
        val dataUri = "data:image/png;base64,$imageBase64"
        
        // The backend expects the entire Data URI string to be Base64 encoded
        return Base64.encodeToString(dataUri.toByteArray(), Base64.NO_WRAP)
    }
}
