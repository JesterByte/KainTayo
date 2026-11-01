package com.jesterbyte.kaintayo.feature.setup

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
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
import com.jesterbyte.kaintayo.domain.usecase.MealPlanGenerationStatus
import com.jesterbyte.kaintayo.feature.groceries.GroceryListScreen
import com.jesterbyte.kaintayo.feature.mealplan.MealPlanScreen
import com.jesterbyte.kaintayo.ui.theme.KainAccent
import com.jesterbyte.kaintayo.ui.theme.KainBackground
import com.jesterbyte.kaintayo.ui.theme.KainGreen
import com.jesterbyte.kaintayo.ui.theme.KainGreenDark
import com.jesterbyte.kaintayo.ui.theme.KainSurface
import com.jesterbyte.kaintayo.ui.theme.KainTextPrimary
import com.jesterbyte.kaintayo.ui.theme.KainTextSecondary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private enum class Destination { HOME, MEAL_PLAN, BUDGET, GROCERIES }

private val HeaderDateFormatter = DateTimeFormatter.ofPattern("EEEE · MMMM d", Locale.ENGLISH)
private val RangeDateFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH)

@Composable
fun DashboardScreen(appContainer: KainTayoAppContainer) {

    val coroutineScope = rememberCoroutineScope()

    var destination by rememberSaveable { mutableStateOf(Destination.HOME) }
    // Remembers which card opened the last screen, so focus returns to it
    var lastVisited by rememberSaveable { mutableStateOf(Destination.MEAL_PLAN) }

    var generationMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    val cycle = remember(today) { PaydayCycleCalculator.getCurrentCycle(today) }

    var plannedMeals by remember { mutableStateOf<List<PlannedMeal>>(emptyList()) }

    // Keeps "Today's menu" fresh, including right after a new plan is generated
    LaunchedEffect(cycle) {
        appContainer.mealPlanRepository
            .getPlannedMealsForPeriod(
                startDate = cycle.startDate,
                endDate = cycle.endDate
            )
            .collectLatest { plannedMeals = it }
    }

    val todaysMeals = remember(plannedMeals, today) {
        plannedMeals
            .filter { it.date == today }
            .sortedBy { it.mealType.ordinal }
    }

    fun open(target: Destination) {
        lastVisited = target
        destination = target
    }

    when (destination) {
        Destination.MEAL_PLAN -> MealPlanScreen(
            appContainer = appContainer,
            onBack = { destination = Destination.HOME }
        )

        Destination.GROCERIES -> GroceryListScreen(
            appContainer = appContainer,
            onBack = { destination = Destination.HOME }
        )

        Destination.BUDGET -> BudgetSetupScreen(
            initialBudgetCentavos = appContainer.preferences.getBudgetCentavos(),
            initialHouseholdSize = appContainer.preferences.getHouseholdSize(),
            generationMessage = generationMessage,
            isGenerating = isGenerating,
            onBack = { destination = Destination.HOME },
            onGenerate = { budgetCentavos, householdSize ->
                if (!isGenerating) {
                    appContainer.preferences.saveBudgetCentavos(budgetCentavos)
                    appContainer.preferences.saveHouseholdSize(householdSize)

                    isGenerating = true
                    generationMessage = "Generating your meal plan..."

                    coroutineScope.launch {
                        try {
                            val result = appContainer.generatePaydayMealPlanUseCase(
                                today = LocalDate.now(),
                                targetServings = householdSize,
                                budgetCentavos = budgetCentavos
                            )

                            generationMessage = when (result?.status) {
                                MealPlanGenerationStatus.GENERATED ->
                                    "Your meal plan was generated successfully!"

                                MealPlanGenerationStatus.OVER_BUDGET ->
                                    "The estimated cost exceeds your budget."

                                MealPlanGenerationStatus.CANNOT_GENERATE ->
                                    "Not enough valid recipes to create a plan."

                                null ->
                                    "Please check your budget and household settings."
                            }

                            if (result?.status == MealPlanGenerationStatus.GENERATED) {
                                destination = Destination.HOME
                            }
                        } catch (exception: Exception) {
                            if (exception is CancellationException) throw exception

                            generationMessage =
                                "Something went wrong while generating your plan."
                        } finally {
                            isGenerating = false
                        }
                    }
                }
            }
        )

        Destination.HOME -> DashboardHome(
            today = today,
            cycle = cycle,
            todaysMeals = todaysMeals,
            generationMessage = generationMessage,
            lastVisited = lastVisited,
            onOpenMealPlan = { open(Destination.MEAL_PLAN) },
            onOpenBudget = {
                generationMessage = null
                open(Destination.BUDGET)
            },
            onOpenGroceries = { open(Destination.GROCERIES) }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DashboardHome(
    today: LocalDate,
    cycle: PaydayCycle,
    todaysMeals: List<PlannedMeal>,
    generationMessage: String?,
    lastVisited: Destination,
    onOpenMealPlan: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenGroceries: () -> Unit
) {
    val focusRequesters = remember {
        mapOf(
            Destination.MEAL_PLAN to FocusRequester(),
            Destination.BUDGET to FocusRequester(),
            Destination.GROCERIES to FocusRequester()
        )
    }

    // Start on the card you came from (Meal Plan on first launch)
    LaunchedEffect(Unit) {
        runCatching { focusRequesters[lastVisited]?.requestFocus() }
    }

    val totalDays = maxOf(cycle.totalDays, 1)
    val dayNumber = (ChronoUnit.DAYS.between(cycle.startDate, today).toInt() + 1)
        .coerceIn(1, totalDays.toInt())
    val cycleRange = "${cycle.startDate.format(RangeDateFormatter)} – " +
            cycle.endDate.format(RangeDateFormatter)

    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🍲", fontSize = 32.sp)
                    Text(
                        text = "KainTayo",
                        color = KainTextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = today.format(HeaderDateFormatter),
                    color = KainTextSecondary,
                    fontSize = 14.sp
                )
            }

            // Greeting
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$greeting! 👋",
                    color = KainTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Here's where things stand today.",
                    color = KainTextSecondary,
                    fontSize = 15.sp
                )
            }

            // Cycle progress + today's menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CycleCard(
                    cycleRange = cycleRange,
                    dayNumber = dayNumber,
                    totalDays = totalDays.toInt(),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                TodayMenuCard(
                    meals = todaysMeals,
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight()
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

            // Navigation cards
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    Alignment.CenterHorizontally
                )
            ) {
                item {
                    DashboardCard(
                        title = "My Meal Plan",
                        description = "View your daily meals",
                        symbol = "📅",
                        modifier = Modifier
                            .width(220.dp)
                            .focusRequester(focusRequesters.getValue(Destination.MEAL_PLAN)),
                        highlighted = true,
                        onClick = onOpenMealPlan
                    )
                }

                item {
                    DashboardCard(
                        title = "Set Budget",
                        description = "Manage your cut-off budget",
                        symbol = "💰",
                        modifier = Modifier
                            .width(220.dp)
                            .focusRequester(focusRequesters.getValue(Destination.BUDGET)),
                        onClick = onOpenBudget
                    )
                }

                item {
                    DashboardCard(
                        title = "Grocery List",
                        description = "View your ingredients",
                        symbol = "🛒",
                        modifier = Modifier
                            .width(220.dp)
                            .focusRequester(focusRequesters.getValue(Destination.GROCERIES)),
                        onClick = onOpenGroceries
                    )
                }
            }

            Text(
                text = "Use your TV remote to navigate",
                color = KainTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CycleCard(
    cycleRange: String,
    dayNumber: Int,
    totalDays: Int,
    modifier: Modifier = Modifier
) {
    val progress = dayNumber / totalDays.toFloat()

    Column(
        modifier = modifier
            .background(KainGreenDark, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "CURRENT BUDGET CYCLE",
            color = KainTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        Text(
            text = cycleRange,
            color = KainTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(KainBackground.copy(alpha = 0.5f), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(KainAccent, RoundedCornerShape(50))
            )
        }

        Text(
            text = "Day $dayNumber of $totalDays",
            color = KainAccent,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TodayMenuCard(
    meals: List<PlannedMeal>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(KainSurface, RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "TODAY'S MENU",
            color = KainTextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        if (meals.isEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "No meals planned yet",
                color = KainTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Open Set Budget to generate a plan for this cycle.",
                color = KainTextSecondary,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            MealType.entries.forEach { type ->
                val meal = meals.firstOrNull { it.mealType == type }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = mealEmoji(type), fontSize = 18.sp)

                    Text(
                        text = mealLabel(type),
                        color = KainAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.width(84.dp)
                    )

                    Text(
                        text = meal?.recipeName ?: "—",
                        color = if (meal != null) KainTextPrimary else KainTextSecondary,
                        fontSize = 15.sp,
                        fontWeight = if (meal != null) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun DashboardCard(
    title: String,
    description: String,
    symbol: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        scale = CardDefaults.scale(focusedScale = 1.05f),
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(12.dp)
        ),
        colors = CardDefaults.colors(
            containerColor = if (highlighted) KainGreenDark else KainSurface,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            border = Border.None,
            focusedBorder = Border(
                border = BorderStroke(2.dp, KainAccent),
                shape = RoundedCornerShape(12.dp)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = symbol,
                fontSize = 22.sp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = description,
                    color = KainTextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
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