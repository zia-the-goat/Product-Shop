package com.example.productshop.data.model

data class SignupRequest(
    val username: String,
    val password: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val idNumber: String? = null
)

data class CreateCustomerDto(
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val idNumber: String,
    val customerTypeId: Long
)

data class LoginResult(
    val success: String,
    val errorMessage: String? = null,
    val loginAccessKey: String? = null
)
