
package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.ConsolidatedIngredient
import com.example.kaintayo.domain.model.GroceryPurchaseEstimate
import com.example.kaintayo.domain.model.GroceryPurchaseItem
import com.example.kaintayo.domain.model.Ingredient
import kotlin.math.ceil

object GroceryPurchaseCalculatorUseCase {

    fun calculate(
        consolidatedIngredients: List<ConsolidatedIngredient>,
        ingredientCatalog: List<Ingredient>
    ): GroceryPurchaseEstimate? {

        val ingredientsById = ingredientCatalog.associateBy { it.id }

        // Reject duplicate catalog IDs.
        if (ingredientsById.size != ingredientCatalog.size) {
            return null
        }

        // Inputs should already be consolidated by ingredient.
        val requestedIds = consolidatedIngredients.map { it.ingredientId }

        if (requestedIds.toSet().size != requestedIds.size) {
            return null
        }

        val purchaseItems = mutableListOf<GroceryPurchaseItem>()

        for ((ingredientId, quantityToPurchase, unit) in consolidatedIngredients) {
            val ingredient = ingredientsById[ingredientId]
                ?: return null

            if (!quantityToPurchase.isFinite() || quantityToPurchase < 0.0) {
                return null
            }

            if (!ingredient.purchaseQuantity.isFinite() ||
                ingredient.purchaseQuantity <= 0.0
            ) {
                return null
            }

            // Include the full recipe requirement.

            // Convert the full requirement into the purchase unit.
            val requiredPurchaseUnits = MeasurementUnitConverter.convert(
                quantity = quantityToPurchase,
                from = unit,
                to = ingredient.purchaseUnit
            ) ?: return null

            val purchaseCountDouble = ceil(
                requiredPurchaseUnits / ingredient.purchaseQuantity
            )

            if (!purchaseCountDouble.isFinite() ||
                purchaseCountDouble > Long.MAX_VALUE.toDouble()
            ) {
                return null
            }

            val purchaseCount = purchaseCountDouble.toLong()

            val purchasedQuantity =
                purchaseCount.toDouble() * ingredient.purchaseQuantity

            if (!purchasedQuantity.isFinite()) {
                return null
            }

            // Calculate the cost of one complete purchase unit.
            val costPerPurchaseUnit =
                IngredientCostCalculator.calculateCostInCentavos(
                    ingredient = ingredient,
                    quantity = ingredient.purchaseQuantity,
                    unit = ingredient.purchaseUnit
                ) ?: return null

            val totalItemCost =
                costPerPurchaseUnit * purchaseCount.toDouble()

            if (!totalItemCost.isFinite()) {
                return null
            }

            purchaseItems.add(
                GroceryPurchaseItem(
                    ingredientId = ingredient.id,
                    name = ingredient.name,
                    requiredQuantity = quantityToPurchase,
                    requiredUnit = unit,
                    quantityToPurchase = quantityToPurchase,
                    purchaseQuantity = ingredient.purchaseQuantity,
                    purchaseUnit = ingredient.purchaseUnit,
                    purchaseCount = purchaseCount,
                    purchasedQuantity = purchasedQuantity,
                    estimatedCostCentavos = totalItemCost
                )
            )
        }

        val totalCost = purchaseItems.sumOf {
            it.estimatedCostCentavos
        }

        if (!totalCost.isFinite()) {
            return null
        }

        return GroceryPurchaseEstimate(
            items = purchaseItems,
            totalCostCentavos = totalCost
        )
    }
}
