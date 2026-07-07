package com.example.productshop.security

object SessionManager {
    var bearerToken: String? = null
        private set

    var isDebugMode: Boolean = false

    var lastRoute: String? = null

    fun setToken(token: String) {
        bearerToken = token
    }

    fun clearSession() {
        bearerToken = null
        lastRoute = null
    }

    fun hasToken(): Boolean = bearerToken != null
}
