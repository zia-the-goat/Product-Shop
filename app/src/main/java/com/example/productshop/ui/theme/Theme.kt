package com.example.productshop.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.productshop.ui.viewmodel.BrandTheme
import com.example.productshop.ui.viewmodel.ThemeMode

private val BlueLightColorScheme = lightColorScheme(
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

private val BlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6), // Brighter, more vibrant blue for dark mode
    onPrimary = Color(0xFF003355),
    primaryContainer = Color(0xFF004B7A),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFFA6C8FF),
    onSecondary = Color(0xFF003062),
    background = Color(0xFF0F111A), // Premium Deep Navy background
    surface = Color(0xFF0F111A),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF3F4759), // Enhanced contrast for cards and variants
    onSurfaceVariant = Color(0xFFC0C7D9),
    error = Color(0xFFFFB4AB)
)

private val EmeraldLightColorScheme = lightColorScheme(
    primary = Color(0xFF2E7D32),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF66BB6A),
    background = Color(0xFFF1F8F1),
    surface = Color.White,
    onSurface = Color(0xFF1B1C1B)
)

private val EmeraldDarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF1B5E20),
    primaryContainer = Color(0xFF2E7D32),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFA5D6A7),
    background = Color(0xFF0D140D),
    surface = Color(0xFF0D140D),
    onSurface = Color(0xFFE1E2E1)
)

private val RoseLightColorScheme = lightColorScheme(
    primary = Color(0xFFC2185B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF8BBD0),
    onPrimaryContainer = Color(0xFF880E4F),
    secondary = Color(0xFFE91E63),
    background = Color(0xFFFFF1F4),
    surface = Color.White,
    onSurface = Color(0xFF1C1B1B)
)

private val RoseDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF48FB1),
    onPrimary = Color(0xFF880E4F),
    primaryContainer = Color(0xFFC2185B),
    onPrimaryContainer = Color(0xFFF8BBD0),
    secondary = Color(0xFFF06292),
    background = Color(0xFF1A1113),
    surface = Color(0xFF1A1113),
    onSurface = Color(0xFFE6E1E1)
)

@Composable
fun ProductShopTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    brandTheme: BrandTheme = BrandTheme.BLUE,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Set to false to prefer our brand colors
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        brandTheme == BrandTheme.EMERALD -> {
            if (darkTheme) EmeraldDarkColorScheme else EmeraldLightColorScheme
        }
        
        brandTheme == BrandTheme.ROSE -> {
            if (darkTheme) RoseDarkColorScheme else RoseLightColorScheme
        }

        darkTheme -> BlueDarkColorScheme
        else -> BlueLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
