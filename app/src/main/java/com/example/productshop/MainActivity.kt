package com.example.productshop

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.productshop.ui.navigation.ProductNavHost
import com.example.productshop.security.SessionManager
import com.example.productshop.util.AnalyticsManager
import com.example.productshop.ui.viewmodel.AuthViewModel
import com.example.productshop.ui.viewmodel.KycViewModel
import com.example.productshop.ui.viewmodel.ProductViewModel
import com.example.productshop.ui.viewmodel.SubscriptionViewModel
import com.example.productshop.ui.viewmodel.SettingsViewModel
import com.example.productshop.ui.theme.ProductShopTheme

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle navigation bar visibility
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        
        // Note: In a real app, we might want to check for gesture navigation.
        // For this requirement, we hide the nav bar if it's the "normal" one.
        // We'll use a listener to hide it when it appears if we are in immersive mode.
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.navigationBars())

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            ProductShopTheme(
                themeMode = settingsViewModel.themeMode,
                brandTheme = settingsViewModel.brandTheme
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProductApp(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}

@Composable
fun ProductApp(
    viewModel: ProductViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    kycViewModel: KycViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel(),
    notificationViewModel: com.example.productshop.ui.viewmodel.NotificationViewModel = viewModel(),
    settingsViewModel: SettingsViewModel
) {
    val navController = rememberNavController()

    ProductNavHost(
        navController = navController,
        productViewModel = viewModel,
        authViewModel = authViewModel,
        kycViewModel = kycViewModel,
        subscriptionViewModel = subscriptionViewModel,
        notificationViewModel = notificationViewModel,
        settingsViewModel = settingsViewModel
    )
}
