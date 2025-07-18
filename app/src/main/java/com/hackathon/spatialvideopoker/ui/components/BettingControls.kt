package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.game.GameStateMachine
import com.hackathon.spatialvideopoker.ui.theme.*

@Composable
fun BettingControls(
    currentBet: Int,
    credits: Int,
    gamePhase: GameStateMachine.GamePhase,
    onBetChange: (Int) -> Unit,
    onMaxBet: () -> Unit,
    onDeal: () -> Unit,
    onDraw: () -> Unit,
    onPaytableClick: () -> Unit,
    onSettingsClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val isBettingPhase = gamePhase == GameStateMachine.GamePhase.BETTING
    val isHoldingPhase = gamePhase == GameStateMachine.GamePhase.HOLDING
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // BET ONE button
        CasinoButton(
            text = "BET ONE",
            enabled = enabled && isBettingPhase,
            onClick = {
                val nextBet = if (currentBet >= 5) 1 else currentBet + 1
                onBetChange(nextBet)
            },
            color = CasinoYellow,
            textColor = Color.Black,
            modifier = Modifier.weight(1f),
            height = 50.dp
        )
        
        // SEE PAYS button
        CasinoButton(
            text = "SEE PAYS",
            enabled = enabled,
            onClick = onPaytableClick,
            color = CasinoYellow,
            textColor = Color.Black,
            modifier = Modifier.weight(1f),
            height = 50.dp
        )
        
        // MAX BET button
        CasinoButton(
            text = "MAX BET",
            enabled = enabled && isBettingPhase && credits >= 5, // Only enabled if can afford max bet
            onClick = {
                onMaxBet() // Set max bet
                onDeal()   // Automatically deal
            },
            color = CasinoYellow,
            textColor = Color.Black,
            modifier = Modifier.weight(1f),
            height = 50.dp
        )
        
        // DEAL/DRAW button
        CasinoButton(
            text = when (gamePhase) {
                GameStateMachine.GamePhase.BETTING -> "DEAL"
                GameStateMachine.GamePhase.HOLDING -> "DRAW"
                else -> "..."
            },
            enabled = enabled && ((isBettingPhase && credits >= currentBet) || isHoldingPhase),
            onClick = {
                when (gamePhase) {
                    GameStateMachine.GamePhase.BETTING -> onDeal()
                    GameStateMachine.GamePhase.HOLDING -> onDraw()
                    else -> { }
                }
            },
            color = if (isHoldingPhase) Color.Red else CasinoYellow,
            textColor = if (isHoldingPhase) Color.White else Color.Black,
            modifier = Modifier.weight(1f),
            height = 50.dp
        )
    }
}

@Composable
private fun CasinoButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    color: Color,
    textColor: Color = Color.White,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .background(
                brush = if (enabled) {
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.9f),
                            color,
                            color.copy(alpha = 0.7f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(Color.DarkGray, Color.DarkGray)
                    )
                },
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = if (enabled) {
                        listOf(
                            Color.White.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    } else {
                        listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.2f))
                    }
                ),
                shape = RoundedCornerShape(6.dp)
            )
            .clickable(enabled = enabled) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // Inner shadow effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)
                .background(
                    Color.Black.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                )
        )
        
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = if (enabled) textColor else Color.Gray,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}