package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.game.HandEvaluator
import com.hackathon.spatialvideopoker.ui.theme.*

@Composable
fun IntegratedPaytable(
    currentBet: Int,
    lastWinHandRank: HandEvaluator.HandRank? = null,
    isWinning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val payouts = listOf(
        PayoutRow("ROYAL FLUSH", listOf(250, 500, 750, 1000, 4000), HandEvaluator.HandRank.ROYAL_FLUSH),
        PayoutRow("STRAIGHT FLUSH", listOf(50, 100, 150, 200, 250), HandEvaluator.HandRank.STRAIGHT_FLUSH),
        PayoutRow("4 OF A KIND", listOf(25, 50, 75, 100, 125), HandEvaluator.HandRank.FOUR_OF_A_KIND),
        PayoutRow("FULL HOUSE", listOf(9, 18, 27, 36, 45), HandEvaluator.HandRank.FULL_HOUSE),
        PayoutRow("FLUSH", listOf(6, 12, 18, 24, 30), HandEvaluator.HandRank.FLUSH),
        PayoutRow("STRAIGHT", listOf(4, 8, 12, 16, 20), HandEvaluator.HandRank.STRAIGHT),
        PayoutRow("3 OF A KIND", listOf(3, 6, 9, 12, 15), HandEvaluator.HandRank.THREE_OF_A_KIND),
        PayoutRow("TWO PAIR", listOf(2, 4, 6, 8, 10), HandEvaluator.HandRank.TWO_PAIR),
        PayoutRow("JACKS OR BETTER", listOf(1, 2, 3, 4, 5), HandEvaluator.HandRank.JACKS_OR_BETTER)
    )
    
    Column(
        modifier = modifier
            .background(Color.Black)
            .border(2.dp, Color.Yellow)
    ) {
            // Payout rows
            payouts.forEach { (handName, payoutAmounts, handRank) ->
                val isWinningHand = isWinning && lastWinHandRank == handRank
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(23.dp), // Reduced from 26dp to 23dp (about 10% shorter)
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hand name - left aligned
                    Box(
                        modifier = Modifier
                            .weight(0.35f)  // Use flexible weight instead of fixed width
                            .fillMaxHeight()
                            .background(Color(0xFF000066))
                            .padding(start = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = handName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Yellow,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    
                    // Yellow divider after hand name
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(Color.Yellow)
                    )
                    
                    // Payouts in columns with yellow dividers between each
                    payoutAmounts.forEachIndexed { index, payout ->
                        val coinCount = index + 1
                        val isCurrentBetColumn = coinCount == currentBet
                        val isMaxBet = coinCount == 5
                        
                        Box(
                            modifier = Modifier
                                .weight(0.13f)  // 5 columns sharing 65% of width (0.65/5 = 0.13)
                                .fillMaxHeight()
                                .background(
                                    when {
                                        isCurrentBetColumn -> Color(0xFFCC0000) // Red for current bet
                                        else -> Color(0xFF000066) // Blue for others
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = payout.toString(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Yellow,
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        // Yellow divider after each column (except the last one)
                        if (index < payoutAmounts.size - 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(2.dp)
                                    .background(Color.Yellow)
                            )
                        }
                    }
                }
            }
    }
}

private data class PayoutRow(
    val handName: String,
    val payouts: List<Int>,
    val handRank: HandEvaluator.HandRank
)