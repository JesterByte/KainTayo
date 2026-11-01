package com.jesterbyte.kaintayo.domain.model

import java.time.LocalDate

data class MealPlan(
    val date: LocalDate,
    val mealType: MealType,
    val recipeId: String
)