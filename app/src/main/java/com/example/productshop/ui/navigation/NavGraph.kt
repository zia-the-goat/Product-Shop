package com.example.productshop.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.productshop.security.SessionManager
import com.example.productshop.util.AnalyticsManager
import com.example.productshop.ui.screens.*
import com.example.productshop.ui.viewmodel.AuthViewModel
import com.example.productshop.ui.viewmodel.KycViewModel
import com.example.productshop.ui.viewmodel.ProductViewModel
import com.example.productshop.ui.viewmodel.SubscriptionViewModel
import com.example.productshop.ui.components.SignupSkeleton
import com.example.productshop.ui.components.GenericSkeleton
import com.example.productshop.util.DeepLinkManager
import kotlinx.coroutines.delay

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Landing : Screen("landing")
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Discover : Screen("discover")
    object DiscoverSubscriptions : Screen("discover_subscriptions")
    object DiscoverAccount : Screen("discover_account")
    object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Long) = "detail/$productId"
    }
    object Kyc : Screen("kyc")
    object Fulfillment : Screen("fulfillment")
    object PaymentGateway : Screen("payment_gateway")
    object Settings : Screen("settings")
    object Support : Screen("support")
    object SubscriptionDetail : Screen("subscription_detail/{subscriptionId}") {
        fun createRoute(subscriptionId: Long) = "subscription_detail/$subscriptionId"
    }
}

