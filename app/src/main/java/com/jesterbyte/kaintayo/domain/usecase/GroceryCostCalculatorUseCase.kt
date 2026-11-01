
package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.ConsolidatedIngredient
import com.jesterbyte.kaintayo.domain.model.GroceryCostEstimate
import com.jesterbyte.kaintayo.domain.model.GroceryCostItem
import com.jesterbyte.kaintayo.domain.model.Ingredient

object GroceryCostCalculatorUseCase {

    fun calculate(
        consolidatedIngredients: List<ConsolidatedIngredient>,
        ingredientCatalog: List<Ingredient>
    ): GroceryCostEstimate? {

        val ingredientsById = ingredientCatalog.associateBy { it.id }

        // Reject duplicate catalog IDs to avoid ambiguous pricing.
        if (ingredientsById.size != ingredientCatalog.size) {
            return null
        }

        val costItems = mutableListOf<GroceryCostItem>()

        for (item in consolidatedIngredients) {
            val ingredient = ingredientsById[item.ingredientId]
                ?: return null

            val cost = IngredientCostCalculator.calculateCostInCentavos(
                ingredient = ingredient,
                quantity = item.quantity,
                unit = item.unit
            ) ?: return null

            costItems.add(
                GroceryCostItem(
                    ingredientId = ingredient.id,
                    name = ingredient.name,
                    quantity = item.quantity,
                    unit = item.unit,
                    estimatedCostCentavos = cost
                )
            )
        }

        val totalCost = costItems.sumOf {
            it.estimatedCostCentavos
        }

        if (!totalCost.isFinite()) {
            return null
        }

        return GroceryCostEstimate(
            items = costItems,
            totalCostCentavos = totalCost
        )
    }
}
