package com.example.productshop.data.remote

import com.example.productshop.data.model.CreateCustomerDto
import com.example.productshop.data.model.CustomerDto
import retrofit2.http.*

interface CustomerService {
    @POST("v1/customer")
    suspend fun registerCustomer(@Header("Authorization") authHeader: String, @Body request: CreateCustomerDto): CustomerDto

    @GET("v1/customer")
    suspend fun getCustomerByEmail(@Query("emailAddress") email: String, @Header("Authorization") authHeader: String): CustomerDto

    @POST("v1/customer/{customerId}/accounts/{accountTypeId}")
    suspend fun addAccountToCustomer(
        @Path("customerId") customerId: Long,
        @Path("accountTypeId") accountTypeId: Long,
        @Header("Authorization") authHeader: String
    )
}
