package com.example.productshop.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import androidx.compose.material.icons.filled.Close
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
import com.example.productshop.data.model.NotificationDto
import com.example.productshop.ui.components.ShimmerItem
import com.example.productshop.ui.viewmodel.AuthViewModel
import com.example.productshop.ui.viewmodel.KycViewModel
import com.example.productshop.ui.viewmodel.ProductViewModel
import com.example.productshop.ui.viewmodel.SubscriptionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.Canvas
import androidx.compose.animation.animateColorAsState
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    selectedTab: Int,
    viewModel: ProductViewModel,
    kycViewModel: KycViewModel,
    authViewModel: AuthViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    notificationViewModel: com.example.productshop.ui.viewmodel.NotificationViewModel,
    onTabSelected: (Int) -> Unit,
    onProductClick: (Long) -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    onStartKyc: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToSupport: () -> Unit = {}
) {
    val products = viewModel.products
    
    val drawCardFactors = remember {
        listOf(
            DrawCardData("Instant Approval", "Get verified in minutes", Icons.Default.FlashOn, Color(0xFF4CAF50)),
            DrawCardData("Affordable", "Best premiums in market", Icons.Default.Payments, Color(0xFF2196F3)),
            DrawCardData("Transparent", "No hidden fees or terms", Icons.Default.Info, Color(0xFFFF9800)),
            DrawCardData("Flexible", "Easy plan adjustments", Icons.Default.History, Color(0xFF9C27B0)),
            DrawCardData("Secure", "Bank-grade protection", Icons.Default.Security, Color(0xFFF44336))
        )
    }
    
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
    var sortByPriceLowHigh by remember { mutableStateOf<Boolean?>(null) } // null = no sort, true = low-high, false = high-low
    var isSearchActive by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit App") },
            text = { Text("Are you sure you want to exit the application?") },
            confirmButton = {
                Button(onClick = { 
                    android.os.Process.killProcess(android.os.Process.myPid())
                }) {
                    Text("Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    val filteredProducts = remember(selectedCategory, sortByPriceLowHigh, products, viewModel.searchQuery) {
        val baseFiltered = if (viewModel.searchQuery.isEmpty()) {
            if (selectedCategory == "All") {
                products
            } else {
                val searchTerm = selectedCategory.removeSuffix("s")
                products.filter { 
                    it.name.contains(searchTerm, ignoreCase = true) || 
                    it.description.contains(searchTerm, ignoreCase = true) 
                }
            }
        } else {
            products.filter {
                it.name.contains(viewModel.searchQuery, ignoreCase = true) ||
                it.description.contains(viewModel.searchQuery, ignoreCase = true)
            }
        }

        when (sortByPriceLowHigh) {
            true -> baseFiltered.sortedBy { it.price }
            false -> baseFiltered.sortedByDescending { it.price }
            else -> baseFiltered
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = viewModel.searchQuery,
                            onValueChange = { viewModel.searchQuery = it },
                            placeholder = { Text("Search products...", style = MaterialTheme.typography.bodyLarge) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            trailingIcon = {
                                IconButton(onClick = { 
                                    viewModel.searchQuery = ""
                                    isSearchActive = false 
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close search")
                                }
                            }
                        )
                    } else {
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
                                    contentDescription = "App Logo",
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
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { 
                            onTabSelected(0)
                            isSearchActive = true 
                        }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search products"
                            )
                        }

                        IconButton(onClick = { showNotifications = true }) {
                            Box {
                                    Icon(
                                        Icons.Default.Notifications,
                                        contentDescription = "View notifications"
                                    )
                                    val unreadCount = notificationViewModel.notifications.filter { !it.isRead }.size
                                    if (unreadCount > 0) {
                                        Surface(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .align(Alignment.TopEnd)
                                                .offset(x = 4.dp, y = (-4).dp),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.error
                                        ) {
                                            Text(
                                                text = unreadCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                modifier = Modifier.padding(1.dp)
                                            )
                                        }
                                    }
                            }
                        }
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
                            onTabSelected(index)
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
            when (selectedTab) {
                0 -> {
                    if (viewModel.isLoading) {
                        DiscoverLoadingState()
                    } else if (viewModel.errorMessage != null) {
                        DiscoverErrorState(viewModel.errorMessage!!, onRetry = { scope.launch { viewModel.fetchProducts() } })
                    } else {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(6),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item(span = { GridItemSpan(6) }) {
                                DynamicHeroCarousel()
                            }

                            item(span = { GridItemSpan(6) }) {
                                Text(
                                    text = "Why Choose Us?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                                )
                            }

                            items(drawCardFactors, span = { GridItemSpan(2) }) { factor ->
                                DrawCardItem(factor)
                            }

                            item(span = { GridItemSpan(6) }) {
                                Text(
                                    text = "Quick Access",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                                )
                            }

                            item(span = { GridItemSpan(3) }) {
                                QuickLinkItem(
                                    title = "Short-Term Products",
                                    icon = Icons.Default.Star,
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    onClick = {
                                        selectedCategory = "Investments"
                                        viewModel.searchQuery = "Short-Term"
                                        scope.launch {
                                            gridState.animateScrollToItem(10) // Land on Discover header
                                        }
                                    }
                                )
                            }

                            item(span = { GridItemSpan(3) }) {
                                QuickLinkItem(
                                    title = "Starter Insurance",
                                    icon = Icons.Default.Whatshot,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                    onClick = {
                                        selectedCategory = "Insurance"
                                        viewModel.searchQuery = "Life"
                                        scope.launch {
                                            gridState.animateScrollToItem(10)
                                        }
                                    }
                                )
                            }

                            item(span = { GridItemSpan(6) }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Discover",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                    )

                                    // Sorting Chips - Research Doc: Sorting near top
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(
                                            selected = sortByPriceLowHigh == true,
                                            onClick = {
                                                sortByPriceLowHigh = if (sortByPriceLowHigh == true) null else true
                                            },
                                            label = { Text("Price ↓", fontSize = 12.sp) },
                                            leadingIcon = if (sortByPriceLowHigh == true) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                        FilterChip(
                                            selected = sortByPriceLowHigh == false,
                                            onClick = {
                                                sortByPriceLowHigh = if (sortByPriceLowHigh == false) null else false
                                            },
                                            label = { Text("Price ↑", fontSize = 12.sp) },
                                            leadingIcon = if (sortByPriceLowHigh == false) {
                                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                            } else null,
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                            }

                            item(span = { GridItemSpan(6) }) {
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

                            items(filteredProducts, span = { GridItemSpan(3) }) { product ->
                                DiscoverProductItem(product) {
                                    onProductClick(product.id)
                                }
                            }
                        }
                    }
                }
                1 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        SubscriptionsScreen(
                            viewModel = subscriptionViewModel,
                            isGuest = authViewModel.isGuest,
                            onLoginRequest = onLogout, // Reuse logout logic to return to landing
                            onNavigateToDiscover = { onTabSelected(0) },
                            onSubscriptionClick = onSubscriptionClick
                        )
                    }
                }
                2 -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AccountScreen(
                            viewModel = kycViewModel,
                            authViewModel = authViewModel,
                            onStartKyc = onStartKyc,
                            onLogout = onLogout,
                            onNavigateToSettings = onNavigateToSettings,
                            onNavigateToSupport = onNavigateToSupport
                        )
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Coming Soon", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }

        if (showNotifications) {
            ModalBottomSheet(
                onDismissRequest = { showNotifications = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )
                    
                    if (notificationViewModel.notifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No new notifications", color = Color.Gray)
                        }
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn {
                            items(notificationViewModel.notifications) { notification ->
                                ListItem(
                                    headlineContent = { Text(notification.title, fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text(notification.message) },
                                    leadingContent = {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = if (notification.isRead) Color.Gray else MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingContent = {
                                        Text(
                                            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(notification.timestamp)),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    modifier = Modifier.clickable { 
                                        notificationViewModel.markAsRead(notification.id)
                                    }
                                )
                            }
                            
                            item {
                                TextButton(
                                    onClick = { notificationViewModel.clearAll() },
                                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                                ) {
                                    Text("Clear All")
                                }
                            }
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
                .clip(RoundedCornerShape(24.dp))
        ) { page ->
            val item = carouselItems[page]
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = "Banner: ${item.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Gradient overlay for better text readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                                startY = 300f
                            )
                        )
                )
                
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        item.desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        
        Row(
            modifier = Modifier
                .padding(top = 12.dp)
                .align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(carouselItems.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp, label = "width")
                val color by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    label = "color"
                )
                
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
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

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp) // Slightly taller for better spacing
            .clickable(onClick = onClick)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .height(130.dp)
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                AsyncImage(
                    model = if (product.imageUrl.contains("placeholder") || product.imageUrl.isEmpty()) placeholderImage else product.imageUrl,
                    contentDescription = "Product Image: ${product.name}",
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
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "R ${String.format("%.2f", product.price)} p/m",
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

@Composable
fun DrawCardItem(factor: DrawCardData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.height(110.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = factor.icon,
                contentDescription = null,
                tint = factor.color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = factor.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = factor.desc,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun QuickLinkItem(
    title: String, 
    icon: ImageVector, 
    containerColor: Color, 
    contentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier.height(90.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

data class NavigationItem(val label: String, val icon: ImageVector)
data class CarouselData(val title: String, val desc: String, val imageUrl: String)
data class DrawCardData(val title: String, val desc: String, val icon: ImageVector, val color: Color)
