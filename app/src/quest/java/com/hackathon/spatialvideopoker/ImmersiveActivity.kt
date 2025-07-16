package com.hackathon.spatialvideopoker

import android.content.Intent
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Pose
import com.meta.spatial.core.Quaternion
import com.meta.spatial.core.Vector3
import com.meta.spatial.runtime.*
import com.meta.spatial.toolkit.*
import com.meta.spatial.vr.VRFeature

class ImmersiveActivity : AppSystemActivity() {
    
    override fun registerFeatures() = listOf(VRFeature(this))
    
    override fun onSceneReady() {
        super.onSceneReady()
        
        // Configure scene
        scene.setViewOrigin(0.0f, 0.0f, 0.0f)
        scene.enableHolePunching(true)
        scene.setReferenceSpace(ReferenceSpace.LOCAL_FLOOR)
        
        // Create panel entity
        Entity.createPanelEntity(
            R.id.panel_main,
            Transform(Pose(Vector3(0f, 1.3f, 2f), Quaternion(0f, 0f, 0f)))
        )
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