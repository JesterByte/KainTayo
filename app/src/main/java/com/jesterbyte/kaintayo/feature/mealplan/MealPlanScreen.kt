package com.jesterbyte.kaintayo.feature.mealplan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.jesterbyte.kaintayo.core.time.PaydayCycle
import com.jesterbyte.kaintayo.core.time.PaydayCycleCalculator
import com.jesterbyte.kaintayo.data.KainTayoAppContainer
import com.jesterbyte.kaintayo.domain.model.MealType
import com.jesterbyte.kaintayo.domain.model.PlannedMeal
import com.jesterbyte.kaintayo.ui.theme.KainAccent
import com.jesterbyte.kaintayo.ui.theme.KainBackground
import com.jesterbyte.kaintayo.ui.theme.KainGreen
import com.jesterbyte.kaintayo.ui.theme.KainGreenDark
import com.jesterbyte.kaintayo.ui.theme.KainSurface
import com.jesterbyte.kaintayo.ui.theme.KainTextPrimary
import com.jesterbyte.kaintayo.ui.theme.KainTextSecondary
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate

private val FilipinoDays = listOf(
    "Lunes", "Martes", "Miyerkules", "Huwebes", "Biyernes", "Sabado", "Linggo"
)

private val ShortFilipinoDays = listOf("Lun", "Mar", "Miy", "Huw", "Biy", "Sab", "Lin")

private val FilipinoMonths = listOf(
    "Enero", "Pebrero", "Marso", "Abril", "Mayo", "Hunyo",
    "Hulyo", "Agosto", "Setyembre", "Oktubre", "Nobyembre", "Disyembre"
)

private val ShortFilipinoMonths = listOf(
    "Ene", "Peb", "Mar", "Abr", "May", "Hun",
    "Hul", "Ago", "Set", "Okt", "Nob", "Dis"
)

private fun formatShortDate(date: LocalDate): String =
    "${ShortFilipinoMonths[date.monthValue - 1]} ${date.dayOfMonth}"

