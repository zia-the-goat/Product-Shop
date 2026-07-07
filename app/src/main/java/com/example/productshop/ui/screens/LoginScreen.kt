package com.example.productshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material.icons.filled.Face
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
    settingsViewModel: com.example.productshop.ui.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
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
                title = { Text("Login", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF1A1C2E)
    ) { padding ->
        if (uiState is AuthUiState.Loading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .height(2.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            // Header - Research Doc: Focus on value proposition/branding
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sign in to continue your journey",
                style = MaterialTheme.typography.bodyLarge,
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
                        Text("Username is required", style = MaterialTheme.typography.bodySmall)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    errorTextColor = Color.White,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    errorSupportingTextColor = MaterialTheme.colorScheme.error
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
                        Text(text = message, style = MaterialTheme.typography.bodySmall)
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
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    errorTextColor = Color.White,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    errorSupportingTextColor = MaterialTheme.colorScheme.error
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
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    ),
                    enabled = uiState !is AuthUiState.Loading
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Text("LOGIN", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                if (viewModel.canUseBiometric() && settingsViewModel.biometricEnabled) {
                    Spacer(modifier = Modifier.width(16.dp))
                    IconButton(
                        onClick = { biometricPrompt?.authenticate(promptInfo) },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Login",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
