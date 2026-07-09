package com.example.productshop.security

import android.content.Context

object SessionManager {
    private var securityManager: SecurityManager? = null

    var bearerToken: String? = null
        private set

    var isDebugMode: Boolean = false

    var lastRoute: String? = null

    var pendingProductId: Long? = null

    fun initialize(context: Context) {
        securityManager = SecurityManager(context)
        bearerToken = securityManager?.getToken()
    }

    fun setToken(token: String) {
        bearerToken = token
        securityManager?.saveToken(token)
    }

    fun clearSession() {
        bearerToken = null
        lastRoute = null
        pendingProductId = null
        securityManager?.clearToken()
    }

    fun hasToken(): Boolean = bearerToken != null
}
