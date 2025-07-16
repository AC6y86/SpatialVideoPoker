# Meta Spatial SDK Android Porting Guide

## Overview
Convert your Android app to run on Meta Quest with your UI as a floating panel in VR space.
- **Mobile flavor**: Runs normally on phones
- **Quest flavor**: UI appears as a panel in VR

## Steps

### 1. Add Plugin
In root `build.gradle.kts`:
```kotlin
plugins {
    id("com.meta.spatial.plugin") version "0.7.0" apply true
    id("com.google.devtools.ksp") version "2.0.20-1.0.24" apply true
}
```

### 2. Configure App Module
In `app/build.gradle.kts`:

```kotlin
plugins {
    id("com.meta.spatial.plugin")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    // ... existing plugins
}

android {
    buildFeatures { buildConfig = true }
    
    flavorDimensions += "device"
    productFlavors {
        create("mobile") { dimension = "device" }
        create("quest") { dimension = "device" }
    }
}

dependencies {
    val spatialVersion = "0.7.0"
    implementation("com.meta.spatial:meta-spatial-sdk:$spatialVersion")
    implementation("com.meta.spatial:meta-spatial-sdk-toolkit:$spatialVersion")
    implementation("com.meta.spatial:meta-spatial-sdk-vr:$spatialVersion")
    ksp("com.meta.spatial.plugin:com.meta.spatial.plugin.gradle.plugin:$spatialVersion")
}

// Add at end of file
spatial {
    allowUsageDataCollection = true
    scenes {
        exportItems {
            item {
                projectPath.set(file("spatial_editor/Main.metaspatial"))
                outputPath.set(file("src/quest/assets/scenes"))
            }
        }
    }
}
```

### 3. Create Quest-Specific Files

**a) Panel ID** - `app/src/quest/res/values/ids.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <item name="panel_main" type="id" />  <!-- Must be type="id" NOT "integer" -->
</resources>
```

**b) ImmersiveActivity** - `app/src/quest/java/.../ImmersiveActivity.kt`:
```kotlin
import android.content.Intent
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Pose
import com.meta.spatial.core.Quaternion
import com.meta.spatial.core.Vector3
import com.meta.spatial.runtime.*
import com.meta.spatial.toolkit.*
import com.meta.spatial.vr.VRFeature

class ImmersiveActivity : AppSystemActivity() {
    
    override fun registerFeatures() = listOf(VRFeature(this))
    
    override fun onSceneReady() {
        super.onSceneReady()
        
        // Configure scene
        scene.setViewOrigin(0.0f, 0.0f, 0.0f)
        scene.enableHolePunching(true)
        scene.setReferenceSpace(ReferenceSpace.LOCAL_FLOOR)
        
        // Create panel
        Entity.createPanelEntity(
            R.id.panel_main,
            Transform(Pose(Vector3(0f, 1.3f, 2f), Quaternion(0f, 0f, 0f)))
        )
    }
    
    override fun registerPanels() = listOf(
        PanelRegistration(R.id.panel_main) {
            panelIntent = Intent().apply {
                setClassName(applicationContext, MainActivity::class.qualifiedName!!)
            }
            config {
                width = 3.84f
                height = 2.16f
                layoutWidthInPx = 1920
                layoutHeightInPx = 1080
                layerConfig = LayerConfig()  // Required
                panelShader = SceneMaterial.HOLE_PUNCH_SHADER  // Required
                alphaMode = AlphaMode.HOLE_PUNCH  // Required
            }
        }
    )
}
```

**c) Quest Manifest**
`app/src/quest/AndroidManifest.xml`:
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <uses-feature android:name="android.hardware.vr.headtracking" android:required="true" />
    <uses-permission android:name="com.oculus.permission.HAND_TRACKING" />

    <application>
        <meta-data android:name="com.oculus.supportedDevices" 
                   android:value="quest2|questpro|quest3" />
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:allowEmbedded="true"
            android:enableOnBackInvokedCallback="true"
            android:taskAffinity="com.yourpackage.panel" />
            
        <activity
            android:name=".ImmersiveActivity"
            android:exported="true"
            android:theme="@android:style/Theme.Black.NoTitleBar.Fullscreen"
            android:launchMode="singleTask">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
                <category android:name="com.oculus.intent.category.VR" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### 4. Update Main Manifest
Remove the main activity from `app/src/main/AndroidManifest.xml` and create `app/src/mobile/AndroidManifest.xml` with your original mobile activity.

## Critical Points
1. Panel ID must be `type="id"` not `type="integer"`
2. Panel config MUST include: `layerConfig`, `panelShader`, `alphaMode`
3. MainActivity needs `enableOnBackInvokedCallback="true"`
4. Use `R.id.panel_main` not `R.integer.panel_main`
5. No Hilt/Dagger in ImmersiveActivity
6. Kotlin 2.0 requires Compose compiler plugin when using Compose

## Troubleshooting
**Panel not visible?**
- Check panel ID is `type="id"` in ids.xml
- Verify all three config items: `layerConfig`, `panelShader`, `alphaMode`
- Ensure MainActivity has `enableOnBackInvokedCallback="true"`
- Use positive Z coordinate (e.g., 2f) for panel position

## Build & Test
```bash
./gradlew assembleQuestDebug
adb install -r app/build/outputs/apk/quest/debug/app-quest-debug.apk
adb shell am start -n com.yourpackage/.ImmersiveActivity
```