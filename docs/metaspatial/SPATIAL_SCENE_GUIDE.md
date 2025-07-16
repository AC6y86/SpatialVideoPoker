# Spatial Scene Development Guide for Meta Spatial SDK

This guide documents the process of creating 3D scenes, including rooms, walls, and object placement using Meta Spatial SDK 0.6.1.

## Key Learnings

### 1. Mesh Primitives Require Components

When using primitive mesh URIs like `mesh://box` or `mesh://plane`, you MUST also add the corresponding component:

```kotlin
// WRONG - This will crash with "Failed to get component class"
val wall = Entity.create()
wall.setComponent(Mesh(Uri.parse("mesh://box")))

// CORRECT - Include the Box component
val wall = Entity.create()
wall.setComponent(Mesh(Uri.parse("mesh://box")))
wall.setComponent(Box(
    Vector3(-5f, 0f, -0.1f),  // min corner
    Vector3(5f, 3f, 0.1f)     // max corner
))
```

### 2. Box Component Defines Dimensions

The Box component takes two Vector3 parameters:
- **min**: The minimum corner (x, y, z)
- **max**: The maximum corner (x, y, z)

Example for a 10m wide, 3m tall, 0.2m thick wall:
```kotlin
Box(
    Vector3(-5f, 0f, -0.1f),  // min: left=-5, bottom=0, back=-0.1
    Vector3(5f, 3f, 0.1f)     // max: right=5, top=3, front=0.1
)
```

#### Understanding the Room Coordinate System
- **X-axis**: Left (-5) to Right (+5)
- **Y-axis**: Floor (0) to Ceiling (~4)
- **Z-axis**: Back (-5) to Front (+5)
- **Panel location**: Z = 2 (positive/front)
- **Important**: "Back wall" is at negative Z, not positive

### 3. Transform vs Scale

The Transform component in Spatial SDK only accepts a Pose parameter (position + rotation):

```kotlin
// Transform only takes Pose
Transform(Pose(Vector3(x, y, z), Quaternion(x, y, z, w)))

// For uniform scaling, use the Scale component separately
entity.setComponent(Scale(2.0f))  // Scales uniformly by 2x
```

#### Quaternion Rotations for Wall-Facing Objects
When using `Quaternion(x, y, z)` with 3 parameters, values are Euler angles in DEGREES:
- **Left wall objects** (face right): `Quaternion(0f, 90f, 0f)`
- **Right wall objects** (face left): `Quaternion(0f, -90f, 0f)`
- **Back wall objects** (face forward): `Quaternion(0f, 0f, 0f)`
- **Front wall objects** (face backward): `Quaternion(0f, 180f, 0f)`

### 4. Setting Material Colors

The Material class in SDK 0.6.1 does support color properties! You can use the `baseColor` property with a `Color4` object:

```kotlin
import com.meta.spatial.core.Color4

// Create colored material
val material = Material().apply {
    baseColor = Color4(1.0f, 0.0f, 0.0f, 1.0f) // Red (RGBA)
}
entity.setComponent(material)
```

Color4 constructor takes float values (0.0f to 1.0f) for:
- Red
- Green  
- Blue
- Alpha (transparency)

Example colors used in the gallery:
```kotlin
// White ceiling
baseColor = Color4(1.0f, 1.0f, 1.0f, 1.0f)

// Light blue wall
baseColor = Color4(0.7f, 0.85f, 1.0f, 1.0f)  

// Light green wall
baseColor = Color4(0.7f, 1.0f, 0.7f, 1.0f)

// Light yellow wall
baseColor = Color4(1.0f, 1.0f, 0.7f, 1.0f)

// Light pink wall
baseColor = Color4(1.0f, 0.8f, 0.9f, 1.0f)

// Brown wood color
baseColor = Color4(0.54f, 0.27f, 0.07f, 1.0f)
```

### 5. Complete Room Example

