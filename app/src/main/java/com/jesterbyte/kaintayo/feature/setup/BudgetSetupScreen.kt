package com.jesterbyte.kaintayo.feature.setup

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.jesterbyte.kaintayo.ui.theme.KainAccent
import com.jesterbyte.kaintayo.ui.theme.KainBackground
import com.jesterbyte.kaintayo.ui.theme.KainGreen
import com.jesterbyte.kaintayo.ui.theme.KainGreenDark
import com.jesterbyte.kaintayo.ui.theme.KainSurface
import com.jesterbyte.kaintayo.ui.theme.KainTextPrimary
import com.jesterbyte.kaintayo.ui.theme.KainTextSecondary
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun BudgetSetupScreen(
    initialBudgetCentavos: Long?,
    initialHouseholdSize: Int?,
    generationMessage: String?,
    onGenerate: (budgetCentavos: Long, householdSize: Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler {
        onBack()
    }

    var budgetText by remember(initialBudgetCentavos) {
        mutableStateOf(
            initialBudgetCentavos?.let {
                BigDecimal.valueOf(it, 2).toPlainString()
            } ?: ""
        )
    }

    var householdText by remember(initialHouseholdSize) {
        mutableStateOf(initialHouseholdSize?.toString() ?: "")
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KainBackground)
            .padding(horizontal = 64.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Budget & Household Setup",
            color = KainTextPrimary,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Set your budget for this payday cycle and the number of people eating.",
            color = KainTextSecondary,
            fontSize = 16.sp
        )

        if (generationMessage != null) {
            Text(
                text = generationMessage,
                color = KainAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "BUDGET PER PAYDAY (PHP)",
                color = KainAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            BasicTextField(
                value = budgetText,
                onValueChange = {
                    budgetText = it.filter { character ->
                        character.isDigit() || character == '.'
                    }
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        KainSurface,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(20.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = KainTextPrimary,
                    fontSize = 22.sp
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                decorationBox = { innerTextField ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "₱ ",
                            color = KainAccent,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        if (budgetText.isEmpty()) {
                            Text(
                                text = "Enter your budget",
                                color = KainTextSecondary,
                                fontSize = 20.sp
                            )
                        }

                        innerTextField()
                    }
                }
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "NUMBER OF PEOPLE",
                color = KainAccent,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            BasicTextField(
                value = householdText,
                onValueChange = {
                    householdText = it.filter(Char::isDigit)
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        KainSurface,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(20.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = KainTextPrimary,
                    fontSize = 22.sp
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                decorationBox = { innerTextField ->
                    if (householdText.isEmpty()) {
                        Text(
                            text = "Enter household size",
                            color = KainTextSecondary,
                            fontSize = 20.sp
                        )
                    }
                    innerTextField()
                }
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color(0xFFFF8A80),
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SetupActionCard(
                title = "Generate Meal Plan",
                highlighted = true,
                modifier = Modifier.weight(1f)
            ) {
                val parsedBudget = runCatching {
                    BigDecimal(budgetText.trim())
                        .setScale(2, RoundingMode.HALF_UP)
                        .movePointRight(2)
                        .longValueExact()
                }.getOrNull()

                val parsedHousehold = householdText.toIntOrNull()

                when {
                    parsedBudget == null || parsedBudget <= 0L -> {
                        errorMessage = "Enter a valid budget greater than ₱0."
                    }

                    parsedHousehold == null || parsedHousehold <= 0 -> {
                        errorMessage = "Enter at least one person."
                    }

                    else -> {
                        errorMessage = null
                        onGenerate(parsedBudget, parsedHousehold)
                    }
                }
            }

            SetupActionCard(
                title = "Back",
                modifier = Modifier.weight(1f),
                highlighted = false,
                onClick = onBack
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Your settings will be saved locally on this device.",
            color = KainTextSecondary,
            fontSize = 13.sp
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SetupActionCard(
    title: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(76.dp),
        shape = CardDefaults.shape(
            shape = RoundedCornerShape(14.dp)
        ),
        colors = CardDefaults.colors(
            containerColor = if (highlighted) {
                KainGreenDark
            } else {
                KainSurface
            },
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
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
