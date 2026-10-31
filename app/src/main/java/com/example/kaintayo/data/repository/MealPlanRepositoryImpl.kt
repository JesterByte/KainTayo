package com.example.kaintayo.data.repository

import com.example.kaintayo.data.local.dao.MealPlanDao
import com.example.kaintayo.data.local.entity.MealPlanEntity
import com.example.kaintayo.domain.model.MealPlan
import com.example.kaintayo.domain.model.MealType
import com.example.kaintayo.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class MealPlanRepositoryImpl(
    private val mealPlanDao: MealPlanDao
) : MealPlanRepository {

    override fun getMealsForDate(
        date: LocalDate
    ): Flow<List<MealPlan>> {
        return mealPlanDao.getMealsForDate(date.toString())
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun getMealsForPeriod(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<MealPlan>> {
        require(!endDate.isBefore(startDate)) {
            "End date cannot be before start date."
        }

        return mealPlanDao.getMealsForPeriod(
            startDate = startDate.toString(),
            endDate = endDate.toString()
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveMealPlan(mealPlan: MealPlan) {
        mealPlanDao.insertMealPlan(mealPlan.toEntity())
    }

    override suspend fun saveMealPlans(mealPlans: List<MealPlan>) {
        mealPlanDao.insertMealPlans(
            mealPlans.map { it.toEntity() }
        )
    }

    override suspend fun deleteMeal(
        date: LocalDate,
        mealType: MealType
    ) {
        mealPlanDao.deleteMeal(
            date = date.toString(),
            mealType = mealType.name
        )
    }

    private fun MealPlanEntity.toDomain() = MealPlan(
        date = LocalDate.parse(date),
        mealType = mealType,
        recipeId = recipeId
    )

    private fun MealPlan.toEntity() = MealPlanEntity(
        date = date.toString(),
        mealType = mealType,
        recipeId = recipeId
    )
}