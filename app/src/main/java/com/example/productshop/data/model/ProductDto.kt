package com.example.productshop.data.model

data class ProductDto(
    val id: Long,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String
)

data class ProductRequest(
    val customerId: Long,
    val productIds: List<Long>
)

data class EligibilityResultsDto(
    val isEligible: Boolean,
    val productId: Long,
    val failureReasons: List<String>? = null
)

data class TakeUpResponse(
    val subscriptionId: Long?,
    val fulfilmentResultList: List<FulfilmentResultDto>
)

data class FulfilmentResultDto(
    val checkName: String,
    val passed: Boolean,
    val failureMessage: String? = null,
    val productIds: List<Long>? = null
)
