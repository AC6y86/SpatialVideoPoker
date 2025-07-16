package com.hackathon.spatialvideopoker.game

import com.hackathon.spatialvideopoker.game.GameStateMachine.GamePhase
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class GameStateMachineTest {
    
    private lateinit var gameStateMachine: GameStateMachine
    
    @Before
    fun setUp() {
        gameStateMachine = GameStateMachine()
    }
    
    @Test
    fun `initial state is BETTING`() {
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.BETTING)
    }
    
    @Test
    fun `canTransitionTo returns true for valid transitions`() {
        val validTransitions = mapOf(
            GamePhase.BETTING to GamePhase.DEALING,
            GamePhase.DEALING to GamePhase.HOLDING,
            GamePhase.HOLDING to GamePhase.DRAWING,
            GamePhase.DRAWING to GamePhase.EVALUATING,
            GamePhase.EVALUATING to GamePhase.PAYOUT,
            GamePhase.PAYOUT to GamePhase.BETTING
        )
        
        validTransitions.forEach { (fromPhase, toPhase) ->
            gameStateMachine.reset()
            
            // Navigate to the from phase
            var currentPhase = GamePhase.BETTING
            while (currentPhase != fromPhase) {
                val nextPhase = getNextValidPhase(currentPhase)
                gameStateMachine.transitionTo(nextPhase)
                currentPhase = nextPhase
            }
            
            assertThat(gameStateMachine.canTransitionTo(toPhase)).isTrue()
        }
    }
    
    @Test
    fun `canTransitionTo returns false for invalid transitions`() {
        // Test some invalid transitions from BETTING
        assertThat(gameStateMachine.canTransitionTo(GamePhase.HOLDING)).isFalse()
        assertThat(gameStateMachine.canTransitionTo(GamePhase.DRAWING)).isFalse()
        assertThat(gameStateMachine.canTransitionTo(GamePhase.EVALUATING)).isFalse()
        assertThat(gameStateMachine.canTransitionTo(GamePhase.PAYOUT)).isFalse()
        assertThat(gameStateMachine.canTransitionTo(GamePhase.BETTING)).isFalse() // Self-transition
    }
    
    @Test
    fun `transitionTo successfully changes state for valid transitions`() {
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.BETTING)
        
        val success = gameStateMachine.transitionTo(GamePhase.DEALING)
        assertThat(success).isTrue()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.DEALING)
    }
    
    @Test
    fun `transitionTo fails and preserves state for invalid transitions`() {
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.BETTING)
        
        val success = gameStateMachine.transitionTo(GamePhase.HOLDING)
        assertThat(success).isFalse()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.BETTING)
    }
    
    @Test
    fun `complete game flow transitions work correctly`() {
        // Full game cycle: BETTING -> DEALING -> HOLDING -> DRAWING -> EVALUATING -> PAYOUT -> BETTING
        
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.BETTING)
        
        assertThat(gameStateMachine.transitionTo(GamePhase.DEALING)).isTrue()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.DEALING)
        
        assertThat(gameStateMachine.transitionTo(GamePhase.HOLDING)).isTrue()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.HOLDING)
        
        assertThat(gameStateMachine.transitionTo(GamePhase.DRAWING)).isTrue()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.DRAWING)
        
        assertThat(gameStateMachine.transitionTo(GamePhase.EVALUATING)).isTrue()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.EVALUATING)
        
        assertThat(gameStateMachine.transitionTo(GamePhase.PAYOUT)).isTrue()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.PAYOUT)
        
        assertThat(gameStateMachine.transitionTo(GamePhase.BETTING)).isTrue()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.BETTING)
    }
    
    @Test
    fun `reset returns state to BETTING from any phase`() {
        val allPhases = GamePhase.values()
        
        allPhases.forEach { targetPhase ->
            // Navigate to target phase
            gameStateMachine.reset()
            navigateToPhase(targetPhase)
            assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(targetPhase)
            
            // Reset and verify
            gameStateMachine.reset()
            assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.BETTING)
        }
    }
    
    @Test
    fun `isInBettingPhase returns correct values`() {
        assertThat(gameStateMachine.isInBettingPhase()).isTrue()
        
        gameStateMachine.transitionTo(GamePhase.DEALING)
        assertThat(gameStateMachine.isInBettingPhase()).isFalse()
        
        // Navigate back to betting
        navigateToPhase(GamePhase.BETTING)
        assertThat(gameStateMachine.isInBettingPhase()).isTrue()
    }
    
    @Test
    fun `isInHoldingPhase returns correct values`() {
        assertThat(gameStateMachine.isInHoldingPhase()).isFalse()
        
        navigateToPhase(GamePhase.HOLDING)
        assertThat(gameStateMachine.isInHoldingPhase()).isTrue()
        
        gameStateMachine.transitionTo(GamePhase.DRAWING)
        assertThat(gameStateMachine.isInHoldingPhase()).isFalse()
    }
    
    @Test
    fun `canDeal returns true only in BETTING phase`() {
        assertThat(gameStateMachine.canDeal()).isTrue()
        
        val otherPhases = GamePhase.values().filter { it != GamePhase.BETTING }
        otherPhases.forEach { phase ->
            navigateToPhase(phase)
            assertThat(gameStateMachine.canDeal()).isFalse()
        }
    }
    
    @Test
    fun `canDraw returns true only in HOLDING phase`() {
        assertThat(gameStateMachine.canDraw()).isFalse()
        
        navigateToPhase(GamePhase.HOLDING)
        assertThat(gameStateMachine.canDraw()).isTrue()
        
        val otherPhases = GamePhase.values().filter { it != GamePhase.HOLDING }
        otherPhases.forEach { phase ->
            navigateToPhase(phase)
            assertThat(gameStateMachine.canDraw()).isFalse()
        }
    }
    
    @Test
    fun `canChangeBet returns true only in BETTING phase`() {
        assertThat(gameStateMachine.canChangeBet()).isTrue()
        
        val otherPhases = GamePhase.values().filter { it != GamePhase.BETTING }
        otherPhases.forEach { phase ->
            navigateToPhase(phase)
            assertThat(gameStateMachine.canChangeBet()).isFalse()
        }
    }
    
    @Test
    fun `canHoldCards returns true only in HOLDING phase`() {
        assertThat(gameStateMachine.canHoldCards()).isFalse()
        
        navigateToPhase(GamePhase.HOLDING)
        assertThat(gameStateMachine.canHoldCards()).isTrue()
        
        val otherPhases = GamePhase.values().filter { it != GamePhase.HOLDING }
        otherPhases.forEach { phase ->
            navigateToPhase(phase)
            assertThat(gameStateMachine.canHoldCards()).isFalse()
        }
    }
    
    @Test
    fun `phase transitions are exclusive and sequential`() {
        // Verify that from each phase, only one transition is valid
        val exclusiveTransitions = mapOf(
            GamePhase.BETTING to listOf(GamePhase.DEALING),
            GamePhase.DEALING to listOf(GamePhase.HOLDING),
            GamePhase.HOLDING to listOf(GamePhase.DRAWING),
            GamePhase.DRAWING to listOf(GamePhase.EVALUATING),
            GamePhase.EVALUATING to listOf(GamePhase.PAYOUT),
            GamePhase.PAYOUT to listOf(GamePhase.BETTING)
        )
        
        exclusiveTransitions.forEach { (fromPhase, validTransitions) ->
            navigateToPhase(fromPhase)
            
            GamePhase.values().forEach { toPhase ->
                val isValid = validTransitions.contains(toPhase)
                assertThat(gameStateMachine.canTransitionTo(toPhase)).isEqualTo(isValid)
            }
        }
    }
    
    @Test
    fun `multiple transitions from same phase fail after first success`() {
        gameStateMachine.transitionTo(GamePhase.DEALING)
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.DEALING)
        
        // First valid transition should succeed
        assertThat(gameStateMachine.transitionTo(GamePhase.HOLDING)).isTrue()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.HOLDING)
        
        // Trying to transition back to DEALING should fail
        assertThat(gameStateMachine.transitionTo(GamePhase.DEALING)).isFalse()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.HOLDING)
    }
    
    @Test
    fun `state machine maintains consistency through invalid transition attempts`() {
        val originalPhase = gameStateMachine.getCurrentPhase()
        
        // Try several invalid transitions
        val invalidTransitions = listOf(
            GamePhase.HOLDING,
            GamePhase.DRAWING,
            GamePhase.EVALUATING,
            GamePhase.PAYOUT
        )
        
        invalidTransitions.forEach { invalidPhase ->
            assertThat(gameStateMachine.transitionTo(invalidPhase)).isFalse()
            assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(originalPhase)
        }
        
        // Valid transition should still work
        assertThat(gameStateMachine.transitionTo(GamePhase.DEALING)).isTrue()
        assertThat(gameStateMachine.getCurrentPhase()).isEqualTo(GamePhase.DEALING)
    }
    
    @Test
    fun `game phases have correct string representations`() {
        // Verify that enum values are properly defined
        assertThat(GamePhase.BETTING.name).isEqualTo("BETTING")
        assertThat(GamePhase.DEALING.name).isEqualTo("DEALING")
        assertThat(GamePhase.HOLDING.name).isEqualTo("HOLDING")
        assertThat(GamePhase.DRAWING.name).isEqualTo("DRAWING")
        assertThat(GamePhase.EVALUATING.name).isEqualTo("EVALUATING")
        assertThat(GamePhase.PAYOUT.name).isEqualTo("PAYOUT")
    }
    
    private fun navigateToPhase(targetPhase: GamePhase) {
        gameStateMachine.reset()
        
        val phaseSequence = listOf(
            GamePhase.BETTING,
            GamePhase.DEALING,
            GamePhase.HOLDING,
            GamePhase.DRAWING,
            GamePhase.EVALUATING,
            GamePhase.PAYOUT
        )
        
        val targetIndex = phaseSequence.indexOf(targetPhase)
        for (i in 0 until targetIndex) {
            gameStateMachine.transitionTo(phaseSequence[i + 1])
        }
    }
    
    private fun getNextValidPhase(currentPhase: GamePhase): GamePhase {
        return when (currentPhase) {
            GamePhase.BETTING -> GamePhase.DEALING
            GamePhase.DEALING -> GamePhase.HOLDING
            GamePhase.HOLDING -> GamePhase.DRAWING
            GamePhase.DRAWING -> GamePhase.EVALUATING
            GamePhase.EVALUATING -> GamePhase.PAYOUT
            GamePhase.PAYOUT -> GamePhase.BETTING
        }
    }
}