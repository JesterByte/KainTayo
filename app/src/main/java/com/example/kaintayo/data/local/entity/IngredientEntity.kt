package com.example.kaintayo.data.local.entity

import androidx.room.ColumnInfo
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

    // Price basis, for example ₱210 per 1 kilogram.
    val priceQuantity: Double,

    val priceUnit: MeasurementUnit,

    // Amount purchased in one whole purchase unit.
    @ColumnInfo(defaultValue = "1.0")
    val purchaseQuantity: Double = priceQuantity,

    @ColumnInfo(defaultValue = "'PIECE'")
    val purchaseUnit: MeasurementUnit = priceUnit
)
