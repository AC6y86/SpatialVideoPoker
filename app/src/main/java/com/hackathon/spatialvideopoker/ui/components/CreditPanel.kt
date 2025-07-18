package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CreditPanel(
    credits: Int,
    bet: Int,
    win: Int,
    modifier: Modifier = Modifier
) {
    val isWinning = win > 0
    var displayWinAmount by remember { mutableStateOf(1) }
    var showWinDisplay by remember { mutableStateOf(false) }
    var displayCredits by remember { mutableStateOf(credits) }
    
    // Simple WIN animation that works
    LaunchedEffect(win) {
        if (win > 0) {
            showWinDisplay = true
            displayWinAmount = 1
            val steps = win - 1
            
            if (steps > 0) {
                for (i in 1..steps) {
                    delay(100L) // 10 per second
                    displayWinAmount = i + 1
                }
            }
        } else {
            showWinDisplay = false
        }
    }
    
    // Simple credits update (no animation for now)
    LaunchedEffect(credits) {
        displayCredits = credits
    }
    
    val winFlashingAlpha by rememberInfiniteTransition(label = "win_flash").animateFloat(
        initialValue = if (isWinning) 0.5f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "win_alpha"
    )
    
    Box(
        modifier = modifier
            .background(Color.Black)
            .border(2.dp, CasinoYellow)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // CREDITS - fixed left position
            Text(
                text = "CREDITS ${displayCredits.toString().padStart(4, ' ')}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CasinoYellow,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.weight(1f)
            )
            
            // BET - fixed center position
            Text(
                text = "BET $bet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CasinoYellow,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            
            // WIN - fixed right position (always takes space)
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (showWinDisplay) {
                    Text(
                        text = "WIN $displayWinAmount",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CasinoOrange.copy(alpha = winFlashingAlpha),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}