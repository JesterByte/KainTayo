package com.jesterbyte.kaintayo.domain.model

data class Ingredient(
    val id: String,
    val name: String,
    val defaultUnit: MeasurementUnit,
    val estimatedPriceCentavos: Long,
    val priceQuantity: Double,
    val priceUnit: MeasurementUnit,
    val purchaseQuantity: Double = priceQuantity,
    val purchaseUnit: MeasurementUnit = priceUnit
)
