package com.jesterbyte.kaintayo.feature.groceries

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.jesterbyte.kaintayo.core.time.PaydayCycleCalculator
import com.jesterbyte.kaintayo.data.KainTayoAppContainer
import com.jesterbyte.kaintayo.domain.model.BudgetStatus
import com.jesterbyte.kaintayo.domain.model.GroceryPurchaseItem
import com.jesterbyte.kaintayo.domain.model.MealPlanGroceryEstimate
import com.jesterbyte.kaintayo.ui.theme.KainAccent
import com.jesterbyte.kaintayo.ui.theme.KainBackground
import com.jesterbyte.kaintayo.ui.theme.KainText
import kotlinx.coroutines.CancellationException
import java.text.NumberFormat.getCurrencyInstance
import java.time.LocalDate
import java.util.Locale

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GroceryListScreen(
    appContainer: KainTayoAppContainer,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    val today = remember { LocalDate.now() }
    val cycle = remember(today) {
        PaydayCycleCalculator.getCurrentCycle(today)
    }

    val householdSize = remember {
        appContainer.preferences.getHouseholdSize()
    }

    val budgetCentavos = remember {
        appContainer.preferences.getBudgetCentavos()
    }

    var estimate by remember {
        mutableStateOf<MealPlanGroceryEstimate?>(null)
    }

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(cycle, householdSize, budgetCentavos) {
        isLoading = true
        errorMessage = null

        if (householdSize == null || budgetCentavos == null) {
            errorMessage = "Set your household size and payday budget first."
            isLoading = false
            return@LaunchedEffect
        }

        try {
            estimate = appContainer.getPaydayGroceryListUseCase.estimate(
                startDate = cycle.startDate,
                endDate = cycle.endDate,
                targetServings = householdSize,
                budgetCentavos = budgetCentavos
            )

            if (estimate == null) {
                errorMessage =
                    "No grocery estimate is available. Generate a meal plan first."
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            errorMessage =
                "Unable to calculate the grocery list. Please try again."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Grocery List",
            color = KainText,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${cycle.startDate} – ${cycle.endDate}",
            color = KainAccent,
            fontSize = 18.sp
        )

        if (householdSize != null) {
            Text(
                text = "Household: $householdSize people",
                color = KainText,
                fontSize = 16.sp
            )
        }

        when {
            isLoading -> {
                Text(
                    text = "Calculating your grocery list...",
                    color = KainText,
                    fontSize = 20.sp
                )
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = KainText,
                    fontSize = 20.sp
                )
            }

            estimate != null -> {
                GrocerySummary(estimate = estimate!!)
                GroceryItems(items = estimate!!.purchaseEstimate.items)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onBack) {
            Text("Back to Dashboard")
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GrocerySummary(
    estimate: MealPlanGroceryEstimate
) {
    val assessment = estimate.budgetAssessment

    val statusText = when (assessment.status) {
        BudgetStatus.WITHIN_BUDGET -> "Within budget"
        BudgetStatus.EXACT_BUDGET -> "Exactly on budget"
        BudgetStatus.OVER_BUDGET -> "Over budget"
    }

    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.colors(
            containerColor = KainBackground
        ),
        border = CardDefaults.border(
            border = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    KainAccent
                )
            )
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Estimated Grocery Cost",
                color = KainText,
                fontSize = 18.sp
            )

            Text(
                text = formatPeso(assessment.estimatedCostCentavos),
                color = KainAccent,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Budget: ${formatPeso(assessment.budgetCentavos)}",
                color = KainText,
                fontSize = 18.sp
            )

            Text(
                text = "Remaining: ${formatPeso(assessment.remainingBudgetCentavos)}",
                color = KainText,
                fontSize = 18.sp
            )

            Text(
                text = statusText,
                color = KainAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun GroceryItems(
    items: List<GroceryPurchaseItem>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Ingredients to Buy (${items.size})",
            color = KainText,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        items.sortedBy { it.name }.forEach { item ->
            GroceryItemCard(item = item)
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GroceryItemCard(
    item: GroceryPurchaseItem
) {
    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.colors(
            containerColor = KainBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    color = KainText,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Required: ${formatQuantity(item.requiredQuantity)} ${item.requiredUnit.name.lowercase()}",
                    color = KainText,
                    fontSize = 15.sp
                )

                Text(
                    text = "Buy: ${item.purchaseCount} × ${formatQuantity(item.purchaseQuantity)} ${item.purchaseUnit.name.lowercase()}",
                    color = KainText,
                    fontSize = 15.sp
                )

                Text(
                    text = "Total purchased: ${formatQuantity(item.purchasedQuantity)} ${item.purchaseUnit.name.lowercase()}",
                    color = KainText,
                    fontSize = 15.sp
                )
            }

            Text(
                text = formatPeso(item.estimatedCostCentavos),
                color = KainAccent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatPeso(centavos: Number): String {
    val pesos = centavos.toDouble() / 100.0
    return getCurrencyInstance(
        Locale.forLanguageTag("en-PH")
    ).format(pesos)
}

private fun formatQuantity(quantity: Double): String {
    return if (quantity % 1.0 == 0.0) {
        quantity.toLong().toString()
    } else {
        String.format(Locale.US, "%.2f", quantity)
            .trimEnd('0')
            .trimEnd('.')
    }
}
