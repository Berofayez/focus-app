package com.focusapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Muted, low-stimulation palette: slate blue-green, no alarming reds/oranges.
private val FocusPrimary = Color(0xFF4A6670)
private val FocusOnPrimary = Color(0xFFFFFFFF)
private val FocusSecondary = Color(0xFF7D9B8A)
private val FocusBackground = Color(0xFFF5F3EF)
private val FocusSurface = Color(0xFFFFFFFF)
private val FocusOnBackground = Color(0xFF2B2B2B)

private val FocusColorScheme = lightColorScheme(
    primary = FocusPrimary,
    onPrimary = FocusOnPrimary,
    secondary = FocusSecondary,
    background = FocusBackground,
    onBackground = FocusOnBackground,
    surface = FocusSurface,
    onSurface = FocusOnBackground,
)

// Larger-than-default type scale for readability.
private val FocusTypography = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 26.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
)

@Composable
fun FocusAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FocusColorScheme,
        typography = FocusTypography,
        content = content,
    )
}
