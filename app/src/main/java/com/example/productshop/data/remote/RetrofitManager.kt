package com.example.productshop.data.remote

import com.example.productshop.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitManager {

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("ngrok-skip-browser-warning", "true")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ProductService = retrofit.create(ProductService::class.java)
    val authService: AuthService = retrofit.create(AuthService::class.java)
    val profileService: ProfileService = retrofit.create(ProfileService::class.java)
    val kycService: KycService = retrofit.create(KycService::class.java)
    val customerService: CustomerService = retrofit.create(CustomerService::class.java)
    val dhaService: DhaService = retrofit.create(DhaService::class.java)
}