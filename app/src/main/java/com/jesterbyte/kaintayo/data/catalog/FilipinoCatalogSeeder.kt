package com.jesterbyte.kaintayo.data.catalog

import com.jesterbyte.kaintayo.data.local.dao.IngredientDao
import com.jesterbyte.kaintayo.data.local.dao.RecipeDao
import com.jesterbyte.kaintayo.data.local.entity.IngredientEntity
import com.jesterbyte.kaintayo.data.local.entity.RecipeEntity
import com.jesterbyte.kaintayo.data.local.entity.RecipeIngredientEntity

class FilipinoCatalogSeeder(
    private val ingredientDao: IngredientDao,
    private val recipeDao: RecipeDao
) {

    suspend fun seed() {
        seedIngredients()
        seedRecipes()
    }

    private suspend fun seedIngredients() {
        FilipinoIngredientCatalog.ingredients.forEach { ingredient ->
            ingredientDao.insertIngredientIfAbsent(
                IngredientEntity(
                    id = ingredient.id,
                    name = ingredient.name,
                    defaultUnit = ingredient.defaultUnit,
                    estimatedPriceCentavos = ingredient.estimatedPriceCentavos,
                    priceQuantity = ingredient.priceQuantity,
                    priceUnit = ingredient.priceUnit
                )
            )
        }
    }

    private suspend fun seedRecipes() {
        FilipinoRecipeCatalog.recipes.forEach { recipe ->
            val recipeEntity = RecipeEntity(
                id = recipe.id,
                name = recipe.name,
                description = recipe.description,
                mealType = recipe.mealType,
                servings = recipe.servings,
                instructions = recipe.instructions.joinToString("\n"),
                preparationMinutes = recipe.preparationMinutes
            )

            val ingredientEntities = recipe.ingredients.map { ingredient ->
                RecipeIngredientEntity(
                    recipeId = recipe.id,
                    ingredientId = ingredient.ingredientId,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit
                )
            }

            recipeDao.insertRecipeWithIngredientsIfAbsent(
                recipeEntity,
                ingredientEntities
            )
        }
    }
}