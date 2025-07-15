package com.hackathon.spatialvideopoker.game

import com.hackathon.spatialvideopoker.game.HandEvaluator.HandRank

class PayoutCalculator {
    
    // 9/6 Jacks or Better pay table
    // Each list represents payouts for 1-5 coin bets
    private val payoutTable = mapOf(
        HandRank.ROYAL_FLUSH to listOf(250, 500, 750, 1000, 4000),
        HandRank.STRAIGHT_FLUSH to listOf(50, 100, 150, 200, 250),
        HandRank.FOUR_OF_A_KIND to listOf(25, 50, 75, 100, 125),
        HandRank.FULL_HOUSE to listOf(9, 18, 27, 36, 45),
        HandRank.FLUSH to listOf(6, 12, 18, 24, 30),
        HandRank.STRAIGHT to listOf(4, 8, 12, 16, 20),
        HandRank.THREE_OF_A_KIND to listOf(3, 6, 9, 12, 15),
        HandRank.TWO_PAIR to listOf(2, 4, 6, 8, 10),
        HandRank.JACKS_OR_BETTER to listOf(1, 2, 3, 4, 5),
        HandRank.HIGH_CARD to listOf(0, 0, 0, 0, 0)
    )
    
    fun calculatePayout(hand: HandRank, betAmount: Int): Int {
        require(betAmount in 1..5) { "Bet amount must be between 1 and 5" }
        
        return payoutTable[hand]?.get(betAmount - 1) ?: 0
    }
    
    fun getPayoutMultiplier(hand: HandRank, betAmount: Int): Int {
        val payout = calculatePayout(hand, betAmount)
        return if (betAmount > 0) payout / betAmount else 0
    }
    
    fun getPayoutTableForDisplay(): Map<HandRank, List<Int>> {
        return payoutTable.filter { it.key != HandRank.HIGH_CARD }
    }
    
    fun isWinningHand(hand: HandRank): Boolean {
        return hand != HandRank.HIGH_CARD
    }
    
    fun getRoyalFlushBonus(betAmount: Int): Boolean {
        // Returns true if betting max coins gives the special Royal Flush bonus
        return betAmount == 5
    }
    
    fun getExpectedReturn(): Double {
        // 9/6 Jacks or Better has a theoretical return of 99.54% with optimal play
        return 0.9954
    }
}