
package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.BudgetStatus
import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MealType
import com.example.kaintayo.domain.model.MeasurementUnit
import com.example.kaintayo.domain.model.PantryStock
import com.example.kaintayo.domain.model.Recipe
import com.example.kaintayo.domain.model.RecipeIngredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MealPlanGroceryEstimateUseCaseTest {

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

    private val tinola = Recipe(
        id = "tinola",
        name = "Tinolang Manok",
        description = "Chicken soup with sayote",
        mealType = MealType.LUNCH,
        servings = 2,
        ingredients = listOf(
            RecipeIngredient(
                "chicken",
                500.0,
                MeasurementUnit.GRAM
            ),
            RecipeIngredient(
                "sayote",
                1.0,
                MeasurementUnit.PIECE
            )
        ),
        instructions = listOf("Cook chicken.", "Add sayote."),
        preparationMinutes = 40
    )

    private val chickenAdobo = Recipe(
        id = "chicken-adobo",
        name = "Chicken Adobo",
        description = "Braised chicken",
        mealType = MealType.DINNER,
        servings = 4,
        ingredients = listOf(
            RecipeIngredient(
                "chicken",
                1.0,
                MeasurementUnit.KILOGRAM
            ),
            RecipeIngredient(
                "sayote",
                2.0,
                MeasurementUnit.PIECE
            )
        ),
        instructions = listOf("Simmer chicken."),
        preparationMinutes = 45
    )

    @Test
    fun combinesScaledRecipesAndCalculatesTotalCost() {
        val result = MealPlanGroceryEstimateUseCase.estimate(
            recipes = listOf(tinola, chickenAdobo),
            targetServings = 4,
            ingredientCatalog = catalog,
            budgetCentavos = 50000L
        )!!

        // Tinola: 1,000 g chicken and 2 sayote.
        // Adobo: 1,000 g chicken and 2 sayote.
        // Total: 2 kg chicken and 4 sayote.
        assertEquals(
            2000.0,
            result.groceryCost.items[0].quantity,
            0.0001
        )

        assertEquals(
            4.0,
            result.groceryCost.items[1].quantity,
            0.0001
        )

        // Chicken: 2 x 15,000 centavos.
        // Sayote: 4 x 1,500 centavos.
        assertEquals(
            36000.0,
            result.groceryCost.totalCostCentavos,
            0.0001
        )

        assertEquals(
            36000L,
            result.budgetAssessment.estimatedCostCentavos
        )

        assertEquals(
            14000L,
            result.budgetAssessment.remainingBudgetCentavos
        )

        assertEquals(
            BudgetStatus.WITHIN_BUDGET,
            result.budgetAssessment.status
        )
    }

    @Test
    fun identifiesMealPlanThatExceedsBudget() {
        val result = MealPlanGroceryEstimateUseCase.estimate(
            recipes = listOf(tinola, chickenAdobo),
            targetServings = 4,
            ingredientCatalog = catalog,
            budgetCentavos = 30000L
        )!!

        assertEquals(
            BudgetStatus.OVER_BUDGET,
            result.budgetAssessment.status
        )

        assertEquals(
            -6000L,
            result.budgetAssessment.remainingBudgetCentavos
        )
    }

    @Test
    fun rejectsEmptyRecipeList() {
        assertNull(
            MealPlanGroceryEstimateUseCase.estimate(
                recipes = emptyList(),
                targetServings = 4,
                ingredientCatalog = catalog,
                budgetCentavos = 50000L
            )
        )
    }

    @Test
    fun rejectsMissingIngredientFromCatalog() {
        assertNull(
            MealPlanGroceryEstimateUseCase.estimate(
                recipes = listOf(tinola),
                targetServings = 4,
                ingredientCatalog = listOf(chicken),
                budgetCentavos = 50000L
            )
        )
    }

    @Test
    fun rejectsInvalidTargetServings() {
        assertNull(
            MealPlanGroceryEstimateUseCase.estimate(
                recipes = listOf(tinola),
                targetServings = 0,
                ingredientCatalog = catalog,
                budgetCentavos = 50000L
            )
        )
    }

    @Test
    fun rejectsNegativeBudget() {
        assertNull(
            MealPlanGroceryEstimateUseCase.estimate(
                recipes = listOf(tinola),
                targetServings = 4,
                ingredientCatalog = catalog,
                budgetCentavos = -100L
            )
        )
    }

    @Test
    fun pantryStockReducesPurchaseCostButNotRecipeConsumptionCost() {
        val result = MealPlanGroceryEstimateUseCase.estimate(
            recipes = listOf(tinola, chickenAdobo),
            targetServings = 4,
            ingredientCatalog = catalog,
            budgetCentavos = 20000L,
            pantryStock = listOf(
                PantryStock(
                    ingredientId = "chicken",
                    quantity = 1.2,
                    unit = MeasurementUnit.KILOGRAM
                ),
                PantryStock(
                    ingredientId = "sayote",
                    quantity = 2.0,
                    unit = MeasurementUnit.PIECE
                )
            )
        )!!

        // Recipes still consume 2 kg chicken and 4 sayote.
        assertEquals(
            36000.0,
            result.groceryCost.totalCostCentavos,
            0.0001
        )

        val chickenPurchase = result.purchaseEstimate.items
            .first { it.ingredientId == "chicken" }

        val sayotePurchase = result.purchaseEstimate.items
            .first { it.ingredientId == "sayote" }

        // 1.2 kg chicken stock leaves 800 g to buy.
        // The purchase size is 1 kg, so buy 1 kg.
        assertEquals(2000.0, chickenPurchase.requiredQuantity, 0.0001)
        assertEquals(1200.0, chickenPurchase.availableQuantity, 0.0001)
        assertEquals(800.0, chickenPurchase.quantityToPurchase, 0.0001)
        assertEquals(1L, chickenPurchase.purchaseCount)

        // Two sayote are already available; buy the remaining two.
        assertEquals(4.0, sayotePurchase.requiredQuantity, 0.0001)
        assertEquals(2.0, sayotePurchase.availableQuantity, 0.0001)
        assertEquals(2.0, sayotePurchase.quantityToPurchase, 0.0001)
        assertEquals(2L, sayotePurchase.purchaseCount)

        // Purchase cost: 15,000 chicken + 3,000 sayote.
        assertEquals(
            18000.0,
            result.purchaseEstimate.totalCostCentavos,
            0.0001
        )

        assertEquals(
            18000L,
            result.budgetAssessment.estimatedCostCentavos
        )

        assertEquals(
            2000L,
            result.budgetAssessment.remainingBudgetCentavos
        )

        assertEquals(
            BudgetStatus.WITHIN_BUDGET,
            result.budgetAssessment.status
        )
    }

    @Test
    fun emptyPantryPreservesOriginalPurchaseEstimate() {
        val result = MealPlanGroceryEstimateUseCase.estimate(
            recipes = listOf(tinola, chickenAdobo),
            targetServings = 4,
            ingredientCatalog = catalog,
            budgetCentavos = 50000L,
            pantryStock = emptyList()
        )!!

        assertEquals(
            36000.0,
            result.purchaseEstimate.totalCostCentavos,
            0.0001
        )

        assertEquals(
            36000L,
            result.budgetAssessment.estimatedCostCentavos
        )
    }
}
