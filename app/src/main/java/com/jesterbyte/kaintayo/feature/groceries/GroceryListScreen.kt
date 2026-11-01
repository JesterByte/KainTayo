package com.jesterbyte.kaintayo.feature.groceries

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import java.util.Locale
import kotlin.math.abs

private val StatusWithin = KainAccent
private val StatusExact = Color(0xFFF2B84B)
private val StatusOver = Color(0xFFE5534B)

private val ShortFilipinoMonths = listOf(
    "Ene", "Peb", "Mar", "Abr", "May", "Hun",
    "Hul", "Ago", "Set", "Okt", "Nob", "Dis"
)

private fun formatShortDate(date: LocalDate): String =
    "${ShortFilipinoMonths[date.monthValue - 1]} ${date.dayOfMonth}"

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

    val firstItemFocus = remember { FocusRequester() }

    LaunchedEffect(cycle, householdSize, budgetCentavos) {
        isLoading = true
        errorMessage = null

        if (householdSize == null || budgetCentavos == null) {
            errorMessage = "Gawin muna ang Step 1: I-set ang budget at kung ilan kayong kakain."
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
                errorMessage = "Wala pang listahan. Gumawa muna ng Meal Plan sa Step 1."
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            errorMessage = "Hindi makalkula ang grocery list. Pakisubukan ulit."
        } finally {
            isLoading = false
        }
    }

    // When the list is ready, move the remote onto the first item so ▲ ▼ scrolls right away
    LaunchedEffect(estimate) {
        if (estimate != null) {
            withFrameNanos { } // let the list lay out first
            withFrameNanos { }
            runCatching { firstItemFocus.requestFocus() }
        }
    }

    val currentEstimate = estimate
    val currentError = errorMessage

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = NonInteractiveSurfaceDefaults.colors(containerColor = KainBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        Text("← Bumalik", fontSize = 16.sp)
                    }
                    Text("🛒", fontSize = 30.sp)
                    Column {
                        Text(
                            text = "Step 2: Grocery List",
                            color = KainTextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ito ang bibilhin sa palengke o grocery para sa buong cycle.",
                            color = KainTextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${formatShortDate(cycle.startDate)} – " +
                                formatShortDate(cycle.endDate),
                        color = KainAccent,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (householdSize != null) {
                        Text(
                            text = "Para sa $householdSize tao",
                            color = KainTextSecondary,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            when {
                isLoading -> {
                    Text(
                        text = "Kinakalkula ang grocery list... sandali lang po.",
                        color = KainTextSecondary,
                        fontSize = 18.sp
                    )
                }

                currentError != null -> {
                    MessageCard(
                        message = currentError,
                        hint = "Pindutin ang ← Bumalik para bumalik sa Dashboard."
                    )
                }

                currentEstimate != null -> {
                    // Summary stays on the left while the list scrolls on the right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        GrocerySummary(
                            estimate = currentEstimate,
                            modifier = Modifier.width(330.dp)
                        )
                        GroceryItems(
                            items = currentEstimate.purchaseEstimate.items,
                            firstItemFocus = firstItemFocus,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }

                    Text(
                        text = "Tapos na dito? Pindutin ang ← Bumalik, tapos piliin ang Step 3: Meal Plan.",
                        color = KainTextSecondary,
                        fontSize = 15.sp
                    )
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
        BudgetStatus.WITHIN_BUDGET -> "✓ Pasok sa budget" to StatusWithin
        BudgetStatus.EXACT_BUDGET -> "Eksakto sa budget" to StatusExact
        BudgetStatus.OVER_BUDGET -> "⚠ Lampas sa budget" to StatusOver
    }

    val budget = assessment.budgetCentavos.toDouble()
    val cost = assessment.estimatedCostCentavos.toDouble()
    val fraction = if (budget > 0) (cost / budget).toFloat().coerceIn(0f, 1f) else 0f

    val remaining = assessment.remainingBudgetCentavos.toDouble()
    val remainingLabel = if (remaining < 0) "Sobra ng" else "Natitira"

    Column(
        modifier = modifier
            .background(KainSurface, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "TANTYANG GASTOS",
            color = KainTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        Text(
            text = formatPeso(assessment.estimatedCostCentavos),
            color = KainTextPrimary,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        // Budget usage bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
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
            text = "mula sa ${formatPeso(assessment.budgetCentavos)} na budget",
            color = KainTextSecondary,
            fontSize = 16.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = remainingLabel,
                color = KainTextSecondary,
                fontSize = 17.sp
            )
            Text(
                text = formatPeso(abs(remaining)),
                color = if (remaining < 0) StatusOver else KainTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(statusColor.copy(alpha = 0.18f), RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (assessment.status == BudgetStatus.OVER_BUDGET) {
            Text(
                text = "Pwedeng dagdagan ang budget sa Step 1.",
                color = KainTextSecondary,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun GroceryItems(
    items: List<GroceryPurchaseItem>,
    firstItemFocus: FocusRequester,
    modifier: Modifier = Modifier
) {
    val sortedItems = remember(items) { items.sortedBy { it.name } }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Mga bibilhin · ${items.size}",
            color = KainTextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Pindutin ang ▲ ▼ para mag-scroll sa listahan.",
            color = KainTextSecondary,
            fontSize = 15.sp
        )

        if (sortedItems.isEmpty()) {
            MessageCard(message = "Walang bibilhin para sa cycle na ito.")
        } else {
            // Padding gives focused cards room to scale without being clipped
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(sortedItems, key = { _, item -> item.name }) { index, item ->
                    GroceryItemCard(
                        item = item,
                        modifier = if (index == 0) {
                            Modifier.focusRequester(firstItemFocus)
                        } else {
                            Modifier
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GroceryItemCard(
    item: GroceryPurchaseItem,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    // Focusable so the D-pad can scroll through the list
    Card(
        onClick = {},
        modifier = modifier.fillMaxWidth(),
        scale = CardDefaults.scale(focusedScale = 1.02f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = KainSurface,
            focusedContainerColor = KainGreenDark
        ),
        border = CardDefaults.border(
            border = Border.None,
            focusedBorder = Border(BorderStroke(3.dp, KainAccent), shape = shape)
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
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = "Bilhin: ${item.purchaseCount} × " +
                            "${formatQuantity(item.purchaseQuantity)} " +
                            item.purchaseUnit.name.lowercase(),
                    color = KainAccent,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Kailangan: ${formatQuantity(item.requiredQuantity)} " +
                            "${item.requiredUnit.name.lowercase()} · " +
                            "Kabuuan: ${formatQuantity(item.purchasedQuantity)} " +
                            item.purchaseUnit.name.lowercase(),
                    color = KainTextSecondary,
                    fontSize = 15.sp,
                    maxLines = 1
                )
            }

            Text(
                text = formatPeso(item.estimatedCostCentavos),
                color = KainTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    hint: String? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KainSurface, RoundedCornerShape(16.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "🛒", fontSize = 40.sp)
        Text(
            text = message,
            color = KainTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        if (hint != null) {
            Text(
                text = hint,
                color = KainTextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
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