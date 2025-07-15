# Android Video Poker Implementation Guide

This document provides a detailed, step-by-step implementation plan for the Android Jacks or Better Video Poker game optimized for high-end tablets.

## Overview

The implementation is divided into three main phases:
1. **Android Foundation & Core Logic** - Setting up the project and implementing game mechanics
2. **UI/UX & Features** - Building the user interface and adding features
3. **Testing & Optimization** - Ensuring quality and performance

Each step includes specific deliverables and testing criteria to ensure measurable progress.

## Phase 1: Android Foundation & Core Logic

### 1.1 Project Setup

#### Step 1.1.1: Create Android Studio Project
**Deliverable**: New Kotlin-based Android project
- Create new project in Android Studio
- Select "Empty Activity" template
- Configure package name (e.g., `com.yourcompany.videopoker`)
- Set minimum SDK to API 24 (Android 7.0)
- Choose Kotlin as the programming language
- **Test**: Project builds successfully with Gradle

#### Step 1.1.2: Configure Dependencies
**Deliverable**: build.gradle with all required dependencies
```gradle
dependencies {
    // Room database
    implementation "androidx.room:room-runtime:$room_version"
    implementation "androidx.room:room-ktx:$room_version"
    kapt "androidx.room:room-compiler:$room_version"
    
    // Jetpack Compose (if using Compose)
    implementation "androidx.compose.ui:ui:$compose_version"
    implementation "androidx.compose.material3:material3:$material3_version"
    
    // ViewModel and LiveData
    implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
    implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
    
    // Coroutines
    implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version"
    
    // Testing
    testImplementation "junit:junit:4.13.2"
    androidTestImplementation "androidx.test.espresso:espresso-core:$espresso_version"
}
```
- **Test**: All dependencies resolve without conflicts

#### Step 1.1.3: Configure App Settings
**Deliverable**: AndroidManifest.xml configured for tablets
```xml
<activity
    android:name=".MainActivity"
    android:screenOrientation="landscape"
    android:configChanges="orientation|keyboardHidden|screenSize">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```
- **Test**: App locks to landscape on tablet devices

### 1.2 Data Models & Database

#### Step 1.2.1: Create Card Data Class
**Deliverable**: Card.kt data model
```kotlin
data class Card(
    val suit: Suit,
    val rank: Rank,
    val isHeld: Boolean = false
) {
    enum class Suit { HEARTS, DIAMONDS, CLUBS, SPADES }
    enum class Rank(val value: Int) {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), 
        SEVEN(7), EIGHT(8), NINE(9), TEN(10),
        JACK(11), QUEEN(12), KING(13), ACE(14)
    }
}
```
- **Test**: Unit test card creation and property access

#### Step 1.2.2: Implement Deck Class
**Deliverable**: Deck.kt with full deck initialization
```kotlin
class Deck {
    private val cards = mutableListOf<Card>()
    
    init {
        reset()
    }
    
    fun reset() {
        cards.clear()
        Card.Suit.values().forEach { suit ->
            Card.Rank.values().forEach { rank ->
                cards.add(Card(suit, rank))
            }
        }
    }
    
    fun shuffle(random: SecureRandom) {
        cards.shuffle(random)
    }
    
    fun deal(count: Int): List<Card> {
        return cards.take(count).also { 
            cards.removeAll(it) 
        }
    }
}
```
- **Test**: Deck contains exactly 52 unique cards

#### Step 1.2.3: Build Room Database Entities
**Deliverable**: Database entities for game persistence
```kotlin
@Entity(tableName = "game_state")
data class GameState(
    @PrimaryKey val id: Int = 1,
    val credits: Int,
    val currentBet: Int,
    val lastWin: Int,
    val totalGamesPlayed: Int,
    val totalWinnings: Int
)

@Entity(tableName = "statistics")
data class Statistics(
    @PrimaryKey val handType: String,
    val count: Int,
    val lastSeen: Long
)
```
- **Test**: Database schema creation and migration

#### Step 1.2.4: Create DAOs
**Deliverable**: Data Access Objects for Room
```kotlin
@Dao
interface GameStateDao {
    @Query("SELECT * FROM game_state WHERE id = 1")
    suspend fun getGameState(): GameState?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGameState(gameState: GameState)
}

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM statistics")
    suspend fun getAllStatistics(): List<Statistics>
    
    @Update
    suspend fun updateStatistic(statistic: Statistics)
}
```
- **Test**: CRUD operations work correctly

### 1.3 Core Game Logic

#### Step 1.3.1: Implement Secure Card Shuffling
**Deliverable**: CardShuffler.kt with SecureRandom
```kotlin
class CardShuffler {
    private val secureRandom = SecureRandom()
    
    fun shuffleDeck(deck: Deck) {
        deck.shuffle(secureRandom)
    }
    
    // Fisher-Yates implementation
    fun <T> MutableList<T>.shuffle(random: SecureRandom) {
        for (i in size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val temp = this[i]
            this[i] = this[j]
            this[j] = temp
        }
    }
}
```
- **Test**: Statistical randomness validation over 10,000 shuffles

