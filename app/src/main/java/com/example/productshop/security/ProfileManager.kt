package com.example.productshop.security

import android.content.Context
import android.os.Bundle
import com.example.productshop.data.model.NotificationDto
import com.example.productshop.util.AnalyticsManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProfileManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("profile_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getContext(): Context = context

    companion object {
        private const val KEY_PROFILES = "profiles_list"
        private const val MAX_PROFILES = 10
        private const val MAX_NOTIFICATIONS = 100
    }

    /**
     * Returns the list of registered profile usernames on this device.
     */
    fun getProfiles(): List<String> {
        val json = prefs.getString(KEY_PROFILES, null) ?: return emptyList()
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    /**
     * Adds a profile if there's space. Returns true if successful or if already exists.
     */
    fun addProfile(username: String): Boolean {
        val profiles = getProfiles().toMutableList()
        if (profiles.contains(username)) return true
        if (profiles.size >= MAX_PROFILES) return false
        
        profiles.add(username)
        prefs.edit().putString(KEY_PROFILES, gson.toJson(profiles)).apply()
        return true
    }

    /**
     * Checks if a new profile can be added.
     */
    fun canAddProfile(username: String): Boolean {
        val profiles = getProfiles()
        return profiles.contains(username) || profiles.size < MAX_PROFILES
    }

    // --- Notifications ---

    fun saveNotifications(username: String, notifications: List<NotificationDto>) {
        val limited = notifications.take(MAX_NOTIFICATIONS)
        val json = gson.toJson(limited)
        prefs.edit().putString("notifications_$username", json).apply()
    }

    fun getNotifications(username: String): List<NotificationDto> {
        val json = prefs.getString("notifications_$username", null) ?: return emptyList()
        val type = object : TypeToken<List<NotificationDto>>() {}.type
        return gson.fromJson(json, type)
    }

    // --- Subscription Progress ---

    fun saveSubscriptionProgress(username: String, accountTypeId: Long, productId: Long, step: Int) {
        prefs.edit().putInt("progress_${username}_${accountTypeId}_$productId", step).apply()
    }

    fun getSubscriptionProgress(username: String, accountTypeId: Long, productId: Long): Int {
        return prefs.getInt("progress_${username}_${accountTypeId}_$productId", 1)
    }

    // --- Subscription Dates ---

    fun saveSubscriptionDate(username: String?, subscriptionId: Long, date: Long) {
        prefs.edit().putLong("sub_date_${username}_$subscriptionId", date).apply()
        AnalyticsManager.logEvent("Subscription_Date", Bundle().apply { putLong("Date: ",date) })

    }

    fun getSubscriptionDate(username: String, subscriptionId: Long): Long {
        return prefs.getLong("sub_date_${username}_$subscriptionId", 0L)
    }

    // --- Profile Picture ---

    fun saveProfilePicture(username: String, path: String) {
        prefs.edit().putString("profile_pic_$username", path).apply()
    }

    fun getProfilePicture(username: String): String? {
        return prefs.getString("profile_pic_$username", null)
    }
}
