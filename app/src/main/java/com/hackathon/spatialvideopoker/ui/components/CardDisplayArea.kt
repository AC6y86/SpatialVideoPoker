package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hackathon.spatialvideopoker.model.Card

@Composable
fun CardDisplayArea(
    cards: List<Card>,
    heldCardIndices: Set<Int>,
    onCardClick: (Int) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cards.isEmpty()) {
                // Show placeholder cards when no cards are dealt
                repeat(5) { index ->
                    SimpleCardView(
                        card = null,
                        isHeld = false,
                        onClick = { },
                        enabled = false,
                        animationDelay = 0,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.7f) // Standard playing card ratio
                    )
                }
            } else {
                // Show dealt cards
                cards.forEachIndexed { index, card ->
                    SimpleCardView(
                        card = card,
                        isHeld = index in heldCardIndices,
                        onClick = { if (enabled) onCardClick(index) },
                        enabled = enabled,
                        animationDelay = index * 100, // Staggered animation
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.7f)
                    )
                }
            }
        }
    }
}