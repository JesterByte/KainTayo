package com.jesterbyte.kaintayo.ui.theme

import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

@OptIn(ExperimentalTvMaterial3Api::class)
private val KainTayoColorScheme = darkColorScheme(
    primary = KainGreen,
    secondary = KainAccent,
    background = KainBackground,
    surface = KainSurface,
    onPrimary = KainTextPrimary,
    onSecondary = KainBackground,
    onBackground = KainTextPrimary,
    onSurface = KainTextPrimary
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun KainTayoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KainTayoColorScheme,
        typography = Typography,
        content = content
    )
}