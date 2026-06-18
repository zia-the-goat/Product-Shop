package com.example.productshop.data.remote

import com.example.productshop.data.model.CreateDocumentResponse
import com.example.productshop.data.model.CustomerDto
import com.example.productshop.data.model.DocumentDto
import retrofit2.http.*

interface ProfileService {
    @GET("client/v1/profile")
    suspend fun getProfile(@Header("Authorization") authHeader: String): CustomerDto

    @POST("v1/customer/{customerId}/documents")
    suspend fun addDocument(
        @Path("customerId") customerId: Long,
        @Body document: DocumentDto,
        @Header("Authorization") authHeader: String
    ): CreateDocumentResponse

    @GET("client/v1/profile/documents")
    suspend fun getDocuments(@Header("Authorization") authHeader: String): List<DocumentDto>
}
