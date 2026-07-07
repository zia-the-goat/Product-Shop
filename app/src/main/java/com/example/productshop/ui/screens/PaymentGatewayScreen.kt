package com.example.productshop.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.productshop.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewayScreen(
    viewModel: ProductViewModel,
    onPaymentComplete: () -> Unit,
    onCancel: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    
    // Custom Gateway Theme
    val gatewayPrimary = Color(0xFF2D3436)
    val gatewayAccent = Color(0xFF0984E3)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF00B894))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SECURE PAY GATEWAY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = Color.Red)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF1F2F6))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF1F2F6))
        ) {
            when {
                isProcessing -> {
                    ProcessingState()
                    LaunchedEffect(Unit) {
                        delay(3000)
                        isProcessing = false
                        isSuccess = true
                        viewModel.isPaymentVerified = true
                    }
                }
                isSuccess -> {
                    SuccessState(onPaymentComplete)
                }
                else -> {
                    PaymentForm(
                        viewModel = viewModel,
                        onPayClick = { isProcessing = true }
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentForm(viewModel: ProductViewModel, onPayClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Card Payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Transaction: #PS-${(10000..99999).random()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = viewModel.cardNumber,
                    onValueChange = { if (it.length <= 16) viewModel.cardNumber = it },
                    label = { Text("Card Number") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("XXXX XXXX XXXX XXXX") },
                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = viewModel.cardExpiry,
                        onValueChange = { if (it.length <= 5) viewModel.cardExpiry = it },
                        label = { Text("Expiry (MM/YY)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = viewModel.cardCvv,
                        onValueChange = { if (it.length <= 3) viewModel.cardCvv = it },
                        label = { Text("CVV") },
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onPayClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0984E3)),
                    enabled = viewModel.cardNumber.length >= 12
                ) {
                    Text("AUTHORIZE PAYMENT", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
            Spacer(modifier = Modifier.width(4.dp))
            Text("PCI-DSS COMPLIANT SYSTEM", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun ProcessingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            color = Color(0xFF0984E3),
            strokeWidth = 6.dp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Verifying Transaction...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Communicating with your bank",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun SuccessState(onComplete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF00B894)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(56.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Authorization Successful",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your payment has been verified by Secure Pay.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onComplete,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D3436))
        ) {
            Text("RETURN TO MERCHANT")
        }
    }
}
