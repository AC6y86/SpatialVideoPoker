package vr.debugserver

import com.meta.spatial.debugserver.utils.FileLogger
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Pose
import com.meta.spatial.core.Quaternion
import com.meta.spatial.core.Vector3
import com.meta.spatial.runtime.Scene
import com.meta.spatial.toolkit.Box
import com.meta.spatial.toolkit.Material
import com.meta.spatial.toolkit.Mesh
import com.meta.spatial.toolkit.Plane
import com.meta.spatial.toolkit.Scale
import com.meta.spatial.toolkit.Sphere
import com.meta.spatial.toolkit.Transform
import android.net.Uri
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages 3D object spawning and lifecycle in the VR scene
 */
class VRSceneManager(private val scene: Scene) {
    
    companion object {
        private const val TAG = "VRSceneManager"
    }
    
    // Track spawned objects - map entity ID to entity and info
    private val spawnedEntities = ConcurrentHashMap<Long, Pair<Entity, SpawnedObjectInfo>>()
    
    /**
     * Spawn a new object in the scene
     */
    fun spawnObject(request: ObjectSpawnRequest): SpawnResult {
        return try {
            FileLogger.d(TAG, "Spawning ${request.type} at position (${request.position.x}, ${request.position.y}, ${request.position.z})")
            
            val entity = createEntityFromRequest(request)
            if (entity == null) {
                return SpawnResult(false, null, "Failed to create entity")
            }
            
            // Track the spawned object
            val objectInfo = SpawnedObjectInfo(
                entityId = entity.id,
                type = request.type,
                position = request.position,
                rotation = request.rotation ?: Rotation3D(0f, 0f, 0f),
                scale = request.scale ?: 1f,
                material = request.material ?: MaterialProperties()
            )
            spawnedEntities[entity.id] = Pair(entity, objectInfo)
            
            FileLogger.d(TAG, "Successfully spawned ${request.type} with entity ID: ${entity.id}")
            SpawnResult(true, entity.id, "Object spawned successfully")
            
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to spawn object", e)
            SpawnResult(false, null, "Error spawning object: ${e.message}")
        }
    }
    
    /**
     * Delete an object from the scene
     */
    fun deleteObject(entityId: Long): Boolean {
        return try {
            val entityPair = spawnedEntities[entityId]
            if (entityPair == null) {
                FileLogger.w(TAG, "Object with ID $entityId not found")
                return false
            }
            
            // Destroy the entity
            val (entity, _) = entityPair
            entity.destroy()
            spawnedEntities.remove(entityId)
            
            FileLogger.d(TAG, "Deleted object with ID: $entityId")
            true
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to delete object $entityId", e)
            false
        }
    }
    
    /**
     * List all spawned objects
     */
    fun listObjects(): List<EntityInfo> {
        return spawnedEntities.values.map { (_, obj) ->
            EntityInfo(
                id = obj.entityId,
                name = "${obj.type}_${obj.entityId}",
                position = obj.position,
                type = obj.type
            )
        }
    }
    
    /**
     * Get details of a specific object
     */
    fun getObject(entityId: Long): EntityInfo? {
        val (_, obj) = spawnedEntities[entityId] ?: return null
        return EntityInfo(
            id = obj.entityId,
            name = "${obj.type}_${obj.entityId}",
            position = obj.position,
            type = obj.type
        )
    }
    
    /**
     * Create an entity based on the spawn request
     */
    private fun createEntityFromRequest(request: ObjectSpawnRequest): Entity? {
        return when (request.type.lowercase()) {
            "box" -> createBoxEntity(request)
            "sphere" -> createSphereEntity(request)
            "plane" -> createPlaneEntity(request)
            else -> {
                FileLogger.e(TAG, "Unknown object type: ${request.type}")
                null
            }
        }
    }
    
    /**
     * Create a box entity
     */
    private fun createBoxEntity(request: ObjectSpawnRequest): Entity? {
        val boxSize = request.size as? BoxSize ?: BoxSize()
        
        val entity = Entity.create()
        
        // Add mesh component
        entity.setComponent(Mesh(Uri.parse("mesh://box")))
        
        // Add box component with size
        val halfX = boxSize.width / 2f
        val halfY = boxSize.height / 2f  
        val halfZ = boxSize.depth / 2f
        entity.setComponent(Box(
            Vector3(-halfX, -halfY, -halfZ),
            Vector3(halfX, halfY, halfZ)
        ))
        
        // Add transform
        entity.setComponent(createTransform(request))
        
        // Add scale if specified
        val scale = request.scale ?: 1f
        if (scale != 1f) {
            entity.setComponent(Scale(scale))
        }
        
        // Add material
        entity.setComponent(createMaterial(request.material))
        
        return entity
    }
    
    /**
     * Create a sphere entity
     */
    private fun createSphereEntity(request: ObjectSpawnRequest): Entity? {
        val sphereSize = request.size as? SphereSize ?: SphereSize()
        
        val entity = Entity.create()
        
        // Add mesh component
        entity.setComponent(Mesh(Uri.parse("mesh://sphere")))
        
        // Add sphere component
        entity.setComponent(Sphere(sphereSize.radius))
        
        // Add transform
        entity.setComponent(createTransform(request))
        
        // Add scale if specified
        val scale = request.scale ?: 1f
        if (scale != 1f) {
            entity.setComponent(Scale(scale))
        }
        
        // Add material
        entity.setComponent(createMaterial(request.material))
        
        return entity
    }
    
    /**
     * Create a plane entity
     */
    private fun createPlaneEntity(request: ObjectSpawnRequest): Entity? {
        val planeSize = request.size as? PlaneSize ?: PlaneSize()
        
        val entity = Entity.create()
        
        // Add mesh component
        entity.setComponent(Mesh(Uri.parse("mesh://plane")))
        
        // Add plane component
        entity.setComponent(Plane(planeSize.width, planeSize.depth))
        
        // Add transform
        entity.setComponent(createTransform(request))
        
        // Add scale if specified
        val scale = request.scale ?: 1f
        if (scale != 1f) {
            entity.setComponent(Scale(scale))
        }
        
        // Add material
        entity.setComponent(createMaterial(request.material))
        
        return entity
    }
    
    /**
     * Create transform component from request
     */
    private fun createTransform(request: ObjectSpawnRequest): Transform {
        val position = Vector3(request.position.x, request.position.y, request.position.z)
        
        val rotation = if (request.rotation != null) {
            // Convert Euler angles (degrees) to quaternion
            Quaternion(request.rotation.pitch, request.rotation.yaw, request.rotation.roll)
        } else {
            Quaternion(0f, 0f, 0f, 1f) // Identity quaternion
        }
        
        return Transform(Pose(position, rotation))
    }
    
    /**
     * Create material component from request
     */
    private fun createMaterial(materialProps: MaterialProperties?): Material {
        val props = materialProps ?: MaterialProperties()
        
        return Material().apply {
            baseColor = com.meta.spatial.core.Color4(
                props.red,
                props.green, 
                props.blue,
                props.alpha
            )
        }
    }
}

/**
 * Internal tracking for spawned objects
 */
private data class SpawnedObjectInfo(
    val entityId: Long,
    val type: String,
    val position: Position3D,
    val rotation: Rotation3D,
    val scale: Float,
    val material: MaterialProperties
)