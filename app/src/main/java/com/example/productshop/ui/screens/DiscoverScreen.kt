package com.example.productshop.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.productshop.data.model.ProductDto
import com.example.productshop.ui.components.ShimmerItem
import com.example.productshop.ui.viewmodel.ProductViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.animation.animateColorAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: ProductViewModel,
    onProductClick: (Long) -> Unit
) {
    val products = viewModel.products
    
    // Updated Categories: Removed Mobile, added Contract
    val categories = remember(products) {
        val baseCategories = mutableSetOf("All")
        products.forEach { product ->
            if (product.name.contains("Insurance", ignoreCase = true)) baseCategories.add("Insurance")
            if (product.name.contains("Investment", ignoreCase = true)) baseCategories.add("Investments")
            if (product.name.contains("Account", ignoreCase = true)) baseCategories.add("Accounts")
            if (product.name.contains("Contract", ignoreCase = true)) baseCategories.add("Contracts")
        }
        baseCategories.toList()
    }
    
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val filteredProducts = remember(selectedCategory, products) {
        if (selectedCategory == "All") {
            products
        } else {
            val searchTerm = selectedCategory.removeSuffix("s")
            products.filter { 
                it.name.contains(searchTerm, ignoreCase = true) || 
                it.description.contains(searchTerm, ignoreCase = true) 
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(52.dp)
                        ) {

                            Canvas(
                                modifier = Modifier.size(70.dp)
                            ) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            primaryColor.copy(alpha = 0.18f),
                                            Color.Transparent
                                        ),
                                        center = center,
                                        radius = size.minDimension / 2
                                    )
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "InsureTechGuard",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search products"
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "View notifications"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val items = listOf(
                    NavigationItem("Home", Icons.Default.Home),
                    NavigationItem("Subscriptions", Icons.Default.PlayArrow),
                    NavigationItem("Cart", Icons.Default.ShoppingCart),
                    NavigationItem("Account", Icons.Default.AccountCircle)
                )

                items.forEachIndexed { index, item ->

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(item.label)
                        },
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (viewModel.isLoading) {
                DiscoverLoadingState()
            } else if (viewModel.errorMessage != null) {
                DiscoverErrorState(viewModel.errorMessage!!, onRetry = { scope.launch { viewModel.fetchProducts() } })
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        DynamicHeroCarousel()
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "Discover",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            items(categories) { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }

                    items(filteredProducts) { product ->
                        DiscoverProductItem(product) {
                            onProductClick(product.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DynamicHeroCarousel() {
    val carouselItems = listOf(
        CarouselData(
            title = "Device Contracts",
            desc = "Get the latest devices with affordable monthly payments",
            imageUrl = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800"
        ),
        CarouselData(
            title = "Life Insurance",
            desc = "Protect what matters most with financial security for your loved ones",
            imageUrl = "https://images.unsplash.com/photo-1511988617509-a57c8a288659?w=800"
        ),
        CarouselData(
            title = "Smart Investing",
            desc = "Build your wealth with investment solutions tailored to your goals",
            imageUrl = "https://images.unsplash.com/photo-1559526324-593bc073d938?w=800"
        )
    )

    val pagerState = rememberPagerState(pageCount = { carouselItems.size })
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            if (!pagerState.isScrollInProgress) {
                val nextPage = (pagerState.currentPage + 1) % carouselItems.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(28.dp))
        ) { page ->
            val item = carouselItems[page]
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().alpha(0.6f)
                )
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(24.dp)
                ) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        item.desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(carouselItems.size) { index ->
                val color = if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 12.dp else 6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun DiscoverProductItem(product: ProductDto, onClick: () -> Unit) {
    val placeholderImage = when {
        product.name.contains("Insurance", ignoreCase = true) -> "https://images.unsplash.com/photo-1450101499163-c8848c66ca85?w=400"
        product.name.contains("Investment", ignoreCase = true) -> "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=400"
        else -> "https://images.unsplash.com/photo-1556742044-3c52d6e88c62?w=400"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(modifier = Modifier.height(140.dp).fillMaxWidth().background(Color.White)) {
                AsyncImage(
                    model = if (product.imageUrl.contains("placeholder") || product.imageUrl.isEmpty()) placeholderImage else product.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }
            
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2, // Allow 2 lines to show 'Commercial Short' and 'Long'
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "R ${product.price} p/m",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        }
    }
}

@Composable
fun DiscoverLoadingState() {
    Column(modifier = Modifier.padding(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        )
        Spacer(modifier = Modifier.height(24.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(6) {
                ShimmerItem()
            }
        }
    }
}

@Composable
fun DiscoverErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                "Oops! Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "We couldn't load the products. Please check your connection.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

data class NavigationItem(val label: String, val icon: ImageVector)
data class CarouselData(val title: String, val desc: String, val imageUrl: String)