#### Step 1.3.2: Create Hand Evaluation Engine
**Deliverable**: HandEvaluator.kt with all poker hands
```kotlin
class HandEvaluator {
    enum class HandRank {
        HIGH_CARD, JACKS_OR_BETTER, TWO_PAIR, THREE_OF_A_KIND,
        STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND,
        STRAIGHT_FLUSH, ROYAL_FLUSH
    }
    
    fun evaluateHand(cards: List<Card>): HandRank {
        // Implementation for each hand type
        if (isRoyalFlush(cards)) return HandRank.ROYAL_FLUSH
        if (isStraightFlush(cards)) return HandRank.STRAIGHT_FLUSH
        if (isFourOfAKind(cards)) return HandRank.FOUR_OF_A_KIND
        if (isFullHouse(cards)) return HandRank.FULL_HOUSE
        if (isFlush(cards)) return HandRank.FLUSH
        if (isStraight(cards)) return HandRank.STRAIGHT
        if (isThreeOfAKind(cards)) return HandRank.THREE_OF_A_KIND
        if (isTwoPair(cards)) return HandRank.TWO_PAIR
        if (isJacksOrBetter(cards)) return HandRank.JACKS_OR_BETTER
        return HandRank.HIGH_CARD
    }
}
```
- **Test**: Comprehensive unit tests for each hand ranking

#### Step 1.3.3: Build Payout System
**Deliverable**: PayoutCalculator.kt with 9/6 pay table
```kotlin
class PayoutCalculator {
    private val payoutTable = mapOf(
        HandRank.ROYAL_FLUSH to listOf(250, 500, 750, 1000, 4000),
        HandRank.STRAIGHT_FLUSH to listOf(50, 100, 150, 200, 250),
        HandRank.FOUR_OF_A_KIND to listOf(25, 50, 75, 100, 125),
        HandRank.FULL_HOUSE to listOf(9, 18, 27, 36, 45),
        HandRank.FLUSH to listOf(6, 12, 18, 24, 30),
        HandRank.STRAIGHT to listOf(4, 8, 12, 16, 20),
        HandRank.THREE_OF_A_KIND to listOf(3, 6, 9, 12, 15),
        HandRank.TWO_PAIR to listOf(2, 4, 6, 8, 10),
        HandRank.JACKS_OR_BETTER to listOf(1, 2, 3, 4, 5)
    )
    
    fun calculatePayout(hand: HandRank, betAmount: Int): Int {
        return payoutTable[hand]?.get(betAmount - 1) ?: 0
    }
}
```
- **Test**: Verify payouts match 9/6 Jacks or Better table

#### Step 1.3.4: Implement Betting Logic
**Deliverable**: BettingManager.kt with validation
```kotlin
class BettingManager {
    var currentBet = 1
        private set
    
    fun validateBet(amount: Int, availableCredits: Int): Boolean {
        return amount in 1..5 && amount <= availableCredits
    }
    
    fun placeBet(amount: Int, availableCredits: Int): Boolean {
        if (validateBet(amount, availableCredits)) {
            currentBet = amount
            return true
        }
        return false
    }
    
    fun maxBet(availableCredits: Int) {
        currentBet = minOf(5, availableCredits)
    }
}
```
- **Test**: Edge cases (insufficient credits, invalid bets)

### 1.4 MVVM Architecture

#### Step 1.4.1: Create GameViewModel
**Deliverable**: GameViewModel.kt with StateFlow
```kotlin
class GameViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {
    
    private val _gameState = MutableStateFlow(GameUiState())
    val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()
    
    data class GameUiState(
        val credits: Int = 1000,
        val currentBet: Int = 1,
        val dealtCards: List<Card> = emptyList(),
        val heldCards: Set<Int> = emptySet(),
        val gamePhase: GamePhase = GamePhase.BETTING,
        val lastHandRank: HandRank? = null,
        val lastWinAmount: Int = 0
    )
    
    fun deal() {
        viewModelScope.launch {
            // Deal logic
        }
    }
    
    fun toggleHold(cardIndex: Int) {
        // Toggle hold state
    }
    
    fun draw() {
        viewModelScope.launch {
            // Draw logic
        }
    }
}
```
- **Test**: State changes propagate correctly to observers