```kotlin
private fun createRoomWalls() {
    // Create floor with brown wood color
    val floor = Entity.create()
    floor.setComponent(Mesh(Uri.parse("mesh://box")))
    floor.setComponent(Box(
        Vector3(-5f, -0.1f, -5f),
        Vector3(5f, 0f, 5f)
    ))
    floor.setComponent(Material().apply {
        baseColor = Color4(0.54f, 0.27f, 0.07f, 1.0f)
    })
    floor.setComponent(Transform(Pose(Vector3(0f, 0f, 0f))))
    
    // Create ceiling (white)
    val ceiling = Entity.create()
    ceiling.setComponent(Mesh(Uri.parse("mesh://box")))
    ceiling.setComponent(Box(
        Vector3(-5f, 4f, -5f),
        Vector3(5f, 4.1f, 5f)
    ))
    ceiling.setComponent(Material().apply {
        baseColor = Color4(1.0f, 1.0f, 1.0f, 1.0f)
    })
    ceiling.setComponent(Transform(Pose(Vector3(0f, 0f, 0f))))
    
    // Create walls with different colors...
}
```

### 5.1 Placing Objects on Walls

When placing objects on walls, use an inward offset to prevent z-fighting:

```kotlin
val wallOffset = 0.5f  // Prevents z-fighting

// Apply offset INWARD from wall:
val leftWallPos = Vector3(-5f + wallOffset, height, zPos)    // Left wall at x=-5
val rightWallPos = Vector3(5f - wallOffset, height, zPos)     // Right wall at x=5
val backWallPos = Vector3(xPos, height, -5f + wallOffset)     // Back wall at z=-5
val frontWallPos = Vector3(xPos, height, 5f - wallOffset)     // Front wall at z=5
```

### 6. Material and Rendering Notes

- Default Material() renders as dark/black surfaces
- Without proper lighting setup, walls appear very dark  
- Walls might be single-sided (only visible from one direction)
- Using `baseColor` property with Color4 provides solid colors without needing texture files
- Colors appear properly lit even without explicit lighting setup when using baseColor

### 7. Required Imports

```kotlin
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Pose
import com.meta.spatial.core.Quaternion
import com.meta.spatial.core.Vector3
import com.meta.spatial.core.Color4  // For colored materials
import com.meta.spatial.toolkit.Transform
import com.meta.spatial.toolkit.Material
import com.meta.spatial.toolkit.Mesh
import com.meta.spatial.toolkit.Box
import com.meta.spatial.toolkit.Scale
```

### 8. Common Errors and Solutions

**Error**: `Failed to get component class com.meta.spatial.toolkit.Box`
**Solution**: Always include the Box/Plane component when using mesh primitives

**Error**: `Too many arguments for 'constructor(transform: Pose = ...): Transform'`
**Solution**: Transform only accepts Pose. Use Scale component for scaling.

**Error**: Material properties like `baseColorFactor` not found
**Solution**: Use `baseColor` property with Color4 object instead (discovered this works in SDK 0.6.1)

**Issue**: All objects appearing on one wall despite different positions
**Solution**: Check coordinate values - ensure you're varying the correct axis

**Issue**: Objects facing wrong direction on walls
**Solution**: Use correct Euler angles in degrees for Quaternion, not radians

**Issue**: GLB models appearing with repeated textures when scaled
**Solution**: Use Scale(1.0f) for GLB models that have internal scaling

### 9. Alternative Approaches

Instead of using primitives, you can:
1. Load 3D models (GLTF/GLB files)
2. Create custom meshes using `registerMeshCreator`
3. Use Spatial Editor to create and export scenes
4. Use textures with `baseTextureAndroidResourceUri` for more complex patterns

### 10. Tips for Debugging

- Add logging after each entity creation
- Test without the room first to ensure panel visibility
- Use screenshots to debug rendering issues
- Check if you're inside the room looking at walls from the wrong side
- Use colored debug cubes to verify object positions before final placement
- Create different colored markers for different walls (red=left, green=right, blue=back)
- Log position vectors to ensure correct coordinate values
- Remember: Panel is at positive Z, back wall is at negative Z

### 11. Object Placement Patterns

#### Wall Offset Strategy
```kotlin
val wallOffset = 0.5f  // Prevents z-fighting
// Apply offset INWARD from wall:
leftWallPos = Vector3(-5f + wallOffset, height, zPos)
rightWallPos = Vector3(5f - wallOffset, height, zPos)
backWallPos = Vector3(xPos, height, -5f + wallOffset)
```

