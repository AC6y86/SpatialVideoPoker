package com.hackathon.spatialvideopoker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.game.HandEvaluator
import com.hackathon.spatialvideopoker.game.PayoutCalculator

@Composable
fun PaytableScreen(
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1B5E20)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAYTABLE - JACKS OR BETTER",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow
                    )
                    
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.8f)
                        )
                    ) {
                        Text("CLOSE", color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Paytable content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Coin headers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        for (coins in 1..5) {
                            Text(
                                text = "$coins COIN${if (coins > 1) "S" else ""}",
                                modifier = Modifier.width(80.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    
                    // Payout rows
                    PayoutRow("ROYAL FLUSH", listOf(250, 500, 750, 1000, 4000), isRoyal = true)
                    PayoutRow("STRAIGHT FLUSH", listOf(50, 100, 150, 200, 250))
                    PayoutRow("FOUR OF A KIND", listOf(25, 50, 75, 100, 125))
                    PayoutRow("FULL HOUSE", listOf(9, 18, 27, 36, 45))
                    PayoutRow("FLUSH", listOf(6, 12, 18, 24, 30))
                    PayoutRow("STRAIGHT", listOf(4, 8, 12, 16, 20))
                    PayoutRow("THREE OF A KIND", listOf(3, 6, 9, 12, 15))
                    PayoutRow("TWO PAIR", listOf(2, 4, 6, 8, 10))
                    PayoutRow("JACKS OR BETTER", listOf(1, 2, 3, 4, 5))
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Information section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "HOW TO PLAY",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Yellow
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Bet 1 to 5 coins per hand\n" +
                                      "• You need at least a pair of Jacks to win\n" +
                                      "• Royal Flush pays 4000 coins on max bet!\n" +
                                      "• Hold the cards you want to keep\n" +
                                      "• Draw to replace unheld cards",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PayoutRow(
    handName: String,
    payouts: List<Int>,
    isRoyal: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(
                if (isRoyal) {
                    Modifier
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Yellow.copy(alpha = 0.2f),
                                    Color.Yellow.copy(alpha = 0.1f),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.Yellow.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        )
                } else Modifier
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = handName,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            fontWeight = if (isRoyal) FontWeight.Bold else FontWeight.Normal,
            color = if (isRoyal) Color.Yellow else Color.White
        )
        
        payouts.forEachIndexed { index, payout ->
            val isMaxBet = index == 4
            Text(
                text = payout.toString(),
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontWeight = if (isMaxBet && isRoyal) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isMaxBet && isRoyal -> Color.Yellow
                    isMaxBet -> Color(0xFF90EE90)
                    else -> Color.White
                }
            )
        }
    }
}