#### Step 1.4.2: Implement Game State Machine
**Deliverable**: GameStateMachine.kt
```kotlin
class GameStateMachine {
    enum class GamePhase {
        BETTING, DEALING, HOLDING, DRAWING, EVALUATING, PAYOUT
    }
    
    private var currentPhase = GamePhase.BETTING
    
    fun canTransitionTo(newPhase: GamePhase): Boolean {
        return when (currentPhase) {
            GamePhase.BETTING -> newPhase == GamePhase.DEALING
            GamePhase.DEALING -> newPhase == GamePhase.HOLDING
            GamePhase.HOLDING -> newPhase == GamePhase.DRAWING
            GamePhase.DRAWING -> newPhase == GamePhase.EVALUATING
            GamePhase.EVALUATING -> newPhase == GamePhase.PAYOUT
            GamePhase.PAYOUT -> newPhase == GamePhase.BETTING
        }
    }
    
    fun transitionTo(newPhase: GamePhase): Boolean {
        if (canTransitionTo(newPhase)) {
            currentPhase = newPhase
            return true
        }
        return false
    }
}
```
- **Test**: Invalid state transitions are prevented

#### Step 1.4.3: Build Credit Management
**Deliverable**: CreditManager.kt with persistence
```kotlin
class CreditManager(private val gameStateDao: GameStateDao) {
    private var credits = 1000
    
    suspend fun loadCredits() {
        credits = gameStateDao.getGameState()?.credits ?: 1000
    }
    
    suspend fun deductBet(amount: Int): Boolean {
        if (credits >= amount) {
            credits -= amount
            saveCredits()
            return true
        }
        return false
    }
    
    suspend fun addWinnings(amount: Int) {
        credits += amount
        saveCredits()
    }
    
    private suspend fun saveCredits() {
        gameStateDao.saveGameState(
            GameState(credits = credits, /* other fields */)
        )
    }
}
```
- **Test**: Credit calculations are accurate and persistent

## Phase 2: UI/UX & Features

### 2.1 Tablet-Optimized UI Layout

#### Step 2.1.1: Create Main Game Screen
**Deliverable**: GameScreen layout optimized for tablets
```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel) {
    val gameState by viewModel.gameState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B5D1E)) // Casino green
    ) {
        // Top bar with credits, bet, win
        TopInfoBar(
            credits = gameState.credits,
            bet = gameState.currentBet,
            lastWin = gameState.lastWinAmount
        )
        
        // Card display area
        CardDisplayArea(
            cards = gameState.dealtCards,
            heldCards = gameState.heldCards,
            onCardClick = { index -> viewModel.toggleHold(index) }
        )
        
        // Betting controls
        BettingControls(
            enabled = gameState.gamePhase == GamePhase.BETTING,
            onBetChange = { viewModel.setBet(it) },
            onDeal = { viewModel.deal() },
            onDraw = { viewModel.draw() }
        )
    }
}
```
- **Test**: UI scales properly on 10"+ tablet displays

#### Step 2.1.2: Implement Card Display
**Deliverable**: Card layout with proper tablet spacing
```kotlin
@Composable
fun CardDisplayArea(
    cards: List<Card>,
    heldCards: Set<Int>,
    onCardClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        cards.forEachIndexed { index, card ->
            CardView(
                card = card,
                isHeld = index in heldCards,
                onClick = { onCardClick(index) },
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(0.7f) // Standard card ratio
                    .padding(horizontal = 8.dp)
            )
        }
    }
}
```
- **Test**: Cards display with optimal sizing for tablets

#### Step 2.1.3: Add Betting Controls
**Deliverable**: Touch-optimized betting interface
```kotlin
@Composable
fun BettingControls(
    enabled: Boolean,
    onBetChange: (Int) -> Unit,
    onDeal: () -> Unit,
    onDraw: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Bet buttons
        (1..5).forEach { bet ->
            Button(
                onClick = { onBetChange(bet) },
                enabled = enabled,
                modifier = Modifier
                    .size(width = 120.dp, height = 64.dp)
            ) {
                Text("BET $bet", fontSize = 18.sp)
            }
        }
        
        // Deal/Draw button
        Button(
            onClick = { if (enabled) onDeal() else onDraw() },
            modifier = Modifier
                .size(width = 160.dp, height = 64.dp)
        ) {
            Text(
                if (enabled) "DEAL" else "DRAW",
                fontSize = 20.sp
            )
        }
    }
}
```
- **Test**: Button sizes meet tablet touch target guidelines

### 2.2 High-Resolution Card Rendering

#### Step 2.2.1: Design Card Graphics
**Deliverable**: High-res card rendering system
```kotlin
class CardRenderer {
    private val cardPaint = Paint().apply {
        isAntiAlias = true
    }
    
    fun drawCard(
        canvas: Canvas,
        card: Card,
        bounds: RectF,
        isHeld: Boolean
    ) {
        // Draw card background
        cardPaint.color = Color.WHITE
        canvas.drawRoundRect(bounds, 12f, 12f, cardPaint)
        
        // Draw card border
        if (isHeld) {
            cardPaint.color = Color.YELLOW
            cardPaint.style = Paint.Style.STROKE
            cardPaint.strokeWidth = 8f
            canvas.drawRoundRect(bounds, 12f, 12f, cardPaint)
        }
        
        // Draw suit and rank
        drawSuit(canvas, card.suit, bounds)
        drawRank(canvas, card.rank, bounds)
    }
    
    private fun drawSuit(canvas: Canvas, suit: Suit, bounds: RectF) {
        // High-res suit rendering
    }
    
    private fun drawRank(canvas: Canvas, rank: Rank, bounds: RectF) {
        // High-res rank rendering
    }
}
```
- **Test**: Cards render crisply on high-DPI tablet screens

