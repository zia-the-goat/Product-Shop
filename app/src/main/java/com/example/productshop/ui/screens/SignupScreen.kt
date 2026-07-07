package com.example.productshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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

import com.example.productshop.util.IdValidationUtils

import kotlinx.coroutines.delay
import java.util.Locale

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
    var customerTypeId by remember { mutableLongStateOf(1L) }
    var selectedAccountTypeIds by remember { mutableStateOf(setOf(1L)) }
    var passwordVisible by remember { mutableStateOf(false) }
    var otp by remember { mutableStateOf("") }

    var systemPassword by remember { mutableStateOf("") }

    // Validation State
    var emailDirty by remember { mutableStateOf(false) }
    var passwordDirty by remember { mutableStateOf(false) }
    var confirmPasswordDirty by remember { mutableStateOf(false) }
    var firstNameDirty by remember { mutableStateOf(false) }
    var lastNameDirty by remember { mutableStateOf(false) }
    var idNumberDirty by remember { mutableStateOf(false) }
    var hasBeenSubmitted by remember { mutableStateOf(false) }

    val randomIdPlaceholder = remember { IdValidationUtils.generateRandomId() }

    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]{2,}\$".toRegex()
    val isEmailValid = email.matches(emailRegex)
    val isFirstNameValid = firstName.isNotBlank()
    val isLastNameValid = lastName.isNotBlank()
    val isIdNumberValid = IdValidationUtils.isValidSouthAfricanId(idNumber)
    
    val uiState = viewModel.uiState
    val scrollState = rememberScrollState()
    val isEmailAlreadyRegistered = uiState is AuthUiState.Error && 
            uiState.message.contains("already registered", ignoreCase = true)

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

    val isApiError = uiState is AuthUiState.Error
    val isOtpSent = uiState is AuthUiState.OtpSent || (uiState is AuthUiState.Error && uiState.isOtpError)

    fun shouldShowError(dirty: Boolean, valid: Boolean): Boolean {
        return (dirty || hasBeenSubmitted) && !valid
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Up", style = MaterialTheme.typography.titleLarge) },
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
                .imePadding() // Add padding for the keyboard
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Join our community today",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(40.dp))

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
                isError = shouldShowError(emailDirty, isEmailValid) || isEmailAlreadyRegistered,
                supportingText = {
                    if (shouldShowError(emailDirty, isEmailValid)) {
                        Text("Enter a valid email address", style = MaterialTheme.typography.bodySmall)
                    } else if (isEmailAlreadyRegistered) {
                        Text((uiState as AuthUiState.Error).message, style = MaterialTheme.typography.bodySmall)
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
                        Text("First name is required", style = MaterialTheme.typography.bodySmall)
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
                        Text("Last name is required", style = MaterialTheme.typography.bodySmall)
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

            Spacer(modifier = Modifier.height(16.dp))

            // ID Number
            OutlinedTextField(
                value = idNumber,
                onValueChange = { idNumber = randomIdPlaceholder },
                label = { Text("ID Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { if (!it.isFocused && idNumber.isNotEmpty()) idNumberDirty = true },
                singleLine = true,
                isError = shouldShowError(idNumberDirty, isIdNumberValid),
                supportingText = {
                    if (shouldShowError(idNumberDirty, isIdNumberValid)) {
                        Text("Enter a valid ID number", style = MaterialTheme.typography.bodySmall)
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Account Type Selection
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Account Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                val accountTypes = listOf(
                    1L to "Gold Cheque",
                    2L to "Platinum Cheque",
                    3L to "Signet Cheque",
                    4L to "Islamic Cheque",
                    5L to "Savings",
                    6L to "SME Checking",
                    7L to "Medium Enterprise Checking",
                    8L to "Large Enterprise Checking"
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(accountTypes.size) { index ->
                        val (id, label) = accountTypes[index]
                        val isSelected = selectedAccountTypeIds.contains(id)
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                if (isSelected) {
                                    if (selectedAccountTypeIds.size > 1) {
                                        selectedAccountTypeIds = selectedAccountTypeIds - id
                                    }
                                } else {
                                    if (selectedAccountTypeIds.size < 5) {
                                        selectedAccountTypeIds = selectedAccountTypeIds + id
                                    }
                                }
                            },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.05f),
                                labelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.White.copy(alpha = 0.2f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
                Text(
                    text = "${selectedAccountTypeIds.size}/5 accounts selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Customer Type Selection - Research Doc: Fewer than 5 options = SegmentedButton
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Customer Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                val customerTypes = listOf(
                    1L to "Individual",
                    2L to "Sole Propriety",
                    3L to "Non-Profit",
                    4L to "CIPC",
                    5L to "System"
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(customerTypes.size) { index ->
                        val (id, label) = customerTypes[index]
                        FilterChip(
                            selected = customerTypeId == id,
                            onClick = { customerTypeId = id },
                            label = { Text(label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.05f),
                                labelColor = Color.White.copy(alpha = 0.7f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = customerTypeId == id,
                                borderColor = Color.White.copy(alpha = 0.2f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            if (customerTypeId == 5L) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = systemPassword,
                    onValueChange = { systemPassword = it },
                    label = { Text("System Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
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
                        errorLabelColor = MaterialTheme.colorScheme.error
                    )
                )
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
                    errorLabelColor = MaterialTheme.colorScheme.error
                )
            )

            // Password Strength Monitor
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Password Requirements:",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        passwordRequirements.forEach { (req, met) ->
                            Text(
                                text = if (met) "✓ $req" else "○ $req",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (met) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.5f)
                            )
                        }
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
                        Text(text = "Passwords do not match", style = MaterialTheme.typography.bodySmall)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
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
                            Text(text = uiState.message, style = MaterialTheme.typography.bodySmall)
                        } else {
                            // Resend Link attached directly below the field's content
                            TextButton(
                                onClick = { viewModel.resendOtp(email) },
                                modifier = Modifier.offset(x = (-12).dp, y = (-8).dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    text = "Didn't receive code? Resend OTP",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                if (uiState is AuthUiState.OtpSent) {
                    Text(
                        text = "A verification code has been sent to your email.",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // Separate Timer and Expiration Button
                if (!viewModel.isOtpExpired) {
                    Text(
                        text = String.format(Locale.getDefault(), "OTP expires in %02d:%02d", viewModel.otpTimer / 60, viewModel.otpTimer % 60),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Button(
                        onClick = { viewModel.sendOtp(email, customerTypeId, systemPassword) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "OTP EXPIRED - SEND NEW OTP",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Sign Up Button
            Button(
                onClick = { 
                    hasBeenSubmitted = true
                    if (isEmailValid && isFirstNameValid && isLastNameValid && 
                        isIdNumberValid && isPasswordValid && isConfirmPasswordValid &&
                        (customerTypeId != 5L || systemPassword.isNotEmpty())) {
                        if (isOtpSent) {
                            viewModel.signup(email, password, firstName, lastName, idNumber, customerTypeId, selectedAccountTypeIds.toList(), otp, systemPassword, onBack)
                        } else {
                            viewModel.sendOtp(email, customerTypeId, systemPassword)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
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
                    Text(
                        text = if (isOtpSent) "VERIFY & SIGN UP" else "SEND OTP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Summary Error Message
            if (hasBeenSubmitted && !(isEmailValid && isFirstNameValid && isLastNameValid && 
                isIdNumberValid && isPasswordValid && isConfirmPasswordValid)) {
                Text(
                    text = "Please complete all fields correctly",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (uiState is AuthUiState.Error && !uiState.isOtpError) {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

