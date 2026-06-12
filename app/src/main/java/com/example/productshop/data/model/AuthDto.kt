package com.example.productshop.data.model

data class SignupRequest(
    val username: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val idNumber: String? = null
)

data class LoginResult(
    val success: String,
    val errorMessage: String? = null,
    val loginAccessKey: String? = null
)
