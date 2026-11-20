package com.bankmanaging.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.bankmanaging.app.data.ThemeMode

@Immutable
data class FinancialColors(
    val income: Color,
    val incomeContainer: Color,
    val outcome: Color,
    val outcomeContainer: Color,
    val other: Color,
    val otherContainer: Color,
    val heroStart: Color,
    val heroEnd: Color
)

private val LightFinancialColors = FinancialColors(
    income = IncomeEmerald,
    incomeContainer = IncomeContainerLight,
    outcome = OutcomeRose,
    outcomeContainer = OutcomeContainerLight,
    other = OtherAmber,
    otherContainer = OtherContainerLight,
    heroStart = SapphireDeep,
    heroEnd = SapphirePrimary
)

private val DarkFinancialColors = FinancialColors(
    income = IncomeEmeraldDark,
    incomeContainer = IncomeContainerDark,
    outcome = OutcomeRoseDark,
    outcomeContainer = OutcomeContainerDark,
    other = OtherAmberDark,
    otherContainer = OtherContainerDark,
    heroStart = Color(0xFF0E2157),
    heroEnd = Color(0xFF1A3E99)
)

val LocalFinancialColors = staticCompositionLocalOf { LightFinancialColors }

private val LightColorScheme = lightColorScheme(
    primary = SapphirePrimary,
    onPrimary = Color.White,
    primaryContainer = SapphireContainerLight,
    onPrimaryContainer = SapphireDeep,
    secondary = TealAccent,
    onSecondary = Color.White,
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = OutcomeRose,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SapphireLight,
    onPrimary = Color(0xFF081433),
    primaryContainer = SapphireContainerDark,
    onPrimaryContainer = Color(0xFFD6E2FF),
    secondary = TealAccent,
    onSecondary = Color(0xFF041C2B),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = OutcomeRoseDark,
    onError = Color(0xFF3E0714)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BankManagingTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme
    val financialColors = if (isDark) DarkFinancialColors else LightFinancialColors

    CompositionLocalProvider(LocalFinancialColors provides financialColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}
