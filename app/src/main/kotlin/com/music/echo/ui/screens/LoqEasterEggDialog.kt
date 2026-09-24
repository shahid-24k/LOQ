package echo.music.iad1tya.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import echo.music.iad1tya.R
import kotlin.math.sin

/**
 * Checks if the entered name matches the secret easter egg name variations (Bena / Benaser).
 */
fun isEasterEggName(name: String): Boolean {
    val normalized = name.trim().lowercase().replace(Regex("[\\s._\\-]+"), "")
    return normalized == "bena" || normalized == "benaser"
}

private data class HeartParticle(
    val initialXOffset: Dp,
    val initialYOffset: Dp,
    val driftX: Dp,
    val floatDistance: Dp,
    val fontSize: Int,
    val durationMillis: Int,
    val delayMillis: Int,
    val emoji: String = "❤️"
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
        val infiniteTransition = rememberInfiniteTransition(label = "EasterEggTransition")

        // Subtle glow & pulse animation around the LOQ logo
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logoGlow"
        )

        val logoScale by infiniteTransition.animateFloat(
            initialValue = 0.98f,
            targetValue = 1.02f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "logoScale"
        )

        // Text gentle pulse
        val textScale by infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "textPulse"
        )

        // Staggered floating heart particles configuration
        val hearts = remember {
            listOf(
                HeartParticle(initialXOffset = (-110).dp, initialYOffset = 20.dp, driftX = (-20).dp, floatDistance = 160.dp, fontSize = 22, durationMillis = 2400, delayMillis = 0),
                HeartParticle(initialXOffset = 90.dp, initialYOffset = 40.dp, driftX = 25.dp, floatDistance = 180.dp, fontSize = 28, durationMillis = 2800, delayMillis = 400),
                HeartParticle(initialXOffset = (-50).dp, initialYOffset = 70.dp, driftX = (-15).dp, floatDistance = 190.dp, fontSize = 18, durationMillis = 2200, delayMillis = 800),
                HeartParticle(initialXOffset = 60.dp, initialYOffset = 80.dp, driftX = 18.dp, floatDistance = 170.dp, fontSize = 24, durationMillis = 2600, delayMillis = 1200),
                HeartParticle(initialXOffset = (-130).dp, initialYOffset = 100.dp, driftX = (-30).dp, floatDistance = 200.dp, fontSize = 26, durationMillis = 3000, delayMillis = 1600),
                HeartParticle(initialXOffset = 120.dp, initialYOffset = 90.dp, driftX = 20.dp, floatDistance = 175.dp, fontSize = 20, durationMillis = 2500, delayMillis = 600),
                HeartParticle(initialXOffset = 0.dp, initialYOffset = 110.dp, driftX = 10.dp, floatDistance = 210.dp, fontSize = 32, durationMillis = 2900, delayMillis = 1000, emoji = "💖"),
                HeartParticle(initialXOffset = (-80).dp, initialYOffset = (-30).dp, driftX = (-18).dp, floatDistance = 150.dp, fontSize = 20, durationMillis = 2300, delayMillis = 1400)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF09090D)),
            contentAlignment = Alignment.Center
        ) {
            // Ambient soft background glow
            Box(
                modifier = Modifier
                    .size(340.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0x33FF4D6D),
                                Color(0x15FFFFFF),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )

            // Floating hearts around the center area
            Box(
                modifier = Modifier
                    .size(320.dp),
                contentAlignment = Alignment.Center
            ) {
                hearts.forEachIndexed { index, particle ->
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
                        label = "heart_$index"
                    )

                    // Calculate trajectory and fade
                    val yOffset = particle.initialYOffset - (particle.floatDistance * animProgress)
                    val xDrift = particle.initialXOffset + (particle.driftX * sin(animProgress * Math.PI.toFloat()))
                    val alpha = when {
                        animProgress < 0.2f -> animProgress / 0.2f
                        animProgress > 0.7f -> (1f - animProgress) / 0.3f
                        else -> 1f
                    }
                    val scale = 0.6f + (animProgress * 0.5f)

                    Text(
                        text = particle.emoji,
                        fontSize = particle.fontSize.sp,
                        modifier = Modifier
                            .offset(x = xDrift, y = yOffset)
                            .alpha(alpha.coerceIn(0f, 1f))
                            .scale(scale)
                    )
                }
            }

            // Central Column with Logo, Text, and Continue Button
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top placeholder to balance space
                Spacer(modifier = Modifier.height(24.dp))

                // Middle section: Logo and Heart Message
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo with luminous glow
                    Box(
                        modifier = Modifier
                            .size(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Halo behind logo
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .scale(logoScale)
                                .alpha(glowAlpha)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.25f),
                                            Color(0xFFFF758F).copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        // Exact supplied transparent white LOQ logo
                        Image(
                            painter = painterResource(R.drawable.loq_logo_white_transparent),
                            contentDescription = "LOQ Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(150.dp)
                                .scale(logoScale)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // "I LOVE YOU ❤️" message
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
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.5.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // Bottom section: "Continue to LOQ" button
                Button(
                    onClick = onContinue,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF09090D)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.68f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Continue to LOQ",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = Color(0xFF09090D)
                        )
                    )
                }
            }
        }
    }
}
