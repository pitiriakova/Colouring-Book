package com.example.colouringbook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ColouringBookColorScheme = lightColorScheme(
    primary = MintText,
    onPrimary = MintCard,
    secondary = MintBackgroundSoft,
    onSecondary = MintText,
    tertiary = MintMist,
    background = MintBackground,
    onBackground = MintText,
    surface = MintCard,
    onSurface = MintText,
    surfaceVariant = MintBackgroundSoft,
    onSurfaceVariant = MintTextMuted
)

@Composable
fun ColouringBookTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColouringBookColorScheme,
        typography = Typography,
        content = content
    )
}
