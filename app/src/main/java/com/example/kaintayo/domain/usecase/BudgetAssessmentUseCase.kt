
package com.example.kaintayo.domain.usecase

import com.example.kaintayo.domain.model.BudgetAssessment
import com.example.kaintayo.domain.model.BudgetStatus
import kotlin.math.roundToLong

object BudgetAssessmentUseCase {

    fun assess(
        budgetCentavos: Long,
        estimatedCostCentavos: Double
    ): BudgetAssessment? {

        if (budgetCentavos < 0L) {
            return null
        }

        if (!estimatedCostCentavos.isFinite() ||
            estimatedCostCentavos < 0.0 ||
            estimatedCostCentavos > Long.MAX_VALUE.toDouble()
        ) {
            return null
        }

        val roundedCost = estimatedCostCentavos.roundToLong()

        val remaining = budgetCentavos - roundedCost

        val status = when {
            remaining > 0L -> BudgetStatus.WITHIN_BUDGET
            remaining == 0L -> BudgetStatus.EXACT_BUDGET
            else -> BudgetStatus.OVER_BUDGET
        }

        return BudgetAssessment(
            budgetCentavos = budgetCentavos,
            estimatedCostCentavos = roundedCost,
            remainingBudgetCentavos = remaining,
            status = status
        )
    }
}
