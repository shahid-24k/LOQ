

package echo.music.iad1tya.ui.theme

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.materialkolor.score.Score

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

val DefaultThemeColor = Color(0xFFB18CFF)

// LOQ Coherent Palette inspired by visual design reference
val LoqLavenderPrimary = Color(0xFFB18CFF)
val LoqLavenderOnPrimary = Color(0xFF1B0934)
val LoqLavenderPrimaryContainer = Color(0xFF38235B)
val LoqLavenderOnPrimaryContainer = Color(0xFFEADBFF)
val LoqLilacSecondary = Color(0xFFD3BBFF)
val LoqLilacOnSecondary = Color(0xFF220F41)
val LoqPinkTertiary = Color(0xFFF7B1D3)
val LoqPinkOnTertiary = Color(0xFF400A25)
val LoqDeepDarkBackground = Color(0xFF0E0A17)
val LoqOnBackgroundText = Color(0xFFFAF8FF)
val LoqDarkSurface = Color(0xFF140F22)
val LoqDarkSurfaceVariant = Color(0xFF221A37)
val LoqDarkSurfaceContainer = Color(0xFF1B152B)
val LoqDarkSurfaceContainerLow = Color(0xFF151022)
val LoqDarkSurfaceContainerHigh = Color(0xFF271F3E)
val LoqDarkSurfaceContainerHighest = Color(0xFF362C54)
val LoqDarkOutline = Color(0xFF4C416A)
val LoqDarkOutlineVariant = Color(0xFF2B2240)

val LoqDarkColorScheme = darkColorScheme(
    primary = LoqLavenderPrimary,
    onPrimary = LoqLavenderOnPrimary,
    primaryContainer = LoqLavenderPrimaryContainer,
    onPrimaryContainer = LoqLavenderOnPrimaryContainer,
    secondary = LoqLilacSecondary,
    onSecondary = LoqLilacOnSecondary,
    secondaryContainer = Color(0xFF342651),
    onSecondaryContainer = Color(0xFFEADBFF),
    tertiary = LoqPinkTertiary,
    onTertiary = LoqPinkOnTertiary,
    tertiaryContainer = Color(0xFF5A193B),
    onTertiaryContainer = Color(0xFFFFD8E7),
    background = LoqDeepDarkBackground,
    onBackground = LoqOnBackgroundText,
    surface = LoqDarkSurface,
    onSurface = LoqOnBackgroundText,
    surfaceVariant = LoqDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCDC5E3),
    surfaceContainer = LoqDarkSurfaceContainer,
    surfaceContainerLow = LoqDarkSurfaceContainerLow,
    surfaceContainerHigh = LoqDarkSurfaceContainerHigh,
    surfaceContainerHighest = LoqDarkSurfaceContainerHighest,
    outline = LoqDarkOutline,
    outlineVariant = LoqDarkOutlineVariant,
)

val LoqLightColorScheme = lightColorScheme(
    primary = Color(0xFF7E46D8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFECDDFE),
    onPrimaryContainer = Color(0xFF270057),
    secondary = Color(0xFF6B538C),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEDBFF),
    onSecondaryContainer = Color(0xFF261044),
    tertiary = Color(0xFF8F4264),
    onTertiary = Color.White,
    background = Color(0xFFF9F6FE),
    onBackground = Color(0xFF1B1624),
    surface = Color.White,
    onSurface = Color(0xFF1B1624),
    surfaceVariant = Color(0xFFE9E2F4),
    onSurfaceVariant = Color(0xFF4A4458),
    surfaceContainer = Color(0xFFF2EDFA),
    surfaceContainerHigh = Color(0xFFEAE3F4),
    surfaceContainerHighest = Color(0xFFE2DCED),
)

@Composable
fun echomusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit,
) {
    val baseColorScheme = if (darkTheme) {
        if (themeColor == DefaultThemeColor) {
            LoqDarkColorScheme
        } else {
            rememberDynamicColorScheme(
                seedColor = themeColor,
                isDark = true,
                specVersion = ColorSpec.SpecVersion.SPEC_2025,
                style = PaletteStyle.TonalSpot,
            ).copy(
                background = LoqDeepDarkBackground,
                surface = LoqDarkSurface,
                surfaceContainer = LoqDarkSurfaceContainer,
                surfaceContainerHigh = LoqDarkSurfaceContainerHigh,
                surfaceContainerHighest = LoqDarkSurfaceContainerHighest,
            )
        }
    } else {
        if (themeColor == DefaultThemeColor) {
            LoqLightColorScheme
        } else {
            rememberDynamicColorScheme(
                seedColor = themeColor,
                isDark = false,
                specVersion = ColorSpec.SpecVersion.SPEC_2025,
                style = PaletteStyle.TonalSpot,
            )
        }
    }

    val colorScheme = remember(baseColorScheme, pureBlack, darkTheme) {
        if (darkTheme && pureBlack) {
            baseColorScheme.pureBlack(true)
        } else {
            baseColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = androidx.compose.material3.MaterialTheme.shapes.copy(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
        ),
        content = content
    )
}

fun Bitmap.extractThemeColor(): Color {
    val colorsToPopulation = Palette.from(this)
        .maximumColorCount(8)
        .generate()
        .swatches
        .associate { it.rgb to it.population }
    val rankedColors = Score.score(colorsToPopulation)
    return Color(rankedColors.first())
}

fun Bitmap.extractGradientColors(): List<Color> {
    val extractedColors = Palette.from(this)
        .maximumColorCount(64)
        .generate()
        .swatches
        .associate { it.rgb to it.population }

    val orderedColors = Score.score(extractedColors, 2, 0xff4285f4.toInt(), true)
        .sortedByDescending { Color(it).luminance() }

    return if (orderedColors.size >= 2)
        listOf(Color(orderedColors[0]), Color(orderedColors[1]))
    else
        listOf(Color(0xFF595959), Color(0xFF0D0D0D))
}

fun ColorScheme.pureBlack(apply: Boolean) =
    if (apply) copy(
        surface = Color.Black,
        background = Color.Black
    ) else this

val ColorSaver = object : Saver<Color, Int> {
    override fun restore(value: Int): Color = Color(value)
    override fun SaverScope.save(value: Color): Int = value.toArgb()
}
