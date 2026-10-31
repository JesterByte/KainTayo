package com.example.kaintayo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kaintayo.domain.model.MealType

@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String,

    val name: String,

    val description: String,

    val mealType: MealType,

    val servings: Int,

    // Cooking instructions stored as newline-separated text.
    val instructions: String,

    val preparationMinutes: Int
)