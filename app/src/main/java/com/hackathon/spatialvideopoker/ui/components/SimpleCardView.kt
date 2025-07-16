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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.model.Card

@Composable
fun SimpleCardView(
    card: Card?,
    isHeld: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    animationDelay: Int = 0,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(card) {
        if (card != null && animationDelay > 0) {
            kotlinx.coroutines.delay(animationDelay.toLong())
        }
        visible = card != null
    }
    
    // Card entrance animation
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "card_scale"
    )
    
    // Click animation
    var clicked by remember { mutableStateOf(false) }
    val clickScale by animateFloatAsState(
        targetValue = if (clicked) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioHighBouncy
        ),
        label = "click_scale"
    )
    
    Box(
        modifier = modifier.scale(scale * clickScale)
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = enabled && card != null) {
                    clicked = true
                    onClick()
                },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (card != null) Color.White else Color(0xFF1E5128)
            ),
            border = if (isHeld && card != null) BorderStroke(4.dp, Color.Yellow) else null,
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
                // Card back or placeholder
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
        
        // HELD indicator with animation
        if (isHeld && card != null) {
            val heldScale by animateFloatAsState(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "held_scale"
            )
            
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
                    .scale(heldScale)
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
    
    LaunchedEffect(clicked) {
        if (clicked) {
            kotlinx.coroutines.delay(100)
            clicked = false
        }
    }
}

private val Color.Companion.Gold: Color
    get() = Color(0xFFFFD700)