package echo.music.iad1tya.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlin.math.sin

/**
 * Checks if the entered name matches the secret easter egg name variations (Bena / Benaser).
 * Case-insensitive and tolerates surrounding whitespace.
 */
fun isEasterEggName(name: String): Boolean {
    val normalized = name.trim().lowercase()
    return normalized == "bena" || normalized == "benaser"
}

private data class FloatingParticle(
    val initialXOffset: Dp,
    val initialYOffset: Dp,
    val driftX: Dp,
    val floatDistance: Dp,
    val fontSize: Int,
    val durationMillis: Int,
    val delayMillis: Int,
    val emoji: String
)

@Composable
fun LoqEasterEggDialog(
    onContinue: () -> Unit
) {
    Dialog(
        onDismissRequest = onContinue,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "HlbkTransition")

        // Heartbeat pulse for the central white heart
        val heartScale by infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "heartPulse"
        )

        // Romantic glow intensity behind the heart
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "heartGlow"
        )

        // Text pulse for "I LOVE YOU ❤️"
        val textScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "textPulse"
        )

        // Floating hearts, roses, and rose petals
        val particles = remember {
            listOf(
                // Hearts
                FloatingParticle((-120).dp, 40.dp, (-25).dp, 190.dp, 24, 2600, 0, "❤️"),
                FloatingParticle(100.dp, 60.dp, 28.dp, 200.dp, 28, 2900, 400, "💖"),
                FloatingParticle((-60).dp, 90.dp, (-18).dp, 210.dp, 22, 2400, 800, "💕"),
                FloatingParticle(70.dp, 110.dp, 22.dp, 190.dp, 26, 2700, 1200, "❤️"),
                FloatingParticle((-140).dp, 120.dp, (-30).dp, 220.dp, 20, 3100, 1600, "💗"),
                FloatingParticle(130.dp, 100.dp, 24.dp, 195.dp, 22, 2500, 600, "💓"),
                FloatingParticle((-30).dp, 140.dp, (-15).dp, 230.dp, 24, 2800, 1000, "❤️"),
                FloatingParticle(40.dp, 130.dp, 16.dp, 210.dp, 20, 2600, 1400, "💖"),
                // Roses
                FloatingParticle((-90).dp, (-10).dp, (-20).dp, 170.dp, 24, 2500, 300, "🌹"),
                FloatingParticle(85.dp, 10.dp, 20.dp, 180.dp, 26, 2800, 700, "🌹"),
                FloatingParticle(0.dp, 130.dp, 14.dp, 225.dp, 30, 3000, 1100, "🌹"),
                FloatingParticle((-110).dp, 160.dp, (-22).dp, 240.dp, 22, 3200, 1500, "🌹"),
                FloatingParticle(115.dp, 150.dp, 25.dp, 230.dp, 24, 2900, 900, "🌹"),
                // Rose Petals & Flowers
                FloatingParticle((-50).dp, 40.dp, (-12).dp, 160.dp, 18, 2200, 200, "🌸"),
                FloatingParticle(55.dp, 30.dp, 14.dp, 175.dp, 20, 2300, 500, "🌸"),
                FloatingParticle((-80).dp, 100.dp, (-16).dp, 200.dp, 22, 2700, 1300, "🌸"),
                FloatingParticle(95.dp, 80.dp, 18.dp, 185.dp, 18, 2400, 1700, "🌸"),
                FloatingParticle((-20).dp, 80.dp, (-10).dp, 190.dp, 16, 2100, 100, "🌸"),
                FloatingParticle(30.dp, 70.dp, 12.dp, 180.dp, 18, 2300, 900, "🌸"),
                FloatingParticle((-130).dp, 60.dp, (-26).dp, 210.dp, 20, 3300, 1100, "🌸"),
                FloatingParticle(120.dp, 40.dp, 22.dp, 195.dp, 22, 2800, 1500, "🌸")
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF07050A)),
            contentAlignment = Alignment.Center
        ) {
            // Ambient romantic deep glow
            Box(
                modifier = Modifier
                    .size(420.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x35E63956),
                                Color(0x18FF758F),
                                Color(0x08FFB3C1),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Floating particles layer (hearts, roses, petals)
            Box(
                modifier = Modifier.size(380.dp),
                contentAlignment = Alignment.Center
            ) {
                particles.forEachIndexed { index, particle ->
                    val animProgress by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(
                                durationMillis = particle.durationMillis,
                                delayMillis = particle.delayMillis,
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "particle_$index"
                    )

                    val yOffset = particle.initialYOffset - (particle.floatDistance * animProgress)
                    val xDrift = particle.initialXOffset + (particle.driftX * sin(animProgress * Math.PI.toFloat()))
                    val alpha = when {
                        animProgress < 0.12f -> animProgress / 0.12f
                        animProgress > 0.72f -> (1f - animProgress) / 0.28f
                        else -> 1f
                    }
                    val pScale = 0.6f + (animProgress * 0.5f)

                    Text(
                        text = particle.emoji,
                        fontSize = particle.fontSize.sp,
                        modifier = Modifier
                            .offset(x = xDrift, y = yOffset)
                            .alpha(alpha.coerceIn(0f, 1f))
                            .scale(pScale)
                    )
                }
            }

            // Central content layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Center section: Shiny White Heart with HLBK and Love Message
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Large Shiny White Heart container
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .scale(heartScale),
                        contentAlignment = Alignment.Center
                    ) {
                        // Soft glow behind the white heart
                        Box(
                            modifier = Modifier
                                .size(230.dp)
                                .alpha(glowAlpha)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.40f),
                                            Color(0xFFFF758F).copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        // Shiny White Heart Canvas
                        Canvas(
                            modifier = Modifier.size(200.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            val heartPath = Path().apply {
                                moveTo(w * 0.50f, h * 0.88f)
                                // Left half curve
                                cubicTo(
                                    w * 0.04f, h * 0.60f,
                                    0f, h * 0.25f,
                                    w * 0.26f, h * 0.12f
                                )
                                cubicTo(
                                    w * 0.42f, h * 0.05f,
                                    w * 0.50f, h * 0.22f,
                                    w * 0.50f, h * 0.30f
                                )
                                // Right half curve
                                cubicTo(
                                    w * 0.50f, h * 0.22f,
                                    w * 0.58f, h * 0.05f,
                                    w * 0.74f, h * 0.12f
                                )
                                cubicTo(
                                    w, h * 0.25f,
                                    w * 0.96f, h * 0.60f,
                                    w * 0.50f, h * 0.88f
                                )
                                close()
                            }

                            // Base shiny pearlescent white gradient
                            drawPath(
                                path = heartPath,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFF8F9FA),
                                        Color(0xFFECEFF4)
                                    ),
                                    start = Offset(w * 0.2f, 0f),
                                    end = Offset(w * 0.8f, h)
                                )
                            )

                            // Subtle specular highlight gloss across upper left lobe
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.85f),
                                        Color.White.copy(alpha = 0f)
                                    ),
                                    center = Offset(w * 0.32f, h * 0.28f),
                                    radius = w * 0.25f
                                ),
                                center = Offset(w * 0.32f, h * 0.28f),
                                radius = w * 0.25f
                            )
                        }

                        // "HLBK" prominently written on the heart
                        Text(
                            text = "HLBK",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp,
                                color = Color(0xFF140D16)
                            ),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(y = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // "I LOVE YOU ❤️"
                    Text(
                        text = "I LOVE YOU ❤️",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.5.sp,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.scale(textScale)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Forever & Always",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp,
                            color = Color.White.copy(alpha = 0.70f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // Continue button → normal LOQ Home (no LOQ symbol or branding inside easter egg)
                Button(
                    onClick = onContinue,
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF09090D)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .height(52.dp)
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFF09090D)
                        )
                    )
                }
            }
        }
    }
}
