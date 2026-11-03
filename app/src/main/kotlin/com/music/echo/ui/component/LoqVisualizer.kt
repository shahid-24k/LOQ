package echo.music.iad1tya.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import echo.music.iad1tya.performance.PerformanceProfile
import echo.music.iad1tya.performance.currentPerformanceProfile

/**
 * Modern LOQ visualizer component with automatic performance adaptation:
 * - In LITE mode: displays static visualizer bars with zero CPU/recomposition overhead.
 * - In BALANCED / FULL mode: renders smooth, interactive animated equalizer bars.
 */
@Composable
fun LoqAudioVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 4,
    barWidth: Dp = 3.dp,
    maxBarHeight: Dp = 16.dp,
    minBarHeight: Dp = 4.dp,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    val profile = currentPerformanceProfile()
    val isLite = profile == PerformanceProfile.LITE

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        if (isLite || !isPlaying) {
            // Static fallback for low-RAM devices or paused state
            val staticHeights = listOf(0.4f, 0.8f, 0.6f, 1.0f)
            repeat(barCount) { index ->
                val fraction = staticHeights.getOrElse(index % staticHeights.size) { 0.5f }
                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(minBarHeight + (maxBarHeight - minBarHeight) * fraction)
                        .clip(RoundedCornerShape(barWidth / 2))
                        .background(accentColor)
                )
            }
        } else {
            // Animated visualizer for BALANCED and FULL modes
            val transition = rememberInfiniteTransition(label = "visualizer")
            val animDuration = listOf(450, 600, 350, 500)

            repeat(barCount) { index ->
                val duration = animDuration.getOrElse(index % animDuration.size) { 500 }
                val heightFraction by transition.animateFloat(
                    initialValue = 0.2f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = duration, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "bar_$index"
                )

                Box(
                    modifier = Modifier
                        .width(barWidth)
                        .height(minBarHeight + (maxBarHeight - minBarHeight) * heightFraction)
                        .clip(RoundedCornerShape(barWidth / 2))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accentColor,
                                    accentColor.copy(alpha = 0.65f)
                                )
                            )
                        )
                )
            }
        }
    }
}

