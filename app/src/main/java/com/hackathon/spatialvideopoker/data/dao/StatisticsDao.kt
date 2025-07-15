package com.hackathon.spatialvideopoker.data.dao

import androidx.room.*
import com.hackathon.spatialvideopoker.data.entity.Statistics
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM statistics")
    suspend fun getAllStatistics(): List<Statistics>
    
    @Query("SELECT * FROM statistics")
    fun observeAllStatistics(): Flow<List<Statistics>>
    
    @Query("SELECT * FROM statistics WHERE handType = :handType")
    suspend fun getStatisticForHand(handType: String): Statistics?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatistic(statistic: Statistics)
    
    @Update
    suspend fun updateStatistic(statistic: Statistics)
    
    @Query("UPDATE statistics SET count = count + 1, lastSeen = :timestamp WHERE handType = :handType")
    suspend fun incrementHandCount(handType: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE statistics SET bestPayout = :payout WHERE handType = :handType AND :payout > bestPayout")
    suspend fun updateBestPayout(handType: String, payout: Int)
    
    @Query("DELETE FROM statistics")
    suspend fun clearAllStatistics()
    
    @Transaction
    suspend fun recordHand(handType: String, payout: Int) {
        val existing = getStatisticForHand(handType)
        if (existing == null) {
            insertStatistic(
                Statistics(
                    handType = handType,
                    count = 1,
                    lastSeen = System.currentTimeMillis(),
                    bestPayout = payout
                )
            )
        } else {
            incrementHandCount(handType)
            if (payout > existing.bestPayout) {
                updateBestPayout(handType, payout)
            }
        }
    }
}