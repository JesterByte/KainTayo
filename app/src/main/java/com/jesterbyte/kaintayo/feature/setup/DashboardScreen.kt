package com.jesterbyte.kaintayo.feature.setup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import java.time.temporal.ChronoUnit

private enum class Destination { HOME, MEAL_PLAN, BUDGET, GROCERIES }

private data class StepInfo(
    val number: Int,
    val destination: Destination,
    val symbol: String,
    val title: String,
    val description: String
)

// Order matters: the meal plan is created in Step 1, so Steps 2 and 3 depend on it
private val Steps = listOf(
    StepInfo(
        number = 1,
        destination = Destination.BUDGET,
        symbol = "💰",
        title = "I-set ang Budget",
        description = "Ilagay ang budget at kung ilan kayong kakain."
    ),
    StepInfo(
        number = 2,
        destination = Destination.GROCERIES,
        symbol = "🛒",
        title = "Tingnan ang Grocery List",
        description = "Alamin kung ano ang bibilhin sa palengke."
    ),
    StepInfo(
        number = 3,
        destination = Destination.MEAL_PLAN,
        symbol = "📅",
        title = "Tingnan ang Meal Plan",
        description = "Alamin ang ulam sa almusal, tanghalian, at hapunan."
    )
)

private val FilipinoDays = listOf(
    "Lunes", "Martes", "Miyerkules", "Huwebes", "Biyernes", "Sabado", "Linggo"
)

private val FilipinoMonths = listOf(
    "Enero", "Pebrero", "Marso", "Abril", "Mayo", "Hunyo",
    "Hulyo", "Agosto", "Setyembre", "Oktubre", "Nobyembre", "Disyembre"
)

private fun monthDay(date: LocalDate): String =
    "${FilipinoMonths[date.monthValue - 1]} ${date.dayOfMonth}"

private fun fullDate(date: LocalDate): String =
    "${FilipinoDays[date.dayOfWeek.value - 1]} · ${monthDay(date)}"

