package com.example.kaintayo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kaintayo.domain.model.MeasurementUnit

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    val defaultUnit: MeasurementUnit,

    // Price expressed in centavos.
    val estimatedPriceCentavos: Long,

    // Example: ₱150.00 per 1 kilogram.
    val priceQuantity: Double,

    val priceUnit: MeasurementUnit
)