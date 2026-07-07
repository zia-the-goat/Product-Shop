package com.example.productshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.productshop.ui.viewmodel.SubscriptionUiState
import com.example.productshop.ui.viewmodel.SubscriptionViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailsScreen(
    subscriptionId: Long,
    viewModel: SubscriptionViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit
) {
    val uiState = viewModel.uiState
    val subscription = (uiState as? SubscriptionUiState.Success)?.subscriptions?.find { it.subscriptionId == subscriptionId }
    var showCancelDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchSubscriptions()
    }

    if (subscription == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Subscription Details") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onHome) {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Subscription not found", style = MaterialTheme.typography.titleLarge)
            }
        }
        return
    }

    val product = subscription.product.firstOrNull() ?: return

    val fallbackImage = when {
        product.name.contains("Insurance", ignoreCase = true) -> "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=800"
        product.name.contains("Investment", ignoreCase = true) -> "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800"
        else -> "https://images.unsplash.com/photo-1556742044-3c52d6e88c62?w=800"
    }

    if (showCancelDialog) {
        // Calculate cancellation fee based on proximity to next payment date
        val isPenaltyApplied = remember(subscription) {
            try {
                val dateStr = subscription.dateSubscribed ?: ""
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val subscribeDate = LocalDate.parse(dateStr, formatter)
                val today = LocalDate.now()
                
                val dayOfMonth = subscribeDate.dayOfMonth
                var nextPaymentDate = today.withDayOfMonth(
                    if (dayOfMonth <= today.month.length(today.isLeapYear())) dayOfMonth 
                    else today.month.length(today.isLeapYear())
                )
                
                if (nextPaymentDate.isBefore(today) || nextPaymentDate.isEqual(today)) {
                    nextPaymentDate = nextPaymentDate.plusMonths(1)
                }
                
                val daysUntilPayment = ChronoUnit.DAYS.between(today, nextPaymentDate)
                daysUntilPayment <= 7
            } catch (e: Exception) {
                false
            }
        }
        
        val cancelFee = if (isPenaltyApplied) product.price * 0.1 else 0.0
        
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Subscription?") },
            text = { 
                Column {
                    Text("Are you sure you want to cancel your ${product.name} subscription?")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isPenaltyApplied) {
                        Text(
                            "A cancellation fee of R ${String.format("%.2f", cancelFee)} will be applied because your next payment is within 7 days.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        Text(
                            "No cancellation fee will be applied.",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelSubscription(subscription.subscriptionId) {
                            showCancelDialog = false
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Cancellation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Go Back")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onHome) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                AsyncImage(
                    model = if (product.imageUrl.contains("placeholder") || product.imageUrl.isEmpty()) fallbackImage else product.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(32.dp)
                )
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "R ${String.format("%.2f", product.price)} per month",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Subscription Info",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        InfoRow(label = "Subscription ID", value = "#${subscription.subscriptionId}")
                        InfoRow(label = "Status", value = "Active")
                        InfoRow(label = "Billing Cycle", value = "Monthly")
                        InfoRow(label = "Start Date", value = subscription.dateSubscribed ?: "N/A")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "About this Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { showCancelDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancel Subscription", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
