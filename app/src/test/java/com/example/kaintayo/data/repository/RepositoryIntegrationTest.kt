package com.example.kaintayo.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
import com.example.kaintayo.data.catalog.FilipinoCatalogSeeder
import com.example.kaintayo.data.catalog.FilipinoIngredientCatalog
import com.example.kaintayo.data.catalog.FilipinoRecipeCatalog

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
}