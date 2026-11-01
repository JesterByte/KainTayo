package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MealType
import com.example.kaintayo.domain.model.MeasurementUnit
import com.example.kaintayo.domain.model.Recipe
import com.example.kaintayo.domain.model.RecipeIngredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecipeGroceryCostUseCaseTest {

    private val chicken = Ingredient(
        id = "chicken",
        name = "Chicken",
        defaultUnit = MeasurementUnit.GRAM,
        estimatedPriceCentavos = 15000L,
        priceQuantity = 1.0,
        priceUnit = MeasurementUnit.KILOGRAM
    )

    private val sayote = Ingredient(
        id = "sayote",
        name = "Sayote",
        defaultUnit = MeasurementUnit.PIECE,
        estimatedPriceCentavos = 1500L,
        priceQuantity = 1.0,
        priceUnit = MeasurementUnit.PIECE
    )

    private val catalog = listOf(chicken, sayote)

    private val recipe = Recipe(
        id = "tinola",
        name = "Tinolang Manok",
        description = "Chicken soup with sayote",
        mealType = MealType.LUNCH,
        servings = 2,
        ingredients = listOf(
            RecipeIngredient(
                "chicken",
                500.0,
                MeasurementUnit.GRAM
            ),
            RecipeIngredient(
                "sayote",
                1.0,
                MeasurementUnit.PIECE
            )
        ),
        instructions = listOf("Cook chicken.", "Add sayote."),
        preparationMinutes = 40
    )

    @Test
    fun calculatesCostForOriginalServings() {
        val result = RecipeGroceryCostUseCase.calculate(
            recipe = recipe,
            targetServings = 2,
            ingredientCatalog = catalog
        )!!

        assertEquals(
            9000.0,
            result.totalCostCentavos,
            0.0001
        )
    }

    @Test
    fun scalesCostForMoreServings() {
        val result = RecipeGroceryCostUseCase.calculate(
            recipe = recipe,
            targetServings = 4,
            ingredientCatalog = catalog
        )!!

        assertEquals(
            18000.0,
            result.totalCostCentavos,
            0.0001
        )
    }

    @Test
    fun scalesCostForFewerServings() {
        val result = RecipeGroceryCostUseCase.calculate(
            recipe = recipe,
            targetServings = 1,
            ingredientCatalog = catalog
        )!!

        assertEquals(
            4500.0,
            result.totalCostCentavos,
            0.0001
        )
    }

    @Test
    fun returnsIngredientCostDetails() {
        val result = RecipeGroceryCostUseCase.calculate(
            recipe = recipe,
            targetServings = 2,
            ingredientCatalog = catalog
        )!!

        assertEquals(2, result.items.size)
        assertEquals("Chicken", result.items[0].name)
        assertEquals("Sayote", result.items[1].name)
    }

    @Test
    fun rejectsMissingIngredientFromCatalog() {
        assertNull(
            RecipeGroceryCostUseCase.calculate(
                recipe = recipe,
                targetServings = 2,
                ingredientCatalog = listOf(chicken)
            )
        )
    }

    @Test
    fun rejectsInvalidTargetServings() {
        assertNull(
            RecipeGroceryCostUseCase.calculate(
                recipe = recipe,
                targetServings = 0,
                ingredientCatalog = catalog
            )
        )
    }
}
