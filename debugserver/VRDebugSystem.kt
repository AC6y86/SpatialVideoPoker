package vr.debugserver

import android.content.Context
import android.net.wifi.WifiManager
import com.meta.spatial.debugserver.utils.FileLogger
import com.meta.spatial.core.Entity
import com.meta.spatial.core.Query
import com.meta.spatial.core.SystemBase
import com.meta.spatial.runtime.Scene
import com.meta.spatial.toolkit.AppSystemActivity
import com.meta.spatial.toolkit.Controller
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/**
 * Spatial SDK System that manages the debug server and provides access to VR components
 */
class VRDebugSystem private constructor(
    private val activity: AppSystemActivity
) : SystemBase() {
    
    companion object {
        private const val TAG = "VRDebugSystem"
        private var instance: VRDebugSystem? = null
        
        fun initialize(activity: AppSystemActivity, configBlock: DebugServerConfig.() -> Unit = {}) {
            if (instance != null) {
                FileLogger.w(TAG, "VRDebugSystem already initialized")
                return
            }
            
            DebugServerConfiguration.updateConfig(configBlock)
            val config = DebugServerConfiguration.getConfig()
            
            // Initialize FileLogger with configuration
            FileLogger.initialize(
                activity, 
                config.appName, 
                config.logFileName,
                config.enableFileLogging
            )
            
            instance = VRDebugSystem(activity)
            FileLogger.d(TAG, "VRDebugSystem initialized for app: ${config.appName}")
        }
        
        fun getInstance(): VRDebugSystem {
            return instance ?: throw IllegalStateException("VRDebugSystem not initialized. Call initialize() first.")
        }
        
        fun shutdown() {
            instance?.stopServer()
            instance = null
            FileLogger.d(TAG, "VRDebugSystem shut down")
            FileLogger.close()
        }
    }
    
    private var server: VRDebugServer? = null
    private val appState = AppState()
    private var inputSimulator: VRInputSimulator? = null
    private var sceneManager: VRSceneManager? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Register an app-specific debug extension
     */
    fun registerExtension(extension: AppDebugExtension) {
        server?.registerExtension(extension)
            ?: FileLogger.w(TAG, "Server not yet initialized, extension will be registered when server starts")
    }
    
    /**
     * Get the scene instance for extensions
     */
    fun getVRScene(): Scene = activity.scene
    
    /**
     * Get the activity instance for extensions
     */
    fun getActivity(): AppSystemActivity = activity
    
    /**
     * Get the input simulator for extensions
     */
    fun getInputSimulator(): VRInputSimulator? = inputSimulator
    
    
    init {
        FileLogger.d(TAG, "VRDebugSystem initialized")
        startServer()
    }
    
    override fun execute() {
        // This system doesn't need regular updates
    }
    
    fun notifyAppReady() {
        FileLogger.d(TAG, "App ready notification triggered")
        
        appState.isReady = true
        appState.sceneInfo = SceneInfo(
            entities = countEntities(),
            controllers = 2
        )
        
        // Load start position if set
        coroutineScope.launch {
            try {
                delay(500) // Small delay to ensure scene is fully ready
                if (inputSimulator?.loadStartPositionIfSet() == true) {
                    FileLogger.i(TAG, "Start position loaded successfully")
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Failed to load start position", e)
            }
        }
        
        // Notify all registered webhooks
        coroutineScope.launch {
            notifyWebhooks()
        }
        
        // Log the server URL
        logServerInfo()
    }
    
    private fun startServer() {
        try {
            val config = DebugServerConfiguration.getConfig()
            val scene = activity.scene
            
            // Create input simulator
            inputSimulator = VRInputSimulator(scene, { isLeft ->
                getControllerEntity(isLeft)
            }, activity.applicationContext)
            
            // Initialize scene manager
            sceneManager = VRSceneManager(scene)
            
            // Create and start server
            server = VRDebugServer(
                config.port,
                inputSimulator!!,
                sceneManager!!,
                appState
            ).apply {
                initializeExtensions(this@VRDebugSystem)
                start()
            }
            
            FileLogger.i(TAG, "Debug server started on port ${config.port}")
            logServerInfo()
            
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to start debug server", e)
        }
    }
    
    private fun stopServer() {
        try {
            server?.stop()
            server = null
            FileLogger.i(TAG, "Debug server stopped")
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to stop debug server", e)
        }
    }
    
    private fun getControllerEntity(isLeft: Boolean): Entity? {
        return try {
            // Query for controller entities
            val controllers = Query.where { has(Controller.id) }
                .eval()
            
            // For now, return the first controller found
            // In a real implementation, you'd need to distinguish left/right
            controllers.firstOrNull()
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to get controller entity", e)
            null
        }
    }
    
    private fun countEntities(): Int {
        return try {
            Query.where { }.eval().count()
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to count entities", e)
            0
        }
    }
    
    private fun logServerInfo() {
        try {
            val ipAddress = getDeviceIpAddress()
            val config = DebugServerConfiguration.getConfig()
            
            FileLogger.i(TAG, "╔════════════════════════════════════════════════════════╗")
            FileLogger.i(TAG, "║              VR DEBUG SERVER RUNNING                    ║")
            FileLogger.i(TAG, "╠════════════════════════════════════════════════════════╣")
            FileLogger.i(TAG, "║ Web UI:   http://$ipAddress:${config.port}/              ")
            FileLogger.i(TAG, "║ API Base: http://$ipAddress:${config.port}/api           ")
            FileLogger.i(TAG, "║ Status:   ${if (appState.isReady) "READY" else "NOT READY"}                                   ")
            FileLogger.i(TAG, "╚════════════════════════════════════════════════════════╝")
            
            // Also log to system console for easier access
            println("VR Debug Server: http://$ipAddress:${config.port}/")
            
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to log server info", e)
        }
    }
    
    private fun getDeviceIpAddress(): String {
        return try {
            val wifiManager = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ipInt = wifiInfo.ipAddress
            
            if (ipInt != 0) {
                // Convert integer IP to string
                String.format(
                    "%d.%d.%d.%d",
                    ipInt and 0xff,
                    ipInt shr 8 and 0xff,
                    ipInt shr 16 and 0xff,
                    ipInt shr 24 and 0xff
                )
            } else {
                "localhost"
            }
        } catch (e: Exception) {
            FileLogger.e(TAG, "Failed to get IP address", e)
            "localhost"
        }
    }
    
    private suspend fun notifyWebhooks() {
        val readyNotification = AppReadyResponse(
            ready = true,
            timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date()),
            scene = appState.sceneInfo
        )
        
        val json = com.google.gson.Gson().toJson(readyNotification)
        
        appState.webhooks.forEach { webhook ->
            try {
                withContext(Dispatchers.IO) {
                    val url = URL(webhook.url)
                    val connection = url.openConnection() as HttpURLConnection
                    
                    connection.apply {
                        requestMethod = "POST"
                        doOutput = true
                        setRequestProperty("Content-Type", "application/json")
                        
                        // Add custom headers if provided
                        webhook.headers?.forEach { (key, value) ->
                            setRequestProperty(key, value)
                        }
                        
                        outputStream.use { os ->
                            os.write(json.toByteArray())
                        }
                        
                        val responseCode = responseCode
                        if (responseCode == HttpURLConnection.HTTP_OK) {
                            FileLogger.d(TAG, "Successfully notified webhook: ${webhook.url}")
                        } else {
                            FileLogger.w(TAG, "Webhook notification failed: ${webhook.url} (HTTP $responseCode)")
                        }
                        
                        disconnect()
                    }
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Failed to notify webhook: ${webhook.url}", e)
            }
        }
    }
}