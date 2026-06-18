package com.example.productshop.data.model

import com.google.gson.annotations.SerializedName

data class KycDto(
    val primaryIndicator: Boolean,
    val secondaryIndicator: Boolean,
    val taxCompliance: String // "red", "amber", "green"
)

data class DocumentDto(
    val id: Long? = null,
    val document: String,
    val type: String // "PNG", "JPEG", "PDF"
)

data class CreateDocumentResponse(
    val documentId: Long
)

data class SubscriptionDto(
    val subscriptionId: Long,
    val product: List<ProductDto>
)

data class SubscriptionsDto(
    val subscriptions: List<SubscriptionDto>
)

data class CustomerDto(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val idNumber: String,
    val customerType: CustomerTypeDto?,
    val customerAccounts: List<AccountTypeDto>?
)

data class CustomerTypeDto(
    val id: Long,
    val name: String,
    val description: String
)

data class AccountTypeDto(
    val id: Long,
    val name: String,
    val description: String
)
