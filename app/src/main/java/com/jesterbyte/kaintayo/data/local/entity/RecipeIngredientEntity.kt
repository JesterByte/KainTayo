package com.jesterbyte.kaintayo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit

@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "ingredientId"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("ingredientId")
    ]
)
data class RecipeIngredientEntity(
    val recipeId: String,

    val ingredientId: String,

    val quantity: Double,

    val unit: MeasurementUnit
)