private fun formatFullDate(date: LocalDate): String =
    "${FilipinoDays[date.dayOfWeek.value - 1]}, " +
            "${FilipinoMonths[date.monthValue - 1]} ${date.dayOfMonth}"

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MealPlanScreen(
    appContainer: KainTayoAppContainer,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val today = remember { LocalDate.now() }
    val cycle = remember(today) { PaydayCycleCalculator.getCurrentCycle(today) }
    val householdSize = remember { appContainer.preferences.getHouseholdSize() }

    var selectedDate by remember(cycle) {
        mutableStateOf(today.coerceIn(cycle.startDate, cycle.endDate))
    }
    var plannedMeals by remember { mutableStateOf<List<PlannedMeal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(cycle) {
        appContainer.mealPlanRepository
            .getPlannedMealsForPeriod(
                startDate = cycle.startDate,
                endDate = cycle.endDate
            )
            .collectLatest { meals ->
                plannedMeals = meals
                isLoading = false
            }
    }

    val mealsForSelectedDate = remember(plannedMeals, selectedDate) {
        plannedMeals
            .filter { it.date == selectedDate }
            .sortedBy { it.mealType.ordinal }
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
                    Text("📅", fontSize = 30.sp)
                    Column {
                        Text(
                            text = "Step 3: Meal Plan",
                            color = KainTextPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ito ang ulam sa bawat araw: almusal, tanghalian, at hapunan.",
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
                        text = "Kinukuha ang meal plan... sandali lang po.",
                        color = KainTextSecondary,
                        fontSize = 18.sp
                    )
                }

                plannedMeals.isEmpty() -> {
                    EmptyState(
                        title = "Wala pang meal plan",
                        message = "Gawin muna ang Step 1: I-set ang Budget para magkaroon ng ulam.",
                        hint = "Pindutin ang ← Bumalik para bumalik sa Dashboard."
                    )
                }

                else -> {
                    Text(
                        text = "Pumili ng araw. Pindutin ang ◀ ▶ para lumipat.",
                        color = KainTextSecondary,
                        fontSize = 16.sp
                    )

                    DateStrip(
                        cycle = cycle,
                        today = today,
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it }
                    )

                    // Selected day title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = formatFullDate(selectedDate),
                            color = KainTextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (selectedDate == today) {
                            Text(
                                text = "NGAYON",
                                color = KainBackground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(KainAccent, RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    if (mealsForSelectedDate.isEmpty()) {
                        EmptyState(
                            title = "Walang nakaplano sa araw na ito",
                            message = "Pumili ng ibang araw sa itaas.",
                            hint = null
                        )
                    } else {
                        MealRow(meals = mealsForSelectedDate)
                    }

                    Text(
                        text = "Tapos na ang 3 hakbang! Pindutin ang ← Bumalik para sa Dashboard.",
                        color = KainTextSecondary,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DateStrip(
    cycle: PaydayCycle,
    today: LocalDate,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val days = remember(cycle) {
        generateSequence(cycle.startDate) { it.plusDays(1) }
            .takeWhile { !it.isAfter(cycle.endDate) }
            .toList()
    }
    val initialDate = remember(cycle) { selectedDate }
    val listState = rememberLazyListState()
    val initialFocus = remember { FocusRequester() }

    // Start with the current day scrolled into view and focused
    LaunchedEffect(days) {
        val index = days.indexOf(initialDate).coerceAtLeast(0)
        listState.scrollToItem((index - 2).coerceAtLeast(0))
        withFrameNanos { } // let the row lay out before asking for focus
        runCatching { initialFocus.requestFocus() }
    }

    LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(days, key = { it.toEpochDay() }) { date ->
            DayChip(
                date = date,
                isToday = date == today,
                isSelected = date == selectedDate,
                onFocused = { onDateSelected(date) },
                modifier = if (date == initialDate) {
                    Modifier.focusRequester(initialFocus)
                } else {
                    Modifier
                }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DayChip(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)

    Card(
        // Days are selected as soon as they receive focus, so browsing with the D-pad is instant
        onClick = {},
        modifier = modifier
            .width(88.dp)
            .height(96.dp)
            .onFocusChanged { if (it.isFocused) onFocused() },
        scale = CardDefaults.scale(focusedScale = 1.06f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = if (isSelected) KainGreenDark else KainSurface,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            border = if (isSelected) {
                Border(BorderStroke(2.dp, KainAccent), shape = shape)
            } else {
                Border.None
            },
            focusedBorder = Border(BorderStroke(3.dp, KainAccent), shape = shape)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = ShortFilipinoDays[date.dayOfWeek.value - 1].uppercase(),
                color = KainTextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = date.dayOfMonth.toString(),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isToday) "NGAYON" else " ",
                color = KainAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MealRow(meals: List<PlannedMeal>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MealType.entries.forEach { mealType ->
            val meal = meals.firstOrNull { it.mealType == mealType }
            val cardModifier = Modifier
                .weight(1f)
                .height(250.dp)

            if (meal != null) {
                MealCard(meal = meal, modifier = cardModifier)
            } else {
                EmptyMealSlot(mealType = mealType, modifier = cardModifier)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MealCard(
    meal: PlannedMeal,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)

    // Focusable so the D-pad can reach and scroll to it; onClick is ready for a future recipe screen
    Card(
        onClick = {},
        modifier = modifier,
        scale = CardDefaults.scale(focusedScale = 1.03f),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = mealEmoji(meal.mealType), fontSize = 24.sp)
                Text(
                    text = mealLabel(meal.mealType),
                    color = KainAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = meal.recipeName,
                color = KainTextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = meal.description,
                color = KainTextSecondary,
                fontSize = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "⏱ ${meal.preparationMinutes} minuto",
                color = KainAccent,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyMealSlot(
    mealType: MealType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(KainSurface.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = mealLabel(mealType),
                color = KainTextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Walang nakaplano",
                color = KainTextSecondary,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    message: String,
    hint: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KainSurface, RoundedCornerShape(16.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "🍽️", fontSize = 40.sp)
        Text(
            text = title,
            color = KainTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            color = KainTextSecondary,
            fontSize = 18.sp,
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

private fun mealLabel(type: MealType): String = when (type) {
    MealType.BREAKFAST -> "ALMUSAL"
    MealType.LUNCH -> "TANGHALIAN"
    MealType.DINNER -> "HAPUNAN"
}

private fun mealEmoji(type: MealType): String = when (type) {
    MealType.BREAKFAST -> "🌅"
    MealType.LUNCH -> "☀️"
    MealType.DINNER -> "🌙"
}