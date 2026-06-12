package com.example.productshop.data.remote

import com.example.productshop.data.model.LoginResult
import com.example.productshop.data.model.SignupRequest
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthService {
    @POST("user")
    suspend fun createUser(@Body request: SignupRequest)

    @POST("token")
    suspend fun login(@Header("Authorization") authHeader: String): LoginResult

    @POST("token/validate")
    suspend fun validateToken(@Header("Authorization") authHeader: String)
}
