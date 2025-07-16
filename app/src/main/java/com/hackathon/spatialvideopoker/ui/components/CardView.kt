package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.model.Card
import com.hackathon.spatialvideopoker.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CardView(
    card: Card?,
    isHeld: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Card
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(enabled = enabled) { onClick() },
            shape = RoundedCornerShape(8.dp), // Slightly more rounded
            colors = CardDefaults.cardColors(
                containerColor = if (card != null) CardWhite else Color(0xFF000066) // Dark blue for empty cards
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp // More prominent shadow
            ),
            border = BorderStroke(1.dp, Color.Black) // Add black border
        ) {
            if (card != null) {
                // Card face
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Rank
                        Text(
                            text = card.rank.symbol,
                            fontSize = 36.sp, // Smaller font
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (card.suit.color == Card.CardColor.RED) Color.Red else Color.Black
                        )
                        
                        // Suit
                        Text(
                            text = card.suit.symbol,
                            fontSize = 32.sp, // Smaller font
                            fontFamily = FontFamily.SansSerif,
                            color = if (card.suit.color == Card.CardColor.RED) Color.Red else Color.Black
                        )
                    }
                    
                    // Rank in corners
                    Text(
                        text = card.rank.symbol,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (card.suit.color == Card.CardColor.RED) Color.Red else Color.Black,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    )
                    
                    Text(
                        text = card.rank.symbol,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (card.suit.color == Card.CardColor.RED) Color.Red else Color.Black,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    )
                }
            } else {
                // Card back - dark blue with pattern
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF000066))
                ) {
                    // Add simple pattern or logo
                    Text(
                        text = "VIDEO\nPOKER",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0000CC),
                        fontFamily = FontFamily.SansSerif,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
        
        // HELD indicator below card
        if (isHeld && card != null) {
            Box(
                modifier = Modifier
                    .background(CasinoRed, RoundedCornerShape(2.dp))
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "HELD",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            // Empty space to maintain layout
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

private val Color.Companion.Gold: Color
    get() = Color(0xFFFFD700)