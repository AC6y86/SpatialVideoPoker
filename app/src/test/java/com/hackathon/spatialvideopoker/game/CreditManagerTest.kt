package com.hackathon.spatialvideopoker.game

import com.hackathon.spatialvideopoker.data.dao.GameStateDao
import com.hackathon.spatialvideopoker.data.entity.GameState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CreditManagerTest {
    
    @Mock
    private lateinit var mockGameStateDao: GameStateDao
    
    private lateinit var creditManager: CreditManager
    
    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        creditManager = CreditManager(mockGameStateDao)
    }
    
    @Test
    fun `loadCredits uses default when no saved state exists`() = runTest {
        whenever(mockGameStateDao.getGameState()).thenReturn(null)
        
        creditManager.loadCredits()
        
        assertThat(creditManager.getCurrentCredits()).isEqualTo(CreditManager.DEFAULT_CREDITS)
    }
    
    @Test
    fun `loadCredits uses saved credits when state exists`() = runTest {
        val savedCredits = 1500
        val gameState = GameState(credits = savedCredits)
        whenever(mockGameStateDao.getGameState()).thenReturn(gameState)
        
        creditManager.loadCredits()
        
        assertThat(creditManager.getCurrentCredits()).isEqualTo(savedCredits)
    }
    
    @Test
    fun `loadCredits uses default when exception occurs`() = runTest {
        whenever(mockGameStateDao.getGameState()).thenThrow(RuntimeException("Database error"))
        
        creditManager.loadCredits()
        
        assertThat(creditManager.getCurrentCredits()).isEqualTo(CreditManager.DEFAULT_CREDITS)
    }
    
    @Test
    fun `observeCredits returns flow of current credits`() = runTest {
        val credits = 750
        val gameState = GameState(credits = credits)
        whenever(mockGameStateDao.observeGameState()).thenReturn(flowOf(gameState))
        
        creditManager.observeCredits().collect { observedCredits ->
            assertThat(observedCredits).isEqualTo(credits)
        }
    }
    
    @Test
    fun `observeCredits returns default when no state exists`() = runTest {
        whenever(mockGameStateDao.observeGameState()).thenReturn(flowOf(null))
        
        creditManager.observeCredits().collect { observedCredits ->
            assertThat(observedCredits).isEqualTo(CreditManager.DEFAULT_CREDITS)
        }
    }
    
    @Test
    fun `deductBet successfully deducts valid amounts`() = runTest {
        val initialCredits = 1000
        val betAmount = 50
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = initialCredits))
        whenever(mockGameStateDao.saveGameState(any())).thenReturn(Unit)
        
        creditManager.loadCredits()
        val success = creditManager.deductBet(betAmount)
        
        assertThat(success).isTrue()
        assertThat(creditManager.getCurrentCredits()).isEqualTo(initialCredits - betAmount)
        verify(mockGameStateDao).saveGameState(any())
    }
    
    @Test
    fun `deductBet fails when insufficient credits`() = runTest {
        val initialCredits = 30
        val betAmount = 50
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = initialCredits))
        
        creditManager.loadCredits()
        val success = creditManager.deductBet(betAmount)
        
        assertThat(success).isFalse()
        assertThat(creditManager.getCurrentCredits()).isEqualTo(initialCredits) // Unchanged
    }
    
    @Test
    fun `deductBet fails for zero or negative amounts`() = runTest {
        val initialCredits = 1000
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = initialCredits))
        
        creditManager.loadCredits()
        
        assertThat(creditManager.deductBet(0)).isFalse()
        assertThat(creditManager.deductBet(-10)).isFalse()
        assertThat(creditManager.getCurrentCredits()).isEqualTo(initialCredits)
    }
    
    @Test
    fun `deductBet works for exact credit amount`() = runTest {
        val initialCredits = 100
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = initialCredits))
        whenever(mockGameStateDao.saveGameState(any())).thenReturn(Unit)
        
        creditManager.loadCredits()
        val success = creditManager.deductBet(initialCredits)
        
        assertThat(success).isTrue()
        assertThat(creditManager.getCurrentCredits()).isEqualTo(0)
    }
    
    @Test
    fun `addWinnings increases credits and updates statistics`() = runTest {
        val initialCredits = 500
        val winnings = 250
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = initialCredits))
        whenever(mockGameStateDao.saveGameState(any())).thenReturn(Unit)
        whenever(mockGameStateDao.addToTotalWinnings(any())).thenReturn(Unit)
        whenever(mockGameStateDao.updateLastWin(any())).thenReturn(Unit)
        whenever(mockGameStateDao.updateHighestWin(any())).thenReturn(Unit)
        
        creditManager.loadCredits()
        creditManager.addWinnings(winnings)
        
        assertThat(creditManager.getCurrentCredits()).isEqualTo(initialCredits + winnings)
        verify(mockGameStateDao).saveGameState(any())
        verify(mockGameStateDao).addToTotalWinnings(winnings)
        verify(mockGameStateDao).updateLastWin(winnings)
        verify(mockGameStateDao).updateHighestWin(winnings)
    }
    
    @Test
    fun `addWinnings ignores zero and negative amounts`() = runTest {
        val initialCredits = 500
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = initialCredits))
        
        creditManager.loadCredits()
        creditManager.addWinnings(0)
        creditManager.addWinnings(-50)
        
        assertThat(creditManager.getCurrentCredits()).isEqualTo(initialCredits)
    }
    
    @Test
    fun `resetCredits sets credits to default value`() = runTest {
        val modifiedCredits = 2500
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = modifiedCredits))
        whenever(mockGameStateDao.saveGameState(any())).thenReturn(Unit)
        
        creditManager.loadCredits()
        assertThat(creditManager.getCurrentCredits()).isEqualTo(modifiedCredits)
        
        creditManager.resetCredits()
        assertThat(creditManager.getCurrentCredits()).isEqualTo(CreditManager.DEFAULT_CREDITS)
        verify(mockGameStateDao).saveGameState(any())
    }
    
    @Test
    fun `getCurrentCredits returns current credit amount`() = runTest {
        val credits = 1337
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = credits))
        
        creditManager.loadCredits()
        assertThat(creditManager.getCurrentCredits()).isEqualTo(credits)
    }
    
    @Test
    fun `hasCredits returns true when credits above minimum`() = runTest {
        val credits = 50
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = credits))
        
        creditManager.loadCredits()
        assertThat(creditManager.hasCredits()).isTrue()
    }
    
    @Test
    fun `hasCredits returns false when credits at minimum`() = runTest {
        val credits = CreditManager.MINIMUM_CREDITS
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = credits))
        
        creditManager.loadCredits()
        assertThat(creditManager.hasCredits()).isFalse()
    }
    
    @Test
    fun `canAffordBet returns correct affordability`() = runTest {
        val credits = 100
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = credits))
        
        creditManager.loadCredits()
        
        assertThat(creditManager.canAffordBet(50)).isTrue()
        assertThat(creditManager.canAffordBet(100)).isTrue()
        assertThat(creditManager.canAffordBet(101)).isFalse()
        assertThat(creditManager.canAffordBet(0)).isFalse()
        assertThat(creditManager.canAffordBet(-10)).isFalse()
    }
    
    @Test
    fun `constants have expected values`() {
        assertThat(CreditManager.DEFAULT_CREDITS).isEqualTo(1000)
        assertThat(CreditManager.MINIMUM_CREDITS).isEqualTo(0)
    }
    
    @Test
    fun `credit operations maintain consistency`() = runTest {
        val initialCredits = 1000
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = initialCredits))
        whenever(mockGameStateDao.saveGameState(any())).thenReturn(Unit)
        whenever(mockGameStateDao.addToTotalWinnings(any())).thenReturn(Unit)
        whenever(mockGameStateDao.updateLastWin(any())).thenReturn(Unit)
        whenever(mockGameStateDao.updateHighestWin(any())).thenReturn(Unit)
        
        creditManager.loadCredits()
        
        // Sequence of operations: bet, win, bet, win
        assertThat(creditManager.deductBet(100)).isTrue()
        assertThat(creditManager.getCurrentCredits()).isEqualTo(900)
        
        creditManager.addWinnings(200)
        assertThat(creditManager.getCurrentCredits()).isEqualTo(1100)
        
        assertThat(creditManager.deductBet(50)).isTrue()
        assertThat(creditManager.getCurrentCredits()).isEqualTo(1050)
        
        creditManager.addWinnings(75)
        assertThat(creditManager.getCurrentCredits()).isEqualTo(1125)
        
        // Verify affordability checks work correctly
        assertThat(creditManager.canAffordBet(1000)).isTrue()
        assertThat(creditManager.canAffordBet(1200)).isFalse()
        assertThat(creditManager.hasCredits()).isTrue()
    }
    
    @Test
    fun `edge case - exact credit deduction and restoration`() = runTest {
        val initialCredits = 50
        whenever(mockGameStateDao.getGameState()).thenReturn(GameState(credits = initialCredits))
        whenever(mockGameStateDao.saveGameState(any())).thenReturn(Unit)
        whenever(mockGameStateDao.addToTotalWinnings(any())).thenReturn(Unit)
        whenever(mockGameStateDao.updateLastWin(any())).thenReturn(Unit)
        whenever(mockGameStateDao.updateHighestWin(any())).thenReturn(Unit)
        
        creditManager.loadCredits()
        
        // Deduct all credits
        assertThat(creditManager.deductBet(50)).isTrue()
        assertThat(creditManager.getCurrentCredits()).isEqualTo(0)
        assertThat(creditManager.hasCredits()).isFalse()
        
        // Add winnings to restore credits
        creditManager.addWinnings(100)
        assertThat(creditManager.getCurrentCredits()).isEqualTo(100)
        assertThat(creditManager.hasCredits()).isTrue()
        assertThat(creditManager.canAffordBet(75)).isTrue()
    }
}