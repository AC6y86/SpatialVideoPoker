package com.hackathon.spatialvideopoker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hackathon.spatialvideopoker.game.GameStateMachine
import com.hackathon.spatialvideopoker.ui.components.*
import com.hackathon.spatialvideopoker.ui.theme.*
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
            .background(CasinoBlue)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Integrated Paytable (always visible at top)
            IntegratedPaytable(
                currentBet = gameState.currentBet,
                lastWinHandRank = if (gameState.gamePhase == GameStateMachine.GamePhase.PAYOUT) gameState.lastHandRank else null,
                isWinning = gameState.gamePhase == GameStateMachine.GamePhase.PAYOUT && gameState.lastWinAmount > 0,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Game message - show as banner when in betting phase
            if (gameState.gamePhase == GameStateMachine.GamePhase.BETTING && gameState.dealtCards.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gameState.message,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PaytableText,
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            
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
                    .padding(vertical = 8.dp)
            )
            
            // Combined credit display and controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Credit Panel
                CreditPanel(
                    credits = gameState.credits,
                    bet = gameState.currentBet,
                    win = gameState.lastWinAmount,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Betting controls
                BettingControls(
                    currentBet = gameState.currentBet,
                    credits = gameState.credits,
                    gamePhase = gameState.gamePhase,
                    onBetChange = { bet -> viewModel.setBet(bet) },
                    onMaxBet = { viewModel.maxBet() },
                    onDeal = { viewModel.deal() },
                    onDraw = { viewModel.draw() },
                    onPaytableClick = { viewModel.togglePaytable() },
                    onSettingsClick = { viewModel.toggleSettings() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
        
        // Paytable overlay
        if (gameState.showPaytable) {
            PaytableScreen(
                onDismiss = { viewModel.togglePaytable() }
            )
        }
        
        // Settings overlay
        if (gameState.showSettings) {
            SettingsScreen(
                currentSettings = gameState.gameSettings,
                onSettingsChanged = { newSettings ->
                    viewModel.updateSettings(newSettings)
                },
                onDismiss = { viewModel.toggleSettings() }
            )
        }
    }
}