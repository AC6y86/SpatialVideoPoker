package com.hackathon.spatialvideopoker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hackathon.spatialvideopoker.data.dao.GameStateDao
import com.hackathon.spatialvideopoker.data.dao.StatisticsDao
import com.hackathon.spatialvideopoker.data.entity.GameState
import com.hackathon.spatialvideopoker.data.entity.Statistics

@Database(
    entities = [GameState::class, Statistics::class],
    version = 1,
    exportSchema = false
)
abstract class VideoPokerDatabase : RoomDatabase() {
    abstract fun gameStateDao(): GameStateDao
    abstract fun statisticsDao(): StatisticsDao
    
    companion object {
        @Volatile
        private var INSTANCE: VideoPokerDatabase? = null
        
        fun getDatabase(context: Context): VideoPokerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VideoPokerDatabase::class.java,
                    "video_poker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}