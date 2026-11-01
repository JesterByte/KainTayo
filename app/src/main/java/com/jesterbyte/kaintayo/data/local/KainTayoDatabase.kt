
package com.jesterbyte.kaintayo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jesterbyte.kaintayo.data.local.dao.IngredientDao
import com.jesterbyte.kaintayo.data.local.dao.MealPlanDao
import com.jesterbyte.kaintayo.data.local.dao.RecipeDao
import com.jesterbyte.kaintayo.data.local.entity.IngredientEntity
import com.jesterbyte.kaintayo.data.local.entity.MealPlanEntity
import com.jesterbyte.kaintayo.data.local.entity.RecipeEntity
import com.jesterbyte.kaintayo.data.local.entity.RecipeIngredientEntity

@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        RecipeIngredientEntity::class,
        MealPlanEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KainTayoDatabase : RoomDatabase() {

    abstract fun ingredientDao(): IngredientDao

    abstract fun recipeDao(): RecipeDao

    abstract fun mealPlanDao(): MealPlanDao
}
