package com.example.productshop.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitManager {

    val service: ProductService = Retrofit.Builder()
        .baseUrl("https://boozy-supply-ripping.ngrok-free.dev/v1/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ProductService::class.java)
}