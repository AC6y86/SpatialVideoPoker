# VR Debug Server Integration Guide

## Overview
The VR Debug Server is a lightweight HTTP server that runs inside your Meta Spatial SDK application, allowing you to remotely control VR input events through a REST API. This is particularly useful for automated testing, debugging without wearing a headset, and creating demo scenarios.

## Features
- Remote camera control (rotation, position)
- Controller simulation (position, pointing, button presses)
- Scene state inspection
- App readiness notifications
- Web UI for manual control
- Minimal integration footprint

## Prerequisites
- Meta Spatial SDK 0.7.0 or higher
- Android project with Quest support
- NanoHTTPD dependency (already added if you're using this module)

## Quick Start Integration

### Step 1: Add NanoHTTPD Dependency
In your app's `build.gradle.kts`, add:
```kotlin
dependencies {
    implementation("org.nanohttpd:nanohttpd:2.3.1")
}
```

### Step 2: Copy the Debug Server Folder
Copy the entire `debugserver` folder to your project root.

### Step 3: Configure Source Sets
Add the debugserver folder as a source directory in your app's `build.gradle.kts`:
```kotlin
android {
    sourceSets {
        getByName("main") { // or your flavor name like "quest"
            java.srcDirs("src/main/java", "../debugserver")
        }
    }
}
```

### Step 4: Integrate into Your Spatial Activity

Add these minimal changes to your immersive activity:

```kotlin
import vr.debugserver.VRDebugSystem

class ImmersiveActivity : AppSystemActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize debug server (only in debug builds)
        if (BuildConfig.DEBUG) {
            VRDebugSystem.initialize(this)
        }
    }
    
    override fun onSceneReady() {
        super.onSceneReady()
        
        // Register the debug system
        if (BuildConfig.DEBUG) {
            systemManager.registerSystem(VRDebugSystem.getInstance())
        }
    }
    
    override fun onVRReady() {
        super.onVRReady()
        
        // Notify that the app is fully ready
        if (BuildConfig.DEBUG) {
            VRDebugSystem.getInstance().notifyAppReady()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Clean up
        if (BuildConfig.DEBUG) {
            VRDebugSystem.shutdown()
        }
    }
}
```

### Step 5: Network Permissions
Ensure your `AndroidManifest.xml` includes:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Configuration

The debug server runs on port 8080 by default. You can change this and other settings:

```kotlin
// In your activity's onCreate()
VRDebugSystem.initialize(this) {
    port = 8888
    enableWebUI = true
    authToken = "my-secret-token" // Optional security
    maxRequestSize = 10 * 1024 * 1024 // 10MB
}
```

## API Documentation

### Base URL
```
http://[quest-ip-address]:8080
```

### Endpoints

#### 1. App Ready Status
Check if the VR app is fully initialized and ready to receive commands.

```bash
GET /api/app/ready

Response:
{
  "ready": true,
  "timestamp": "2025-07-14T10:30:00Z",
  "scene": {
    "entities": 15,
    "controllers": 2
  }
}
```

#### 2. Camera Control

##### Rotate Camera
```bash
POST /api/camera/rotate
Content-Type: application/json

{
  "pitch": 0,    // Rotation around X axis (degrees)
  "yaw": 90,     // Rotation around Y axis (degrees)
  "roll": 0      // Rotation around Z axis (degrees)
}
```

##### Set Camera Position
```bash
POST /api/camera/position
Content-Type: application/json

{
  "x": 0.0,
  "y": 1.6,  // Eye height
  "z": 0.0
}
```

#### 3. Controller Simulation

##### Point Controller at Screen Position
```bash
POST /api/controller/point
Content-Type: application/json

{
  "controller": "right",  // "left" or "right"
  "screen": {
    "x": 0.5,  // 0.0 to 1.0 (0.5 = center)
    "y": 0.5   // 0.0 to 1.0 (0.5 = center)
  }
}
```

##### Point Controller at World Position
```bash
POST /api/controller/point
Content-Type: application/json

{
  "controller": "right",
  "world": {
    "x": 2.0,
    "y": 1.5,
    "z": -3.0
  }
}
```

##### Move Controller
```bash
POST /api/controller/move
Content-Type: application/json

{
  "controller": "right",
  "position": {
    "x": 0.5,
    "y": 1.2,
    "z": -0.3
  }
}
```

#### 4. Button Simulation

##### Trigger Press/Release
```bash
POST /api/input/trigger
Content-Type: application/json

{
  "controller": "right",
  "action": "press"  // "press" or "release"
}
```

##### Generic Button Press
```bash
POST /api/input/button
Content-Type: application/json

{
  "controller": "right",
  "button": "A",  // A, B, X, Y, Menu, Squeeze, etc.
  "action": "press"
}
```

#### 5. Scene Information

##### Get Current Scene State
```bash
GET /api/scene/info

Response:
{
  "entities": [
    {
      "id": 123,
      "name": "Picture1",
      "position": {"x": 0, "y": 1.5, "z": -2},
      "type": "mesh"
    }
  ],
  "camera": {
    "position": {"x": 0, "y": 1.6, "z": 0},
    "rotation": {"pitch": 0, "yaw": 0, "roll": 0}
  },
  "controllers": {
    "left": {
      "position": {"x": -0.3, "y": 1.2, "z": -0.5},
      "pointing": null
    },
    "right": {
      "position": {"x": 0.3, "y": 1.2, "z": -0.5},
      "pointing": {"x": 0, "y": 1.5, "z": -2}
    }
  }
}
```

#### 6. Webhook Registration

Register to receive notifications when the app is ready:

```bash
POST /api/webhooks/register
Content-Type: application/json

{
  "url": "http://my-pc:3000/vr-app-ready",
  "headers": {  // Optional
    "Authorization": "Bearer my-token"
  }
}
```

## Web UI

Access the web interface at `http://[quest-ip]:8080/`

Features:
- Visual status indicator
- Camera rotation controls (arrow keys)
- Controller position sliders
- Button press simulators
- Quick action buttons
- Real-time scene visualization

## Example Usage Scenarios

### 1. Automated Testing Script (Python)
```python
import requests
import time

QUEST_IP = "192.168.1.100"
BASE_URL = f"http://{QUEST_IP}:8080"

# Wait for app to be ready
while True:
    response = requests.get(f"{BASE_URL}/api/app/ready")
    if response.json()["ready"]:
        break
    time.sleep(1)

# Look at each painting
for angle in [0, 90, 180, 270]:
    requests.post(f"{BASE_URL}/api/camera/rotate", 
                  json={"yaw": angle})
    time.sleep(2)
    
    # Simulate trigger press
    requests.post(f"{BASE_URL}/api/input/trigger",
                  json={"controller": "right", "action": "press"})
    time.sleep(0.1)
    requests.post(f"{BASE_URL}/api/input/trigger",
                  json={"controller": "right", "action": "release"})
```

### 2. Interactive Demo Control (JavaScript)
```javascript
const questIP = '192.168.1.100';

async function pointAtPainting(paintingNumber) {
  const positions = [
    {x: 0.2, y: 0.5},   // Painting 1
    {x: 0.5, y: 0.5},   // Painting 2
    {x: 0.8, y: 0.5}    // Painting 3
  ];
  
  await fetch(`http://${questIP}:8080/api/controller/point`, {
    method: 'POST',
    headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({
      controller: 'right',
      screen: positions[paintingNumber - 1]
    })
  });
}
```

## Security Considerations

For production builds:
1. The debug server is automatically disabled when `BuildConfig.DEBUG` is false
2. Consider adding authentication tokens for additional security
3. Restrict server to local network only
4. Use HTTPS if exposing to wider network

## Troubleshooting

### Server Not Accessible
1. Check that both devices are on the same network
2. Verify the Quest IP address: `adb shell ip addr show wlan0`
3. Ensure port 8080 is not blocked by firewall
4. Try accessing from Quest browser first: `http://localhost:8080`

### Commands Not Working
1. Check `/api/app/ready` returns `true`
2. Verify scene is fully loaded
3. Check logs: `adb logcat | grep VRDebug`
4. Ensure debug build is installed

### Integration Issues
1. Verify all imports are updated to your package name
2. Check that NanoHTTPD dependency is added
3. Ensure all four integration points are added to your activity
4. Build with `./gradlew assembleDebug`

## Performance Impact

The debug server has minimal performance impact:
- Uses ~5MB of RAM
- Negligible CPU usage when idle
- Network requests are handled on background thread
- Automatically disabled in release builds

## Contributing

To enhance the debug server:
1. Add new endpoints in `VRDebugServer.kt`
2. Implement simulation logic in `VRInputSimulator.kt`
3. Update models in `DebugModels.kt`
4. Document new features in this guide

## License

This debug server module is provided as-is for development and testing purposes.