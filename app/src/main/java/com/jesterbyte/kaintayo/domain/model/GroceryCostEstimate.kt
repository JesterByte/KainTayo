
package com.jesterbyte.kaintayo.domain.model

data class GroceryCostItem(
    val ingredientId: String,
    val name: String,
    val quantity: Double,
    val unit: MeasurementUnit,
    val estimatedCostCentavos: Double
)

data class GroceryCostEstimate(
    val items: List<GroceryCostItem>,
    val totalCostCentavos: Double
)
