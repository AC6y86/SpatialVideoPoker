package vr.debugserver

import com.meta.spatial.core.Vector3

// Request Models

data class CameraRotateRequest(
    val pitch: Float = 0f,  // Rotation around X axis (degrees)
    val yaw: Float = 0f,    // Rotation around Y axis (degrees)
    val roll: Float = 0f    // Rotation around Z axis (degrees)
)

data class CameraPositionRequest(
    val x: Float,
    val y: Float,
    val z: Float
)

data class ControllerPointRequest(
    val controller: String,  // "left" or "right"
    val screen: ScreenPosition? = null,
    val world: WorldPosition? = null
)

data class ScreenPosition(
    val x: Float,  // 0.0 to 1.0
    val y: Float   // 0.0 to 1.0
)

data class WorldPosition(
    val x: Float,
    val y: Float,
    val z: Float
)

data class ControllerMoveRequest(
    val controller: String,  // "left" or "right"
    val position: WorldPosition
)

data class TriggerRequest(
    val controller: String,  // "left" or "right"
    val action: String      // "press" or "release"
)

data class ButtonRequest(
    val controller: String,  // "left" or "right"
    val button: String,     // Button name: A, B, X, Y, Menu, Squeeze, etc.
    val action: String      // "press" or "release"
)

data class WebhookRegistration(
    val url: String,
    val headers: Map<String, String>? = null
)

// Object Spawning Models

data class ObjectSpawnRequest(
    val type: String,                        // "box", "sphere", "plane"
    val position: Position3D,                // World position
    val rotation: Rotation3D? = null,        // Optional rotation (Euler angles in degrees)
    val scale: Float? = null,                // Optional uniform scale factor
    val size: ObjectSize,                    // Type-specific size parameters
    val material: MaterialProperties? = null  // Optional material properties
)

// Size specifications for different object types
sealed class ObjectSize

data class BoxSize(
    val width: Float = 1f,    // X dimension
    val height: Float = 1f,   // Y dimension  
    val depth: Float = 1f     // Z dimension
) : ObjectSize()

data class SphereSize(
    val radius: Float = 0.5f
) : ObjectSize()

data class PlaneSize(
    val width: Float = 2f,    // X dimension
    val depth: Float = 2f     // Z dimension
) : ObjectSize()

// Material properties
data class MaterialProperties(
    val red: Float = 1f,      // 0.0 to 1.0
    val green: Float = 1f,    // 0.0 to 1.0
    val blue: Float = 1f,     // 0.0 to 1.0
    val alpha: Float = 1f     // 0.0 to 1.0, transparency
)

// Object spawning response
data class SpawnResult(
    val success: Boolean,
    val entityId: Long? = null,
    val message: String? = null
)

// Object listing response
data class ObjectsListResponse(
    val objects: List<EntityInfo>
)

// Response Models

data class AppReadyResponse(
    val ready: Boolean,
    val timestamp: String = System.currentTimeMillis().toString(),
    val scene: SceneInfo? = null
)

data class SceneInfo(
    val entities: Int,
    val controllers: Int = 2
)

data class SuccessResponse(
    val success: Boolean = true,
    val message: String? = null
)

data class ErrorResponse(
    val error: String,
    val details: String? = null
)

data class SceneStateResponse(
    val entities: List<EntityInfo>,
    val camera: CameraInfo,
    val controllers: ControllersInfo
)

data class EntityInfo(
    val id: Long,
    val name: String?,
    val position: Position3D,
    val type: String
)

data class Position3D(
    val x: Float,
    val y: Float,
    val z: Float
) {
    constructor(vector: Vector3) : this(vector.x, vector.y, vector.z)
}

data class Rotation3D(
    val pitch: Float,
    val yaw: Float,
    val roll: Float
)

data class CameraInfo(
    val position: Position3D,
    val rotation: Rotation3D
)

data class ControllersInfo(
    val left: ControllerInfo,
    val right: ControllerInfo
)

data class ControllerInfo(
    val position: Position3D,
    val pointing: Position3D? = null,
    val buttonsPressed: List<String> = emptyList()
)

// Internal Models

data class AppState(
    var isReady: Boolean = false,
    var sceneInfo: SceneInfo? = null,
    val webhooks: MutableList<WebhookRegistration> = mutableListOf()
)

// Configuration

data class DebugServerConfig(
    val appName: String = "SpatialApp",
    val port: Int = 8080,
    val enableWebUI: Boolean = true,
    val authToken: String? = null,
    val maxRequestSize: Long = 10 * 1024 * 1024, // 10MB
    val allowedOrigins: List<String> = listOf("*"),
    val logFileName: String? = null, // Auto-generated from appName if null
    val enableFileLogging: Boolean = true
)

// App-specific extension models can be defined in project packages
// Example: com.yourapp.debug.models.*

