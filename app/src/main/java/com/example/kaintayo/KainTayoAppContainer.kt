package com.example.kaintayo

import android.content.Context
import com.example.kaintayo.data.catalog.FilipinoCatalogSeeder
import com.example.kaintayo.data.local.KainTayoDatabaseProvider
import com.example.kaintayo.data.local.KainTayoPreferences
import com.example.kaintayo.data.repository.IngredientRepositoryImpl
import com.example.kaintayo.data.repository.MealPlanRepositoryImpl
import com.example.kaintayo.data.repository.RecipeRepositoryImpl
import com.example.kaintayo.domain.usecase.GeneratePaydayMealPlanUseCase

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

    val generatePaydayMealPlanUseCase =
        GeneratePaydayMealPlanUseCase(
            ingredientRepository = ingredientRepository,
            recipeRepository = recipeRepository,
            mealPlanRepository = mealPlanRepository
        )

    suspend fun seedCatalog() {
        FilipinoCatalogSeeder(
            database.ingredientDao(),
            database.recipeDao()
        ).seed()
    }
}
