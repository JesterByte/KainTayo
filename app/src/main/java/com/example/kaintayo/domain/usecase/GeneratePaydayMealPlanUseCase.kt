package com.example.kaintayo.domain.usecase

import com.example.kaintayo.core.time.PaydayCycle
import com.example.kaintayo.core.time.PaydayCycleCalculator
import com.example.kaintayo.domain.model.MealPlan
import com.example.kaintayo.domain.repository.IngredientRepository
import com.example.kaintayo.domain.repository.MealPlanRepository
import com.example.kaintayo.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate

enum class MealPlanGenerationStatus {
    GENERATED,
    OVER_BUDGET,
    CANNOT_GENERATE
}

data class PaydayMealPlanGenerationResult(
    val cycle: PaydayCycle,
    val status: MealPlanGenerationStatus,
    val mealPlans: List<MealPlan>,
    val estimatedPurchaseCostCentavos: Long?,
    val budgetCentavos: Long
)

class GeneratePaydayMealPlanUseCase(
    private val ingredientRepository: IngredientRepository,
    private val recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository
) {

    suspend operator fun invoke(
        today: LocalDate,
        targetServings: Int,
        budgetCentavos: Long
    ): PaydayMealPlanGenerationResult? {

        if (targetServings <= 0 || budgetCentavos < 0) {
            return null
        }

        val cycle = PaydayCycleCalculator.getCurrentCycle(today)

        val slots = MealPlanScheduleGeneratorUseCase.generate(
            startDate = cycle.startDate,
            endDate = cycle.endDate
        ) ?: return null

        val ingredients = ingredientRepository
            .getAllIngredients()
            .first()

        val recipes = recipeRepository
            .getAllRecipes()
            .first()

        val optimizedPlan = BudgetAwareMealPlanGeneratorUseCase.generate(
            slots = slots,
            recipes = recipes,
            targetServings = targetServings,
            ingredientCatalog = ingredients,
            budgetCentavos = budgetCentavos
        )

        if (optimizedPlan == null) {
            return PaydayMealPlanGenerationResult(
                cycle = cycle,
                status = MealPlanGenerationStatus.CANNOT_GENERATE,
                mealPlans = emptyList(),
                estimatedPurchaseCostCentavos = null,
                budgetCentavos = budgetCentavos
            )
        }

        if (!optimizedPlan.fitsBudget) {
            return PaydayMealPlanGenerationResult(
                cycle = cycle,
                status = MealPlanGenerationStatus.OVER_BUDGET,
                mealPlans = emptyList(),
                estimatedPurchaseCostCentavos =
                    optimizedPlan.estimatedPurchaseCostCentavos,
                budgetCentavos = budgetCentavos
            )
        }

        mealPlanRepository.replaceMealPlansForPeriod(
            startDate = cycle.startDate,
            endDate = cycle.endDate,
            mealPlans = optimizedPlan.mealPlans
        )

        return PaydayMealPlanGenerationResult(
            cycle = cycle,
            status = MealPlanGenerationStatus.GENERATED,
            mealPlans = optimizedPlan.mealPlans,
            estimatedPurchaseCostCentavos =
                optimizedPlan.estimatedPurchaseCostCentavos,
            budgetCentavos = budgetCentavos
        )
    }
}