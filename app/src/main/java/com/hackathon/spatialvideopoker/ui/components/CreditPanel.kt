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
    var preWinCredits by remember { mutableStateOf(credits) }
    var lastWinAmount by remember { mutableStateOf(0) }
    
    // Keep displayCredits in sync with credits when not animating
    LaunchedEffect(credits) {
        if (win == 0) {
            displayCredits = credits
        }
    }
    
    // Synchronized WIN and Credits animation
    LaunchedEffect(win) {
        if (win > 0 && win != lastWinAmount) {
            // Capture current credits before any updates from CreditManager
            preWinCredits = displayCredits
            showWinDisplay = true
            val steps = win - 1
            
            // Both animations start with initial values
            displayWinAmount = 1
            displayCredits = preWinCredits
            
            if (steps > 0) {
                for (i in 1..steps) {
                    delay(100L)
                    val progress = i.toFloat() / steps.toFloat()
                    
                    // Update both simultaneously - credits animate from preWin to preWin+win
                    displayWinAmount = i + 1
                    displayCredits = (preWinCredits + (win * progress)).toInt()
                }
            }
            
            // Ensure final values are exact
            displayWinAmount = win
            displayCredits = preWinCredits + win
            lastWinAmount = win
        } else if (win == 0) {
            showWinDisplay = false
            displayCredits = credits
            lastWinAmount = 0
        } else {
            // Non-animation update (like game reset)
            displayCredits = credits
        }
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