#### Step 2.2.2: Implement Card States
**Deliverable**: Card face/back rendering
```kotlin
@Composable
fun CardView(
    card: Card?,
    isHeld: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .graphicsLayer {
                // Card flip animation
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (card != null) {
                drawCard(card, isHeld)
            } else {
                drawCardBack()
            }
        }
        
        if (isHeld) {
            Text(
                "HELD",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .background(Color.Yellow, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}
```
- **Test**: Visual verification of all 52 card faces at tablet resolution

#### Step 2.2.3: Add Selection Effects
**Deliverable**: Visual feedback for card selection
```kotlin
@Composable
fun CardSelectionEffect(isHeld: Boolean) {
    AnimatedVisibility(
        visible = isHeld,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Yellow.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
```
- **Test**: Hold state clearly visible with enhanced graphics

### 2.3 Touch Interactions

#### Step 2.3.1: Card Hold Functionality
**Deliverable**: Touch handling for card selection
```kotlin
@Composable
fun CardTouchHandler(
    cardIndex: Int,
    onHoldToggle: (Int) -> Unit
) {
    var touchDownTime by remember { mutableStateOf(0L) }
    
    Modifier.pointerInput(cardIndex) {
        detectTapGestures(
            onPress = { 
                touchDownTime = System.currentTimeMillis()
            },
            onTap = {
                val tapDuration = System.currentTimeMillis() - touchDownTime
                if (tapDuration < 500) { // Quick tap
                    onHoldToggle(cardIndex)
                    // Haptic feedback
                    performHapticFeedback(HapticFeedbackConstants.LIGHT_TICK)
                }
            }
        )
    }
}
```
- **Test**: Touch events correctly toggle hold state

#### Step 2.3.2: Deal/Draw Button Logic
**Deliverable**: Primary action button with states
```kotlin
class GameActionButton {
    enum class ButtonState {
        DEAL_ENABLED,
        DEAL_DISABLED,
        DRAW_ENABLED,
        DRAW_DISABLED
    }
    
    @Composable
    fun ActionButton(
        state: ButtonState,
        onDeal: () -> Unit,
        onDraw: () -> Unit
    ) {
        val enabled = state == ButtonState.DEAL_ENABLED || 
                     state == ButtonState.DRAW_ENABLED
        val isDeal = state == ButtonState.DEAL_ENABLED || 
                    state == ButtonState.DEAL_DISABLED
        
        Button(
            onClick = { if (isDeal) onDeal() else onDraw() },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (enabled) Color.Green else Color.Gray
            )
        ) {
            Text(
                text = if (isDeal) "DEAL" else "DRAW",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
```
- **Test**: Button enables/disables based on game state

#### Step 2.3.3: Menu Navigation Gestures
**Deliverable**: Swipe gesture handling
```kotlin
@Composable
fun SwipeableMenu(
    onMenuOpen: () -> Unit,
    content: @Composable () -> Unit
) {
    val swipeableState = rememberSwipeableState(0)
    val sizePx = with(LocalDensity.current) { 300.dp.toPx() }
    val anchors = mapOf(0f to 0, sizePx to 1)
    
    Box(
        modifier = Modifier
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
    ) {
        content()
        
        if (swipeableState.targetValue == 1) {
            LaunchedEffect(Unit) {
                onMenuOpen()
            }
        }
    }
}
```
- **Test**: Swipe gestures work consistently

### 2.4 Premium Animations & Visual Polish

#### Step 2.4.1: Card Dealing Animation
**Deliverable**: Smooth dealing animations
```kotlin
@Composable
fun DealingAnimation(
    cards: List<Card>,
    onComplete: () -> Unit
) {
    cards.forEachIndexed { index, card ->
        val animatedOffset = animateFloatAsState(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * 100,
                easing = FastOutSlowInEasing
            )
        )
        
        Card(
            card = card,
            modifier = Modifier.offset(
                y = animatedOffset.value.dp
            )
        )
    }
    
    LaunchedEffect(cards) {
        delay(cards.size * 100L + 300L)
        onComplete()
    }
}
```
- **Test**: Consistent 60fps animations on high-end tablets

