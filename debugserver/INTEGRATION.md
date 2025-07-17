# VR Debug Server Integration Guide

## Overview
The VR Debug Server is a generic, lightweight HTTP server that runs inside your Meta Spatial SDK application, allowing you to remotely control VR input events through a REST API. This is particularly useful for automated testing, debugging without wearing a headset, and creating demo scenarios.

## Features
- 🎮 Remote camera control (rotation, position)
- 🎯 Controller simulation (position, pointing, button presses)
- 🌐 Scene state inspection
- 📱 App readiness notifications
- 🖥️ Web UI for manual control
- 🔧 Minimal integration footprint
- 🎨 Configurable for any app (app name, logging, etc.)
- 🧩 Extensible with app-specific debugging features

## Prerequisites
- Meta Spatial SDK 0.7.0 or higher
- Android project with Quest support
- NanoHTTPD dependency (automatically added during integration)

## Quick Start Integration

### Step 1: Add Dependencies
In your app's `build.gradle.kts`, add:
```kotlin
dependencies {
    implementation("org.nanohttpd:nanohttpd:2.3.1")
    implementation("com.google.code.gson:gson:2.11.0")
}
```

### Step 2: Copy the Debug Server Folder
Copy the entire `debugserver` folder to your project root.

### Step 3: Configure Source Sets
Add the debugserver folder as a source directory in your app's `build.gradle.kts`:
```kotlin
android {
    sourceSets {
        getByName("quest") { // or your VR flavor name
            java.srcDirs("src/quest/java", "../debugserver")
        }
    }
}
```

### Step 4: Add Network Permissions
Ensure your `AndroidManifest.xml` includes:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### Step 5: Integrate into Your Spatial Activity

Add these minimal changes to your immersive activity:

```kotlin
import vr.debugserver.VRDebugSystem

class ImmersiveActivity : AppSystemActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize debug server (only in debug builds)
        if (BuildConfig.DEBUG) {
            VRDebugSystem.initialize(this) {
                appName = "My Spatial App"  // Customize app name
                port = 8080                 // Default port
                enableFileLogging = true   // Enable persistent logging
            }
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

## Configuration Options

The debug server can be configured during initialization:

```kotlin
VRDebugSystem.initialize(this) {
    appName = "My Spatial App"              // Used for log file naming
    port = 8080                             // Server port (default: 8080)
    enableWebUI = true                      // Enable web interface
    enableFileLogging = true                // Enable persistent log files
    logFileName = "my_app_debug.log"        // Custom log file name (optional)
    authToken = "my-secret-token"           // Optional authentication
    maxRequestSize = 10 * 1024 * 1024      // Max request size (10MB)
}
```

## Creating Project-Specific Extensions

The debug server supports app-specific extensions for custom debugging features.

### Extension Interface

Create an extension by implementing the `AppDebugExtension` interface:

```kotlin
import vr.debugserver.AppDebugExtension
import vr.debugserver.VRDebugSystem
import fi.iki.elonen.NanoHTTPD

class MyAppDebugExtension : AppDebugExtension {
    override val namespace = "myapp"
    override val displayName = "My App Debug"
    override val version = "1.0.0"
    
    override fun initialize(debugSystem: VRDebugSystem) {
        // Initialize your extension
    }
    
    override fun handleRequest(uri: String, method: NanoHTTPD.Method, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        return when (uri) {
            "status" -> getAppStatus()
            "reset" -> resetApp()
            "data" -> getAppData()
            else -> null // Return null for unhandled endpoints
        }
    }
    
    override fun getAppStatus(): Map<String, Any>? {
        return mapOf(
            "active" to true,
            "version" to "1.0.0",
            "uptime" to System.currentTimeMillis()
        )
    }
    
    override fun getWebUIContent(): String? {
        return """
            <div class="my-app-controls">
                <h3>My App Controls</h3>
                <button onclick="resetMyApp()">Reset App</button>
                <button onclick="showAppData()">Show Data</button>
            </div>
        """
    }
    
    override fun getWebUIJavaScript(): String? {
        return """
            function resetMyApp() {
                fetch('/api/app/myapp/reset', { method: 'POST' })
                    .then(response => response.json())
                    .then(data => console.log('Reset:', data));
            }
            
            function showAppData() {
                fetch('/api/app/myapp/data')
                    .then(response => response.json())
                    .then(data => console.log('Data:', data));
            }
        """
    }
    
    override fun getWebUIStyles(): String? {
        return """
            .my-app-controls {
                background: #f0f8ff;
                padding: 15px;
                border-radius: 8px;
                margin: 10px 0;
            }
        """
    }
    
