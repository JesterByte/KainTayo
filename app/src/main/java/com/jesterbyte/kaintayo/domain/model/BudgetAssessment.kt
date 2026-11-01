
package com.jesterbyte.kaintayo.domain.model

enum class BudgetStatus {
    WITHIN_BUDGET,
    EXACT_BUDGET,
    OVER_BUDGET
}

data class BudgetAssessment(
    val budgetCentavos: Long,
    val estimatedCostCentavos: Long,
    val remainingBudgetCentavos: Long,
    val status: BudgetStatus
)