#### Debug Visualization Template
```kotlin
// Quick debug cube for position testing:
val debugCube = Entity.create()
debugCube.setComponent(Mesh(Uri.parse("mesh://box")))
debugCube.setComponent(Box(Vector3(-0.2f, -0.2f, -0.2f), Vector3(0.2f, 0.2f, 0.2f)))
debugCube.setComponent(Material().apply { baseColor = Color4(1f, 0f, 0f, 1f) })
debugCube.setComponent(Transform(Pose(position, Quaternion(0f, 0f, 0f))))
```

#### Picture Frame Placement Example
```kotlin
private fun createPictureFrames() {
    val frameHeight = 2.0f  // Higher on the wall
    val frameScale = 1.0f   // Normal scale for GLB models
    val wallOffset = 0.5f   // Offset from wall
    
    // Create one centered frame on the left wall (x = -5)
    val leftFrame = Entity.create()
    leftFrame.setComponent(Mesh(Uri.parse("apk:///scenes/Objects/picture frame.glb")))
    leftFrame.setComponent(Scale(frameScale))
    val leftPosition = Vector3(-5f + wallOffset, frameHeight, 0f)
    // Rotate 90 degrees to face right (towards room center)
    leftFrame.setComponent(Transform(Pose(leftPosition, Quaternion(0f, 90f, 0f))))
    
    // Similar for right and back walls...
}
```

### 12. Asset Management for Quest
- Place GLB/GLTF files in `app/src/main/assets/`
- Use URI format: `apk:///path/to/file.glb`
- Ensure assets are copied to correct build variant folder
- Common issue: Assets in wrong folder won't be packaged in APK

### 13. Universal Object Positioning Strategies

#### Binary Search Positioning
When aligning objects without clear measurements:
1. Make large adjustments first (25-50% of the space)
2. Halve your adjustment size each iteration
3. Stop when visually acceptable

Example workflow:
- Gap looks "about half the object size" → adjust by 0.5f
- Still off but closer → adjust by 0.25f  
- Almost there → adjust by 0.1f or less

#### Axis Isolation
Debug one dimension at a time:
- Fix X position first (left/right)
- Then Y position (up/down)
- Finally Z position (forward/back)

This prevents confusion when multiple axes need adjustment.

#### Progressive Refinement
```kotlin
// Start with rough positioning
var position = Vector3(0f, 1f, 0f)  // Initial guess

// Large adjustment phase (>0.2f increments)
position = Vector3(0f, 1.5f, 0f)  // Too low? Move up significantly

// Medium adjustment phase (0.05f-0.2f)
position = Vector3(0f, 1.3f, 0f)  // Bit too high

// Fine adjustment phase (<0.05f)
position = Vector3(0f, 1.25f, 0f)  // Perfect
```

### 14. Visual Debugging Techniques

#### Temporary Visual Markers
Add temporary visual elements to verify positioning:
- Colored primitives at reference points
- Contrasting backgrounds for better visibility
- Grid or ruler objects for measurement
- Wireframe boxes to show bounds

#### Debug Visualization Examples

```kotlin
// Generic debug marker for any position
fun createDebugMarker(
    position: Vector3, 
    color: Color4 = Color4(1f, 0f, 0f, 1f),
    size: Float = 0.1f
): Entity {
    val marker = Entity.create()
    marker.setComponent(Mesh(Uri.parse("mesh://box")))
    marker.setComponent(Box(
        Vector3(-size/2, -size/2, -size/2), 
        Vector3(size/2, size/2, size/2)
    ))
    marker.setComponent(Material().apply { baseColor = color })
    marker.setComponent(Transform(Pose(position)))
    return marker
}

// Create axis-aligned debug lines
fun createAxisDebugger(origin: Vector3, length: Float = 1.0f) {
    // Red = X axis
    createDebugMarker(origin + Vector3(length/2, 0f, 0f), Color4(1f, 0f, 0f, 1f), 0.05f)
    // Green = Y axis  
    createDebugMarker(origin + Vector3(0f, length/2, 0f), Color4(0f, 1f, 0f, 1f), 0.05f)
    // Blue = Z axis
    createDebugMarker(origin + Vector3(0f, 0f, length/2), Color4(0f, 0f, 1f, 1f), 0.05f)
}
```

