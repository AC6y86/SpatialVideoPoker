package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.game.HandEvaluator
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun WinningAnimation(
    handRank: HandEvaluator.HandRank?,
    payout: Int,
    onAnimationComplete: () -> Unit = {}
) {
    var showAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(handRank, payout) {
        showAnimation = handRank != null && payout > 0
        if (showAnimation) {
            delay(3000) // Show for 3 seconds
            showAnimation = false
            onAnimationComplete()
        }
    }
    
    AnimatedVisibility(
        visible = showAnimation,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Background overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
            
            // Particle effects for big wins
            if (handRank != null && handRank.ordinal >= HandEvaluator.HandRank.FULL_HOUSE.ordinal) {
                ParticleEffects()
            }
            
            // Main win display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Hand name with glow effect
                Box {
                    // Glow background
                    Text(
                        text = handRank?.displayName?.uppercase() ?: "",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow,
                        modifier = Modifier
                            .alpha(0.5f)
                            .scale(1.1f)
                    )
                    
                    // Main text
                    AnimatedText(
                        text = handRank?.displayName?.uppercase() ?: "",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow
                    )
                }
                
                // Payout amount
                PayoutCountAnimation(
                    targetPayout = payout,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AnimatedText(
    text: String,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "text_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_scale"
    )
    
    Text(
        text = text,
        fontSize = fontSize,
        fontWeight = fontWeight,
        color = color,
        modifier = Modifier.scale(scale)
    )
}

@Composable
private fun PayoutCountAnimation(
    targetPayout: Int,
    modifier: Modifier = Modifier
) {
    var displayAmount by remember { mutableStateOf(0) }
    
    LaunchedEffect(targetPayout) {
        val duration = 1500L
        val steps = 30
        val stepDelay = duration / steps
        val stepAmount = targetPayout / steps
        
        for (i in 1..steps) {
            displayAmount = (stepAmount * i).coerceAtMost(targetPayout)
            delay(stepDelay)
        }
        displayAmount = targetPayout
    }
    
    Box(
        modifier = modifier
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Yellow.copy(alpha = 0.3f),
                        Color.Yellow.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Text(
            text = "WIN: $displayAmount",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ParticleEffects() {
    val particles = remember { List(20) { Particle() } }
    
    particles.forEach { particle ->
        ParticleView(particle)
    }
}

@Composable
private fun ParticleView(particle: Particle) {
    val infiniteTransition = rememberInfiniteTransition(label = "particle_${particle.id}")
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = particle.startY,
        targetValue = particle.endY,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = particle.duration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_y_${particle.id}"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = particle.duration
                0f at 0
                1f at particle.duration / 4
                1f at particle.duration * 3 / 4
                0f at particle.duration
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_alpha_${particle.id}"
    )
    
    Box(
        modifier = Modifier
            .offset(x = particle.x.dp, y = offsetY.dp)
            .size(particle.size.dp)
            .alpha(alpha)
            .background(particle.color, CircleShape)
    )
}

private data class Particle(
    val id: Int = Random.nextInt(),
    val x: Float = Random.nextFloat() * 400f - 200f,
    val startY: Float = 200f,
    val endY: Float = -200f,
    val size: Float = Random.nextFloat() * 8f + 4f,
    val duration: Int = Random.nextInt(2000, 4000),
    val color: Color = listOf(
        Color.Yellow,
        Color(0xFFFFD700),
        Color(0xFFFFA500),
        Color.White
    ).random()
)