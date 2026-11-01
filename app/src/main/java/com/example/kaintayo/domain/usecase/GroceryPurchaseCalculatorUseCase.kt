package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.ConsolidatedIngredient
import com.example.kaintayo.domain.model.GroceryPurchaseEstimate
import com.example.kaintayo.domain.model.GroceryPurchaseItem
import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.PantryStock
import kotlin.math.ceil

object GroceryPurchaseCalculatorUseCase {

    fun calculate(
        consolidatedIngredients: List<ConsolidatedIngredient>,
        ingredientCatalog: List<Ingredient>,
        pantryStock: List<PantryStock> = emptyList()
    ): GroceryPurchaseEstimate? {

        val ingredientsById = ingredientCatalog.associateBy { it.id }

        // Reject duplicate catalog IDs.
        if (ingredientsById.size != ingredientCatalog.size) {
            return null
        }

        // Reject duplicate pantry records.
        val pantryById = pantryStock.associateBy { it.ingredientId }

        if (pantryById.size != pantryStock.size) {
            return null
        }

        // Inputs should already be consolidated by ingredient.
        val requestedIds = consolidatedIngredients.map { it.ingredientId }

        if (requestedIds.toSet().size != requestedIds.size) {
            return null
        }

        val purchaseItems = mutableListOf<GroceryPurchaseItem>()

        for (item in consolidatedIngredients) {
            val ingredient = ingredientsById[item.ingredientId]
                ?: return null

            if (!item.quantity.isFinite() || item.quantity < 0.0) {
                return null
            }

            if (!ingredient.purchaseQuantity.isFinite() ||
                ingredient.purchaseQuantity <= 0.0
            ) {
                return null
            }

            // Find the saved stock for this ingredient.
            val stock = pantryById[item.ingredientId]

            // Convert available pantry stock to the requirement unit.
            val availableQuantity = if (stock != null) {
                if (!stock.quantity.isFinite() || stock.quantity < 0.0) {
                    return null
                }

                MeasurementUnitConverter.convert(
                    quantity = stock.quantity,
                    from = stock.unit,
                    to = item.unit
                ) ?: return null
            } else {
                0.0
            }

            if (!availableQuantity.isFinite()) {
                return null
            }

            // Never allow the remaining requirement to become negative.
            val quantityToPurchase = (
                    item.quantity - availableQuantity
                    ).coerceAtLeast(0.0)

            // Convert only the remaining requirement into the purchase unit.
            val requiredPurchaseUnits = MeasurementUnitConverter.convert(
                quantity = quantityToPurchase,
                from = item.unit,
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
                    requiredQuantity = item.quantity,
                    requiredUnit = item.unit,
                    availableQuantity = availableQuantity,
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