package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hackathon.spatialvideopoker.model.Card
import com.hackathon.spatialvideopoker.ui.theme.*

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
        
        // Show banner overlay when no cards are dealt
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .background(
                        Color(0xFFFF0000).copy(alpha = 0.9f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(3.dp, Color(0xFFFFFF00), RoundedCornerShape(8.dp))
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CLICK DEAL TO START",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFFF00),
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}