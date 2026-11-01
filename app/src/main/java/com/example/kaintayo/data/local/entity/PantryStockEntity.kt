package com.example.kaintayo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.kaintayo.domain.model.MeasurementUnit

@Entity(
    tableName = "pantry_stock",
    foreignKeys = [
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["ingredientId"])
    ]
)
data class PantryStockEntity(
    @PrimaryKey
    val ingredientId: String,
    val quantity: Double,
    val unit: MeasurementUnit
)