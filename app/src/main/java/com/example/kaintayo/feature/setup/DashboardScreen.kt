package com.example.kaintayo.feature.setup

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
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
import com.example.kaintayo.core.time.PaydayCycleCalculator
import com.example.kaintayo.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DashboardScreen() {

    val context = LocalContext.current
    val today = LocalDate.now()
    val cycle = remember(today) {
        PaydayCycleCalculator.getCurrentCycle(today)
    }

    val dateFormatter =
        DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH)

    val currentDate = today.format(
        DateTimeFormatter.ofPattern(
            "EEEE · MMMM d",
            Locale.ENGLISH
        )
    )

    val cycleRange =
        "${cycle.startDate.format(dateFormatter)} – " +
                cycle.endDate.format(dateFormatter)

    Surface(
        modifier = Modifier.fillMaxSize(),
        colors = NonInteractiveSurfaceDefaults.colors(containerColor = KainBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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

            // Dashboard menu
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
                    Toast.makeText(
                        context,
                        "Meal Plan coming soon!",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                DashboardCard(
                    title = "Set Budget",
                    description = "Manage your cut-off budget",
                    symbol = "💰",
                    modifier = Modifier.weight(1f)
                ) {
                    Toast.makeText(
                        context,
                        "Budget Setup coming soon!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

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
                    Toast.makeText(
                        context,
                        "Grocery List coming soon!",
                        Toast.LENGTH_SHORT
                    ).show()
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

            Spacer(modifier = Modifier.weight(1f))

            Text(
                "Use your TV remote to navigate",
                color = KainTextSecondary,
                fontSize = 12.sp
            )
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
