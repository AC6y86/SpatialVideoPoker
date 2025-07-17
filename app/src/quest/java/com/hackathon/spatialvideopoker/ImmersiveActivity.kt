package com.hackathon.spatialvideopoker

import android.content.Intent
import android.net.Uri
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

class ImmersiveActivity : AppSystemActivity() {
    
    val activityScope = CoroutineScope(Dispatchers.Main)
    
    override fun registerFeatures() = listOf(VRFeature(this))
    
    override fun onSceneReady() {
        super.onSceneReady()
        scene.setViewOrigin(0.0f, 0.0f, 0.0f)
        
        activityScope.launch {
            glXFManager.inflateGLXF(
                Uri.parse("apk:///scenes/Composition.glxf"),
                keyName = "scene")
            
            // Delay panel creation to ensure registration is complete
            kotlinx.coroutines.delay(100)
            Entity.createPanelEntity(
                R.id.panel_main,
                Transform(Pose(Vector3(0f, 1.3f, 2f), Quaternion(0f, 0f, 0f)))
            )
        }
        
        scene.updateIBLEnvironment("chromatic.env")
    }
    
    override fun registerPanels() = listOf(
        PanelRegistration(R.id.panel_main) {
            panelIntent = Intent().apply {
                setClassName(applicationContext, MainActivity::class.qualifiedName!!)
            }
            config {
                width = 3.84f
                height = 2.16f
                layoutWidthInPx = 1920
                layoutHeightInPx = 1080
                layerConfig = LayerConfig()
                panelShader = SceneMaterial.HOLE_PUNCH_SHADER
                alphaMode = AlphaMode.HOLE_PUNCH
            }
        }
    )
}