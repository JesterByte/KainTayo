package com.example.kaintayo.domain.model

data class GroceryPurchaseItem(
    val ingredientId: String,
    val name: String,

    // Amount required by the recipes.
    val requiredQuantity: Double,
    val requiredUnit: MeasurementUnit,

    // Size of one purchase unit, such as 1 kg or 1 liter.
    val purchaseQuantity: Double,
    val purchaseUnit: MeasurementUnit,

    // Number of whole purchase units to buy.
    val purchaseCount: Long,

    // Total amount purchased after rounding up.
    val purchasedQuantity: Double,

    // Cost of all purchase units.
    val estimatedCostCentavos: Double
)

data class GroceryPurchaseEstimate(
    val items: List<GroceryPurchaseItem>,
    val totalCostCentavos: Double
)