#### Step 2.4.2: Winning Hand Effects
**Deliverable**: Visual effects for winning hands
```kotlin
@Composable
fun WinningHandEffect(
    handRank: HandRank,
    show: Boolean
) {
    AnimatedVisibility(
        visible = show,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Particle effects
            ParticleSystem(
                particleCount = when (handRank) {
                    HandRank.ROYAL_FLUSH -> 100
                    HandRank.STRAIGHT_FLUSH -> 75
                    HandRank.FOUR_OF_A_KIND -> 50
                    else -> 25
                }
            )
            
            // Hand name display
            Text(
                text = handRank.displayName,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(24.dp)
            )
        }
    }
}
```
- **Test**: Visual effects render smoothly without frame drops

#### Step 2.4.3: Payout Animations
**Deliverable**: Credit counting animation
```kotlin
@Composable
fun PayoutAnimation(
    startCredits: Int,
    endCredits: Int,
    duration: Int = 2000
) {
    var displayCredits by remember { mutableStateOf(startCredits) }
    
    LaunchedEffect(endCredits) {
        val startTime = System.currentTimeMillis()
        val creditDiff = endCredits - startCredits
        
        while (displayCredits < endCredits) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
            
            displayCredits = startCredits + (creditDiff * progress).toInt()
            delay(16) // 60fps
        }
        
        displayCredits = endCredits
    }
    
    Text(
        text = "CREDITS: $displayCredits",
        fontSize = 36.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}
```
- **Test**: Credit updates animate with high-quality transitions

### 2.5 Audio System

#### Step 2.5.1: Background Music
**Deliverable**: MediaPlayer implementation
```kotlin
class MusicManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    
    fun startBackgroundMusic() {
        mediaPlayer = MediaPlayer.create(context, R.raw.casino_ambience).apply {
            isLooping = true
            setVolume(0.5f, 0.5f)
            start()
        }
    }
    
    fun stopMusic() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }
    
    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
}
```
- **Test**: Music plays cleanly through tablet speakers

#### Step 2.5.2: Sound Effects
**Deliverable**: SoundPool for game sounds
```kotlin
class SoundEffectManager(context: Context) {
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(5)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    
    private val soundIds = mutableMapOf<SoundEffect, Int>()
    
    enum class SoundEffect {
        CARD_FLIP, CARD_DEAL, COIN_DROP, WIN_CHIME, BUTTON_CLICK
    }
    
    init {
        soundIds[SoundEffect.CARD_FLIP] = soundPool.load(context, R.raw.card_flip, 1)
        soundIds[SoundEffect.CARD_DEAL] = soundPool.load(context, R.raw.card_deal, 1)
        soundIds[SoundEffect.COIN_DROP] = soundPool.load(context, R.raw.coin_drop, 1)
        soundIds[SoundEffect.WIN_CHIME] = soundPool.load(context, R.raw.win_chime, 1)
        soundIds[SoundEffect.BUTTON_CLICK] = soundPool.load(context, R.raw.button_click, 1)
    }
    
    fun play(effect: SoundEffect) {
        soundIds[effect]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
        }
    }
}
```
- **Test**: Sound effects have proper spatial positioning

#### Step 2.5.3: Audio Settings
**Deliverable**: Volume control and persistence
```kotlin
class AudioSettingsManager(private val sharedPrefs: SharedPreferences) {
    companion object {
        const val KEY_MUSIC_VOLUME = "music_volume"
        const val KEY_EFFECTS_VOLUME = "effects_volume"
        const val KEY_MUSIC_ENABLED = "music_enabled"
        const val KEY_EFFECTS_ENABLED = "effects_enabled"
    }
    
    var musicVolume: Float
        get() = sharedPrefs.getFloat(KEY_MUSIC_VOLUME, 0.5f)
        set(value) = sharedPrefs.edit().putFloat(KEY_MUSIC_VOLUME, value).apply()
    
    var effectsVolume: Float
        get() = sharedPrefs.getFloat(KEY_EFFECTS_VOLUME, 0.7f)
        set(value) = sharedPrefs.edit().putFloat(KEY_EFFECTS_VOLUME, value).apply()
    
    var musicEnabled: Boolean
        get() = sharedPrefs.getBoolean(KEY_MUSIC_ENABLED, true)
        set(value) = sharedPrefs.edit().putBoolean(KEY_MUSIC_ENABLED, value).apply()
    
    var effectsEnabled: Boolean
        get() = sharedPrefs.getBoolean(KEY_EFFECTS_ENABLED, true)
        set(value) = sharedPrefs.edit().putBoolean(KEY_EFFECTS_ENABLED, value).apply()
}
```
- **Test**: Audio settings persist and adapt to device

### 2.6 Menu System

#### Step 2.6.1: Main Menu
**Deliverable**: Tablet-optimized menu layout
```kotlin
@Composable
fun MainMenu(
    onNewGame: () -> Unit,
    onContinue: () -> Unit,
    onSettings: () -> Unit,
    onHelp: () -> Unit,
    onStatistics: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "VIDEO POKER",
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gold
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        MenuButton("NEW GAME", onClick = onNewGame)
        MenuButton("CONTINUE", onClick = onContinue)
        MenuButton("SETTINGS", onClick = onSettings)
        MenuButton("HELP", onClick = onHelp)
        MenuButton("STATISTICS", onClick = onStatistics)
    }
}

@Composable
fun MenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(72.dp)
    ) {
        Text(text, fontSize = 24.sp)
    }
}
```
- **Test**: Menu utilizes tablet screen space effectively

