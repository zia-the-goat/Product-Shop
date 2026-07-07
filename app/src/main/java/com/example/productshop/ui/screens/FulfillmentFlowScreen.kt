package com.example.productshop.ui.screens

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productshop.data.model.ProductDto
import com.example.productshop.ui.viewmodel.FulfillmentUiState
import com.example.productshop.ui.viewmodel.ProductViewModel
import com.example.productshop.data.model.FulfilmentResultDto
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.example.productshop.util.AnalyticsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FulfillmentFlowScreen(
    viewModel: ProductViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onComplete: () -> Unit,
    onNavigateToPayment: () -> Unit
) {
    val product = viewModel.selectedProductForFulfillment ?: return
    var currentStep by remember { 
        mutableIntStateOf(
            if (viewModel.isPaymentVerified) 4 
            else viewModel.getSavedProgress(product.id)
        ) 
    }
    val productType = ProductType.fromProduct(product)

    // Save progress whenever currentStep changes
    LaunchedEffect(currentStep) {
        viewModel.saveProgress(currentStep)
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = product.name, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    if (viewModel.fulfillmentUiState !is FulfillmentUiState.Error && !viewModel.isPaymentVerified) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (viewModel.fulfillmentUiState !is FulfillmentUiState.Error && !viewModel.isPaymentVerified) {
                        IconButton(onClick = onHome) {
                            Icon(androidx.compose.material.icons.Icons.Default.Home, contentDescription = "Home")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Step Indicator
            FulfillmentStepIndicator(currentStep = currentStep, totalSteps = 4)

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (viewModel.fulfillmentUiState is FulfillmentUiState.Error) {
                    val errorState = viewModel.fulfillmentUiState as FulfillmentUiState.Error
                        FulfillmentErrorUI(
                        viewModel = viewModel,
                        product = product,
                        message = errorState.message,
                        failedChecks = errorState.failedChecks,
                        onRetry = { viewModel.resetFulfillmentState() },
                        onCancel = onBack
                    )
                } else {
                    when (currentStep) {
                        1, 2 -> {
                            when (productType) {
                                ProductType.CONTRACT -> ContractFlow(currentStep, viewModel)
                                ProductType.INVESTMENT -> InvestmentFlow(currentStep, viewModel)
                                ProductType.INSURANCE -> InsuranceFlow(currentStep, viewModel)
                            }
                        }
                        3 -> FinalReviewFlow(viewModel)
                        4 -> PaymentFlow(viewModel, onNavigateToPayment)
                    }
                }
            }

            // Bottom Navigation
            if (viewModel.fulfillmentUiState !is FulfillmentUiState.Error) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 1 && !viewModel.isPaymentVerified) {
                        TextButton(onClick = { currentStep-- }) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(64.dp))
                    }

                    if (currentStep < 4) {
                        Button(
                            onClick = { 
                                if (currentStep == 3) {
                                    viewModel.validateEligibility {
                                        currentStep++
                                    }
                                } else {
                                    currentStep++
                                }
                            },
                            enabled = viewModel.fulfillmentUiState !is FulfillmentUiState.Loading,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (viewModel.fulfillmentUiState is FulfillmentUiState.Loading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying...")
                            } else {
                                Text("Continue")
                                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                            }
                        }
                    } else {
                        FulfillmentSubmitButton(viewModel, onComplete)
                    }
                }
            }
        }
    }
}

