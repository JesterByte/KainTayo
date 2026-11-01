package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.ConsolidatedIngredient
import com.jesterbyte.kaintayo.domain.model.Ingredient
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GroceryCostCalculatorUseCaseTest {

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

    @Test
    fun calculatesCostForMultipleIngredients() {
        val items = listOf(
            ConsolidatedIngredient(
                "chicken",
                500.0,
                MeasurementUnit.GRAM
            ),
            ConsolidatedIngredient(
                "sayote",
                2.0,
                MeasurementUnit.PIECE
            )
        )

        val result = GroceryCostCalculatorUseCase.calculate(
            items,
            catalog
        )!!

        assertEquals(7500.0, result.items[0].estimatedCostCentavos, 0.0001)
        assertEquals(3000.0, result.items[1].estimatedCostCentavos, 0.0001)
        assertEquals(10500.0, result.totalCostCentavos, 0.0001)
    }


    @Test
    fun preservesFractionalCentavosInTotal() {
        val fractionalIngredient = Ingredient(
            id = "test-ingredient",
            name = "Test Ingredient",
            defaultUnit = MeasurementUnit.GRAM,
            estimatedPriceCentavos = 10001L,
            priceQuantity = 3.0,
            priceUnit = MeasurementUnit.KILOGRAM
        )

        val items = listOf(
            ConsolidatedIngredient(
                "test-ingredient",
                1.0,
                MeasurementUnit.GRAM
            )
        )

        val result = GroceryCostCalculatorUseCase.calculate(
            items,
            listOf(fractionalIngredient)
        )!!

        assertEquals(
            10001.0 / 3000.0,
            result.totalCostCentavos,
            0.0000001
        )
    }

    @Test
    fun returnsZeroForEmptyGroceryList() {
        val result = GroceryCostCalculatorUseCase.calculate(
            emptyList(),
            catalog
        )!!

        assertEquals(0.0, result.totalCostCentavos, 0.0001)
        assertEquals(0, result.items.size)
    }

    @Test
    fun rejectsMissingCatalogIngredient() {
        val items = listOf(
            ConsolidatedIngredient(
                "unknown",
                100.0,
                MeasurementUnit.GRAM
            )
        )

        assertNull(
            GroceryCostCalculatorUseCase.calculate(items, catalog)
        )
    }

    @Test
    fun rejectsDuplicateCatalogIds() {
        val items = listOf(
            ConsolidatedIngredient(
                "chicken",
                100.0,
                MeasurementUnit.GRAM
            )
        )

        assertNull(
            GroceryCostCalculatorUseCase.calculate(
                items,
                catalog + chicken
            )
        )
    }

    @Test
    fun includesIngredientNamesInCostItems() {
        val items = listOf(
            ConsolidatedIngredient(
                "chicken",
                500.0,
                MeasurementUnit.GRAM
            )
        )

        val result = GroceryCostCalculatorUseCase.calculate(
            items,
            catalog
        )

        assertNotNull(result)
        assertEquals("Chicken", result!!.items[0].name)
    }
}
