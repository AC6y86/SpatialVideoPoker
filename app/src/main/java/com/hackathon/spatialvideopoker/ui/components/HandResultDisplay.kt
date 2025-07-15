package com.hackathon.spatialvideopoker.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.game.HandEvaluator

@Composable
fun HandResultDisplay(
    handRank: HandEvaluator.HandRank,
    payout: Int,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(handRank) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible && payout > 0,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = Color(0xFFFFD700).copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = handRank.displayName.uppercase(),
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}