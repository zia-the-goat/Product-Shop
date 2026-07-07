package com.example.productshop.data.model

data class LivingStatusDto(
    val livingStatus: String, // "alive" or "deceased"
    val deceasedDate: String? = null
)

data class MaritalStatusDto(
    val status: String, // "Single", "Widowed", "Divorced", "Married"
    val effectiveFrom: String,
    val effectiveTo: String? = null
)

data class MaritalStatusList(
    val currentStatus: MaritalStatusDto,
    val previousStatus: List<MaritalStatusDto>
)

data class DuplicateIDDocumentCheck(
    val hasDuplicateId: Boolean,
    val duplicateIdIssueDate: String? = null
)
