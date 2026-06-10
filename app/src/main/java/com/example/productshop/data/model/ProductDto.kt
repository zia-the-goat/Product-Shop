package com.example.productshop.data.model

data class ProductDto(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String
)
