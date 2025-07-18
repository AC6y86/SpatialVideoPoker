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
        
        // Set reference space for proper floor-relative tracking
        scene.setReferenceSpace(ReferenceSpace.LOCAL_FLOOR)
        
        // Position camera back from panel - LOCAL_FLOOR handles height automatically
        scene.setViewOrigin(0.0f, 0.0f, 1.56f, 180.0f)
        
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
                // 16:10 aspect ratio - optimized for Medium Tablet (2560×1600)
                layoutWidthInPx = 2560   // 16:10 resolution width
                layoutHeightInPx = 1600  // 16:10 resolution height (2560/1600 = 1.6)
                layerConfig = LayerConfig()
                panelShader = SceneMaterial.HOLE_PUNCH_SHADER
                alphaMode = AlphaMode.HOLE_PUNCH
            }
        }
    )
}