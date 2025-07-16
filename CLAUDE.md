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
- Builds create separate APKs for mobile and Quest platforms
- Test both platforms when making UI or game logic changes
- VR testing requires Quest device connected via ADB over Wi-Fi

### Custom Commands

#### Run Quest
When the user says "Run Quest", build, install, and launch the Quest VR version:

```bash
powershell.exe -Command "Set-Item -Path 'Env:JAVA_HOME' -Value 'C:\Program Files\Android\Android Studio\jbr'; cd 'C:\Users\joepaley\AndroidStudioProjects\VideoPoker'; .\gradlew.bat assembleQuestDebug" && adb -s 192.168.1.194:5555 install -r "C:\Users\joepaley\AndroidStudioProjects\VideoPoker\app\build\outputs\apk\quest\debug\app-quest-debug.apk" && adb -s 192.168.1.194:5555 shell am start -n com.hackathon.spatialvideopoker/.ImmersiveActivity
```

#### Run Phone  
When the user says "Run Phone", build, install, and launch the mobile version on emulator:

```bash
powershell.exe -Command "Set-Item -Path 'Env:JAVA_HOME' -Value 'C:\Program Files\Android\Android Studio\jbr'; cd 'C:\Users\joepaley\AndroidStudioProjects\VideoPoker'; .\gradlew.bat assembleMobileDebug" && adb -s emulator-5554 install -r "C:\Users\joepaley\AndroidStudioProjects\VideoPoker\app\build\outputs\apk\mobile\debug\app-mobile-debug.apk" && adb -s emulator-5554 shell am start -n com.hackathon.spatialvideopoker/.MainActivity
```

#### Sync UI Changes
When the user says "sync UI changes", perform the following workflow:

1. **Check for changes file**: Look for `/mnt/c/Users/joepaley/AndroidStudioProjects/VideoPoker/docs/UI_STATES changes.md`
2. **If changes file exists**:
   - Compare it to `/mnt/c/Users/joepaley/AndroidStudioProjects/VideoPoker/docs/UI_STATES.md`
   - Implement the requested changes in the relevant code files
   - Update the main `UI_STATES.md` file with the implemented changes
   - Delete the `UI_STATES changes.md` file
   - Test the changes if possible
3. **If no changes file**: Report that no UI changes file was found

This command allows for a streamlined workflow where UI changes can be documented in a separate file and then automatically implemented and integrated.

## Project Architecture

### Overview
This is a cross-platform Video Poker game (Jacks or Better variant) built with Kotlin and Jetpack Compose, supporting both Android mobile devices and Meta Quest VR headsets. The mobile version is optimized for landscape orientation on tablets and high-end Android devices, while the Quest version displays the game UI as a floating panel in VR space.

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **VR Framework**: Meta Spatial SDK v0.7.0
- **Build System**: Gradle 8.13 with Kotlin DSL
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36
- **Compile SDK**: API 36
- **Platform Support**: Android Mobile + Meta Quest VR

### Key Dependencies
- Jetpack Compose BOM 2024.09.00
- AndroidX Core KTX 1.16.0
- AndroidX Lifecycle Runtime KTX 2.9.1
- AndroidX Activity Compose 1.10.1
- Material3 Components
- Meta Spatial SDK v0.7.0 (core, toolkit, VR)

