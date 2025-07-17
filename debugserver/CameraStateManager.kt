package vr.debugserver

import android.content.Context
import android.content.SharedPreferences
import com.meta.spatial.debugserver.utils.FileLogger
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Manages persistence of camera positions and rotations using SharedPreferences
 */
class CameraStateManager(context: Context) {
    companion object {
        private const val TAG = "CameraStateManager"
        private const val PREFS_NAME = "vr_camera_states"
        private const val KEY_SAVED_POSITIONS = "saved_positions"
        private const val KEY_START_POSITION = "start_position_name"
        private const val ROTATION_TOLERANCE = 0.1f
    }
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    
    /**
     * Save a camera position and rotation with a given name
     */
    fun savePosition(name: String, position: Position3D, rotation: Rotation3D): Boolean {
        return try {
            val savedPositions = getSavedPositions().toMutableMap()
            
            val cameraState = SavedCameraState(
                name = name,
                position = position,
                rotation = rotation,
                timestamp = System.currentTimeMillis()
            )
            
            savedPositions[name] = cameraState
            
            val json = gson.toJson(savedPositions)
            sharedPrefs.edit()
                .putString(KEY_SAVED_POSITIONS, json)
                .apply()
                
            FileLogger.d(TAG, "Saved camera position '$name': pos=(${position.x}, ${position.y}, ${position.z}), yaw=${rotation.yaw}°")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to save camera position '$name'", e)
            false
        }
    }
    
    /**
     * Load a saved camera position by name
     */
    fun loadPosition(name: String): SavedCameraState? {
        return try {
            val savedPositions = getSavedPositions()
            val state = savedPositions[name]
            
            if (state != null) {
                FileLogger.d(TAG, "Loaded camera position '$name': pos=(${state.position.x}, ${state.position.y}, ${state.position.z}), yaw=${state.rotation.yaw}°")
            } else {
                FileLogger.w(TAG, "Camera position '$name' not found")
            }
            
            state
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to load camera position '$name'", e)
            null
        }
    }
    
    /**
     * Get all saved camera positions
     */
    fun getSavedPositions(): Map<String, SavedCameraState> {
        return try {
            val json = sharedPrefs.getString(KEY_SAVED_POSITIONS, null)
            if (json != null) {
                val type = object : TypeToken<Map<String, SavedCameraState>>() {}.type
                gson.fromJson(json, type) ?: emptyMap()
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to get saved positions", e)
            emptyMap()
        }
    }
    
    /**
     * Delete a saved camera position
     */
    fun deletePosition(name: String): Boolean {
        return try {
            val savedPositions = getSavedPositions().toMutableMap()
            val removed = savedPositions.remove(name)
            
            if (removed != null) {
                val json = gson.toJson(savedPositions)
                sharedPrefs.edit()
                    .putString(KEY_SAVED_POSITIONS, json)
                    .apply()
                    
                FileLogger.d(TAG, "Deleted camera position '$name'")
                true
            } else {
                FileLogger.w(TAG, "Camera position '$name' not found for deletion")
                false
            }
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to delete camera position '$name'", e)
            false
        }
    }
    
    /**
     * Clear all saved positions
     */
    fun clearAllPositions(): Boolean {
        return try {
            sharedPrefs.edit()
                .remove(KEY_SAVED_POSITIONS)
                .apply()
                
            FileLogger.d(TAG, "Cleared all saved camera positions")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to clear saved positions", e)
            false
        }
    }
    
    /**
     * Check if two rotation values are approximately equal within tolerance
     */
    fun rotationsAreEqual(rotation1: Float, rotation2: Float): Boolean {
        val diff = Math.abs(rotation1 - rotation2)
        return diff <= ROTATION_TOLERANCE || diff >= (360f - ROTATION_TOLERANCE)
    }
    
    /**
     * Check if two positions are approximately equal within tolerance
     */
    fun positionsAreEqual(pos1: Position3D, pos2: Position3D, tolerance: Float = 0.01f): Boolean {
        return Math.abs(pos1.x - pos2.x) <= tolerance &&
               Math.abs(pos1.y - pos2.y) <= tolerance &&
               Math.abs(pos1.z - pos2.z) <= tolerance
    }
    
    /**
     * Set the start position (position to load on app startup)
     */
    fun setStartPosition(name: String): Boolean {
        return try {
            // Verify the position exists
            val savedPositions = getSavedPositions()
            if (!savedPositions.containsKey(name)) {
                FileLogger.w(TAG, "Cannot set start position - position '$name' not found")
                return false
            }
            
            sharedPrefs.edit()
                .putString(KEY_START_POSITION, name)
                .apply()
                
            FileLogger.d(TAG, "Set start position to '$name'")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to set start position", e)
            false
        }
    }
    
    /**
     * Get the name of the start position
     */
    fun getStartPositionName(): String? {
        return sharedPrefs.getString(KEY_START_POSITION, null)
    }
    
    /**
     * Load the start position if set
     */
    fun loadStartPosition(): SavedCameraState? {
        return try {
            val startPositionName = getStartPositionName()
            if (startPositionName != null) {
                FileLogger.d(TAG, "Loading start position: $startPositionName")
                loadPosition(startPositionName)
            } else {
                FileLogger.d(TAG, "No start position set")
                null
            }
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to load start position", e)
            null
        }
    }
    
    /**
     * Clear the start position
     */
    fun clearStartPosition(): Boolean {
        return try {
            sharedPrefs.edit()
                .remove(KEY_START_POSITION)
                .apply()
                
            FileLogger.d(TAG, "Cleared start position")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to clear start position", e)
            false
        }
    }
}

/**
 * Data class representing a saved camera state
 */
data class SavedCameraState(
    val name: String,
    val position: Position3D,
    val rotation: Rotation3D,
    val timestamp: Long
)

/**
 * Request models for save/load operations
 */
data class CameraSaveRequest(
    val name: String
)

data class CameraLoadRequest(
    val name: String
)

data class SavedPositionsResponse(
    val positions: Map<String, SavedCameraState>,
    val startPosition: String? = null
)

data class SetStartPositionRequest(
    val name: String
)