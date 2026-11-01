package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MealPlan
import com.example.kaintayo.domain.model.MealType
import com.example.kaintayo.domain.model.MeasurementUnit
import com.example.kaintayo.domain.model.PlannedMeal
import com.example.kaintayo.domain.model.Recipe
import com.example.kaintayo.domain.model.RecipeIngredient
import com.example.kaintayo.domain.repository.IngredientRepository
import com.example.kaintayo.domain.repository.MealPlanRepository
import com.example.kaintayo.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GeneratePaydayMealPlanUseCaseTest {

    private lateinit var ingredientRepository: FakeIngredientRepository
    private lateinit var recipeRepository: FakeRecipeRepository
    private lateinit var mealPlanRepository: FakeMealPlanRepository
    private lateinit var useCase: GeneratePaydayMealPlanUseCase

    private val today = LocalDate.of(2026, 10, 31)

    @Before
    fun setUp() {
        ingredientRepository = FakeIngredientRepository()
        recipeRepository = FakeRecipeRepository()
        mealPlanRepository = FakeMealPlanRepository()

        useCase = GeneratePaydayMealPlanUseCase(
            ingredientRepository = ingredientRepository,
            recipeRepository = recipeRepository,
            mealPlanRepository = mealPlanRepository
        )
    }

    @Test
    fun generatesAndSavesCompletePlanWhenBudgetIsSufficient() = runBlocking {
        ingredientRepository.ingredients = testIngredients()
        recipeRepository.recipes = testRecipes()

        val result = useCase(
            today = today, targetServings = 2, budgetCentavos = 100_000
        )

        assertNotNull(result)
        assertEquals(
            MealPlanGenerationStatus.GENERATED, result!!.status
        )
        assertEquals(48, result.mealPlans.size)
        assertEquals(
            LocalDate.of(2026, 10, 30), result.cycle.startDate
        )
        assertEquals(
            LocalDate.of(2026, 11, 14), result.cycle.endDate
        )
        assertTrue(result.estimatedPurchaseCostCentavos!! > 0)

        assertEquals(1, mealPlanRepository.replacementCount)
        assertEquals(result.mealPlans, mealPlanRepository.savedMealPlans)
    }

    @Test
    fun overBudgetResultDoesNotReplaceExistingMealPlan() = runBlocking {
        ingredientRepository.ingredients = testIngredients()
        recipeRepository.recipes = testRecipes()

        val result = useCase(
            today = today, targetServings = 2, budgetCentavos = 0
        )

        assertNotNull(result)
        assertEquals(
            MealPlanGenerationStatus.OVER_BUDGET, result!!.status
        )
        assertTrue(result.mealPlans.isEmpty())
        assertTrue(result.estimatedPurchaseCostCentavos!! > 0)
        assertEquals(0, mealPlanRepository.replacementCount)
    }

    @Test
    fun invalidInputsReturnNullWithoutSaving() = runBlocking {
        val invalidServingsResult = useCase(
            today = today, targetServings = 0, budgetCentavos = 100_000
        )

        val invalidBudgetResult = useCase(
            today = today, targetServings = 2, budgetCentavos = -1
        )

        assertNull(invalidServingsResult)
        assertNull(invalidBudgetResult)
        assertEquals(0, mealPlanRepository.replacementCount)
    }

    @Test
    fun insufficientRecipesReturnCannotGenerateWithoutSaving() = runBlocking {
        ingredientRepository.ingredients = testIngredients()
        recipeRepository.recipes = emptyList()

        val result = useCase(
            today = today, targetServings = 2, budgetCentavos = 100_000
        )

        assertNotNull(result)
        assertEquals(
            MealPlanGenerationStatus.CANNOT_GENERATE, result!!.status
        )
        assertTrue(result.mealPlans.isEmpty())
        assertNull(result.estimatedPurchaseCostCentavos)
        assertEquals(0, mealPlanRepository.replacementCount)
    }

    private fun testIngredients() = listOf(
        Ingredient(
            id = "test-ingredient",
            name = "Test Ingredient",
            defaultUnit = MeasurementUnit.GRAM,
            estimatedPriceCentavos = 1_000,
            priceQuantity = 1_000.0,
            priceUnit = MeasurementUnit.GRAM
        )
    )

    private fun testRecipes(): List<Recipe> {
        return MealType.entries.flatMap { mealType ->
            (1..4).map { index ->
                Recipe(
                    id = "${mealType.name.lowercase()}-recipe-$index",
                    name = "Test ${mealType.name} Recipe $index",
                    description = "Test recipe",
                    mealType = mealType,
                    servings = 2,
                    ingredients = listOf(
                        RecipeIngredient(
                            ingredientId = "test-ingredient",
                            quantity = 100.0,
                            unit = MeasurementUnit.GRAM
                        )
                    ),
                    instructions = listOf("Prepare test recipe"),
                    preparationMinutes = 15
                )
            }
        }
    }

    private class FakeIngredientRepository : IngredientRepository {
        var ingredients: List<Ingredient> = emptyList()

        override fun getAllIngredients(): Flow<List<Ingredient>> = flowOf(ingredients)

        override suspend fun getIngredientById(id: String): Ingredient? =
            ingredients.find { it.id == id }

        override suspend fun saveIngredient(ingredient: Ingredient) {
            ingredients = ingredients.filterNot { it.id == ingredient.id } + ingredient
        }

        override suspend fun saveIngredients(ingredients: List<Ingredient>) {
            ingredients.forEach { saveIngredient(it) }
        }

        override suspend fun deleteIngredient(id: String) {
            ingredients = ingredients.filterNot { it.id == id }
        }
    }

    private class FakeRecipeRepository : RecipeRepository {
        var recipes: List<Recipe> = emptyList()

        override fun getAllRecipes(): Flow<List<Recipe>> = flowOf(recipes)

        override suspend fun getRecipeById(id: String): Recipe? = recipes.find { it.id == id }

        override suspend fun saveRecipe(recipe: Recipe) {
            recipes = recipes.filterNot { it.id == recipe.id } + recipe
        }

        override suspend fun deleteRecipe(id: String) {
            recipes = recipes.filterNot { it.id == id }
        }
    }

    private class FakeMealPlanRepository : MealPlanRepository {
        var savedMealPlans: List<MealPlan> = emptyList()
        var replacementCount = 0

        override fun getMealsForDate(
            date: LocalDate
        ): Flow<List<MealPlan>> = flowOf(savedMealPlans.filter { it.date == date })

        override fun getMealsForPeriod(
            startDate: LocalDate, endDate: LocalDate
        ): Flow<List<MealPlan>> = flowOf(
            savedMealPlans.filter {
                !it.date.isBefore(startDate) && !it.date.isAfter(endDate)
            })

        override suspend fun saveMealPlan(mealPlan: MealPlan) {
            savedMealPlans = savedMealPlans.filterNot {
                it.date == mealPlan.date && it.mealType == mealPlan.mealType
            } + mealPlan
        }

        override suspend fun saveMealPlans(mealPlans: List<MealPlan>) {
            mealPlans.forEach { saveMealPlan(it) }
        }

        override suspend fun deleteMeal(
            date: LocalDate, mealType: MealType
        ) {
            savedMealPlans = savedMealPlans.filterNot {
                it.date == date && it.mealType == mealType
            }
        }

        override suspend fun replaceMealPlansForPeriod(
            startDate: LocalDate, endDate: LocalDate, mealPlans: List<MealPlan>
        ) {
            replacementCount++

            savedMealPlans = savedMealPlans.filterNot {
                !it.date.isBefore(startDate) && !it.date.isAfter(endDate)
            } + mealPlans
        }

        override fun getPlannedMealsForPeriod(
            startDate: LocalDate, endDate: LocalDate
        ): Flow<List<PlannedMeal>> {
            return flowOf(emptyList())
        }
    }
}
