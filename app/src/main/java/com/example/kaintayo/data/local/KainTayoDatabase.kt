package com.example.kaintayo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.kaintayo.data.local.dao.IngredientDao
import com.example.kaintayo.data.local.dao.MealPlanDao
import com.example.kaintayo.data.local.dao.RecipeDao
import com.example.kaintayo.data.local.entity.IngredientEntity
import com.example.kaintayo.data.local.entity.MealPlanEntity
import com.example.kaintayo.data.local.entity.RecipeEntity
import com.example.kaintayo.data.local.entity.RecipeIngredientEntity

@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        MealPlanEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KainTayoDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao

    abstract fun recipeDao(): RecipeDao

    abstract fun mealPlanDao(): MealPlanDao
}