@Composable
fun FulfillmentStepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val stepNum = index + 1
            val isActive = stepNum == currentStep
            val isCompleted = stepNum < currentStep

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        color = when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isActive -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = stepNum.toString(),
                        color = when {
                            isActive -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .padding(horizontal = 4.dp)
                        .background(
                            color = if (isCompleted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

@Composable
fun FulfillmentSubmitButton(viewModel: ProductViewModel, onComplete: () -> Unit) {
    val state = viewModel.fulfillmentUiState

    LaunchedEffect(state) {
        if (state is FulfillmentUiState.Success) {
            viewModel.saveProgress(1)
            onComplete()
        }
    }

    if (state is FulfillmentUiState.Loading) {
        CircularProgressIndicator()
    } else {
        Button(
            onClick = { viewModel.completeFulfillment() },
            enabled = viewModel.isPaymentVerified,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Confirm & Subscribe")
        }
    }
}

@Composable
fun FulfillmentErrorUI(
    viewModel: ProductViewModel,
    product: ProductDto,
    message: String,
    failedChecks: List<FulfilmentResultDto>,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    val productType = ProductType.fromProduct(product)
    
    // Qualifying constraints now fetched from ViewModel to avoid redundancy
    val (qualifyingCustomerTypes, qualifyingAccountTypes) = viewModel.getQualifyingConstraints(product.name)

    val baseCheckPatterns = when (productType) {
        ProductType.CONTRACT -> listOf("KYC")
        ProductType.INVESTMENT -> listOf("KYC", "Living", "ID", "Fraud")
        ProductType.INSURANCE -> listOf("KYC", "Fraud", "Living", "ID", "Credit", "Marital")
    }
    
    val requiredCheckNames = baseCheckPatterns + listOf("Customer Type", "Account Type")
    AnalyticsManager.logEvent("Fulfillment Error", Bundle().apply { putString("Message", message) })
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Verification Incomplete",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "We were unable to verify some requirements for this ${productType.label.lowercase()}.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(
                    text = "Requirements Checklist",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                requiredCheckNames.forEach { checkName ->
                    val failedCheck = when (checkName) {
                        "Customer Type" -> {
                            val userCustomerType = com.example.productshop.ui.viewmodel.AuthViewModel.currentCustomer?.customerType?.name?.uppercase()
                            if (userCustomerType != null && qualifyingCustomerTypes.any { it.contains(userCustomerType) || userCustomerType.contains(it) }) null
                            else FulfilmentResultDto("Customer Type", false, "Your customer type ($userCustomerType) is not eligible for this product. Required: ${qualifyingCustomerTypes.joinToString(", ")}")
                        }
                        "Account Type" -> {
                            val context = androidx.compose.ui.platform.LocalContext.current
                            val securityManager = remember { com.example.productshop.security.SecurityManager(context) }
                            val activeAccountId = securityManager.getActiveAccountTypeId()
                            val userAccountType = com.example.productshop.ui.viewmodel.AuthViewModel.currentCustomer?.customerAccounts?.find { it.id == activeAccountId }?.name

                            if (userAccountType != null && qualifyingAccountTypes.any { it.contains(userAccountType) || userAccountType.contains(it) }) null
                            else FulfilmentResultDto("Account Type", false, "Your account type (${userAccountType ?: "Unknown"}) is not eligible for this product. Required: ${qualifyingAccountTypes.joinToString(", ")}")
                        }
                        else -> failedChecks.find { it.checkName.contains(checkName, ignoreCase = true) }
                    }

                    val passed = failedCheck == null

                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (passed) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = checkName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (passed) FontWeight.Normal else FontWeight.Bold,
                                color = if (passed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                            )
                            if (!passed && failedCheck?.failureMessage != null) {
                                Text(
                                    text = failedCheck.failureMessage ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
            Button(onClick = onRetry) {
                Text("Retry Verification")
            }
        }
    }
}

@Composable
fun PaymentFlow(viewModel: ProductViewModel, onNavigateToPayment: () -> Unit) {
    var isRedirecting by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isRedirecting -> {
                Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Redirecting to Secure Gateway...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Please do not refresh or close this window",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    delay(2000)
                    isRedirecting = false
                    onNavigateToPayment()
                }
            }

            viewModel.isPaymentVerified -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Payment Verified!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Your transaction was successful.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        "Click 'Confirm & Subscribe' to finalize.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Secure Payment Gateway",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "We use a secure third-party gateway to process your payment.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { isRedirecting = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Verify with 3rd Party Gateway")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalReviewFlow(viewModel: ProductViewModel) {
    val product = viewModel.selectedProductForFulfillment ?: return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Final Review",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Please confirm your subscription details",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Product Summary", 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                DetailRow("Product", product.name)
                DetailRow("Monthly Premium", "R ${String.format("%.2f", product.price)}", isPrimary = true)
                DetailRow("Duration", viewModel.selectedContractTerm.ifEmpty { "N/A" })
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        var showTerms by remember { mutableStateOf(false) }
        
        if (showTerms) {
            ModalBottomSheet(
                onDismissRequest = { showTerms = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Terms & Conditions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = """
                            1. Introduction
                            By subscribing to this product, you agree to be bound by these terms and conditions.
                            
                            2. Eligibility
                            You must be a verified user with a valid identification document to apply for financial products.
                            
                            3. Monthly Premiums
                            Premiums are collected via debit order on the selected date each month. Failure to pay may lead to policy cancellation.
                            
                            4. Cancellation
                            You may cancel your subscription at any time. A 10% cancellation fee applies if the cancellation occurs within 7 days of the next monthly payment date.
                            
                            5. Privacy Policy
                            Your data is handled according to our secure privacy guidelines and shared only with necessary financial regulators.
                            
                            6. Compliance
                            This product complies with all local financial service board regulations.
                            
                            7. Claims Process
                            Claims can be initiated via the mobile app or our 24/7 helpline. Supporting documentation will be required.
                            
                            8. Device Warranty (Contracts only)
                            Devices are covered by a standard manufacturer warranty for 24 months.
                            
                            9. Investment Risk (Investments only)
                            Market-linked investments carry inherent risks. Past performance is not indicative of future results.
                            
                            10. Jurisdiction
                            These terms are governed by the laws of the Republic of South Africa.
                        """.trimIndent(),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { showTerms = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("I Understand")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.clickable { showTerms = true }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                val annotatedString = buildAnnotatedString {
                    append("By clicking 'Continue', you agree to the ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)) {
                        append("terms and conditions")
                    }
                    append(" of this product.")
                }
                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, isPrimary: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ContractFlow(step: Int, viewModel: ProductViewModel) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        when (step) {
            1 -> {
                Text("Customize your Device", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Select Color", style = MaterialTheme.typography.titleMedium)
                val colors = listOf("Titanium Gray", "Phantom Black", "Cloud Blue")
                colors.forEach { color ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectedColor = color }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = viewModel.selectedColor == color, onClick = { viewModel.selectedColor = color })
                        Text(color)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Storage", style = MaterialTheme.typography.titleMedium)
                val storages = listOf("128GB", "256GB", "512GB")
                storages.forEach { storage ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectedStorage = storage }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = viewModel.selectedStorage == storage, onClick = { viewModel.selectedStorage = storage })
                        Text(storage)
                    }
                }
            }
            2 -> {
                Text("Select your Plan", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                val terms = listOf("12 Months", "24 Months", "36 Months")
                terms.forEach { term ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { viewModel.selectedContractTerm = term },
                        colors = CardDefaults.cardColors(
                            containerColor = if (viewModel.selectedContractTerm == term) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (viewModel.selectedContractTerm == term) null else CardDefaults.outlinedCardBorder()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = viewModel.selectedContractTerm == term, onClick = { viewModel.selectedContractTerm = term })
                            Text(term, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InvestmentFlow(step: Int, viewModel: ProductViewModel) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        when (step) {
            1 -> {
                Text("Define your Goal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                val goals = listOf("Retirement", "Education", "Wealth Building", "Emergency Fund")
                goals.forEach { goal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.investmentGoal = goal }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = viewModel.investmentGoal == goal, onClick = { viewModel.investmentGoal = goal })
                        Text(goal)
                    }
                }
            }
            2 -> {
                Text("Risk Profiling", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Determine your risk tolerance level:")
                Slider(
                    value = viewModel.riskTolerance,
                    onValueChange = { viewModel.riskTolerance = it },
                    modifier = Modifier.padding(vertical = 24.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Conservative", style = MaterialTheme.typography.labelSmall)
                    Text("Balanced", style = MaterialTheme.typography.labelSmall)
                    Text("Aggressive", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun InsuranceFlow(step: Int, viewModel: ProductViewModel) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        when (step) {
            1 -> {
                Text("Tell us about your Asset", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = viewModel.assetDescription,
                    onValueChange = { viewModel.assetDescription = it },
                    label = { Text("Asset Description (e.g. 2024 BMW X5)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
            2 -> {
                Text("Customize Coverage", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Monthly Excess: R ${viewModel.selectedExcess.toInt()}", fontWeight = FontWeight.Bold)
                Slider(
                    value = viewModel.selectedExcess.toFloat(),
                    onValueChange = { viewModel.selectedExcess = it.toDouble() },
                    valueRange = 0f..2000f
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text("Coverage Limit: R ${viewModel.coverageLimit.toInt()}", fontWeight = FontWeight.Bold)
                Slider(
                    value = viewModel.coverageLimit.toFloat(),
                    onValueChange = { viewModel.coverageLimit = it.toDouble() },
                    valueRange = 5000f..50000f
                )
            }
        }
    }
}
