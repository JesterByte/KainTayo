
package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.MealPlanGroceryEstimate
import com.jesterbyte.kaintayo.domain.repository.IngredientRepository
import com.jesterbyte.kaintayo.domain.repository.MealPlanRepository
import com.jesterbyte.kaintayo.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class GetPaydayGroceryListUseCase(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository
) {

    suspend fun estimate(
        startDate: LocalDate,
        endDate: LocalDate,
        targetServings: Int,
        budgetCentavos: Long
    ): MealPlanGroceryEstimate? {

        if (endDate.isBefore(startDate) ||
            targetServings <= 0 ||
            budgetCentavos < 0L
        ) {
            return null
        }

        val mealPlans = mealPlanRepository
            .getMealsForPeriod(startDate, endDate)
            .first()

        if (mealPlans.isEmpty()) {
            return null
        }

        val recipesById = recipeRepository
            .getAllRecipes()
            .first()
            .associateBy { it.id }

        val selectedRecipes = mealPlans.map { mealPlan ->
            recipesById[mealPlan.recipeId] ?: return null
        }

        val ingredients = ingredientRepository
            .getAllIngredients()
            .first()

        return MealPlanGroceryEstimateUseCase.estimate(
            recipes = selectedRecipes,
            targetServings = targetServings,
            ingredientCatalog = ingredients,
            budgetCentavos = budgetCentavos
        )
    }
}
