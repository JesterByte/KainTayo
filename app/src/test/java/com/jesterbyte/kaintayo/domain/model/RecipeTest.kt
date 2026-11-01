package com.jesterbyte.kaintayo.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeTest {

    @Test
    fun recipeStoresCorrectMealType() {
        val recipe = Recipe(
            id = "tinolang-manok",
            name = "Tinolang Manok",
            description = "Filipino chicken ginger soup",
            mealType = MealType.LUNCH,
            servings = 4,
            ingredients = emptyList(),
            instructions = emptyList(),
            preparationMinutes = 40
        )

        assertEquals(MealType.LUNCH, recipe.mealType)
    }

    @Test
    fun recipeStoresIngredientQuantities() {
        val ingredient = RecipeIngredient(
            ingredientId = "chicken",
            quantity = 0.5,
            unit = MeasurementUnit.KILOGRAM
        )

        assertEquals("chicken", ingredient.ingredientId)
        assertEquals(0.5, ingredient.quantity, 0.001)
        assertEquals(
            MeasurementUnit.KILOGRAM,
            ingredient.unit
        )
    }

    @Test
    fun recipeCanContainMultipleIngredients() {
        val recipe = Recipe(
            id = "ginisang-pechay",
            name = "Ginisang Pechay",
            description = "Sautéed pechay",
            mealType = MealType.DINNER,
            servings = 4,
            ingredients = listOf(
                RecipeIngredient(
                    "pechay", 2.0, MeasurementUnit.PIECE
                ),
                RecipeIngredient(
                    "garlic", 3.0, MeasurementUnit.PIECE
                )
            ),
            instructions = listOf("Prepare ingredients", "Cook"),
            preparationMinutes = 20
        )

        assertEquals(2, recipe.ingredients.size)
        assertTrue(recipe.servings > 0)
    }
}
