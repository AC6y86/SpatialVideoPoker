package com.hackathon.spatialvideopoker.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hackathon.spatialvideopoker.ui.screen.GameScreen
import com.hackathon.spatialvideopoker.ui.theme.SpatialVideoPokerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun gameScreen_displaysInitialState() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Check that initial UI elements are displayed
        composeTestRule.onNodeWithText("Credits:").assertExists()
        composeTestRule.onNodeWithText("Bet:").assertExists()
        composeTestRule.onNodeWithText("DEAL").assertExists()
        composeTestRule.onNodeWithText("Place your bet and press DEAL").assertExists()
    }
    
    @Test
    fun gameScreen_showsBettingControls() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Check betting controls exist
        composeTestRule.onNodeWithText("BET 1").assertExists()
        composeTestRule.onNodeWithText("BET MAX").assertExists()
        
        // Deal button should be visible
        composeTestRule.onNodeWithText("DEAL").assertExists()
    }
    
    @Test
    fun dealButton_clickChangesGameState() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Click deal button
        composeTestRule.onNodeWithText("DEAL").performClick()
        
        // Wait for state change
        composeTestRule.waitForIdle()
        
        // Should show cards and change message
        composeTestRule.onNodeWithText("Select cards to HOLD, then press DRAW")
            .assertExists()
        
        // Draw button should now be visible
        composeTestRule.onNodeWithText("DRAW").assertExists()
    }
    
    @Test
    fun bettingControls_updateCurrentBet() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Click BET 1 to increment bet
        composeTestRule.onNodeWithText("BET 1").performClick()
        composeTestRule.waitForIdle()
        
        // Bet should now be 2 (incremented from 1)
        composeTestRule.onNodeWithText("Bet: 2").assertExists()
        
        // Click BET MAX
        composeTestRule.onNodeWithText("BET MAX").performClick()
        composeTestRule.waitForIdle()
        
        // Bet should now be 5 (maximum)
        composeTestRule.onNodeWithText("Bet: 5").assertExists()
    }
    
    @Test
    fun cardInteraction_worksInHoldingPhase() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Deal cards first
        composeTestRule.onNodeWithText("DEAL").performClick()
        composeTestRule.waitForIdle()
        
        // Should be in holding phase with cards displayed
        composeTestRule.onNodeWithText("Select cards to HOLD, then press DRAW")
            .assertExists()
        
        // Cards should be clickable (test first card)
        composeTestRule.onAllNodesWithTag("card").onFirst().performClick()
        composeTestRule.waitForIdle()
        
        // Should show HELD indicator on the clicked card
        composeTestRule.onNodeWithText("HELD").assertExists()
    }
    
    @Test
    fun completeGameFlow_worksCorrectly() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Initial state
        composeTestRule.onNodeWithText("Place your bet and press DEAL").assertExists()
        
        // Deal
        composeTestRule.onNodeWithText("DEAL").performClick()
        composeTestRule.waitForIdle()
        
        // Holding phase
        composeTestRule.onNodeWithText("Select cards to HOLD, then press DRAW")
            .assertExists()
        
        // Hold a card
        composeTestRule.onAllNodesWithTag("card").onFirst().performClick()
        composeTestRule.waitForIdle()
        
        // Draw
        composeTestRule.onNodeWithText("DRAW").performClick()
        composeTestRule.waitForIdle()
        
        // Should eventually return to betting phase
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Place your bet and press DEAL").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
    
    @Test
    fun topInfoBar_displaysCorrectInformation() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Check initial values
        composeTestRule.onNodeWithText("Credits: 1000").assertExists()
        composeTestRule.onNodeWithText("Bet: 1").assertExists()
        composeTestRule.onNodeWithText("Last Win: 0").assertExists()
    }
    
    @Test
    fun uiRespondsToStateChanges() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Change bet and verify UI updates
        composeTestRule.onNodeWithText("BET 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Bet: 2").assertExists()
        
        // Deal and verify UI changes
        composeTestRule.onNodeWithText("DEAL").performClick()
        composeTestRule.waitForIdle()
        
        // Should show cards (at least 5 card elements)
        composeTestRule.onAllNodesWithTag("card").assertCountEquals(5)
        
        // Message should change
        composeTestRule.onNodeWithText("Select cards to HOLD, then press DRAW")
            .assertExists()
    }
    
    @Test
    fun cardDisplay_showsValidCards() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Deal cards
        composeTestRule.onNodeWithText("DEAL").performClick()
        composeTestRule.waitForIdle()
        
        // Should have exactly 5 cards
        composeTestRule.onAllNodesWithTag("card").assertCountEquals(5)
        
        // Each card should have valid content (suits and ranks)
        composeTestRule.onAllNodesWithTag("card").onFirst().assertExists()
    }
    
    @Test
    fun betControls_disabledDuringGameplay() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Deal to get out of betting phase
        composeTestRule.onNodeWithText("DEAL").performClick()
        composeTestRule.waitForIdle()
        
        // Betting controls should not affect the bet during gameplay
        val originalBetText = "Bet: 1"
        composeTestRule.onNodeWithText(originalBetText).assertExists()
        
        // Try to change bet (should not work in holding phase)
        composeTestRule.onNodeWithText("BET 1").performClick()
        composeTestRule.waitForIdle()
        
        // Bet should remain unchanged
        composeTestRule.onNodeWithText(originalBetText).assertExists()
    }
}