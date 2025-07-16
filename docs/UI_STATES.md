# UI States - Banner and Overlay System

## System Overview

The Video Poker game uses two distinct UI messaging systems:

### **Banner System**
- **Purpose**: Shows current game state and what's happening
- **Display**: Single line text at top of screen
- **Rule**: Only one banner at a time
- **Visibility**: Always visible except during initial betting phase

### **Overlay System**
- **Purpose**: Transition screens between hands
- **Display**: Red box with yellow border overlaying the cards
- **Rule**: Only appears at end of hand before new hand starts
- **Behavior**: Does not move cards, truly overlaid

## Complete Game Flow

### **State 1: Initial Game Start**
- **Banner**: None (clean state)
- **Overlay**: "CLICK DEAL TO START" (overlays empty card placeholders)
- **Cards**: Empty placeholders
- **User Action**: Press DEAL button

### **State 2: Dealing Phase**
- **Banner**: "Dealing..."
- **Overlay**: None
- **Cards**: Animation of cards being dealt
- **Duration**: ~500ms
- **Transition**: Automatic to HOLDING phase

### **State 3: Holding Phase**
- **Banner**: "Select cards to HOLD, then press DRAW"
- **Overlay**: None
- **Cards**: 5 dealt cards, clickable to hold/unhold
- **User Action**: Select cards to hold, then press DRAW

### **State 4: Drawing Phase**
- **Banner**: "Drawing..."
- **Overlay**: None
- **Cards**: Animation of non-held cards being replaced
- **Duration**: ~500ms
- **Transition**: Automatic to EVALUATING phase

### **State 5: Evaluating Phase**
- **Banner**: "Evaluating..."
- **Overlay**: None
- **Cards**: Final 5 cards displayed
- **Duration**: Brief moment
- **Transition**: Automatic to PAYOUT phase

### **State 6A: Payout Phase - Winning Hand**
- **Banner**: Hand name (e.g., "JACKS OR BETTER", "FULL HOUSE", "ROYAL FLUSH")
- **Overlay**: None
- **Cards**: Winning cards displayed
- **Additional**: "WIN [amount]" counter animation in lower left
- **Duration**: Counter animation (1 to final amount at 20/second) + 1 second
- **Transition**: Automatic to BETTING phase after animation

### **State 6B: Payout Phase - Losing Hand**
- **Banner**: "Lose"
- **Overlay**: None
- **Cards**: Losing cards displayed
- **Duration**: ~1 second
- **Transition**: Automatic to BETTING phase

### **State 7: Post-Hand Betting Phase**
- **Banner**: Winning hand name OR "Lose" (persists from previous phase)
- **Overlay**: "CLICK DEAL TO START" (overlays the previous hand's cards)
- **Cards**: Previous hand's cards still visible
- **User Action**: Press DEAL button to start new hand

## Banner Messages by Game Phase

| Game Phase | Banner Text | Notes |
|------------|-------------|-------|
| BETTING (initial) | *(none)* | Clean state at game start |
| DEALING | "Dealing..." | Shows during card dealing animation |
| HOLDING | "Select cards to HOLD, then press DRAW" | Player interaction phase |
| DRAWING | "Drawing..." | Shows during card replacement animation |
| EVALUATING | "Evaluating..." | Brief evaluation phase |
| PAYOUT (win) | Hand name (e.g., "JACKS OR BETTER") | Shows winning hand type |
| PAYOUT (lose) | "Lose" | Shows for losing hands |
| BETTING (post-hand) | Previous result (hand name or "Lose") | **Persists until new DEAL** |

## Overlay Behavior

| State | Overlay Visible | Overlay Text | Cards Affected |
|-------|----------------|--------------|----------------|
| Initial game | ✅ | "CLICK DEAL TO START" | Empty placeholders |
| During hand | ❌ | *(none)* | Various states |
| Post-hand | ✅ | "CLICK DEAL TO START" | Previous hand visible |

## Key Design Principles

1. **Single Banner Rule**: Only one banner message at a time
2. **Banner Persistence**: Hand result banner stays until new hand starts
3. **Overlay Isolation**: Overlays never move cards, always truly overlaid
4. **Clear Transitions**: Each phase has distinct visual feedback
5. **User-Driven Progression**: Player controls when to start new hand

## Technical Implementation

### Banner Logic (GameScreen.kt)
```kotlin
text = when {
    gameState.gamePhase == GameStateMachine.GamePhase.DEALING -> "Dealing..."
    gameState.gamePhase == GameStateMachine.GamePhase.HOLDING -> "Select cards to HOLD, then press DRAW"
    gameState.gamePhase == GameStateMachine.GamePhase.DRAWING -> "Drawing..."
    gameState.gamePhase == GameStateMachine.GamePhase.EVALUATING -> "Evaluating..."
    gameState.gamePhase == GameStateMachine.GamePhase.PAYOUT && gameState.lastWinAmount > 0 -> 
        gameState.lastHandRank!!.displayName.uppercase()
    gameState.gamePhase == GameStateMachine.GamePhase.PAYOUT && gameState.lastWinAmount == 0 -> "Lose"
    gameState.gamePhase == GameStateMachine.GamePhase.BETTING && gameState.lastHandRank != null -> 
        if (gameState.lastWinAmount > 0) gameState.lastHandRank!!.displayName.uppercase() else "Lose"
    else -> ""
}
```

### Overlay Logic (CardDisplayArea.kt)
```kotlin
showDealBanner = gameState.gamePhase == GameStateMachine.GamePhase.BETTING && gameState.dealtCards.isNotEmpty()
```

## WIN Animation Integration

- **Winning Hands**: WIN counter animates from 1 to final amount at 20/second
- **Position**: Lower left corner, does not overlap with cards
- **Duration**: Synchronized with banner display
- **Persistence**: WIN amount stays visible until new hand starts

## Card Positioning Guarantee

- **Cards never move**: All banners and overlays are positioned absolutely
- **Consistent layout**: Cards always in same position regardless of UI state
- **True overlay**: Overlays use z-index and absolute positioning within card area