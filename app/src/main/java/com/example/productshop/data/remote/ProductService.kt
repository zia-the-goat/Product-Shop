package com.example.productshop.data.remote

import com.example.productshop.data.model.ProductDto
import retrofit2.http.GET

interface ProductService {
    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}
