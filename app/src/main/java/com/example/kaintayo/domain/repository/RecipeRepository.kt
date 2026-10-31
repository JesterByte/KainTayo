package com.example.kaintayo.domain.repository

import com.example.kaintayo.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {

    fun getAllRecipes(): Flow<List<Recipe>>

    suspend fun getRecipeById(id: String): Recipe?

    suspend fun saveRecipe(recipe: Recipe)

    suspend fun deleteRecipe(id: String)
}