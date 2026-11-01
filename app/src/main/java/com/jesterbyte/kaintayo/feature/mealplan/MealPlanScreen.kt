package com.jesterbyte.kaintayo.feature.mealplan

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.jesterbyte.kaintayo.data.KainTayoAppContainer
import com.jesterbyte.kaintayo.core.time.PaydayCycle
import com.jesterbyte.kaintayo.core.time.PaydayCycleCalculator
import com.jesterbyte.kaintayo.domain.model.MealType
import com.jesterbyte.kaintayo.domain.model.PlannedMeal
import com.jesterbyte.kaintayo.ui.theme.KainAccent
import com.jesterbyte.kaintayo.ui.theme.KainTextPrimary
import com.jesterbyte.kaintayo.ui.theme.KainTextSecondary
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MealPlanScreen(
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

    var selectedDate by remember(cycle) {
        mutableStateOf(
            today.coerceIn(cycle.startDate, cycle.endDate)
        )
    }

    var plannedMeals by remember {
        mutableStateOf<List<PlannedMeal>>(emptyList())
    }

    var isLoading by remember {
        mutableStateOf(true)
    }

    val dateFormatter = remember {
        DateTimeFormatter.ofPattern(
            "EEEE, MMMM d, yyyy",
            Locale.ENGLISH
        )
    }

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

    val mealsForSelectedDate = remember(
        plannedMeals,
        selectedDate
    ) {
        plannedMeals
            .filter { it.date == selectedDate }
            .sortedBy { it.mealType.ordinal }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "My Meal Plan",
            fontSize = 32.sp,
            color = KainTextPrimary
        )

        Text(
            text = "Payday cycle: ${cycle.startDate} – ${cycle.endDate}",
            fontSize = 18.sp,
            color = KainTextSecondary
        )

        if (householdSize != null) {
            Text(
                text = "Current household setting: $householdSize people",
                fontSize = 16.sp,
                color = KainTextSecondary
            )
        }

        DateNavigation(
            selectedDate = selectedDate,
            cycle = cycle,
            dateFormatter = dateFormatter,
            onDateSelected = { selectedDate = it }
        )

        when {
            isLoading -> {
                Text(
                    text = "Loading meal plan...",
                    fontSize = 20.sp,
                    color = KainTextPrimary
                )
            }

            mealsForSelectedDate.isEmpty() -> {
                if (plannedMeals.isEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No meal plan generated yet.",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = KainTextPrimary
                        )

                        Text(
                            text = "Go to Set Budget on the dashboard to generate meals for this payday cycle.",
                            fontSize = 18.sp,
                            color = KainTextSecondary
                        )
                    }
                } else {
                    Text(
                        text = "No meals have been planned for this date.",
                        fontSize = 20.sp,
                        color = KainTextSecondary
                    )
                }
            }

            else -> {
                MealList(
                    meals = mealsForSelectedDate
                )
            }
        }

        androidx.tv.material3.Button(
            onClick = onBack
        ) {
            Text("Back to Dashboard")
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DateNavigation(
    selectedDate: LocalDate,
    cycle: PaydayCycle,
    dateFormatter: DateTimeFormatter,
    onDateSelected: (LocalDate) -> Unit
) {
    val previousDate = selectedDate.minusDays(1)
    val nextDate = selectedDate.plusDays(1)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.tv.material3.Button(
            onClick = { onDateSelected(previousDate) },
            enabled = selectedDate.isAfter(cycle.startDate)
        ) {
            Text("Previous Day")
        }

        Text(
            text = selectedDate.format(dateFormatter),
            fontSize = 22.sp,
            color = KainAccent,
            fontWeight = FontWeight.Bold
        )

        androidx.tv.material3.Button(
            onClick = { onDateSelected(nextDate) },
            enabled = selectedDate.isBefore(cycle.endDate)
        ) {
            Text("Next Day")
        }
    }
}

@Composable
private fun MealList(
    meals: List<PlannedMeal>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MealType.entries.forEach { mealType ->
            val meal = meals.firstOrNull {
                it.mealType == mealType
            }

            if (meal != null) {
                MealCard(meal = meal)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun MealCard(
    meal: PlannedMeal
) {
    androidx.tv.material3.Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = when (meal.mealType) {
                    MealType.BREAKFAST -> "BREAKFAST"
                    MealType.LUNCH -> "LUNCH"
                    MealType.DINNER -> "DINNER"
                },
                fontSize = 16.sp,
                color = KainTextSecondary
            )

            Text(
                text = meal.recipeName,
                fontSize = 24.sp,
                color = KainTextPrimary
            )

            Text(
                text = meal.description,
                fontSize = 16.sp,
                color = KainTextSecondary
            )

            Text(
                text = "${meal.preparationMinutes} minutes",
                fontSize = 14.sp,
                color = KainTextSecondary
            )
        }
    }
}