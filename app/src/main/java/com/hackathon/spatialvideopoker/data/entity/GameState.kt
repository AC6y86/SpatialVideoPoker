package com.hackathon.spatialvideopoker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_state")
data class GameState(
    @PrimaryKey val id: Int = 1,
    val credits: Int = 1000,
    val currentBet: Int = 1,
    val lastWin: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalWinnings: Int = 0,
    val highestWin: Int = 0,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)