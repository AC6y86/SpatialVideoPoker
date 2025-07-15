package com.hackathon.spatialvideopoker.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "statistics")
data class Statistics(
    @PrimaryKey val handType: String,
    val count: Int = 0,
    val lastSeen: Long = 0,
    val bestPayout: Int = 0
)