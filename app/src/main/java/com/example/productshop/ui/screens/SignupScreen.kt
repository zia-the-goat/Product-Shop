package com.example.productshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

import androidx.compose.ui.platform.LocalContext

@Preview
@Composable
fun SignupScreenPreview() {
    SignupScreen(viewModel = AuthViewModel(LocalContext.current.applicationContext as android.app.Application), onBack = {})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var customerTypeId by remember { mutableStateOf(1L) }
    var passwordVisible by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }

    // Validation State
    var emailDirty by remember { mutableStateOf(false) }
    var passwordDirty by remember { mutableStateOf(false) }
    var confirmPasswordDirty by remember { mutableStateOf(false) }
    var firstNameDirty by remember { mutableStateOf(false) }
    var lastNameDirty by remember { mutableStateOf(false) }
    var idNumberDirty by remember { mutableStateOf(false) }
    var hasBeenSubmitted by remember { mutableStateOf(false) }

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}\$".toRegex()
    val isEmailValid = email.matches(emailRegex)
    val isFirstNameValid = firstName.isNotBlank()
    val isLastNameValid = lastName.isNotBlank()
    val isIdNumberValid = idNumber.length == 13 && idNumber.all { it.isDigit() }
    
    val passwordRequirements = remember(password) {
        listOf(
            "Min 8 characters" to (password.length >= 8),
            "One uppercase letter" to password.any { it.isUpperCase() },
            "One number" to password.any { it.isDigit() },
            "One special character" to password.any { !it.isLetterOrDigit() }
        )
    }
    val isPasswordValid = passwordRequirements.all { it.second }
    val isConfirmPasswordValid = confirmPassword == password && confirmPassword.isNotEmpty()

    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()
    val isApiError = uiState is AuthUiState.Error
    val isOtpSent = uiState is AuthUiState.OtpSent || (uiState is AuthUiState.Error && uiState.isOtpError)

    fun shouldShowError(dirty: Boolean, valid: Boolean): Boolean {
        return (dirty || hasBeenSubmitted) && !valid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Up") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                .imePadding() // Add padding for the keyboard
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Join our community today",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email (Username)
            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it 
                    if (isApiError) viewModel.resetState()
                },
                label = { Text("Email (Username)") },
                placeholder = { Text("Used for login") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused && email.isNotEmpty()) emailDirty = true },
                singleLine = true,
                isError = shouldShowError(emailDirty, isEmailValid),
                supportingText = {
                    if (shouldShowError(emailDirty, isEmailValid)) {
                        Text("Enter a valid email address", color = Color.Red)
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

            // First Name
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused && firstName.isNotEmpty()) firstNameDirty = true },
                singleLine = true,
                isError = shouldShowError(firstNameDirty, isFirstNameValid),
                supportingText = {
                    if (shouldShowError(firstNameDirty, isFirstNameValid)) {
                        Text("First name is required", color = Color.Red)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Last Name
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused && lastName.isNotEmpty()) lastNameDirty = true },
                singleLine = true,
                isError = shouldShowError(lastNameDirty, isLastNameValid),
                supportingText = {
                    if (shouldShowError(lastNameDirty, isLastNameValid)) {
                        Text("Last name is required", color = Color.Red)
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

            Spacer(modifier = Modifier.height(16.dp))

            // ID Number
            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = it },
                label = { Text("ID Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused && idNumber.isNotEmpty()) idNumberDirty = true },
                singleLine = true,
                isError = shouldShowError(idNumberDirty, isIdNumberValid),
                supportingText = {
                    if (shouldShowError(idNumberDirty, isIdNumberValid)) {
                        Text("Enter a valid ID number", color = Color.Red)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            // Customer Type Selection - Research Doc: Fewer than 5 options = SegmentedButton
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Customer Type",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64B5F6),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val customerTypes = listOf(
                    1L to "Individual",
                    2L to "Business",
                    3L to "Special"
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    customerTypes.forEachIndexed { index, (id, label) ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = customerTypes.size),
                            onClick = { customerTypeId = id },
                            selected = customerTypeId == id,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Color(0xFF1E88E5),
                                activeContentColor = Color.White,
                                inactiveContainerColor = Color.Transparent,
                                inactiveContentColor = Color.White.copy(alpha = 0.7f),
                                activeBorderColor = Color(0xFF64B5F6),
                                inactiveBorderColor = Color.White.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(label, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password
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
                isError = shouldShowError(passwordDirty, isPasswordValid),
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

            // Password Strength Monitor
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Password Requirements:",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Bold
                    )
                    passwordRequirements.forEach { (req, met) ->
                        Text(
                            text = if (met) "✓ $req" else "○ $req",
                            fontSize = 12.sp,
                            color = if (met) Color.Green else Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it 
                    if (isApiError) viewModel.resetState()
                },
                label = { Text("Confirm Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused && confirmPassword.isNotEmpty()) confirmPasswordDirty = true },
                singleLine = true,
                isError = shouldShowError(confirmPasswordDirty, isConfirmPasswordValid),
                supportingText = {
                    if (shouldShowError(confirmPasswordDirty, isConfirmPasswordValid)) {
                        Text(text = "Passwords do not match", color = Color.Red)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

            if (isOtpSent) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = otp,
                    onValueChange = { otp = it },
                    label = { Text("Enter 6-digit OTP") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState is AuthUiState.Error && uiState.isOtpError,
                    supportingText = {
                        if (uiState is AuthUiState.Error && uiState.isOtpError) {
                            Text(text = uiState.message, color = Color.Red)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                if (uiState is AuthUiState.OtpSent) {
                    Text(
                        text = "A verification code has been sent to your email.",
                        color = Color.Green,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Sign Up Button
            Button(
                onClick = { 
                    hasBeenSubmitted = true
                    if (isEmailValid && isFirstNameValid && isLastNameValid && 
                        isIdNumberValid && isPasswordValid && isConfirmPasswordValid) {
                        if (isOtpSent) {
                            viewModel.signup(email, password, firstName, lastName, idNumber, customerTypeId, otp, onBack)
                        } else {
                            viewModel.sendOtp(email)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
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
                        Text(
                            text = if (isOtpSent) "VERIFY & SIGN UP" else "SEND OTP",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

