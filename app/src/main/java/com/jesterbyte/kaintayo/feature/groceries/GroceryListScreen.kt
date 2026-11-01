package com.jesterbyte.kaintayo.feature.groceries

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.NonInteractiveSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.jesterbyte.kaintayo.core.time.PaydayCycleCalculator
import com.jesterbyte.kaintayo.data.KainTayoAppContainer
import com.jesterbyte.kaintayo.domain.model.BudgetStatus
import com.jesterbyte.kaintayo.domain.model.GroceryPurchaseItem
import com.jesterbyte.kaintayo.domain.model.MealPlanGroceryEstimate
import com.jesterbyte.kaintayo.ui.theme.KainAccent
import com.jesterbyte.kaintayo.ui.theme.KainBackground
import com.jesterbyte.kaintayo.ui.theme.KainGreenDark
import com.jesterbyte.kaintayo.ui.theme.KainSurface
import com.jesterbyte.kaintayo.ui.theme.KainTextPrimary
import com.jesterbyte.kaintayo.ui.theme.KainTextSecondary
import kotlinx.coroutines.CancellationException
import java.text.NumberFormat.getCurrencyInstance
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

private val CycleDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

private val StatusWithin = KainAccent
private val StatusExact = Color(0xFFF2B84B)
private val StatusOver = Color(0xFFE5534B)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GroceryListScreen(
    appContainer: KainTayoAppContainer,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val today = remember { LocalDate.now() }
    val cycle = remember(today) { PaydayCycleCalculator.getCurrentCycle(today) }
    val householdSize = remember { appContainer.preferences.getHouseholdSize() }
    val budgetCentavos = remember { appContainer.preferences.getBudgetCentavos() }

    var estimate by remember { mutableStateOf<MealPlanGroceryEstimate?>(null) }
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
                errorMessage = "No grocery estimate is available. Generate a meal plan first."
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            errorMessage = "Unable to calculate the grocery list. Please try again."
        } finally {
            isLoading = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = NonInteractiveSurfaceDefaults.colors(containerColor = KainBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(onClick = onBack) {
                        Text("← Back", fontSize = 14.sp)
                    }
                    Text("🛒", fontSize = 28.sp)
                    Text(
                        text = "Grocery List",
                        color = KainTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${cycle.startDate.format(CycleDateFormatter)} – " +
                                cycle.endDate.format(CycleDateFormatter),
                        color = KainAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (householdSize != null) {
                        Text(
                            text = "Shopping for $householdSize",
                            color = KainTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            when {
                isLoading -> {
                    Text(
                        text = "Calculating your grocery list...",
                        color = KainTextSecondary,
                        fontSize = 16.sp
                    )
                }

                errorMessage != null -> {
                    MessageCard(message = errorMessage!!)
                }

                estimate != null -> {
                    // Summary stays on the left while the list scrolls on the right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        GrocerySummary(
                            estimate = estimate!!,
                            modifier = Modifier.width(340.dp)
                        )
                        GroceryItems(
                            items = estimate!!.purchaseEstimate.items,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GrocerySummary(
    estimate: MealPlanGroceryEstimate,
    modifier: Modifier = Modifier
) {
    val assessment = estimate.budgetAssessment

    val (statusText, statusColor) = when (assessment.status) {
        BudgetStatus.WITHIN_BUDGET -> "Within budget" to StatusWithin
        BudgetStatus.EXACT_BUDGET -> "Exactly on budget" to StatusExact
        BudgetStatus.OVER_BUDGET -> "Over budget" to StatusOver
    }

    val budget = assessment.budgetCentavos.toDouble()
    val cost = assessment.estimatedCostCentavos.toDouble()
    val fraction = if (budget > 0) (cost / budget).toFloat().coerceIn(0f, 1f) else 0f

    val remaining = assessment.remainingBudgetCentavos.toDouble()
    val remainingLabel = if (remaining < 0) "Over by" else "Remaining"

    Column(
        modifier = modifier
            .background(KainSurface, RoundedCornerShape(16.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "ESTIMATED COST",
            color = KainTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        Text(
            text = formatPeso(assessment.estimatedCostCentavos),
            color = KainTextPrimary,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )

        // Budget usage bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(KainBackground, RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .background(statusColor, RoundedCornerShape(50))
            )
        }

        Text(
            text = "of ${formatPeso(assessment.budgetCentavos)} budget",
            color = KainTextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = remainingLabel,
                color = KainTextSecondary,
                fontSize = 14.sp
            )
            Text(
                text = formatPeso(abs(remaining)),
                color = if (remaining < 0) StatusOver else KainTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.18f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun GroceryItems(
    items: List<GroceryPurchaseItem>,
    modifier: Modifier = Modifier
) {
    val sortedItems = remember(items) { items.sortedBy { it.name } }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Ingredients to buy · ${items.size}",
            color = KainTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        if (sortedItems.isEmpty()) {
            MessageCard(message = "Nothing to buy for this cycle.")
        } else {
            // Padding gives focused cards room to scale without being clipped
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedItems, key = { it.name }) { item ->
                    GroceryItemCard(item = item)
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GroceryItemCard(item: GroceryPurchaseItem) {
    val shape = RoundedCornerShape(12.dp)

    // Focusable so the D-pad can scroll through the list
    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        scale = CardDefaults.scale(focusedScale = 1.02f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = KainSurface,
            focusedContainerColor = KainGreenDark
        ),
        border = CardDefaults.border(
            border = Border.None,
            focusedBorder = Border(BorderStroke(2.dp, KainAccent), shape = shape)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    color = KainTextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = "Buy ${item.purchaseCount} × " +
                            "${formatQuantity(item.purchaseQuantity)} " +
                            item.purchaseUnit.name.lowercase(),
                    color = KainAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Need ${formatQuantity(item.requiredQuantity)} " +
                            "${item.requiredUnit.name.lowercase()} · " +
                            "Total ${formatQuantity(item.purchasedQuantity)} " +
                            item.purchaseUnit.name.lowercase(),
                    color = KainTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }

            Text(
                text = formatPeso(item.estimatedCostCentavos),
                color = KainTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MessageCard(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KainSurface, RoundedCornerShape(16.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "🛒", fontSize = 36.sp)
        Text(
            text = message,
            color = KainTextSecondary,
            fontSize = 16.sp
        )
    }
}

private fun formatPeso(centavos: Number): String {
    val pesos = centavos.toDouble() / 100.0
    return getCurrencyInstance(Locale.forLanguageTag("en-PH")).format(pesos)
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