#### Step 2.6.2: Settings Screen
**Deliverable**: Comprehensive settings interface
```kotlin
@Composable
fun SettingsScreen(
    audioSettings: AudioSettingsManager,
    gameSettings: GameSettingsManager,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("SETTINGS", fontSize = 36.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onBack) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        
        Divider(modifier = Modifier.padding(vertical = 16.dp))
        
        // Audio Settings
        Text("AUDIO", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        
        SwitchSetting(
            label = "Music Enabled",
            checked = audioSettings.musicEnabled,
            onCheckedChange = { audioSettings.musicEnabled = it }
        )
        
        SliderSetting(
            label = "Music Volume",
            value = audioSettings.musicVolume,
            onValueChange = { audioSettings.musicVolume = it },
            enabled = audioSettings.musicEnabled
        )
        
        SwitchSetting(
            label = "Sound Effects",
            checked = audioSettings.effectsEnabled,
            onCheckedChange = { audioSettings.effectsEnabled = it }
        )
        
        SliderSetting(
            label = "Effects Volume",
            value = audioSettings.effectsVolume,
            onValueChange = { audioSettings.effectsVolume = it },
            enabled = audioSettings.effectsEnabled
        )
        
        // Game Settings
        Text("GAMEPLAY", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        
        SwitchSetting(
            label = "Auto-Hold Suggestions",
            checked = gameSettings.autoHoldEnabled,
            onCheckedChange = { gameSettings.autoHoldEnabled = it }
        )
        
        SliderSetting(
            label = "Animation Speed",
            value = gameSettings.animationSpeed,
            onValueChange = { gameSettings.animationSpeed = it }
        )
    }
}
```
- **Test**: Settings persist and apply correctly

#### Step 2.6.3: Help System
**Deliverable**: Comprehensive help interface
```kotlin
@Composable
fun HelpScreen(onBack: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("HELP", fontSize = 36.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }
        
        item { 
            Text("HOW TO PLAY", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(
                """
                1. Place your bet (1-5 coins)
                2. Press DEAL to receive 5 cards
                3. Select cards to HOLD
                4. Press DRAW to replace unheld cards
                5. Get paid for winning hands!
                """.trimIndent()
            )
        }
        
        item {
            Text("HAND RANKINGS", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            HandRankingTable()
        }
        
        item {
            Text("PAYOUT TABLE", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            PayoutTable()
        }
        
        item {
            Text("STRATEGY TIPS", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            StrategyGuide()
        }
    }
}
```
- **Test**: Help content displays with proper formatting

## Phase 3: Testing & Optimization

### 3.1 Comprehensive Testing

#### Step 3.1.1: Unit Tests
**Deliverable**: Complete test suite for game logic
```kotlin
class HandEvaluatorTest {
    private val evaluator = HandEvaluator()
    
    @Test
    fun testRoyalFlush() {
        val cards = listOf(
            Card(Suit.HEARTS, Rank.ACE),
            Card(Suit.HEARTS, Rank.KING),
            Card(Suit.HEARTS, Rank.QUEEN),
            Card(Suit.HEARTS, Rank.JACK),
            Card(Suit.HEARTS, Rank.TEN)
        )
        assertEquals(HandRank.ROYAL_FLUSH, evaluator.evaluateHand(cards))
    }
    
    @Test
    fun testStraightFlush() {
        val cards = listOf(
            Card(Suit.CLUBS, Rank.NINE),
            Card(Suit.CLUBS, Rank.EIGHT),
            Card(Suit.CLUBS, Rank.SEVEN),
            Card(Suit.CLUBS, Rank.SIX),
            Card(Suit.CLUBS, Rank.FIVE)
        )
        assertEquals(HandRank.STRAIGHT_FLUSH, evaluator.evaluateHand(cards))
    }
    
    // Test all hand types...
}

class PayoutCalculatorTest {
    private val calculator = PayoutCalculator()
    
    @Test
    fun testRoyalFlushPayouts() {
        assertEquals(250, calculator.calculatePayout(HandRank.ROYAL_FLUSH, 1))
        assertEquals(500, calculator.calculatePayout(HandRank.ROYAL_FLUSH, 2))
        assertEquals(750, calculator.calculatePayout(HandRank.ROYAL_FLUSH, 3))
        assertEquals(1000, calculator.calculatePayout(HandRank.ROYAL_FLUSH, 4))
        assertEquals(4000, calculator.calculatePayout(HandRank.ROYAL_FLUSH, 5))
    }
    
    // Test all payout combinations...
}
```
- **Test**: 100% code coverage for core logic classes

