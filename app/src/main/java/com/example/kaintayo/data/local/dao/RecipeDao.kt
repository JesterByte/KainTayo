package com.example.kaintayo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.kaintayo.data.local.entity.RecipeEntity
import com.example.kaintayo.data.local.entity.RecipeIngredientEntity
import com.example.kaintayo.data.local.entity.RecipeWithIngredients
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    @Transaction
    @Query("SELECT * FROM recipes ORDER BY name ASC")
    fun getAllRecipes(): Flow<List<RecipeWithIngredients>>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: String): RecipeWithIngredients?

    @Upsert
    suspend fun insertRecipe(recipe: RecipeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRecipeIfAbsent(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(
        ingredients: List<RecipeIngredientEntity>
    )

    @Delete
    suspend fun deleteRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteRecipeIngredients(recipeId: String)

    @Transaction
    suspend fun insertRecipeWithIngredients(
        recipe: RecipeEntity,
        ingredients: List<RecipeIngredientEntity>
    ) {
        insertRecipe(recipe)

        deleteRecipeIngredients(recipe.id)

        if (ingredients.isNotEmpty()) {
            insertRecipeIngredients(ingredients)
        }
    }

    @Transaction
    suspend fun insertRecipeWithIngredientsIfAbsent(
        recipe: RecipeEntity,
        ingredients: List<RecipeIngredientEntity>
    ) {
        val insertedId = insertRecipeIfAbsent(recipe)

        if (insertedId != -1L && ingredients.isNotEmpty()) {
            insertRecipeIngredients(ingredients)
        }
    }
}