@Composable
fun ProductNavHost(
    navController: NavHostController,
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    kycViewModel: KycViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    notificationViewModel: com.example.productshop.ui.viewmodel.NotificationViewModel,
    settingsViewModel: com.example.productshop.ui.viewmodel.SettingsViewModel
) {
    var loadingRoute by remember { mutableStateOf<String?>(null) }

    // Helper to navigate with a skeleton loader
    val navigateWithLoading: (String, (androidx.navigation.NavOptionsBuilder.() -> Unit)?) -> Unit = { route, builder ->
        loadingRoute = route
        if (builder != null) {
            navController.navigate(route, builder)
        } else {
            navController.navigate(route)
        }
    }

    val onHome: () -> Unit = {
        navController.navigate(Screen.Discover.route) {
            popUpTo(Screen.Discover.route) { inclusive = true }
        }
    }

    // Handle deep links manually to preserve backstack
    LaunchedEffect(navController) {
        DeepLinkManager.deepLinkUri.collect { uri ->
            val productId = uri.getQueryParameter("product")?.toLongOrNull()
            if (productId != null) {
                navController.navigate(Screen.Detail.createRoute(productId))
            } else if (uri.host == "open") {
                navController.navigate(Screen.Discover.route)
            }
        }
    }

    // Reset loading state when destination is reached
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            destination.route?.let { route ->
                AnalyticsManager.logScreenView(route)
                // Persist the last route - only if it's not a template (no {arguments})
                if (route != Screen.Splash.route && route != Screen.Landing.route && !route.contains("{")) {
                    SessionManager.lastRoute = route
                }
            }
            loadingRoute = null
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val startDest = remember { SessionManager.lastRoute ?: Screen.Splash.route }
        NavHost(
            navController = navController,
            startDestination = startDest,
            enterTransition = {
                fadeIn(animationSpec = tween(400)) + slideInHorizontally(animationSpec = tween(400)) { it / 3 }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(400)) + slideOutHorizontally(animationSpec = tween(400)) { -it / 3 }
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(400)) + slideInHorizontally(animationSpec = tween(400)) { -it / 3 }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(400)) + slideOutHorizontally(animationSpec = tween(400)) { it / 3 }
            }
        ) {
            composable(
                route = Screen.Splash.route
            ) {
                SplashScreen(onSplashFinished = {
                    if (navController.currentDestination?.route == Screen.Splash.route) {
                        if (SessionManager.hasToken()) {
                            navigateWithLoading(Screen.Discover.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            navigateWithLoading(Screen.Landing.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                })
            }

            composable(Screen.Landing.route) {
                LandingScreen(
                    onContinueAsGuest = {
                        authViewModel.clearData()
                        kycViewModel.clearData()
                        subscriptionViewModel.clearData()
                        authViewModel.setGuestMode(true)
                        navigateWithLoading(Screen.Discover.route) {
                            popUpTo(Screen.Landing.route) { inclusive = true }
                        }
                    },
                    onLogin = {
                        authViewModel.resetState()
                        navigateWithLoading(Screen.Login.route, null)
                    },
                    onSignUp = {
                        authViewModel.resetState()
                        navigateWithLoading(Screen.Signup.route, null)
                    }
                )
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    settingsViewModel = settingsViewModel,
                    onBack = { navController.popBackStack() },
                    onLoginSuccess = {
                        productViewModel.searchQuery = ""
                        val pendingId = SessionManager.pendingProductId
                        if (pendingId != null) {
                            SessionManager.pendingProductId = null
                            navigateWithLoading(Screen.Detail.createRoute(pendingId)) {
                                popUpTo(Screen.Landing.route) { inclusive = true }
                            }
                        } else {
                            navigateWithLoading(Screen.Discover.route) {
                                popUpTo(Screen.Landing.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Signup.route) {
                SignupScreen(
                    viewModel = authViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                Screen.Discover.route
            ) {
                DiscoverScreen(
                    selectedTab = 0,
                    viewModel = productViewModel,
                    kycViewModel = kycViewModel,
                    authViewModel = authViewModel,
                    subscriptionViewModel = subscriptionViewModel,
                    notificationViewModel = notificationViewModel,
                    onTabSelected = { tab ->
                        val route = when(tab) {
                            1 -> Screen.DiscoverSubscriptions.route
                            2 -> Screen.DiscoverAccount.route
                            else -> Screen.Discover.route
                        }
                        if (route != Screen.Discover.route) {
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onProductClick = { id ->
                        navigateWithLoading(Screen.Detail.createRoute(id), null)
                    },
                    onSubscriptionClick = { id ->
                        navigateWithLoading(Screen.SubscriptionDetail.createRoute(id), null)
                    },
                    onStartKyc = {
                        navigateWithLoading(Screen.Kyc.route, null)
                    },
                    onLogout = {
                        SessionManager.clearSession()
                        authViewModel.clearData()
                        kycViewModel.clearData()
                        subscriptionViewModel.clearData()
                        navigateWithLoading(Screen.Landing.route) {
                            popUpTo(Screen.Discover.route) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = {
                        navigateWithLoading(Screen.Settings.route, null)
                    },
                    onNavigateToSupport = {
                        navigateWithLoading(Screen.Support.route, null)
                    }
                )
            }

            composable(Screen.DiscoverSubscriptions.route) {
                DiscoverScreen(
                    selectedTab = 1,
                    viewModel = productViewModel,
                    kycViewModel = kycViewModel,
                    authViewModel = authViewModel,
                    subscriptionViewModel = subscriptionViewModel,
                    notificationViewModel = notificationViewModel,
                    onTabSelected = { tab ->
                        val route = when(tab) {
                            0 -> Screen.Discover.route
                            2 -> Screen.DiscoverAccount.route
                            else -> Screen.DiscoverSubscriptions.route
                        }
                        if (route != Screen.DiscoverSubscriptions.route) {
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onProductClick = { id ->
                        navigateWithLoading(Screen.Detail.createRoute(id), null)
                    },
                    onSubscriptionClick = { id ->
                        navigateWithLoading(Screen.SubscriptionDetail.createRoute(id), null)
                    },
                    onStartKyc = {
                        navigateWithLoading(Screen.Kyc.route, null)
                    },
                    onLogout = {
                        SessionManager.clearSession()
                        authViewModel.clearData()
                        kycViewModel.clearData()
                        subscriptionViewModel.clearData()
                        navigateWithLoading(Screen.Landing.route) {
                            popUpTo(Screen.Discover.route) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = {
                        navigateWithLoading(Screen.Settings.route, null)
                    },
                    onNavigateToSupport = {
                        navigateWithLoading(Screen.Support.route, null)
                    }
                )
            }

            composable(Screen.DiscoverAccount.route) {
                DiscoverScreen(
                    selectedTab = 2,
                    viewModel = productViewModel,
                    kycViewModel = kycViewModel,
                    authViewModel = authViewModel,
                    subscriptionViewModel = subscriptionViewModel,
                    notificationViewModel = notificationViewModel,
                    onTabSelected = { tab ->
                        val route = when(tab) {
                            0 -> Screen.Discover.route
                            1 -> Screen.DiscoverSubscriptions.route
                            else -> Screen.DiscoverAccount.route
                        }
                        if (route != Screen.DiscoverAccount.route) {
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    onProductClick = { id ->
                        navigateWithLoading(Screen.Detail.createRoute(id), null)
                    },
                    onSubscriptionClick = { id ->
                        navigateWithLoading(Screen.SubscriptionDetail.createRoute(id), null)
                    },
                    onStartKyc = {
                        navigateWithLoading(Screen.Kyc.route, null)
                    },
                    onLogout = {
                        SessionManager.clearSession()
                        authViewModel.clearData()
                        kycViewModel.clearData()
                        subscriptionViewModel.clearData()
                        navigateWithLoading(Screen.Landing.route) {
                            popUpTo(Screen.Discover.route) { inclusive = true }
                        }
                    },
                    onNavigateToSettings = {
                        navigateWithLoading(Screen.Settings.route, null)
                    },
                    onNavigateToSupport = {
                        navigateWithLoading(Screen.Support.route, null)
                    }
                )
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("productId") { type = NavType.LongType })
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getLong("productId") ?: return@composable
                ProductDetailScreen(
                    productId = productId,
                    viewModel = productViewModel,
                    authViewModel = authViewModel,
                    kycViewModel = kycViewModel,
                    notificationViewModel = notificationViewModel,
                    onBack = { 
                        val previousRoute = navController.previousBackStackEntry?.destination?.route
                        val isAtRestrictedScreen = previousRoute == Screen.Splash.route || 
                                                 previousRoute == Screen.Landing.route || 
                                                 previousRoute == Screen.Login.route
                        
                        if (navController.previousBackStackEntry == null || (isAtRestrictedScreen && SessionManager.hasToken())) {
                            navController.navigate(Screen.Discover.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            navController.popBackStack()
                        }
                    },
                    onHome = onHome,
                    onApply = {
                        navigateWithLoading(Screen.Fulfillment.route, null)
                    },
                    onProductClick = { id ->
                        navigateWithLoading(Screen.Detail.createRoute(id), null)
                    },
                    onLoginRedirect = { id ->
                        SessionManager.pendingProductId = id
                        navigateWithLoading(Screen.Login.route, null)
                    },
                    onKycRedirect = {
                        navigateWithLoading(Screen.Kyc.route, null)
                    }
                )
            }

            composable(Screen.Kyc.route) {
                KycScreen(
                    viewModel = kycViewModel,
                    onBack = { navController.popBackStack() },
                    onHome = onHome
                )
            }

            composable(Screen.Fulfillment.route) {
                FulfillmentFlowScreen(
                    viewModel = productViewModel,
                    onBack = { navController.popBackStack() },
                    onHome = onHome,
                    onComplete = {
                        navigateWithLoading(Screen.Discover.route) {
                            popUpTo(Screen.Discover.route) { inclusive = true }
                        }
                    },
                    onNavigateToPayment = {
                        navController.navigate(Screen.PaymentGateway.route)
                    }
                )
            }

            composable(Screen.PaymentGateway.route) {
                PaymentGatewayScreen(
                    viewModel = productViewModel,
                    onPaymentComplete = {
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBack = { navController.popBackStack() },
                    onHome = onHome,
                    onNavigateToSupport = {
                        navController.navigate(Screen.Support.route)
                    }
                )
            }

            composable(Screen.Support.route) {
                val supportViewModel: com.example.productshop.ui.viewmodel.SupportViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                SupportScreen(
                    viewModel = supportViewModel,
                    onBack = { navController.popBackStack() },
                    onHome = onHome
                )
            }

            composable(
                route = Screen.SubscriptionDetail.route,
                arguments = listOf(navArgument("subscriptionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId") ?: return@composable
                SubscriptionDetailsScreen(
                    subscriptionId = subscriptionId,
                    viewModel = subscriptionViewModel,
                    onBack = { 
                        navController.popBackStack()
                    },
                    onHome = onHome
                )
            }
        }

        // Loading Overlay
        AnimatedVisibility(
            visible = loadingRoute != null,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            when (loadingRoute) {
                Screen.Signup.route -> SignupSkeleton()
                else -> {
                    // Skip skeleton for Landing to avoid redundant fades from Splash
                    if (loadingRoute != null && 
                        loadingRoute != Screen.Splash.route && 
                        loadingRoute != Screen.Landing.route) {
                         GenericSkeleton()
                    }
                }
            }
        }
    }
}
