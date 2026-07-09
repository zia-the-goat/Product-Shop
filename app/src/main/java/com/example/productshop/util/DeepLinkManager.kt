package com.example.productshop.util

import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DeepLinkManager {
    private val _deepLinkUri = MutableStateFlow<Uri?>(null)
    val deepLinkUri = _deepLinkUri.asStateFlow()

    fun handleDeepLink(uri: Uri?) {
        uri?.let {
            _deepLinkUri.value = it
        }
    }

    fun consumeDeepLink() {
        _deepLinkUri.value = null
    }
}
