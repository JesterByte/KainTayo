
package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MealPlanGroceryEstimate
import com.example.kaintayo.domain.model.Recipe

object MealPlanGroceryEstimateUseCase {

    fun estimate(
        recipes: List<Recipe>,
        targetServings: Int,
        ingredientCatalog: List<Ingredient>,
        budgetCentavos: Long
    ): MealPlanGroceryEstimate? {

        if (recipes.isEmpty() || targetServings <= 0) {
            return null
        }

        // Scale every recipe to the household size.
        val allIngredients = mutableListOf<
                com.example.kaintayo.domain.model.RecipeIngredient
                >()

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

        // Calculate grocery costs using the local ingredient catalog.
        val groceryCost = GroceryCostCalculatorUseCase.calculate(
            consolidatedIngredients,
            ingredientCatalog
        ) ?: return null

        // Compare the estimated grocery cost with the payday budget.
        val budgetAssessment = BudgetAssessmentUseCase.assess(
            budgetCentavos = budgetCentavos,
            estimatedCostCentavos = groceryCost.totalCostCentavos
        ) ?: return null

        return MealPlanGroceryEstimate(
            groceryCost = groceryCost,
            budgetAssessment = budgetAssessment
        )
    }
}
