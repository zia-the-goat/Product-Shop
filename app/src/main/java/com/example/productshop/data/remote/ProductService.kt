package com.example.productshop.data.remote

import com.example.productshop.data.model.EligibilityResultsDto
import com.example.productshop.data.model.ProductDto
import com.example.productshop.data.model.ProductRequest
import com.example.productshop.data.model.SubscriptionDto
import com.example.productshop.data.model.TakeUpResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ProductService {
    @GET("v1/products")
    suspend fun getProducts(): List<ProductDto>

    @GET("v1/subscriptions/customer/{customerId}")
    suspend fun getCustomerSubscriptions(
        @Path("customerId") customerId: Long,
        @Header("Authorization") authHeader: String
    ): List<SubscriptionDto>

    @POST("v1/product/eligibility")
    suspend fun eligibilityCheck(
        @Header("Authorization") authHeader: String,
        @Body request: ProductRequest
    ): List<EligibilityResultsDto>

    @POST("v1/product/take-up")
    suspend fun productTakeUp(
        @Header("Authorization") authHeader: String,
        @Body request: ProductRequest
    ): TakeUpResponse

    @DELETE("v1/subscriptions/{subscriptionId}")
    suspend fun deleteCustomerSubscription(
        @Path("subscriptionId") subscriptionId: Long,
        @Header("Authorization") authHeader: String
    )
}
