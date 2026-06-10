package com.example.productshop.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.productshop.data.model.ProductDto
import com.example.productshop.data.remote.RetrofitManager

class ProductViewModel : ViewModel() {
    var products by mutableStateOf<List<ProductDto>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Default to your working ngrok URL
    val retrofitManager = RetrofitManager()

    suspend fun fetchProducts() {
        isLoading = true
        try {
            val response = retrofitManager.service.getProducts()
            // Fix image URLs if they contain localhost or are relative
            products = response.map { product ->
                product.copy(imageUrl = fixImageUrl(product.imageUrl))
            }
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = "Failed to load products: ${e.message}"
        } finally {
            isLoading = false
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