#### Contrast Enhancement
```kotlin
// Temporarily change material for better visibility
val originalMaterial = entity.getComponent<Material>()
entity.setComponent(Material().apply {
    baseColor = Color4(1f, 1f, 0f, 1f)  // Bright yellow for contrast
})
// ... test positioning ...
entity.setComponent(originalMaterial)  // Restore
```

#### Boundary Visualization
```kotlin
// Show the bounds of an area with wireframe
fun createBoundingBox(min: Vector3, max: Vector3): Entity {
    val box = Entity.create()
    box.setComponent(Mesh(Uri.parse("mesh://box")))
    box.setComponent(Box(min, max))
    box.setComponent(Material().apply { 
        baseColor = Color4(1f, 0f, 1f, 0.3f)  // Semi-transparent magenta
    })
    return box
}
```

### 15. Coordinate System Best Practices

#### Understanding Your Space
- **Units**: 1.0f = 1 meter (standard in VR)
- **Origin**: Usually floor center or play area center  
- **Handedness**: Meta Spatial SDK uses right-handed coordinates
- **Y-up Convention**: Y axis points upward, X is right, Z is forward

#### Common Offset Patterns

```kotlin
// Generic offset from any surface
fun calculateSurfaceOffset(
    surfacePosition: Vector3,
    surfaceNormal: Vector3,  // Direction surface faces
    offsetDistance: Float
): Vector3 {
    return surfacePosition + (surfaceNormal * offsetDistance)
}

// Example: Placing object 10cm from a wall
val wallPosition = Vector3(-5f, 1f, 0f)  // Left wall
val wallNormal = Vector3(1f, 0f, 0f)     // Faces right (into room)
val objectPosition = calculateSurfaceOffset(wallPosition, wallNormal, 0.1f)
```

#### Relative Positioning Patterns

```kotlin
// Position relative to another object
fun positionRelativeTo(
    referencePos: Vector3,
    offset: Vector3,
    referenceRotation: Quaternion? = null
): Vector3 {
    return if (referenceRotation != null) {
        // Apply rotation to offset for local space
        referencePos + referenceRotation.rotate(offset)
    } else {
        // World space offset
        referencePos + offset
    }
}

// Grid positioning helper
fun getGridPosition(row: Int, col: Int, spacing: Float = 1.0f): Vector3 {
    return Vector3(col * spacing, 0f, row * spacing)
}
```

#### Boundary Calculations

```kotlin
// Keep objects within bounds
fun clampToBounds(position: Vector3, minBounds: Vector3, maxBounds: Vector3): Vector3 {
    return Vector3(
        position.x.coerceIn(minBounds.x, maxBounds.x),
        position.y.coerceIn(minBounds.y, maxBounds.y),
        position.z.coerceIn(minBounds.z, maxBounds.z)
    )
}

// Check if position is within bounds
fun isInBounds(position: Vector3, minBounds: Vector3, maxBounds: Vector3): Boolean {
    return position.x in minBounds.x..maxBounds.x &&
           position.y in minBounds.y..maxBounds.y &&
           position.z in minBounds.z..maxBounds.z
}
```

### 16. Development Without Direct VR View

#### Feedback Strategies
When you can't see the VR view directly:

1. **Automated Screenshots**: Capture after each position change
2. **Position Logging**: Log coordinates with descriptive names
3. **Reference Images**: Save screenshots of correct positions
4. **Incremental Testing**: Test one change at a time

#### Screenshot-Based Workflow

```kotlin
// Position verification with logging
fun positionObject(entity: Entity, position: Vector3, description: String) {
    entity.setComponent(Transform(Pose(position)))
    Log.d("Positioning", "$description at: $position")
    
    // For critical positions, add measurement info
    val distanceFromOrigin = position.length()
    Log.d("Positioning", "Distance from origin: ${distanceFromOrigin}m")
}

// Batch testing multiple positions
fun testPositions(basePosition: Vector3, offsets: List<Vector3>) {
    offsets.forEachIndexed { index, offset ->
        val testPos = basePosition + offset
        createDebugMarker(testPos, Color4(1f, index * 0.3f, 0f, 1f))
        Log.d("PositionTest", "Test $index: $testPos")
    }
}
```

