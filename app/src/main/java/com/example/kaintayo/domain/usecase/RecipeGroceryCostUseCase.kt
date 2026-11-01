package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.GroceryCostEstimate
import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.Recipe

object RecipeGroceryCostUseCase {

    fun calculate(
        recipe: Recipe,
        targetServings: Int,
        ingredientCatalog: List<Ingredient>
    ): GroceryCostEstimate? {

        val scaledRecipe = RecipeScalingUseCase.scaleRecipe(
            recipe = recipe,
            targetServings = targetServings
        ) ?: return null

        val consolidatedIngredients =
            IngredientConsolidationUseCase.consolidate(
                scaledRecipe.ingredients
            ) ?: return null

        return GroceryCostCalculatorUseCase.calculate(
            consolidatedIngredients = consolidatedIngredients,
            ingredientCatalog = ingredientCatalog
        )
    }
}
