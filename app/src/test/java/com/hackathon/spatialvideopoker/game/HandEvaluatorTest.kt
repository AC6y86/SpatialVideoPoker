package com.hackathon.spatialvideopoker.game

import com.hackathon.spatialvideopoker.model.Card
import com.hackathon.spatialvideopoker.model.Card.Rank.*
import com.hackathon.spatialvideopoker.model.Card.Suit.*
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class HandEvaluatorTest {
    
    private lateinit var handEvaluator: HandEvaluator
    
    @Before
    fun setUp() {
        handEvaluator = HandEvaluator()
    }
    
    @Test
    fun `evaluateHand throws exception for wrong number of cards`() {
        val fourCards = listOf(
            Card(SPADES, ACE),
            Card(SPADES, KING),
            Card(SPADES, QUEEN),
            Card(SPADES, JACK)
        )
        
        try {
            handEvaluator.evaluateHand(fourCards)
            assert(false) { "Expected exception for wrong number of cards" }
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("Hand must contain exactly 5 cards")
        }
    }
    
    @Test
    fun `evaluateHand identifies royal flush`() {
        val royalFlush = listOf(
            Card(SPADES, ACE),
            Card(SPADES, KING),
            Card(SPADES, QUEEN),
            Card(SPADES, JACK),
            Card(SPADES, TEN)
        )
        
        val result = handEvaluator.evaluateHand(royalFlush)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.ROYAL_FLUSH)
    }
    
    @Test
    fun `evaluateHand identifies royal flush in different suits`() {
        val suits = listOf(HEARTS, DIAMONDS, CLUBS, SPADES)
        
        suits.forEach { suit ->
            val royalFlush = listOf(
                Card(suit, ACE),
                Card(suit, KING),
                Card(suit, QUEEN),
                Card(suit, JACK),
                Card(suit, TEN)
            )
            
            val result = handEvaluator.evaluateHand(royalFlush)
            assertThat(result).isEqualTo(HandEvaluator.HandRank.ROYAL_FLUSH)
        }
    }
    
    @Test
    fun `evaluateHand identifies straight flush`() {
        val straightFlush = listOf(
            Card(HEARTS, NINE),
            Card(HEARTS, EIGHT),
            Card(HEARTS, SEVEN),
            Card(HEARTS, SIX),
            Card(HEARTS, FIVE)
        )
        
        val result = handEvaluator.evaluateHand(straightFlush)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.STRAIGHT_FLUSH)
    }
    
    @Test
    fun `evaluateHand identifies wheel straight flush (A-2-3-4-5)`() {
        val wheelStraightFlush = listOf(
            Card(DIAMONDS, ACE),
            Card(DIAMONDS, FIVE),
            Card(DIAMONDS, FOUR),
            Card(DIAMONDS, THREE),
            Card(DIAMONDS, TWO)
        )
        
        val result = handEvaluator.evaluateHand(wheelStraightFlush)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.STRAIGHT_FLUSH)
    }
    
    @Test
    fun `evaluateHand identifies four of a kind`() {
        val fourOfAKind = listOf(
            Card(SPADES, KING),
            Card(HEARTS, KING),
            Card(DIAMONDS, KING),
            Card(CLUBS, KING),
            Card(SPADES, TWO)
        )
        
        val result = handEvaluator.evaluateHand(fourOfAKind)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.FOUR_OF_A_KIND)
    }
    
    @Test
    fun `evaluateHand identifies full house`() {
        val fullHouse = listOf(
            Card(SPADES, QUEEN),
            Card(HEARTS, QUEEN),
            Card(DIAMONDS, QUEEN),
            Card(CLUBS, JACK),
            Card(SPADES, JACK)
        )
        
        val result = handEvaluator.evaluateHand(fullHouse)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.FULL_HOUSE)
    }
    
    @Test
    fun `evaluateHand identifies flush`() {
        val flush = listOf(
            Card(CLUBS, ACE),
            Card(CLUBS, JACK),
            Card(CLUBS, NINE),
            Card(CLUBS, SEVEN),
            Card(CLUBS, THREE)
        )
        
        val result = handEvaluator.evaluateHand(flush)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.FLUSH)
    }
    
    @Test
    fun `evaluateHand identifies straight`() {
        val straight = listOf(
            Card(SPADES, TEN),
            Card(HEARTS, NINE),
            Card(DIAMONDS, EIGHT),
            Card(CLUBS, SEVEN),
            Card(SPADES, SIX)
        )
        
        val result = handEvaluator.evaluateHand(straight)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.STRAIGHT)
    }
    
    @Test
    fun `evaluateHand identifies wheel straight (A-2-3-4-5)`() {
        val wheelStraight = listOf(
            Card(SPADES, ACE),
            Card(HEARTS, FIVE),
            Card(DIAMONDS, FOUR),
            Card(CLUBS, THREE),
            Card(SPADES, TWO)
        )
        
        val result = handEvaluator.evaluateHand(wheelStraight)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.STRAIGHT)
    }
    
    @Test
    fun `evaluateHand identifies ace high straight (10-J-Q-K-A)`() {
        val aceHighStraight = listOf(
            Card(SPADES, ACE),
            Card(HEARTS, KING),
            Card(DIAMONDS, QUEEN),
            Card(CLUBS, JACK),
            Card(SPADES, TEN)
        )
        
        val result = handEvaluator.evaluateHand(aceHighStraight)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.STRAIGHT)
    }
    
    @Test
    fun `evaluateHand identifies three of a kind`() {
        val threeOfAKind = listOf(
            Card(SPADES, EIGHT),
            Card(HEARTS, EIGHT),
            Card(DIAMONDS, EIGHT),
            Card(CLUBS, KING),
            Card(SPADES, FOUR)
        )
        
        val result = handEvaluator.evaluateHand(threeOfAKind)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.THREE_OF_A_KIND)
    }
    
    @Test
    fun `evaluateHand identifies two pair`() {
        val twoPair = listOf(
            Card(SPADES, KING),
            Card(HEARTS, KING),
            Card(DIAMONDS, SEVEN),
            Card(CLUBS, SEVEN),
            Card(SPADES, THREE)
        )
        
        val result = handEvaluator.evaluateHand(twoPair)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.TWO_PAIR)
    }
    
    @Test
    fun `evaluateHand identifies jacks or better`() {
        val jacksOrBetterHands = listOf(
            // Pair of Jacks
            listOf(Card(SPADES, JACK), Card(HEARTS, JACK), Card(DIAMONDS, NINE), Card(CLUBS, FIVE), Card(SPADES, TWO)),
            // Pair of Queens
            listOf(Card(SPADES, QUEEN), Card(HEARTS, QUEEN), Card(DIAMONDS, NINE), Card(CLUBS, FIVE), Card(SPADES, TWO)),
            // Pair of Kings
            listOf(Card(SPADES, KING), Card(HEARTS, KING), Card(DIAMONDS, NINE), Card(CLUBS, FIVE), Card(SPADES, TWO)),
            // Pair of Aces
            listOf(Card(SPADES, ACE), Card(HEARTS, ACE), Card(DIAMONDS, NINE), Card(CLUBS, FIVE), Card(SPADES, TWO))
        )
        
        jacksOrBetterHands.forEach { hand ->
            val result = handEvaluator.evaluateHand(hand)
            assertThat(result).isEqualTo(HandEvaluator.HandRank.JACKS_OR_BETTER)
        }
    }
    
    @Test
    fun `evaluateHand identifies high card for non-qualifying pairs`() {
        val nonQualifyingPairs = listOf(
            // Pair of Tens
            listOf(Card(SPADES, TEN), Card(HEARTS, TEN), Card(DIAMONDS, NINE), Card(CLUBS, FIVE), Card(SPADES, TWO)),
            // Pair of Nines
            listOf(Card(SPADES, NINE), Card(HEARTS, NINE), Card(DIAMONDS, EIGHT), Card(CLUBS, FIVE), Card(SPADES, TWO)),
            // Pair of Twos
            listOf(Card(SPADES, TWO), Card(HEARTS, TWO), Card(DIAMONDS, NINE), Card(CLUBS, FIVE), Card(SPADES, THREE))
        )
        
        nonQualifyingPairs.forEach { hand ->
            val result = handEvaluator.evaluateHand(hand)
            assertThat(result).isEqualTo(HandEvaluator.HandRank.HIGH_CARD)
        }
    }
    
    @Test
    fun `evaluateHand identifies high card for no pairs`() {
        val highCard = listOf(
            Card(SPADES, ACE),
            Card(HEARTS, JACK),
            Card(DIAMONDS, NINE),
            Card(CLUBS, SEVEN),
            Card(SPADES, THREE)
        )
        
        val result = handEvaluator.evaluateHand(highCard)
        assertThat(result).isEqualTo(HandEvaluator.HandRank.HIGH_CARD)
    }
    
    @Test
    fun `evaluateHand rejects invalid straight patterns`() {
        val invalidStraights = listOf(
            // Q-K-A-2-3 (invalid wrap-around)
            listOf(Card(SPADES, QUEEN), Card(HEARTS, KING), Card(DIAMONDS, ACE), Card(CLUBS, TWO), Card(SPADES, THREE)),
            // Non-consecutive cards
            listOf(Card(SPADES, TEN), Card(HEARTS, EIGHT), Card(DIAMONDS, SEVEN), Card(CLUBS, SIX), Card(SPADES, FIVE))
        )
        
        invalidStraights.forEach { hand ->
            val result = handEvaluator.evaluateHand(hand)
            assertThat(result).isNotEqualTo(HandEvaluator.HandRank.STRAIGHT)
            assertThat(result).isNotEqualTo(HandEvaluator.HandRank.STRAIGHT_FLUSH)
        }
    }
    
    @Test
    fun `getWinningCards returns all cards for royal flush`() {
        val royalFlush = listOf(
            Card(SPADES, ACE),
            Card(SPADES, KING),
            Card(SPADES, QUEEN),
            Card(SPADES, JACK),
            Card(SPADES, TEN)
        )
        
        val winningCards = handEvaluator.getWinningCards(royalFlush, HandEvaluator.HandRank.ROYAL_FLUSH)
        assertThat(winningCards).containsExactlyElementsIn(royalFlush)
    }
    
    @Test
    fun `getWinningCards returns four cards for four of a kind`() {
        val fourOfAKind = listOf(
            Card(SPADES, KING),
            Card(HEARTS, KING),
            Card(DIAMONDS, KING),
            Card(CLUBS, KING),
            Card(SPADES, TWO)
        )
        
        val winningCards = handEvaluator.getWinningCards(fourOfAKind, HandEvaluator.HandRank.FOUR_OF_A_KIND)
        val expectedWinningCards = fourOfAKind.filter { it.rank == KING }
        
        assertThat(winningCards).containsExactlyElementsIn(expectedWinningCards)
        assertThat(winningCards).hasSize(4)
    }
    
    @Test
    fun `getWinningCards returns three cards for three of a kind`() {
        val threeOfAKind = listOf(
            Card(SPADES, EIGHT),
            Card(HEARTS, EIGHT),
            Card(DIAMONDS, EIGHT),
            Card(CLUBS, KING),
            Card(SPADES, FOUR)
        )
        
        val winningCards = handEvaluator.getWinningCards(threeOfAKind, HandEvaluator.HandRank.THREE_OF_A_KIND)
        val expectedWinningCards = threeOfAKind.filter { it.rank == EIGHT }
        
        assertThat(winningCards).containsExactlyElementsIn(expectedWinningCards)
        assertThat(winningCards).hasSize(3)
    }
    
    @Test
    fun `getWinningCards returns four cards for two pair`() {
        val twoPair = listOf(
            Card(SPADES, KING),
            Card(HEARTS, KING),
            Card(DIAMONDS, SEVEN),
            Card(CLUBS, SEVEN),
            Card(SPADES, THREE)
        )
        
        val winningCards = handEvaluator.getWinningCards(twoPair, HandEvaluator.HandRank.TWO_PAIR)
        val expectedWinningCards = twoPair.filter { it.rank == KING || it.rank == SEVEN }
        
        assertThat(winningCards).containsExactlyElementsIn(expectedWinningCards)
        assertThat(winningCards).hasSize(4)
    }
    
    @Test
    fun `getWinningCards returns two cards for jacks or better`() {
        val jacksOrBetter = listOf(
            Card(SPADES, JACK),
            Card(HEARTS, JACK),
            Card(DIAMONDS, NINE),
            Card(CLUBS, FIVE),
            Card(SPADES, TWO)
        )
        
        val winningCards = handEvaluator.getWinningCards(jacksOrBetter, HandEvaluator.HandRank.JACKS_OR_BETTER)
        val expectedWinningCards = jacksOrBetter.filter { it.rank == JACK }
        
        assertThat(winningCards).containsExactlyElementsIn(expectedWinningCards)
        assertThat(winningCards).hasSize(2)
    }
    
    @Test
    fun `getWinningCards returns empty list for high card`() {
        val highCard = listOf(
            Card(SPADES, ACE),
            Card(HEARTS, JACK),
            Card(DIAMONDS, NINE),
            Card(CLUBS, SEVEN),
            Card(SPADES, THREE)
        )
        
        val winningCards = handEvaluator.getWinningCards(highCard, HandEvaluator.HandRank.HIGH_CARD)
        assertThat(winningCards).isEmpty()
    }
    
    @Test
    fun `hand rank display names are correct`() {
        assertThat(HandEvaluator.HandRank.ROYAL_FLUSH.displayName).isEqualTo("Royal Flush")
        assertThat(HandEvaluator.HandRank.STRAIGHT_FLUSH.displayName).isEqualTo("Straight Flush")
        assertThat(HandEvaluator.HandRank.FOUR_OF_A_KIND.displayName).isEqualTo("Four of a Kind")
        assertThat(HandEvaluator.HandRank.FULL_HOUSE.displayName).isEqualTo("Full House")
        assertThat(HandEvaluator.HandRank.FLUSH.displayName).isEqualTo("Flush")
        assertThat(HandEvaluator.HandRank.STRAIGHT.displayName).isEqualTo("Straight")
        assertThat(HandEvaluator.HandRank.THREE_OF_A_KIND.displayName).isEqualTo("Three of a Kind")
        assertThat(HandEvaluator.HandRank.TWO_PAIR.displayName).isEqualTo("Two Pair")
        assertThat(HandEvaluator.HandRank.JACKS_OR_BETTER.displayName).isEqualTo("Jacks or Better")
        assertThat(HandEvaluator.HandRank.HIGH_CARD.displayName).isEqualTo("High Card")
    }
    
    @Test
    fun `hand evaluation is consistent regardless of card order`() {
        val originalHand = listOf(
            Card(SPADES, ACE),
            Card(SPADES, KING),
            Card(SPADES, QUEEN),
            Card(SPADES, JACK),
            Card(SPADES, TEN)
        )
        
        // Test different orderings of the same hand
        val shuffledHands = listOf(
            originalHand.shuffled(),
            originalHand.reversed(),
            listOf(originalHand[2], originalHand[0], originalHand[4], originalHand[1], originalHand[3])
        )
        
        shuffledHands.forEach { hand ->
            val result = handEvaluator.evaluateHand(hand)
            assertThat(result).isEqualTo(HandEvaluator.HandRank.ROYAL_FLUSH)
        }
    }
}