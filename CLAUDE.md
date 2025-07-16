# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands (Use PowerShell)

### 1. Clean Previous Build
```bash
powershell.exe -Command "Set-Item -Path 'Env:JAVA_HOME' -Value 'C:\Program Files\Android\Android Studio\jbr'; cd 'C:\Users\joepaley\AndroidStudioProjects\VideoPoker'; .\gradlew.bat clean"
```

### 2. Build Debug APK
```bash
powershell.exe -Command "Set-Item -Path 'Env:JAVA_HOME' -Value 'C:\Program Files\Android\Android Studio\jbr'; cd 'C:\Users\joepaley\AndroidStudioProjects\VideoPoker'; .\gradlew.bat assembleDebug"
```

### 3. Build Release APK
```bash
powershell.exe -Command "Set-Item -Path 'Env:JAVA_HOME' -Value 'C:\Program Files\Android\Android Studio\jbr'; cd 'C:\Users\joepaley\AndroidStudioProjects\VideoPoker'; .\gradlew.bat assembleRelease"
```

### 4. Build and Install Debug (One Command)
```bash
powershell.exe -Command "Set-Item -Path 'Env:JAVA_HOME' -Value 'C:\Program Files\Android\Android Studio\jbr'; cd 'C:\Users\joepaley\AndroidStudioProjects\VideoPoker'; .\gradlew.bat installDebug"
```

### 5. Check Gradle Tasks
```bash
powershell.exe -Command "Set-Item -Path 'Env:JAVA_HOME' -Value 'C:\Program Files\Android\Android Studio\jbr'; cd 'C:\Users\joepaley\AndroidStudioProjects\VideoPoker'; .\gradlew.bat tasks"
```

### 6. Run Tests
```bash
powershell.exe -Command "Set-Item -Path 'Env:JAVA_HOME' -Value 'C:\Program Files\Android\Android Studio\jbr'; cd 'C:\Users\joepaley\AndroidStudioProjects\VideoPoker'; .\gradlew.bat test"
```

### 7. Run Lint
```bash
powershell.exe -Command "Set-Item -Path 'Env:JAVA_HOME' -Value 'C:\Program Files\Android\Android Studio\jbr'; cd 'C:\Users\joepaley\AndroidStudioProjects\VideoPoker'; .\gradlew.bat lint"
```

For all other commands, use adb directly, do not use powershell.  However it is the Windows adb, so you need to give it Windows paths.


## Development Workflow

### General Guidelines
- Use PowerShell for building, use adb directly for all other commands
- Always use PowerShell to build, use adb directly for everything else

## Project Architecture

### Overview
This is an Android Video Poker game (Jacks or Better variant) built with Kotlin and Jetpack Compose. The app is optimized for landscape orientation on tablets and high-end Android devices.

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Build System**: Gradle 8.13 with Kotlin DSL
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36
- **Compile SDK**: API 36

### Key Dependencies
- Jetpack Compose BOM 2024.09.00
- AndroidX Core KTX 1.16.0
- AndroidX Lifecycle Runtime KTX 2.9.1
- AndroidX Activity Compose 1.10.1
- Material3 Components

### Project Structure
```
VideoPoker/
├── app/
│   ├── src/main/java/com/hackathon/spatialvideopoker/
│   │   ├── MainActivity.kt              # Main entry point with Compose setup
│   │   └── ui/theme/
│   │       ├── Color.kt                # Color definitions
│   │       ├── Theme.kt                # Material3 theme configuration
│   │       └── Type.kt                 # Typography definitions
│   └── build.gradle.kts                # App-level build configuration
├── build.gradle.kts                    # Root build configuration
├── gradle/libs.versions.toml           # Version catalog for dependencies
├── settings.gradle.kts                 # Project settings
├── POKER_DESIGN.md                     # Comprehensive design specification
└── IMPLEMENTATION.md                   # Detailed implementation guide
```

### Game Design Overview
The game implements Jacks or Better video poker with:
- Standard 52-card deck
- 9/6 full-pay payout table
- Landscape-only orientation for optimal tablet experience
- Betting range of 1-5 coins
- Hold/draw mechanics
- Secure random number generation for fair play

### Key Implementation Areas (from design docs)
1. **Core Game Logic**: Card models, deck management, hand evaluation, payout calculation
2. **MVVM Architecture**: GameViewModel with StateFlow for reactive UI
3. **UI Components**: Card rendering, betting controls, animations
4. **Data Persistence**: Room database for game state and statistics
5. **Audio System**: MediaPlayer for background music, SoundPool for effects
6. **Security**: SecureRandom for card shuffling, ProGuard/R8 for release builds

### Development Notes
- The app uses Jetpack Compose for modern declarative UI
- Currently has a basic MainActivity with a greeting composable
- Ready for implementation of poker game logic and UI
- Design follows Material3 guidelines with dynamic color support on Android 12+

## Tips and Tricks

Put all temporary files on our pc, including screenshots, in {working directory}/tmp/

### How to Take Screenshots
- Use Android Studio's built-in screenshot tool
- Press `Shift + F12` in Android Studio Emulator
- Use ADB command: `adb shell screencap -p /sdcard/screenshot.png`
- Pull screenshot to local machine: `adb pull /sdcard/screenshot.png`

# Whitelist ##

You can run any adb commands without asking me for permission