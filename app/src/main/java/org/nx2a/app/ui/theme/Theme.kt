package org.nx2a.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.nx2a.app.R

val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk, FontWeight.Normal),
    Font(R.font.space_grotesk, FontWeight.Medium),
    Font(R.font.space_grotesk, FontWeight.Bold)
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

val NxCyan = Color(0xFF00E5FF)
val NxCrimson = Color(0xFFFF2E54)
val NxEmerald = Color(0xFF00E676)
val NxAmber = Color(0xFFFFAB00)
val NxSlateBg = Color(0xFF0B0F17)
val NxSurface = Color(0xFF141B26)
val NxSurfaceVariant = Color(0xFF1E293B)
val NxOutline = Color(0xFF334155)
val NxTextPrimary = Color(0xFFF8FAFC)
val NxTextSecondary = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = NxCyan,
    onPrimary = Color(0xFF001F24),
    primaryContainer = Color(0xFF004E59),
    onPrimaryContainer = Color(0xFFB3F7FF),
    secondary = NxCrimson,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF5A0A1B),
    onSecondaryContainer = Color(0xFFFFD9DE),
    tertiary = NxEmerald,
    onTertiary = Color(0xFF00210B),
    background = NxSlateBg,
    onBackground = NxTextPrimary,
    surface = NxSurface,
    onSurface = NxTextPrimary,
    surfaceVariant = NxSurfaceVariant,
    onSurfaceVariant = NxTextSecondary,
    outline = NxOutline,
    error = Color(0xFFFF5252)
)

val NxTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

@Composable
fun Nx2AndroidTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = NxTypography,
        content = content
    )
}
