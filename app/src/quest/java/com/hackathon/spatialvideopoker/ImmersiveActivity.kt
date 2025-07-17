package com.hackathon.spatialvideopoker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Pose
import com.meta.spatial.core.Quaternion
import com.meta.spatial.core.Vector3
import com.meta.spatial.runtime.*
import com.meta.spatial.toolkit.*
import com.meta.spatial.vr.VRFeature
import vr.debugserver.VRDebugSystem

class ImmersiveActivity : AppSystemActivity() {
    
    val activityScope = CoroutineScope(Dispatchers.Main)
    
    override fun registerFeatures() = listOf(VRFeature(this))
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize debug server (only in debug builds)
        if (BuildConfig.DEBUG) {
            VRDebugSystem.initialize(this)
        }
    }
    
    override fun onSceneReady() {
        super.onSceneReady()
        scene.setViewOrigin(0.0f, 0.0f, 0.0f)
        
        // Register the debug system
        if (BuildConfig.DEBUG) {
            systemManager.registerSystem(VRDebugSystem.getInstance())
        }
        
        activityScope.launch {
            glXFManager.inflateGLXF(
                Uri.parse("apk:///scenes/Composition.glxf"),
                keyName = "scene")
            
            // Delay panel creation to ensure registration is complete
            kotlinx.coroutines.delay(100)
            
            // Position panel on top face of the cube
            // Cube position: (-0.044, 0.327, 0.068) with scale (3.33, 1, 4.37)
            val cubeTopY = 0.327f + (1f * 0.5f) + 0.01f // cube center Y + half height + small offset
            
            // Rotate panel to lie flat on top of cube (facing upward)
            // 90 degrees pitch rotation to make panel horizontal
            val horizontalRotation = Quaternion(pitch = 90f, yaw = 0f, roll = 0f)
            
            Entity.createPanelEntity(
                R.id.panel_main,
                Transform(Pose(Vector3(-0.044f, cubeTopY, 0.068f), horizontalRotation))
            )
        }
        
        scene.updateIBLEnvironment("chromatic.env")
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
    
    override fun registerPanels() = listOf(
        PanelRegistration(R.id.panel_main) {
            panelIntent = Intent().apply {
                setClassName(applicationContext, MainActivity::class.qualifiedName!!)
            }
            config {
                // Scale panel to fit on cube top surface (3.33 x 4.37)
                // Use 80% of cube dimensions to leave some margin
                width = 3.0f    // 80% of cube width (3.33)
                height = 1.69f  // Maintain 16:9 aspect ratio
                layoutWidthInPx = 1920
                layoutHeightInPx = 1080
                layerConfig = LayerConfig()
                panelShader = SceneMaterial.HOLE_PUNCH_SHADER
                alphaMode = AlphaMode.HOLE_PUNCH
            }
        }
    )
}