@Composable
fun DashboardScreen(appContainer: KainTayoAppContainer) {

    val coroutineScope = rememberCoroutineScope()

    var destination by rememberSaveable { mutableStateOf(Destination.HOME) }
    // Remembers which step was opened last, so the home screen can suggest what's next
    var lastVisited by rememberSaveable { mutableStateOf(Destination.MEAL_PLAN) }

    var generationMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val today = remember { LocalDate.now() }
    val cycle = remember(today) { PaydayCycleCalculator.getCurrentCycle(today) }

    // null = still loading
    var plannedMeals by remember { mutableStateOf<List<PlannedMeal>?>(null) }

    // Keeps the home screen fresh, including right after a new plan is generated
    LaunchedEffect(cycle) {
        appContainer.mealPlanRepository
            .getPlannedMealsForPeriod(
                startDate = cycle.startDate,
                endDate = cycle.endDate
            )
            .collectLatest { plannedMeals = it }
    }

    val hasPlan: Boolean? = plannedMeals?.isNotEmpty()

    val todaysMeals = remember(plannedMeals, today) {
        plannedMeals.orEmpty()
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
                    generationMessage = "Ginagawa na ang meal plan... Sandali lang po."

                    coroutineScope.launch {
                        try {
                            val result = appContainer.generatePaydayMealPlanUseCase(
                                today = LocalDate.now(),
                                targetServings = householdSize,
                                budgetCentavos = budgetCentavos
                            )

                            generationMessage = when (result?.status) {
                                MealPlanGenerationStatus.GENERATED ->
                                    "Tapos na ang meal plan! Sunod: tingnan ang Grocery List."

                                MealPlanGenerationStatus.OVER_BUDGET ->
                                    "Lampas sa budget ang tantyang gastos. Subukang dagdagan ang budget."

                                MealPlanGenerationStatus.CANNOT_GENERATE ->
                                    "Kulang ang mga recipe para makabuo ng plano."

                                null ->
                                    "Pakisuri ang budget at kung ilan ang kakain."
                            }

                            if (result?.status == MealPlanGenerationStatus.GENERATED) {
                                destination = Destination.HOME
                            }
                        } catch (exception: Exception) {
                            if (exception is CancellationException) throw exception

                            generationMessage =
                                "May nangyaring problema. Pakisubukan ulit."
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
            hasPlan = hasPlan,
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
    hasPlan: Boolean?,
    todaysMeals: List<PlannedMeal>,
    generationMessage: String?,
    lastVisited: Destination,
    onOpenMealPlan: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenGroceries: () -> Unit
) {
    val planReady = hasPlan == true

    // Which step to point the user to
    val nextStep: Destination? = when {
        !planReady -> Destination.BUDGET
        lastVisited == Destination.BUDGET -> Destination.GROCERIES
        lastVisited == Destination.GROCERIES -> Destination.MEAL_PLAN
        else -> null // everything done; this is the everyday "just show me the meals" case
    }

    val focusRequesters = remember {
        mapOf(
            Destination.MEAL_PLAN to FocusRequester(),
            Destination.BUDGET to FocusRequester(),
            Destination.GROCERIES to FocusRequester()
        )
    }

    var hint by remember { mutableStateOf<String?>(null) }
    var focusPlaced by remember { mutableStateOf(false) }

    // Wait until we know if a plan exists, then focus the right step once
    LaunchedEffect(hasPlan) {
        if (hasPlan != null && !focusPlaced) {
            focusPlaced = true
            val target = nextStep ?: lastVisited
            runCatching { focusRequesters.getValue(target).requestFocus() }
        }
    }

    val totalDays = maxOf(cycle.totalDays, 1)
    val dayNumber = (ChronoUnit.DAYS.between(cycle.startDate, today).toInt() + 1)
        .coerceIn(1, totalDays.toInt())
    val cycleRange = "${monthDay(cycle.startDate)} – ${monthDay(cycle.endDate)}"

    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "Magandang umaga po"
        in 12..13 -> "Magandang tanghali po"
        in 14..17 -> "Magandang hapon po"
        else -> "Magandang gabi po"
    }

    val message = hint ?: generationMessage

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = NonInteractiveSurfaceDefaults.colors(containerColor = KainBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 48.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    text = fullDate(today),
                    color = KainTextSecondary,
                    fontSize = 16.sp
                )
            }

            // Greeting + instruction (also shows messages, so the layout never jumps)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$greeting! 👋",
                    color = KainTextPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message ?: "Sundan lang ang 3 hakbang na ito para sa ulam at grocery ngayong sweldo.",
                    color = if (message != null) KainAccent else KainTextSecondary,
                    fontSize = 17.sp,
                    fontWeight = if (message != null) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // The 3 steps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Steps.forEach { step ->
                    val locked = step.destination != Destination.BUDGET && !planReady
                    val done = step.destination == Destination.BUDGET && planReady

                    StepCard(
                        step = step,
                        locked = locked,
                        done = done,
                        isNext = step.destination == nextStep,
                        modifier = Modifier
                            .weight(1f)
                            .height(200.dp)
                            .focusRequester(focusRequesters.getValue(step.destination))
                    ) {
                        if (locked) {
                            hint = "Gawin muna ang Step 1: I-set ang Budget para magkaroon ng meal plan."
                        } else {
                            hint = null
                            when (step.destination) {
                                Destination.BUDGET -> onOpenBudget()
                                Destination.GROCERIES -> onOpenGroceries()
                                else -> onOpenMealPlan()
                            }
                        }
                    }
                }
            }

            // Cycle progress + today's menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(132.dp),
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
                    hasPlan = planReady,
                    meals = todaysMeals,
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight()
                )
            }

            Text(
                text = "Gamitin ang ◀ ▶ na arrow at ang OK button ng remote.",
                color = KainTextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun StepCard(
    step: StepInfo,
    locked: Boolean,
    done: Boolean,
    isNext: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    val chip = when {
        locked -> "🔒 Step 1 muna"
        done -> "✓ Tapos na"
        isNext && step.number == 1 -> "👉 Simulan dito"
        isNext -> "👉 Susunod na"
        else -> null
    }

    Card(
        onClick = onClick,
        modifier = modifier,
        scale = CardDefaults.scale(focusedScale = 1.05f),
        shape = CardDefaults.shape(shape = shape),
        colors = CardDefaults.colors(
            containerColor = if (done) KainGreenDark else KainSurface,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            // The suggested next step keeps a visible outline even when not focused
            border = if (isNext) {
                Border(BorderStroke(2.dp, KainAccent), shape = shape)
            } else {
                Border.None
            },
            focusedBorder = Border(BorderStroke(3.dp, KainAccent), shape = shape)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (locked) KainBackground else KainAccent,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (done) "✓" else step.number.toString(),
                        color = if (locked) KainTextSecondary else KainBackground,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(text = step.symbol, fontSize = 28.sp)
            }

            Text(
                text = step.title,
                color = if (locked) KainTextSecondary else Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = step.description,
                color = KainTextSecondary,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.weight(1f))

            if (chip != null) {
                Text(
                    text = chip,
                    color = if (locked) KainTextSecondary else KainAccent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "BUDGET CYCLE NGAYON",
            color = KainTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        Text(
            text = cycleRange,
            color = KainTextPrimary,
            fontSize = 22.sp,
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
            text = "Araw $dayNumber ng $totalDays",
            color = KainAccent,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun TodayMenuCard(
    hasPlan: Boolean,
    meals: List<PlannedMeal>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(KainSurface, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "ULAM NGAYON",
            color = KainTextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp
        )

        if (meals.isEmpty()) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (hasPlan) "Walang nakaplanong ulam ngayon" else "Wala pang meal plan",
                color = KainTextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (hasPlan) {
                    "Buksan ang Meal Plan para makita ang ibang araw."
                } else {
                    "Gawin muna ang Step 1 para magkaroon ng ulam."
                },
                color = KainTextSecondary,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.weight(1f))
        } else {
            MealType.entries.forEach { type ->
                val meal = meals.firstOrNull { it.mealType == type }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = mealEmoji(type), fontSize = 18.sp)

                    Text(
                        text = mealLabel(type),
                        color = KainAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        modifier = Modifier.width(112.dp)
                    )

                    Text(
                        text = meal?.recipeName ?: "—",
                        color = if (meal != null) KainTextPrimary else KainTextSecondary,
                        fontSize = 16.sp,
                        fontWeight = if (meal != null) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
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