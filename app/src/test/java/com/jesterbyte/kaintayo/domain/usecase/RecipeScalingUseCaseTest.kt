
package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.MealType
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit
import com.jesterbyte.kaintayo.domain.model.Recipe
import com.jesterbyte.kaintayo.domain.model.RecipeIngredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeScalingUseCaseTest {

    private val recipe = Recipe(
        id = "tinolang-manok",
        name = "Tinolang Manok",
        description = "Chicken soup with vegetables",
        mealType = MealType.LUNCH,
        servings = 4,
        ingredients = listOf(
            RecipeIngredient(
                ingredientId = "chicken",
                quantity = 500.0,
                unit = MeasurementUnit.GRAM
            ),
            RecipeIngredient(
                ingredientId = "sayote",
                quantity = 2.0,
                unit = MeasurementUnit.PIECE
            ),
            RecipeIngredient(
                ingredientId = "ginger",
                quantity = 20.0,
                unit = MeasurementUnit.GRAM
            )
        ),
        instructions = listOf("Cook the chicken.", "Add vegetables."),
        preparationMinutes = 40
    )

    @Test
    fun doublesIngredientsForEightServings() {
        val result = RecipeScalingUseCase.scaleRecipe(recipe, 8)!!

        assertEquals(8, result.servings)
        assertEquals(1000.0, result.ingredients[0].quantity, 0.0001)
        assertEquals(4.0, result.ingredients[1].quantity, 0.0001)
        assertEquals(40.0, result.ingredients[2].quantity, 0.0001)
    }

    @Test
    fun scalesIngredientsForTwoServings() {
        val result = RecipeScalingUseCase.scaleRecipe(recipe, 2)!!

        assertEquals(2, result.servings)
        assertEquals(250.0, result.ingredients[0].quantity, 0.0001)
        assertEquals(1.0, result.ingredients[1].quantity, 0.0001)
        assertEquals(10.0, result.ingredients[2].quantity, 0.0001)
    }

    @Test
    fun preservesIngredientUnitsAndRecipeDetails() {
        val result = RecipeScalingUseCase.scaleRecipe(recipe, 6)!!

        assertEquals(MeasurementUnit.GRAM, result.ingredients[0].unit)
        assertEquals(MeasurementUnit.PIECE, result.ingredients[1].unit)
        assertEquals(recipe.name, result.name)
        assertEquals(recipe.instructions, result.instructions)
    }

    @Test
    fun doesNotModifyOriginalRecipe() {
        RecipeScalingUseCase.scaleRecipe(recipe, 8)

        assertEquals(4, recipe.servings)
        assertEquals(500.0, recipe.ingredients[0].quantity, 0.0001)
    }

    @Test
    fun rejectsZeroTargetServings() {
        assertNull(RecipeScalingUseCase.scaleRecipe(recipe, 0))
    }

    @Test
    fun rejectsNegativeTargetServings() {
        assertNull(RecipeScalingUseCase.scaleRecipe(recipe, -2))
    }

    @Test
    fun rejectsRecipeWithZeroOriginalServings() {
        val invalidRecipe = recipe.copy(servings = 0)

        assertNull(RecipeScalingUseCase.scaleRecipe(invalidRecipe, 4))
    }

    @Test
    fun rejectsInvalidIngredientQuantities() {
        val invalidRecipe = recipe.copy(
            ingredients = listOf(
                recipe.ingredients[0].copy(quantity = Double.NaN)
            )
        )

        assertNull(RecipeScalingUseCase.scaleRecipe(invalidRecipe, 4))
    }
}
