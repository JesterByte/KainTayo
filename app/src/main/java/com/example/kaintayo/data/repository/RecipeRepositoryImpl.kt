package com.example.kaintayo.data.repository

import com.example.kaintayo.data.local.dao.RecipeDao
import com.example.kaintayo.data.local.entity.RecipeEntity
import com.example.kaintayo.data.local.entity.RecipeIngredientEntity
import com.example.kaintayo.data.local.entity.RecipeWithIngredients
import com.example.kaintayo.domain.model.Recipe
import com.example.kaintayo.domain.model.RecipeIngredient
import com.example.kaintayo.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RecipeRepositoryImpl(
    private val recipeDao: RecipeDao
) : RecipeRepository {

    override fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRecipeById(id: String): Recipe? {
        return recipeDao.getRecipeById(id)?.toDomain()
    }

    override suspend fun saveRecipe(recipe: Recipe) {
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

        recipeDao.insertRecipeWithIngredients(
            recipeEntity,
            ingredientEntities
        )
    }

    override suspend fun deleteRecipe(id: String) {
        val recipe = recipeDao.getRecipeById(id)?.recipe
            ?: return

        recipeDao.deleteRecipe(recipe)
    }

    private fun RecipeWithIngredients.toDomain() = Recipe(
        id = recipe.id,
        name = recipe.name,
        description = recipe.description,
        mealType = recipe.mealType,
        servings = recipe.servings,
        ingredients = ingredients.map { ingredient ->
            RecipeIngredient(
                ingredientId = ingredient.ingredientId,
                quantity = ingredient.quantity,
                unit = ingredient.unit
            )
        },
        instructions = recipe.instructions
            .split("\n")
            .filter { it.isNotBlank() },
        preparationMinutes = recipe.preparationMinutes
    )
}