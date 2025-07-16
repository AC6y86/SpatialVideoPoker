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

@Composable
fun CreditPanel(
    credits: Int,
    bet: Int,
    win: Int,
    modifier: Modifier = Modifier
) {
    val isWinning = win > 0
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
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CREDITS ${credits.toString().padStart(4, ' ')}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CasinoYellow,
                fontFamily = FontFamily.Monospace
            )
            
            Text(
                text = "BET $bet",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CasinoYellow,
                fontFamily = FontFamily.Monospace
            )
            
            Text(
                text = "WIN ${if (win > 0) win else "0"}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isWinning) CasinoOrange.copy(alpha = winFlashingAlpha) else CasinoYellow,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}