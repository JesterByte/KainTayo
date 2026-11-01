package com.example.kaintayo.domain.model

import java.time.LocalDate

data class MealPlanSlot(
    val date: LocalDate,
    val mealType: MealType
)
