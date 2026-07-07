package com.example.productshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LandingScreen(onContinueAsGuest: () -> Unit, onLogin: () -> Unit, onSignUp: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A1C2E), Color(0xFF121320))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Icon and branding header grouped together
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Branding Icon",
                    modifier = Modifier
                        .size(112.dp)
                        .semantics { contentDescription = "Security branding icon" },
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Brand Text
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraLight, color = Color.White)) {
                            append("InsureTech")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)) {
                            append("Guard")
                        }
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.semantics { contentDescription = "Application Name" }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Your Secure Insurance Companion",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Bottom section actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Login Button
                Button(
                    onClick = onLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .semantics { contentDescription = "Primary Login button" },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Login", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sign Up link
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don't have an account? ", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        "Sign up",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier
                            .clickable { onSignUp() }
                            .semantics { contentDescription = "Sign up link" }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Continue as Guest
                TextButton(
                    onClick = onContinueAsGuest,
                    modifier = Modifier.semantics { contentDescription = "Continue as guest button" }
                ) {
                    Text(
                        text = "Continue as guest",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
