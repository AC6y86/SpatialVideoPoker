# Meta Spatial SDK Camera Coordinate System

## Overview

The Meta Spatial SDK uses a coordinate system that can be confusing due to differences between world space and view space conventions. This document clarifies how the coordinate system works based on practical testing and debugging conducted on July 14, 2025.

## ⚠️ CRITICAL: Actual Coordinate System (Verified by Testing)

The SDK uses a **right-handed coordinate system** with:

```
+Y (Up)
 |
 |_____ +X (Right)
/
-Z (FORWARD - toward UI/away from wall)
```

**VERIFIED FACTS:**
- **+X** = Right ✅
- **+Y** = Up ✅  
- **+Z** = **BACKWARD** (toward wall/away from UI) ⚠️
- **-Z** = **FORWARD** (toward UI/away from wall) ✅
- **Origin (0,0,0)** = Initial spawn position

## Camera/View Coordinate System

The camera uses the **same coordinate system as world space**:
- **Forward direction** = -Z (negative Z)
- **Backward direction** = +Z (positive Z)
- This follows standard VR/3D graphics conventions

## Rotation (Yaw) ✅ VERIFIED

Rotation around the Y-axis (yaw) is measured in degrees:

```
         0° (Facing +Z - BACKWARD!)
         |
         |
270° ----+---- 90° (Facing +X - Right) ✅
         |
         |
       180° (Facing -Z - FORWARD!)
```

**VERIFIED ROTATION MAPPING:**
- **0°** = Facing +Z (BACKWARD toward wall) ⚠️
- **90°** = Facing +X (right) ✅
- **180°** = Facing -Z (FORWARD toward UI) ✅
- **270°** = Facing -X (left) ✅
- Rotation is **clockwise** when viewed from above ✅

**KEY INSIGHT:** At 0° rotation, the camera faces the BACKWARD direction (+Z), not forward!

## API Functions

### Setting View Position and Rotation

```kotlin
// Position only (preserves current rotation)
scene.setViewOrigin(x: Float, y: Float, z: Float)

// Position and rotation
scene.setViewOrigin(x: Float, y: Float, z: Float, yaw: Float)
```

**Important**: There's no way to set rotation independently. To update position without affecting rotation, use the 3-parameter version.

### Reading Current State

```kotlin
// Get current position
val position: Vector3 = scene.getViewOrigin()

// Get current yaw rotation
val yaw: Float = scene.getViewSceneRotation()
```

## Movement Relative to Camera Direction ⚠️ CORRECTED

To move the player relative to their current facing direction, you need to calculate direction vectors based on the camera's yaw rotation.

### ✅ CORRECTED JavaScript Example (Verified July 14, 2025)

```javascript
function movePlayer(deltaX, deltaY, deltaZ) {
    // Get current yaw in radians
    const yawRadians = currentCameraYaw * Math.PI / 180;
    
    // Calculate direction vectors based on yaw
    // Forward direction: where the camera is facing
    const forwardX = Math.sin(yawRadians);
    const forwardZ = Math.cos(yawRadians);
    
    // Right direction: 90° clockwise from forward
    const rightX = Math.cos(yawRadians);
    const rightZ = -Math.sin(yawRadians);
    
    // Apply movement: deltaX is strafe (left/right), deltaZ is forward/backward
    // Note: In our coordinate system, -Z is forward, so negate deltaZ
    const rotatedX = deltaX * rightX + (-deltaZ) * forwardX;
    const rotatedZ = deltaX * rightZ + (-deltaZ) * forwardZ;
    
    // Apply movement
    newPosition.x += rotatedX;
    newPosition.y += deltaY;
    newPosition.z += rotatedZ;
}
```

### ✅ CORRECTED Movement Input Mapping

