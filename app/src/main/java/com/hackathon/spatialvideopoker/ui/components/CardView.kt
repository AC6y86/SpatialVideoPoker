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
import androidx.compose.ui.zIndex
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
    Box(
        modifier = modifier
    ) {
        // HELD indicator above card
        if (isHeld && card != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
                    .background(CasinoRed, RoundedCornerShape(2.dp))
                    .padding(horizontal = 12.dp, vertical = 2.dp)
                    .zIndex(1f)
            ) {
                Text(
                    text = "HELD",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        // Card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = enabled) { onClick() },
            shape = RoundedCornerShape(8.dp), // Slightly more rounded
            colors = CardDefaults.cardColors(
                containerColor = if (card != null) Color.White else Color(0xFF000066) // Pure white for cards
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp // Subtle shadow
            ),
            border = BorderStroke(2.dp, Color.Black) // Slightly thicker border
        ) {
            if (card != null) {
                // Card face
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val cardHeight = maxHeight
                    val cardWidth = maxWidth
                    
                    // Value and suit in upper left corner (proportional to card size)
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(
                                start = cardWidth * 0.06f,
                                top = cardHeight * 0.06f
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = card.rank.symbol,
                            fontSize = (cardHeight.value * 0.14f).sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            color = if (card.suit.color == Card.CardColor.RED) Color(0xFFFF0000) else Color(0xFF000000)
                        )
                        Text(
                            text = card.suit.symbol,
                            fontSize = (cardHeight.value * 0.12f).sp,
                            fontFamily = FontFamily.SansSerif,
                            color = if (card.suit.color == Card.CardColor.RED) Color(0xFFFF0000) else Color(0xFF000000)
                        )
                    }
                    
                    // Bottom suit symbol (smaller size)
                    Text(
                        text = card.suit.symbol,
                        fontSize = (cardHeight.value * 0.30f).sp,
                        fontFamily = FontFamily.SansSerif,
                        color = if (card.suit.color == Card.CardColor.RED) Color(0xFFFF0000) else Color(0xFF000000),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = cardHeight * 0.10f)
                    )
                }
            } else {
                // Card back with diamond pattern
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFCC6666))
                ) {
                    // Create a simple pattern effect
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(8) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                repeat(5) {
                                    Text(
                                        text = "◆",
                                        fontSize = 12.sp,
                                        color = Color(0xFF993333),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private val Color.Companion.Gold: Color
    get() = Color(0xFFFFD700)