package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.ConsolidatedIngredient
import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MeasurementUnit
import com.example.kaintayo.domain.model.PantryStock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class GroceryPurchaseCalculatorUseCaseTest {

    @Test
    fun chicken300GramsRequiresOneKilogramPurchase() {
        val chicken = ingredient(
            id = "chicken",
            name = "Chicken",
            price = 21000,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.KILOGRAM,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.KILOGRAM
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(
                ConsolidatedIngredient(
                    ingredientId = "chicken",
                    quantity = 300.0,
                    unit = MeasurementUnit.GRAM
                )
            ),
            ingredientCatalog = listOf(chicken)
        )

        assertNotNull(result)
        assertEquals(1L, result!!.items[0].purchaseCount)
        assertEquals(1.0, result.items[0].purchasedQuantity, 0.0001)
        assertEquals(21000.0, result.totalCostCentavos, 0.0001)
    }

    @Test
    fun chicken1200GramsRequiresTwoKilograms() {
        val chicken = ingredient(
            id = "chicken",
            name = "Chicken",
            price = 21000,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.KILOGRAM,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.KILOGRAM
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(
                ConsolidatedIngredient(
                    ingredientId = "chicken",
                    quantity = 1200.0,
                    unit = MeasurementUnit.GRAM
                )
            ),
            ingredientCatalog = listOf(chicken)
        )

        assertNotNull(result)
        assertEquals(2L, result!!.items[0].purchaseCount)
        assertEquals(2.0, result.items[0].purchasedQuantity, 0.0001)
        assertEquals(42000.0, result.totalCostCentavos, 0.0001)
    }

    @Test
    fun cookingOil100MillilitersRequiresOneLiterBottle() {
        val oil = ingredient(
            id = "cooking-oil",
            name = "Cooking Oil",
            price = 10000,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.LITER,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.LITER
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(
                ConsolidatedIngredient(
                    ingredientId = "cooking-oil",
                    quantity = 100.0,
                    unit = MeasurementUnit.MILLILITER
                )
            ),
            ingredientCatalog = listOf(oil)
        )

        assertNotNull(result)
        assertEquals(1L, result!!.items[0].purchaseCount)
        assertEquals(10000.0, result.totalCostCentavos, 0.0001)
    }

    @Test
    fun eggsAreRoundedUpToWholePieces() {
        val eggs = ingredient(
            id = "egg",
            name = "Egg",
            price = 1200,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.PIECE,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.PIECE
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(
                ConsolidatedIngredient(
                    ingredientId = "egg",
                    quantity = 5.0,
                    unit = MeasurementUnit.PIECE
                )
            ),
            ingredientCatalog = listOf(eggs)
        )

        assertNotNull(result)
        assertEquals(5L, result!!.items[0].purchaseCount)
        assertEquals(6000.0, result.totalCostCentavos, 0.0001)
    }

    @Test
    fun tofuIsRoundedUpToWholePacks() {
        val tofu = ingredient(
            id = "tofu",
            name = "Tofu",
            price = 2500,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.PACK,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.PACK
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(
                ConsolidatedIngredient(
                    ingredientId = "tofu",
                    quantity = 2.0,
                    unit = MeasurementUnit.PACK
                )
            ),
            ingredientCatalog = listOf(tofu)
        )

        assertNotNull(result)
        assertEquals(2L, result!!.items[0].purchaseCount)
        assertEquals(5000.0, result.totalCostCentavos, 0.0001)
    }

    @Test
    fun rejectsMissingIngredient() {
        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(
                ConsolidatedIngredient(
                    ingredientId = "missing",
                    quantity = 100.0,
                    unit = MeasurementUnit.GRAM
                )
            ),
            ingredientCatalog = emptyList()
        )

        assertNull(result)
    }

    @Test
    fun rejectsDuplicateCatalogIds() {
        val chicken = ingredient(
            id = "chicken",
            name = "Chicken",
            price = 21000,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.KILOGRAM,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.KILOGRAM
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = emptyList(),
            ingredientCatalog = listOf(chicken, chicken)
        )

        assertNull(result)
    }

    @Test
    fun rejectsDuplicateIngredientLines() {
        val chicken = ingredient(
            id = "chicken",
            name = "Chicken",
            price = 21000,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.KILOGRAM,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.KILOGRAM
        )

        val item = ConsolidatedIngredient(
            ingredientId = "chicken",
            quantity = 300.0,
            unit = MeasurementUnit.GRAM
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(item, item),
            ingredientCatalog = listOf(chicken)
        )

        assertNull(result)
    }

    private fun ingredient(
        id: String,
        name: String,
        price: Long,
        priceQuantity: Double,
        priceUnit: MeasurementUnit,
        purchaseQuantity: Double,
        purchaseUnit: MeasurementUnit
    ) = Ingredient(
        id = id,
        name = name,
        defaultUnit = priceUnit,
        estimatedPriceCentavos = price,
        priceQuantity = priceQuantity,
        priceUnit = priceUnit,
        purchaseQuantity = purchaseQuantity,
        purchaseUnit = purchaseUnit
    )

    @Test
    fun subtractsPantryStockBeforeRoundingPurchase() {
        val rice = ingredient(
            id = "rice",
            name = "Rice",
            price = 6000,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.KILOGRAM,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.KILOGRAM
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(
                ConsolidatedIngredient(
                    ingredientId = "rice",
                    quantity = 1500.0,
                    unit = MeasurementUnit.GRAM
                )
            ),
            ingredientCatalog = listOf(rice),
            pantryStock = listOf(
                PantryStock(
                    ingredientId = "rice",
                    quantity = 500.0,
                    unit = MeasurementUnit.GRAM
                )
            )
        )

        assertNotNull(result)

        val item = result!!.items.first()

        assertEquals(1500.0, item.requiredQuantity, 0.0001)
        assertEquals(500.0, item.availableQuantity, 0.0001)
        assertEquals(1000.0, item.quantityToPurchase, 0.0001)
        assertEquals(1L, item.purchaseCount)
        assertEquals(1.0, item.purchasedQuantity, 0.0001)
        assertEquals(6000.0, result.totalCostCentavos, 0.0001)
    }

    @Test
    fun sufficientPantryStockRequiresNoPurchase() {
        val rice = ingredient(
            id = "rice",
            name = "Rice",
            price = 6000,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.KILOGRAM,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.KILOGRAM
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(
                ConsolidatedIngredient(
                    ingredientId = "rice",
                    quantity = 1000.0,
                    unit = MeasurementUnit.GRAM
                )
            ),
            ingredientCatalog = listOf(rice),
            pantryStock = listOf(
                PantryStock(
                    ingredientId = "rice",
                    quantity = 1.0,
                    unit = MeasurementUnit.KILOGRAM
                )
            )
        )

        assertNotNull(result)

        val item = result!!.items.first()

        assertEquals(1000.0, item.requiredQuantity, 0.0001)
        assertEquals(1000.0, item.availableQuantity, 0.0001)
        assertEquals(0.0, item.quantityToPurchase, 0.0001)
        assertEquals(0L, item.purchaseCount)
        assertEquals(0.0, item.purchasedQuantity, 0.0001)
        assertEquals(0.0, result.totalCostCentavos, 0.0001)
    }

    @Test
    fun excessPantryStockDoesNotCreateNegativePurchaseRequirement() {
        val eggs = ingredient(
            id = "egg",
            name = "Egg",
            price = 1200,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.PIECE,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.PIECE
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(
                ConsolidatedIngredient(
                    ingredientId = "egg",
                    quantity = 5.0,
                    unit = MeasurementUnit.PIECE
                )
            ),
            ingredientCatalog = listOf(eggs),
            pantryStock = listOf(
                PantryStock(
                    ingredientId = "egg",
                    quantity = 10.0,
                    unit = MeasurementUnit.PIECE
                )
            )
        )

        assertNotNull(result)

        val item = result!!.items.first()

        assertEquals(5.0, item.requiredQuantity, 0.0001)
        assertEquals(10.0, item.availableQuantity, 0.0001)
        assertEquals(0.0, item.quantityToPurchase, 0.0001)
        assertEquals(0L, item.purchaseCount)
        assertEquals(0.0, result.totalCostCentavos, 0.0001)
    }

    @Test
    fun convertsPantryStockFromLitersToMilliliters() {
        val oil = ingredient(
            id = "cooking-oil",
            name = "Cooking Oil",
            price = 10000,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.LITER,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.LITER
        )

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = listOf(
                ConsolidatedIngredient(
                    ingredientId = "cooking-oil",
                    quantity = 750.0,
                    unit = MeasurementUnit.MILLILITER
                )
            ),
            ingredientCatalog = listOf(oil),
            pantryStock = listOf(
                PantryStock(
                    ingredientId = "cooking-oil",
                    quantity = 0.5,
                    unit = MeasurementUnit.LITER
                )
            )
        )

        assertNotNull(result)

        val item = result!!.items.first()

        assertEquals(750.0, item.requiredQuantity, 0.0001)
        assertEquals(500.0, item.availableQuantity, 0.0001)
        assertEquals(250.0, item.quantityToPurchase, 0.0001)
        assertEquals(1L, item.purchaseCount)
        assertEquals(10000.0, result.totalCostCentavos, 0.0001)
    }
}