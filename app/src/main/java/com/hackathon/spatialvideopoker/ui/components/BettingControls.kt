package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.game.GameStateMachine

@Composable
fun BettingControls(
    currentBet: Int,
    credits: Int,
    gamePhase: GameStateMachine.GamePhase,
    onBetChange: (Int) -> Unit,
    onMaxBet: () -> Unit,
    onDeal: () -> Unit,
    onDraw: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBettingPhase = gamePhase == GameStateMachine.GamePhase.BETTING
    val isHoldingPhase = gamePhase == GameStateMachine.GamePhase.HOLDING
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bet buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Bet 1-5 buttons
            for (bet in 1..5) {
                BetButton(
                    bet = bet,
                    isSelected = currentBet == bet,
                    enabled = isBettingPhase && credits >= bet,
                    onClick = { onBetChange(bet) },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Max Bet button
            Button(
                onClick = onMaxBet,
                enabled = isBettingPhase && credits >= 5,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B6B),
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .height(56.dp)
            ) {
                Text(
                    text = "MAX\nBET",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 16.sp
                )
            }
        }
        
        // Deal/Draw button
        Button(
            onClick = {
                when (gamePhase) {
                    GameStateMachine.GamePhase.BETTING -> onDeal()
                    GameStateMachine.GamePhase.HOLDING -> onDraw()
                    else -> { }
                }
            },
            enabled = (isBettingPhase && credits >= currentBet) || isHoldingPhase,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4ECDC4),
                disabledContainerColor = Color.Gray
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Text(
                text = when (gamePhase) {
                    GameStateMachine.GamePhase.BETTING -> "DEAL"
                    GameStateMachine.GamePhase.HOLDING -> "DRAW"
                    else -> "..."
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BetButton(
    bet: Int,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFFFFD93D) else Color(0xFF6C757D),
            disabledContainerColor = Color.Gray
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(56.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BET",
                fontSize = 12.sp,
                color = if (isSelected) Color.Black else Color.White
            )
            Text(
                text = bet.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.Black else Color.White
            )
        }
    }
}