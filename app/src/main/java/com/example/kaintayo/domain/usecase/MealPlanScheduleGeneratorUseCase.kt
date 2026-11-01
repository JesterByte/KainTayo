package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.MealPlanSlot
import com.example.kaintayo.domain.model.MealType
import java.time.LocalDate

object MealPlanScheduleGeneratorUseCase {

    private val mealTypes = listOf(
        MealType.BREAKFAST,
        MealType.LUNCH,
        MealType.DINNER
    )

    fun generate(
        startDate: LocalDate,
        endDate: LocalDate
    ): List<MealPlanSlot>? {

        if (endDate.isBefore(startDate)) {
            return null
        }

        val slots = mutableListOf<MealPlanSlot>()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            mealTypes.forEach { mealType ->
                slots.add(
                    MealPlanSlot(
                        date = currentDate,
                        mealType = mealType
                    )
                )
            }

            currentDate = currentDate.plusDays(1)
        }

        return slots
    }
}