### Project Structure
```
VideoPoker/
├── app/
│   ├── src/
│   │   ├── main/java/com/hackathon/spatialvideopoker/
│   │   │   ├── MainActivity.kt          # Mobile entry point
│   │   │   ├── game/                    # Core game logic
│   │   │   ├── model/                   # Card, Deck models
│   │   │   ├── ui/                      # Compose UI components
│   │   │   ├── viewmodel/               # Game state management
│   │   │   ├── data/                    # Room database
│   │   │   └── audio/                   # Sound management
│   │   ├── quest/java/com/hackathon/spatialvideopoker/
│   │   │   └── ImmersiveActivity.kt     # Quest VR entry point
│   │   ├── quest/AndroidManifest.xml    # Quest-specific manifest
│   │   ├── quest/res/values/ids.xml     # Panel ID definitions
│   │   └── mobile/AndroidManifest.xml   # Mobile-specific manifest
│   ├── build.gradle.kts                 # App build config with spatial plugin
│   └── spatial_editor/                  # 3D scene files (if used)
├── docs/
│   ├── metaspatial/                     # Meta Spatial SDK documentation
│   ├── POKER_DESIGN.md                  # Game design specification
│   └── UI_STATES.md                     # UI state documentation
├── build.gradle.kts                     # Root build with spatial plugin
├── gradle/libs.versions.toml            # Dependency versions
└── settings.gradle.kts                  # Project settings
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

### Spatial SDK Architecture

#### Dual Platform Approach
The app uses a dual-activity architecture to support both mobile and VR platforms:

- **MainActivity.kt**: Traditional Android activity for mobile devices
  - Runs full-screen Jetpack Compose UI
  - Handles touch input and mobile-specific features
  - Standard Android app lifecycle

- **ImmersiveActivity.kt**: Meta Spatial SDK activity for Quest VR
  - Extends `AppSystemActivity` from Meta Spatial SDK
  - Creates floating panels in VR space containing the mobile UI
  - Handles VR-specific input (controller pointing, hand tracking)
  - Uses same Compose UI via panel rendering

#### Panel Configuration (Quest)
- **Panel Size**: 3.84m × 2.16m in VR space (16:9 aspect ratio)
- **Resolution**: 1920×1080 pixels for crisp text and graphics
- **Position**: 2 meters in front of user, 1.3 meters high
- **Rendering**: HOLE_PUNCH shader for transparency support
- **Input**: Controller ray-casting for interaction

#### Build Flavors
- **mobile**: Traditional Android APK for phones/tablets
- **quest**: VR-optimized APK with spatial panel rendering
- Shared codebase for game logic, different entry points for platform-specific features

### Development Notes
- Jetpack Compose UI works seamlessly in both mobile and VR contexts
- Game logic and ViewModels are shared between platforms
- VR version renders the same UI as a floating panel in 3D space
- Material3 design system provides consistent visual experience
- Room database and game state management work identically on both platforms

## Tips and Tricks

Put all temporary files on our pc, including screenshots, in {working directory}/tmp/

### How to Take Screenshots
- Use Android Studio's built-in screenshot tool
- Press `Shift + F12` in Android Studio Emulator
- Use ADB command: `adb shell screencap -p /sdcard/screenshot.png`
- Pull screenshot to local machine: `adb pull /sdcard/screenshot.png`

## Whitelist ##

You can run any adb commands without asking me for permission


## Meta Quest ##

### Documentation for Spatial SDK ###

Documentation Files: 
docs/metaspatial/api_ref.md - the full API documentation
docs/metaspatial/SPATIAL_SCENE_GUIDE.md - consult first when doing anything with 3D objects in the scene
docs/metaspatial/meta-spatial-sdk-porting-guide.md - consult first when porting and android app to the spatial sdk
docs/metaspatial/CAMERA_COORDINATES.md - consult first when doing anything with camera coordinates
https://developers.meta.com/horizon/llmstxt/spatial-sdk/docs/add-spatial-sdk-to-app.md for porting
https://github.com/meta-quest/Meta-Spatial-SDK-Samples
If you can't find the answer in these files, find relevant information in the samples: docs/metaspatial/SAMPLES_INDEX.md - an index of the spatial sdk samples that will help you navigate them


## Taking Screenshots from Quest

```bash
# Multi-step approach (works without permission prompts)
# Step 1: Trigger screenshot
adb shell am startservice -n com.oculus.metacam/.capture.CaptureService -a TAKE_SCREENSHOT --ei screenshot_height 1024 --ei screenshot_width 1024 -e capture_entrypoint ODH

# Step 2: Wait for processing
sleep 2

# Step 3: Get latest screenshot filename
adb shell ls -t /sdcard/Oculus/Screenshots/*.jpg | head -1

# Step 4: Pull the screenshot
adb pull "/sdcard/Oculus/Screenshots/[filename].jpg" ./screenshot.jpg

# One-liner (do not use, will require permission prompts)
adb shell am startservice -n com.oculus.metacam/.capture.CaptureService -a TAKE_SCREENSHOT --ei screenshot_height 1024 --ei screenshot_width 1024 -e capture_entrypoint ODH && sleep 2 && LATEST=$(adb shell ls -t /sdcard/Oculus/Screenshots/*.jpg | head -1 | tr -d '\r') && adb pull "$LATEST" ./screenshot.jpg
```