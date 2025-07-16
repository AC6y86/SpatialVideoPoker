package com.hackathon.spatialvideopoker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathon.spatialvideopoker.game.GameStateMachine
import com.hackathon.spatialvideopoker.ui.components.*
import com.hackathon.spatialvideopoker.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val gameState by viewModel.gameState.collectAsState()
    var showWinAnimation by remember { mutableStateOf(false) }
    
    // Trigger win animation when payout phase starts with a win
    LaunchedEffect(gameState.gamePhase, gameState.lastWinAmount) {
        if (gameState.gamePhase == GameStateMachine.GamePhase.PAYOUT && gameState.lastWinAmount > 0) {
            showWinAnimation = true
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B5D1E)) // Casino green background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top information bar
            TopInfoBar(
                credits = gameState.credits,
                currentBet = gameState.currentBet,
                lastWin = gameState.lastWinAmount,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Game message
            GameMessage(
                message = gameState.message,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            
            // Card display area
            CardDisplayArea(
                cards = gameState.dealtCards,
                heldCardIndices = gameState.heldCardIndices,
                onCardClick = { index ->
                    viewModel.toggleHold(index)
                },
                enabled = gameState.gamePhase == GameStateMachine.GamePhase.HOLDING,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            
            // Last hand result (if any)
            gameState.lastHandRank?.let { handRank ->
                HandResultDisplay(
                    handRank = handRank,
                    payout = gameState.lastWinAmount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            
            // Betting controls
            BettingControls(
                currentBet = gameState.currentBet,
                credits = gameState.credits,
                gamePhase = gameState.gamePhase,
                onBetChange = { bet -> viewModel.setBet(bet) },
                onMaxBet = { viewModel.maxBet() },
                onDeal = { viewModel.deal() },
                onDraw = { viewModel.draw() },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Winning animation overlay
        if (showWinAnimation) {
            WinningAnimation(
                handRank = gameState.lastHandRank,
                payout = gameState.lastWinAmount,
                onAnimationComplete = {
                    showWinAnimation = false
                }
            )
        }
    }
}