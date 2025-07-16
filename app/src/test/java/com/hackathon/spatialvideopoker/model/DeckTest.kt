package com.hackathon.spatialvideopoker.model

import com.hackathon.spatialvideopoker.model.Card.Rank.*
import com.hackathon.spatialvideopoker.model.Card.Suit.*
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import java.security.SecureRandom

class DeckTest {
    
    private lateinit var deck: Deck
    
    @Before
    fun setUp() {
        deck = Deck()
    }
    
    @Test
    fun `new deck contains exactly 52 cards`() {
        assertThat(deck.cardsRemaining()).isEqualTo(52)
    }
    
    @Test
    fun `new deck contains all unique cards`() {
        val allCards = deck.deal(52)
        
        // Check that we have all suits and ranks
        val suits = allCards.map { it.suit }.toSet()
        val ranks = allCards.map { it.rank }.toSet()
        
        assertThat(suits).containsExactlyElementsIn(Card.Suit.values())
        assertThat(ranks).containsExactlyElementsIn(Card.Rank.values())
        
        // Check for duplicates
        assertThat(allCards.toSet()).hasSize(52)
    }
    
    @Test
    fun `new deck contains exactly one of each card combination`() {
        val allCards = deck.deal(52)
        
        // Group by suit and rank combination
        val cardCombinations = allCards.groupBy { "${it.suit}_${it.rank}" }
        
        // Each combination should appear exactly once
        cardCombinations.values.forEach { cards ->
            assertThat(cards).hasSize(1)
        }
        
        // Should have exactly 52 unique combinations (4 suits × 13 ranks)
        assertThat(cardCombinations).hasSize(52)
    }
    
    @Test
    fun `deal returns correct number of cards`() {
        val hand = deck.deal(5)
        
        assertThat(hand).hasSize(5)
        assertThat(deck.cardsRemaining()).isEqualTo(47)
    }
    
    @Test
    fun `deal removes cards from deck`() {
        val initialCount = deck.cardsRemaining()
        val cardsDealt = 3
        
        val hand = deck.deal(cardsDealt)
        
        assertThat(hand).hasSize(cardsDealt)
        assertThat(deck.cardsRemaining()).isEqualTo(initialCount - cardsDealt)
    }
    
    @Test
    fun `deal throws exception when not enough cards remaining`() {
        // Deal 50 cards, leaving only 2
        deck.deal(50)
        assertThat(deck.cardsRemaining()).isEqualTo(2)
        
        try {
            deck.deal(3) // Try to deal 3 when only 2 remain
            assert(false) { "Expected exception when dealing more cards than available" }
        } catch (e: IllegalArgumentException) {
            assertThat(e.message).contains("Not enough cards remaining in deck")
        }
    }
    
    @Test
    fun `deal zero cards returns empty list`() {
        val hand = deck.deal(0)
        
        assertThat(hand).isEmpty()
        assertThat(deck.cardsRemaining()).isEqualTo(52)
    }
    
    @Test
    fun `dealt cards are tracked correctly`() {
        val hand1 = deck.deal(3)
        val hand2 = deck.deal(2)
        
        val dealtCards = deck.getDealtCards()
        
        assertThat(dealtCards).hasSize(5)
        assertThat(dealtCards).containsExactlyElementsIn(hand1 + hand2)
    }
    
    @Test
    fun `shuffle changes card order`() {
        // Deal all cards to get initial order
        val originalOrder = deck.deal(52)
        
        // Reset and shuffle with fixed seed for reproducibility
        deck.reset()
        deck.shuffle(SecureRandom.getInstance("SHA1PRNG").apply { setSeed(12345) })
        val shuffledOrder = deck.deal(52)
        
        assertThat(shuffledOrder).hasSize(52)
        assertThat(shuffledOrder.toSet()).containsExactlyElementsIn(originalOrder.toSet())
        assertThat(shuffledOrder).isNotEqualTo(originalOrder)
    }
    
    @Test
    fun `shuffle with different seeds produces different orders`() {
        deck.shuffle(SecureRandom.getInstance("SHA1PRNG").apply { setSeed(123) })
        val order1 = deck.deal(10)
        
        deck.reset()
        deck.shuffle(SecureRandom.getInstance("SHA1PRNG").apply { setSeed(456) })
        val order2 = deck.deal(10)
        
        // Very unlikely to be the same with good shuffling
        assertThat(order1).isNotEqualTo(order2)
    }
    
    @Test
    fun `multiple shuffles on same deck work correctly`() {
        val firstShuffle = SecureRandom.getInstance("SHA1PRNG").apply { setSeed(111) }
        val secondShuffle = SecureRandom.getInstance("SHA1PRNG").apply { setSeed(222) }
        
        deck.shuffle(firstShuffle)
        val order1 = deck.deal(5).toList()
        
        deck.returnCardsToDeck(order1)
        deck.shuffle(secondShuffle)
        val order2 = deck.deal(5).toList()
        
        assertThat(order1).isNotEqualTo(order2)
    }
    
