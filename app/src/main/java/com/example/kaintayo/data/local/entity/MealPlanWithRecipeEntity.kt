package com.example.kaintayo.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MealPlanWithRecipeEntity(
    @Embedded
    val mealPlan: MealPlanEntity,

    @Relation(
        parentColumn = "recipeId",
        entityColumn = "id"
    )
    val recipe: RecipeEntity
)