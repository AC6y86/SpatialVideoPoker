package com.hackathon.spatialvideopoker.data.dao

import androidx.room.*
import com.hackathon.spatialvideopoker.data.entity.GameState
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStateDao {
    @Query("SELECT * FROM game_state WHERE id = 1")
    suspend fun getGameState(): GameState?
    
    @Query("SELECT * FROM game_state WHERE id = 1")
    fun observeGameState(): Flow<GameState?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(gameState: GameState)
    
    @Update
    suspend fun updateGameState(gameState: GameState)
    
    @Query("UPDATE game_state SET credits = :credits WHERE id = 1")
    suspend fun updateCredits(credits: Int)
    
    @Query("UPDATE game_state SET currentBet = :bet WHERE id = 1")
    suspend fun updateCurrentBet(bet: Int)
    
    @Query("UPDATE game_state SET lastWin = :amount WHERE id = 1")
    suspend fun updateLastWin(amount: Int)
    
    @Query("UPDATE game_state SET totalGamesPlayed = totalGamesPlayed + 1, lastPlayedTimestamp = :timestamp WHERE id = 1")
    suspend fun incrementGamesPlayed(timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE game_state SET totalWinnings = totalWinnings + :amount WHERE id = 1")
    suspend fun addToTotalWinnings(amount: Int)
    
    @Query("UPDATE game_state SET highestWin = :amount WHERE id = 1 AND :amount > highestWin")
    suspend fun updateHighestWin(amount: Int)
}