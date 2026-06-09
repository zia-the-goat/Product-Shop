package com.example.productshop
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

// 1. Data Models (Matched to your product.yaml)
data class ProductDto(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String
)

// 2. Retrofit API Interface
interface ProductService {
    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}

// 3. Dynamic Retrofit Management
class RetrofitManager(initialIp: String) {
    var currentIp by mutableStateOf(initialIp)
    
    val service: ProductService
        get() = Retrofit.Builder()
            .baseUrl("$currentIp/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductService::class.java)
}

// 4. ViewModel to manage state
class ProductViewModel : ViewModel() {
    var products by mutableStateOf<List<ProductDto>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    
    // Default to Emulator IP, but allows changing
    val retrofitManager = RetrofitManager("https://boozy-supply-ripping.ngrok-free.dev")

    suspend fun fetchProducts() {
        isLoading = true
        try {
            products = retrofitManager.service.getProducts()
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = "Failed to load products from ${retrofitManager.currentIp}: ${e.message}"
        } finally {
            isLoading = false
        }
    }
}

// 5. Main Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ProductScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(viewModel: ProductViewModel = viewModel()) {
    val scope = rememberCoroutineScope()

    // Fetch products when the screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Shop") }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator()
            } else if (viewModel.errorMessage != null) {
                Text(text = viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { 
                    scope.launch { viewModel.fetchProducts() } 
                }) {
                    Text("Retry")
                }
            } else {
                LazyColumn {
                    items(viewModel.products) { product ->
                        ProductItem(product)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: ProductDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "R ${product.price}", style = MaterialTheme.typography.bodyMedium)
                Text(text = product.description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