#### Step 3.1.2: UI Tests
**Deliverable**: Espresso tests for tablet UI
```kotlin
@RunWith(AndroidJUnit4::class)
class GameScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()
    
    @Test
    fun testCompleteGameFlow() {
        // Place bet
        composeTestRule.onNodeWithText("BET 5").performClick()
        
        // Deal cards
        composeTestRule.onNodeWithText("DEAL").performClick()
        
        // Wait for dealing animation
        composeTestRule.waitForIdle()
        
        // Select cards to hold
        composeTestRule.onAllNodesWithTag("card").onFirst().performClick()
        
        // Draw
        composeTestRule.onNodeWithText("DRAW").performClick()
        
        // Verify payout or next hand
        composeTestRule.onNodeWithText("DEAL").assertExists()
    }
    
    @Test
    fun testMenuNavigation() {
        // Open menu
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        
        // Navigate to settings
        composeTestRule.onNodeWithText("SETTINGS").performClick()
        
        // Verify settings screen
        composeTestRule.onNodeWithText("AUDIO").assertExists()
    }
}
```
- **Test**: Complete game flow automated testing

#### Step 3.1.3: Android Component Tests
**Deliverable**: Robolectric tests
```kotlin
@RunWith(RobolectricTestRunner::class)
class GameViewModelTest {
    private lateinit var viewModel: GameViewModel
    private lateinit var repository: FakeGameRepository
    
    @Before
    fun setup() {
        repository = FakeGameRepository()
        viewModel = GameViewModel(repository)
    }
    
    @Test
    fun testDealingCards() = runTest {
        viewModel.deal()
        
        val state = viewModel.gameState.value
        assertEquals(5, state.dealtCards.size)
        assertEquals(GamePhase.HOLDING, state.gamePhase)
    }
    
    @Test
    fun testCreditDeduction() = runTest {
        val initialCredits = viewModel.gameState.value.credits
        viewModel.setBet(5)
        viewModel.deal()
        
        assertEquals(initialCredits - 5, viewModel.gameState.value.credits)
    }
}
```
- **Test**: ViewModel and Repository layer testing

### 3.2 Performance Optimization

#### Step 3.2.1: Memory Profiling
**Deliverable**: Optimized memory usage
```kotlin
class MemoryOptimization {
    // Use object pooling for frequently created objects
    object CardPool {
        private val pool = mutableListOf<Card>()
        
        fun obtain(suit: Suit, rank: Rank): Card {
            return pool.removeFirstOrNull() ?: Card(suit, rank)
        }
        
        fun recycle(card: Card) {
            pool.add(card)
        }
    }
    
    // Optimize bitmap caching for card images
    object CardImageCache {
        private val cache = LruCache<String, Bitmap>(52)
        
        fun getCardImage(card: Card): Bitmap {
            val key = "${card.suit}_${card.rank}"
            return cache[key] ?: loadCardImage(card).also {
                cache.put(key, it)
            }
        }
    }
}
```
- **Test**: Memory usage stays optimal with large graphics

#### Step 3.2.2: Animation Optimization
**Deliverable**: Consistent 60fps performance
```kotlin
class AnimationOptimizer {
    // Use hardware acceleration
    fun enableHardwareAcceleration(view: View) {
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }
    
    // Optimize animation timing
    fun optimizeCardAnimation(): AnimationSpec<Float> {
        return tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        )
    }
    
    // Profile frame timing
    fun profileAnimation(animation: () -> Unit) {
        Choreographer.getInstance().postFrameCallback { frameTime ->
            val startTime = System.nanoTime()
            animation()
            val duration = System.nanoTime() - startTime
            val fps = 1_000_000_000 / duration
            Log.d("Performance", "FPS: $fps")
        }
    }
}
```
- **Test**: Frame rate monitoring shows stable performance

#### Step 3.2.3: Rendering Optimization
**Deliverable**: Efficient GPU usage
```kotlin
class RenderingOptimizer {
    // Batch draw calls
    fun batchCardRendering(
        canvas: Canvas,
        cards: List<Card>,
        positions: List<RectF>
    ) {
        // Enable batch mode
        canvas.save()
        
        // Draw all card backgrounds first
        cards.forEachIndexed { index, card ->
            drawCardBackground(canvas, positions[index])
        }
        
        // Then draw all card faces
        cards.forEachIndexed { index, card ->
            drawCardFace(canvas, card, positions[index])
        }
        
        canvas.restore()
    }
    
    // Use efficient shaders
    fun createGradientShader(): Shader {
        return LinearGradient(
            0f, 0f, 0f, 100f,
            intArrayOf(Color.GOLD, Color.YELLOW),
            null,
            Shader.TileMode.CLAMP
        )
    }
}
```
- **Test**: GPU usage remains efficient during gameplay

### 3.3 Build Configuration

