
package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MeasurementUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IngredientCostCalculatorTest {

    private val chicken = Ingredient(
        id = "chicken",
        name = "Chicken",
        defaultUnit = MeasurementUnit.GRAM,
        estimatedPriceCentavos = 15000L,
        priceQuantity = 1.0,
        priceUnit = MeasurementUnit.KILOGRAM
    )

    @Test
    fun calculatesCostForOneKilogram() {
        val result = IngredientCostCalculator.calculateCostInCentavos(
            chicken,
            1.0,
            MeasurementUnit.KILOGRAM
        )

        assertEquals(15000.0, result!!, 0.0001)
    }

    @Test
    fun calculatesCostForFiveHundredGrams() {
        val result = IngredientCostCalculator.calculateCostInCentavos(
            chicken,
            500.0,
            MeasurementUnit.GRAM
        )

        assertEquals(7500.0, result!!, 0.0001)
    }

    @Test
    fun calculatesCostForTwoHundredFiftyGrams() {
        val result = IngredientCostCalculator.calculateCostInCentavos(
            chicken,
            250.0,
            MeasurementUnit.GRAM
        )

        assertEquals(3750.0, result!!, 0.0001)
    }

    @Test
    fun preservesFractionalCentavos() {
        val result = IngredientCostCalculator.calculateCostInCentavos(
            chicken,
            1.0,
            MeasurementUnit.GRAM
        )

        assertEquals(15.0, result!!, 0.0001)
    }

    @Test
    fun rejectsIncompatibleUnits() {
        val result = IngredientCostCalculator.calculateCostInCentavos(
            chicken,
            1.0,
            MeasurementUnit.CUP
        )

        assertNull(result)
    }

    @Test
    fun rejectsNegativeQuantity() {
        val result = IngredientCostCalculator.calculateCostInCentavos(
            chicken,
            -100.0,
            MeasurementUnit.GRAM
        )

        assertNull(result)
    }

    @Test
    fun rejectsZeroPriceQuantity() {
        val invalidIngredient = chicken.copy(
            priceQuantity = 0.0
        )

        val result = IngredientCostCalculator.calculateCostInCentavos(
            invalidIngredient,
            100.0,
            MeasurementUnit.GRAM
        )

        assertNull(result)
    }
}
