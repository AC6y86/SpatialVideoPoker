package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    onPaytableClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onPaytableClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.Yellow
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Paytable"
                    )
                }
                
                IconButton(
                    onClick = onSettingsClick,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.Yellow
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
            
            // Game info
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
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
        when (label) {
            "CREDITS" -> {
                AnimatedCreditDisplay(
                    targetCredits = value.toIntOrNull() ?: 0
                )
            }
            "WIN" -> {
                val winAmount = value.toIntOrNull() ?: 0
                val animatedColor by animateColorAsState(
                    targetValue = if (winAmount > 0) Color.Green else Color.White,
                    animationSpec = tween(500),
                    label = "win_color"
                )
                Text(
                    text = value,
                    color = animatedColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            else -> {
                Text(
                    text = value,
                    color = color,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}