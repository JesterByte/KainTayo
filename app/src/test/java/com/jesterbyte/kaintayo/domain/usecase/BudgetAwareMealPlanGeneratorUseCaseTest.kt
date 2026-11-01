package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.Ingredient
import com.jesterbyte.kaintayo.domain.model.MealPlanSlot
import com.jesterbyte.kaintayo.domain.model.MealType
import com.jesterbyte.kaintayo.domain.model.MeasurementUnit
import com.jesterbyte.kaintayo.domain.model.Recipe
import com.jesterbyte.kaintayo.domain.model.RecipeIngredient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class BudgetAwareMealPlanGeneratorUseCaseTest {

    private data class Fixture(
        val recipes: List<Recipe>,
        val ingredients: List<Ingredient>
    )

    private fun createFixture(
        mealType: MealType,
        costsCentavos: List<Long>,
        servings: Int = 1
    ): Fixture {

        val ingredients = costsCentavos.mapIndexed { index, cost ->
            Ingredient(
                id = "${mealType.name.lowercase()}-ingredient-$index",
                name = "Ingredient $index",
                defaultUnit = MeasurementUnit.PIECE,
                estimatedPriceCentavos = cost,
                priceQuantity = 1.0,
                priceUnit = MeasurementUnit.PIECE
            )
        }

        val recipes = List(costsCentavos.size) { index ->
            Recipe(
                id = "${mealType.name.lowercase()}-recipe-$index",
                name = "Recipe $index",
                description = "Test recipe $index",
                mealType = mealType,
                servings = servings,
                ingredients = listOf(
                    RecipeIngredient(
                        ingredientId = "${mealType.name.lowercase()}-ingredient-$index",
                        quantity = 1.0,
                        unit = MeasurementUnit.PIECE
                    )
                ),
                instructions = listOf("Prepare ingredient."),
                preparationMinutes = 20
            )
        }

        return Fixture(recipes, ingredients)
    }

    private fun createSlots(
        count: Int,
        mealType: MealType = MealType.BREAKFAST
    ): List<MealPlanSlot> {
        val startDate = LocalDate.of(2026, 10, 30)

        return (0 until count).map { index ->
            MealPlanSlot(
                date = startDate.plusDays(index.toLong()),
                mealType = mealType
            )
        }
    }

    @Test
    fun generatesCompletePlanWithinBudget() {
        val fixture = createFixture(
            mealType = MealType.BREAKFAST,
            costsCentavos = listOf(1000L, 2000L, 3000L, 4000L)
        )

        val result = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = createSlots(4),
            recipes = fixture.recipes,
            targetServings = 1,
            ingredientCatalog = fixture.ingredients,
            budgetCentavos = 10000L
        )

        assertNotNull(result)
        assertTrue(result!!.fitsBudget)
        assertEquals(4, result.mealPlans.size)
        assertEquals(10000L, result.estimatedPurchaseCostCentavos)
        assertEquals(0L, result.remainingBudgetCentavos)
    }

    @Test
    fun returnsNoApprovedPlanWhenMinimumCostExceedsBudget() {
        val fixture = createFixture(
            mealType = MealType.BREAKFAST,
            costsCentavos = listOf(1000L, 2000L, 3000L, 4000L)
        )

        val result = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = createSlots(4),
            recipes = fixture.recipes,
            targetServings = 1,
            ingredientCatalog = fixture.ingredients,
            budgetCentavos = 9999L
        )

        assertNotNull(result)
        assertFalse(result!!.fitsBudget)
        assertTrue(result.mealPlans.isEmpty())
        assertEquals(10000L, result.estimatedPurchaseCostCentavos)
        assertEquals(-1L, result.remainingBudgetCentavos)
    }

    @Test
    fun neverRepeatsRecipeWithinPreviousThreeOccurrences() {
        val fixture = createFixture(
            mealType = MealType.BREAKFAST,
            costsCentavos = listOf(1000L, 1000L, 1000L, 1000L)
        )

        val result = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = createSlots(12),
            recipes = fixture.recipes,
            targetServings = 1,
            ingredientCatalog = fixture.ingredients,
            budgetCentavos = 12000L
        )

        assertNotNull(result)
        val selectedIds = result!!.mealPlans.map { it.recipeId }

        for (index in 3 until selectedIds.size) {
            assertFalse(
                selectedIds[index] in selectedIds
                    .subList(index - 3, index)
            )
        }
    }

    @Test
    fun supportsSeparateVarietyRulesForEachMealType() {
        val breakfast = createFixture(
            MealType.BREAKFAST,
            listOf(1000L, 1000L, 1000L, 1000L)
        )
        val lunch = createFixture(
            MealType.LUNCH,
            listOf(2000L, 2000L, 2000L, 2000L)
        )
        val dinner = createFixture(
            MealType.DINNER,
            listOf(3000L, 3000L, 3000L, 3000L)
        )

        val allRecipes =
            breakfast.recipes + lunch.recipes + dinner.recipes
        val allIngredients =
            breakfast.ingredients + lunch.ingredients + dinner.ingredients

        val slots =
            createSlots(4, MealType.BREAKFAST) +
                    createSlots(4, MealType.LUNCH) +
                    createSlots(4, MealType.DINNER)

        val result = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = slots,
            recipes = allRecipes,
            targetServings = 1,
            ingredientCatalog = allIngredients,
            budgetCentavos = 24000L
        )

        assertNotNull(result)
        assertTrue(result!!.fitsBudget)
        assertEquals(12, result.mealPlans.size)
        assertEquals(24000L, result.estimatedPurchaseCostCentavos)
    }

    @Test
    fun scalesRecipeCostsForTargetHouseholdSize() {
        val fixture = createFixture(
            mealType = MealType.BREAKFAST,
            costsCentavos = listOf(1000L, 1000L, 1000L, 1000L),
            servings = 1
        )

        val result = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = createSlots(4),
            recipes = fixture.recipes,
            targetServings = 2,
            ingredientCatalog = fixture.ingredients,
            budgetCentavos = 8000L
        )

        assertNotNull(result)
        assertEquals(8000L, result!!.estimatedPurchaseCostCentavos)
    }

    @Test
    fun rejectsInsufficientRecipeVariety() {
        val fixture = createFixture(
            mealType = MealType.BREAKFAST,
            costsCentavos = listOf(1000L, 1000L, 1000L)
        )

        val result = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = createSlots(4),
            recipes = fixture.recipes,
            targetServings = 1,
            ingredientCatalog = fixture.ingredients,
            budgetCentavos = 10000L
        )

        assertNull(result)
    }

    @Test
    fun rejectsDuplicateRecipeIds() {
        val fixture = createFixture(
            mealType = MealType.BREAKFAST,
            costsCentavos = listOf(1000L, 2000L, 3000L, 4000L)
        )

        val duplicateRecipes = fixture.recipes + fixture.recipes.first()

        val result = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = createSlots(4),
            recipes = duplicateRecipes,
            targetServings = 1,
            ingredientCatalog = fixture.ingredients,
            budgetCentavos = 10000L
        )

        assertNull(result)
    }

    @Test
    fun rejectsDuplicateMealSlots() {
        val fixture = createFixture(
            mealType = MealType.BREAKFAST,
            costsCentavos = listOf(1000L, 2000L, 3000L, 4000L)
        )

        val slots = createSlots(4) + createSlots(1)

        val result = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = slots,
            recipes = fixture.recipes,
            targetServings = 1,
            ingredientCatalog = fixture.ingredients,
            budgetCentavos = 10000L
        )

        assertNull(result)
    }

    @Test
    fun rejectsNegativeBudget() {
        val fixture = createFixture(
            mealType = MealType.BREAKFAST,
            costsCentavos = listOf(1000L, 2000L, 3000L, 4000L)
        )

        val result = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = createSlots(4),
            recipes = fixture.recipes,
            targetServings = 1,
            ingredientCatalog = fixture.ingredients,
            budgetCentavos = -1L
        )

        assertNull(result)
    }


    @Test
    fun consolidatesSharedIngredientsAcrossDifferentMealTypes() {
        val sharedIngredient = Ingredient(
            id = "onion",
            name = "Onion",
            defaultUnit = MeasurementUnit.GRAM,
            estimatedPriceCentavos = 1_000,
            priceQuantity = 1_000.0,
            priceUnit = MeasurementUnit.GRAM,
            purchaseQuantity = 1.0,
            purchaseUnit = MeasurementUnit.KILOGRAM
        )

        val breakfastRecipe = Recipe(
            id = "breakfast-onion",
            name = "Breakfast with Onion",
            description = "Test breakfast",
            mealType = MealType.BREAKFAST,
            servings = 1,
            ingredients = listOf(
                RecipeIngredient(
                    ingredientId = "onion",
                    quantity = 300.0,
                    unit = MeasurementUnit.GRAM
                )
            ),
            instructions = listOf("Prepare breakfast."),
            preparationMinutes = 10
        )

        val lunchRecipe = Recipe(
            id = "lunch-onion",
            name = "Lunch with Onion",
            description = "Test lunch",
            mealType = MealType.LUNCH,
            servings = 1,
            ingredients = listOf(
                RecipeIngredient(
                    ingredientId = "onion",
                    quantity = 300.0,
                    unit = MeasurementUnit.GRAM
                )
            ),
            instructions = listOf("Prepare lunch."),
            preparationMinutes = 15
        )

        val slots = listOf(
            MealPlanSlot(
                date = LocalDate.of(2026, 10, 30),
                mealType = MealType.BREAKFAST
            ),
            MealPlanSlot(
                date = LocalDate.of(2026, 10, 30),
                mealType = MealType.LUNCH
            )
        )

        val result = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = slots,
            recipes = listOf(breakfastRecipe, lunchRecipe),
            targetServings = 1,
            ingredientCatalog = listOf(sharedIngredient),
            budgetCentavos = 1_000L
        )

        assertNotNull(result)
        assertTrue(result!!.fitsBudget)
        assertEquals(2, result.mealPlans.size)

        // 300 g + 300 g = 600 g.
        // Both meals share one 1 kg purchase.
        assertEquals(1_000L, result.estimatedPurchaseCostCentavos)
        assertEquals(0L, result.remainingBudgetCentavos)
    }
}