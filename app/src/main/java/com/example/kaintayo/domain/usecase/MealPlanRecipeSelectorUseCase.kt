package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.MealPlan
import com.example.kaintayo.domain.model.MealPlanSlot
import com.example.kaintayo.domain.model.MealType
import com.example.kaintayo.domain.model.Recipe

object MealPlanRecipeSelectorUseCase {

    private const val RECENT_RECIPE_LIMIT = 3

    fun select(
        slots: List<MealPlanSlot>,
        recipes: List<Recipe>
    ): List<MealPlan>? {

        // Recipe IDs must be unique.
        if (recipes.map { it.id }.distinct().size != recipes.size) {
            return null
        }

        // Each date can only have one slot of each meal type.
        if (slots.distinctBy { it.date to it.mealType }.size != slots.size) {
            return null
        }

        val recipesByMealType = recipes
            .groupBy { it.mealType }
            .mapValues { (_, mealRecipes) ->
                mealRecipes.sortedBy { it.id }
            }

        val usageCounts = mutableMapOf<String, Int>()

        val recentRecipes = MealType.values().associateWith {
            mutableListOf<String>()
        }

        val selectedMeals = mutableListOf<MealPlan>()

        for (slot in slots) {
            val recent = recentRecipes[slot.mealType] ?: return null

            val candidates = recipesByMealType[slot.mealType]
                .orEmpty()
                .filter { it.id !in recent }

            // Never violate the recipe variety rule.
            if (candidates.isEmpty()) {
                return null
            }

            // Prefer the least-used eligible recipe.
            // Recipe ID provides a deterministic tie-breaker.
            val selectedRecipe = candidates.minWithOrNull(
                compareBy<Recipe> { usageCounts[it.id] ?: 0 }
                    .thenBy { it.id }
            ) ?: return null

            selectedMeals.add(
                MealPlan(
                    date = slot.date,
                    mealType = slot.mealType,
                    recipeId = selectedRecipe.id
                )
            )

            usageCounts[selectedRecipe.id] =
                (usageCounts[selectedRecipe.id] ?: 0) + 1

            recent.add(selectedRecipe.id)

            if (recent.size > RECENT_RECIPE_LIMIT) {
                recent.removeAt(0)
            }
        }

        return selectedMeals
    }
}
