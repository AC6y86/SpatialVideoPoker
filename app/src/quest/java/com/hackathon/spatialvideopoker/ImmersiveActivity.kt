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
        // Camera position will be managed by debug system - don't reset to origin
        
        // Register the debug system
        if (BuildConfig.DEBUG) {
            systemManager.registerSystem(VRDebugSystem.getInstance())
        }
        
        activityScope.launch {
            // Load scene with editor-placed panel
            glXFManager.inflateGLXF(
                Uri.parse("apk:///scenes/Composition.glxf"),
                keyName = "scene")
            
            // No need to create panel entity - it's already in the scene from the editor
            // The panel registration will automatically link to the @id/panel_main panel
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