package com.example.productshop.security

object SessionManager {
    var bearerToken: String? = null
        private set

    fun setToken(token: String) {
        bearerToken = token
    }

    fun clearSession() {
        bearerToken = null
    }

    fun hasToken(): Boolean = bearerToken != null
}
