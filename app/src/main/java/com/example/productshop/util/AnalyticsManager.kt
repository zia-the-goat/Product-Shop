package com.example.productshop.util

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsManager {
    private const val TAG = "AnalyticsManager"
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    fun initialize(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
        Log.d(TAG, "Analytics initialized")
    }

    fun logEvent(name: String, params: Bundle? = null) {
        Log.d(TAG, "Logging Event: $name | Params: ${params ?: "None"}")
        if (::firebaseAnalytics.isInitialized) {
            firebaseAnalytics.logEvent(name, params)
        }
    }

    fun logScreenView(screenName: String, screenClass: String? = null) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: screenName)
        }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logLogin(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun logSignUp(method: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)
    }

    fun logAuthError(errorType: String, message: String) {
        val bundle = Bundle().apply {
            putString("error_type", errorType)
            putString("error_message", message)
        }
        logEvent("auth_error", bundle)
    }

    fun logSelectContent(contentType: String, itemId: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
        }
        logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    fun logAddToCart(itemId: String, itemName: String, price: Double) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
            putString(FirebaseAnalytics.Param.ITEM_NAME, itemName)
            putDouble(FirebaseAnalytics.Param.PRICE, price)
            putString(FirebaseAnalytics.Param.CURRENCY, "ZAR")
        }
        logEvent(FirebaseAnalytics.Event.ADD_TO_CART, bundle)
    }

    fun logKycStarted() {
        logEvent("kyc_started")
    }

    fun logKycCompleted(success: Boolean) {
        val bundle = Bundle().apply {
            putBoolean("success", success)
        }
        logEvent("kyc_completed", bundle)
    }

    fun logSubscriptionLoaded(count: Int) {
        val bundle = Bundle().apply {
            putInt("count", count)
        }
        logEvent("subscriptions_loaded", bundle)
    }
}
