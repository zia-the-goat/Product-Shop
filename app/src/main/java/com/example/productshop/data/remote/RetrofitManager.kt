package com.example.productshop.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitManager {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://boozy-supply-ripping.ngrok-free.dev/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: ProductService = retrofit.create(ProductService::class.java)
    val authService: AuthService = retrofit.create(AuthService::class.java)
}