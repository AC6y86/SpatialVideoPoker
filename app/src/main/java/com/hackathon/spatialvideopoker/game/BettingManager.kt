package com.hackathon.spatialvideopoker.game

class BettingManager {
    var currentBet: Int = 1
        private set
    
    companion object {
        const val MIN_BET = 1
        const val MAX_BET = 5
        const val DEFAULT_BET = 1
    }
    
    fun validateBet(amount: Int, availableCredits: Int): Boolean {
        return amount in MIN_BET..MAX_BET && amount <= availableCredits
    }
    
    fun placeBet(amount: Int, availableCredits: Int): Boolean {
        if (validateBet(amount, availableCredits)) {
            currentBet = amount
            return true
        }
        return false
    }
    
    fun incrementBet(availableCredits: Int): Boolean {
        val newBet = if (currentBet >= MAX_BET) MIN_BET else currentBet + 1
        return placeBet(newBet, availableCredits)
    }
    
    fun maxBet(availableCredits: Int): Boolean {
        val maxPossibleBet = minOf(MAX_BET, availableCredits)
        return placeBet(maxPossibleBet, availableCredits)
    }
    
    fun resetBet() {
        currentBet = DEFAULT_BET
    }
    
    fun canAffordCurrentBet(availableCredits: Int): Boolean {
        return availableCredits >= currentBet
    }
    
    fun adjustBetToCredits(availableCredits: Int) {
        if (currentBet > availableCredits) {
            currentBet = maxOf(MIN_BET, availableCredits)
        }
    }
}