package com.example.productshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productshop.ui.screens.DiscoverScreen
import com.example.productshop.ui.screens.LandingScreen
import com.example.productshop.ui.screens.LoginScreen
import com.example.productshop.ui.screens.ProductDetailScreen
import com.example.productshop.ui.screens.SplashScreen
import com.example.productshop.ui.viewmodel.ProductViewModel

private val AppBlueTheme = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBBDEFB),
    onPrimaryContainer = Color(0xFF0D47A1),
    secondary = Color(0xFF42A5F5),
    onSecondary = Color.White,
    background = Color(0xFFF5F9FF),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    error = Color(0xFFBA1A1A)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppBlueTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProductApp()
                }
            }
        }
    }
}

enum class Screen {
    Splash,
    Landing,
    Login,
    Discover,
    Detail
}

@Composable
fun ProductApp(viewModel: ProductViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(Screen.Splash) }
    var selectedProductId by remember { mutableStateOf<Long?>(null) }

    when (currentScreen) {
        Screen.Splash -> {
            SplashScreen(onSplashFinished = {
                currentScreen = Screen.Login // Screen.Landing
            })
        }
        Screen.Landing -> {
            LandingScreen(onGetStarted = {
                currentScreen = Screen.Login
            })
        }
        Screen.Login -> {
            LoginScreen(onContinueAsGuest = {
                currentScreen = Screen.Discover
            })
        }
        Screen.Discover -> {
            DiscoverScreen(
                viewModel = viewModel,
                onProductClick = { id -> 
                    selectedProductId = id
                    currentScreen = Screen.Detail
                }
            )
        }
        Screen.Detail -> {
            selectedProductId?.let { id ->
                ProductDetailScreen(
                    productId = id,
                    viewModel = viewModel,
                    onBack = { 
                        currentScreen = Screen.Discover
                        selectedProductId = null
                    }
                )
            }
        }
    }
}
