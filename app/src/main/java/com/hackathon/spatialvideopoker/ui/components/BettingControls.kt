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
    modifier: Modifier = Modifier
) {
    val isBettingPhase = gamePhase == GameStateMachine.GamePhase.BETTING
    val isHoldingPhase = gamePhase == GameStateMachine.GamePhase.HOLDING
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // BET ONE button
        CasinoButton(
            text = "BET ONE",
            enabled = isBettingPhase,
            onClick = {
                val nextBet = if (currentBet >= 5) 1 else currentBet + 1
                onBetChange(nextBet)
            },
            color = CasinoYellow,
            textColor = Color.Black,
            modifier = Modifier.weight(1f),
            height = 40.dp
        )
        
        // SEE PAYS button
        CasinoButton(
            text = "SEE/\\nPAYS",
            enabled = true,
            onClick = onPaytableClick,
            color = Color.Gray,
            textColor = Color.Black,
            modifier = Modifier.weight(1f),
            height = 40.dp
        )
        
        // MAX BET button
        CasinoButton(
            text = "MAX BET",
            enabled = isBettingPhase,
            onClick = onMaxBet,
            color = CasinoYellow,
            textColor = Color.Black,
            modifier = Modifier.weight(1f),
            height = 40.dp
        )
        
        // DEAL/DRAW button
        CasinoButton(
            text = when (gamePhase) {
                GameStateMachine.GamePhase.BETTING -> "DEAL"
                GameStateMachine.GamePhase.HOLDING -> "DRAW"
                else -> "..."
            },
            enabled = (isBettingPhase && credits >= currentBet) || isHoldingPhase,
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
            height = 40.dp
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
                color = if (enabled) color else Color.DarkGray,
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 2.dp,
                color = if (enabled) Color.Black else Color.Black.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(enabled = enabled) {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (enabled) textColor else Color.Gray,
            fontFamily = FontFamily.SansSerif,
            lineHeight = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}