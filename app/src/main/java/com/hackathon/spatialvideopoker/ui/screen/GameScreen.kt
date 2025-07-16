package com.hackathon.spatialvideopoker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
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
            
            // Banner - single line message showing current game state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when {
                        gameState.gamePhase == GameStateMachine.GamePhase.DEALING -> "Dealing..."
                        gameState.gamePhase == GameStateMachine.GamePhase.HOLDING -> "Select cards to HOLD, then press DRAW"
                        gameState.gamePhase == GameStateMachine.GamePhase.DRAWING -> "Drawing..."
                        gameState.gamePhase == GameStateMachine.GamePhase.EVALUATING -> "Evaluating..."
                        gameState.gamePhase == GameStateMachine.GamePhase.PAYOUT && gameState.lastWinAmount > 0 -> 
                            gameState.lastHandRank!!.displayName.uppercase()
                        gameState.gamePhase == GameStateMachine.GamePhase.PAYOUT && gameState.lastWinAmount == 0 -> "LOSE"
                        gameState.gamePhase == GameStateMachine.GamePhase.BETTING && gameState.lastHandRank != null -> 
                            if (gameState.lastWinAmount > 0) gameState.lastHandRank!!.displayName.uppercase() else "LOSE"
                        else -> "" // No banner during initial betting phase
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PaytableText,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
            }
            
            // Card display area with win display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 8.dp)
            ) {
                // Cards - always in the same position
                CardDisplayArea(
                    cards = gameState.dealtCards,
                    heldCardIndices = gameState.heldCardIndices,
                    onCardClick = { index ->
                        viewModel.toggleHold(index)
                    },
                    enabled = gameState.gamePhase == GameStateMachine.GamePhase.HOLDING,
                    showDealBanner = gameState.gamePhase == GameStateMachine.GamePhase.BETTING && gameState.dealtCards.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                
            }
            
            // Win amount display between cards and credits - always takes same space
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    .height(40.dp), // Fixed height to prevent layout shifts
                contentAlignment = Alignment.CenterStart
            ) {
                if (gameState.lastWinAmount > 0) {
                    WinAmountDisplay(
                        amount = gameState.lastWinAmount,
                        showWin = true,
                        modifier = Modifier
                    )
                }
            }
            
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
        
        // Win animation trigger for winning hands
        LaunchedEffect(gameState.lastWinAmount, gameState.gamePhase) {
            if (gameState.lastWinAmount > 0 && gameState.gamePhase == GameStateMachine.GamePhase.PAYOUT) {
                showWinAnimation = true
                // Wait for counter animation to complete (amount * 50ms + 1 second)  
                val counterDuration = (gameState.lastWinAmount - 1) * 50L
                delay(counterDuration + 1000L) // Counter duration + 1 second
                showWinAnimation = false
                // Reset game state to show "Click Deal to start" banner
                viewModel.resetForNextHand()
            }
        }
        
        // Reset state after losing hands
        LaunchedEffect(gameState.gamePhase, gameState.lastWinAmount) {
            if (gameState.gamePhase == GameStateMachine.GamePhase.BETTING && 
                gameState.lastWinAmount == 0 && 
                gameState.dealtCards.isNotEmpty()) {
                // This is a losing hand that just finished, reset to show banner
                viewModel.resetForNextHand()
            }
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