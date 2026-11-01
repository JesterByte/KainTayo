
package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.Ingredient
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit

object IngredientCostCalculator {

    /**
     * Calculates an ingredient's cost in centavos.
     *
     * Returns a Double to preserve fractional centavos.
     * Rounding should happen only when displaying the final total.
     */
    fun calculateCostInCentavos(
        ingredient: Ingredient,
        quantity: Double,
        unit: MeasurementUnit
    ): Double? {

        if (!quantity.isFinite() || quantity < 0.0) {
            return null
        }

        if (ingredient.estimatedPriceCentavos < 0L) {
            return null
        }

        if (!ingredient.priceQuantity.isFinite() ||
            ingredient.priceQuantity <= 0.0
        ) {
            return null
        }

        val convertedQuantity = MeasurementUnitConverter.convert(
            quantity,
            unit,
            ingredient.priceUnit
        ) ?: return null

        val cost = ingredient.estimatedPriceCentavos.toDouble() *
                convertedQuantity / ingredient.priceQuantity

        return cost.takeIf { it.isFinite() }
    }
}
