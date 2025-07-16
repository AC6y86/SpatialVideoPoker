package com.hackathon.spatialvideopoker.game

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class BettingManagerTest {
    
    private lateinit var bettingManager: BettingManager
    
    @Before
    fun setUp() {
        bettingManager = BettingManager()
    }
    
    @Test
    fun `initial bet is set to default value`() {
        assertThat(bettingManager.currentBet).isEqualTo(BettingManager.DEFAULT_BET)
        assertThat(bettingManager.currentBet).isEqualTo(1)
    }
    
    @Test
    fun `validateBet returns true for valid bets with sufficient credits`() {
        val validScenarios = listOf(
            Triple(1, 10, true),  // Min bet with plenty of credits
            Triple(5, 10, true),  // Max bet with sufficient credits
            Triple(3, 3, true),   // Bet equals available credits
            Triple(2, 5, true)    // Mid-range bet
        )
        
        validScenarios.forEach { (bet, credits, expected) ->
            assertThat(bettingManager.validateBet(bet, credits)).isEqualTo(expected)
        }
    }
    
    @Test
    fun `validateBet returns false for invalid bets`() {
        val invalidScenarios = listOf(
            Triple(0, 10, false),   // Below minimum bet
            Triple(-1, 10, false),  // Negative bet
            Triple(6, 10, false),   // Above maximum bet
            Triple(3, 2, false),    // Bet exceeds available credits
            Triple(5, 4, false),    // Max bet but insufficient credits
            Triple(10, 100, false)  // Way above maximum bet
        )
        
        invalidScenarios.forEach { (bet, credits, expected) ->
            assertThat(bettingManager.validateBet(bet, credits)).isEqualTo(expected)
        }
    }
    
    @Test
    fun `placeBet sets current bet for valid amounts`() {
        val validBets = listOf(1, 2, 3, 4, 5)
        val sufficientCredits = 10
        
        validBets.forEach { bet ->
            val success = bettingManager.placeBet(bet, sufficientCredits)
            assertThat(success).isTrue()
            assertThat(bettingManager.currentBet).isEqualTo(bet)
        }
    }
    
    @Test
    fun `placeBet rejects invalid amounts and preserves current bet`() {
        val initialBet = 2
        bettingManager.placeBet(initialBet, 10)
        
        val invalidBets = listOf(0, -1, 6, 10)
        
        invalidBets.forEach { invalidBet ->
            val success = bettingManager.placeBet(invalidBet, 10)
            assertThat(success).isFalse()
            assertThat(bettingManager.currentBet).isEqualTo(initialBet)
        }
    }
    
    @Test
    fun `placeBet rejects valid amounts with insufficient credits`() {
        val originalBet = 2
        bettingManager.placeBet(originalBet, 10)
        
        val success = bettingManager.placeBet(5, 3) // Bet 5 with only 3 credits
        assertThat(success).isFalse()
        assertThat(bettingManager.currentBet).isEqualTo(originalBet)
    }
    
    @Test
    fun `incrementBet cycles through valid bet amounts`() {
        val credits = 10
        
        // Start at 1, should increment to 2, 3, 4, 5, then back to 1
        assertThat(bettingManager.currentBet).isEqualTo(1)
        
        assertThat(bettingManager.incrementBet(credits)).isTrue()
        assertThat(bettingManager.currentBet).isEqualTo(2)
        
        assertThat(bettingManager.incrementBet(credits)).isTrue()
        assertThat(bettingManager.currentBet).isEqualTo(3)
        
        assertThat(bettingManager.incrementBet(credits)).isTrue()
        assertThat(bettingManager.currentBet).isEqualTo(4)
        
        assertThat(bettingManager.incrementBet(credits)).isTrue()
        assertThat(bettingManager.currentBet).isEqualTo(5)
        
        // Should cycle back to 1
        assertThat(bettingManager.incrementBet(credits)).isTrue()
        assertThat(bettingManager.currentBet).isEqualTo(1)
    }
    
    @Test
    fun `incrementBet respects credit limitations`() {
        bettingManager.placeBet(3, 10)
        
        // Try to increment when only 3 credits available
        val success = bettingManager.incrementBet(3)
        assertThat(success).isFalse()
        assertThat(bettingManager.currentBet).isEqualTo(3) // Should remain unchanged
    }
    
    @Test
    fun `incrementBet with limited credits cycles appropriately`() {
        val limitedCredits = 3
        
        assertThat(bettingManager.currentBet).isEqualTo(1)
        
        assertThat(bettingManager.incrementBet(limitedCredits)).isTrue()
        assertThat(bettingManager.currentBet).isEqualTo(2)
        
        assertThat(bettingManager.incrementBet(limitedCredits)).isTrue()
        assertThat(bettingManager.currentBet).isEqualTo(3)
        
        // Should fail to increment to 4 because only 3 credits available
        assertThat(bettingManager.incrementBet(limitedCredits)).isFalse()
        assertThat(bettingManager.currentBet).isEqualTo(3) // Should remain unchanged
    }
    
    @Test
    fun `maxBet sets bet to maximum possible amount`() {
        val scenarios = listOf(
            Pair(10, 5),  // Plenty of credits, should set to MAX_BET (5)
            Pair(3, 3),   // Limited credits, should set to 3
            Pair(1, 1),   // Very limited credits, should set to 1
            Pair(7, 5)    // More than max bet, should set to MAX_BET (5)
        )
        
        scenarios.forEach { (credits, expectedBet) ->
            bettingManager.resetBet()
            val success = bettingManager.maxBet(credits)
            assertThat(success).isTrue()
            assertThat(bettingManager.currentBet).isEqualTo(expectedBet)
        }
    }
    
    @Test
    fun `maxBet with zero credits fails`() {
        val success = bettingManager.maxBet(0)
        assertThat(success).isFalse()
        assertThat(bettingManager.currentBet).isEqualTo(BettingManager.DEFAULT_BET)
    }
    
    @Test
    fun `resetBet returns to default value`() {
        bettingManager.placeBet(5, 10)
        assertThat(bettingManager.currentBet).isEqualTo(5)
        
        bettingManager.resetBet()
        assertThat(bettingManager.currentBet).isEqualTo(BettingManager.DEFAULT_BET)
    }
    
    @Test
    fun `canAffordCurrentBet returns correct affordability`() {
        bettingManager.placeBet(3, 10)
        
        assertThat(bettingManager.canAffordCurrentBet(5)).isTrue()
        assertThat(bettingManager.canAffordCurrentBet(3)).isTrue()
        assertThat(bettingManager.canAffordCurrentBet(2)).isFalse()
        assertThat(bettingManager.canAffordCurrentBet(0)).isFalse()
    }
    
    @Test
    fun `adjustBetToCredits reduces bet when necessary`() {
        bettingManager.placeBet(5, 10)
        assertThat(bettingManager.currentBet).isEqualTo(5)
        
        bettingManager.adjustBetToCredits(3)
        assertThat(bettingManager.currentBet).isEqualTo(3)
    }
    
    @Test
    fun `adjustBetToCredits does not increase bet`() {
        bettingManager.placeBet(2, 10)
        assertThat(bettingManager.currentBet).isEqualTo(2)
        
        bettingManager.adjustBetToCredits(10)
        assertThat(bettingManager.currentBet).isEqualTo(2) // Should remain unchanged
    }
    
    @Test
    fun `adjustBetToCredits sets minimum bet when credits are very low`() {
        bettingManager.placeBet(3, 10)
        assertThat(bettingManager.currentBet).isEqualTo(3)
        
        bettingManager.adjustBetToCredits(0)
        assertThat(bettingManager.currentBet).isEqualTo(BettingManager.MIN_BET)
    }
    
    @Test
    fun `adjustBetToCredits with exactly matching credits keeps bet unchanged`() {
        bettingManager.placeBet(4, 10)
        assertThat(bettingManager.currentBet).isEqualTo(4)
        
        bettingManager.adjustBetToCredits(4)
        assertThat(bettingManager.currentBet).isEqualTo(4) // Should remain unchanged
    }
    
    @Test
    fun `constants have expected values`() {
        assertThat(BettingManager.MIN_BET).isEqualTo(1)
        assertThat(BettingManager.MAX_BET).isEqualTo(5)
        assertThat(BettingManager.DEFAULT_BET).isEqualTo(1)
        assertThat(BettingManager.DEFAULT_BET).isEqualTo(BettingManager.MIN_BET)
    }
    
    @Test
    fun `betting manager maintains state across multiple operations`() {
        // Complex scenario: multiple operations in sequence
        bettingManager.placeBet(2, 10)
        assertThat(bettingManager.canAffordCurrentBet(5)).isTrue()
        
        bettingManager.incrementBet(10)
        assertThat(bettingManager.currentBet).isEqualTo(3)
        
        bettingManager.maxBet(10)
        assertThat(bettingManager.currentBet).isEqualTo(5)
        
        bettingManager.adjustBetToCredits(2)
        assertThat(bettingManager.currentBet).isEqualTo(2)
        
        assertThat(bettingManager.canAffordCurrentBet(2)).isTrue()
        assertThat(bettingManager.canAffordCurrentBet(1)).isFalse()
    }
}