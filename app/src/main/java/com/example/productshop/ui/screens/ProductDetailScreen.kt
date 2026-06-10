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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.productshop.data.model.ProductDto
import com.example.productshop.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    val product = viewModel.products.find { it.id == productId }
    var isExpanded by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found", style = MaterialTheme.typography.titleLarge)
        }
        return
    }

    val productType = when {
        product.name.contains("Investment", ignoreCase = true) ||
                product.name.contains("VIP", ignoreCase = true) ||
                product.name.contains("Islamic", ignoreCase = true) ||
                product.name.contains("Short-Term", ignoreCase = true) ->
            ProductType.INVESTMENT

        product.name.contains("Insurance", ignoreCase = true) ||
                product.name.contains("Commercial", ignoreCase = true) ->
            ProductType.INSURANCE

        else ->
            ProductType.CONTRACT
    }

    val relatedProducts = run {
        fun getType(product: ProductDto): ProductType {
            return when {
                product.name.contains("Investment", ignoreCase = true) ||
                        product.name.contains("VIP", ignoreCase = true) ||
                        product.name.contains("Islamic", ignoreCase = true) ||
                        product.name.contains("Short-Term", ignoreCase = true) ->
                    ProductType.INVESTMENT

                product.name.contains("Insurance", ignoreCase = true) ||
                        product.name.contains("Commercial", ignoreCase = true) ->
                    ProductType.INSURANCE

                else ->
                    ProductType.CONTRACT
            }
        }

        val sameTypeProducts = viewModel.products
            .filter {
                it.id != productId &&
                        getType(it) == productType
            }

        val otherProducts = viewModel.products
            .filter {
                it.id != productId &&
                        getType(it) != productType
            }

        (sameTypeProducts + otherProducts)
            .distinctBy { it.id }
            .take(3)
    }

    val benefits = getBenefitsForProduct(product, productType)
    val requirements = getRequirementsForProduct(product, productType)

    val fallbackImage = when {
        product.name.contains("Insurance", ignoreCase = true) -> "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=800"
        product.name.contains("Investment", ignoreCase = true) -> "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=800"
        else -> "https://images.unsplash.com/photo-1556742044-3c52d6e88c62?w=800"
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { /* Empty title to avoid redundancy with the body title */ },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return to products")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.Share, contentDescription = "Share product")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "R ${product.price}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "per month",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(12.dp), // M3 compliant radii
                        modifier = Modifier
                            .height(52.dp)
                            .fillMaxWidth(0.65f),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text("Add to cart", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
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
                    .height(320.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                AsyncImage(
                    model = if (product.imageUrl.contains("placeholder") || product.imageUrl.isEmpty()) fallbackImage else product.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(40.dp)
                )
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                val description = product.description
                val annotatedString = buildAnnotatedString {
                    val limit = 120
                    if (isExpanded || description.length <= limit) {
                        append(description)
                        append(" ")
                    } else {
                        append(description.take(limit))
                        append("... ")
                    }

                    withStyle(style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )) {
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
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Key Benefits",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    benefits.forEach { benefit ->
                        BenefitItem(benefit)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Requirements",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    requirements.forEach { requirement ->
                        BenefitItem(requirement)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "You might also like",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(relatedProducts) { related ->
                        RelatedProductItem(related)
                    }
                }
            }
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(6.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {}
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun RelatedProductItem(product: ProductDto) {
    val fallbackImage = when {
        product.name.contains("Insurance", ignoreCase = true) -> "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=400"
        product.name.contains("Investment", ignoreCase = true) -> "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=400"
        else -> "https://images.unsplash.com/photo-1556742044-3c52d6e88c62?w=400"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.width(180.dp)
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(Color.White)) {
                AsyncImage(
                    model = if (product.imageUrl.contains("placeholder") || product.imageUrl.isEmpty()) fallbackImage else product.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(12.dp)
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
                Text(
                    text = "from R ${product.price}",
                    style = MaterialTheme.typography.labelMedium,
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
        INVESTMENT -> "Open Investment"
        INSURANCE -> "Get Cover Now"
        CONTRACT -> "Apply for Contract"
    }

    companion object {
        fun fromProduct(product: ProductDto): ProductType {
            return when {
                product.name.contains("Investment", ignoreCase = true) -> INVESTMENT
                product.name.contains("Insurance", ignoreCase = true) -> INSURANCE
                else -> CONTRACT
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
