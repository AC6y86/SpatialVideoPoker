package com.hackathon.spatialvideopoker.model

import java.security.SecureRandom

class Deck {
    private val cards = mutableListOf<Card>()
    private val dealtCards = mutableListOf<Card>()
    private var forcedHand: List<Card>? = null
    
    init {
        reset()
    }
    
    fun reset() {
        cards.clear()
        dealtCards.clear()
        
        // Create a standard 52-card deck
        Card.Suit.values().forEach { suit ->
            Card.Rank.values().forEach { rank ->
                cards.add(Card(suit, rank))
            }
        }
    }
    
    fun shuffle(random: SecureRandom = SecureRandom()) {
        // Fisher-Yates shuffle algorithm
        for (i in cards.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = cards[i]
            cards[i] = cards[j]
            cards[j] = temp
        }
    }
    
    fun deal(count: Int): List<Card> {
        // If we have a forced hand and dealing 5 cards (initial deal), return it
        if (forcedHand != null && count == 5) {
            val result = forcedHand!!
            forcedHand = null // Clear after use
            dealtCards.addAll(result)
            return result
        }
        
        require(count <= cards.size) { "Not enough cards remaining in deck" }
        
        val dealtHand = mutableListOf<Card>()
        repeat(count) {
            val card = cards.removeAt(0)
            dealtCards.add(card)
            dealtHand.add(card)
        }
        return dealtHand
    }
    
    fun cardsRemaining(): Int = cards.size
    
    fun getDealtCards(): List<Card> = dealtCards.toList()
    
    fun returnCardsToDeck(cardsToReturn: List<Card>) {
        cards.addAll(cardsToReturn)
        dealtCards.removeAll(cardsToReturn)
    }
    
    fun setForcedHand(cards: List<Card>) {
        require(cards.size == 5) { "Forced hand must contain exactly 5 cards" }
        forcedHand = cards
    }
}