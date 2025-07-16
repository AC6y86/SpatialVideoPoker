package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.animation.core.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun AnimatedCreditDisplay(
    targetCredits: Int,
    modifier: Modifier = Modifier
) {
    var displayCredits by remember { mutableStateOf(targetCredits) }
    var previousTarget by remember { mutableStateOf(targetCredits) }
    
    LaunchedEffect(targetCredits) {
        if (targetCredits != previousTarget) {
            val difference = targetCredits - previousTarget
            val duration = when {
                kotlin.math.abs(difference) > 100 -> 2000L
                kotlin.math.abs(difference) > 50 -> 1500L
                else -> 1000L
            }
            
            val steps = 30
            val stepDelay = duration / steps
            val startValue = displayCredits
            
            for (i in 1..steps) {
                val progress = i.toFloat() / steps
                displayCredits = (startValue + (difference * progress)).toInt()
                delay(stepDelay)
            }
            
            displayCredits = targetCredits
            previousTarget = targetCredits
        }
    }
    
    // Pulse animation when credits increase
    val scale by animateFloatAsState(
        targetValue = if (displayCredits > previousTarget && displayCredits != targetCredits) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "credit_pulse"
    )
    
    Text(
        text = displayCredits.toString(),
        color = if (displayCredits > previousTarget) Color.Green else Color.Yellow,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    )
}