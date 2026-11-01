
package com.example.kaintayo.feature.setup

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.NonInteractiveSurfaceDefaults
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.kaintayo.data.KainTayoAppContainer
import com.example.kaintayo.core.time.PaydayCycleCalculator
import com.example.kaintayo.domain.usecase.MealPlanGenerationStatus
import com.example.kaintayo.feature.groceries.GroceryListScreen
import com.example.kaintayo.feature.mealplan.MealPlanScreen
import com.example.kaintayo.ui.theme.KainAccent
import com.example.kaintayo.ui.theme.KainBackground
import com.example.kaintayo.ui.theme.KainGreen
import com.example.kaintayo.ui.theme.KainGreenDark
import com.example.kaintayo.ui.theme.KainSurface
import com.example.kaintayo.ui.theme.KainTextPrimary
import com.example.kaintayo.ui.theme.KainTextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DashboardScreen(appContainer: KainTayoAppContainer) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showBudgetSetup by remember { mutableStateOf(false) }
    var showMealPlan by remember { mutableStateOf(false) }
    var showGroceryList by remember { mutableStateOf(false) }

    var generationMessage by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    val cycle = remember(today) {
        PaydayCycleCalculator.getCurrentCycle(today)
    }

    val dateFormatter = DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH)
    val currentDate = today.format(
        DateTimeFormatter.ofPattern("EEEE · MMMM d", Locale.ENGLISH)
    )
    val cycleRange = "${cycle.startDate.format(dateFormatter)} – " +
            cycle.endDate.format(dateFormatter)

    if (showMealPlan) {
        MealPlanScreen(
            appContainer = appContainer,
            onBack = { showMealPlan = false }
        )
        return
    }

    if (showGroceryList) {
        GroceryListScreen(
            appContainer = appContainer,
            onBack = { showGroceryList = false }
        )
        return
    }

    if (showBudgetSetup) {
        BudgetSetupScreen(
            initialBudgetCentavos = appContainer.preferences.getBudgetCentavos(),
            initialHouseholdSize = appContainer.preferences.getHouseholdSize(),
            onBack = { showBudgetSetup = false },
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
                                showBudgetSetup = false
                            }
                        } catch (exception: Exception) {
                            if (exception is kotlinx.coroutines.CancellationException) {
                                throw exception
                            }
                            generationMessage =
                                "Something went wrong while generating your plan."
                        } finally {
                            isGenerating = false
                        }
                    }
                }
            }
        )
        return
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = NonInteractiveSurfaceDefaults.colors(
            containerColor = KainBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 28.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                    text = currentDate,
                    color = KainTextSecondary,
                    fontSize = 14.sp
                )
            }

            // Payday information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        KainGreenDark,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "CURRENT BUDGET CYCLE",
                    color = KainTextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    cycleRange,
                    color = KainTextPrimary,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "${cycle.totalDays} calendar days in this cycle",
                    color = KainAccent,
                    fontSize = 14.sp
                )
            }

            // Welcome
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "Welcome to KainTayo!",
                    color = KainTextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    "What would you like to do today?",
                    color = KainTextSecondary,
                    fontSize = 16.sp
                )
            }

            if (generationMessage != null) {
                Text(
                    text = generationMessage!!,
                    color = KainAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Dashboard menu - Row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "My Meal Plan",
                    description = "View your daily meals",
                    symbol = "📅",
                    modifier = Modifier.weight(1f),
                    highlighted = true
                ) {
                    showMealPlan = true
                }

                DashboardCard(
                    title = "Set Budget",
                    description = "Manage your cut-off budget",
                    symbol = "💰",
                    modifier = Modifier.weight(1f)
                ) {
                    generationMessage = null
                    showBudgetSetup = true
                }
            }

            // Dashboard menu - Row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DashboardCard(
                    title = "Grocery List",
                    description = "View your ingredients",
                    symbol = "🛒",
                    modifier = Modifier.weight(1f)
                ) {
                    showGroceryList = true
                }

                DashboardCard(
                    title = "Settings",
                    description = "Household preferences",
                    symbol = "⚙️",
                    modifier = Modifier.weight(1f)
                ) {
                    Toast.makeText(
                        context,
                        "Settings coming soon!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer navigation hint
            Text(
                "Use your TV remote to navigate",
                color = KainTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
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
        modifier = modifier.height(112.dp),
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(14.dp)
        ),
        colors = CardDefaults.colors(
            containerColor = if (highlighted) KainGreenDark else KainSurface,
            focusedContainerColor = KainGreen
        ),
        border = CardDefaults.border(
            border = Border.None,
            focusedBorder = Border(
                border = BorderStroke(3.dp, KainAccent),
                shape = RoundedCornerShape(14.dp)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = symbol,
                fontSize = 30.sp
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    color = KainTextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
