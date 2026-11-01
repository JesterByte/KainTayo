package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.GroceryCostEstimate
import com.jesterbyte.kaintayo.domain.model.Ingredient
import com.jesterbyte.kaintayo.domain.model.Recipe

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
