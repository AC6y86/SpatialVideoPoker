package com.hackathon.spatialvideopoker.game

import com.hackathon.spatialvideopoker.game.HandEvaluator.HandRank
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class PayoutCalculatorTest {
    
    private lateinit var payoutCalculator: PayoutCalculator
    
    @Before
    fun setUp() {
        payoutCalculator = PayoutCalculator()
    }
    
    @Test
    fun `calculatePayout throws exception for invalid bet amounts`() {
        val invalidBets = listOf(0, -1, 6, 10, 100)
        
        invalidBets.forEach { invalidBet ->
            try {
                payoutCalculator.calculatePayout(HandRank.JACKS_OR_BETTER, invalidBet)
                assert(false) { "Expected exception for invalid bet: $invalidBet" }
            } catch (e: IllegalArgumentException) {
                assertThat(e.message).contains("Bet amount must be between 1 and 5")
            }
        }
    }
    
    @Test
    fun `calculatePayout returns correct royal flush payouts`() {
        // Royal Flush has special bonus for max bet (5 coins)
        val expectedPayouts = mapOf(
            1 to 250,
            2 to 500,
            3 to 750,
            4 to 1000,
            5 to 4000  // Special bonus for max bet
        )
        
        expectedPayouts.forEach { (bet, expectedPayout) ->
            val actualPayout = payoutCalculator.calculatePayout(HandRank.ROYAL_FLUSH, bet)
            assertThat(actualPayout).isEqualTo(expectedPayout)
        }
    }
    
    @Test
    fun `calculatePayout returns correct straight flush payouts`() {
        val expectedPayouts = mapOf(
            1 to 50,
            2 to 100,
            3 to 150,
            4 to 200,
            5 to 250
        )
        
        expectedPayouts.forEach { (bet, expectedPayout) ->
            val actualPayout = payoutCalculator.calculatePayout(HandRank.STRAIGHT_FLUSH, bet)
            assertThat(actualPayout).isEqualTo(expectedPayout)
        }
    }
    
    @Test
    fun `calculatePayout returns correct four of a kind payouts`() {
        val expectedPayouts = mapOf(
            1 to 25,
            2 to 50,
            3 to 75,
            4 to 100,
            5 to 125
        )
        
        expectedPayouts.forEach { (bet, expectedPayout) ->
            val actualPayout = payoutCalculator.calculatePayout(HandRank.FOUR_OF_A_KIND, bet)
            assertThat(actualPayout).isEqualTo(expectedPayout)
        }
    }
    
    @Test
    fun `calculatePayout returns correct full house payouts for 9 6 table`() {
        val expectedPayouts = mapOf(
            1 to 9,
            2 to 18,
            3 to 27,
            4 to 36,
            5 to 45
        )
        
        expectedPayouts.forEach { (bet, expectedPayout) ->
            val actualPayout = payoutCalculator.calculatePayout(HandRank.FULL_HOUSE, bet)
            assertThat(actualPayout).isEqualTo(expectedPayout)
        }
    }
    
    @Test
    fun `calculatePayout returns correct flush payouts for 9 6 table`() {
        val expectedPayouts = mapOf(
            1 to 6,
            2 to 12,
            3 to 18,
            4 to 24,
            5 to 30
        )
        
        expectedPayouts.forEach { (bet, expectedPayout) ->
            val actualPayout = payoutCalculator.calculatePayout(HandRank.FLUSH, bet)
            assertThat(actualPayout).isEqualTo(expectedPayout)
        }
    }
    
    @Test
    fun `calculatePayout returns correct straight payouts`() {
        val expectedPayouts = mapOf(
            1 to 4,
            2 to 8,
            3 to 12,
            4 to 16,
            5 to 20
        )
        
        expectedPayouts.forEach { (bet, expectedPayout) ->
            val actualPayout = payoutCalculator.calculatePayout(HandRank.STRAIGHT, bet)
            assertThat(actualPayout).isEqualTo(expectedPayout)
        }
    }
    
    @Test
    fun `calculatePayout returns correct three of a kind payouts`() {
        val expectedPayouts = mapOf(
            1 to 3,
            2 to 6,
            3 to 9,
            4 to 12,
            5 to 15
        )
        
        expectedPayouts.forEach { (bet, expectedPayout) ->
            val actualPayout = payoutCalculator.calculatePayout(HandRank.THREE_OF_A_KIND, bet)
            assertThat(actualPayout).isEqualTo(expectedPayout)
        }
    }
    
    @Test
    fun `calculatePayout returns correct two pair payouts`() {
        val expectedPayouts = mapOf(
            1 to 2,
            2 to 4,
            3 to 6,
            4 to 8,
            5 to 10
        )
        
        expectedPayouts.forEach { (bet, expectedPayout) ->
            val actualPayout = payoutCalculator.calculatePayout(HandRank.TWO_PAIR, bet)
            assertThat(actualPayout).isEqualTo(expectedPayout)
        }
    }
    
    @Test
    fun `calculatePayout returns correct jacks or better payouts`() {
        val expectedPayouts = mapOf(
            1 to 1,
            2 to 2,
            3 to 3,
            4 to 4,
            5 to 5
        )
        
        expectedPayouts.forEach { (bet, expectedPayout) ->
            val actualPayout = payoutCalculator.calculatePayout(HandRank.JACKS_OR_BETTER, bet)
            assertThat(actualPayout).isEqualTo(expectedPayout)
        }
    }
    
    @Test
    fun `calculatePayout returns zero for high card hands`() {
        val bets = listOf(1, 2, 3, 4, 5)
        
        bets.forEach { bet ->
            val payout = payoutCalculator.calculatePayout(HandRank.HIGH_CARD, bet)
            assertThat(payout).isEqualTo(0)
        }
    }
    
    @Test
    fun `getPayoutMultiplier calculates correct multipliers for most hands`() {
        // Most hands have linear scaling (except Royal Flush with max bet)
        val hands = listOf(
            HandRank.STRAIGHT_FLUSH,
            HandRank.FOUR_OF_A_KIND,
            HandRank.FULL_HOUSE,
            HandRank.FLUSH,
            HandRank.STRAIGHT,
            HandRank.THREE_OF_A_KIND,
            HandRank.TWO_PAIR,
            HandRank.JACKS_OR_BETTER
        )
        
        hands.forEach { hand ->
            for (bet in 1..4) {
                val payout = payoutCalculator.calculatePayout(hand, bet)
                val multiplier = payoutCalculator.getPayoutMultiplier(hand, bet)
                val basePayout = payoutCalculator.calculatePayout(hand, 1)
                
                assertThat(multiplier).isEqualTo(basePayout)
                assertThat(payout).isEqualTo(basePayout * bet)
            }
        }
    }
    
    @Test
    fun `getPayoutMultiplier calculates special royal flush multiplier for max bet`() {
        // Royal Flush has 800:1 multiplier for max bet instead of 250:1
        val maxBetMultiplier = payoutCalculator.getPayoutMultiplier(HandRank.ROYAL_FLUSH, 5)
        assertThat(maxBetMultiplier).isEqualTo(800)
        
        // Other bets should have 250:1 multiplier
        for (bet in 1..4) {
            val multiplier = payoutCalculator.getPayoutMultiplier(HandRank.ROYAL_FLUSH, bet)
            assertThat(multiplier).isEqualTo(250)
        }
    }
    
    @Test
    fun `getPayoutMultiplier returns zero for high card hands`() {
        val bets = listOf(1, 2, 3, 4, 5)
        
        bets.forEach { bet ->
            val multiplier = payoutCalculator.getPayoutMultiplier(HandRank.HIGH_CARD, bet)
            assertThat(multiplier).isEqualTo(0)
        }
    }
    
    @Test
    fun `getPayoutTableForDisplay excludes high card`() {
        val displayTable = payoutCalculator.getPayoutTableForDisplay()
        
        assertThat(displayTable).doesNotContainKey(HandRank.HIGH_CARD)
        assertThat(displayTable).containsKey(HandRank.ROYAL_FLUSH)
        assertThat(displayTable).containsKey(HandRank.JACKS_OR_BETTER)
        assertThat(displayTable).hasSize(9) // All winning hands except HIGH_CARD
    }
    
    @Test
    fun `getPayoutTableForDisplay contains correct payout values`() {
        val displayTable = payoutCalculator.getPayoutTableForDisplay()
        
        // Verify some key entries
        assertThat(displayTable[HandRank.ROYAL_FLUSH]).isEqualTo(listOf(250, 500, 750, 1000, 4000))
        assertThat(displayTable[HandRank.FULL_HOUSE]).isEqualTo(listOf(9, 18, 27, 36, 45))
        assertThat(displayTable[HandRank.FLUSH]).isEqualTo(listOf(6, 12, 18, 24, 30))
        assertThat(displayTable[HandRank.JACKS_OR_BETTER]).isEqualTo(listOf(1, 2, 3, 4, 5))
    }
    
    @Test
    fun `isWinningHand returns true for all winning hands`() {
        val winningHands = listOf(
            HandRank.ROYAL_FLUSH,
            HandRank.STRAIGHT_FLUSH,
            HandRank.FOUR_OF_A_KIND,
            HandRank.FULL_HOUSE,
            HandRank.FLUSH,
            HandRank.STRAIGHT,
            HandRank.THREE_OF_A_KIND,
            HandRank.TWO_PAIR,
            HandRank.JACKS_OR_BETTER
        )
        
        winningHands.forEach { hand ->
            assertThat(payoutCalculator.isWinningHand(hand)).isTrue()
        }
    }
    
    @Test
    fun `isWinningHand returns false for high card`() {
        assertThat(payoutCalculator.isWinningHand(HandRank.HIGH_CARD)).isFalse()
    }
    
    @Test
    fun `getRoyalFlushBonus returns true only for max bet`() {
        assertThat(payoutCalculator.getRoyalFlushBonus(5)).isTrue()
        
        val nonMaxBets = listOf(1, 2, 3, 4)
        nonMaxBets.forEach { bet ->
            assertThat(payoutCalculator.getRoyalFlushBonus(bet)).isFalse()
        }
    }
    
    @Test
    fun `getExpectedReturn returns correct theoretical RTP`() {
        val expectedRTP = payoutCalculator.getExpectedReturn()
        assertThat(expectedRTP).isEqualTo(0.9954)
        assertThat(expectedRTP).isGreaterThan(0.99)
        assertThat(expectedRTP).isLessThan(1.0)
    }
    
    @Test
    fun `royal flush max bet bonus provides significant incentive`() {
        val regularMultiplier = payoutCalculator.getPayoutMultiplier(HandRank.ROYAL_FLUSH, 1)
        val maxBetMultiplier = payoutCalculator.getPayoutMultiplier(HandRank.ROYAL_FLUSH, 5)
        
        // Max bet should provide more than 3x the multiplier incentive
        assertThat(maxBetMultiplier).isGreaterThan(regularMultiplier * 3)
        assertThat(maxBetMultiplier).isEqualTo(800)
        assertThat(regularMultiplier).isEqualTo(250)
    }
    
    @Test
    fun `payout calculations are mathematically consistent`() {
        // Verify that all payouts (except Royal Flush max bet) scale linearly
        val hands = HandRank.values().filter { it != HandRank.HIGH_CARD }
        
        hands.forEach { hand ->
            val basePayout = payoutCalculator.calculatePayout(hand, 1)
            
            for (bet in 2..5) {
                val expectedPayout = if (hand == HandRank.ROYAL_FLUSH && bet == 5) {
                    4000 // Special case
                } else {
                    basePayout * bet
                }
                
                val actualPayout = payoutCalculator.calculatePayout(hand, bet)
                assertThat(actualPayout).isEqualTo(expectedPayout)
            }
        }
    }
    
    @Test
    fun `all hand ranks have defined payouts`() {
        val allHandRanks = HandRank.values().toList()
        
        allHandRanks.forEach { hand ->
            for (bet in 1..5) {
                // Should not throw exception
                val payout = payoutCalculator.calculatePayout(hand, bet)
                assertThat(payout).isAtLeast(0)
            }
        }
    }
    
    @Test
    fun `higher ranking hands have higher or equal base payouts`() {
        val handsByRank = listOf(
            HandRank.HIGH_CARD,
            HandRank.JACKS_OR_BETTER,
            HandRank.TWO_PAIR,
            HandRank.THREE_OF_A_KIND,
            HandRank.STRAIGHT,
            HandRank.FLUSH,
            HandRank.FULL_HOUSE,
            HandRank.FOUR_OF_A_KIND,
            HandRank.STRAIGHT_FLUSH,
            HandRank.ROYAL_FLUSH
        )
        
        for (i in 0 until handsByRank.size - 1) {
            val lowerHand = handsByRank[i]
            val higherHand = handsByRank[i + 1]
            
            val lowerPayout = payoutCalculator.calculatePayout(lowerHand, 1)
            val higherPayout = payoutCalculator.calculatePayout(higherHand, 1)
            
            assertThat(higherPayout).isAtLeast(lowerPayout)
        }
    }
}