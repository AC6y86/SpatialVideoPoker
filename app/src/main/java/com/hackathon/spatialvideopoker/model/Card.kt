package com.hackathon.spatialvideopoker.model

data class Card(
    val suit: Suit,
    val rank: Rank,
    val isHeld: Boolean = false
) {
    enum class Suit(val symbol: String, val color: CardColor) {
        HEARTS("♥", CardColor.RED),
        DIAMONDS("♦", CardColor.RED),
        CLUBS("♣", CardColor.BLACK),
        SPADES("♠", CardColor.BLACK)
    }
    
    enum class Rank(val value: Int, val symbol: String) {
        TWO(2, "2"),
        THREE(3, "3"),
        FOUR(4, "4"),
        FIVE(5, "5"),
        SIX(6, "6"),
        SEVEN(7, "7"),
        EIGHT(8, "8"),
        NINE(9, "9"),
        TEN(10, "10"),
        JACK(11, "J"),
        QUEEN(12, "Q"),
        KING(13, "K"),
        ACE(14, "A")
    }
    
    enum class CardColor {
        RED, BLACK
    }
    
    fun getDisplayName(): String = "${rank.symbol}${suit.symbol}"
    
    override fun toString(): String = getDisplayName()
}