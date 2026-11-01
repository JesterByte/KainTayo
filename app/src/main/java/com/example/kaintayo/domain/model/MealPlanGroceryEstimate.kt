
package com.example.kaintayo.domain.model

data class MealPlanGroceryEstimate(
    // Cost of the quantities actually consumed by recipes.
    val groceryCost: GroceryCostEstimate,

    // Cost of buying complete packages, bottles, and kilograms.
    val purchaseEstimate: GroceryPurchaseEstimate,

    // Budget comparison based on the actual purchase estimate.
    val budgetAssessment: BudgetAssessment
)