For intuitive controls:
- **Forward** = `movePlayer(0, 0, -0.5)` (move toward camera's facing direction)
- **Backward** = `movePlayer(0, 0, 0.5)` (move away from camera's facing direction)
- **Strafe Left** = `movePlayer(-0.5, 0, 0)` (move to camera's left)
- **Strafe Right** = `movePlayer(0.5, 0, 0)` (move to camera's right)

## Common Pitfalls ⚠️ UPDATED

### 1. Z-Axis Direction Confusion (CRITICAL ERROR IN PREVIOUS DOCS)
- **WRONG ASSUMPTION**: "+Z is forward in world space"
- **VERIFIED REALITY**: "+Z is BACKWARD, -Z is FORWARD" in this coordinate system
- **KEY**: There is NO mismatch between world and view space - they use the same coordinate system

### 2. Rotation Drift
- Rotation values may drift slightly (e.g., 90° becomes 89.95528°)
- Use tolerance when comparing angles

### 3. Position Updates Reset Rotation
- Using the 4-parameter `setViewOrigin()` will update rotation even if you only meant to change position
- Always use the 3-parameter version for position-only updates

### 4. No Pitch/Roll Support ✅ VERIFIED
- Only yaw rotation is supported by `setViewOrigin()`
- Pitch and roll can be tracked internally but aren't applied to the view

## ✅ CORRECTED Practical Examples (Verified July 14, 2025)

### Example 1: Moving Forward Regardless of Rotation

```kotlin
fun moveForward(distance: Float) {
    val currentPos = scene.getViewOrigin()
    val currentYaw = scene.getViewSceneRotation()
    
    // Convert yaw to radians
    val yawRad = Math.toRadians(currentYaw.toDouble())
    
    // Calculate forward direction using corrected direction vectors
    val forwardX = sin(yawRad) * distance
    val forwardZ = cos(yawRad) * distance
    
    // Apply movement
    scene.setViewOrigin(
        currentPos.x + forwardX.toFloat(),
        currentPos.y,
        currentPos.z + forwardZ.toFloat()
    )
}
```

### Example 2: Strafing Right

```kotlin
fun strafeRight(distance: Float) {
    val currentPos = scene.getViewOrigin()
    val currentYaw = scene.getViewSceneRotation()
    
    // Convert yaw to radians
    val yawRad = Math.toRadians(currentYaw.toDouble())
    
    // Calculate right direction (90° clockwise from forward)
    val rightX = cos(yawRad) * distance
    val rightZ = -sin(yawRad) * distance
    
    // Apply movement
    scene.setViewOrigin(
        currentPos.x + rightX.toFloat(),
        currentPos.y,
        currentPos.z + rightZ.toFloat()
    )
}
```

## ✅ CORRECTED Visual Reference (Verified July 14, 2025)

When facing different directions:

```
Facing 0° (BACKWARD toward wall):
    +Z (camera facing direction - BACKWARD!)
     ↑
-X ←-+-→ +X (right)
     ↓
    -Z (UI direction - FORWARD!)

Facing Right (90°):
    +X (camera facing direction)
     ↑
-Z ←-+-→ +Z
     ↓
    -X (right relative to camera)

Facing 180° (FORWARD toward UI):
    -Z (camera facing direction - FORWARD!)
     ↑
+X ←-+-→ -X (right relative to camera)
     ↓
    +Z (wall direction - BACKWARD!)

Facing Left (270°):
    -X (camera facing direction)
     ↑
+Z ←-+-→ -Z
     ↓
    +X (right relative to camera)
```

**CRITICAL INSIGHT**: At 0° rotation, the camera faces +Z (BACKWARD), not -Z (forward)!

## ✅ CORRECTED Summary (July 14, 2025)

**VERIFIED FACTS:**
- The SDK uses a consistent coordinate system for both world and view space
- **+Z = BACKWARD, -Z = FORWARD** in all contexts
- **0° rotation = facing BACKWARD (+Z direction)**
- Movement calculations use direction vectors based on sin/cos of yaw angle
- Always use the appropriate `setViewOrigin()` overload to avoid unintended side effects
- Test movement at different rotations (0°, 90°, 180°, 270°) to verify correct behavior

**TESTING VALIDATION:**
- All coordinate assumptions verified through systematic screenshot testing
- Rotation directions confirmed through visual comparison
- Movement math corrected based on empirical results

**PREVIOUS DOCUMENTATION ERRORS CORRECTED:**
- Wrong assumption about +Z being "forward in world space"
- Incorrect movement calculation examples
- Misunderstanding of coordinate system consistency