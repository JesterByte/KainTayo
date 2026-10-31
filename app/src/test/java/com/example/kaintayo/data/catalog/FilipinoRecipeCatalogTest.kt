package com.example.kaintayo.data.catalog

import com.example.kaintayo.domain.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilipinoRecipeCatalogTest {

    @Test
    fun catalogContainsExactly30Recipes() {
        assertEquals(30, FilipinoRecipeCatalog.recipes.size)
    }

    @Test
    fun catalogContainsTenRecipesForEachMealType() {
        val recipes = FilipinoRecipeCatalog.recipes

        assertEquals(
            10,
            recipes.count { it.mealType == MealType.BREAKFAST }
        )

        assertEquals(
            10,
            recipes.count { it.mealType == MealType.LUNCH }
        )

        assertEquals(
            10,
            recipes.count { it.mealType == MealType.DINNER }
        )
    }

    @Test
    fun allRecipeIdsAreUnique() {
        val ids = FilipinoRecipeCatalog.recipes.map { it.id }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun everyRecipeHasValidServingSizeAndIngredients() {
        assertTrue(
            FilipinoRecipeCatalog.recipes.all { recipe ->
                recipe.servings > 0 &&
                        recipe.ingredients.isNotEmpty() &&
                        recipe.ingredients.all { it.quantity > 0 }
            }
        )
    }

    @Test
    fun everyRecipeUsesExistingCatalogIngredients() {
        val ingredientIds =
            FilipinoIngredientCatalog.ingredients.map { it.id }.toSet()

        val unknownIngredients = FilipinoRecipeCatalog.recipes
            .flatMap { it.ingredients }
            .map { it.ingredientId }
            .filter { it !in ingredientIds }
            .distinct()

        assertTrue(
            "Unknown ingredient IDs: $unknownIngredients",
            unknownIngredients.isEmpty()
        )
    }

    @Test
    fun recipeIngredientUnitsMatchCatalogDefaults() {
        val ingredientsById =
            FilipinoIngredientCatalog.ingredients.associateBy { it.id }

        FilipinoRecipeCatalog.recipes.forEach { recipe ->
            recipe.ingredients.forEach { recipeIngredient ->
                val catalogIngredient =
                    ingredientsById[recipeIngredient.ingredientId]

                assertEquals(
                    "Unit mismatch for ${recipeIngredient.ingredientId} in ${recipe.name}",
                    catalogIngredient?.defaultUnit,
                    recipeIngredient.unit
                )
            }
        }
    }
}
