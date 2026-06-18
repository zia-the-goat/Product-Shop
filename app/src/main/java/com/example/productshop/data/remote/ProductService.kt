package com.example.productshop.data.remote

import com.example.productshop.data.model.ProductDto
import com.example.productshop.data.model.SubscriptionDto
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ProductService {
    @GET("v1/products")
    suspend fun getProducts(): List<ProductDto>

    @GET("v1/subscriptions/customer/{customerId}")
    suspend fun getCustomerSubscriptions(
        @Path("customerId") customerId: Long,
        @Header("Authorization") authHeader: String
    ): List<SubscriptionDto>
}
