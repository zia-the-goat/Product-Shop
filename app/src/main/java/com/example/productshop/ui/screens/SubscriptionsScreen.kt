package com.example.productshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.productshop.data.model.SubscriptionDto
import com.example.productshop.ui.viewmodel.SubscriptionUiState
import com.example.productshop.ui.viewmodel.SubscriptionViewModel

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun SubscriptionsScreen(
    viewModel: SubscriptionViewModel,
    isGuest: Boolean = false,
    onLoginRequest: () -> Unit = {},
    onNavigateToDiscover: () -> Unit = {},
    onSubscriptionClick: (Long) -> Unit = {}
) {
    val uiState = viewModel.uiState
    var subscriptionToCancel by remember { mutableStateOf<com.example.productshop.data.model.SubscriptionDto?>(null) }

    LaunchedEffect(Unit) {
        if (!isGuest) {
            viewModel.fetchSubscriptions()
        }
    }

    if (subscriptionToCancel != null) {
        val product = subscriptionToCancel!!.product.firstOrNull()
        
        // Calculate cancellation fee based on proximity to next payment date
        val isPenaltyApplied = remember(subscriptionToCancel) {
            try {
                val dateStr = subscriptionToCancel!!.dateSubscribed ?: ""
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val subscribeDate = LocalDate.parse(dateStr, formatter)
                val today = LocalDate.now()
                
                // Next payment date is same day of month as subscription date
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
                false // Default to no penalty if date parsing fails
            }
        }
        
        val cancelFee = if (isPenaltyApplied) (product?.price ?: 0.0) * 0.1 else 0.0
        
        AlertDialog(
            onDismissRequest = { subscriptionToCancel = null },
            title = { Text("Cancel Subscription?") },
            text = { 
                Column {
                    Text("Are you sure you want to cancel your ${product?.name} subscription?")
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
                        viewModel.cancelSubscription(subscriptionToCancel!!.subscriptionId) {
                            subscriptionToCancel = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Confirm Cancellation")
                }
            },
            dismissButton = {
                TextButton(onClick = { subscriptionToCancel = null }) {
                    Text("Go Back")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Subscriptions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isGuest) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Log in to view your subscriptions.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onLoginRequest,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log In Now")
                    }
                }
            }
        } else {
            when (uiState) {
                is SubscriptionUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is SubscriptionUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is SubscriptionUiState.Success -> {
                    if (uiState.subscriptions.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "No active subscriptions found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = onNavigateToDiscover,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Discover Products")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(uiState.subscriptions.reversed()) { subscription ->
                                SubscriptionItem(
                                    subscription = subscription,
                                    onCancel = { subscriptionToCancel = subscription },
                                    onClick = { onSubscriptionClick(subscription.subscriptionId) }
                                )
                            }
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun SubscriptionItem(subscription: SubscriptionDto, onCancel: () -> Unit, onClick: () -> Unit) {
    val product = subscription.product.firstOrNull() ?: return

    val fallbackImage = when {
        product.name.contains("Insurance", ignoreCase = true) -> "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=400"
        product.name.contains("Investment", ignoreCase = true) -> "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=400"
        else -> "https://images.unsplash.com/photo-1556742044-3c52d6e88c62?w=400"
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        ListItem(
            headlineContent = { 
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                ) 
            },
            supportingContent = {
                Column {
                    Text(
                        text = "Subscribed on: ${subscription.dateSubscribed ?: "N/A"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "R ${String.format("%.2f", product.price)} p/m",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(
                        onClick = onCancel,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel Subscription", style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
            leadingContent = {
                Box(
                    modifier = Modifier
                        .size(64.dp) // Slightly larger
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = if (product.imageUrl.contains("placeholder") || product.imageUrl.isEmpty()) fallbackImage else product.imageUrl,
                        contentDescription = "Product: ${product.name}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
        )
    }
}
