package com.example.kaintayo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kaintayo.data.local.entity.MealPlanEntity
import com.example.kaintayo.data.local.entity.MealPlanWithRecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {

    @Query(
        """
        SELECT * FROM meal_plans
        WHERE date = :date
        ORDER BY
        CASE mealType
            WHEN 'BREAKFAST' THEN 0
            WHEN 'LUNCH' THEN 1
            WHEN 'DINNER' THEN 2
            ELSE 3
        END
        """
    )
    fun getMealsForDate(date: String): Flow<List<MealPlanEntity>>

    @Query(
        """
        SELECT * FROM meal_plans
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date ASC,
        CASE mealType
            WHEN 'BREAKFAST' THEN 0
            WHEN 'LUNCH' THEN 1
            WHEN 'DINNER' THEN 2
            ELSE 3
        END
        """
    )
    fun getMealsForPeriod(
        startDate: String,
        endDate: String
    ): Flow<List<MealPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlans(mealPlans: List<MealPlanEntity>)

    @Query(
        """
        DELETE FROM meal_plans
        WHERE date = :date AND mealType = :mealType
        """
    )
    suspend fun deleteMeal(
        date: String,
        mealType: String
    )

    @Query(
        """
    DELETE FROM meal_plans
    WHERE date BETWEEN :startDate AND :endDate
    """
    )
    suspend fun deleteMealsForPeriod(
        startDate: String,
        endDate: String
    )

    @Transaction
    suspend fun replaceMealPlansForPeriod(
        startDate: String,
        endDate: String,
        mealPlans: List<MealPlanEntity>
    ) {
        deleteMealsForPeriod(startDate, endDate)

        if (mealPlans.isNotEmpty()) {
            insertMealPlans(mealPlans)
        }
    }

    @Transaction
    @Query(
        """
    SELECT * FROM meal_plans
    WHERE date BETWEEN :startDate AND :endDate
    ORDER BY date ASC,
    CASE mealType
        WHEN 'BREAKFAST' THEN 0
        WHEN 'LUNCH' THEN 1
        WHEN 'DINNER' THEN 2
        ELSE 3
    END
    """
    )
    fun getPlannedMealsForPeriod(
        startDate: String,
        endDate: String
    ): Flow<List<MealPlanWithRecipeEntity>>
}