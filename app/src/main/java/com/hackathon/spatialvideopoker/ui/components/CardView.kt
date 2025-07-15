package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.model.Card

@Composable
fun CardView(
    card: Card?,
    isHeld: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = enabled) { onClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (card != null) Color.White else Color(0xFF1E5128)
            ),
            border = if (isHeld) BorderStroke(4.dp, Color.Yellow) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (card.suit.color == Card.CardColor.RED) Color.Red else Color.Black
                        )
                        
                        // Suit
                        Text(
                            text = card.suit.symbol,
                            fontSize = 36.sp,
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
                // Card back
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF2E7D32)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VIDEO\nPOKER",
                        color = Color.Gold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }
            }
        }
        
        // HELD indicator
        if (isHeld && card != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
                    .background(Color.Yellow, RoundedCornerShape(4.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "HELD",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private val Color.Companion.Gold: Color
    get() = Color(0xFFFFD700)