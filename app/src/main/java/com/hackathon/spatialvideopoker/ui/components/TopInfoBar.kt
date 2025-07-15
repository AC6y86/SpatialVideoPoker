package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TopInfoBar(
    credits: Int,
    currentBet: Int,
    lastWin: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.7f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoItem(
                label = "CREDITS",
                value = credits.toString(),
                color = Color.Yellow
            )
            
            InfoItem(
                label = "BET",
                value = currentBet.toString(),
                color = Color.White
            )
            
            InfoItem(
                label = "WIN",
                value = lastWin.toString(),
                color = if (lastWin > 0) Color.Green else Color.White
            )
        }
    }
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
    }
}