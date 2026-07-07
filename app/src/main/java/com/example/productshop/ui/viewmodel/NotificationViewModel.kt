package com.example.productshop.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.productshop.data.model.NotificationDto
import java.util.UUID

class NotificationViewModel : ViewModel() {
    companion object {
        private val _notifications = mutableStateListOf<NotificationDto>()
        val notifications: List<NotificationDto> get() = _notifications
        private const val MAX_NOTIFICATIONS = 100
        private var profileManager: com.example.productshop.security.ProfileManager? = null

        fun init(manager: com.example.productshop.security.ProfileManager) {
            profileManager = manager
        }

        fun addNotification(title: String, message: String) {
            // Check if notifications are enabled before adding
            val context = profileManager?.getContext() // We need a context to check prefs
            val prefs = context?.getSharedPreferences("settings_prefs", android.content.Context.MODE_PRIVATE)
            val enabled = prefs?.getBoolean("notifications_enabled", true) ?: true
            
            if (!enabled) return

            _notifications.add(0, NotificationDto(
                id = java.util.UUID.randomUUID().toString(),
                title = title,
                message = message
            ))
            // Enforce limit
            if (_notifications.size > MAX_NOTIFICATIONS) {
                _notifications.removeRange(MAX_NOTIFICATIONS, _notifications.size)
            }
            saveCurrentNotifications()
        }

        fun setNotifications(newList: List<NotificationDto>) {
            _notifications.clear()
            _notifications.addAll(newList.take(MAX_NOTIFICATIONS))
        }

        private fun saveCurrentNotifications() {
            val username = AuthViewModel.currentCustomer?.email ?: return
            profileManager?.saveNotifications(username, _notifications.toList())
        }
    }

    val notifications: List<NotificationDto> get() = Companion.notifications

    fun markAsRead(id: String) {
        val index = _notifications.indexOfFirst { it.id == id }
        if (index != -1) {
            _notifications[index] = _notifications[index].copy(isRead = true)
        }
    }

    fun clearAll() {
        _notifications.clear()
    }
}