#### Step 3.3.1: ProGuard Configuration
**Deliverable**: Optimized release build
```kotlin
// proguard-rules.pro
-keepattributes SourceFile,LineNumberTable
-keep class com.yourcompany.videopoker.model.** { *; }
-keep class com.yourcompany.videopoker.data.** { *; }

# Room database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
```
- **Test**: Release APK functions identically to debug

#### Step 3.3.2: APK Optimization
**Deliverable**: Optimized APK for tablets
```gradle
android {
    buildTypes {
        release {
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            
            // APK splits for different architectures
            splits {
                abi {
                    enable true
                    reset()
                    include 'arm64-v8a', 'x86_64' // Tablet architectures
                    universalApk false
                }
            }
        }
    }
    
    // Bundle configuration
    bundle {
        density {
            enableSplit = true
        }
        abi {
            enableSplit = true
        }
        language {
            enableSplit = false // Keep all languages
        }
    }
}
```
- **Test**: APK optimized for tablet deployment

#### Step 3.3.3: CI/CD Pipeline
**Deliverable**: Automated build and test system
```yaml
# .github/workflows/android.yml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run unit tests
      run: ./gradlew test
    
    - name: Run instrumentation tests
      uses: reactivecircus/android-emulator-runner@v2
      with:
        api-level: 33
        target: google_apis
        arch: x86_64
        profile: pixel_tablet
        script: ./gradlew connectedCheck
    
    - name: Build release APK
      run: ./gradlew assembleRelease
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-release
        path: app/build/outputs/apk/release/app-release.apk
```
- **Test**: CI/CD builds pass all automated tests

### 3.4 Tablet Compatibility

#### Step 3.4.1: Multi-Manufacturer Testing
**Deliverable**: Compatibility verification
```kotlin
class TabletCompatibilityTest {
    @Test
    fun testSamsungTablets() {
        // Test on Galaxy Tab S8, S7, S6
        val devices = listOf("SM-X700", "SM-T870", "SM-T860")
        devices.forEach { model ->
            testOnDevice(model)
        }
    }
    
    @Test
    fun testIPadOS() {
        // Test on iPad Pro, iPad Air
        // Using Android compatibility layer
    }
    
    @Test
    fun testPixelTablet() {
        // Test on Google Pixel Tablet
        testOnDevice("pixel_tablet")
    }
    
    private fun testOnDevice(deviceModel: String) {
        // Verify:
        // - Layout renders correctly
        // - Touch targets are appropriate size
        // - Performance meets targets
        // - Audio works properly
    }
}
```
- **Test**: Consistent behavior across tablet manufacturers

#### Step 3.4.2: Screen Size Adaptation
**Deliverable**: Responsive layouts for 10"-13" tablets
```kotlin
@Composable
fun AdaptiveGameLayout() {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    
    when {
        screenWidthDp >= 1200 -> { // 13" tablets
            LargeTabletLayout()
        }
        screenWidthDp >= 900 -> { // 11" tablets
            MediumTabletLayout()
        }
        else -> { // 10" tablets
            StandardTabletLayout()
        }
    }
}

// Resource qualifiers
// res/layout-sw720dp/ - 10" tablets
// res/layout-sw800dp/ - 11" tablets
// res/layout-sw900dp/ - 13" tablets
```
- **Test**: UI adapts correctly to various tablet sizes

#### Step 3.4.3: Orientation Handling
**Deliverable**: Landscape optimization
```kotlin
class OrientationHandler {
    @Composable
    fun EnforceLandscape() {
        val activity = LocalContext.current as Activity
        
        DisposableEffect(Unit) {
            val originalOrientation = activity.requestedOrientation
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            
            onDispose {
                activity.requestedOrientation = originalOrientation
            }
        }
    }
    
    fun handleOrientationChange(
        configuration: Configuration,
        onOrientationChanged: (Boolean) -> Unit
    ) {
        when (configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                onOrientationChanged(true)
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                // Force back to landscape
                onOrientationChanged(false)
            }
        }
    }
}
```
- **Test**: Proper layout in both landscape orientations

## Conclusion

This implementation guide provides a comprehensive roadmap for developing a high-quality Android Video Poker game optimized for tablets. Each step is designed to be discrete and testable, allowing for incremental development and validation.

### Key Success Metrics
- **Performance**: Consistent 60fps on high-end tablets
- **Quality**: Zero critical bugs, polished UI/UX
- **Compatibility**: Works on all major tablet models
- **User Experience**: Intuitive controls, smooth animations
- **Code Quality**: 80%+ test coverage, clean architecture

### Next Steps
1. Set up the Android project following Phase 1
2. Implement core game logic with comprehensive testing
3. Build the UI layer with tablet optimizations
4. Add polish and premium features
5. Conduct thorough testing on target devices
6. Prepare for release with optimized builds

By following this guide, you'll create a professional-grade video poker game that takes full advantage of Android's native capabilities and provides an excellent experience on high-end tablets.