#### Remote Debugging Helpers

```kotlin
// Create visual feedback for remote debugging
fun createPositionIndicator(position: Vector3, label: String) {
    // Visual marker
    createDebugMarker(position)
    
    // Log with context
    Log.d("Position:$label", "X=${position.x}, Y=${position.y}, Z=${position.z}")
    
    // Optional: Create text label (if supported)
    // createTextLabel(position + Vector3(0f, 0.2f, 0f), label)
}

// Verification pattern for alignment
fun verifyAlignment(objectPos: Vector3, targetPos: Vector3, tolerance: Float = 0.01f): Boolean {
    val distance = (objectPos - targetPos).length()
    val aligned = distance <= tolerance
    
    Log.d("Alignment", if (aligned) "✓ Aligned" else "✗ Misaligned by ${distance}m")
    return aligned
}
```

#### Building Reference Documentation

```kotlin
// Document successful positions for reuse
data class PositionReference(
    val name: String,
    val position: Vector3,
    val rotation: Quaternion,
    val notes: String
)

// Save known good positions
val referencePositions = listOf(
    PositionReference("WallArt_Left", Vector3(-4.5f, 2f, 0f), Quaternion(0f, 90f, 0f), "Centered on left wall"),
    PositionReference("UI_Panel", Vector3(0f, 1.3f, 2f), Quaternion.identity, "Comfortable viewing height"),
    // ... more positions
)
```

### 17. Common Spatial Positioning Pitfalls

#### Z-Fighting
When objects are at the same depth, they flicker as the renderer can't determine which is in front.

**Solutions:**
```kotlin
// Add small offset perpendicular to surface
val Z_FIGHT_OFFSET = 0.01f  // 1cm is usually enough

// Example: Picture on wall
val wallZ = -5.0f
val pictureZ = wallZ + Z_FIGHT_OFFSET  // Slightly in front

// For dynamic placement
fun preventZFighting(position: Vector3, normal: Vector3, offset: Float = 0.01f): Vector3 {
    return position + (normal * offset)
}
```

#### Scale vs Size Confusion

**Common mistakes:**
```kotlin
// WRONG: Thinking Box takes center and size
Box(Vector3(0f, 0f, 0f), Vector3(2f, 2f, 2f))  // This is NOT center + size!

// CORRECT: Box takes min and max corners
Box(Vector3(-1f, -1f, -1f), Vector3(1f, 1f, 1f))  // 2x2x2 box centered at origin

// Helper to convert center/size to min/max
fun boxFromCenterSize(center: Vector3, size: Vector3): Pair<Vector3, Vector3> {
    val halfSize = size * 0.5f
    return Pair(center - halfSize, center + halfSize)
}
```

#### Rotation Side Effects

Rotation changes an object's local axes:

```kotlin
// WRONG: Applying offset after rotation
entity.setComponent(Transform(Pose(Vector3.zero, Quaternion(0f, 90f, 0f))))
entity.setComponent(Transform(Pose(Vector3(1f, 0f, 0f))))  // This overwrites rotation!

// CORRECT: Apply rotation and position together
val position = Vector3(1f, 0f, 0f)
val rotation = Quaternion(0f, 90f, 0f)
entity.setComponent(Transform(Pose(position, rotation)))

// Or calculate rotated offset
fun getRotatedOffset(rotation: Quaternion, localOffset: Vector3): Vector3 {
    // This gives you the world-space offset after rotation
    return rotation.rotate(localOffset)
}
```

#### Parent-Child Transform Stacking

Child positions are relative to parent:

