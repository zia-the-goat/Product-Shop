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
import com.example.productshop.ui.screens.DiscoverScreen
import com.example.productshop.ui.screens.LandingScreen
import com.example.productshop.ui.screens.LoginScreen
import com.example.productshop.ui.screens.SignupScreen
import com.example.productshop.ui.screens.ProductDetailScreen
import com.example.productshop.ui.screens.SplashScreen
import com.example.productshop.ui.screens.AccountScreen
import com.example.productshop.ui.screens.FaceSetupScreen
import com.example.productshop.ui.screens.FaceLoginScreen
import com.example.productshop.ui.screens.KycScreen
import com.example.productshop.ui.screens.SubscriptionsScreen
import com.example.productshop.security.SessionManager
import com.example.productshop.ui.viewmodel.AuthViewModel
import com.example.productshop.ui.viewmodel.KycViewModel
import com.example.productshop.ui.viewmodel.ProductViewModel
import com.example.productshop.ui.viewmodel.SubscriptionViewModel

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

class MainActivity : FragmentActivity() {
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
    Signup,
    Discover,
    Detail,
    Kyc,
    FaceSetup,
    FaceLogin
}

@Composable
fun ProductApp(
    viewModel: ProductViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    kycViewModel: KycViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(Screen.Splash) }
    var selectedProductId by remember { mutableStateOf<Long?>(null) }

    when (currentScreen) {
        Screen.Splash -> {
            SplashScreen(onSplashFinished = {
                currentScreen = Screen.Landing
            })
        }
        Screen.Landing -> {
            LandingScreen(
                onContinueAsGuest = {
                    authViewModel.clearData()
                    kycViewModel.clearData()
                    subscriptionViewModel.clearData()
                    authViewModel.setGuestMode(true)
                    currentScreen = Screen.Discover
                },
                onLogin = {
                    authViewModel.resetState()
                    currentScreen = Screen.Login
                },
                onSignUp = {
                    authViewModel.resetState()
                    currentScreen = Screen.Signup
                }
            )
        }
        Screen.Login -> {
            LoginScreen(
                viewModel = authViewModel,
                onBack = { currentScreen = Screen.Landing },
                onLoginSuccess = { currentScreen = Screen.Discover },
                onFaceLoginClick = { currentScreen = Screen.FaceLogin }
            )
        }
        Screen.Signup -> {
            SignupScreen(
                viewModel = authViewModel,
                onBack = { currentScreen = Screen.Landing }
            )
        }
        Screen.Discover -> {
            DiscoverScreen(
                viewModel = viewModel,
                kycViewModel = kycViewModel,
                authViewModel = authViewModel,
                subscriptionViewModel = subscriptionViewModel,
                onProductClick = { id ->
                    selectedProductId = id
                    currentScreen = Screen.Detail
                },
                onStartKyc = {
                    currentScreen = Screen.Kyc
                },
                onSetupFace = {
                    currentScreen = Screen.FaceSetup
                },
                onLogout = {
                    SessionManager.clearSession()
                    authViewModel.clearData()
                    kycViewModel.clearData()
                    subscriptionViewModel.clearData()
                    currentScreen = Screen.Landing
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
        Screen.Kyc -> {
            KycScreen(
                viewModel = kycViewModel,
                onBack = { currentScreen = Screen.Discover }
            )
        }
        Screen.FaceSetup -> {
            FaceSetupScreen(
                viewModel = authViewModel,
                onBack = { currentScreen = Screen.Discover },
                onSetupComplete = { currentScreen = Screen.Discover }
            )
        }
        Screen.FaceLogin -> {
            FaceLoginScreen(
                viewModel = authViewModel,
                onBack = { currentScreen = Screen.Login },
                onLoginSuccess = { currentScreen = Screen.Discover }
            )
        }
    }
}
