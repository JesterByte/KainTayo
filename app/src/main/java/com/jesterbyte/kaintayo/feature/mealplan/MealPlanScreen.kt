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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CycleDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
private val FullDateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)
private val DayNameFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

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
                    Text("📅", fontSize = 28.sp)
                    Text(
                        text = "My Meal Plan",
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
                            text = "Cooking for $householdSize",
                            color = KainTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Day picker
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
                    text = selectedDate.format(FullDateFormatter),
                    color = KainTextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                if (selectedDate == today) {
                    Text(
                        text = "TODAY",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(KainGreen, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Content
            when {
                isLoading -> {
                    Text(
                        text = "Loading meal plan...",
                        color = KainTextSecondary,
                        fontSize = 16.sp
                    )
                }

                plannedMeals.isEmpty() -> {
                    EmptyState(
                        title = "No meal plan generated yet",
                        message = "Go to Set Budget on the dashboard to generate meals for this payday cycle."
                    )
                }

                mealsForSelectedDate.isEmpty() -> {
                    EmptyState(
                        title = "Nothing planned for this day",
                        message = "Pick another day from the strip above."
                    )
                }

                else -> {
                    MealRow(meals = mealsForSelectedDate)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
    val shape = RoundedCornerShape(12.dp)

    Card(
        // Days are selected as soon as they receive focus, so D-pad browsing is instant
        onClick = {},
        modifier = modifier
            .width(72.dp)
            .height(80.dp)
            .onFocusChanged { if (it.isFocused) onFocused() },
        scale = CardDefaults.scale(focusedScale = 1.05f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = if (isSelected) KainGreenDark else KainSurface,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            border = if (isSelected) {
                Border(BorderStroke(1.5.dp, KainAccent), shape = shape)
            } else {
                Border.None
            },
            focusedBorder = Border(BorderStroke(2.dp, KainAccent), shape = shape)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.format(DayNameFormatter).uppercase(),
                color = KainTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = date.dayOfMonth.toString(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isToday) "TODAY" else " ",
                color = KainAccent,
                fontSize = 9.sp,
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
                .height(210.dp)

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
    val shape = RoundedCornerShape(14.dp)

    // Focusable so the D-pad can scroll the page; onClick is ready for a future detail view
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
            focusedBorder = Border(BorderStroke(2.dp, KainAccent), shape = shape)
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
                Text(text = mealEmoji(meal.mealType), fontSize = 20.sp)
                Text(
                    text = mealLabel(meal.mealType),
                    color = KainAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = meal.recipeName,
                color = KainTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = meal.description,
                color = KainTextSecondary,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "⏱ ${meal.preparationMinutes} min",
                color = KainAccent,
                fontSize = 13.sp,
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
            .background(KainSurface.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = mealLabel(mealType),
                color = KainTextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Nothing planned",
                color = KainTextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun EmptyState(
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KainSurface, RoundedCornerShape(16.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "🍽️", fontSize = 36.sp)
        Text(
            text = title,
            color = KainTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message,
            color = KainTextSecondary,
            fontSize = 14.sp
        )
    }
}

private fun mealLabel(type: MealType): String = when (type) {
    MealType.BREAKFAST -> "BREAKFAST"
    MealType.LUNCH -> "LUNCH"
    MealType.DINNER -> "DINNER"
}

private fun mealEmoji(type: MealType): String = when (type) {
    MealType.BREAKFAST -> "🌅"
    MealType.LUNCH -> "☀️"
    MealType.DINNER -> "🌙"
}