package com.example.kaintayo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.kaintayo.data.local.entity.PantryStockEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryStockDao {

    @Query("SELECT * FROM pantry_stock ORDER BY ingredientId ASC")
    fun getAllPantryStock(): Flow<List<PantryStockEntity>>

    @Query("SELECT * FROM pantry_stock WHERE ingredientId = :ingredientId")
    suspend fun getPantryStock(
        ingredientId: String
    ): PantryStockEntity?

    @Upsert
    suspend fun savePantryStock(stock: PantryStockEntity)

    @Delete
    suspend fun deletePantryStock(stock: PantryStockEntity)

    @Query("DELETE FROM pantry_stock")
    suspend fun clearPantryStock()
}