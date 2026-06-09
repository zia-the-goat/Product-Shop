package com.example.productshop
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

// 3. Retrofit Instance
object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/v1/" // 10.0.2.2 is localhost for Android Emulator

    val instance: ProductService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductService::class.java)
    }
}

// 4. ViewModel to manage state
class ProductViewModel : ViewModel() {
    var products by mutableStateOf<List<ProductDto>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    suspend fun fetchProducts() {
        isLoading = true
        try {
            products = RetrofitClient.instance.getProducts()
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = "Failed to load products: ${e.message}"
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

@Composable
fun ProductScreen(viewModel: ProductViewModel = viewModel()) {
    // Fetch products when the screen loads
    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Product Shop", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isLoading) {
            CircularProgressIndicator()
        } else if (viewModel.errorMessage != null) {
            Text(text = viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn {
                items(viewModel.products) { product ->
                    ProductItem(product)
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
