package com.example.productshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.productshop.data.model.ProductDto
import com.example.productshop.ui.viewmodel.ProductViewModel
import com.example.productshop.ui.viewmodel.FulfillmentUiState
import com.example.productshop.data.model.FulfilmentResultDto
import com.example.productshop.util.AnalyticsManager
import com.example.productshop.ui.viewmodel.AuthViewModel
import com.example.productshop.ui.viewmodel.KycViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.example.productshop.ui.viewmodel.KycUiState

@Preview(showBackground = true)
@Composable
fun ProductDetailScreenPreview() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val app = context.applicationContext as android.app.Application
    val productViewModel = ProductViewModel(app)
    val authViewModel = AuthViewModel(app)
    val kycViewModel = KycViewModel(app)
    val sampleProduct = ProductDto(1, "Life Insurance Plus", "Full coverage for your family", 299.0, "")
    productViewModel.products = listOf(sampleProduct)
    
    MaterialTheme {
        ProductDetailScreen(
            productId = 1,
            viewModel = productViewModel,
            authViewModel = authViewModel,
            kycViewModel = kycViewModel,
            notificationViewModel = com.example.productshop.ui.viewmodel.NotificationViewModel(),
            onBack = {},
            onHome = {},
            onApply = {},
            onProductClick = {},
            onLoginRedirect = { _ -> },
            onKycRedirect = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    kycViewModel: KycViewModel,
    notificationViewModel: com.example.productshop.ui.viewmodel.NotificationViewModel,
    onBack: () -> Unit,
    onHome: () -> Unit,
    onApply: () -> Unit,
    onProductClick: (Long) -> Unit,
    onLoginRedirect: (Long) -> Unit,
    onKycRedirect: () -> Unit
) {
    val product = viewModel.products.find { it.id == productId }
    val isKycLoading = kycViewModel.uiState is KycUiState.Loading
    val isKycVerified = kycViewModel.kycStatus?.primaryIndicator == true
    val fulfillmentUiState = viewModel.fulfillmentUiState
    
    var showGuestDialog by remember { mutableStateOf(false) }
    var showKycDialog by remember { mutableStateOf(false) }

    if (showGuestDialog) {
        AlertDialog(
            onDismissRequest = { showGuestDialog = false },
            title = { Text("Login Required") },
            text = { Text("Guest users cannot apply for products. Please log in or sign up to continue.") },
            confirmButton = {
                Button(onClick = {
                    showGuestDialog = false
                    product?.let { onLoginRedirect(it.id) }
                }) {
                    Text("Log In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGuestDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showKycDialog) {
        AlertDialog(
            onDismissRequest = { showKycDialog = false },
            title = { Text("Identity Verification Required") },
            text = { Text("To apply for this product, we need to verify your identity. It only takes a few minutes.") },
            confirmButton = {
                Button(onClick = {
                    showKycDialog = false
                    onKycRedirect()
                }) {
                    Text("Start Verification")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKycDialog = false }) {
                    Text("Later")
                }
            }
        )
    }

    if (fulfillmentUiState is FulfillmentUiState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetFulfillmentState() },
            title = { Text(fulfillmentUiState.message) },
            text = {
                Column {
                    if (fulfillmentUiState.failedChecks.isNotEmpty()) {
                        fulfillmentUiState.failedChecks.forEach { check ->
                            Text("• ${check.failureMessage ?: check.checkName}", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    } else {
                        Text("An unexpected error occurred. Please try again later.")
                    }
                }
            },
            confirmButton = {
                Button(onClick = { viewModel.resetFulfillmentState() }) {
                    Text("OK")
                }
            }
        )
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val shareProduct: (ProductDto) -> Unit = { p ->
        val shareUri = "https://product-shop.ziauddeen-mohamad.workers.dev/?product=${p.id}"
        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, "Check out this ${p.name} on Product Shop: $shareUri")
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, "Share ${p.name}")
        context.startActivity(shareIntent)
    }
    
    LaunchedEffect(product) {
        product?.let {
            AnalyticsManager.logSelectContent("product_view", it.id.toString())
            if (authViewModel.isGuest) {
                com.example.productshop.security.SessionManager.pendingProductId = it.id
            }
        }
        if (viewModel.products.isEmpty()) {
            viewModel.fetchProducts()
        }
        if (!authViewModel.isGuest) {
            kycViewModel.fetchProfileAndKyc()
        }
    }
    
    var isExpanded by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    if (product == null) {
        if (viewModel.isLoading || viewModel.products.isEmpty()) {
            com.example.productshop.ui.components.ProductDetailSkeleton()
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Product not found", style = MaterialTheme.typography.titleLarge)
            }
        }
        return
    }

    val productType = remember(product) { ProductType.fromProduct(product) }

    val relatedProducts = remember(product) {
        viewModel.products
            .filter { it.id != productId && ProductType.fromProduct(it) == productType }
            .distinctBy { it.id }
            .take(5)
    }

    val exploreMoreProducts = remember(product) {
        viewModel.products
            .filter { it.id != productId && ProductType.fromProduct(it) != productType }
            .distinctBy { it.id }
            .take(5)
    }

    val benefits = remember(product) { getBenefitsForProduct(product, productType) }
    val requirements = remember(product) { getRequirementsForProduct(product, productType) }

    val fallbackImage = remember(product) {
        when {
            product.name.contains("Insurance", ignoreCase = true) -> "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=800"
            product.name.contains("Investment", ignoreCase = true) -> "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800"
            else -> "https://images.unsplash.com/photo-1556742044-3c52d6e88c62?w=800"
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface)
            {
            TopAppBar(
                title = {
                    Text(
                        text = product.name,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 2,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Return to products"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { product?.let { shareProduct(it) } }) {
                        Icon(Icons.Default.Share, contentDescription = "Share product")
                    }
                    IconButton(onClick = onHome) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                )
                ,
                scrollBehavior = scrollBehavior
            )
        }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "R ${String.format("%.2f", product.price)}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "per month",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        enabled = !isKycLoading && fulfillmentUiState !is FulfillmentUiState.Loading,
                        onClick = { 
                            when {
                                authViewModel.isGuest -> {
                                    showGuestDialog = true
                                }
                                kycViewModel.uiState is KycUiState.Loading -> {
                                    // Ignore click or show loading message
                                }

                                !isKycVerified -> {
                                    showKycDialog = true
                                }
                                else -> {
                                    AnalyticsManager.logAddToCart(
                                        itemId = product.id.toString(),
                                        itemName = product.name,
                                        price = product.price.toDouble()
                                    )
                                    viewModel.startFulfillment(product)
                                    viewModel.validateEligibility {
                                        onApply()
                                    }
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .weight(1.2f),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (fulfillmentUiState is FulfillmentUiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = productType.actionButtonText(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                AsyncImage(
                    model = if (product.imageUrl.contains("placeholder") || product.imageUrl.isEmpty()) fallbackImage else product.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                )
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val description = product.description
                val annotatedString = buildAnnotatedString {
                    val limit = 150
                    if (isExpanded || description.length <= limit) {
                        append(description)
                        append(" ")
                    } else {
                        append(description.take(limit))
                        append("... ")
                    }

                    withStyle(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    ) {
                        append(if (isExpanded) "Read less" else "Read more")
                    }
                }

                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )

                if (isExpanded) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Key Benefits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        benefits.forEach { benefit ->
                            BenefitItem(benefit)
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Requirements",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        requirements.forEach { requirement ->
                            AssistChip(
                                onClick = { },
                                label = { Text(requirement) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
                
                if (relatedProducts.isNotEmpty() || exploreMoreProducts.isNotEmpty()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (relatedProducts.isNotEmpty()) {
                    Text(
                        text = "You might also like",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(if (relatedProducts.size > 5) relatedProducts.take(5) else relatedProducts) { related ->
                            RelatedProductItem(related) {
                                onProductClick(related.id)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }

                if (exploreMoreProducts.isNotEmpty()) {
                    Text(
                        text = "Explore more",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(exploreMoreProducts) { related ->
                            RelatedProductItem(related) {
                                onProductClick(related.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    ListItem(
        headlineContent = { 
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ) 
        },
        leadingContent = {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {}
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun RelatedProductItem(product: ProductDto, onClick: () -> Unit) {
    val fallbackImage = when {
        product.name.contains("Insurance", ignoreCase = true) -> "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=400"
        product.name.contains("Investment", ignoreCase = true) -> "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=400"
        else -> "https://images.unsplash.com/photo-1556742044-3c52d6e88c62?w=400"
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = if (product.imageUrl.contains("placeholder") || product.imageUrl.isEmpty()) fallbackImage else product.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                )
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "from R ${String.format("%.2f", product.price)}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

enum class ProductType(val label: String) {
    INVESTMENT("Investment Portfolio"),
    INSURANCE("Insurance Coverage"),
    CONTRACT("Device & Subscription");

    @Composable
    fun primaryColor(): Color = when (this) {
        INVESTMENT -> Color(0xFF2E7D32) // Soft Emerald Green for wealth
        INSURANCE -> Color(0xFF1565C0)  // Solid Trust Blue for protection
        CONTRACT -> Color(0xFFE65100)   // Vibrant Amber/Orange for tech/lifestyle
    }

    fun actionButtonText(): String = when (this) {
        INVESTMENT -> "Start Investing"
        INSURANCE -> "Get a Quote"
        CONTRACT -> "Apply Now"
    }

    companion object {
        fun fromProduct(product: ProductDto): ProductType {
            return when {
                product.name.contains("Investment", ignoreCase = true) ||
                        product.name.contains("VIP", ignoreCase = true) ||
                        product.name.contains("Islamic", ignoreCase = true) ||
                        product.name.contains("Short-Term", ignoreCase = true) ->
                    INVESTMENT

                product.name.contains("Insurance", ignoreCase = true) ||
                        product.name.contains("Commercial", ignoreCase = true) ->
                    INSURANCE

                else ->
                    CONTRACT
            }
        }
    }
}

// Fine-grained content differentiation logic based on product variations
private fun getBenefitsForProduct(product: ProductDto, type: ProductType): List<String> {
    return when (type) {
        ProductType.INVESTMENT -> {
            val base = mutableListOf("Compound interest yield generation", "Flexible online asset Tracking")
            when {
                product.name.contains("Islamic", ignoreCase = true) -> {
                    base.add(0, "100% Shariah-compliant asset structures")
                    base.add(1, "Ethical, non-interest pure-profit sharing")
                }
                product.name.contains("VIP", ignoreCase = true) -> {
                    base.add(0, "Personalized Private Wealth Manager access")
                    base.add(1, "Bespoke global market priority execution")
                }
                product.name.contains("Short-Term", ignoreCase = true) -> {
                    base.add("Quick liquidity options with minimal exit constraints")
                }
                else -> {
                    base.add("Tax-optimized wealth growth options")
                }
            }
            base
        }
        ProductType.INSURANCE -> {
            val base = mutableListOf("24/7 Rapid Incident Helpline support", "Simplified digital claims management")
            when {
                product.name.contains("Commercial", ignoreCase = true) -> {
                    base.add(0, "Business operations continuity indemnity")
                    base.add(1, "Liability mitigation frameworks protection")
                }
                else -> {
                    base.add(0, "Comprehensive immediate risk protection asset coverage")
                    base.add("Tailorable monthly excess structure adjustment flexibility")
                }
            }
            base
        }
        ProductType.CONTRACT -> {
            listOf(
                "Immediate brand new device deployment allocation",
                "Full comprehensive manufacturer dynamic device warranty",
                "Flexible lifecycle upgrade eligibility at month 21"
            )
        }
    }
}

private fun getRequirementsForProduct(product: ProductDto, type: ProductType): List<String> {
    val commonRequirements = listOf("Valid identification or verified passport documentation")
    return when (type) {
        ProductType.INVESTMENT -> {
            val reqs = commonRequirements.toMutableList()
            reqs.add("Completed FICA regulatory compliance onboarding details")
            if (product.name.contains("VIP", ignoreCase = true)) {
                reqs.add("Verified Net Asset Value minimum declaration verification proof")
            } else {
                reqs.add("Minimum starting capital allocation clearance authorization")
            }
            reqs
        }
        ProductType.INSURANCE -> {
            val reqs = commonRequirements.toMutableList()
            if (product.name.contains("Commercial", ignoreCase = true)) {
                reqs.add("Valid active corporate legal registry registration layout")
                reqs.add("Detailed historic risk loss declaration files update")
            } else {
                reqs.add("Detailed asset physical risk assessment profiling assessment")
                reqs.add("Authorized regular active monthly debit check framework")
            }
            reqs
        }
        ProductType.CONTRACT -> {
            listOf(
                "3 Months recent verifiable banking salary deposit proof statement",
                "Employment stability active verification pay slip documentation",
                "Passed standard transparent personal affordability credit assessment checks"
            )
        }
    }
}
