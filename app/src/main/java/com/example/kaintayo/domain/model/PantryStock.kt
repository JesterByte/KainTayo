package com.example.kaintayo.domain.model

data class PantryStock(
    val ingredientId: String,
    val quantity: Double,
    val unit: MeasurementUnit
) {
    init {
        require(quantity.isFinite() && quantity >= 0.0) {
            "Pantry quantity must be finite and nonnegative."
        }
    }
}