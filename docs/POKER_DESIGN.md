# Android Jacks or Better Video Poker Game Specification

## 1. Game Design & Mechanics

### 1.1 Core Gameplay
- **Game Type**: Jacks or Better Video Poker
- **Players**: Single player vs. house
- **Orientation**: Landscape mode
- **Platform**: Android mobile devices

### 1.2 Game Rules
1. Player places bet (1-5 coins)
2. Five cards are dealt face up
3. Player selects which cards to hold (0-5 cards)
4. Remaining cards are discarded and replaced with new cards
5. Final hand is evaluated for winning combinations
6. Payouts are awarded based on hand strength and bet amount

### 1.3 Hand Rankings (Lowest to Highest)
1. **Jacks or Better** - Pair of Jacks, Queens, Kings, or Aces
2. **Two Pair** - Two different pairs
3. **Three of a Kind** - Three cards of same rank
4. **Straight** - Five consecutive cards (any suit)
5. **Flush** - Five cards of same suit (any order)
6. **Full House** - Three of a kind + pair
7. **Four of a Kind** - Four cards of same rank
8. **Straight Flush** - Five consecutive cards of same suit
9. **Royal Flush** - 10, J, Q, K, A of same suit

### 1.4 Payout Table (9/6 Full-Pay Standard)
| Hand | 1 Coin | 2 Coins | 3 Coins | 4 Coins | 5 Coins |
|------|--------|---------|---------|---------|---------|
| Royal Flush | 250 | 500 | 750 | 1000 | 4000 |
| Straight Flush | 50 | 100 | 150 | 200 | 250 |
| Four of a Kind | 25 | 50 | 75 | 100 | 125 |
| Full House | 9 | 18 | 27 | 36 | 45 |
| Flush | 6 | 12 | 18 | 24 | 30 |
| Straight | 4 | 8 | 12 | 16 | 20 |
| Three of a Kind | 3 | 6 | 9 | 12 | 15 |
| Two Pair | 2 | 4 | 6 | 8 | 10 |
| Jacks or Better | 1 | 2 | 3 | 4 | 5 |

### 1.5 Betting System
- **Coin Values**: Configurable (default: 1 credit = 1 coin)
- **Bet Range**: 1-5 coins per hand
- **Max Bet Advantage**: Royal Flush pays disproportionately higher at 5-coin bet
- **Starting Credits**: 1000 credits (configurable)

## 2. Technical Architecture

### 2.1 Platform Requirements
- **Target Android Version**: API 24+ (Android 7.0+)
- **Minimum RAM**: 2GB
- **Storage**: 100MB minimum
- **Screen Resolution**: 1280x720 minimum (landscape)

### 2.2 Technology Stack
- **Platform**: Native Android (API 24+)
- **Programming Language**: Kotlin (primary) with Java interop
- **UI Framework**: Jetpack Compose (modern) or Android Views (traditional)
- **Graphics**: Android Canvas API with custom drawables
- **Database**: Room persistence library (SQLite wrapper)
- **Audio**: MediaPlayer and SoundPool APIs
- **Build System**: Gradle with Android Plugin

### 2.3 Architecture Components
```
┌─────────────────────────────────────────────────────────┐
│              MainActivity / GameFragment                │
│                   (Android Lifecycle)                  │
└─────────────────────────────────────────────────────────┘
                          │
┌─────────────────┬─────────────────┬─────────────────────┐
│  GameViewModel  │  CardRenderer   │   AudioService      │
│   (MVVM Logic)  │  (Canvas API)   │ (MediaPlayer/Pool)  │
└─────────────────┴─────────────────┴─────────────────────┘
         │                 │                 │
┌─────────────────┬─────────────────┬─────────────────────┐
│   Card Model    │ Touch Gestures  │  PreferenceManager  │
│ (Data Classes)  │   (View Events) │   (SharedPrefs)     │
└─────────────────┴─────────────────┴─────────────────────┘
         │                 │                 │
┌─────────────────┬─────────────────┬─────────────────────┐
│ Room Database   │ Hand Evaluator  │   Statistics DAO    │
│   (Entities)    │  (Pure Logic)   │   (Room Queries)    │
└─────────────────┴─────────────────┴─────────────────────┘
```

