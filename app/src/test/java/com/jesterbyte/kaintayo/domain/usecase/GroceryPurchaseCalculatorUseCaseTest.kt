package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.ConsolidatedIngredient
import com.jesterbyte.kaintayo.domain.model.Ingredient
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit
import com.jesterbyte.kaintayo.domain.model.RecipeIngredient
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

    @Test
    fun purchasesFullRiceRequirementWithoutPantryDeduction() {
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
            ingredientCatalog = listOf(rice)
        )

        assertNotNull(result)

        val item = result!!.items.first()

        assertEquals(1500.0, item.requiredQuantity, 0.0001)
        assertEquals(1500.0, item.quantityToPurchase, 0.0001)
        assertEquals(2L, item.purchaseCount)
        assertEquals(2.0, item.purchasedQuantity, 0.0001)
        assertEquals(12000.0, result.totalCostCentavos, 0.0001)
    }

    @Test
    fun purchasesFullOilRequirementWithoutPantryDeduction() {
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
            ingredientCatalog = listOf(oil)
        )

        assertNotNull(result)

        val item = result!!.items.first()

        assertEquals(750.0, item.requiredQuantity, 0.0001)
        assertEquals(750.0, item.quantityToPurchase, 0.0001)
        assertEquals(1L, item.purchaseCount)
        assertEquals(1.0, item.purchasedQuantity, 0.0001)
        assertEquals(10000.0, result.totalCostCentavos, 0.0001)
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
    fun consolidatesSharedChickenAcrossMealsBeforeCalculatingPurchases() {
        val chicken = ingredient(
            id = "chicken",
            name = "Chicken",
            price = 21000,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.KILOGRAM,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.KILOGRAM
        )

        val recipeIngredients = listOf(
            RecipeIngredient("chicken", 300.0, MeasurementUnit.GRAM),
            RecipeIngredient("chicken", 500.0, MeasurementUnit.GRAM),
            RecipeIngredient("chicken", 400.0, MeasurementUnit.GRAM)
        )

        val consolidatedIngredients =
            IngredientConsolidationUseCase.consolidate(recipeIngredients)

        assertNotNull(consolidatedIngredients)
        assertEquals(1, consolidatedIngredients!!.size)
        assertEquals(1200.0, consolidatedIngredients[0].quantity, 0.0001)

        val result = GroceryPurchaseCalculatorUseCase.calculate(
            consolidatedIngredients = consolidatedIngredients,
            ingredientCatalog = listOf(chicken)
        )

        assertNotNull(result)

        val item = result!!.items.first()

        assertEquals(2L, item.purchaseCount)
        assertEquals(2.0, item.purchasedQuantity, 0.0001)
        assertEquals(42000.0, result.totalCostCentavos, 0.0001)
    }
}
