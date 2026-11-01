package com.jesterbyte.kaintayo.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class RecipeWithIngredients(

    @Embedded
    val recipe: RecipeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId"
    )
    val ingredients: List<RecipeIngredientEntity>
)