### 2.4 Security & Fairness
- **Random Number Generator**: `java.security.SecureRandom` for cryptographically secure randomness
- **Card Shuffling**: Fisher-Yates shuffle algorithm with SecureRandom implementation
- **Data Integrity**: Room database constraints and local data validation
- **Code Obfuscation**: ProGuard/R8 enabled for release builds
- **No Network Dependencies**: Fully offline gameplay

## 3. User Interface Design

### 3.1 Layout Structure (Landscape)
```
┌─────────────────────────────────────────────────────────────┐
│ [Credits: 1000] [Bet: 5] [Win: 0]              [Menu] [?]   │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│    ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐       │
│    │   A   │ │   K   │ │   Q   │ │   J   │ │  10   │       │
│    │   ♠   │ │   ♠   │ │   ♠   │ │   ♠   │ │   ♠   │       │
│    └───────┘ └───────┘ └───────┘ └───────┘ └───────┘       │
│    [HOLD]     [HOLD]     [HOLD]     [HOLD]     [HOLD]       │
│                                                             │
│                    ROYAL FLUSH!                             │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│ [BET 1] [BET 2] [BET 3] [BET 4] [MAX BET] [DEAL/DRAW]       │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 Visual Design Elements
- **Card Design**: Standard 52-card deck with clear, readable faces
- **Color Scheme**: Traditional casino green background with gold accents
- **Typography**: Clear, high-contrast fonts for readability
- **Animations**: Smooth card dealing, flipping, and payout effects
- **Feedback**: Visual highlights for held cards and winning hands

### 3.3 Screen Components

#### 3.3.1 Game Area
- **Card Display**: 5 card positions with standard playing card graphics
- **Hold Buttons**: Toggle buttons below each card
- **Hand Result**: Large text display for winning hands
- **Animation Space**: Area for card dealing and payout effects

#### 3.3.2 Control Panel
- **Bet Buttons**: 1-5 coin bet selection
- **Max Bet**: Quick 5-coin bet button
- **Deal/Draw**: Primary action button
- **Auto-Hold**: Optional suggestion system toggle

#### 3.3.3 Information Display
- **Credits**: Current player balance
- **Bet Amount**: Current wager
- **Last Win**: Most recent payout
- **Paytable**: Accessible payout reference

### 3.4 Menu System
- **Main Menu**: New Game, Continue, Settings, Help, Statistics, Exit
- **Settings**: Sound, Music, Auto-Hold, Card Speed, Reset Game
- **Help**: Rules, Hand Rankings, Strategy Tips
- **Statistics**: Games Played, Hands Won, Best Hand, Total Winnings

## 4. User Experience

### 4.1 Interaction Flow
1. **Game Start**: Player sees dealt cards and betting interface
2. **Card Selection**: Tap cards to toggle hold state
3. **Draw Action**: Tap Deal/Draw button to replace non-held cards
4. **Result Display**: Winning hands highlighted with payout animation
5. **Next Hand**: Automatic progression to new hand

### 4.2 Touch Controls
- **Card Selection**: Single tap to hold/unhold
- **Button Interaction**: Standard button taps with visual feedback
- **Gesture Support**: Swipe gestures for menu navigation
- **Haptic Feedback**: Subtle vibration for card selection and wins

### 4.3 Accessibility Features
- **High Contrast Mode**: Enhanced visibility option
- **Large Text Option**: Scalable font sizes
- **Color-Blind Support**: Alternative card suit indicators
- **Screen Reader**: Compatible UI elements and descriptions

### 4.4 Performance Optimization
- **60 FPS Target**: Smooth animations and transitions
- **Memory Management**: Efficient sprite loading and disposal
- **Battery Optimization**: Reduced CPU usage during idle states
- **Loading Times**: Sub-2 second game start

## 5. Core Features

### 5.1 Gameplay Features
- **Standard Jacks or Better**: Classic video poker rules
- **Multiple Difficulty Levels**: Adjustable starting credits
- **Auto-Hold Suggestions**: Optional optimal play hints
- **Quick Play Mode**: Rapid-fire hands for experienced players
- **Practice Mode**: Play without affecting statistics

### 5.2 Progression System
- **Statistics Tracking**: Comprehensive gameplay metrics
- **Achievement System**: Goals and milestones
- **High Score Records**: Best single hands and sessions
- **Streak Tracking**: Consecutive wins and losses

### 5.3 Customization Options
- **Card Back Designs**: Multiple decorative options
- **Table Themes**: Various background and color schemes
- **Audio Settings**: Individual sound and music controls
- **Game Speed**: Adjustable animation timing

### 5.4 Help & Tutorial
- **Interactive Tutorial**: Step-by-step gameplay introduction
- **Strategy Guide**: Basic optimal play recommendations
- **Hand Ranking Reference**: Quick lookup of winning combinations
- **Rules Explanation**: Complete game rules and procedures

## 6. Testing Strategy

### 6.1 Unit Testing
- **Game Logic Testing**: Hand evaluation accuracy
- **Card Dealing**: RNG distribution and shuffle validation
- **Payout Calculations**: Correct winnings computation
- **State Management**: Save/load functionality
- **Input Validation**: Bet limits and invalid actions

### 6.2 Integration Testing
- **UI Components**: Button interactions and state updates
- **Audio Integration**: Sound effects and music playback
- **Settings Persistence**: Configuration save/restore
- **Scene Transitions**: Menu and game flow
- **Data Consistency**: Statistics and progress tracking

### 6.3 Performance Testing
- **Memory Usage**: RAM consumption monitoring
- **CPU Performance**: Frame rate stability
- **Battery Impact**: Power consumption measurement
- **Storage Efficiency**: Save file size optimization
- **Loading Performance**: Startup and scene transition times

## 7. Technical Requirements

### 7.1 Data Persistence
- **Save Game State**: Current credits, settings, statistics
- **Local Storage**: SQLite database for game data
- **Backup System**: Export/import save data functionality
- **Data Validation**: Integrity checks and error recovery

### 7.2 Performance Specifications
- **Minimum Hardware**: 2GB RAM, 1.5GHz processor
- **Target Frame Rate**: 60 FPS during gameplay
- **Memory Footprint**: <500MB RAM usage
- **Storage Requirements**: 100MB installation size
- **Battery Life**: <5% drain per hour of gameplay

### 7.3 Distribution
- **Installation Method**: APK side-loading
- **No App Store**: Independent distribution
- **Version Updates**: Manual APK replacement
- **Compatibility**: Android 7.0+ devices
- **No Network Requirements**: Fully offline operation

### 7.4 Development Tools
- **IDE**: Android Studio (recommended) or IntelliJ IDEA
- **Language**: Kotlin 1.9+ with Android Gradle Plugin 8.0+
- **Build System**: Gradle with Android DSL and version catalogs
- **Version Control**: Git repository with .gitignore for Android
- **Testing Framework**: JUnit 5, Espresso (UI), and Robolectric (unit)
- **Static Analysis**: Ktlint, Detekt for code quality
- **CI/CD**: GitHub Actions or Jenkins for automated builds

## 8. Implementation Timeline

### Phase 1: Android Foundation & Core Logic (4-6 weeks)
- Android project setup with Gradle and dependencies
- Room database schema and DAO implementation
- Card model classes and hand evaluation logic
- GameViewModel with LiveData/StateFlow
- Basic UI layouts (Compose or XML) for game screen
- SecureRandom implementation for card shuffling

### Phase 2: UI/UX & Features (3-4 weeks)
- Custom card rendering with Canvas API
- Touch gesture handling and animations
- MediaPlayer/SoundPool audio integration
- Settings screen with PreferenceManager
- Menu navigation with fragments or Compose Navigation
- Statistics tracking and display

### Phase 3: Testing & Android Optimization (2-3 weeks)
- Unit tests with JUnit and Robolectric
- UI tests with Espresso
- Memory profiling and optimization
- Battery usage optimization
- ProGuard/R8 configuration for release builds
- APK generation and testing on multiple devices

**Total Estimated Timeline: 9-13 weeks**

---

*This specification serves as the complete technical and design foundation for developing an Android Jacks or Better video poker game.*