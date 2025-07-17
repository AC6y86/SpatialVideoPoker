package vr.debugserver

import android.content.Context
import com.gallery.artbrowser.utils.FileLogger
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
    
    // Camera Control
    
    fun rotateCamera(request: CameraRotateRequest): Boolean {
        return try {
            FileLogger.d(TAG, "Rotating camera by: pitch=${request.pitch}°, yaw=${request.yaw}°, roll=${request.roll}°")
            
            // Add rotation to current values
            var newYaw = cameraRotation.yaw + request.yaw
            
            // Normalize yaw to -180 to 180 range for better behavior
            newYaw = normalizeYaw(newYaw)
            
            cameraRotation = Rotation3D(
                pitch = cameraRotation.pitch + request.pitch,
                yaw = newYaw,
                roll = cameraRotation.roll + request.roll
            )
            
            FileLogger.d(TAG, "Normalized yaw from ${cameraRotation.yaw - request.yaw}° to $newYaw°")
            
            updateCameraView()
            
            // Get and log the actual rotation after update
            val actualYaw = scene.getViewSceneRotation()
            val actualPos = scene.getViewOrigin()
            FileLogger.d(TAG, "Camera after rotation: position=(${actualPos.x}, ${actualPos.y}, ${actualPos.z}), yaw=$actualYaw° (requested total yaw=${cameraRotation.yaw}°)")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to rotate camera", e)
            false
        }
    }
    
    private fun updateCameraView(updateRotation: Boolean = true) {
        // Scene.setViewOrigin only supports yaw rotation in current SDK version
        // Unfortunately, we cannot directly apply pitch rotation
        // We'll track it for debugging/logging purposes
        
        if (updateRotation) {
            // Update both position and rotation
            scene.setViewOrigin(
                cameraPosition.x,
                cameraPosition.y,
                cameraPosition.z,
                cameraRotation.yaw
            )
        } else {
            // Update position only (3-parameter version)
            scene.setViewOrigin(
                cameraPosition.x,
                cameraPosition.y,
                cameraPosition.z
            )
        }
        
        // Get actual position and rotation from scene after update
        val actualPosition = scene.getViewOrigin()
        val actualYaw = scene.getViewSceneRotation()
        
        FileLogger.d(TAG, "Camera updated - position: (${actualPosition.x}, ${actualPosition.y}, ${actualPosition.z}), rotation: $actualYaw°, updateType: ${if (updateRotation) "position+rotation" else "position-only"}")
    }
    
    fun setCameraPosition(request: CameraPositionRequest): Boolean {
        return try {
            cameraPosition = Position3D(request.x, request.y, request.z)
            
            // Only reset rotation if this is explicitly a reset position request (0,0,0)
            // Otherwise preserve the current rotation
            if (request.x == 0f && request.y == 0f && request.z == 0f) {
                // This is a reset request - don't change rotation
                // Let the web UI handle rotation reset separately
            }
            
            FileLogger.d(TAG, "Setting camera position to: (${request.x}, ${request.y}, ${request.z})")
            
            // Update the camera view with position only (don't update rotation)
            updateCameraView(updateRotation = false)
            
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
            
            // Get current actual state from scene
            val actualPosition = scene.getViewOrigin()
            val actualYaw = scene.getViewSceneRotation()
            
            // Update our tracked values to match scene state
            cameraPosition = Position3D(actualPosition.x, actualPosition.y, actualPosition.z)
            cameraRotation = Rotation3D(cameraRotation.pitch, actualYaw, cameraRotation.roll)
            
            // Save the current position and rotation
            val success = cameraStateManager.savePosition(
                name = request.name,
                position = cameraPosition,
                rotation = cameraRotation
            )
            
            if (success) {
                FileLogger.d(TAG, "Successfully saved camera position '${request.name}': pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), yaw=${cameraRotation.yaw}°")
            }
            
            success
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to save camera position '${request.name}'", e)
            false
        }
    }
    
    fun loadCameraPosition(request: CameraLoadRequest): Boolean {
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
            
            // Apply to scene using 4-parameter version since we need both position and rotation
            scene.setViewOrigin(
                cameraPosition.x,
                cameraPosition.y,
                cameraPosition.z,
                cameraRotation.yaw
            )
            
            // Verify the actual state after update
            val actualPosition = scene.getViewOrigin()
            val actualYaw = scene.getViewSceneRotation()
            
            FileLogger.d(TAG, "Successfully loaded camera position '${request.name}': pos=(${actualPosition.x}, ${actualPosition.y}, ${actualPosition.z}), yaw=${actualYaw}°")
            
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
    
    fun loadStartPositionIfSet(): Boolean {
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
                
                // Apply to scene using 4-parameter version since we need both position and rotation
                scene.setViewOrigin(
                    cameraPosition.x,
                    cameraPosition.y,
                    cameraPosition.z,
                    cameraRotation.yaw
                )
                
                FileLogger.d(TAG, "Successfully loaded start position: pos=(${cameraPosition.x}, ${cameraPosition.y}, ${cameraPosition.z}), yaw=${cameraRotation.yaw}°")
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