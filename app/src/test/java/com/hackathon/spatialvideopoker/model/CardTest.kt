package com.hackathon.spatialvideopoker.model

import com.hackathon.spatialvideopoker.model.Card.Rank.*
import com.hackathon.spatialvideopoker.model.Card.Suit.*
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CardTest {
    
    @Test
    fun `card creation with valid suit and rank`() {
        val card = Card(SPADES, ACE)
        
        assertThat(card.suit).isEqualTo(SPADES)
        assertThat(card.rank).isEqualTo(ACE)
        assertThat(card.isHeld).isFalse()
    }
    
    @Test
    fun `card creation with isHeld parameter`() {
        val heldCard = Card(HEARTS, KING, isHeld = true)
        val nonHeldCard = Card(DIAMONDS, QUEEN, isHeld = false)
        
        assertThat(heldCard.isHeld).isTrue()
        assertThat(nonHeldCard.isHeld).isFalse()
    }
    
    @Test
    fun `card equality for same suit and rank`() {
        val card1 = Card(CLUBS, JACK)
        val card2 = Card(CLUBS, JACK)
        val card3 = Card(CLUBS, JACK, isHeld = true)
        
        assertThat(card1).isEqualTo(card2)
        assertThat(card1).isNotEqualTo(card3) // isHeld affects equality
    }
    
    @Test
    fun `card inequality for different suit or rank`() {
        val aceOfSpades = Card(SPADES, ACE)
        val aceOfHearts = Card(HEARTS, ACE)
        val kingOfSpades = Card(SPADES, KING)
        
        assertThat(aceOfSpades).isNotEqualTo(aceOfHearts)
        assertThat(aceOfSpades).isNotEqualTo(kingOfSpades)
        assertThat(aceOfHearts).isNotEqualTo(kingOfSpades)
    }
    
    @Test
    fun `getDisplayName returns correct format`() {
        val testCases = mapOf(
            Card(SPADES, ACE) to "A♠",
            Card(HEARTS, KING) to "K♥",
            Card(DIAMONDS, QUEEN) to "Q♦",
            Card(CLUBS, JACK) to "J♣",
            Card(SPADES, TEN) to "10♠",
            Card(HEARTS, TWO) to "2♥"
        )
        
        testCases.forEach { (card, expectedDisplay) ->
            assertThat(card.getDisplayName()).isEqualTo(expectedDisplay)
        }
    }
    
    @Test
    fun `toString returns same as getDisplayName`() {
        val card = Card(DIAMONDS, NINE)
        assertThat(card.toString()).isEqualTo(card.getDisplayName())
    }
    
    @Test
    fun `suit enum has correct symbols and colors`() {
        assertThat(HEARTS.symbol).isEqualTo("♥")
        assertThat(HEARTS.color).isEqualTo(Card.CardColor.RED)
        
        assertThat(DIAMONDS.symbol).isEqualTo("♦")
        assertThat(DIAMONDS.color).isEqualTo(Card.CardColor.RED)
        
        assertThat(CLUBS.symbol).isEqualTo("♣")
        assertThat(CLUBS.color).isEqualTo(Card.CardColor.BLACK)
        
        assertThat(SPADES.symbol).isEqualTo("♠")
        assertThat(SPADES.color).isEqualTo(Card.CardColor.BLACK)
    }
    
    @Test
    fun `rank enum has correct values and symbols`() {
        val expectedRanks = mapOf(
            TWO to Pair(2, "2"),
            THREE to Pair(3, "3"),
            FOUR to Pair(4, "4"),
            FIVE to Pair(5, "5"),
            SIX to Pair(6, "6"),
            SEVEN to Pair(7, "7"),
            EIGHT to Pair(8, "8"),
            NINE to Pair(9, "9"),
            TEN to Pair(10, "10"),
            JACK to Pair(11, "J"),
            QUEEN to Pair(12, "Q"),
            KING to Pair(13, "K"),
            ACE to Pair(14, "A")
        )
        
        expectedRanks.forEach { (rank, expectedValueAndSymbol) ->
            assertThat(rank.value).isEqualTo(expectedValueAndSymbol.first)
            assertThat(rank.symbol).isEqualTo(expectedValueAndSymbol.second)
        }
    }
    
    @Test
    fun `rank values are in ascending order`() {
        val ranks = Card.Rank.values()
        
        for (i in 0 until ranks.size - 1) {
            assertThat(ranks[i + 1].value).isGreaterThan(ranks[i].value)
        }
    }
    
    @Test
    fun `ace has highest rank value`() {
        val allRanks = Card.Rank.values()
        val aceValue = ACE.value
        
        allRanks.forEach { rank ->
            if (rank != ACE) {
                assertThat(aceValue).isGreaterThan(rank.value)
            }
        }
    }
    
    @Test
    fun `two has lowest rank value`() {
        val allRanks = Card.Rank.values()
        val twoValue = TWO.value
        
        allRanks.forEach { rank ->
            if (rank != TWO) {
                assertThat(twoValue).isLessThan(rank.value)
            }
        }
    }
    
    @Test
    fun `face cards have correct values for poker rankings`() {
        assertThat(JACK.value).isEqualTo(11)
        assertThat(QUEEN.value).isEqualTo(12)
        assertThat(KING.value).isEqualTo(13)
        assertThat(ACE.value).isEqualTo(14)
    }
    
    @Test
    fun `all suits have different symbols`() {
        val suits = Card.Suit.values()
        val symbols = suits.map { it.symbol }
        
        assertThat(symbols).hasSize(4)
        assertThat(symbols.toSet()).hasSize(4) // No duplicates
    }
    
    @Test
    fun `red and black suits are correctly categorized`() {
        val redSuits = Card.Suit.values().filter { it.color == Card.CardColor.RED }
        val blackSuits = Card.Suit.values().filter { it.color == Card.CardColor.BLACK }
        
        assertThat(redSuits).containsExactly(HEARTS, DIAMONDS)
        assertThat(blackSuits).containsExactly(CLUBS, SPADES)
    }
    
    @Test
    fun `card copy functionality with different isHeld value`() {
        val originalCard = Card(SPADES, ACE, isHeld = false)
        val heldCard = originalCard.copy(isHeld = true)
        
        assertThat(heldCard.suit).isEqualTo(originalCard.suit)
        assertThat(heldCard.rank).isEqualTo(originalCard.rank)
        assertThat(heldCard.isHeld).isTrue()
        assertThat(originalCard.isHeld).isFalse()
    }
    
    @Test
    fun `hashCode is consistent for equal cards`() {
        val card1 = Card(HEARTS, QUEEN)
        val card2 = Card(HEARTS, QUEEN)
        
        assertThat(card1.hashCode()).isEqualTo(card2.hashCode())
    }
    
    @Test
    fun `hashCode is different for different cards`() {
        val card1 = Card(HEARTS, QUEEN)
        val card2 = Card(SPADES, QUEEN)
        val card3 = Card(HEARTS, KING)
        
        assertThat(card1.hashCode()).isNotEqualTo(card2.hashCode())
        assertThat(card1.hashCode()).isNotEqualTo(card3.hashCode())
    }
}