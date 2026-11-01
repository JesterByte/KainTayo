package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.Ingredient
import com.example.kaintayo.domain.model.MealPlan
import com.example.kaintayo.domain.model.MealPlanSlot
import com.example.kaintayo.domain.model.MealType
import com.example.kaintayo.domain.model.Recipe
import kotlin.math.roundToLong

data class BudgetAwareMealPlanResult(
    val mealPlans: List<MealPlan>,
    val minimumEstimatedCostCentavos: Long,
    val budgetCentavos: Long
) {
    val fitsBudget: Boolean
        get() = minimumEstimatedCostCentavos <= budgetCentavos

    val remainingBudgetCentavos: Long
        get() = budgetCentavos - minimumEstimatedCostCentavos
}

object BudgetAwareMealPlanGeneratorUseCase {

    private const val RECENT_RECIPE_LIMIT = 3

    private data class Candidate(
        val costCentavos: Double,
        val meals: List<MealPlan>,
        val recipeIds: List<String>
    )

    fun generate(
        slots: List<MealPlanSlot>,
        recipes: List<Recipe>,
        targetServings: Int,
        ingredientCatalog: List<Ingredient>,
        budgetCentavos: Long
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

        // Calculate the cost of every recipe for the target household size.
        val recipeCosts = recipes.associate { recipe ->
            val estimate = RecipeGroceryCostUseCase.calculate(
                recipe = recipe,
                targetServings = targetServings,
                ingredientCatalog = ingredientCatalog
            ) ?: return null

            recipe.id to estimate.totalCostCentavos
        }

        val recipesByMealType = recipes
            .groupBy { it.mealType }
            .mapValues { (_, items) -> items.sortedBy { it.id } }

        val selectedMeals = mutableListOf<MealPlan>()
        var totalMinimumCost = 0.0

        // Each meal type can be optimized independently because its
        // recipe-repeat restriction only applies to that meal type.
        for (mealType in MealType.values()) {

            val mealSlots = sortedSlots.filter {
                it.mealType == mealType
            }

            if (mealSlots.isEmpty()) {
                continue
            }

            val candidates = recipesByMealType[mealType].orEmpty()

            if (candidates.isEmpty()) {
                return null
            }

            // State key: the last three recipe IDs selected for this
            // meal type. Future eligibility depends only on this state.
            var states = mapOf(
                emptyList<String>() to Candidate(
                    costCentavos = 0.0,
                    meals = emptyList(),
                    recipeIds = emptyList()
                )
            )

            for (slot in mealSlots) {
                val nextStates = mutableMapOf<List<String>, Candidate>()

                for ((recentIds, previous) in states) {
                    for (recipe in candidates) {

                        // Do not reuse any of the previous three recipes.
                        if (recipe.id in recentIds) {
                            continue
                        }

                        val recipeCost = recipeCosts[recipe.id]
                            ?: return null

                        val newRecipeIds = previous.recipeIds + recipe.id

                        val newCandidate = Candidate(
                            costCentavos =
                                previous.costCentavos + recipeCost,
                            meals = previous.meals + MealPlan(
                                date = slot.date,
                                mealType = slot.mealType,
                                recipeId = recipe.id
                            ),
                            recipeIds = newRecipeIds
                        )

                        val newState = newRecipeIds.takeLast(
                            RECENT_RECIPE_LIMIT
                        )

                        val existing = nextStates[newState]

                        val isBetter = existing == null ||
                                newCandidate.costCentavos <
                                existing.costCentavos ||
                                (
                                        newCandidate.costCentavos ==
                                                existing.costCentavos &&
                                                newCandidate.recipeIds.joinToString("|") <
                                                existing.recipeIds.joinToString("|")
                                        )

                        if (isBetter) {
                            nextStates[newState] = newCandidate
                        }
                    }
                }

                if (nextStates.isEmpty()) {
                    return null
                }

                states = nextStates
            }

            // Find the cheapest complete sequence for this meal type.
            val bestCandidate = states.values.minWithOrNull(
                compareBy<Candidate> { it.costCentavos }
                    .thenBy { it.recipeIds.joinToString("|") }
            ) ?: return null

            selectedMeals.addAll(bestCandidate.meals)
            totalMinimumCost += bestCandidate.costCentavos
        }

        if (
            !totalMinimumCost.isFinite() ||
            totalMinimumCost > Long.MAX_VALUE.toDouble()
        ) {
            return null
        }

        val roundedMinimumCost = totalMinimumCost.roundToLong()
        val fitsBudget = roundedMinimumCost <= budgetCentavos

        return BudgetAwareMealPlanResult(
            // Never return an over-budget plan as an approved plan.
            mealPlans = if (fitsBudget) {
                selectedMeals.sortedWith(
                    compareBy<MealPlan> { it.date }
                        .thenBy { it.mealType.ordinal }
                )
            } else {
                emptyList()
            },
            minimumEstimatedCostCentavos = roundedMinimumCost,
            budgetCentavos = budgetCentavos
        )
    }
}