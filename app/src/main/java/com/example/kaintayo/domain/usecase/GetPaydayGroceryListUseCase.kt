package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.MealPlanGroceryEstimate
import com.example.kaintayo.domain.repository.IngredientRepository
import com.example.kaintayo.domain.repository.MealPlanRepository
import com.example.kaintayo.domain.repository.PantryStockRepository
import com.example.kaintayo.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class GetPaydayGroceryListUseCase(
    private val mealPlanRepository: MealPlanRepository,
    private val recipeRepository: RecipeRepository,
    private val ingredientRepository: IngredientRepository,
    private val pantryStockRepository: PantryStockRepository
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

        val pantryStock = pantryStockRepository
            .getAllPantryStock()
            .first()

        return MealPlanGroceryEstimateUseCase.estimate(
            recipes = selectedRecipes,
            targetServings = targetServings,
            ingredientCatalog = ingredients,
            budgetCentavos = budgetCentavos,
            pantryStock = pantryStock
        )
    }
}