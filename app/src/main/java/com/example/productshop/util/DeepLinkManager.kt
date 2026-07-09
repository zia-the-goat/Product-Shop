package com.example.productshop.util

import android.net.Uri
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object DeepLinkManager {
    private val _deepLinkUri = MutableSharedFlow<Uri>(extraBufferCapacity = 1)
    val deepLinkUri = _deepLinkUri.asSharedFlow()

    fun handleDeepLink(uri: Uri?) {
        uri?.let {
            _deepLinkUri.tryEmit(it)
        }
    }
}
