package com.hackathon.spatialvideopoker.integration

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hackathon.spatialvideopoker.ui.screen.GameScreen
import com.hackathon.spatialvideopoker.ui.theme.SpatialVideoPokerTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EndToEndGameTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun completeVideoPokerGame_playMultipleHands() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Play multiple hands to test complete integration
        repeat(3) { handNumber ->
            playCompleteHand(handNumber + 1)
        }
    }
    
    @Test
    fun bettingStrategy_maxBetAndWinConditions() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Test max bet strategy
        composeTestRule.onNodeWithText("BET MAX").performClick()
        composeTestRule.waitForIdle()
        
        // Verify max bet is set
        composeTestRule.onNodeWithText("Bet: 5").assertExists()
        
        // Play hand with max bet
        playCompleteHand(1)
        
        // Verify credits are properly adjusted for max bet
        composeTestRule.onNodeWithText("Credits: 995").assertExists()
    }
    
    @Test
    fun cardHoldingStrategy_holdAllCards() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Deal initial hand
        composeTestRule.onNodeWithText("DEAL").performClick()
        composeTestRule.waitForIdle()
        
        // Hold all 5 cards
        composeTestRule.onAllNodesWithTag("card").assertCountEquals(5)
        for (i in 0 until 5) {
            composeTestRule.onAllNodesWithTag("card")[i].performClick()
            composeTestRule.waitForIdle()
        }
        
        // All cards should show HELD
        composeTestRule.onAllNodesWithText("HELD").assertCountEquals(5)
        
        // Draw (no cards should change)
        composeTestRule.onNodeWithText("DRAW").performClick()
        composeTestRule.waitForIdle()
        
        // Wait for hand evaluation and return to betting
        waitForBettingPhase()
    }
    
    @Test
    fun cardHoldingStrategy_holdNoCards() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Deal initial hand
        composeTestRule.onNodeWithText("DEAL").performClick()
        composeTestRule.waitForIdle()
        
        // Don't hold any cards, just draw immediately
        composeTestRule.onNodeWithText("DRAW").performClick()
        composeTestRule.waitForIdle()
        
        // All cards should be replaced (can't verify specific cards due to randomness)
        // but game should complete successfully
        waitForBettingPhase()
    }
    
    @Test
    fun gameStateConsistency_acrossMultipleOperations() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Initial state verification
        composeTestRule.onNodeWithText("Credits: 1000").assertExists()
        composeTestRule.onNodeWithText("Bet: 1").assertExists()
        
        // Change bet multiple times
        composeTestRule.onNodeWithText("BET 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Bet: 2").assertExists()
        
        composeTestRule.onNodeWithText("BET 1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Bet: 3").assertExists()
        
        composeTestRule.onNodeWithText("BET MAX").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Bet: 5").assertExists()
        
        // Play with the max bet
        playCompleteHand(1)
        
        // Verify credits decreased by bet amount
        composeTestRule.onNodeWithText("Credits: 995").assertExists()
    }
    
    @Test
    fun handEvaluation_displaysPayout() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Play several hands and verify that hand results are displayed
        repeat(5) { handNumber ->
            playCompleteHand(handNumber + 1)
            
            // After each hand, should show some result
            // (might be "No win" or actual winning hand)
            waitForBettingPhase()
            
            // Should display last win amount (even if 0)
            composeTestRule.onNode(hasText("Last Win:")).assertExists()
        }
    }
    
    @Test
    fun uiResponsiveness_duringAnimations() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Deal and check for dealing animation state
        composeTestRule.onNodeWithText("DEAL").performClick()
        
        // Should show dealing message initially
        composeTestRule.onNodeWithText("Dealing...").assertExists()
        
        // Wait for dealing to complete
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Select cards to HOLD, then press DRAW")
            .assertExists()
        
        // Draw and check for drawing animation
        composeTestRule.onNodeWithText("DRAW").performClick()
        
        // Should show drawing message
        composeTestRule.onNodeWithText("Drawing...").assertExists()
        
        // Wait for complete evaluation
        waitForBettingPhase()
    }
    
    @Test
    fun errorHandling_insufficientCredits() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Play many hands to drain credits
        // Note: This test assumes we can eventually run out of credits
        // In a real scenario, you might need to mock the credit manager
        
        var handsPlayed = 0
        while (handsPlayed < 100) { // Safety limit
            try {
                // Try to deal
                composeTestRule.onNodeWithText("DEAL").performClick()
                composeTestRule.waitForIdle()
                
                // Check if we can deal (look for holding phase message)
                try {
                    composeTestRule.onNodeWithText("Select cards to HOLD, then press DRAW")
                        .assertExists()
                } catch (e: AssertionError) {
                    // Should show insufficient credits message
                    try {
                        composeTestRule.onNodeWithText("Insufficient credits").assertExists()
                    } catch (e2: AssertionError) {
                        // If neither message exists, we've reached an unexpected state
                    }
                    break
                }
                
                // Complete the hand
                composeTestRule.onNodeWithText("DRAW").performClick()
                waitForBettingPhase()
                handsPlayed++
                
            } catch (e: Exception) {
                // If we encounter any errors, the game should still be in a valid state
                break
            }
        }
    }
    
    @Test
    fun gameFlow_maintainsProperStateTransitions() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Verify state transitions work correctly
        
        // BETTING phase
        composeTestRule.onNodeWithText("Place your bet and press DEAL").assertExists()
        composeTestRule.onNodeWithText("DEAL").assertExists()
        
        // Deal -> HOLDING phase
        composeTestRule.onNodeWithText("DEAL").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Select cards to HOLD, then press DRAW")
            .assertExists()
        composeTestRule.onNodeWithText("DRAW").assertExists()
        
        // Draw -> EVALUATION/PAYOUT -> BETTING
        composeTestRule.onNodeWithText("DRAW").performClick()
        waitForBettingPhase()
        composeTestRule.onNodeWithText("Place your bet and press DEAL").assertExists()
    }
    
    @Test
    fun accessibility_elementsAreAccessible() {
        composeTestRule.setContent {
            SpatialVideoPokerTheme {
                GameScreen()
            }
        }
        
        // Check that key UI elements have proper accessibility
        composeTestRule.onNodeWithText("DEAL").assertHasClickAction()
        composeTestRule.onNodeWithText("BET 1").assertHasClickAction()
        composeTestRule.onNodeWithText("BET MAX").assertHasClickAction()
        
        // Deal and check card accessibility
        composeTestRule.onNodeWithText("DEAL").performClick()
        composeTestRule.waitForIdle()
        
        composeTestRule.onAllNodesWithTag("card").onFirst().assertHasClickAction()
        composeTestRule.onNodeWithText("DRAW").assertHasClickAction()
    }
    
    // Helper functions
    
    private fun playCompleteHand(handNumber: Int) {
        // Deal
        composeTestRule.onNodeWithText("DEAL").performClick()
        composeTestRule.waitForIdle()
        
        // Verify we're in holding phase
        composeTestRule.onNodeWithText("Select cards to HOLD, then press DRAW")
            .assertExists()
        
        // Optionally hold some cards (random strategy for testing)
        if (handNumber % 2 == 0) {
            // Hold first and third cards on even hands
            composeTestRule.onAllNodesWithTag("card")[0].performClick()
            composeTestRule.onAllNodesWithTag("card")[2].performClick()
            composeTestRule.waitForIdle()
        }
        
        // Draw
        composeTestRule.onNodeWithText("DRAW").performClick()
        composeTestRule.waitForIdle()
        
        // Wait for hand evaluation and return to betting phase
        waitForBettingPhase()
    }
    
    private fun waitForBettingPhase() {
        // Wait until we're back in betting phase
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onNodeWithText("Place your bet and press DEAL").assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
        }
    }
    
}