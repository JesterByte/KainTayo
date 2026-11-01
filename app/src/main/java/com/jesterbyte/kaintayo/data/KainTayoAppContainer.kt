
package com.jesterbyte.kaintayo.data

import android.content.Context
import com.jesterbyte.kaintayo.data.catalog.FilipinoCatalogSeeder
import com.jesterbyte.kaintayo.data.local.KainTayoDatabaseProvider
import com.jesterbyte.kaintayo.data.local.KainTayoPreferences
import com.jesterbyte.kaintayo.data.repository.IngredientRepositoryImpl
import com.jesterbyte.kaintayo.data.repository.MealPlanRepositoryImpl
import com.jesterbyte.kaintayo.data.repository.RecipeRepositoryImpl
import com.jesterbyte.kaintayo.domain.usecase.GeneratePaydayMealPlanUseCase
import com.jesterbyte.kaintayo.domain.usecase.GetPaydayGroceryListUseCase

class KainTayoAppContainer(context: Context) {

    val preferences = KainTayoPreferences(context)

    private val database = KainTayoDatabaseProvider
        .getDatabase(context.applicationContext)

    val ingredientRepository = IngredientRepositoryImpl(
        database.ingredientDao()
    )

    val recipeRepository = RecipeRepositoryImpl(
        database.recipeDao()
    )

    val mealPlanRepository = MealPlanRepositoryImpl(
        database.mealPlanDao()
    )

    val generatePaydayMealPlanUseCase = GeneratePaydayMealPlanUseCase(
        ingredientRepository = ingredientRepository,
        recipeRepository = recipeRepository,
        mealPlanRepository = mealPlanRepository
    )

    val getPaydayGroceryListUseCase =
        GetPaydayGroceryListUseCase(
            mealPlanRepository = mealPlanRepository,
            recipeRepository = recipeRepository,
            ingredientRepository = ingredientRepository
        )

    suspend fun seedCatalog() {
        FilipinoCatalogSeeder(
            database.ingredientDao(),
            database.recipeDao()
        ).seed()
    }
}
