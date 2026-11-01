
package com.jesterbyte.kaintayo.domain.usecase

import com.jesterbyte.kaintayo.domain.model.BudgetStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BudgetAssessmentUseCaseTest {

    @Test
    fun identifiesWithinBudget() {
        val result = BudgetAssessmentUseCase.assess(
            budgetCentavos = 1000000L,
            estimatedCostCentavos = 750000.0
        )!!

        assertEquals(250000L, result.remainingBudgetCentavos)
        assertEquals(BudgetStatus.WITHIN_BUDGET, result.status)
    }

    @Test
    fun identifiesExactBudget() {
        val result = BudgetAssessmentUseCase.assess(
            budgetCentavos = 1000000L,
            estimatedCostCentavos = 1000000.0
        )!!

        assertEquals(0L, result.remainingBudgetCentavos)
        assertEquals(BudgetStatus.EXACT_BUDGET, result.status)
    }

    @Test
    fun identifiesOverBudget() {
        val result = BudgetAssessmentUseCase.assess(
            budgetCentavos = 1000000L,
            estimatedCostCentavos = 1200000.0
        )!!

        assertEquals(-200000L, result.remainingBudgetCentavos)
        assertEquals(BudgetStatus.OVER_BUDGET, result.status)
    }

    @Test
    fun roundsEstimatedCostToNearestCentavo() {
        val result = BudgetAssessmentUseCase.assess(
            budgetCentavos = 100L,
            estimatedCostCentavos = 99.6
        )!!

        assertEquals(100L, result.estimatedCostCentavos)
        assertEquals(BudgetStatus.EXACT_BUDGET, result.status)
    }

    @Test
    fun rejectsNegativeBudget() {
        assertNull(
            BudgetAssessmentUseCase.assess(
                budgetCentavos = -100L,
                estimatedCostCentavos = 50.0
            )
        )
    }

    @Test
    fun rejectsNegativeEstimatedCost() {
        assertNull(
            BudgetAssessmentUseCase.assess(
                budgetCentavos = 100L,
                estimatedCostCentavos = -50.0
            )
        )
    }

    @Test
    fun rejectsInvalidEstimatedCost() {
        assertNull(
            BudgetAssessmentUseCase.assess(
                budgetCentavos = 100L,
                estimatedCostCentavos = Double.NaN
            )
        )
    }
}
