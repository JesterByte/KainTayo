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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import java.util.Locale

private val ErrorColor = Color(0xFFFF8A80)

// Quick-pick budgets in whole pesos, so nobody has to type with the remote
private val BudgetPresets = listOf(3_000L, 5_000L, 7_500L, 10_000L)
private const val BudgetStepPesos = 500L
private const val MaxBudgetCentavos = 99_999_999_900L
private const val DefaultHousehold = 2
private const val MaxHousehold = 20

private val ShortFilipinoMonths = listOf(
    "Ene", "Peb", "Mar", "Abr", "May", "Hun",
    "Hul", "Ago", "Set", "Okt", "Nob", "Dis"
)

private fun formatShortDate(date: LocalDate): String =
    "${ShortFilipinoMonths[date.monthValue - 1]} ${date.dayOfMonth}"

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
        mutableStateOf(initialBudgetCentavos?.let { centavosToText(it) } ?: "")
    }
    var householdSize by remember(initialHouseholdSize) {
        mutableStateOf((initialHouseholdSize ?: DefaultHousehold).coerceIn(1, MaxHousehold))
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cycle = remember { PaydayCycleCalculator.getCurrentCycle(LocalDate.now()) }
    val parsedBudget = remember(budgetText) { parseBudgetCentavos(budgetText) }
    val hasValidBudget = parsedBudget != null && parsedBudget > 0L

    val firstPresetFocus = remember { FocusRequester() }
    val generateFocus = remember { FocusRequester() }

    // New users start on the budget choices; returning users start on the Generate button
    LaunchedEffect(Unit) {
        runCatching {
            if (hasValidBudget) generateFocus.requestFocus() else firstPresetFocus.requestFocus()
        }
    }

    fun adjustBudget(deltaPesos: Long) {
        val updated = ((parsedBudget ?: 0L) + deltaPesos * 100L)
            .coerceIn(0L, MaxBudgetCentavos)
        budgetText = if (updated == 0L) "" else centavosToText(updated)
        errorMessage = null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = NonInteractiveSurfaceDefaults.colors(containerColor = KainBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 48.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onBack) {
                    Text("← Bumalik", fontSize = 16.sp)
                }
                Text("💰", fontSize = 30.sp)
                Column {
                    Text(
                        text = "Step 1: I-set ang Budget",
                        color = KainTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dalawa lang ang kailangan: ang budget at kung ilan kayong kakain.",
                        color = KainTextSecondary,
                        fontSize = 16.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Left: inputs
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionTitle(
                            number = 1,
                            title = "Magkano ang budget?",
                            hint = "Pindutin ang + o − (₱500 bawat pindot), o pumili sa ibaba."
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StepperButton("−") { adjustBudget(-BudgetStepPesos) }

                            BudgetField(
                                value = budgetText,
                                onValueChange = {
                                    budgetText = sanitizeBudgetInput(it)
                                    errorMessage = null
                                },
                                onDone = { focusManager.moveFocus(FocusDirection.Down) },
                                modifier = Modifier.weight(1f)
                            )

                            StepperButton("+") { adjustBudget(BudgetStepPesos) }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BudgetPresets.forEachIndexed { index, pesos ->
                                PresetChip(
                                    label = formatPeso(pesos * 100, fractionDigits = 0),
                                    selected = parsedBudget == pesos * 100,
                                    modifier = Modifier
                                        .weight(1f)
                                        .then(
                                            if (index == 0) {
                                                Modifier.focusRequester(firstPresetFocus)
                                            } else {
                                                Modifier
                                            }
                                        )
                                ) {
                                    budgetText = pesos.toString()
                                    errorMessage = null
                                }
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionTitle(
                            number = 2,
                            title = "Ilan kayong kakain?",
                            hint = "Pindutin ang + o − para baguhin ang bilang."
                        )

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
                                fontSize = 40.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.width(80.dp)
                            )

                            StepperButton("+") {
                                if (householdSize < MaxHousehold) householdSize++
                                errorMessage = null
                            }

                            Text(
                                text = "tao",
                                color = KainTextSecondary,
                                fontSize = 22.sp
                            )
                        }
                    }
                }

                // Right: preview and action
                Column(
                    modifier = Modifier.weight(0.9f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PlanPreview(
                        cycleRange = "${formatShortDate(cycle.startDate)} – " +
                                formatShortDate(cycle.endDate),
                        totalDays = cycle.totalDays.toInt(),
                        budgetCentavos = parsedBudget.takeIf { hasValidBudget },
                        householdSize = householdSize
                    )

                    SectionTitle(
                        number = 3,
                        title = "Kapag okay na, pindutin ito",
                        hint = null
                    )

                    GenerateCard(
                        isGenerating = isGenerating,
                        modifier = Modifier.focusRequester(generateFocus)
                    ) {
                        if (isGenerating) return@GenerateCard

                        if (parsedBudget == null || parsedBudget <= 0L) {
                            errorMessage = "Maglagay muna ng budget na higit sa ₱0."
                        } else {
                            errorMessage = null
                            onGenerate(parsedBudget, householdSize)
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = "⚠ ${errorMessage!!}",
                            color = ErrorColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (generationMessage != null) {
                        Text(
                            text = generationMessage,
                            color = KainAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Text(
                text = "Naka-save sa TV na ito ang mga inilagay mo.",
                color = KainTextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(
    number: Int,
    title: String,
    hint: String?
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(KainAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    color = KainBackground,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = title,
                color = KainTextPrimary,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (hint != null) {
            Text(
                text = hint,
                color = KainTextSecondary,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 40.dp)
            )
        }
    }
}

@Composable
private fun BudgetField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .onFocusChanged { focused = it.hasFocus }
            .background(KainSurface, shape)
            .border(
                width = if (focused) 3.dp else 1.dp,
                color = if (focused) KainAccent else KainGreenDark,
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        singleLine = true,
        textStyle = TextStyle(
            color = KainTextPrimary,
            fontSize = 28.sp,
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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Ibang halaga? Pindutin ang OK",
                            color = KainTextSecondary,
                            fontSize = 16.sp
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Card(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        scale = CardDefaults.scale(focusedScale = 1.06f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = if (selected) KainGreenDark else KainSurface,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            border = if (selected) {
                Border(BorderStroke(2.dp, KainAccent), shape = shape)
            } else {
                Border.None
            },
            focusedBorder = Border(BorderStroke(3.dp, KainAccent), shape = shape)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
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
    val shape = RoundedCornerShape(14.dp)

    Card(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        scale = CardDefaults.scale(focusedScale = 1.1f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = KainSurface,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            border = Border.None,
            focusedBorder = Border(BorderStroke(3.dp, KainAccent), shape = shape)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                color = Color.White,
                fontSize = 32.sp,
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "BUOD NG PLANO",
            color = KainTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        PreviewRow(label = "Panahon", value = "$cycleRange · $totalDays araw")
        PreviewRow(
            label = "Budget",
            value = budgetCentavos?.let { formatPeso(it) } ?: "—"
        )
        PreviewRow(label = "Kakain", value = "$householdSize tao")

        Spacer(modifier = Modifier.height(2.dp))

        if (budgetCentavos != null && totalDays > 0) {
            val perPersonPerDay = budgetCentavos / (totalDays.toLong() * householdSize)

            Text(
                text = "Halos ${formatPeso(perPersonPerDay)}",
                color = KainAccent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "bawat tao, bawat araw",
                color = KainTextSecondary,
                fontSize = 15.sp
            )
        } else {
            Text(
                text = "Maglagay ng budget para makita kung magkano ang pwedeng gastusin kada araw.",
                color = KainTextSecondary,
                fontSize = 15.sp
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
        Text(text = label, color = KainTextSecondary, fontSize = 16.sp)
        Text(
            text = value,
            color = KainTextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun GenerateCard(
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        scale = CardDefaults.scale(focusedScale = 1.04f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = KainGreenDark,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            border = Border(BorderStroke(2.dp, KainAccent), shape = shape),
            focusedBorder = Border(BorderStroke(4.dp, KainAccent), shape = shape)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isGenerating) "Ginagawa na… sandali lang po" else "🍲  Gumawa ng Meal Plan",
                color = Color.White,
                fontSize = 21.sp,
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

/** "500000" centavos -> "5000"; "500050" -> "5000.50" */
private fun centavosToText(centavos: Long): String =
    if (centavos % 100L == 0L) {
        (centavos / 100L).toString()
    } else {
        BigDecimal.valueOf(centavos, 2).toPlainString()
    }

private fun formatPeso(centavos: Long, fractionDigits: Int = 2): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-PH")).apply {
        minimumFractionDigits = fractionDigits
        maximumFractionDigits = fractionDigits
    }
    return format.format(centavos / 100.0)
}