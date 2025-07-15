package com.hackathon.spatialvideopoker.game

import com.hackathon.spatialvideopoker.model.Card

class HandEvaluator {
    
    enum class HandRank(val displayName: String) {
        HIGH_CARD("High Card"),
        JACKS_OR_BETTER("Jacks or Better"),
        TWO_PAIR("Two Pair"),
        THREE_OF_A_KIND("Three of a Kind"),
        STRAIGHT("Straight"),
        FLUSH("Flush"),
        FULL_HOUSE("Full House"),
        FOUR_OF_A_KIND("Four of a Kind"),
        STRAIGHT_FLUSH("Straight Flush"),
        ROYAL_FLUSH("Royal Flush")
    }
    
    fun evaluateHand(cards: List<Card>): HandRank {
        require(cards.size == 5) { "Hand must contain exactly 5 cards" }
        
        val sortedCards = cards.sortedByDescending { it.rank.value }
        
        // Check for flush
        val isFlush = cards.all { it.suit == cards[0].suit }
        
        // Check for straight
        val isStraight = checkStraight(sortedCards)
        
        // Count rank occurrences
        val rankCounts = cards.groupBy { it.rank }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
        
        // Evaluate hand
        return when {
            isRoyalFlush(sortedCards, isFlush) -> HandRank.ROYAL_FLUSH
            isStraight && isFlush -> HandRank.STRAIGHT_FLUSH
            rankCounts[0].second == 4 -> HandRank.FOUR_OF_A_KIND
            rankCounts[0].second == 3 && rankCounts[1].second == 2 -> HandRank.FULL_HOUSE
            isFlush -> HandRank.FLUSH
            isStraight -> HandRank.STRAIGHT
            rankCounts[0].second == 3 -> HandRank.THREE_OF_A_KIND
            rankCounts[0].second == 2 && rankCounts[1].second == 2 -> HandRank.TWO_PAIR
            isJacksOrBetter(rankCounts) -> HandRank.JACKS_OR_BETTER
            else -> HandRank.HIGH_CARD
        }
    }
    
    private fun checkStraight(sortedCards: List<Card>): Boolean {
        // Check for regular straight
        var isRegularStraight = true
        for (i in 0 until sortedCards.size - 1) {
            if (sortedCards[i].rank.value - sortedCards[i + 1].rank.value != 1) {
                isRegularStraight = false
                break
            }
        }
        
        if (isRegularStraight) return true
        
        // Check for A-2-3-4-5 straight (wheel)
        val ranks = sortedCards.map { it.rank.value }
        return ranks == listOf(14, 5, 4, 3, 2) // Ace, 5, 4, 3, 2
    }
    
    private fun isRoyalFlush(sortedCards: List<Card>, isFlush: Boolean): Boolean {
        if (!isFlush) return false
        
        val royalRanks = listOf(14, 13, 12, 11, 10) // A, K, Q, J, 10
        val cardRanks = sortedCards.map { it.rank.value }
        
        return cardRanks == royalRanks
    }
    
    private fun isJacksOrBetter(rankCounts: List<Pair<Card.Rank, Int>>): Boolean {
        if (rankCounts[0].second != 2) return false
        
        val pairRank = rankCounts[0].first
        return pairRank.value >= Card.Rank.JACK.value
    }
    
    fun getWinningCards(cards: List<Card>, handRank: HandRank): List<Card> {
        // Returns the cards that contribute to the winning hand
        // This is useful for highlighting winning cards in the UI
        
        val sortedCards = cards.sortedByDescending { it.rank.value }
        val rankCounts = cards.groupBy { it.rank }
        
        return when (handRank) {
            HandRank.ROYAL_FLUSH, HandRank.STRAIGHT_FLUSH, 
            HandRank.FLUSH, HandRank.STRAIGHT -> cards // All cards contribute
            
            HandRank.FOUR_OF_A_KIND -> {
                val fourRank = rankCounts.entries.find { it.value.size == 4 }?.key
                cards.filter { it.rank == fourRank }
            }
            
            HandRank.FULL_HOUSE -> cards // All cards contribute
            
            HandRank.THREE_OF_A_KIND -> {
                val threeRank = rankCounts.entries.find { it.value.size == 3 }?.key
                cards.filter { it.rank == threeRank }
            }
            
            HandRank.TWO_PAIR -> {
                val pairs = rankCounts.entries.filter { it.value.size == 2 }.map { it.key }
                cards.filter { it.rank in pairs }
            }
            
            HandRank.JACKS_OR_BETTER -> {
                val pairRank = rankCounts.entries.find { it.value.size == 2 }?.key
                cards.filter { it.rank == pairRank }
            }
            
            HandRank.HIGH_CARD -> emptyList()
        }
    }
}