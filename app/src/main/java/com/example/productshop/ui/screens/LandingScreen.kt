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
    // 12. Trust & Security: Login/Security focused screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1C2E)) // Consistent dark blue
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp), // 13. Visual Design: 8dp grid (32dp)
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon with branding
            Icon(
                imageVector = Icons.Default.Fingerprint,
                contentDescription = "Branding Icon",
                modifier = Modifier
                    .size(100.dp)
                    .semantics { contentDescription = "Security branding icon" },
                tint = Color(0xFF64B5F6)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Brand Text
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Light, color = Color.White)) {
                        append("InsureTech")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                        append("Guard")
                    }
                },
                fontSize = 32.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.semantics { contentDescription = "Application Name" }
            )

            Spacer(modifier = Modifier.height(140.dp))

            // Login Button (Gradient) - 3. User Input (Touch Target)
            Button(
                onClick = { /* Security logic */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .semantics { contentDescription = "Primary Login button" },
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF1E88E5), Color(0xFF64B5F6))
                            )
                        )
                        .clickable{onLogin()},
                    contentAlignment = Alignment.Center
                ) {
                    Text("Login", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up link
            Row {
                Text("Don't have an account? ", color = Color.White.copy(alpha = 0.7f))
                Text(
                    "Sign up",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { onSignUp() }
                        .semantics { contentDescription = "Sign up link" }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Continue as Guest - 11. Performance (Low friction entry)
            Text(
                text = "Continue as guest",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable { onContinueAsGuest() }
                    .semantics { contentDescription = "Continue as guest button" }
            )
        }
    }
}
