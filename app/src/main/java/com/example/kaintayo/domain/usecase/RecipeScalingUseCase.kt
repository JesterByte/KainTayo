
package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.Recipe

object RecipeScalingUseCase {

    fun scaleRecipe(
        recipe: Recipe,
        targetServings: Int
    ): Recipe? {

        if (recipe.servings <= 0 || targetServings <= 0) {
            return null
        }

        if (recipe.ingredients.any {
                !it.quantity.isFinite() || it.quantity < 0.0
            }
        ) {
            return null
        }

        val scaleFactor = targetServings.toDouble() /
                recipe.servings.toDouble()

        val scaledIngredients = recipe.ingredients.map { ingredient ->
            ingredient.copy(
                quantity = ingredient.quantity * scaleFactor
            )
        }

        return recipe.copy(
            servings = targetServings,
            ingredients = scaledIngredients
        )
    }
}
