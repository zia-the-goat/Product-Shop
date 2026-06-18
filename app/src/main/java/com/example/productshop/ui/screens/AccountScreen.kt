package com.example.productshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productshop.ui.viewmodel.AuthViewModel
import com.example.productshop.ui.viewmodel.KycUiState
import com.example.productshop.ui.viewmodel.KycViewModel

@Composable
fun AccountScreen(
    viewModel: KycViewModel,
    authViewModel: AuthViewModel,
    onStartKyc: () -> Unit,
    onSetupFace: () -> Unit,
    onLogout: () -> Unit
) {
    val profile = viewModel.profile
    val kycStatus = viewModel.kycStatus
    val isGuest = authViewModel.isGuest

    LaunchedEffect(Unit) {
        if (!isGuest) {
            viewModel.fetchProfileAndKyc()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Header
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Picture Placeholder",
                modifier = Modifier.size(60.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isGuest) {
            Text(
                text = "Guest User",
                style = MaterialTheme.typography.headlineSmall,
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
        } else if (viewModel.uiState is KycUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }

        // Only show KYC Status and Verification Button when not loading, profile is available, and NOT a guest
        if (!isGuest && viewModel.uiState !is KycUiState.Loading && profile != null) {
            Spacer(modifier = Modifier.height(32.dp))

            // KYC Status Card
            val isVerified = kycStatus?.primaryIndicator == true && kycStatus.secondaryIndicator == true
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = if (isVerified) "Verified" else "Attention required",
                        tint = if (isVerified) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isVerified) "Account Verified" else "Account Unverified",
                            fontWeight = FontWeight.Bold,
                            color = if (isVerified) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                        )
                        Text(
                            text = if (isVerified) "You have full access to all products." else "Complete KYC to unlock all features.",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isVerified) Color(0xFF2E7D32).copy(alpha = 0.8f) else Color(0xFFEF6C00).copy(alpha = 0.8f)
                        )
                    }
                }
            }

            if (!isVerified) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onStartKyc,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Complete KYC Verification")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        } else {
            // Placeholder spacer
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Menu Items
        if (!isGuest) {
            AccountMenuItem(
                icon = Icons.Default.Face, 
                label = if (authViewModel.isFaceAuthSetup()) "Update Face Recognition" else "Setup Face Recognition",
                onClick = onSetupFace
            )
        }
        
        AccountMenuItem(icon = Icons.Default.Settings, label = "Settings")
        AccountMenuItem(icon = Icons.Default.Info, label = "Help & Support")
        
        AccountMenuItem(
            icon = if (isGuest) Icons.Default.Login else Icons.Default.ExitToApp, 
            label = if (isGuest) "Log In" else "Logout", 
            color = if (isGuest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            onClick = onLogout
        )
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
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = "Menu Icon: $label", tint = color)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = color)
            Spacer(modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Go to $label", tint = Color.Gray)
        }
    }
}
