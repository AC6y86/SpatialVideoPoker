package com.hackathon.spatialvideopoker.game

class GameStateMachine {
    
    enum class GamePhase {
        BETTING,      // Player can adjust bet and press deal
        DEALING,      // Cards are being dealt (animation phase)
        HOLDING,      // Player can select cards to hold
        DRAWING,      // Cards are being drawn (animation phase)
        EVALUATING,   // Hand is being evaluated
        PAYOUT        // Payout animation and credit update
    }
    
    private var currentPhase = GamePhase.BETTING
    
    fun getCurrentPhase(): GamePhase = currentPhase
    
    fun canTransitionTo(newPhase: GamePhase): Boolean {
        return when (currentPhase) {
            GamePhase.BETTING -> newPhase == GamePhase.DEALING
            GamePhase.DEALING -> newPhase == GamePhase.HOLDING
            GamePhase.HOLDING -> newPhase == GamePhase.DRAWING
            GamePhase.DRAWING -> newPhase == GamePhase.EVALUATING
            GamePhase.EVALUATING -> newPhase == GamePhase.PAYOUT
            GamePhase.PAYOUT -> newPhase == GamePhase.BETTING
        }
    }
    
    fun transitionTo(newPhase: GamePhase): Boolean {
        if (canTransitionTo(newPhase)) {
            currentPhase = newPhase
            return true
        }
        return false
    }
    
    fun reset() {
        currentPhase = GamePhase.BETTING
    }
    
    fun isInBettingPhase(): Boolean = currentPhase == GamePhase.BETTING
    
    fun isInHoldingPhase(): Boolean = currentPhase == GamePhase.HOLDING
    
    fun canDeal(): Boolean = currentPhase == GamePhase.BETTING
    
    fun canDraw(): Boolean = currentPhase == GamePhase.HOLDING
    
    fun canChangeBet(): Boolean = currentPhase == GamePhase.BETTING
    
    fun canHoldCards(): Boolean = currentPhase == GamePhase.HOLDING
}