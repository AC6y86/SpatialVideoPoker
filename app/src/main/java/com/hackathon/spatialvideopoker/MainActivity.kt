package com.hackathon.spatialvideopoker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hackathon.spatialvideopoker.ui.screen.GameScreen
import com.hackathon.spatialvideopoker.ui.theme.SpatialVideoPokerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
    }
}