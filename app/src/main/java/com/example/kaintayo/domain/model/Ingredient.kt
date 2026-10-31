package com.example.kaintayo.domain.model

data class Ingredient(
    val id: String,
    val name: String,
    val defaultUnit: MeasurementUnit,
    val estimatedPrice: Double
)