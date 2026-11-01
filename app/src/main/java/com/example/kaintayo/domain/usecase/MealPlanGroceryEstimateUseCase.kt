
package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MealPlanGroceryEstimate
import com.example.kaintayo.domain.model.Recipe
import com.example.kaintayo.domain.model.RecipeIngredient

object MealPlanGroceryEstimateUseCase {

    fun estimate(
        recipes: List<Recipe>,
        targetServings: Int,
        ingredientCatalog: List<Ingredient>,
        budgetCentavos: Long
    ): MealPlanGroceryEstimate? {

        if (recipes.isEmpty() || targetServings <= 0 || budgetCentavos < 0L) {
            return null
        }

        // Scale every recipe to the household size.
        val allIngredients = mutableListOf<RecipeIngredient>()

        for (recipe in recipes) {
            val scaledRecipe = RecipeScalingUseCase.scaleRecipe(
                recipe,
                targetServings
            ) ?: return null

            allIngredients.addAll(scaledRecipe.ingredients)
        }

        // Combine repeated ingredients across all selected recipes.
        val consolidatedIngredients =
            IngredientConsolidationUseCase.consolidate(
                allIngredients
            ) ?: return null

        // Calculate the proportional cost of all recipe consumption.
        val groceryCost = GroceryCostCalculatorUseCase.calculate(
            consolidatedIngredients,
            ingredientCatalog
        ) ?: return null

        // Calculate whole-unit purchases for all required ingredients.
        val purchaseEstimate = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = consolidatedIngredients,
            ingredientCatalog = ingredientCatalog
        ) ?: return null

        // Compare the complete purchase cost with the budget.
        val budgetAssessment = BudgetAssessmentUseCase.assess(
            budgetCentavos = budgetCentavos,
            estimatedCostCentavos = purchaseEstimate.totalCostCentavos
        ) ?: return null

        return MealPlanGroceryEstimate(
            groceryCost = groceryCost,
            purchaseEstimate = purchaseEstimate,
            budgetAssessment = budgetAssessment
        )
    }
}
