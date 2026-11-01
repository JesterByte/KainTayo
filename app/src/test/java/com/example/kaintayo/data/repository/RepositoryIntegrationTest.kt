package com.example.kaintayo.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.kaintayo.data.catalog.FilipinoCatalogSeeder
import com.example.kaintayo.data.catalog.FilipinoIngredientCatalog
import com.example.kaintayo.data.catalog.FilipinoRecipeCatalog
import com.example.kaintayo.data.local.KainTayoDatabase
import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MealPlan
import com.example.kaintayo.domain.model.MealType
import com.example.kaintayo.domain.model.MeasurementUnit
import com.example.kaintayo.domain.model.Recipe
import com.example.kaintayo.domain.model.RecipeIngredient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class RepositoryIntegrationTest {

    private lateinit var database: KainTayoDatabase

    private lateinit var ingredientRepository: IngredientRepositoryImpl
    private lateinit var recipeRepository: RecipeRepositoryImpl
    private lateinit var mealPlanRepository: MealPlanRepositoryImpl

    private lateinit var catalogSeeder: FilipinoCatalogSeeder

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database = Room.inMemoryDatabaseBuilder(
            context,
            KainTayoDatabase::class.java
        ).build()

        ingredientRepository = IngredientRepositoryImpl(
            database.ingredientDao()
        )

        recipeRepository = RecipeRepositoryImpl(
            database.recipeDao()
        )

        mealPlanRepository = MealPlanRepositoryImpl(
            database.mealPlanDao()
        )

        catalogSeeder = FilipinoCatalogSeeder(
            database.ingredientDao(),
            database.recipeDao()
        )
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun ingredientCanBeSavedAndRetrieved() = runBlocking {
        val ingredient = Ingredient(
            id = "chicken",
            name = "Chicken",
            defaultUnit = MeasurementUnit.KILOGRAM,
            estimatedPriceCentavos = 15000,
            priceQuantity = 1.0,
            priceUnit = MeasurementUnit.KILOGRAM
        )

        ingredientRepository.saveIngredient(ingredient)

        val result = ingredientRepository
            .getIngredientById("chicken")

        assertEquals(ingredient, result)
    }

    @Test
    fun recipeAndIngredientsCanBeSavedTogether() = runBlocking {
        ingredientRepository.saveIngredients(
            listOf(
                Ingredient(
                    id = "chicken",
                    name = "Chicken",
                    defaultUnit = MeasurementUnit.KILOGRAM,
                    estimatedPriceCentavos = 15000,
                    priceQuantity = 1.0,
                    priceUnit = MeasurementUnit.KILOGRAM
                ),
                Ingredient(
                    id = "ginger",
                    name = "Ginger",
                    defaultUnit = MeasurementUnit.GRAM,
                    estimatedPriceCentavos = 2000,
                    priceQuantity = 100.0,
                    priceUnit = MeasurementUnit.GRAM
                )
            )
        )

        val recipe = Recipe(
            id = "tinolang-manok",
            name = "Tinolang Manok",
            description = "Filipino chicken soup",
            mealType = MealType.LUNCH,
            servings = 4,
            ingredients = listOf(
                RecipeIngredient(
                    ingredientId = "chicken",
                    quantity = 0.5,
                    unit = MeasurementUnit.KILOGRAM
                ),
                RecipeIngredient(
                    ingredientId = "ginger",
                    quantity = 20.0,
                    unit = MeasurementUnit.GRAM
                )
            ),
            instructions = listOf(
                "Prepare the ingredients",
                "Simmer the chicken",
                "Add vegetables"
            ),
            preparationMinutes = 40
        )

        recipeRepository.saveRecipe(recipe)

        val result = recipeRepository.getRecipeById(
            "tinolang-manok"
        )

        assertEquals(recipe, result)
    }

    @Test
    fun mealPlanCanBeSavedAndRetrieved() = runBlocking {
        val recipe = Recipe(
            id = "tinolang-manok",
            name = "Tinolang Manok",
            description = "Filipino chicken soup",
            mealType = MealType.LUNCH,
            servings = 4,
            ingredients = emptyList(),
            instructions = listOf("Cook the chicken"),
            preparationMinutes = 40
        )

        recipeRepository.saveRecipe(recipe)

        val mealPlan = MealPlan(
            date = LocalDate.of(2026, 10, 31),
            mealType = MealType.LUNCH,
            recipeId = "tinolang-manok"
        )

        mealPlanRepository.saveMealPlan(mealPlan)

        val result = mealPlanRepository
            .getMealsForDate(mealPlan.date)
            .first()

        assertEquals(1, result.size)
        assertEquals(mealPlan, result.first())
    }

    @Test
    fun deletingRecipeAlsoDeletesItsMealAssignments() = runBlocking {
        val recipe = Recipe(
            id = "tinolang-manok",
            name = "Tinolang Manok",
            description = "Filipino chicken soup",
            mealType = MealType.LUNCH,
            servings = 4,
            ingredients = emptyList(),
            instructions = emptyList(),
            preparationMinutes = 40
        )

        recipeRepository.saveRecipe(recipe)

        val date = LocalDate.of(2026, 10, 31)

        mealPlanRepository.saveMealPlan(
            MealPlan(
                date = date,
                mealType = MealType.LUNCH,
                recipeId = recipe.id
            )
        )

        recipeRepository.deleteRecipe(recipe.id)

        val result = mealPlanRepository
            .getMealsForDate(date)
            .first()

        assertTrue(result.isEmpty())
        assertNull(recipeRepository.getRecipeById(recipe.id))
    }

    @Test
    fun updatingIngredientPreservesRecipeAssociation() = runBlocking {
        val chicken = Ingredient(
            "chicken", "Chicken",
            MeasurementUnit.KILOGRAM, 15000, 1.0,
            MeasurementUnit.KILOGRAM
        )

        ingredientRepository.saveIngredient(chicken)

        val recipe = Recipe(
            "tinolang-manok",
            "Tinolang Manok",
            "Filipino chicken soup",
            MealType.LUNCH,
            4,
            listOf(
                RecipeIngredient(
                    "chicken", 0.5, MeasurementUnit.KILOGRAM
                )
            ),
            listOf("Cook the chicken"),
            40
        )

        recipeRepository.saveRecipe(recipe)

        val updatedChicken = chicken.copy(
            estimatedPriceCentavos = 18000
        )

        ingredientRepository.saveIngredient(updatedChicken)

        val savedRecipe = recipeRepository.getRecipeById(recipe.id)

        assertEquals(1, savedRecipe?.ingredients?.size)
        assertEquals("chicken", savedRecipe?.ingredients?.first()?.ingredientId)
    }

    @Test
    fun updatingRecipePreservesMealAssignments() = runBlocking {
        val recipe = Recipe(
            "tinolang-manok",
            "Tinolang Manok",
            "Filipino chicken soup",
            MealType.LUNCH,
            4,
            emptyList(),
            listOf("Cook the chicken"),
            40
        )

        recipeRepository.saveRecipe(recipe)

        val date = LocalDate.of(2026, 10, 31)

        mealPlanRepository.saveMealPlan(
            MealPlan(date, MealType.LUNCH, recipe.id)
        )

        val updatedRecipe = recipe.copy(
            description = "Chicken and vegetable soup"
        )

        recipeRepository.saveRecipe(updatedRecipe)

        val savedMeals = mealPlanRepository
            .getMealsForDate(date)
            .first()

        assertEquals(1, savedMeals.size)
        assertEquals(recipe.id, savedMeals.first().recipeId)
    }

    @Test
    fun catalogSeederInsertsAllCatalogItems() = runBlocking {
        catalogSeeder.seed()

        val ingredients = database.ingredientDao()
            .getAllIngredients()
            .first()

        val recipes = database.recipeDao()
            .getAllRecipes()
            .first()

        assertEquals(FilipinoIngredientCatalog.ingredients.size, ingredients.size)
        assertEquals(FilipinoRecipeCatalog.recipes.size, recipes.size)

        assertEquals(40, ingredients.size)
        assertEquals(30, recipes.size)
    }

    @Test
    fun runningCatalogSeederTwiceDoesNotCreateDuplicates() = runBlocking {
        catalogSeeder.seed()
        catalogSeeder.seed()

        val ingredients = database.ingredientDao()
            .getAllIngredients()
            .first()

        val recipes = database.recipeDao()
            .getAllRecipes()
            .first()

        assertEquals(40, ingredients.size)
        assertEquals(30, recipes.size)
    }

    @Test
    fun catalogSeederPreservesEditedIngredientPrices() = runBlocking {
        catalogSeeder.seed()

        val originalChicken = ingredientRepository
            .getIngredientById("chicken")
            ?: throw AssertionError("Chicken ingredient was not seeded")

        val editedPrice = originalChicken.copy(
            estimatedPriceCentavos = 25000
        )

        ingredientRepository.saveIngredient(editedPrice)

        catalogSeeder.seed()

        val savedChicken = ingredientRepository
            .getIngredientById("chicken")

        assertEquals(
            25000L,
            savedChicken?.estimatedPriceCentavos
        )
    }

    @Test
    fun catalogSeederPreservesEditedRecipesAndTheirIngredients() = runBlocking {
        catalogSeeder.seed()

        val originalRecipe = recipeRepository
            .getRecipeById("tinolang-manok")
            ?: throw AssertionError("Tinolang Manok was not seeded")

        val editedRecipe = originalRecipe.copy(
            description = "My family's updated Tinolang Manok recipe"
        )

        recipeRepository.saveRecipe(editedRecipe)

        catalogSeeder.seed()

        val savedRecipe = recipeRepository
            .getRecipeById("tinolang-manok")

        assertEquals(
            "My family's updated Tinolang Manok recipe",
            savedRecipe?.description
        )

        assertEquals(
            originalRecipe.ingredients,
            savedRecipe?.ingredients
        )
    }

    @Test
    fun replacingMealPlansReplacesOnlyMealsInsidePeriod() = runBlocking {
        val recipe = Recipe(
            id = "test-recipe",
            name = "Test Recipe",
            description = "Test",
            mealType = MealType.LUNCH,
            servings = 2,
            ingredients = emptyList(),
            instructions = emptyList(),
            preparationMinutes = 20
        )

        recipeRepository.saveRecipe(recipe)

        val startDate = LocalDate.of(2026, 10, 30)
        val endDate = LocalDate.of(2026, 11, 14)

        val outsideBefore = MealPlan(
            LocalDate.of(2026, 10, 29),
            MealType.LUNCH,
            recipe.id
        )

        val oldInside1 = MealPlan(
            LocalDate.of(2026, 10, 30),
            MealType.BREAKFAST,
            recipe.id
        )

        val oldInside2 = MealPlan(
            LocalDate.of(2026, 11, 14),
            MealType.DINNER,
            recipe.id
        )

        val outsideAfter = MealPlan(
            LocalDate.of(2026, 11, 15),
            MealType.LUNCH,
            recipe.id
        )

        mealPlanRepository.saveMealPlans(
            listOf(outsideBefore, oldInside1, oldInside2, outsideAfter)
        )

        val replacement = listOf(
            MealPlan(
                LocalDate.of(2026, 10, 30),
                MealType.LUNCH,
                recipe.id
            ),
            MealPlan(
                LocalDate.of(2026, 11, 14),
                MealType.BREAKFAST,
                recipe.id
            )
        )

        mealPlanRepository.replaceMealPlansForPeriod(
            startDate,
            endDate,
            replacement
        )

        val savedInside = mealPlanRepository
            .getMealsForPeriod(startDate, endDate)
            .first()

        assertEquals(2, savedInside.size)
        assertTrue(savedInside.containsAll(replacement))
        assertTrue(!savedInside.contains(oldInside1))
        assertTrue(!savedInside.contains(oldInside2))

        assertEquals(
            listOf(outsideBefore),
            mealPlanRepository.getMealsForDate(outsideBefore.date).first()
        )

        assertEquals(
            listOf(outsideAfter),
            mealPlanRepository.getMealsForDate(outsideAfter.date).first()
        )
    }

    @Test
    fun replacingMealPlansWithEmptyListClearsPeriod() = runBlocking {
        val recipe = Recipe(
            id = "test-recipe",
            name = "Test Recipe",
            description = "Test",
            mealType = MealType.LUNCH,
            servings = 2,
            ingredients = emptyList(),
            instructions = emptyList(),
            preparationMinutes = 20
        )

        recipeRepository.saveRecipe(recipe)

        val startDate = LocalDate.of(2026, 10, 30)
        val endDate = LocalDate.of(2026, 11, 14)

        val inside = MealPlan(
            LocalDate.of(2026, 10, 31),
            MealType.LUNCH,
            recipe.id
        )

        val outside = MealPlan(
            LocalDate.of(2026, 11, 15),
            MealType.DINNER,
            recipe.id
        )

        mealPlanRepository.saveMealPlans(listOf(inside, outside))

        mealPlanRepository.replaceMealPlansForPeriod(
            startDate = startDate,
            endDate = endDate,
            mealPlans = emptyList()
        )

        assertTrue(
            mealPlanRepository
                .getMealsForPeriod(startDate, endDate)
                .first()
                .isEmpty()
        )

        assertEquals(
            listOf(outside),
            mealPlanRepository.getMealsForDate(outside.date).first()
        )
    }

    @Test
    fun replacingMealPlansRejectsMealsOutsidePeriod() = runBlocking {
        val recipe = Recipe(
            id = "test-recipe",
            name = "Test Recipe",
            description = "Test",
            mealType = MealType.LUNCH,
            servings = 2,
            ingredients = emptyList(),
            instructions = emptyList(),
            preparationMinutes = 20
        )

        recipeRepository.saveRecipe(recipe)

        val startDate = LocalDate.of(2026, 10, 30)
        val endDate = LocalDate.of(2026, 11, 14)

        val existing = MealPlan(
            LocalDate.of(2026, 10, 31),
            MealType.LUNCH,
            recipe.id
        )

        mealPlanRepository.saveMealPlan(existing)

        val outsidePlan = MealPlan(
            LocalDate.of(2026, 11, 15),
            MealType.DINNER,
            recipe.id
        )

        try {
            mealPlanRepository.replaceMealPlansForPeriod(
                startDate,
                endDate,
                listOf(outsidePlan)
            )

            throw AssertionError("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected validation failure.
        }

        assertEquals(
            listOf(existing),
            mealPlanRepository
                .getMealsForPeriod(startDate, endDate)
                .first()
        )
    }

    @Test
    fun plannedMealRetrievesItsRecipeDetails() = runBlocking {
        val recipe = Recipe(
            id = "tinolang-manok",
            name = "Tinolang Manok",
            description = "Filipino chicken and vegetable soup",
            mealType = MealType.LUNCH,
            servings = 4,
            ingredients = emptyList(),
            instructions = listOf("Prepare ingredients", "Simmer chicken"),
            preparationMinutes = 40
        )

        recipeRepository.saveRecipe(recipe)

        val date = LocalDate.of(2026, 10, 31)

        mealPlanRepository.saveMealPlan(
            MealPlan(
                date = date,
                mealType = MealType.LUNCH,
                recipeId = recipe.id
            )
        )

        val result = mealPlanRepository
            .getPlannedMealsForPeriod(date, date)
            .first()

        assertEquals(1, result.size)

        val plannedMeal = result.first()

        assertEquals(date, plannedMeal.date)
        assertEquals(MealType.LUNCH, plannedMeal.mealType)
        assertEquals("tinolang-manok", plannedMeal.recipeId)
        assertEquals("Tinolang Manok", plannedMeal.recipeName)
        assertEquals(
            "Filipino chicken and vegetable soup",
            plannedMeal.description
        )
        assertEquals(40, plannedMeal.preparationMinutes)
    }

    @Test
    fun plannedMealsAreReturnedInChronologicalOrder() = runBlocking {
        val breakfast = Recipe(
            id = "arroz-caldo",
            name = "Arroz Caldo",
            description = "Filipino rice porridge",
            mealType = MealType.BREAKFAST,
            servings = 4,
            ingredients = emptyList(),
            instructions = emptyList(),
            preparationMinutes = 30
        )

        val lunch = Recipe(
            id = "tinolang-manok",
            name = "Tinolang Manok",
            description = "Filipino chicken soup",
            mealType = MealType.LUNCH,
            servings = 4,
            ingredients = emptyList(),
            instructions = emptyList(),
            preparationMinutes = 40
        )

        val dinner = Recipe(
            id = "sinigang-na-isda",
            name = "Sinigang na Isda",
            description = "Filipino sour fish soup",
            mealType = MealType.DINNER,
            servings = 4,
            ingredients = emptyList(),
            instructions = emptyList(),
            preparationMinutes = 35
        )

        recipeRepository.saveRecipe(breakfast)
        recipeRepository.saveRecipe(lunch)
        recipeRepository.saveRecipe(dinner)

        val firstDate = LocalDate.of(2026, 10, 30)
        val secondDate = firstDate.plusDays(1)

        mealPlanRepository.saveMealPlans(
            listOf(
                MealPlan(secondDate, MealType.DINNER, dinner.id),
                MealPlan(firstDate, MealType.LUNCH, lunch.id),
                MealPlan(secondDate, MealType.BREAKFAST, breakfast.id),
                MealPlan(firstDate, MealType.BREAKFAST, breakfast.id),
                MealPlan(secondDate, MealType.LUNCH, lunch.id),
                MealPlan(firstDate, MealType.DINNER, dinner.id)
            )
        )

        val result = mealPlanRepository
            .getPlannedMealsForPeriod(firstDate, secondDate)
            .first()

        assertEquals(6, result.size)

        assertEquals(
            listOf(
                firstDate,
                firstDate,
                firstDate,
                secondDate,
                secondDate,
                secondDate
            ),
            result.map { it.date }
        )

        assertEquals(
            listOf(
                MealType.BREAKFAST,
                MealType.LUNCH,
                MealType.DINNER,
                MealType.BREAKFAST,
                MealType.LUNCH,
                MealType.DINNER
            ),
            result.map { it.mealType }
        )
    }

    @Test
    fun plannedMealsReturnsEmptyListWhenPeriodHasNoMeals() = runBlocking {
        val startDate = LocalDate.of(2026, 10, 30)
        val endDate = LocalDate.of(2026, 11, 14)

        val result = mealPlanRepository
            .getPlannedMealsForPeriod(startDate, endDate)
            .first()

        assertTrue(result.isEmpty())
    }
}