package com.example.productshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productshop.security.SecurityManager
import com.example.productshop.ui.viewmodel.AuthUiState
import com.example.productshop.ui.viewmodel.AuthViewModel
import com.example.productshop.ui.viewmodel.KycUiState
import com.example.productshop.ui.viewmodel.KycViewModel
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val application = context.applicationContext as? android.app.Application ?: android.app.Application()
    
    val kycViewModel = KycViewModel(application)
    val authViewModel = AuthViewModel(application)
    
    MaterialTheme {
        AccountScreen(
            viewModel = kycViewModel,
            authViewModel = authViewModel,
            onStartKyc = {},
            onLogout = {},
            onNavigateToSettings = {}
        )
    }
}

@Composable
fun AccountScreen(
    viewModel: KycViewModel,
    authViewModel: AuthViewModel,
    onStartKyc: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSupport: () -> Unit = {}
) {
    val profile = viewModel.profile
    val kycStatus = viewModel.kycStatus
    val isGuest = authViewModel.isGuest
    val scrollState = rememberScrollState()
    val securityManager = SecurityManager(androidx.compose.ui.platform.LocalContext.current.applicationContext)
    val activeAccountId = securityManager.getActiveAccountTypeId()
    val authUiState = authViewModel.uiState

    val cropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                authViewModel.updateProfilePicture(uri.toString())
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!isGuest) {
            viewModel.fetchProfileAndKyc()
        }
    }

    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Success && authUiState.message.contains("Account switched", ignoreCase = true)) {
            viewModel.fetchProfileAndKyc()
            authViewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Profile Header
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(enabled = !isGuest) {
                    val options = CropImageContractOptions(
                        uri = null,
                        cropImageOptions = CropImageOptions(
                            guidelines = CropImageView.Guidelines.ON,
                            fixAspectRatio = true,
                            aspectRatioX = 1,
                            aspectRatioY = 1,
                            cropShape = CropImageView.CropShape.OVAL
                        )
                    )
                    cropLauncher.launch(options)
                },
            contentAlignment = Alignment.Center
        ) {
            if (authViewModel.profilePicturePath != null) {
                AsyncImage(
                    model = authViewModel.profilePicturePath,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture Placeholder",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isGuest) {
            Text(
                text = "Guest User",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Log in to access your profile",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (profile != null) {
            Text(
                text = "${profile.firstName} ${profile.lastName}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = profile.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (viewModel.uiState is KycUiState.Loading || authUiState is AuthUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Active Account Section
        if (!isGuest && profile != null && profile.customerAccounts?.isNotEmpty() == true) {
            Text(
                text = "My Accounts",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 12.dp)
            )
            
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    profile.customerAccounts.forEachIndexed { index, account ->
                        val isActive = account.id == activeAccountId
                        Surface(
                            onClick = { authViewModel.switchAccount(account.id) },
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isActive) Icons.Default.AccountBalance else Icons.Default.AccountBalanceWallet,
                                        contentDescription = null,
                                        tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = account.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isActive) {
                                        Text(
                                            text = "Currently Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (isActive) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        if (index < profile.customerAccounts.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Only show KYC Status and Verification Button when not loading, profile is available, and NOT a guest
        if (!isGuest && viewModel.uiState !is KycUiState.Loading && profile != null) {
            // KYC Status Card - Refined
            val isVerified = kycStatus?.primaryIndicator == true
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = if (isVerified) Color(0xFFE8F5E9).copy(alpha = 0.5f) 
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    if (isVerified) Color(0xFF2E7D32).copy(alpha = 0.2f) 
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = if (isVerified) "Verified Account" else "Account Unverified",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isVerified) Color(0xFF1B5E20) else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = if (isVerified) "Full access enabled" else "Complete KYC to unlock features",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isVerified) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (!isVerified) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onStartKyc,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Complete KYC Verification")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Menu Items
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                AccountMenuItem(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    onClick = onNavigateToSettings
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                AccountMenuItem(
                    icon = Icons.Default.Info,
                    label = "Help & Support",
                    onClick = onNavigateToSupport
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Session Section
        Text(
            text = "Session",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 12.dp)
        )
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
        ) {
            AccountMenuItem(
                icon = if (isGuest) Icons.Default.Login else Icons.Default.ExitToApp, 
                label = if (isGuest) "Log In" else "Logout", 
                color = if (isGuest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                onClick = onLogout
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        // Developer Section
        Text(
            text = "Developer Options",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 12.dp)
        )
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            AccountMenuItem(
                icon = Icons.Default.BugReport,
                label = "Test Crash (Crashlytics)",
                color = MaterialTheme.colorScheme.error,
                onClick = { throw RuntimeException("Test Crash for Crashlytics") }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AccountMenuItem(
    icon: ImageVector,
    label: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label, 
                style = MaterialTheme.typography.bodyLarge, 
                color = color,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight, 
                contentDescription = "Go to $label", 
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
