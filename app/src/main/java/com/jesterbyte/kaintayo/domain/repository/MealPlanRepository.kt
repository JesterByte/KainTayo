package com.jesterbyte.kaintayo.domain.repository

import com.jesterbyte.kaintayo.domain.model.MealPlan
import com.jesterbyte.kaintayo.domain.model.PlannedMeal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MealPlanRepository {

    fun getMealsForDate(date: LocalDate): Flow<List<MealPlan>>

    fun getMealsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<MealPlan>>

    suspend fun saveMealPlan(mealPlan: MealPlan)

    suspend fun saveMealPlans(mealPlans: List<MealPlan>)

    suspend fun deleteMeal(
        date: LocalDate,
        mealType: com.jesterbyte.kaintayo.domain.model.MealType
    )

    suspend fun replaceMealPlansForPeriod(
        startDate: LocalDate,
        endDate: LocalDate,
        mealPlans: List<MealPlan>
    )

    fun getPlannedMealsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<PlannedMeal>>
}