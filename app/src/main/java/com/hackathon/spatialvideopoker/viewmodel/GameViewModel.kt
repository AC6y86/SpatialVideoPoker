package com.hackathon.spatialvideopoker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hackathon.spatialvideopoker.audio.SoundManager
import com.hackathon.spatialvideopoker.data.GameSettings
import com.hackathon.spatialvideopoker.data.SettingsManager
import com.hackathon.spatialvideopoker.data.VideoPokerDatabase
import com.hackathon.spatialvideopoker.data.dao.GameStateDao
import com.hackathon.spatialvideopoker.data.dao.StatisticsDao
import com.hackathon.spatialvideopoker.game.*
import com.hackathon.spatialvideopoker.model.Card
import com.hackathon.spatialvideopoker.model.Deck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class GameViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database by lazy { VideoPokerDatabase.getDatabase(application) }
    private val gameStateDao: GameStateDao by lazy { database.gameStateDao() }
    private val statisticsDao: StatisticsDao by lazy { database.statisticsDao() }
    
    private val deck = Deck()
    private val handEvaluator = HandEvaluator()
    private val payoutCalculator = PayoutCalculator()
    private val bettingManager = BettingManager()
    private val gameStateMachine = GameStateMachine()
    private val creditManager by lazy { CreditManager(gameStateDao) }
    
    private val soundManager = SoundManager(application)
    private val settingsManager = SettingsManager(application)
    private var currentSettings = settingsManager.loadSettings()
    
    private val _gameState = MutableStateFlow(GameUiState())
    val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()
    
    data class GameUiState(
        val credits: Int = 1000,
        val currentBet: Int = 1,
        val dealtCards: List<Card> = emptyList(),
        val heldCardIndices: Set<Int> = emptySet(),
        val gamePhase: GameStateMachine.GamePhase = GameStateMachine.GamePhase.BETTING,
        val lastHandRank: HandEvaluator.HandRank? = null,
        val lastWinAmount: Int = 0,
        val isDealing: Boolean = false,
        val isDrawing: Boolean = false,
        val message: String = "Place your bet and press DEAL",
        val showPaytable: Boolean = false,
        val showSettings: Boolean = false,
        val gameSettings: GameSettings = GameSettings()
    )
    
    init {
        // Start with default credits, load from database later
        _gameState.update { it.copy(
            credits = 1000,
            gameSettings = currentSettings
        ) }
        
        // Apply initial settings
        soundManager.setSoundEnabled(currentSettings.soundEnabled)
        soundManager.setMusicEnabled(currentSettings.musicEnabled)
        soundManager.setSoundVolume(currentSettings.soundVolume)
        soundManager.setMusicVolume(currentSettings.musicVolume)
    }
    
    fun deal() {
        if (!gameStateMachine.canDeal()) return
        
        viewModelScope.launch {
            val bet = bettingManager.currentBet
            if (!creditManager.canAffordBet(bet)) {
                updateMessage("Insufficient credits")
                return@launch
            }
            
            // Play button sound
            soundManager.playSound(SoundManager.SoundEffect.BUTTON_CLICK)
            
            // Deduct bet
            creditManager.deductBet(bet)
            gameStateDao.incrementGamesPlayed()
            
            // Transition to dealing phase
            gameStateMachine.transitionTo(GameStateMachine.GamePhase.DEALING)
            _gameState.update { 
                it.copy(
                    gamePhase = GameStateMachine.GamePhase.DEALING,
                    isDealing = true,
                    heldCardIndices = emptySet(),
                    lastHandRank = null,
                    lastWinAmount = 0,
                    message = "Dealing..."
                )
            }
            
            // Shuffle and deal
            deck.reset()
            deck.shuffle()
            val dealtCards = deck.deal(5)
            
            // Play dealing sound
            soundManager.playSound(SoundManager.SoundEffect.CARD_DEAL)
            
            // Animate dealing with adjusted speed based on settings
            delay((500 * currentSettings.gameSpeed.delayMultiplier).toLong())
            
            _gameState.update { 
                it.copy(
                    dealtCards = dealtCards,
                    isDealing = false
                )
            }
            
            // Transition to holding phase
            gameStateMachine.transitionTo(GameStateMachine.GamePhase.HOLDING)
            _gameState.update { 
                it.copy(
                    gamePhase = GameStateMachine.GamePhase.HOLDING,
                    message = "Select cards to HOLD, then press DRAW"
                )
            }
        }
    }
    
    fun toggleHold(cardIndex: Int) {
        if (!gameStateMachine.canHoldCards()) return
        
        soundManager.playSound(SoundManager.SoundEffect.CARD_FLIP)
        
        _gameState.update { state ->
            val newHeldIndices = if (cardIndex in state.heldCardIndices) {
                state.heldCardIndices - cardIndex
            } else {
                state.heldCardIndices + cardIndex
            }
            state.copy(heldCardIndices = newHeldIndices)
        }
    }
    
    fun draw() {
        if (!gameStateMachine.canDraw()) return
        
        viewModelScope.launch {
            // Transition to drawing phase
            gameStateMachine.transitionTo(GameStateMachine.GamePhase.DRAWING)
            _gameState.update { 
                it.copy(
                    gamePhase = GameStateMachine.GamePhase.DRAWING,
                    isDrawing = true,
                    message = "Drawing..."
                )
            }
            
            // Replace non-held cards
            val currentCards = _gameState.value.dealtCards.toMutableList()
            val heldIndices = _gameState.value.heldCardIndices
            
            for (i in currentCards.indices) {
                if (i !in heldIndices) {
                    val newCards = deck.deal(1)
                    if (newCards.isNotEmpty()) {
                        currentCards[i] = newCards[0]
                    }
                }
            }
            
            // Animate drawing
            delay(500) // Simulate drawing animation
            
            _gameState.update { 
                it.copy(
                    dealtCards = currentCards,
                    isDrawing = false
                )
            }
            
            // Evaluate hand
            gameStateMachine.transitionTo(GameStateMachine.GamePhase.EVALUATING)
            evaluateHand(currentCards)
        }
    }
    
    private suspend fun evaluateHand(cards: List<Card>) {
        val handRank = handEvaluator.evaluateHand(cards)
        val payout = payoutCalculator.calculatePayout(handRank, bettingManager.currentBet)
        
        _gameState.update { 
            it.copy(
                gamePhase = GameStateMachine.GamePhase.EVALUATING,
                lastHandRank = handRank,
                lastWinAmount = payout
            )
        }
        
        // Record statistics
        if (handRank != HandEvaluator.HandRank.HIGH_CARD) {
            statisticsDao.recordHand(handRank.name, payout)
        }
        
        // Process payout
        gameStateMachine.transitionTo(GameStateMachine.GamePhase.PAYOUT)
        processPayout(handRank, payout)
    }
    
    private suspend fun processPayout(handRank: HandEvaluator.HandRank, payout: Int) {
        _gameState.update { 
            it.copy(gamePhase = GameStateMachine.GamePhase.PAYOUT)
        }
        
        if (payout > 0) {
            // Play win sound based on hand rank
            if (handRank.ordinal >= HandEvaluator.HandRank.FULL_HOUSE.ordinal) {
                soundManager.playSound(SoundManager.SoundEffect.WIN_BIG)
            } else {
                soundManager.playSound(SoundManager.SoundEffect.WIN_SMALL)
            }
            
            creditManager.addWinnings(payout)
            updateMessage("${handRank.displayName}! Win: $payout credits")
            delay((2000 * currentSettings.gameSpeed.delayMultiplier).toLong())
        } else {
            updateMessage("No win. Try again!")
            delay((1000 * currentSettings.gameSpeed.delayMultiplier).toLong())
        }
        
        // Return to betting phase
        gameStateMachine.transitionTo(GameStateMachine.GamePhase.BETTING)
        _gameState.update { 
            it.copy(
                gamePhase = GameStateMachine.GamePhase.BETTING,
                message = "Place your bet and press DEAL"
            )
        }
        
        updateCreditsInState()
    }
    
    fun setBet(amount: Int) {
        if (!gameStateMachine.canChangeBet()) return
        
        val credits = creditManager.getCurrentCredits()
        if (bettingManager.placeBet(amount, credits)) {
            soundManager.playSound(SoundManager.SoundEffect.COIN_INSERT)
            _gameState.update { 
                it.copy(currentBet = amount)
            }
            viewModelScope.launch {
                gameStateDao.updateCurrentBet(amount)
            }
        }
    }
    
    fun incrementBet() {
        if (!gameStateMachine.canChangeBet()) return
        
        val credits = creditManager.getCurrentCredits()
        if (bettingManager.incrementBet(credits)) {
            soundManager.playSound(SoundManager.SoundEffect.COIN_INSERT)
            _gameState.update { 
                it.copy(currentBet = bettingManager.currentBet)
            }
            viewModelScope.launch {
                gameStateDao.updateCurrentBet(bettingManager.currentBet)
            }
        }
    }
    
    fun maxBet() {
        if (!gameStateMachine.canChangeBet()) return
        
        val credits = creditManager.getCurrentCredits()
        if (bettingManager.maxBet(credits)) {
            soundManager.playSound(SoundManager.SoundEffect.COIN_INSERT)
            _gameState.update { 
                it.copy(currentBet = bettingManager.currentBet)
            }
            viewModelScope.launch {
                gameStateDao.updateCurrentBet(bettingManager.currentBet)
            }
        }
    }
    
    private suspend fun updateCreditsInState() {
        val credits = creditManager.getCurrentCredits()
        _gameState.update { it.copy(credits = credits) }
    }
    
    private fun updateMessage(message: String) {
        _gameState.update { it.copy(message = message) }
    }
    
    fun togglePaytable() {
        soundManager.playSound(SoundManager.SoundEffect.BUTTON_CLICK)
        _gameState.update { it.copy(showPaytable = !it.showPaytable) }
    }
    
    fun toggleSettings() {
        soundManager.playSound(SoundManager.SoundEffect.BUTTON_CLICK)
        _gameState.update { it.copy(showSettings = !it.showSettings) }
    }
    
    fun updateSettings(newSettings: GameSettings) {
        currentSettings = newSettings
        settingsManager.saveSettings(newSettings)
        
        // Apply settings to sound manager
        soundManager.setSoundEnabled(newSettings.soundEnabled)
        soundManager.setMusicEnabled(newSettings.musicEnabled)
        soundManager.setSoundVolume(newSettings.soundVolume)
        soundManager.setMusicVolume(newSettings.musicVolume)
        
        _gameState.update { it.copy(gameSettings = newSettings) }
    }
    
    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}