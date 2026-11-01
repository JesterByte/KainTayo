
package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.ConsolidatedIngredient
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit
import com.jesterbyte.kaintayo.domain.model.RecipeIngredient

object IngredientConsolidationUseCase {

    fun consolidate(
        recipeIngredients: List<RecipeIngredient>
    ): List<ConsolidatedIngredient>? {

        val totals = linkedMapOf<String, ConsolidatedIngredient>()

        for (ingredient in recipeIngredients) {
            if (!ingredient.quantity.isFinite() ||
                ingredient.quantity < 0.0
            ) {
                return null
            }

            val existing = totals[ingredient.ingredientId]

            if (existing == null) {
                val canonicalUnit = getCanonicalUnit(ingredient.unit)

                val quantity = MeasurementUnitConverter.convert(
                    ingredient.quantity,
                    ingredient.unit,
                    canonicalUnit
                ) ?: return null

                totals[ingredient.ingredientId] = ConsolidatedIngredient(
                    ingredientId = ingredient.ingredientId,
                    quantity = quantity,
                    unit = canonicalUnit
                )
            } else {
                val convertedQuantity = MeasurementUnitConverter.convert(
                    ingredient.quantity,
                    ingredient.unit,
                    existing.unit
                ) ?: return null

                totals[ingredient.ingredientId] = existing.copy(
                    quantity = existing.quantity + convertedQuantity
                )
            }
        }

        return totals.values.toList()
    }

    private fun getCanonicalUnit(
        unit: MeasurementUnit
    ): MeasurementUnit {
        return when (unit) {
            MeasurementUnit.GRAM,
            MeasurementUnit.KILOGRAM -> MeasurementUnit.GRAM

            MeasurementUnit.MILLILITER,
            MeasurementUnit.LITER,
            MeasurementUnit.CUP,
            MeasurementUnit.TABLESPOON,
            MeasurementUnit.TEASPOON -> MeasurementUnit.MILLILITER

            MeasurementUnit.PIECE -> MeasurementUnit.PIECE

            MeasurementUnit.PACK -> MeasurementUnit.PACK
        }
    }
}
