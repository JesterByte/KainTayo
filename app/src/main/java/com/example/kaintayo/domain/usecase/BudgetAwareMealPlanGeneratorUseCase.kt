package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MealPlan
import com.example.kaintayo.domain.model.MealPlanSlot
import com.example.kaintayo.domain.model.MealType
import com.example.kaintayo.domain.model.PantryStock
import com.example.kaintayo.domain.model.Recipe
import com.example.kaintayo.domain.model.RecipeIngredient
import kotlin.math.roundToLong

data class BudgetAwareMealPlanResult(
    val mealPlans: List<MealPlan>,
    val estimatedPurchaseCostCentavos: Long,
    val budgetCentavos: Long
) {
    val fitsBudget: Boolean
        get() = estimatedPurchaseCostCentavos <= budgetCentavos

    val remainingBudgetCentavos: Long
        get() = budgetCentavos - estimatedPurchaseCostCentavos
}

object BudgetAwareMealPlanGeneratorUseCase {

    private const val RECENT_RECIPE_LIMIT = 3
    private const val BEAM_WIDTH = 256

    private data class Candidate(
        val meals: List<MealPlan>,
        val recipeIds: List<String>,
        val recentRecipeIds: Map<MealType, List<String>>,
        val accumulatedIngredients: List<RecipeIngredient>,
        val estimatedPurchaseCostCentavos: Double
    )

    fun generate(
        slots: List<MealPlanSlot>,
        recipes: List<Recipe>,
        targetServings: Int,
        ingredientCatalog: List<Ingredient>,
        budgetCentavos: Long,
        pantryStock: List<PantryStock> = emptyList()
    ): BudgetAwareMealPlanResult? {

        if (
            slots.isEmpty() ||
            recipes.isEmpty() ||
            targetServings <= 0 ||
            budgetCentavos < 0
        ) {
            return null
        }

        if (recipes.map { it.id }.distinct().size != recipes.size) {
            return null
        }

        if (
            slots.distinctBy { it.date to it.mealType }.size != slots.size
        ) {
            return null
        }

        val sortedSlots = slots.sortedWith(
            compareBy<MealPlanSlot> { it.date }
                .thenBy { it.mealType.ordinal }
        )

        if (ingredientCatalog.map { it.id }.distinct().size !=
            ingredientCatalog.size
        ) {
            return null
        }

        // Scale every recipe for the target household size.
        // Validate each recipe before beginning the search.
        val scaledIngredientsByRecipe =
            mutableMapOf<String, List<RecipeIngredient>>()

        for (recipe in recipes) {
            val scaledRecipe = RecipeScalingUseCase.scaleRecipe(
                recipe = recipe,
                targetServings = targetServings
            ) ?: return null

            val consolidated = IngredientConsolidationUseCase.consolidate(
                scaledRecipe.ingredients
            ) ?: return null

            // Ensure every recipe can be priced using the current catalog.
            GroceryPurchaseCalculatorUseCase.calculate(
                consolidatedIngredients = consolidated,
                ingredientCatalog = ingredientCatalog,
                pantryStock = pantryStock
            ) ?: return null

            scaledIngredientsByRecipe[recipe.id] =
                scaledRecipe.ingredients
        }

        val recipesByMealType = recipes
            .groupBy { it.mealType }
            .mapValues { (_, items) -> items.sortedBy { it.id } }

        var beam = listOf(
            Candidate(
                meals = emptyList(),
                recipeIds = emptyList(),
                recentRecipeIds = emptyMap(),
                accumulatedIngredients = emptyList(),
                estimatedPurchaseCostCentavos = 0.0
            )
        )

        for (slot in sortedSlots) {
            val eligibleRecipes =
                recipesByMealType[slot.mealType].orEmpty()

            if (eligibleRecipes.isEmpty()) {
                return null
            }

            val nextCandidates = mutableListOf<Candidate>()

            for (candidate in beam) {
                val recentIds =
                    candidate.recentRecipeIds[slot.mealType].orEmpty()

                for (recipe in eligibleRecipes) {
                    // Enforce the cooldown separately for each meal type.
                    if (recipe.id in recentIds) {
                        continue
                    }

                    val recipeIngredients =
                        scaledIngredientsByRecipe[recipe.id]
                            ?: return null

                    val accumulatedIngredients =
                        candidate.accumulatedIngredients +
                                recipeIngredients

                    // Consolidate all ingredients accumulated across
                    // breakfast, lunch, and dinner.
                    val consolidated =
                        IngredientConsolidationUseCase.consolidate(
                            accumulatedIngredients
                        ) ?: continue

                    // Calculate whole-package purchase cost for the
                    // entire partial meal plan, not recipe by recipe.
                    val purchaseEstimate =
                        GroceryPurchaseCalculatorUseCase.calculate(
                            consolidatedIngredients = consolidated,
                            ingredientCatalog = ingredientCatalog,
                            pantryStock = pantryStock
                        ) ?: continue

                    val newCost =
                        purchaseEstimate.totalCostCentavos

                    if (!newCost.isFinite() || newCost < 0.0) {
                        continue
                    }

                    val newRecentIds =
                        (recentIds + recipe.id).takeLast(
                            RECENT_RECIPE_LIMIT
                        )

                    val newRecentMap =
                        candidate.recentRecipeIds +
                                (slot.mealType to newRecentIds)

                    nextCandidates.add(
                        Candidate(
                            meals = candidate.meals + MealPlan(
                                date = slot.date,
                                mealType = slot.mealType,
                                recipeId = recipe.id
                            ),
                            recipeIds = candidate.recipeIds + recipe.id,
                            recentRecipeIds = newRecentMap,
                            accumulatedIngredients =
                                accumulatedIngredients,
                            estimatedPurchaseCostCentavos = newCost
                        )
                    )
                }
            }

            if (nextCandidates.isEmpty()) {
                return null
            }

            // Keep the most promising partial plans.
            // Recipe IDs provide deterministic tie-breaking.
            beam = nextCandidates
                .sortedWith(
                    compareBy<Candidate> {
                        it.estimatedPurchaseCostCentavos
                    }.thenBy {
                        it.recipeIds.joinToString("|")
                    }
                )
                .take(BEAM_WIDTH)
        }

        val bestCandidate = beam.firstOrNull() ?: return null
        val finalCost = bestCandidate.estimatedPurchaseCostCentavos

        if (
            !finalCost.isFinite() ||
            finalCost > Long.MAX_VALUE.toDouble()
        ) {
            return null
        }

        val roundedCost = finalCost.roundToLong()
        val fitsBudget = roundedCost <= budgetCentavos

        return BudgetAwareMealPlanResult(
            // Never approve or return an over-budget meal plan.
            mealPlans = if (fitsBudget) {
                bestCandidate.meals
            } else {
                emptyList()
            },
            estimatedPurchaseCostCentavos = roundedCost,
            budgetCentavos = budgetCentavos
        )
    }
}
