package com.example.kaintayo.domain.repository

import com.example.kaintayo.domain.model.PantryStock
import kotlinx.coroutines.flow.Flow

interface PantryStockRepository {

    fun getAllPantryStock(): Flow<List<PantryStock>>

    suspend fun getPantryStock(ingredientId: String): PantryStock?

    suspend fun savePantryStock(stock: PantryStock)

    suspend fun deletePantryStock(ingredientId: String)

    suspend fun clearPantryStock()
}