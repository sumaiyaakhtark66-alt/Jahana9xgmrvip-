package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val JahanDarkColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = OnGoldDark,
    primaryContainer = GoldContainer,
    onPrimaryContainer = BrightGold,
    secondary = ElectricAmethyst,
    onSecondary = Color(0xFF220054),
    secondaryContainer = DeepVioletContainer,
    onSecondaryContainer = SoftLavender,
    tertiary = EmeraldVictory,
    onTertiary = Color(0xFF00291B),
    tertiaryContainer = Color(0xFF0B4631),
    onTertiaryContainer = Color(0xFFB5FFE0),
    background = ObsidianVoid,
    onBackground = SoftLavender,
    surface = ObsidianPlum,
    onSurface = SoftLavender,
    surfaceVariant = AmethystSurface,
    onSurfaceVariant = MutedLavender,
    surfaceContainerLowest = ObsidianVoid,
    surfaceContainerLow = ObsidianPlum,
    surfaceContainer = RoyalVelvet,
    surfaceContainerHigh = AmethystSurface,
    surfaceContainerHighest = AmethystElevated,
    outline = AmethystBorder,
    outlineVariant = Color(0xFF3A2363),
    error = CrimsonTrap,
    onError = Color(0xFF420010),
    errorContainer = CrimsonContainer,
    onErrorContainer = Color(0xFFFFD9DF)
)

private val JahanLightColorScheme = lightColorScheme(
    primary = AntiqueGold,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE5A3),
    onPrimaryContainer = Color(0xFF382400),
    secondary = ImperialPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DAFF),
    onSecondaryContainer = Color(0xFF24005A),
    tertiary = Color(0xFF0C7E54),
    onTertiary = Color.White,
    background = PearlLavenderBg,
    onBackground = Color(0xFF1A0D30),
    surface = PearlLavenderBg,
    onSurface = Color(0xFF1A0D30),
    surfaceVariant = PearlCard,
    onSurfaceVariant = Color(0xFF534173),
    surfaceContainer = PearlCard,
    surfaceContainerHigh = Color(0xFFE7DAFF),
    outline = Color(0xFFCBB6F0),
    error = Color(0xFFC92A4C),
    onError = Color.White
)

@Immutable
data class VipAccentColors(
    val goldGradient: Brush,
    val purpleAuraGradient: Brush,
    val obsidianCardGradient: Brush,
    val sovereignCardGradient: Brush,
    val goldSolid: Color,
    val goldGlow: Color,
    val amethystGlow: Color,
    val victoryEmerald: Color,
    val dangerCrimson: Color
)

private val DefaultVipAccentColors = VipAccentColors(
    goldGradient = Brush.linearGradient(
        colors = listOf(BrightGold, RoyalGold, DeepBurnishedGold)
    ),
    purpleAuraGradient = Brush.linearGradient(
        colors = listOf(AmethystElevated, RoyalVelvet, ObsidianPlum)
    ),
    obsidianCardGradient = Brush.linearGradient(
        colors = listOf(AmethystSurface, RoyalVelvet, ObsidianVoid)
    ),
    sovereignCardGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF3D1A78), Color(0xFF230E48), Color(0xFF3B2809))
    ),
    goldSolid = RoyalGold,
    goldGlow = BrightGold,
    amethystGlow = ElectricAmethyst,
    victoryEmerald = EmeraldVictory,
    dangerCrimson = CrimsonTrap
)

val LocalVipAccents = staticCompositionLocalOf { DefaultVipAccentColors }

val JahanVipShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun JahanVipTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) JahanDarkColorScheme else JahanLightColorScheme

    CompositionLocalProvider(LocalVipAccents provides DefaultVipAccentColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = JahanVipTypography,
            shapes = JahanVipShapes,
            content = content
        )
    }
}