```kotlin
// Parent at (5, 0, 0)
parent.setComponent(Transform(Pose(Vector3(5f, 0f, 0f))))

// Child at local (1, 0, 0) = world (6, 0, 0)
child.setComponent(Transform(Pose(Vector3(1f, 0f, 0f))))
parent.addChild(child)

// To get world position of child
fun getWorldPosition(entity: Entity): Vector3 {
    var worldPos = entity.getComponent<Transform>()?.position ?: Vector3.zero
    var current = entity.parent
    
    while (current != null) {
        val parentTransform = current.getComponent<Transform>()
        if (parentTransform != null) {
            worldPos = parentTransform.position + parentTransform.rotation.rotate(worldPos)
        }
        current = current.parent
    }
    
    return worldPos
}
```

#### Coordinate System Mismatches

Different tools/engines may use different conventions:

```kotlin
// Converting from Z-up to Y-up system
fun convertZUpToYUp(position: Vector3): Vector3 {
    return Vector3(position.x, position.z, -position.y)
}

// Converting from left-handed to right-handed
fun convertHandedness(position: Vector3): Vector3 {
    return Vector3(position.x, position.y, -position.z)
}
```

#### Precision Issues at Large Distances

VR tracking can lose precision far from origin:

```kotlin
// Keep important objects near origin
val MAX_RECOMMENDED_DISTANCE = 50f  // 50 meters

fun validatePosition(position: Vector3): Boolean {
    val distance = position.length()
    if (distance > MAX_RECOMMENDED_DISTANCE) {
        Log.w("Positioning", "Object at $position is ${distance}m from origin - may have precision issues")
        return false
    }
    return true
}
```

### 18. Making Objects Clickable with InputListener

The Meta Spatial SDK uses an InputListener pattern for handling object interactions. This section covers the correct approach for making objects respond to controller clicks.

#### Overview

To make objects clickable in Meta Spatial SDK:
1. Objects must have their `SceneObject` created by the `MeshCreationSystem`
2. Add an `InputListener` to the `SceneObject` once it's available
3. Set appropriate `hittable` properties on meshes

#### Setting Hittable Properties

Control whether objects can be clicked by setting their hittable property:

```kotlin
// Make an object clickable
val mesh = Mesh(Uri.parse("mesh://artwork"))
mesh.hittable = MeshCollision.LineTest  // Can be clicked
entity.setComponent(mesh)

// Make an object non-interactive (e.g., walls, UI panels)
val wallMesh = Mesh(Uri.parse("mesh://wall"))
wallMesh.hittable = MeshCollision.NoCollision  // Cannot be clicked
wall.setComponent(wallMesh)

// For panels, set hittable in the constructor
val panel = Panel(R.id.panel_main, MeshCollision.NoCollision)
entity.setComponent(panel)
```

#### Adding InputListener to Objects

Objects receive click events through InputListener attached to their SceneObject:

```kotlin
// Get the SceneObject (created asynchronously by MeshCreationSystem)
val sceneObjectFuture = systemManager.findSystem<SceneObjectSystem>()?.getSceneObject(entity)

// Add InputListener when SceneObject is ready
sceneObjectFuture?.thenAccept { sceneObject ->
    sceneObject.addInputListener(object : InputListener {
        override fun onInput(
            receiver: SceneObject,
            hitInfo: HitInfo,
            sourceOfInput: Entity,
            changed: Int,
            clicked: Int,
            downTime: Long
        ): Boolean {
            // Check for trigger button press
            val triggerBits = ButtonBits.ButtonTriggerL or ButtonBits.ButtonTriggerR
            if ((clicked and triggerBits) != 0) {
                // Handle the click
                Log.d("Click", "Object clicked!")
                // Perform your action here
                return true  // Consume the event
            }
            return false
        }
    })
}
```

#### Complete Working Example

Here's a complete example of making picture entities clickable:

