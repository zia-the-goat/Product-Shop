package com.example.productshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productshop.ui.viewmodel.AuthUiState
import com.example.productshop.ui.viewmodel.AuthViewModel

import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executor

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(viewModel = AuthViewModel(LocalContext.current.applicationContext as android.app.Application), onBack = {}, onLoginSuccess = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    
    val biometricPrompt = remember {
        if (context is FragmentActivity) {
            BiometricPrompt(
                context,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        viewModel.onBiometricSuccess(onLoginSuccess)
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                    }
                }
            )
        } else null
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use Password")
            .build()
    }

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Validation States
    var usernameDirty by remember { mutableStateOf(false) }
    var passwordDirty by remember { mutableStateOf(false) }
    var hasBeenSubmitted by remember { mutableStateOf(false) }

    val uiState = viewModel.uiState
    val isApiError = uiState is AuthUiState.Error

    fun isUsernameError() = (usernameDirty || hasBeenSubmitted) && username.isEmpty()
    fun isPasswordError() = ((passwordDirty || hasBeenSubmitted) && password.isEmpty()) || isApiError

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1C2E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1C2E)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header - Research Doc: Focus on value proposition/branding
            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to continue your journey",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Username Field - Research Doc: Keyboard Optimization
            OutlinedTextField(
                value = username,
                onValueChange = { 
                    username = it 
                    if (isApiError) viewModel.resetState()
                },
                label = { Text("Email (Username)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused && username.isNotEmpty()) usernameDirty = true },
                singleLine = true,
                isError = isUsernameError(),
                supportingText = {
                    if (isUsernameError()) {
                        Text("Username is required", color = Color.Red)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    errorTextColor = Color.White,
                    focusedLabelColor = Color(0xFF64B5F6),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedBorderColor = Color(0xFF64B5F6),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    errorBorderColor = Color.Red,
                    errorLabelColor = Color.Red
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field - Research Doc: Supporting Text for errors
            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it 
                    if (isApiError) viewModel.resetState()
                },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused && password.isNotEmpty()) passwordDirty = true },
                singleLine = true,
                isError = isPasswordError(),
                supportingText = {
                    if (isPasswordError()) {
                        val message = if (password.isEmpty()) "Password is required" else (uiState as AuthUiState.Error).message
                        Text(text = message, color = Color.Red)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    errorTextColor = Color.White,
                    focusedLabelColor = Color(0xFF64B5F6),
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedBorderColor = Color(0xFF64B5F6),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    errorBorderColor = Color.Red,
                    errorLabelColor = Color.Red
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Login Button - Research Doc: Optimistic UI / Touch Targets
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { 
                        hasBeenSubmitted = true
                        if (username.isNotEmpty() && password.isNotEmpty()) {
                            viewModel.login(username, password, onLoginSuccess)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF1E88E5), Color(0xFF64B5F6))
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("LOGIN", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (viewModel.canUseBiometric()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = { biometricPrompt?.authenticate(promptInfo) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Login",
                            tint = Color(0xFF64B5F6),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
