package com.jesterbyte.kaintayo.domain.model

data class RecipeIngredient(
    val ingredientId: String,
    val quantity: Double,
    val unit: MeasurementUnit
)

data class Recipe(
    val id: String,
    val name: String,
    val description: String,
    val mealType: MealType,
    val servings: Int,
    val ingredients: List<RecipeIngredient>,
    val instructions: List<String>,
    val preparationMinutes: Int
)