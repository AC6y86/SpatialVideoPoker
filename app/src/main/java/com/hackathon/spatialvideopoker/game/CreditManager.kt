package com.hackathon.spatialvideopoker.game

import com.hackathon.spatialvideopoker.data.dao.GameStateDao
import com.hackathon.spatialvideopoker.data.entity.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CreditManager(private val gameStateDao: GameStateDao) {
    
    companion object {
        const val DEFAULT_CREDITS = 1000
        const val MINIMUM_CREDITS = 0
    }
    
    private var credits = DEFAULT_CREDITS
    
    suspend fun loadCredits() {
        try {
            val gameState = gameStateDao.getGameState()
            credits = gameState?.credits ?: DEFAULT_CREDITS
        } catch (e: Exception) {
            credits = DEFAULT_CREDITS
        }
    }
    
    fun observeCredits(): Flow<Int> {
        return gameStateDao.observeGameState().map { gameState ->
            gameState?.credits ?: DEFAULT_CREDITS
        }
    }
    
    suspend fun deductBet(amount: Int): Boolean {
        if (credits >= amount && amount > 0) {
            credits -= amount
            saveCredits()
            return true
        }
        return false
    }
    
    suspend fun addWinnings(amount: Int) {
        if (amount > 0) {
            credits += amount
            saveCredits()
            gameStateDao.addToTotalWinnings(amount)
            gameStateDao.updateLastWin(amount)
            gameStateDao.updateHighestWin(amount)
        }
    }
    
    suspend fun resetCredits() {
        credits = DEFAULT_CREDITS
        saveCredits()
    }
    
    fun getCurrentCredits(): Int = credits
    
    fun hasCredits(): Boolean = credits > MINIMUM_CREDITS
    
    fun canAffordBet(amount: Int): Boolean = credits >= amount && amount > 0
    
    private suspend fun saveCredits() {
        val currentState = gameStateDao.getGameState() ?: GameState()
        gameStateDao.saveGameState(
            currentState.copy(
                credits = credits,
                lastPlayedTimestamp = System.currentTimeMillis()
            )
        )
    }
}