```kotlin
private fun addInputListenersToPictures() {
    Log.d("Interaction", "Adding input listeners to pictures")
    
    // Wait for SceneObjects to be created by MeshCreationSystem
    activityScope.launch {
        delay(1000) // Give time for SceneObject creation
        
        // Find all entities with PictureComponent
        val pictureQuery = Query.where { has(PictureComponent.id) }
        val pictureEntities = pictureQuery.eval()
        
        pictureEntities.forEach { entity ->
            val picture = entity.getComponent<PictureComponent>()
            val sceneObjectFuture = systemManager.findSystem<SceneObjectSystem>()
                ?.getSceneObject(entity)
            
            sceneObjectFuture?.thenAccept { sceneObject ->
                Log.d("Interaction", "Adding InputListener to: ${picture.title}")
                
                sceneObject.addInputListener(object : InputListener {
                    override fun onInput(
                        receiver: SceneObject,
                        hitInfo: HitInfo,
                        sourceOfInput: Entity,
                        changed: Int,
                        clicked: Int,
                        downTime: Long
                    ): Boolean {
                        // Check for trigger button press
                        val triggerBits = ButtonBits.ButtonTriggerL or ButtonBits.ButtonTriggerR
                        if ((clicked and triggerBits) != 0) {
                            Log.d("Interaction", "Picture clicked: ${picture.title}")
                            // Show selection panel or perform action
                            showArtworkSelectionPanel(entity)
                            return true
                        }
                        return false
                    }
                })
            }
        }
    }
}
```

#### Required Imports

Add these imports for interaction handling:

```kotlin
import com.meta.spatial.runtime.InputListener
import com.meta.spatial.runtime.SceneObject
import com.meta.spatial.runtime.HitInfo
import com.meta.spatial.runtime.ButtonBits
import com.meta.spatial.core.Query
import com.meta.spatial.toolkit.MeshCollision
import kotlinx.coroutines.delay
```

#### Key Points to Remember

1. **Asynchronous SceneObject Creation**: SceneObjects are created asynchronously by MeshCreationSystem. Always use `thenAccept` with the CompletableFuture.

2. **Timing Matters**: Add a delay or wait for the appropriate system lifecycle before adding InputListeners to ensure SceneObjects exist.

3. **Automatic Raycast Detection**: InputListener handles all raycast detection automatically - you don't need to manually cast rays.

4. **Event Consumption**: Return `true` from `onInput` to consume the event and prevent it from propagating.

5. **Button Detection**: Use `ButtonBits` constants to check for specific button presses:
   - `ButtonBits.ButtonTriggerL` - Left controller trigger
   - `ButtonBits.ButtonTriggerR` - Right controller trigger
   - `ButtonBits.ButtonA` - A button
   - Multiple buttons: `(clicked and (ButtonBits.ButtonTriggerL or ButtonBits.ButtonTriggerR)) != 0`

6. **Non-Interactive Elements**: Set `MeshCollision.NoCollision` on decorative elements, walls, and UI panels to prevent them from blocking interactions with objects behind them.

#### Example: Making UI Panels Non-Interactive

When creating panels that shouldn't block interactions:

```kotlin
// Create a non-interactive panel
val panelEntity = Entity.create()
panelEntity.setComponent(Transform(Pose(Vector3(0f, 1.3f, 2f))))
panelEntity.setComponent(Panel(R.id.panel_main, MeshCollision.NoCollision))

// The panel will display but won't block clicks to objects behind it
```

This approach ensures clean, event-driven interaction handling that works reliably with the Meta Spatial SDK's systems.

### Critical: InputListener and Component Best Practices

**⚠️ Component Data Warning**: Complex data (objects, nullable fields) in custom components may NOT persist. When querying entities later, these fields often return null.

**✅ Correct Pattern**: Add InputListeners immediately when creating entities
```kotlin
// RIGHT: Create entity and add listener together
activityScope.launch {
    val entity = createEntity()
    delay(1000) // Wait for SceneObject
    addInputListener(entity, dataYouNeed) // Pass data directly, don't rely on components
}

// WRONG: Create entities then query later
createEntities() // Async
delay(5000)
Query.where { has(Component.id) }.eval() // Component data will be null!
```

**Key Rules:**
1. **Never** store complex data in components expecting to retrieve it later
2. **Always** add InputListeners in the same async block that creates the entity  
3. **Pass data directly** to listener functions instead of reading from components
4. **Store entity references** if needed - don't rely on queries finding async-created entities

**Quick Debugging:**
- No input detected? Check `mesh.hittable = MeshCollision.LineTest`
- Component data null? You can't persist complex data - pass it directly instead
- Query returns empty? Entities created async won't be found immediately