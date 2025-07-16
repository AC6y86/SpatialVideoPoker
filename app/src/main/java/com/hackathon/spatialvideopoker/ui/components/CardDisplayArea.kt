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
                .fillMaxWidth(0.85f) // Make cards take up slightly more horizontal space
                .fillMaxHeight(), // Fill available height
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally), // Adjust spacing between cards
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cards.isEmpty()) {
                // Show placeholder cards when no cards are dealt
                repeat(5) { index ->
                    CardView(
                        card = null,
                        isHeld = false,
                        onClick = { },
                        enabled = false,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.7f) // Standard playing card ratio
                    )
                }
            } else {
                // Show dealt cards
                cards.forEachIndexed { index, card ->
                    CardView(
                        card = card,
                        isHeld = index in heldCardIndices,
                        onClick = { if (enabled) onCardClick(index) },
                        enabled = enabled,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(0.7f)
                    )
                }
            }
        }
    }
}