package com.jesterbyte.kaintayo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.jesterbyte.kaintayo.domain.model.MealType

@Entity(
    tableName = "meal_plans",
    primaryKeys = ["date", "mealType"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("recipeId")
    ]
)
data class MealPlanEntity(
    // ISO date format: yyyy-MM-dd
    val date: String,

    val mealType: MealType,

    val recipeId: String
)