package vr.debugserver

import android.content.Context
import com.meta.spatial.debugserver.utils.FileLogger
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Pose
import com.meta.spatial.core.Quaternion
import com.meta.spatial.core.Vector3
import com.meta.spatial.runtime.Scene
import com.meta.spatial.runtime.ControllerButton
import com.meta.spatial.runtime.ButtonBits
import com.meta.spatial.runtime.HitInfo
import com.meta.spatial.toolkit.Transform
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.asin
import kotlin.math.abs
import kotlin.math.sign

/**
 * Handles the simulation of VR input events and camera state persistence
 */
class VRInputSimulator(
    private val scene: Scene,
    private val getControllerEntity: (isLeft: Boolean) -> Entity?,
    context: Context? = null
) {
    companion object {
        private const val TAG = "VRInputSimulator"
    }
    
    private val simulationScope = CoroutineScope(Dispatchers.Main)
    private var cameraRotation = Rotation3D(0f, 0f, 0f)
    private var cameraPosition = Position3D(0f, 0f, 0f) // Use 0 for LOCAL_FLOOR reference space
    private val cameraStateManager: CameraStateManager? = context?.let { CameraStateManager(it) }
    
    /**
     * Converts Euler angles (pitch, yaw, roll) in degrees to a Quaternion
     * Uses ZYX rotation order (yaw, pitch, roll)
     */
    private fun eulerToQuaternion(pitch: Float, yaw: Float, roll: Float): Quaternion {
        // Convert degrees to radians
        val pitchRad = pitch * PI.toFloat() / 180f
        val yawRad = yaw * PI.toFloat() / 180f
        val rollRad = roll * PI.toFloat() / 180f
        
        // Calculate half angles
        val cy = cos(yawRad * 0.5f)
        val sy = sin(yawRad * 0.5f)
        val cp = cos(pitchRad * 0.5f)
        val sp = sin(pitchRad * 0.5f)
        val cr = cos(rollRad * 0.5f)
        val sr = sin(rollRad * 0.5f)
        
        // Calculate quaternion components (ZYX order)
        val w = cr * cp * cy + sr * sp * sy
        val x = sr * cp * cy - cr * sp * sy
        val y = cr * sp * cy + sr * cp * sy
        val z = cr * cp * sy - sr * sp * cy
        
        return Quaternion(w, x, y, z)
    }
    
    /**
     * Converts a Quaternion to Euler angles (pitch, yaw, roll) in degrees
     * Uses ZYX rotation order (yaw, pitch, roll)
     */
    private fun quaternionToEuler(q: Quaternion): Rotation3D {
        // Convert quaternion to Euler angles
        val sinr_cosp = 2 * (q.w * q.x + q.y * q.z)
        val cosr_cosp = 1 - 2 * (q.x * q.x + q.y * q.y)
        val roll = kotlin.math.atan2(sinr_cosp, cosr_cosp)
        
        val sinp = 2 * (q.w * q.y - q.z * q.x)
        val pitch = if (kotlin.math.abs(sinp) >= 1) {
            kotlin.math.PI.toFloat() / 2 * kotlin.math.sign(sinp)
        } else {
            kotlin.math.asin(sinp)
        }
        
        val siny_cosp = 2 * (q.w * q.z + q.x * q.y)
        val cosy_cosp = 1 - 2 * (q.y * q.y + q.z * q.z)
        val yaw = kotlin.math.atan2(siny_cosp, cosy_cosp)
        
        // Convert radians to degrees
        return Rotation3D(
            pitch = pitch * 180f / PI.toFloat(),
            yaw = yaw * 180f / PI.toFloat(),
            roll = roll * 180f / PI.toFloat()
        )
    }
    
    // Camera Control
    
    fun disableCameraLock(): Boolean {
        return try {
            scene.enableVirtualCamera(false)
            FileLogger.d(TAG, "Virtual camera disabled - camera unlocked for VR tracking")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to disable virtual camera", e)
            false
        }
    }
    
    suspend fun testVirtualCameraPose(): Boolean {
        return try {
            FileLogger.d(TAG, "Starting setVirtualCameraPose tests...")
            
            // Get current pose first
            val initialPose = scene.getViewerPose()
            FileLogger.d(TAG, "Initial pose: pos=(${initialPose.t.x}, ${initialPose.t.y}, ${initialPose.t.z}), quat=(${initialPose.q.w}, ${initialPose.q.x}, ${initialPose.q.y}, ${initialPose.q.z})")
            
            // Test 1: Identity pose (no rotation, origin position)
            FileLogger.d(TAG, "Test 1: Identity pose")
            val identityPose = Pose(Vector3(0f, 0f, 0f), Quaternion(1f, 0f, 0f, 0f))
            scene.setVirtualCameraPose(identityPose)
            delay(200)
            val result1 = scene.getViewerPose()
            FileLogger.d(TAG, "Test 1 result: pos=(${result1.t.x}, ${result1.t.y}, ${result1.t.z}), quat=(${result1.q.w}, ${result1.q.x}, ${result1.q.y}, ${result1.q.z})")
            
            // Test 2: Simple position change, no rotation
            FileLogger.d(TAG, "Test 2: Simple position change")
            val positionPose = Pose(Vector3(1f, 0f, 0f), Quaternion(1f, 0f, 0f, 0f))
            scene.setVirtualCameraPose(positionPose)
            delay(200)
            val result2 = scene.getViewerPose()
            FileLogger.d(TAG, "Test 2 result: pos=(${result2.t.x}, ${result2.t.y}, ${result2.t.z}), quat=(${result2.q.w}, ${result2.q.x}, ${result2.q.y}, ${result2.q.z})")
            
            // Test 3: Simple rotation, no position change
            FileLogger.d(TAG, "Test 3: Simple rotation")
            val rotationPose = Pose(Vector3(0f, 0f, 0f), Quaternion(0.707f, 0f, 0.707f, 0f)) // 90 degree Y rotation
            scene.setVirtualCameraPose(rotationPose)
            delay(200)
            val result3 = scene.getViewerPose()
            FileLogger.d(TAG, "Test 3 result: pos=(${result3.t.x}, ${result3.t.y}, ${result3.t.z}), quat=(${result3.q.w}, ${result3.q.x}, ${result3.q.y}, ${result3.q.z})")
            
            // Test 4: Try the current scene's pose (should be no-op)
            FileLogger.d(TAG, "Test 4: Current scene pose")
            scene.setVirtualCameraPose(result3)
            delay(200)
            val result4 = scene.getViewerPose()
            FileLogger.d(TAG, "Test 4 result: pos=(${result4.t.x}, ${result4.t.y}, ${result4.t.z}), quat=(${result4.q.w}, ${result4.q.x}, ${result4.q.y}, ${result4.q.z})")
            
            FileLogger.d(TAG, "setVirtualCameraPose tests completed")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error in setVirtualCameraPose tests", e)
            false
        }
    }
    
    suspend fun rotateCamera(request: CameraRotateRequest): Boolean {
        return try {
            FileLogger.d(TAG, "Rotating camera by: pitch=${request.pitch}°, yaw=${request.yaw}°, roll=${request.roll}°")
            
            // Get current actual position and rotation from scene
            scene.enableVirtualCamera(true)
            val currentPose = scene.getViewerPose()
            val currentPosition = currentPose.t
            val currentRotationEuler = quaternionToEuler(currentPose.q)
            
            // Add rotation deltas to current rotation
            val newPitch = currentRotationEuler.pitch + request.pitch
            val newYaw = normalizeYaw(currentRotationEuler.yaw + request.yaw)
            val newRoll = currentRotationEuler.roll + request.roll
            
            FileLogger.d(TAG, "Current rotation: pitch=${currentRotationEuler.pitch}°, yaw=${currentRotationEuler.yaw}°, roll=${currentRotationEuler.roll}°")
            FileLogger.d(TAG, "New rotation: pitch=${newPitch}°, yaw=${newYaw}°, roll=${newRoll}°")
            
            // Apply new rotation while preserving current position
            val newQuaternion = eulerToQuaternion(newPitch, newYaw, newRoll)
            val pose = Pose(currentPosition, newQuaternion)
            scene.setVirtualCameraPose(pose)
            
            FileLogger.d(TAG, "Applied rotation setVirtualCameraPose: pos=(${currentPosition.x}, ${currentPosition.y}, ${currentPosition.z}), quat=(${newQuaternion.w}, ${newQuaternion.x}, ${newQuaternion.y}, ${newQuaternion.z})")
            
            // Allow SDK time to process the camera change
            delay(100)
            
            // Update internal tracking to match what we applied
            cameraPosition = Position3D(currentPosition.x, currentPosition.y, currentPosition.z)
            cameraRotation = Rotation3D(newPitch, newYaw, newRoll)
            
            // Get and log the actual state after update
            val actualYaw = scene.getViewSceneRotation()
            val actualPos = scene.getViewOrigin()
            FileLogger.d(TAG, "Camera after rotation: actual_pos=(${actualPos.x}, ${actualPos.y}, ${actualPos.z}), actual_yaw=$actualYaw° (requested yaw=${newYaw}°)")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to rotate camera", e)
            false
        }
    }
    
    private suspend fun updateCameraView(updateRotation: Boolean = true) {
        // Use setVirtualCameraPose for camera control (full 6DOF)
        scene.enableVirtualCamera(true)
        if (updateRotation) {
            // Update both position and full rotation
            FileLogger.d(TAG, "Setting camera - pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), euler=(pitch=${cameraRotation.pitch}°, yaw=${cameraRotation.yaw}°, roll=${cameraRotation.roll}°)")
            val quaternion = eulerToQuaternion(cameraRotation.pitch, cameraRotation.yaw, cameraRotation.roll)
            val pose = Pose(Vector3(cameraPosition.x, cameraPosition.y, cameraPosition.z), quaternion)
            scene.setVirtualCameraPose(pose)
            FileLogger.d(TAG, "Applied setVirtualCameraPose: pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), quat=(${quaternion.w}, ${quaternion.x}, ${quaternion.y}, ${quaternion.z})")
        } else {
            // Update position only (preserve current rotation by getting current pose)
            FileLogger.d(TAG, "Setting camera position only - pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z})")
            val currentPose = scene.getViewerPose()
            val pose = Pose(Vector3(cameraPosition.x, cameraPosition.y, cameraPosition.z), currentPose.q)
            scene.setVirtualCameraPose(pose)
            FileLogger.d(TAG, "Applied position-only setVirtualCameraPose: pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), preserved_quat=(${currentPose.q.w}, ${currentPose.q.x}, ${currentPose.q.y}, ${currentPose.q.z})")
        }
        
        // Allow SDK time to process the camera change
        delay(100)
        
        // Keep virtual camera enabled - user can manually unlock with button if needed
        FileLogger.d(TAG, "Virtual camera remains enabled - use Unlock Camera button to restore VR tracking")
        
        // Get actual position and rotation from scene after update
        val actualPosition = scene.getViewOrigin()
        val actualYaw = scene.getViewSceneRotation()
        
        FileLogger.d(TAG, "Camera updated - actual_pos=(${actualPosition.x}, ${actualPosition.y}, ${actualPosition.z}), actual_yaw=${actualYaw}°, updateType: ${if (updateRotation) "position+rotation" else "position-only"}")
    }
    
    suspend fun setCameraPosition(request: CameraPositionRequest): Boolean {
        return try {
            cameraPosition = Position3D(request.x, request.y, request.z)
            
            FileLogger.d(TAG, "Setting camera position to: (${request.x}, ${request.y}, ${request.z})")
            
            // For position-only updates, preserve current rotation by getting current pose
            scene.enableVirtualCamera(true)
            val currentPose = scene.getViewerPose()
            val pose = Pose(Vector3(cameraPosition.x, cameraPosition.y, cameraPosition.z), currentPose.q)
            scene.setVirtualCameraPose(pose)
            FileLogger.d(TAG, "Applied position-only setVirtualCameraPose: pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), preserved_quat=(${currentPose.q.w}, ${currentPose.q.x}, ${currentPose.q.y}, ${currentPose.q.z})")
            
            // Allow SDK time to process the camera change
            delay(100)
            
            // Get and log the actual position after update
            val actualPos = scene.getViewOrigin()
            FileLogger.d(TAG, "Camera position after update: actual=(${actualPos.x}, ${actualPos.y}, ${actualPos.z}), requested=(${request.x}, ${request.y}, ${request.z})")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to set camera position", e)
            false
        }
    }
    
    // Camera State Persistence
    
    fun saveCameraPosition(request: CameraSaveRequest): Boolean {
        return try {
            if (cameraStateManager == null) {
                FileLogger.w(TAG, "Camera state manager not available - context not provided")
                return false
            }
            
            // Get current actual pose from scene (full 6DOF)
            val actualPose = scene.getViewerPose()
            val actualPosition = actualPose.t
            val actualRotation = quaternionToEuler(actualPose.q)
            
            // Log comparison between internal state and scene state
            FileLogger.d(TAG, "Saving camera position '${request.name}' - Scene pose: pos=(${actualPosition.x}, ${actualPosition.y}, ${actualPosition.z}), euler=(${actualRotation.pitch}, ${actualRotation.yaw}, ${actualRotation.roll}), quat=(${actualPose.q.w}, ${actualPose.q.x}, ${actualPose.q.y}, ${actualPose.q.z})")
            FileLogger.d(TAG, "Saving camera position '${request.name}' - Internal state: pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), euler=(${cameraRotation.pitch}, ${cameraRotation.yaw}, ${cameraRotation.roll})")
            
            // Update our tracked state to match actual scene state
            cameraPosition = Position3D(actualPosition.x, actualPosition.y, actualPosition.z)
            cameraRotation = actualRotation
            
            // Save the current position and rotation
            val success = cameraStateManager.savePosition(
                name = request.name,
                position = cameraPosition,
                rotation = cameraRotation
            )
            
            if (success) {
                FileLogger.d(TAG, "Successfully saved camera position '${request.name}': pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), euler=(pitch=${cameraRotation.pitch}°, yaw=${cameraRotation.yaw}°, roll=${cameraRotation.roll}°)")
            }
            
            success
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to save camera position '${request.name}'", e)
            false
        }
    }
    
    suspend fun loadCameraPosition(request: CameraLoadRequest): Boolean {
        return try {
            if (cameraStateManager == null) {
                FileLogger.w(TAG, "Camera state manager not available - context not provided")
                return false
            }
            
            val savedState = cameraStateManager.loadPosition(request.name)
            if (savedState == null) {
                FileLogger.w(TAG, "Camera position '${request.name}' not found")
                return false
            }
            
            // Update our internal state
            cameraPosition = savedState.position
            cameraRotation = savedState.rotation
            
            // Apply to scene using setVirtualCameraPose (full 6DOF)
            FileLogger.d(TAG, "Loading camera position '${request.name}' - pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), euler=(pitch=${cameraRotation.pitch}°, yaw=${cameraRotation.yaw}°, roll=${cameraRotation.roll}°)")
            
            // Enable virtual camera and set full pose
            scene.enableVirtualCamera(true)
            val quaternion = eulerToQuaternion(cameraRotation.pitch, cameraRotation.yaw, cameraRotation.roll)
            val pose = Pose(Vector3(cameraPosition.x, cameraPosition.y, cameraPosition.z), quaternion)
            scene.setVirtualCameraPose(pose)
            FileLogger.d(TAG, "Applied setVirtualCameraPose: pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), quat=(${quaternion.w}, ${quaternion.x}, ${quaternion.y}, ${quaternion.z})")
            
            // Allow SDK time to process the camera change
            delay(100)
            
            // Keep virtual camera enabled - user can manually unlock with button if needed
            FileLogger.d(TAG, "Virtual camera remains enabled - use Unlock Camera button to restore VR tracking")
            
            // Verify the actual state after update
            val actualPosition = scene.getViewOrigin()
            val actualYaw = scene.getViewSceneRotation()
            
            FileLogger.d(TAG, "Successfully loaded camera position '${request.name}': actual_pos=(${actualPosition.x}, ${actualPosition.y}, ${actualPosition.z}), actual_yaw=${actualYaw}°")
            
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to load camera position '${request.name}'", e)
            false
        }
    }
    
    fun getSavedPositions(): SavedPositionsResponse {
        return try {
            if (cameraStateManager == null) {
                FileLogger.w(TAG, "Camera state manager not available - context not provided")
                return SavedPositionsResponse(emptyMap())
            }
            
            val savedPositions = cameraStateManager.getSavedPositions()
            val startPositionName = cameraStateManager.getStartPositionName()
            SavedPositionsResponse(savedPositions, startPositionName)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to get saved positions", e)
            SavedPositionsResponse(emptyMap())
        }
    }
    
    fun deleteSavedPosition(name: String): Boolean {
        return try {
            if (cameraStateManager == null) {
                FileLogger.w(TAG, "Camera state manager not available - context not provided")
                return false
            }
            
            val success = cameraStateManager.deletePosition(name)
            if (success) {
                FileLogger.d(TAG, "Successfully deleted camera position '$name'")
            }
            
            success
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to delete camera position '$name'", e)
            false
        }
    }
    
    fun setStartPosition(request: SetStartPositionRequest): Boolean {
        return try {
            if (cameraStateManager == null) {
                FileLogger.w(TAG, "Camera state manager not available - context not provided")
                return false
            }
            
            val success = cameraStateManager.setStartPosition(request.name)
            if (success) {
                FileLogger.d(TAG, "Set start position to '${request.name}'")
            }
            
            success
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to set start position", e)
            false
        }
    }
    
    fun clearStartPosition(): Boolean {
        return try {
            if (cameraStateManager == null) {
                FileLogger.w(TAG, "Camera state manager not available - context not provided")
                return false
            }
            
            val success = cameraStateManager.clearStartPosition()
            if (success) {
                FileLogger.d(TAG, "Cleared start position")
            }
            
            success
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to clear start position", e)
            false
        }
    }
    
    suspend fun loadStartPositionIfSet(): Boolean {
        return try {
            if (cameraStateManager == null) {
                FileLogger.w(TAG, "Camera state manager not available - context not provided")
                return false
            }
            
            val startPosition = cameraStateManager.loadStartPosition()
            if (startPosition != null) {
                // Update our internal state
                cameraPosition = startPosition.position
                cameraRotation = startPosition.rotation
                
                // Apply to scene using setVirtualCameraPose (full 6DOF)
                FileLogger.d(TAG, "Loading start position - pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), euler=(pitch=${cameraRotation.pitch}°, yaw=${cameraRotation.yaw}°, roll=${cameraRotation.roll}°)")
                
                scene.enableVirtualCamera(true)
                val quaternion = eulerToQuaternion(cameraRotation.pitch, cameraRotation.yaw, cameraRotation.roll)
                val pose = Pose(Vector3(cameraPosition.x, cameraPosition.y, cameraPosition.z), quaternion)
                scene.setVirtualCameraPose(pose)
                FileLogger.d(TAG, "Applied start position setVirtualCameraPose: pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), quat=(${quaternion.w}, ${quaternion.x}, ${quaternion.y}, ${quaternion.z})")
                
                // Allow SDK time to process the camera change
                delay(100)
                
                // Keep virtual camera enabled - user can manually unlock with button if needed
                FileLogger.d(TAG, "Virtual camera remains enabled - use Unlock Camera button to restore VR tracking")
                
                // Verify the actual state after update
                val actualPosition = scene.getViewOrigin()
                val actualYaw = scene.getViewSceneRotation()
                
                FileLogger.d(TAG, "Successfully loaded start position: actual_pos=(${actualPosition.x}, ${actualPosition.y}, ${actualPosition.z}), actual_yaw=${actualYaw}°")
                true
            } else {
                FileLogger.d(TAG, "No start position set")
                false
            }
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to load start position", e)
            false
        }
    }
    
    // Controller Control
    
    fun pointController(request: ControllerPointRequest): Boolean {
        return try {
            val isLeft = request.controller.lowercase() == "left"
            val controller = getControllerEntity(isLeft)
            
            if (controller == null) {
                FileLogger.w(TAG, "Controller entity not found: ${request.controller}")
                return false
            }
            
            val targetPosition = when {
                request.screen != null -> screenToWorldPosition(request.screen)
                request.world != null -> Vector3(
                    request.world.x,
                    request.world.y,
                    request.world.z
                )
                else -> {
                    FileLogger.w(TAG, "No target position specified")
                    return false
                }
            }
            
            // Calculate controller orientation to point at target
            val controllerTransform = controller.getComponent<Transform>()
            val controllerPos = controllerTransform?.transform?.t ?: Vector3(0f, 1.2f, -0.5f)
            
            val direction = (targetPosition - controllerPos).normalize()
            val rotation = calculateLookRotation(direction)
            
            controller.setComponent(Transform(Pose(controllerPos, rotation)))
            
            FileLogger.d(TAG, "Controller ${request.controller} pointing at: $targetPosition")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to point controller", e)
            false
        }
    }
    
    fun moveController(request: ControllerMoveRequest): Boolean {
        return try {
            val isLeft = request.controller.lowercase() == "left"
            val controller = getControllerEntity(isLeft)
            
            if (controller == null) {
                FileLogger.w(TAG, "Controller entity not found: ${request.controller}")
                return false
            }
            
            val newPosition = Vector3(
                request.position.x,
                request.position.y,
                request.position.z
            )
            
            val currentTransform = controller.getComponent<Transform>()
            val currentRotation = currentTransform?.transform?.q ?: Quaternion()
            
            controller.setComponent(Transform(Pose(newPosition, currentRotation)))
            
            FileLogger.d(TAG, "Controller ${request.controller} moved to: $newPosition")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to move controller", e)
            false
        }
    }
    
    // Button Simulation
    
    fun simulateTrigger(request: TriggerRequest): Boolean {
        return try {
            val button = if (request.controller.lowercase() == "left") {
                ControllerButton.LeftTrigger
            } else {
                ControllerButton.RightTrigger
            }
            
            simulateButtonPress(button, request.action == "press")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to simulate trigger", e)
            false
        }
    }
    
    fun simulateButton(request: ButtonRequest): Boolean {
        return try {
            val button = parseButton(request.button, request.controller)
            if (button == null) {
                FileLogger.w(TAG, "Unknown button: ${request.button}")
                return false
            }
            
            simulateButtonPress(button, request.action == "press")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to simulate button", e)
            false
        }
    }
    
    // Scene Information
    
    fun getSceneState(): SceneStateResponse {
        val entities = mutableListOf<EntityInfo>()
        
        // Get actual camera info from scene
        val actualPosition = scene.getViewOrigin()
        val actualYaw = scene.getViewSceneRotation()
        
        // Update our tracked values to match scene state
        cameraPosition = Position3D(actualPosition.x, actualPosition.y, actualPosition.z)
        cameraRotation = Rotation3D(cameraRotation.pitch, actualYaw, cameraRotation.roll)
        
        // Get camera info
        val cameraInfo = CameraInfo(
            position = cameraPosition,
            rotation = cameraRotation
        )
        
        // Get controller info
        val leftController = getControllerEntity(true)
        val rightController = getControllerEntity(false)
        
        val leftInfo = leftController?.let {
            val transform = it.getComponent<Transform>()
            val pos = transform?.transform?.t ?: Vector3(-0.3f, 1.2f, -0.5f)
            ControllerInfo(
                position = Position3D(pos),
                pointing = null // TODO: Calculate pointing target
            )
        } ?: ControllerInfo(Position3D(-0.3f, 1.2f, -0.5f))
        
        val rightInfo = rightController?.let {
            val transform = it.getComponent<Transform>()
            val pos = transform?.transform?.t ?: Vector3(0.3f, 1.2f, -0.5f)
            ControllerInfo(
                position = Position3D(pos),
                pointing = null // TODO: Calculate pointing target
            )
        } ?: ControllerInfo(Position3D(0.3f, 1.2f, -0.5f))
        
        val controllersInfo = ControllersInfo(left = leftInfo, right = rightInfo)
        
        return SceneStateResponse(
            entities = entities,
            camera = cameraInfo,
            controllers = controllersInfo
        )
    }
    
    // Helper Functions
    
    private fun screenToWorldPosition(screen: ScreenPosition): Vector3 {
        // Convert normalized screen coordinates to world position
        // This is a simplified implementation - you may need to adjust based on your scene setup
        val x = (screen.x - 0.5f) * 10f  // Map 0-1 to -5 to 5
        val y = screen.y * 3f + 0.5f     // Map 0-1 to 0.5 to 3.5
        val z = -3f                       // Default depth
        
        return Vector3(x, y, z)
    }
    
    private fun calculateLookRotation(direction: Vector3): Quaternion {
        // Calculate quaternion to look in the given direction
        return Quaternion.lookRotation(direction, Vector3.Up)
    }
    
    private fun simulateButtonPress(button: ControllerButton, isPress: Boolean) {
        simulationScope.launch {
            // Convert ControllerButton to ButtonBits
            val buttonBit = when (button) {
                ControllerButton.A -> ButtonBits.ButtonA
                ControllerButton.B -> ButtonBits.ButtonB
                ControllerButton.X -> ButtonBits.ButtonX
                ControllerButton.Y -> ButtonBits.ButtonY
                ControllerButton.LeftTrigger -> ButtonBits.ButtonTriggerL
                ControllerButton.RightTrigger -> ButtonBits.ButtonTriggerR
                ControllerButton.LeftSqueeze -> ButtonBits.ButtonSqueezeL
                ControllerButton.RightSqueeze -> ButtonBits.ButtonSqueezeR
                else -> 0 // Menu button and others not available in ButtonBits
            }
            
            if (buttonBit == 0) {
                FileLogger.w(TAG, "Unsupported button for simulation: $button")
                return@launch
            }
            
            // TODO: Implement actual button event dispatch
            // This would require access to the input system or creating synthetic events
            FileLogger.d(TAG, "Simulating button ${button.name} ${if (isPress) "press" else "release"}")
            
            if (isPress) {
                // Simulate press
                delay(100) // Hold for 100ms
                // Simulate release
                FileLogger.d(TAG, "Auto-releasing button ${button.name}")
            }
        }
    }
    
    private fun parseButton(buttonName: String, controller: String): ControllerButton? {
        val isLeft = controller.lowercase() == "left"
        
        return when (buttonName.uppercase()) {
            "A" -> ControllerButton.A
            "B" -> ControllerButton.B
            "X" -> ControllerButton.X
            "Y" -> ControllerButton.Y
            "TRIGGER" -> if (isLeft) ControllerButton.LeftTrigger else ControllerButton.RightTrigger
            "SQUEEZE", "GRIP" -> if (isLeft) ControllerButton.LeftSqueeze else ControllerButton.RightSqueeze
            else -> null // Menu and other buttons not supported
        }
    }
    
    private fun normalizeYaw(yaw: Float): Float {
        // Normalize yaw to -180 to 180 range
        var normalized = yaw % 360f
        if (normalized > 180f) {
            normalized -= 360f
        } else if (normalized < -180f) {
            normalized += 360f
        }
        return normalized
    }
}