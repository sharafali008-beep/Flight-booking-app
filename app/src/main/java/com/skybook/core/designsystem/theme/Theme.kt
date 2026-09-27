package com.skybook.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Blue800,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    secondary = Orange500,
    onSecondary = Color.White,
    secondaryContainer = Orange100,
    onSecondaryContainer = Color(0xFF7C2D12),
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Slate100,
    outline = Slate400,
    outlineVariant = Slate200,
    error = Red600,
)

private val DarkColors = darkColorScheme(
    primary = Blue300,
    onPrimary = Blue900,
    primaryContainer = Blue800,
    onPrimaryContainer = Blue100,
    secondary = Orange400,
    onSecondary = Color(0xFF431407),
    secondaryContainer = Color(0xFF7C2D12),
    onSecondaryContainer = Orange100,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate400,
    surfaceContainer = Slate900,
    surfaceContainerLow = Slate900,
    surfaceContainerHigh = Slate800,
    outline = Slate500,
    outlineVariant = Slate700,
    error = Red400,
)

/** Extra colours Material 3 doesn't have a slot for. */
@Immutable
data class ExtendedColors(
    val price: Color,
    val success: Color,
    val seatAvailable: Color,
    val seatBooked: Color,
    val seatPremium: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(Orange500, Green600, Slate200, Slate400, Orange100)
}

/** Spacing scale from the design guide: 4 / 8 / 12 / 16 / 24 dp. */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
}

private val AppShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
)

@Composable
fun SkyBookTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val extended = if (darkTheme) {
        ExtendedColors(Orange400, Green400, Slate700, Slate800, Color(0xFF7C2D12))
    } else {
        ExtendedColors(Orange500, Green600, Slate100, Slate400, Orange100)
    }
    androidx.compose.runtime.CompositionLocalProvider(LocalExtendedColors provides extended) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = AppTypography,
            shapes = AppShapes,
            content = content
        )
    }
}

/** Shortcut: `MaterialTheme.extended.price` */
val MaterialTheme.extended: ExtendedColors
    @Composable get() = LocalExtendedColors.current
