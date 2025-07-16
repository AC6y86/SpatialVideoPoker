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
        PayoutRow("ROYAL FLUSH", listOf(5000, 16000, 24000, 32000, 40000), HandEvaluator.HandRank.ROYAL_FLUSH),
        PayoutRow("STRAIGHT FLUSH", listOf(500, 1000, 1500, 2000, 2500), HandEvaluator.HandRank.STRAIGHT_FLUSH),
        PayoutRow("4 OF A KIND", listOf(250, 500, 750, 1000, 1250), HandEvaluator.HandRank.FOUR_OF_A_KIND),
        PayoutRow("FULL HOUSE", listOf(90, 180, 270, 360, 450), HandEvaluator.HandRank.FULL_HOUSE),
        PayoutRow("FLUSH", listOf(60, 120, 180, 240, 300), HandEvaluator.HandRank.FLUSH),
        PayoutRow("STRAIGHT", listOf(40, 80, 120, 160, 200), HandEvaluator.HandRank.STRAIGHT),
        PayoutRow("3 OF A KIND", listOf(30, 60, 90, 120, 150), HandEvaluator.HandRank.THREE_OF_A_KIND),
        PayoutRow("TWO PAIR", listOf(20, 40, 60, 80, 100), HandEvaluator.HandRank.TWO_PAIR),
        PayoutRow("JACKS OR", listOf(10, 20, 30, 40, 50), HandEvaluator.HandRank.JACKS_OR_BETTER)
    )
    
    Column(
        modifier = modifier
            .background(Color.Black)
    ) {
            // Payout rows
            payouts.forEach { (handName, payoutAmounts, handRank) ->
                val isWinningHand = isWinning && lastWinHandRank == handRank
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hand name - left aligned
                    Box(
                        modifier = Modifier
                            .width(95.dp)
                            .fillMaxHeight()
                            .background(Color(0xFF000066))
                            .padding(start = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = handName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Yellow,
                            fontFamily = FontFamily.SansSerif
                        )
                    }
                    
                    // Payouts in columns
                    payoutAmounts.forEachIndexed { index, payout ->
                        val coinCount = index + 1
                        val isCurrentBetColumn = coinCount == currentBet
                        val isMaxBet = coinCount == 5
                        
                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .fillMaxHeight()
                                .background(
                                    when {
                                        index == 0 -> Color(0xFFCC0000) // Red for first column
                                        isMaxBet -> Color(0xFFFFD700) // Gold for max bet
                                        else -> Color(0xFF0000AA) // Darker blue for others
                                    }
                                )
,
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = payout.toString(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = when {
                                    isMaxBet -> Color.Black
                                    else -> Color.White
                                },
                                fontFamily = FontFamily.SansSerif,
                                textAlign = TextAlign.Center
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