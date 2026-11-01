package com.jesterbyte.kaintayo.domain.model

import java.time.LocalDate

data class PlannedMeal(
    val date: LocalDate,
    val mealType: MealType,
    val recipeId: String,
    val recipeName: String,
    val description: String,
    val preparationMinutes: Int
)