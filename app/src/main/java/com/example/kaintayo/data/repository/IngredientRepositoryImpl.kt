package com.example.kaintayo.data.repository

import com.example.kaintayo.data.local.dao.IngredientDao
import com.example.kaintayo.data.local.entity.IngredientEntity
import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IngredientRepositoryImpl(
    private val ingredientDao: IngredientDao
) : IngredientRepository {

    override fun getAllIngredients(): Flow<List<Ingredient>> {
        return ingredientDao.getAllIngredients().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getIngredientById(id: String): Ingredient? {
        return ingredientDao.getIngredientById(id)?.toDomain()
    }

    override suspend fun saveIngredient(ingredient: Ingredient) {
        ingredientDao.insertIngredient(ingredient.toEntity())
    }

    override suspend fun saveIngredients(ingredients: List<Ingredient>) {
        ingredientDao.insertIngredients(
            ingredients.map { it.toEntity() }
        )
    }

    override suspend fun deleteIngredient(id: String) {
        val ingredient = ingredientDao.getIngredientById(id)
            ?: return

        ingredientDao.deleteIngredient(ingredient)
    }

    private fun IngredientEntity.toDomain() = Ingredient(
        id = id,
        name = name,
        defaultUnit = defaultUnit,
        estimatedPriceCentavos = estimatedPriceCentavos,
        priceQuantity = priceQuantity,
        priceUnit = priceUnit
    )

    private fun Ingredient.toEntity() = IngredientEntity(
        id = id,
        name = name,
        defaultUnit = defaultUnit,
        estimatedPriceCentavos = estimatedPriceCentavos,
        priceQuantity = priceQuantity,
        priceUnit = priceUnit
    )
}