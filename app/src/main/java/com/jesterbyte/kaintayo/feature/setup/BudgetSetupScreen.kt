package com.jesterbyte.kaintayo.feature.setup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import com.jesterbyte.kaintayo.ui.theme.KainAccent
import com.jesterbyte.kaintayo.ui.theme.KainBackground
import com.jesterbyte.kaintayo.ui.theme.KainGreen
import com.jesterbyte.kaintayo.ui.theme.KainGreenDark
import com.jesterbyte.kaintayo.ui.theme.KainSurface
import com.jesterbyte.kaintayo.ui.theme.KainTextPrimary
import com.jesterbyte.kaintayo.ui.theme.KainTextSecondary
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ErrorColor = Color(0xFFFF8A80)
private val CycleDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

// Quick-pick budgets in whole pesos, so users don't have to type with the remote
private val BudgetPresets = listOf(3_000L, 5_000L, 7_500L, 10_000L)
private const val DefaultHousehold = 2
private const val MaxHousehold = 20

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BudgetSetupScreen(
    initialBudgetCentavos: Long?,
    initialHouseholdSize: Int?,
    generationMessage: String?,
    onGenerate: (budgetCentavos: Long, householdSize: Int) -> Unit,
    onBack: () -> Unit,
    isGenerating: Boolean = false
) {
    BackHandler { onBack() }

    val focusManager = LocalFocusManager.current

    var budgetText by remember(initialBudgetCentavos) {
        mutableStateOf(
            initialBudgetCentavos?.let { BigDecimal.valueOf(it, 2).toPlainString() } ?: ""
        )
    }
    var householdSize by remember(initialHouseholdSize) {
        mutableStateOf((initialHouseholdSize ?: DefaultHousehold).coerceIn(1, MaxHousehold))
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cycle = remember { PaydayCycleCalculator.getCurrentCycle(LocalDate.now()) }
    val parsedBudget = remember(budgetText) { parseBudgetCentavos(budgetText) }
    val hasValidBudget = parsedBudget != null && parsedBudget > 0L

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = NonInteractiveSurfaceDefaults.colors(containerColor = KainBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 48.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onBack) {
                    Text("← Back", fontSize = 14.sp)
                }
                Text("💰", fontSize = 28.sp)
                Column {
                    Text(
                        text = "Budget & Household",
                        color = KainTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set your payday budget and how many people are eating.",
                        color = KainTextSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Left: inputs
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FieldLabel("BUDGET PER PAYDAY")

                        BudgetField(
                            value = budgetText,
                            onValueChange = {
                                budgetText = sanitizeBudgetInput(it)
                                errorMessage = null
                            },
                            onDone = { focusManager.moveFocus(FocusDirection.Down) }
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            BudgetPresets.forEach { pesos ->
                                PresetChip(
                                    label = formatPeso(pesos * 100, fractionDigits = 0),
                                    selected = parsedBudget == pesos * 100
                                ) {
                                    budgetText = pesos.toString()
                                    errorMessage = null
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        FieldLabel("NUMBER OF PEOPLE")

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StepperButton("−") {
                                if (householdSize > 1) householdSize--
                                errorMessage = null
                            }

                            Text(
                                text = householdSize.toString(),
                                color = KainTextPrimary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(64.dp)
                            )

                            StepperButton("+") {
                                if (householdSize < MaxHousehold) householdSize++
                                errorMessage = null
                            }

                            Text(
                                text = if (householdSize == 1) "person" else "people",
                                color = KainTextSecondary,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Right: live preview and action
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    PlanPreview(
                        cycleRange = "${cycle.startDate.format(CycleDateFormatter)} – " +
                                cycle.endDate.format(CycleDateFormatter),
                        totalDays = cycle.totalDays.toInt(),
                        budgetCentavos = parsedBudget.takeIf { hasValidBudget },
                        householdSize = householdSize
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = ErrorColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (generationMessage != null) {
                        Text(
                            text = generationMessage,
                            color = KainAccent,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    GenerateCard(isGenerating = isGenerating) {
                        if (isGenerating) return@GenerateCard

                        if (!hasValidBudget) {
                            errorMessage = "Enter a valid budget greater than ₱0."
                        } else {
                            errorMessage = null
                            onGenerate(parsedBudget, householdSize)
                        }
                    }
                }
            }

            Text(
                text = "Your settings are saved on this device.",
                color = KainTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = KainAccent,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun BudgetField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focused = it.hasFocus }
            .background(KainSurface, shape)
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) KainAccent else KainGreenDark,
                shape = shape
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        singleLine = true,
        textStyle = TextStyle(
            color = KainTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        ),
        cursorBrush = SolidColor(KainAccent),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "₱",
                    color = KainAccent,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Press OK to type an amount",
                            color = KainTextSecondary,
                            fontSize = 18.sp
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Card(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        scale = CardDefaults.scale(focusedScale = 1.08f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = if (selected) KainGreenDark else KainSurface,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            border = if (selected) {
                Border(BorderStroke(1.5.dp, KainAccent), shape = shape)
            } else {
                Border.None
            },
            focusedBorder = Border(BorderStroke(2.dp, KainAccent), shape = shape)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun StepperButton(
    symbol: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)

    Card(
        onClick = onClick,
        modifier = Modifier.width(56.dp).height(56.dp),
        scale = CardDefaults.scale(focusedScale = 1.1f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = KainSurface,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            border = Border.None,
            focusedBorder = Border(BorderStroke(2.dp, KainAccent), shape = shape)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PlanPreview(
    cycleRange: String,
    totalDays: Int,
    budgetCentavos: Long?,
    householdSize: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KainGreenDark, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "PLAN PREVIEW",
            color = KainTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        PreviewRow(label = "Cycle", value = "$cycleRange · $totalDays days")
        PreviewRow(
            label = "Budget",
            value = budgetCentavos?.let { formatPeso(it) } ?: "—"
        )
        PreviewRow(label = "People", value = householdSize.toString())

        Spacer(modifier = Modifier.height(4.dp))

        if (budgetCentavos != null && totalDays > 0) {
            val perPersonPerDay = budgetCentavos / (totalDays.toLong() * householdSize)

            Text(
                text = "About ${formatPeso(perPersonPerDay)}",
                color = KainAccent,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "per person, per day",
                color = KainTextSecondary,
                fontSize = 13.sp
            )
        } else {
            Text(
                text = "Enter a budget to see your daily allowance.",
                color = KainTextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = KainTextSecondary, fontSize = 14.sp)
        Text(
            text = value,
            color = KainTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GenerateCard(
    isGenerating: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        scale = CardDefaults.scale(focusedScale = 1.04f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = KainGreenDark,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            border = Border(BorderStroke(1.5.dp, KainAccent), shape = shape),
            focusedBorder = Border(BorderStroke(3.dp, KainAccent), shape = shape)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isGenerating) "Generating…" else "🍲  Generate Meal Plan",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Keeps digits and a single decimal point, with at most 2 decimal places. */
private fun sanitizeBudgetInput(raw: String): String {
    val filtered = raw.filter { it.isDigit() || it == '.' }
    val dot = filtered.indexOf('.')
    if (dot == -1) return filtered.take(9)

    val whole = filtered.substring(0, dot).take(9)
    val decimals = filtered.substring(dot + 1).filter(Char::isDigit).take(2)
    return "$whole.$decimals"
}

private fun parseBudgetCentavos(text: String): Long? = runCatching {
    BigDecimal(text.trim())
        .setScale(2, RoundingMode.HALF_UP)
        .movePointRight(2)
        .longValueExact()
}.getOrNull()

private fun formatPeso(centavos: Long, fractionDigits: Int = 2): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH")).apply {
        minimumFractionDigits = fractionDigits
        maximumFractionDigits = fractionDigits
    }
    return format.format(centavos / 100.0)
}