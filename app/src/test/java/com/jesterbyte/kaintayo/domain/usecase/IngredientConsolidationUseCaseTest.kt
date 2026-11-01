
package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.MeasurementUnit
import com.jesterbyte.kaintayo.domain.model.RecipeIngredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IngredientConsolidationUseCaseTest {

    @Test
    fun combinesRepeatedIngredients() {
        val ingredients = listOf(
            RecipeIngredient("chicken", 500.0, MeasurementUnit.GRAM),
            RecipeIngredient("sayote", 2.0, MeasurementUnit.PIECE),
            RecipeIngredient("chicken", 300.0, MeasurementUnit.GRAM)
        )

        val result = IngredientConsolidationUseCase
            .consolidate(ingredients)!!

        assertEquals(2, result.size)
        assertEquals("chicken", result[0].ingredientId)
        assertEquals(800.0, result[0].quantity, 0.0001)
        assertEquals(MeasurementUnit.GRAM, result[0].unit)
    }

    @Test
    fun combinesKilogramsAndGrams() {
        val ingredients = listOf(
            RecipeIngredient("chicken", 500.0, MeasurementUnit.GRAM),
            RecipeIngredient("chicken", 0.5, MeasurementUnit.KILOGRAM)
        )

        val result = IngredientConsolidationUseCase
            .consolidate(ingredients)!!

        assertEquals(1000.0, result[0].quantity, 0.0001)
        assertEquals(MeasurementUnit.GRAM, result[0].unit)
    }

    @Test
    fun combinesLitersAndMilliliters() {
        val ingredients = listOf(
            RecipeIngredient("water", 250.0, MeasurementUnit.MILLILITER),
            RecipeIngredient("water", 0.5, MeasurementUnit.LITER)
        )

        val result = IngredientConsolidationUseCase
            .consolidate(ingredients)!!

        assertEquals(750.0, result[0].quantity, 0.0001)
        assertEquals(MeasurementUnit.MILLILITER, result[0].unit)
    }

    @Test
    fun combinesDifferentVolumeUnits() {
        val ingredients = listOf(
            RecipeIngredient("water", 1.0, MeasurementUnit.CUP),
            RecipeIngredient("water", 1.0, MeasurementUnit.TABLESPOON)
        )

        val result = IngredientConsolidationUseCase
            .consolidate(ingredients)!!

        assertEquals(255.0, result[0].quantity, 0.0001)
        assertEquals(MeasurementUnit.MILLILITER, result[0].unit)
    }

    @Test
    fun preservesSeparateIngredientIds() {
        val ingredients = listOf(
            RecipeIngredient("chicken", 500.0, MeasurementUnit.GRAM),
            RecipeIngredient("pork", 500.0, MeasurementUnit.GRAM)
        )

        val result = IngredientConsolidationUseCase
            .consolidate(ingredients)!!

        assertEquals(2, result.size)
        assertEquals("chicken", result[0].ingredientId)
        assertEquals("pork", result[1].ingredientId)
    }

    @Test
    fun returnsEmptyListForNoIngredients() {
        val result = IngredientConsolidationUseCase.consolidate(emptyList())

        assertEquals(emptyList<Any>(), result)
    }

    @Test
    fun rejectsIncompatibleUnitsForSameIngredient() {
        val ingredients = listOf(
            RecipeIngredient("ingredient", 2.0, MeasurementUnit.PIECE),
            RecipeIngredient("ingredient", 1.0, MeasurementUnit.PACK)
        )

        assertNull(
            IngredientConsolidationUseCase.consolidate(ingredients)
        )
    }

    @Test
    fun rejectsNegativeQuantities() {
        val ingredients = listOf(
            RecipeIngredient("chicken", -100.0, MeasurementUnit.GRAM)
        )

        assertNull(
            IngredientConsolidationUseCase.consolidate(ingredients)
        )
    }

    @Test
    fun rejectsInvalidQuantities() {
        val ingredients = listOf(
            RecipeIngredient("chicken", Double.NaN, MeasurementUnit.GRAM)
        )

        assertNull(
            IngredientConsolidationUseCase.consolidate(ingredients)
        )
    }
}
