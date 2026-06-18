package com.example.productshop.data.remote

import com.example.productshop.data.model.KycDto
import retrofit2.http.*

interface KycService {
    @GET("v1/kyc/{customerId}")
    suspend fun getKycStatus(
        @Path("customerId") customerId: Long,
        @Header("Authorization") authHeader: String
    ): KycDto

    @POST("v1/kyc/{customerId}")
    suspend fun updateKycStatus(
        @Path("customerId") customerId: Long,
        @Body kyc: KycDto,
        @Header("Authorization") authHeader: String
    )
}
