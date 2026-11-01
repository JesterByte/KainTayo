package com.example.kaintayo.data.repository

import com.example.kaintayo.data.local.dao.PantryStockDao
import com.example.kaintayo.data.local.entity.PantryStockEntity
import com.example.kaintayo.domain.model.PantryStock
import com.example.kaintayo.domain.repository.PantryStockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PantryStockRepositoryImpl(
    private val pantryStockDao: PantryStockDao
) : PantryStockRepository {

    override fun getAllPantryStock(): Flow<List<PantryStock>> {
        return pantryStockDao.getAllPantryStock().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getPantryStock(
        ingredientId: String
    ): PantryStock? {
        return pantryStockDao.getPantryStock(ingredientId)?.toDomain()
    }

    override suspend fun savePantryStock(stock: PantryStock) {
        pantryStockDao.savePantryStock(stock.toEntity())
    }

    override suspend fun deletePantryStock(ingredientId: String) {
        val stock = pantryStockDao.getPantryStock(ingredientId)
            ?: return

        pantryStockDao.deletePantryStock(stock)
    }

    override suspend fun clearPantryStock() {
        pantryStockDao.clearPantryStock()
    }

    private fun PantryStockEntity.toDomain() = PantryStock(
        ingredientId = ingredientId,
        quantity = quantity,
        unit = unit
    )

    private fun PantryStock.toEntity() = PantryStockEntity(
        ingredientId = ingredientId,
        quantity = quantity,
        unit = unit
    )
}