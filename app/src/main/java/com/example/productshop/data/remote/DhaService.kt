package com.example.productshop.data.remote

import com.example.productshop.data.model.DuplicateIDDocumentCheck
import com.example.productshop.data.model.LivingStatusDto
import com.example.productshop.data.model.MaritalStatusDto
import com.example.productshop.data.model.MaritalStatusList
import retrofit2.http.*

interface DhaService {
    @GET("v1/status/marital/{idNumber}")
    suspend fun getMaritalStatus(
        @Path("idNumber") idNumber: String,
        @Header("Authorization") authHeader: String
    ): MaritalStatusList

    @POST("v1/status/marital/{idNumber}")
    suspend fun addMaritalStatus(
        @Path("idNumber") idNumber: String,
        @Body status: MaritalStatusDto,
        @Header("Authorization") authHeader: String
    )

    @GET("v1/status/duplicateId/{idNumber}")
    suspend fun getDuplicateIdStatus(
        @Path("idNumber") idNumber: String,
        @Header("Authorization") authHeader: String
    ): DuplicateIDDocumentCheck

    @POST("v1/status/duplicateId/{idNumber}")
    suspend fun addIdStatus(
        @Path("idNumber") idNumber: String,
        @Body status: DuplicateIDDocumentCheck,
        @Header("Authorization") authHeader: String
    )

    @GET("v1/status/living/{idNumber}")
    suspend fun getLivingStatus(
        @Path("idNumber") idNumber: String,
        @Header("Authorization") authHeader: String
    ): LivingStatusDto

    @POST("v1/status/living/{idNumber}")
    suspend fun addLivingStatus(
        @Path("idNumber") idNumber: String,
        @Body status: LivingStatusDto,
        @Header("Authorization") authHeader: String
    )
}
