package com.example.kaintayo.domain.repository

import com.example.kaintayo.domain.model.Ingredient
import kotlinx.coroutines.flow.Flow

interface IngredientRepository {

    fun getAllIngredients(): Flow<List<Ingredient>>

    suspend fun getIngredientById(id: String): Ingredient?

    suspend fun saveIngredient(ingredient: Ingredient)

    suspend fun saveIngredients(ingredients: List<Ingredient>)

    suspend fun deleteIngredient(id: String)
}