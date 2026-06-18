package com.example.productshop.data.remote

import com.example.productshop.data.model.LoginResult
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("v1/token")
    suspend fun login(@Header("Authorization") authHeader: String): LoginResult

    @POST("v1/token/validate")
    suspend fun validateToken(@Header("Authorization") authHeader: String)
}