    @Test
    fun `reset restores deck to full 52 cards`() {
        deck.deal(30)
        assertThat(deck.cardsRemaining()).isEqualTo(22)
        
        deck.reset()
        assertThat(deck.cardsRemaining()).isEqualTo(52)
        assertThat(deck.getDealtCards()).isEmpty()
    }
    
    @Test
    fun `reset clears dealt cards history`() {
        val dealtCards = deck.deal(10)
        assertThat(deck.getDealtCards()).hasSize(10)
        
        deck.reset()
        assertThat(deck.getDealtCards()).isEmpty()
    }
    
    @Test
    fun `returnCardsToDeck adds cards back to deck`() {
        val originalCount = deck.cardsRemaining()
        val hand = deck.deal(5)
        
        assertThat(deck.cardsRemaining()).isEqualTo(originalCount - 5)
        
        deck.returnCardsToDeck(hand)
        
        assertThat(deck.cardsRemaining()).isEqualTo(originalCount)
    }
    
    @Test
    fun `returnCardsToDeck removes cards from dealt cards list`() {
        val hand = deck.deal(5)
        assertThat(deck.getDealtCards()).hasSize(5)
        
        deck.returnCardsToDeck(hand.take(3))
        
        assertThat(deck.getDealtCards()).hasSize(2)
        assertThat(deck.getDealtCards()).containsExactlyElementsIn(hand.drop(3))
    }
    
    @Test
    fun `returnCardsToDeck with empty list has no effect`() {
        val hand = deck.deal(5)
        val remainingBefore = deck.cardsRemaining()
        val dealtCountBefore = deck.getDealtCards().size
        
        deck.returnCardsToDeck(emptyList())
        
        assertThat(deck.cardsRemaining()).isEqualTo(remainingBefore)
        assertThat(deck.getDealtCards()).hasSize(dealtCountBefore)
    }
    
    @Test
    fun `partial card return works correctly`() {
        val hand = deck.deal(5)
        val cardsToReturn = hand.take(2)
        val cardsToKeep = hand.drop(2)
        
        deck.returnCardsToDeck(cardsToReturn)
        
        assertThat(deck.cardsRemaining()).isEqualTo(49) // 52 - 5 + 2
        assertThat(deck.getDealtCards()).containsExactlyElementsIn(cardsToKeep)
    }
    
    @Test
    fun `deck supports complete game cycle`() {
        // Simulate a poker hand
        val initialHand = deck.deal(5)
        assertThat(deck.cardsRemaining()).isEqualTo(47)
        
        // Return 3 cards (discard)
        val cardsToDiscard = initialHand.take(3)
        deck.returnCardsToDeck(cardsToDiscard)
        assertThat(deck.cardsRemaining()).isEqualTo(50)
        
        // Deal 3 new cards
        val newCards = deck.deal(3)
        assertThat(newCards).hasSize(3)
        assertThat(deck.cardsRemaining()).isEqualTo(47)
        
        // Final hand should have 5 cards total
        val finalHand = initialHand.drop(3) + newCards
        assertThat(finalHand).hasSize(5)
    }
    
    @Test
    fun `dealt cards are removed in FIFO order`() {
        val firstCard = deck.deal(1)[0]
        val secondCard = deck.deal(1)[0]
        val thirdCard = deck.deal(1)[0]
        
        val dealtCards = deck.getDealtCards()
        
        assertThat(dealtCards[0]).isEqualTo(firstCard)
        assertThat(dealtCards[1]).isEqualTo(secondCard)
        assertThat(dealtCards[2]).isEqualTo(thirdCard)
    }
    
    @Test
    fun `edge case - deal all cards then reset`() {
        val allCards = deck.deal(52)
        assertThat(deck.cardsRemaining()).isEqualTo(0)
        assertThat(allCards).hasSize(52)
        
        deck.reset()
        assertThat(deck.cardsRemaining()).isEqualTo(52)
        
        // Should be able to deal again
        val newHand = deck.deal(5)
        assertThat(newHand).hasSize(5)
    }
    
    @Test
    fun `deck maintains card integrity across operations`() {
        // Deal some cards
        val hand1 = deck.deal(10)
        
        // Return some cards
        deck.returnCardsToDeck(hand1.take(5))
        
        // Deal more cards
        val hand2 = deck.deal(15)
        
        // All cards dealt should be unique
        val allDealtCards = deck.getDealtCards()
        assertThat(allDealtCards.toSet()).hasSize(allDealtCards.size)
        
        // Total cards in deck + dealt should equal 52
        val totalCards = deck.cardsRemaining() + allDealtCards.size
        assertThat(totalCards).isEqualTo(52)
    }
}