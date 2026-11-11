package com.example.microbitcontroller.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

private val DarkColorScheme = darkColorScheme(
    primary = MicrobitTeal,
    secondary = MicrobitOrange,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = MicrobitTeal,
    secondary = MicrobitOrange,
    tertiary = Pink40
)

@Composable
fun MicrobitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