    override fun getApiDocumentation(): String? {
        return """
            <div>
                <strong>GET /api/app/myapp/status</strong> - Get app status<br>
                <strong>POST /api/app/myapp/reset</strong> - Reset app state<br>
                <strong>GET /api/app/myapp/data</strong> - Get app data<br>
            </div>
        """
    }
    
    override fun cleanup() {
        // Clean up resources
    }
    
    // Helper methods
    private fun getAppStatus(): NanoHTTPD.Response {
        // Implementation
    }
    
    private fun resetApp(): NanoHTTPD.Response {
        // Implementation
    }
    
    private fun getAppData(): NanoHTTPD.Response {
        // Implementation
    }
}
```

### Extension Registration

Register your extension in the activity:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    if (BuildConfig.DEBUG) {
        VRDebugSystem.initialize(this) {
            appName = "My Spatial App"
        }
        
        // Register app-specific extensions
        VRDebugSystem.getInstance().registerExtension(MyAppDebugExtension())
    }
}
```

### Extension Best Practices

1. **Namespace**: Use lowercase, hyphens, and be descriptive (e.g., "poker", "gallery", "physics")
2. **Error Handling**: Always handle errors gracefully in your endpoints
3. **Documentation**: Provide clear API documentation via `getApiDocumentation()`
4. **Web UI**: Make your web UI controls intuitive and well-styled
5. **Cleanup**: Properly clean up resources in the `cleanup()` method

## API Documentation

### Base URL
```
http://[quest-ip-address]:8080
```

### Core Endpoints

#### 1. App Ready Status
Check if the VR app is fully initialized and ready to receive commands.

```bash
GET /api/app/ready

Response:
{
  "ready": true,
  "timestamp": "2025-01-17T10:30:00Z",
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
  "entities": [...],
  "camera": {
    "position": {"x": 0, "y": 1.6, "z": 0},
    "rotation": {"pitch": 0, "yaw": 0, "roll": 0}
  },
  "controllers": {...}
}
```

#### 6. Extension Endpoints

##### App-Specific Extensions
```bash
GET /api/app/{namespace}/{endpoint}
POST /api/app/{namespace}/{endpoint}
```

Extensions register their own endpoints under their namespace.

#### 7. Logging API

##### List Log Files
```bash
GET /api/logs

Response:
{
  "files": [
    {
      "name": "my_app_debug.log",
      "size": 1024,
      "lastModified": 1642434000000
    }
  ]
}
```

##### Get Recent Logs
```bash
GET /api/logs/recent

Response:
{
  "logs": [
    {
      "timestamp": "2025-01-17 10:30:00.123",
      "level": "DEBUG",
      "tag": "VRDebugSystem",
      "message": "System initialized"
    }
  ]
}
```

## Web UI

Access the web interface at `http://[quest-ip]:8080/`

Features:
- Visual status indicator
- Camera rotation controls (arrow keys)
- Controller position controls
- Button press simulators
- App-specific extension controls
- Real-time log viewing
- API documentation

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

# Test camera rotation
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

### 2. Extension Integration Example
```kotlin
// VideoPoker extension example
class VideoPokerDebugExtension : AppDebugExtension {
    override val namespace = "poker"
    override val displayName = "Video Poker Debug"
    override val version = "1.0.0"
    
    override fun handleRequest(uri: String, method: Method, session: IHTTPSession): Response? {
        return when (uri) {
            "deal" -> dealNewHand()
            "game/state" -> getGameState()
            "game/reset" -> resetGame()
            else -> null
        }
    }
    
    override fun getWebUIContent(): String? {
        return """
            <div class="poker-controls">
                <h3>Video Poker Controls</h3>
                <button onclick="dealNewHand()">Deal New Hand</button>
                <button onclick="resetGame()">Reset Game</button>
            </div>
        """
    }
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
3. Ensure configured port is not blocked by firewall
4. Try accessing from Quest browser first: `http://localhost:8080`

### Commands Not Working
1. Check `/api/app/ready` returns `true`
2. Verify scene is fully loaded
3. Check logs: `adb logcat | grep VRDebug`
4. Ensure debug build is installed

### Extension Issues
1. Verify extension namespace follows naming rules (lowercase, hyphens only)
2. Check extension initialization doesn't throw exceptions
3. Ensure extension endpoints return proper responses
4. Test extension web UI integration

## Performance Impact

The debug server has minimal performance impact:
- Uses ~5MB of RAM
- Negligible CPU usage when idle
- Network requests are handled on background thread
- Automatically disabled in release builds

## Contributing

To enhance the debug server:
1. Add new core endpoints in `VRDebugServer.kt`
2. Implement simulation logic in `VRInputSimulator.kt`
3. Update models in `DebugModels.kt`
4. Create reusable extensions for common use cases
5. Document new features in this guide

## License

This debug server module is provided as-is for development and testing purposes.