package vr.debugserver

import com.meta.spatial.debugserver.utils.FileLogger
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import fi.iki.elonen.NanoHTTPD
import java.io.IOException
import java.io.File
import kotlinx.coroutines.runBlocking

/**
 * HTTP server that handles REST API requests for VR input simulation
 */
class VRDebugServer(
    port: Int,
    private val inputSimulator: VRInputSimulator,
    private val sceneManager: VRSceneManager,
    private val appState: AppState
) : NanoHTTPD(port) {
    
    private val extensionRegistry = ExtensionRegistry()
    
    companion object {
        private const val TAG = "VRDebugServer"
        private const val MIME_JSON = "application/json"
        private const val MIME_HTML = "text/html"
    }
    
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(ObjectSize::class.java, ObjectSizeDeserializer())
        .create()
    
    /**
     * Initialize the server with debug system reference for extensions
     */
    fun initializeExtensions(debugSystem: VRDebugSystem) {
        extensionRegistry.initialize(debugSystem)
    }
    
    /**
     * Register an app-specific extension
     */
    fun registerExtension(extension: AppDebugExtension) {
        extensionRegistry.registerExtension(extension)
    }
    
    override fun serve(session: IHTTPSession): Response {
        return try {
            // Add CORS headers for web access
            val response = handleRequest(session)
            response.addHeader("Access-Control-Allow-Origin", "*")
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
            response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization")
            response
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error handling request", e)
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Internal server error: ${e.message}")
        }
    }
    
    private fun handleRequest(session: IHTTPSession): Response {
        val method = session.method
        val uri = session.uri
        
        // Handle OPTIONS for CORS preflight
        if (method == Method.OPTIONS) {
            return newFixedLengthResponse(Response.Status.OK, MIME_JSON, "")
        }
        
        // Route to appropriate handler
        return when {
            uri == "/" && method == Method.GET -> serveWebUI()
            uri == "/api/app/ready" && method == Method.GET -> handleAppReady()
            uri == "/api/app/ui-content" && method == Method.GET -> handleAppUIContent()
            uri.startsWith("/api/app/") && uri != "/api/app/ready" && uri != "/api/app/ui-content" -> {
                // Route to extension
                extensionRegistry.handleExtensionRequest(uri, method, session)
                    ?: createErrorResponse(Response.Status.NOT_FOUND, "Extension endpoint not found: $uri")
            }
            uri == "/api/scene/info" && method == Method.GET -> handleSceneInfo()
            uri == "/api/camera/rotate" && method == Method.POST -> handleCameraRotate(session)
            uri == "/api/camera/test-rotation" && method == Method.POST -> handleCameraTestRotation(session)
            uri == "/api/camera/position" && method == Method.POST -> handleCameraPosition(session)
            uri == "/api/camera/test-virtual-pose" && method == Method.POST -> handleTestVirtualCameraPose()
            uri == "/api/camera/save" && method == Method.POST -> handleCameraSave(session)
            uri == "/api/camera/saved" && method == Method.GET -> handleCameraSavedList()
            uri.startsWith("/api/camera/load/") && method == Method.POST -> handleCameraLoad(session)
            uri.startsWith("/api/camera/saved/") && method == Method.DELETE -> handleCameraDelete(session)
            uri == "/api/camera/start-position" && method == Method.POST -> handleSetStartPosition(session)
            uri == "/api/camera/start-position" && method == Method.DELETE -> handleClearStartPosition()
            uri == "/api/camera/unlock" && method == Method.POST -> handleCameraUnlock()
            uri == "/api/controller/point" && method == Method.POST -> handleControllerPoint(session)
            uri == "/api/controller/move" && method == Method.POST -> handleControllerMove(session)
            uri == "/api/input/trigger" && method == Method.POST -> handleTrigger(session)
            uri == "/api/input/button" && method == Method.POST -> handleButton(session)
            uri == "/api/objects/spawn" && method == Method.POST -> handleObjectSpawn(session)
            uri == "/api/objects/list" && method == Method.GET -> handleObjectsList()
            uri.startsWith("/api/objects/") && method == Method.DELETE -> handleObjectDelete(session)
            uri == "/api/webhooks/register" && method == Method.POST -> handleWebhookRegister(session)
            uri == "/api/logs" && method == Method.GET -> handleGetLogFiles()
            uri == "/api/logs/clear" && method == Method.POST -> handleClearLogFiles()
            uri == "/api/logs/download" && method == Method.GET -> handleDownloadLog()
            uri == "/api/logs/recent" && method == Method.GET -> handleGetRecentLogs(session)
            uri == "/api/time" && method == Method.GET -> handleGetTime()
            else -> createErrorResponse(Response.Status.NOT_FOUND, "Endpoint not found: $uri")
        }
    }
    
    // Handlers
    
    private fun handleAppReady(): Response {
        val response = AppReadyResponse(
            ready = appState.isReady,
            scene = appState.sceneInfo
        )
        return createJsonResponse(response)
    }
    
    private fun handleAppUIContent(): Response {
        val content = extensionRegistry.getAllWebUIContent()
        val response = mapOf("content" to content)
        return createJsonResponse(response)
    }
    
    private fun handleSceneInfo(): Response {
        return if (appState.isReady) {
            val sceneState = inputSimulator.getSceneState()
            createJsonResponse(sceneState)
        } else {
            createErrorResponse(Response.Status.SERVICE_UNAVAILABLE, "App not ready yet")
        }
    }
    
    private fun handleCameraRotate(session: IHTTPSession): Response {
        val request = parseJsonBody<CameraRotateRequest>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        return try {
            val success = runBlocking { inputSimulator.rotateCamera(request) }
            if (success) {
                createJsonResponse(SuccessResponse(message = "Camera rotated"))
            } else {
                createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to rotate camera")
            }
        } catch (e: Exception) {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to rotate camera: ${e.message}")
        }
    }
    
    private fun handleCameraTestRotation(session: IHTTPSession): Response {
        val request = parseJsonBody<CameraRotateRequest>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        FileLogger.d(TAG, "TEST ROTATION: pitch=${request.pitch}°, yaw=${request.yaw}°, roll=${request.roll}°")
        
        return try {
            val success = runBlocking { inputSimulator.rotateCamera(request) }
            if (success) {
                createJsonResponse(SuccessResponse(message = "Test rotation applied: pitch=${request.pitch}°, yaw=${request.yaw}°, roll=${request.roll}°"))
            } else {
                createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to apply test rotation")
            }
        } catch (e: Exception) {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to apply test rotation: ${e.message}")
        }
    }
    
    private fun handleCameraPosition(session: IHTTPSession): Response {
        val request = parseJsonBody<CameraPositionRequest>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        return try {
            val success = runBlocking { inputSimulator.setCameraPosition(request) }
            if (success) {
                createJsonResponse(SuccessResponse(message = "Camera position set"))
            } else {
                createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to set camera position")
            }
        } catch (e: Exception) {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to set camera position: ${e.message}")
        }
    }
    
    private fun handleTestVirtualCameraPose(): Response {
        return try {
            val success = runBlocking { inputSimulator.testVirtualCameraPose() }
            if (success) {
                createJsonResponse(SuccessResponse(message = "Virtual camera pose tests completed"))
            } else {
                createErrorResponse(Response.Status.INTERNAL_ERROR, "Virtual camera pose tests failed")
            }
        } catch (e: Exception) {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Virtual camera pose tests failed: ${e.message}")
        }
    }
    
    private fun handleControllerPoint(session: IHTTPSession): Response {
        val request = parseJsonBody<ControllerPointRequest>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        if (request.screen == null && request.world == null) {
            return createErrorResponse(Response.Status.BAD_REQUEST, "Either 'screen' or 'world' position required")
        }
        
        return if (inputSimulator.pointController(request)) {
            createJsonResponse(SuccessResponse(message = "Controller pointed"))
        } else {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to point controller")
        }
    }
    
    private fun handleControllerMove(session: IHTTPSession): Response {
        val request = parseJsonBody<ControllerMoveRequest>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        return if (inputSimulator.moveController(request)) {
            createJsonResponse(SuccessResponse(message = "Controller moved"))
        } else {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to move controller")
        }
    }
    
    private fun handleTrigger(session: IHTTPSession): Response {
        val request = parseJsonBody<TriggerRequest>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        if (request.action != "press" && request.action != "release") {
            return createErrorResponse(Response.Status.BAD_REQUEST, "Action must be 'press' or 'release'")
        }
        
        return if (inputSimulator.simulateTrigger(request)) {
            createJsonResponse(SuccessResponse(message = "Trigger ${request.action}ed"))
        } else {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to simulate trigger")
        }
    }
    
    private fun handleButton(session: IHTTPSession): Response {
        val request = parseJsonBody<ButtonRequest>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        if (request.action != "press" && request.action != "release") {
            return createErrorResponse(Response.Status.BAD_REQUEST, "Action must be 'press' or 'release'")
        }
        
        return if (inputSimulator.simulateButton(request)) {
            createJsonResponse(SuccessResponse(message = "Button ${request.button} ${request.action}ed"))
        } else {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to simulate button")
        }
    }
    
    private fun handleWebhookRegister(session: IHTTPSession): Response {
        val webhook = parseJsonBody<WebhookRegistration>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        appState.webhooks.add(webhook)
        FileLogger.d(TAG, "Webhook registered: ${webhook.url}")
        
        return createJsonResponse(SuccessResponse(message = "Webhook registered"))
    }
    
    private fun handleCameraSave(session: IHTTPSession): Response {
        val request = parseJsonBody<CameraSaveRequest>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        if (request.name.isBlank()) {
            return createErrorResponse(Response.Status.BAD_REQUEST, "Name cannot be empty")
        }
        
        return if (inputSimulator.saveCameraPosition(request)) {
            createJsonResponse(SuccessResponse(message = "Camera position saved as '${request.name}'"))
        } else {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to save camera position")
        }
    }
    
    private fun handleCameraSavedList(): Response {
        val response = inputSimulator.getSavedPositions()
        return createJsonResponse(response)
    }
    
    private fun handleCameraLoad(session: IHTTPSession): Response {
        val uri = session.uri
        val name = uri.substringAfterLast("/")
        
        if (name.isBlank()) {
            return createErrorResponse(Response.Status.BAD_REQUEST, "Position name cannot be empty")
        }
        
        val request = CameraLoadRequest(name)
        return try {
            val success = runBlocking { inputSimulator.loadCameraPosition(request) }
            if (success) {
                createJsonResponse(SuccessResponse(message = "Camera position '${name}' loaded"))
            } else {
                createErrorResponse(Response.Status.NOT_FOUND, "Camera position '${name}' not found")
            }
        } catch (e: Exception) {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to load camera position: ${e.message}")
        }
    }
    
    private fun handleCameraDelete(session: IHTTPSession): Response {
        val uri = session.uri
        val name = uri.substringAfterLast("/")
        
        if (name.isBlank()) {
            return createErrorResponse(Response.Status.BAD_REQUEST, "Position name cannot be empty")
        }
        
        return if (inputSimulator.deleteSavedPosition(name)) {
            createJsonResponse(SuccessResponse(message = "Camera position '${name}' deleted"))
        } else {
            createErrorResponse(Response.Status.NOT_FOUND, "Camera position '${name}' not found")
        }
    }
    
    private fun handleSetStartPosition(session: IHTTPSession): Response {
        val request = parseJsonBody<SetStartPositionRequest>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        if (request.name.isBlank()) {
            return createErrorResponse(Response.Status.BAD_REQUEST, "Position name cannot be empty")
        }
        
        return if (inputSimulator.setStartPosition(request)) {
            createJsonResponse(SuccessResponse(message = "Start position set to '${request.name}'"))
        } else {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to set start position")
        }
    }
    
    private fun handleClearStartPosition(): Response {
        return if (inputSimulator.clearStartPosition()) {
            createJsonResponse(SuccessResponse(message = "Start position cleared"))
        } else {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to clear start position")
        }
    }
    
    private fun handleCameraUnlock(): Response {
        return if (inputSimulator.disableCameraLock()) {
            createJsonResponse(SuccessResponse(message = "Camera unlocked - VR tracking restored"))
        } else {
            createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to unlock camera")
        }
    }
    
    private fun handleObjectSpawn(session: IHTTPSession): Response {
        val request = parseJsonBody<ObjectSpawnRequest>(session) ?: return createErrorResponse(
            Response.Status.BAD_REQUEST, "Invalid request body"
        )
        
        // Validate object type
        if (!listOf("box", "sphere", "plane").contains(request.type.lowercase())) {
            return createErrorResponse(Response.Status.BAD_REQUEST, "Invalid object type: ${request.type}")
        }
        
        return if (appState.isReady) {
            val result = sceneManager.spawnObject(request)
            if (result.success) {
                createJsonResponse(result)
            } else {
                createErrorResponse(Response.Status.INTERNAL_ERROR, result.message ?: "Failed to spawn object")
            }
        } else {
            createErrorResponse(Response.Status.SERVICE_UNAVAILABLE, "App not ready yet")
        }
    }
    
    private fun handleObjectsList(): Response {
        return if (appState.isReady) {
            val objects = sceneManager.listObjects()
            val response = ObjectsListResponse(objects)
            createJsonResponse(response)
        } else {
            createErrorResponse(Response.Status.SERVICE_UNAVAILABLE, "App not ready yet")
        }
    }
    
    private fun handleObjectDelete(session: IHTTPSession): Response {
        val uri = session.uri
        val entityIdStr = uri.substringAfterLast("/")
        
        if (entityIdStr.isBlank()) {
            return createErrorResponse(Response.Status.BAD_REQUEST, "Entity ID cannot be empty")
        }
        
        val entityId = try {
            entityIdStr.toLong()
        } catch (e: NumberFormatException) {
            return createErrorResponse(Response.Status.BAD_REQUEST, "Invalid entity ID: $entityIdStr")
        }
        
        return if (appState.isReady) {
            if (sceneManager.deleteObject(entityId)) {
                createJsonResponse(SuccessResponse(message = "Object deleted successfully"))
            } else {
                createErrorResponse(Response.Status.NOT_FOUND, "Object with ID $entityId not found")
            }
        } else {
            createErrorResponse(Response.Status.SERVICE_UNAVAILABLE, "App not ready yet")
        }
    }
    
    private fun handleGetLogFiles(): Response {
        val logFiles = FileLogger.getLogFiles()
        val fileInfoList = logFiles.map { file ->
            mapOf(
                "name" to file.name,
                "size" to file.length(),
                "lastModified" to file.lastModified()
            )
        }
        return createJsonResponse(mapOf("files" to fileInfoList))
    }
    
    private fun handleClearLogFiles(): Response {
        FileLogger.clearLogs()
        return createJsonResponse(SuccessResponse(message = "Log files cleared"))
    }
    
    private fun handleDownloadLog(): Response {
        val logFiles = FileLogger.getLogFiles()
        val mainLog = logFiles.firstOrNull()
        
        return if (mainLog != null && mainLog.exists()) {
            val content = mainLog.readText()
            val response = newFixedLengthResponse(Response.Status.OK, "text/plain", content)
            response.addHeader("Content-Disposition", "attachment; filename=\"${mainLog.name}\"")
            response
        } else {
            createErrorResponse(Response.Status.NOT_FOUND, "Log file not found")
        }
    }
    
    private fun handleGetRecentLogs(session: IHTTPSession): Response {
        val logFiles = FileLogger.getLogFiles()
        val mainLog = logFiles.firstOrNull()
        
        // Get the 'since' parameter from query parameters
        val sinceParam = session.parms["since"]
        
        return if (mainLog != null && mainLog.exists()) {
            try {
                val lines = mainLog.readLines()
                // Get last 50 lines
                val recentLines = lines.takeLast(50)
                val logData = recentLines.mapNotNull { line ->
                    // Parse log format: "2024-01-01 12:00:00.000 LEVEL/TAG: message"
                    val parts = line.split(" ", limit = 3)
                    if (parts.size >= 3) {
                        val timestamp = "${parts[0]} ${parts[1]}"
                        val levelAndTag = parts[2]
                        val colonIndex = levelAndTag.indexOf(": ")
                        val logEntry = if (colonIndex > 0) {
                            val levelTag = levelAndTag.substring(0, colonIndex)
                            val message = levelAndTag.substring(colonIndex + 2)
                            val slashIndex = levelTag.indexOf("/")
                            if (slashIndex > 0) {
                                val level = levelTag.substring(0, slashIndex)
                                val tag = levelTag.substring(slashIndex + 1)
                                mapOf(
                                    "timestamp" to timestamp,
                                    "level" to level,
                                    "tag" to tag,
                                    "message" to message
                                )
                            } else {
                                mapOf(
                                    "timestamp" to timestamp,
                                    "level" to "INFO",
                                    "tag" to "Unknown",
                                    "message" to line
                                )
                            }
                        } else {
                            mapOf(
                                "timestamp" to timestamp,
                                "level" to "INFO", 
                                "tag" to "Unknown",
                                "message" to line
                            )
                        }
                        
                        // Filter based on 'since' parameter if provided
                        if (sinceParam != null && timestamp.isNotEmpty()) {
                            // Compare timestamps as strings since they're in format "YYYY-MM-DD HH:mm:ss.SSS"
                            if (timestamp.compareTo(sinceParam) > 0) {
                                logEntry
                            } else {
                                null
                            }
                        } else {
                            logEntry
                        }
                    } else {
                        // Handle malformed log lines
                        val logEntry = mapOf(
                            "timestamp" to "",
                            "level" to "INFO",
                            "tag" to "Unknown", 
                            "message" to line
                        )
                        // Only include if no 'since' filter or if timestamp is empty (malformed)
                        if (sinceParam == null) logEntry else null
                    }
                }
                createJsonResponse(mapOf("logs" to logData))
            } catch (e: Exception) {
                createErrorResponse(Response.Status.INTERNAL_ERROR, "Failed to read logs: ${e.message}")
            }
        } else {
            createJsonResponse(mapOf("logs" to emptyList<Map<String, String>>()))
        }
    }
    
    private fun handleGetTime(): Response {
        val currentTime = FileLogger.getCurrentTimestamp()
        return createJsonResponse(mapOf("timestamp" to currentTime))
    }
    
    private fun serveWebUI(): Response {
        val html = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>VR Debug Control</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f0f0f0;
        }
        .container {
            background-color: white;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            margin-bottom: 20px;
        }
        h1, h2 {
            color: #333;
        }
        .status {
            display: inline-block;
            width: 20px;
            height: 20px;
            border-radius: 50%;
            margin-left: 10px;
            vertical-align: middle;
        }
        .status.ready {
            background-color: #4CAF50;
        }
        .status.not-ready {
            background-color: #f44336;
        }
        button {
            background-color: #4CAF50;
            color: white;
            padding: 10px 20px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            margin: 5px;
            font-size: 16px;
        }
        button:hover {
            background-color: #45a049;
        }
        .control-group {
            margin: 20px 0;
        }
        .arrow-controls {
            display: grid;
            grid-template-columns: repeat(3, 80px);
            gap: 5px;
            margin: 20px 0;
        }
        .arrow-controls button {
            width: 100%;
            height: 40px;
        }
        #log {
            background-color: #f9f9f9;
            border: 1px solid #ddd;
            padding: 10px;
            height: 300px;
            overflow-y: auto;
            font-family: monospace;
            font-size: 12px;
        }
        .log-level-ERROR { color: #d32f2f; }
        .log-level-WARN { color: #f57c00; }
        .log-level-INFO { color: #1976d2; }
        .log-level-DEBUG { color: #388e3c; }
        .log-level-VERBOSE { color: #757575; }
        
        /* Extension styles */
        ${extensionRegistry.getAllWebUIStyles()}
    </style>
</head>
<body>
    <div class="container">
        <h1>VR Debug Control <span id="status" class="status not-ready"></span></h1>
        
        
        <div class="control-group">
            <h2>Camera Position Controls (0.1 step)</h2>
            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 5px; width: 200px; margin: 0 auto 20px auto;">
                <div></div>
                <button onclick="adjustPosition(0, 0, -0.1)" style="padding: 8px;">⬆ -Z</button>
                <div></div>
                <button onclick="adjustPosition(0.1, 0, 0)" style="padding: 8px;">← +X</button>
                <button onclick="adjustPosition(0, 0, 0.1)" style="padding: 8px;">⬇ +Z</button>
                <button onclick="adjustPosition(-0.1, 0, 0)" style="padding: 8px;">→ -X</button>
                <button onclick="adjustPosition(0, 0.1, 0)" style="padding: 8px;">↑ +Y</button>
                <button onclick="adjustPosition(0, -0.1, 0)" style="padding: 8px;">↓ -Y</button>
                <div></div>
            </div>
            <div style="text-align: center;">
                <button onclick="resetCameraPosition()" style="background-color: #f44336; color: white; padding: 8px 16px;">Reset Position</button>
            </div>
        </div>
        
        <div class="control-group">
            <h2>Camera Rotation Controls (5° step)</h2>
            <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 5px; width: 250px; margin: 0 auto 20px auto;">
                <div></div>
                <button onclick="adjustRotation(5, 0, 0)" style="padding: 8px;">↑ +Pitch</button>
                <div></div>
                <button onclick="adjustRotation(0, 5, 0)" style="padding: 8px;">← -Yaw</button>
                <button onclick="adjustRotation(-5, 0, 0)" style="padding: 8px;">↓ -Pitch</button>
                <button onclick="adjustRotation(0, -5, 0)" style="padding: 8px;">→ +Yaw</button>
                <button onclick="adjustRotation(0, 0, 5)" style="padding: 8px;">⟲ -Roll</button>
                <button onclick="adjustRotation(0, 0, -5)" style="padding: 8px;">⟳ +Roll</button>
                <div></div>
            </div>
            <div style="text-align: center;">
                <button onclick="resetCameraRotation()" style="background-color: #f44336; color: white; padding: 8px 16px;">Reset Rotation</button>
            </div>
        </div>
        
        <div class="control-group">
            <h2>ROTATION TESTS (15° steps)</h2>
            <div style="display: flex; gap: 10px; margin-bottom: 10px; justify-content: center;">
                <button onclick="testRotation(15, 0, 0)" style="background-color: #4CAF50; color: white; padding: 10px 20px;">TEST PITCH +15°</button>
                <button onclick="testRotation(0, 15, 0)" style="background-color: #2196F3; color: white; padding: 10px 20px;">TEST YAW +15°</button>
                <button onclick="testRotation(0, 0, 15)" style="background-color: #FF9800; color: white; padding: 10px 20px;">TEST ROLL +15°</button>
            </div>
            <div style="text-align: center; margin-bottom: 20px;">
                <button onclick="resetCameraRotation()" style="background-color: #f44336; color: white; padding: 8px 16px;">Reset Before Each Test</button>
            </div>
        </div>
        
        <div class="control-group">
            <h2>Debug Actions</h2>
            <button onclick="logCameraPosition()">Log Camera Position</button>
        </div>
        
        <div class="control-group">
            <h2>Save/Load Camera Position</h2>
            <div style="display: flex; gap: 10px; margin-bottom: 10px; align-items: center;">
                <input type="text" id="savePositionName" placeholder="Position name" style="flex: 1; padding: 8px;">
                <button onclick="saveCurrentPosition()">Save Current</button>
            </div>
            <div style="margin-bottom: 10px;">
                <button onclick="refreshSavedPositions()" style="margin-bottom: 10px;">Refresh Positions</button>
            </div>
            <table id="savedPositionsTable" style="width: 100%; border-collapse: collapse; margin-bottom: 10px;">
                <thead>
                    <tr style="background-color: #f5f5f5;">
                        <th style="border: 1px solid #ddd; padding: 8px; text-align: left;">Name</th>
                        <th style="border: 1px solid #ddd; padding: 8px; text-align: left;">Position</th>
                        <th style="border: 1px solid #ddd; padding: 8px; text-align: left;">Rotation</th>
                        <th style="border: 1px solid #ddd; padding: 8px; text-align: center;">Actions</th>
                    </tr>
                </thead>
                <tbody id="savedPositionsTableBody">
                    <tr>
                        <td colspan="4" style="border: 1px solid #ddd; padding: 8px; text-align: center; font-style: italic; color: #666;">
                            No saved positions found
                        </td>
                    </tr>
                </tbody>
            </table>
            <div style="display: flex; gap: 10px; margin-top: 10px; align-items: center;">
                <button onclick="unlockCamera()" style="background-color: #e91e63; color: white;">Unlock Camera</button>
                <button onclick="clearStartPosition()" style="background-color: #f44336;">Clear Start Position</button>
                <span id="startPositionLabel" style="margin-left: 10px; font-style: italic; color: #666;"></span>
            </div>
        </div>
        
        <div class="control-group">
            <h2>Object Spawning</h2>
            <div style="display: flex; gap: 20px; flex-wrap: wrap; margin-bottom: 20px;">
                <button onclick="spawnRedCube()">Spawn Red Cube</button>
                <button onclick="spawnBlueSphere()">Spawn Blue Sphere</button>
                <button onclick="spawnGreenPlane()">Spawn Green Plane</button>
                <button onclick="spawnRandomObject()">Spawn Random</button>
            </div>
            
            <div style="display: flex; gap: 10px; align-items: center; margin-bottom: 10px;">
                <select id="spawnedObjectsList" style="flex: 1; padding: 8px;">
                    <option value="">Select spawned object...</option>
                </select>
                <button onclick="deleteSelectedObject()">Delete</button>
                <button onclick="refreshSpawnedObjects()">Refresh</button>
            </div>
            
            <div style="font-size: 12px; color: #666;">
                Objects spawn near the camera position with some randomization
            </div>
        </div>
        
        <div class="control-group">
            <h2>App Specific Actions</h2>
            <div id="app-specific-content">
                ${extensionRegistry.getAllWebUIContent()}
                <p style="color: #666; font-style: italic;">App-specific actions will appear here when extensions are registered</p>
            </div>
        </div>
    </div>
    
    <div class="container">
        <h2>Debug Server Log (Live App Logs)</h2>
        <div style="margin-bottom: 10px;">
            <label style="margin-right: 20px;">
                <input type="checkbox" id="autoRefreshAppLogs" checked> Auto-refresh App Logs
            </label>
            <button onclick="refreshAppLogs()">Refresh Now</button>
            <button onclick="clearAppLogs()">Clear</button>
        </div>
        <div id="log"></div>
    </div>
    
    <div class="container">
        <h2>Application Log Files (VR App Logs)</h2>
        <div id="logFiles"></div>
        <button onclick="refreshLogs()">Refresh</button>
        <button onclick="downloadLogs()">Download Current Log</button>
        <button onclick="clearLogs()" style="background-color: #f44336;">Clear All Logs</button>
    </div>
    
    <div class="container">
        <h2>API Documentation</h2>
        <div style="font-family: monospace; font-size: 14px; line-height: 1.6;">
            <h3>Core VR Debug API</h3>
            <div style="margin-bottom: 20px;">
                <strong>GET /api/app/ready</strong> - Check if VR app is ready<br>
                <strong>GET /api/scene/info</strong> - Get current scene state (camera position, rotation)<br>
                <strong>POST /api/camera/rotate</strong> - Rotate camera (body: {"pitch": 0, "yaw": 15, "roll": 0})<br>
                <strong>POST /api/camera/position</strong> - Move camera (body: {"x": 0, "y": 0, "z": 0})<br>
                <strong>POST /api/camera/save</strong> - Save current camera position (body: {"name": "position1"})<br>
                <strong>GET /api/camera/saved</strong> - List all saved camera positions<br>
                <strong>POST /api/camera/load/{name}</strong> - Load saved camera position<br>
                <strong>DELETE /api/camera/saved/{name}</strong> - Delete saved camera position<br>
                <strong>POST /api/camera/start-position</strong> - Set default start position (body: {"name": "position1"})<br>
                <strong>DELETE /api/camera/start-position</strong> - Clear default start position<br>
                <strong>POST /api/objects/spawn</strong> - Spawn 3D objects (box, sphere, plane)<br>
                <strong>GET /api/objects/list</strong> - List all spawned objects<br>
                <strong>DELETE /api/objects/{id}</strong> - Delete spawned object by ID<br>
            </div>
            
            <h3>Extension API</h3>
            <div style="margin-bottom: 20px;">
                <strong>GET /api/app/{namespace}/{endpoint}</strong> - App-specific extension endpoints<br>
                <div style="margin-left: 20px; color: #666; font-size: 12px;">
                    Extensions can register custom endpoints under their namespace.<br>
                    See your app's debug extension documentation for available endpoints.
                </div>
            </div>
            
            ${extensionRegistry.getAllApiDocumentation()}
            
            <h3>Logging API</h3>
            <div style="margin-bottom: 20px;">
                <strong>GET /api/logs</strong> - List available log files<br>
                <strong>GET /api/logs/recent</strong> - Get recent log entries (last 50 lines)<br>
                <strong>GET /api/logs/download</strong> - Download current log file<br>
                <strong>POST /api/logs/clear</strong> - Clear all log files<br>
                <strong>GET /api/time</strong> - Get current server timestamp<br>
            </div>
            
            <h3>Input Simulation API</h3>
            <div style="margin-bottom: 20px;">
                <strong>POST /api/controller/point</strong> - Point controller at screen/world position<br>
                <strong>POST /api/controller/move</strong> - Move controller to world position<br>
                <strong>POST /api/input/trigger</strong> - Simulate trigger press/release<br>
                <strong>POST /api/input/button</strong> - Simulate button press/release<br>
            </div>
            
            <div style="margin-top: 20px; padding: 10px; background-color: #f0f0f0; border-radius: 4px;">
                <strong>Base URL:</strong> <span id="baseUrl">http://[quest-ip]:8080</span><br>
                <strong>Content-Type:</strong> application/json (for POST requests)<br>
                <strong>CORS:</strong> Enabled for web access
            </div>
        </div>
    </div>

    <script>
        const API_BASE = '';
        let isReady = false;
        let currentCameraYaw = 0;
        let clearTimestamp = null; // Track when logs were cleared
        let lastDisplayedTimestamp = null; // Track the most recent log timestamp displayed
        let displayedLogEntries = new Set(); // Track displayed log entries to prevent duplicates
        
        // Track current camera position and rotation internally
        let currentCameraPosition = { x: 0, y: 0, z: 0 };
        let currentCameraRotation = { pitch: 0, yaw: 0, roll: 0 };
        
        // Update base URL display with current host
        document.addEventListener('DOMContentLoaded', function() {
            const baseUrl = document.getElementById('baseUrl');
            if (baseUrl) {
                baseUrl.textContent = window.location.protocol + '//' + window.location.host;
            }
        });
        
        async function log(message) {
            const logDiv = document.getElementById('log');
            try {
                const response = await fetch(API_BASE + '/api/time');
                const data = await response.json();
                const timestamp = data.timestamp;
                logDiv.innerHTML += '[' + timestamp + '] ' + message + '<br>';
            } catch (error) {
                // Fallback to local time if server time unavailable
                const now = new Date();
                const year = now.getFullYear();
                const month = String(now.getMonth() + 1).padStart(2, '0');
                const day = String(now.getDate()).padStart(2, '0');
                const hours = String(now.getHours()).padStart(2, '0');
                const minutes = String(now.getMinutes()).padStart(2, '0');
                const seconds = String(now.getSeconds()).padStart(2, '0');
                const millis = String(now.getMilliseconds()).padStart(3, '0');
                const timestamp = `${'$'}{year}-${'$'}{month}-${'$'}{day} ${'$'}{hours}:${'$'}{minutes}:${'$'}{seconds}.${'$'}{millis}`;
                logDiv.innerHTML += '[' + timestamp + '] ' + message + '<br>';
            }
            logDiv.scrollTop = logDiv.scrollHeight;
        }
        
        let appLogRefreshInterval = null;
        
        async function refreshAppLogs() {
            try {
                // Use the most recent displayed timestamp or clearTimestamp for filtering
                const filterTimestamp = clearTimestamp || lastDisplayedTimestamp;
                const url = filterTimestamp 
                    ? API_BASE + '/api/logs/recent?since=' + encodeURIComponent(filterTimestamp)
                    : API_BASE + '/api/logs/recent';
                
                const response = await fetch(url);
                const data = await response.json();
                
                const logDiv = document.getElementById('log');
                
                if (data.logs && data.logs.length > 0) {
                    // Filter logs to only show new ones that haven't been displayed yet
                    const newLogs = data.logs.filter(logEntry => {
                        // Create a unique key for this log entry
                        const logKey = logEntry.timestamp + '_' + logEntry.level + '_' + logEntry.tag + '_' + logEntry.message;
                        
                        // Check if we've already displayed this exact log entry
                        if (displayedLogEntries.has(logKey)) {
                            return false; // Skip this duplicate
                        }
                        
                        // If we have a filter timestamp, also check that
                        if (filterTimestamp && logEntry.timestamp <= filterTimestamp) {
                            return false; // Skip logs older than filter
                        }
                        
                        return true; // This is a new log entry
                    });
                    
                    // Only update the display if we have new logs to show
                    if (newLogs.length > 0) {
                        // If clearTimestamp is set, we need to rebuild the display
                        if (clearTimestamp) {
                            // Filter existing web UI logs based on clearTimestamp
                            const existingLines = logDiv.innerHTML.split('<br>');
                            const filteredExistingLogs = existingLines.filter(line => {
                                if (!line.trim()) return false;
                                // Extract timestamp from format: [YYYY-MM-DD HH:mm:ss.SSS] message
                                const timestampMatch = line.match(/\[(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3})\]/);
                                if (timestampMatch) {
                                    return timestampMatch[1] > clearTimestamp;
                                }
                                return false; // Remove lines without timestamps
                            });
                            logDiv.innerHTML = filteredExistingLogs.join('<br>');
                        }
                        
                        // Add only the new log entries
                        newLogs.forEach(logEntry => {
                            // Add to displayed set to prevent future duplicates
                            const logKey = logEntry.timestamp + '_' + logEntry.level + '_' + logEntry.tag + '_' + logEntry.message;
                            displayedLogEntries.add(logKey);
                            
                            const levelClass = 'log-level-' + logEntry.level;
                            const logLine = '<div class="' + levelClass + '">[' + logEntry.timestamp + '] ' + logEntry.level + '/' + logEntry.tag + ': ' + logEntry.message + '</div>';
                            logDiv.innerHTML += logLine;
                        });
                        
                        // Update the last displayed timestamp to the most recent new log
                        if (newLogs.length > 0) {
                            lastDisplayedTimestamp = newLogs[newLogs.length - 1].timestamp;
                        }
                        
                        logDiv.scrollTop = logDiv.scrollHeight;
                    }
                } else if (!clearTimestamp && !lastDisplayedTimestamp) {
                    // Only clear if this is the first load and no logs exist
                    logDiv.innerHTML = '';
                }
            } catch (error) {
                console.error('Error fetching app logs:', error);
                log('Error fetching app logs: ' + error);
            }
        }
        
        async function clearAppLogs() {
            // First, fetch the current logs to get the most recent timestamp
            try {
                const response = await fetch(API_BASE + '/api/logs/recent');
                const data = await response.json();
                
                if (data.logs && data.logs.length > 0) {
                    // Get the timestamp from the most recent log entry
                    const mostRecentLog = data.logs[data.logs.length - 1];
                    clearTimestamp = mostRecentLog.timestamp;
                    console.log('Using most recent log timestamp as clear point: ' + clearTimestamp);
                } else {
                    // Fallback to current server time if no logs exist
                    try {
                        const timeResponse = await fetch(API_BASE + '/api/time');
                        const timeData = await timeResponse.json();
                        clearTimestamp = timeData.timestamp;
                        console.log('No logs found, using current server time: ' + clearTimestamp);
                    } catch (timeError) {
                        // Final fallback to local time
                        const now = new Date();
                        const year = now.getFullYear();
                        const month = String(now.getMonth() + 1).padStart(2, '0');
                        const day = String(now.getDate()).padStart(2, '0');
                        const hours = String(now.getHours()).padStart(2, '0');
                        const minutes = String(now.getMinutes()).padStart(2, '0');
                        const seconds = String(now.getSeconds()).padStart(2, '0');
                        const millis = String(now.getMilliseconds()).padStart(3, '0');
                        
                        clearTimestamp = `${'$'}{year}-${'$'}{month}-${'$'}{day} ${'$'}{hours}:${'$'}{minutes}:${'$'}{seconds}.${'$'}{millis}`;
                        console.log('No logs found, using current local time as fallback: ' + clearTimestamp);
                    }
                }
                
                // Clear the display immediately
                const logDiv = document.getElementById('log');
                logDiv.innerHTML = '';
                
                // Reset the last displayed timestamp to prevent showing old logs
                lastDisplayedTimestamp = clearTimestamp;
                
                // Clear the displayed entries set since we're starting fresh
                displayedLogEntries.clear();
                
            } catch (error) {
                console.error('Error fetching logs for clear timestamp:', error);
                // Clear anyway even if we couldn't get the timestamp
                const logDiv = document.getElementById('log');
                logDiv.innerHTML = '';
                
                // Reset tracking variables
                lastDisplayedTimestamp = null;
                clearTimestamp = null;
                displayedLogEntries.clear();
            }
        }
        
        // Auto-refresh setup for app logs
        document.addEventListener('DOMContentLoaded', function() {
            const autoRefreshCheckbox = document.getElementById('autoRefreshAppLogs');
            if (autoRefreshCheckbox) {
                autoRefreshCheckbox.addEventListener('change', function(e) {
                    if (e.target.checked) {
                        appLogRefreshInterval = setInterval(refreshAppLogs, 2000);
                        log('App log auto-refresh enabled');
                    } else {
                        if (appLogRefreshInterval) {
                            clearInterval(appLogRefreshInterval);
                            appLogRefreshInterval = null;
                        }
                        log('App log auto-refresh disabled');
                    }
                });
                
                // Start auto-refresh by default
                appLogRefreshInterval = setInterval(refreshAppLogs, 2000);
                // Load initial logs
                refreshAppLogs();
            }
        });
        
        async function checkReady() {
            try {
                const response = await fetch(API_BASE + '/api/app/ready');
                const data = await response.json();
                isReady = data.ready;
                document.getElementById('status').className = 'status ' + (isReady ? 'ready' : 'not-ready');
                if (isReady && !window.readyLogged) {
                    log('App is ready!');
                    window.readyLogged = true;
                    // Get initial camera state
                    updateCameraState();
                    // Load saved positions
                    refreshSavedPositions();
                    // Load spawned objects
                    refreshSpawnedObjects();
                }
            } catch (error) {
                log('Error checking app status: ' + error);
            }
        }
        
        async function apiCall(endpoint, method = 'GET', body = null) {
            if (!isReady && endpoint !== '/api/app/ready') {
                log('App not ready yet');
                return;
            }
            
            try {
                const options = {
                    method: method,
                    headers: {
                        'Content-Type': 'application/json'
                    }
                };
                
                if (body) {
                    options.body = JSON.stringify(body);
                }
                
                const response = await fetch(API_BASE + endpoint, options);
                const data = await response.json();
                
                if (response.ok) {
                    log('Success: ' + endpoint + ' - ' + (data.message || 'OK'));
                } else {
                    log('Error: ' + endpoint + ' - ' + (data.error || 'Failed'));
                }
                
                return data;
            } catch (error) {
                log('Error: ' + endpoint + ' - ' + error);
            }
        }
        
        async function rotateCamera(pitch, yaw, roll) {
            // Log before rotation
            const beforeState = await apiCall('/api/scene/info');
            if (beforeState && beforeState.camera) {
                log(`Before rotation: position=(${'$'}{beforeState.camera.position.x.toFixed(2)}, ${'$'}{beforeState.camera.position.y.toFixed(2)}, ${'$'}{beforeState.camera.position.z.toFixed(2)}), yaw=${'$'}{beforeState.camera.rotation.yaw.toFixed(2)}°`);
            }
            
            log(`Applying rotation: pitch=${'$'}{pitch}°, yaw=${'$'}{yaw}°, roll=${'$'}{roll}°`);
            
            await apiCall('/api/camera/rotate', 'POST', { pitch, yaw, roll });
            
            // Always get updated camera state from server after rotation
            await updateCameraState();
            
            // Log after rotation
            const afterState = await apiCall('/api/scene/info');
            if (afterState && afterState.camera) {
                log(`After rotation: position=(${'$'}{afterState.camera.position.x.toFixed(2)}, ${'$'}{afterState.camera.position.y.toFixed(2)}, ${'$'}{afterState.camera.position.z.toFixed(2)}), yaw=${'$'}{afterState.camera.rotation.yaw.toFixed(2)}°`);
            }
        }
        
        async function testRotation(pitch, yaw, roll) {
            log('🧪 TESTING ROTATION: pitch=' + pitch + '°, yaw=' + yaw + '°, roll=' + roll + '° - What does this do?');
            
            try {
                const response = await apiCall('/api/camera/test-rotation', 'POST', { pitch, yaw, roll });
                if (response) {
                    log('✅ Test completed: ' + (response.message || 'Success'));
                    await updateCameraState();
                } else {
                    log('❌ Test failed');
                }
            } catch (error) {
                log('❌ Test error: ' + error);
            }
        }
        
        async function updateCameraState() {
            try {
                const sceneInfo = await apiCall('/api/scene/info');
                if (sceneInfo && sceneInfo.camera && sceneInfo.camera.rotation) {
                    // Always use server state as the source of truth
                    currentCameraYaw = sceneInfo.camera.rotation.yaw;
                    // No need to track totalYaw separately - server handles rotation accumulation
                }
            } catch (error) {
                // If we can't get server state, keep current value
                log('Could not update camera state from server');
            }
        }
        
        async function resetRotation() {
            // Reset camera rotation by querying current state and rotating back to 0°
            try {
                const sceneInfo = await apiCall('/api/scene/info');
                if (sceneInfo && sceneInfo.camera && sceneInfo.camera.rotation) {
                    let currentYaw = sceneInfo.camera.rotation.yaw;
                    
                    // Normalize the current yaw to -180 to 180 range before calculating reset
                    currentYaw = currentYaw % 360;
                    if (currentYaw > 180) {
                        currentYaw -= 360;
                    } else if (currentYaw < -180) {
                        currentYaw += 360;
                    }
                    
                    if (Math.abs(currentYaw) > 0.1) { // Only rotate if not already close to 0°
                        log(`Resetting rotation from ${'$'}{currentYaw.toFixed(2)}° to 0°`);
                        
                        // Calculate the shortest rotation to get to 0
                        let resetRotation = -currentYaw;
                        
                        // Apply the rotation
                        await rotateCamera(0, resetRotation, 0);
                    } else {
                        log('Already at 0° rotation');
                    }
                } else {
                    log('Could not get current rotation state for reset');
                }
            } catch (error) {
                log('Error during rotation reset: ' + error);
            }
        }
        
        async function resetPosition() {
            // Reset player position to origin (but keep rotation)
            try {
                await apiCall('/api/camera/position', 'POST', { x: 0, y: 0, z: 0 });
                // Clear local position tracking since we reset to origin
                window.playerX = 0;
                window.playerY = 0;
                window.playerZ = 0;
                log('Position reset to origin (0, 0, 0)');
            } catch (error) {
                log('Error during position reset: ' + error);
            }
        }
        
        function movePlayer(deltaX, deltaY, deltaZ) {
            // Calculate movement relative to camera's current rotation (yaw)
            const yawRadians = currentCameraYaw * Math.PI / 180;
            
            // Calculate direction vectors based on camera's facing direction
            // Forward direction: where the camera is actually facing
            const forwardX = -Math.sin(yawRadians);
            const forwardZ = -Math.cos(yawRadians);
            
            // Right direction: 90° clockwise from forward direction
            const rightX = Math.cos(yawRadians);
            const rightZ = -Math.sin(yawRadians);
            
            // Apply movement: deltaX is strafe (left/right), deltaZ is forward/backward
            // deltaZ < 0 means forward movement, deltaZ > 0 means backward movement
            const rotatedX = deltaX * rightX + deltaZ * forwardX;
            const rotatedZ = deltaX * rightZ + deltaZ * forwardZ;
            
            // Track current player position
            window.playerX = (window.playerX || 0) + rotatedX;
            window.playerY = (window.playerY || 0) + deltaY;
            window.playerZ = (window.playerZ || 0) + rotatedZ;
            
            // Log the calculated direction vectors for debugging
            
            // Add more detailed logging
            const inputDir = deltaX > 0 ? 'RIGHT' : deltaX < 0 ? 'LEFT' : deltaZ < 0 ? 'FORWARD' : 'BACKWARD';
            log(`Moving player: yaw=${'$'}{currentCameraYaw.toFixed(1)}°, forward=(${'$'}{forwardX.toFixed(2)}, ${'$'}{forwardZ.toFixed(2)}), right=(${'$'}{rightX.toFixed(2)}, ${'$'}{rightZ.toFixed(2)})`);
            log(`Input: ${'$'}{inputDir} delta=(${'$'}{deltaX}, ${'$'}{deltaZ}) -> world=(${'$'}{rotatedX.toFixed(2)}, ${'$'}{rotatedZ.toFixed(2)})`);
            log(`New position will be: (${'$'}{window.playerX.toFixed(2)}, ${'$'}{window.playerY.toFixed(2)}, ${'$'}{window.playerZ.toFixed(2)})`);
            
            apiCall('/api/camera/position', 'POST', { 
                x: window.playerX, 
                y: window.playerY, 
                z: window.playerZ 
            });
        }
        
        async function logCameraPosition() {
            const state = await apiCall('/api/scene/info');
            if (state && state.camera) {
                const cam = state.camera;
                log(`=== Camera State ===`);
                log(`Position: x=${'$'}{cam.position.x.toFixed(3)}, y=${'$'}{cam.position.y.toFixed(3)}, z=${'$'}{cam.position.z.toFixed(3)}`);
                log(`Rotation: pitch=${'$'}{cam.rotation.pitch.toFixed(2)}°, yaw=${'$'}{cam.rotation.yaw.toFixed(2)}°, roll=${'$'}{cam.rotation.roll.toFixed(2)}°`);
                log(`Tracked yaw: ${'$'}{window.totalYaw?.toFixed(2) || '0.00'}°, Current yaw: ${'$'}{currentCameraYaw.toFixed(2)}°`);
                log(`===================`);
            } else {
                log('Failed to get camera state');
            }
        }
        
        // Camera Save/Load Functions
        
        async function saveCurrentPosition() {
            const nameInput = document.getElementById('savePositionName');
            const name = nameInput.value.trim();
            
            if (!name) {
                log('Please enter a name for the position');
                return;
            }
            
            try {
                const response = await apiCall('/api/camera/save', 'POST', { name: name });
                if (response) {
                    log(`Position saved as: ${'$'}{name}`);
                    nameInput.value = '';
                    await refreshSavedPositions();
                }
            } catch (error) {
                log('Error saving position: ' + error);
            }
        }
        
        // New table-based position functions
        async function loadPosition(name) {
            try {
                const response = await apiCall(`/api/camera/load/${'$'}{name}`, 'POST');
                if (response) {
                    log(`Position loaded: ${'$'}{name}`);
                    
                    // Get the loaded position and update our internal tracking
                    const savedPositions = await apiCall('/api/camera/saved');
                    if (savedPositions && savedPositions.positions && savedPositions.positions[name]) {
                        const loadedPos = savedPositions.positions[name];
                        currentCameraPosition = {
                            x: loadedPos.position.x,
                            y: loadedPos.position.y,
                            z: loadedPos.position.z
                        };
                        currentCameraRotation = {
                            pitch: loadedPos.rotation.pitch,
                            yaw: loadedPos.rotation.yaw,
                            roll: loadedPos.rotation.roll
                        };
                        log('Internal tracking updated: pos=(' + currentCameraPosition.x.toFixed(3) + ', ' + currentCameraPosition.y.toFixed(3) + ', ' + currentCameraPosition.z.toFixed(3) + '), rot=(' + currentCameraRotation.pitch.toFixed(1) + '°, ' + currentCameraRotation.yaw.toFixed(1) + '°, ' + currentCameraRotation.roll.toFixed(1) + '°)');
                    }
                    
                    // Update camera state after loading
                    await updateCameraState();
                }
            } catch (error) {
                log('Error loading position: ' + error);
            }
        }
        
        async function deletePosition(name) {
            try {
                const response = await apiCall('/api/camera/saved/' + name, 'DELETE');
                if (response) {
                    log('Position deleted: ' + name);
                    await refreshSavedPositions();
                }
            } catch (error) {
                log('Error deleting position: ' + error);
            }
        }
        
        async function setPositionAsDefault(name) {
            try {
                const response = await apiCall('/api/camera/start-position', 'POST', { name: name });
                if (response) {
                    log(`Start position set to: ${'$'}{name}`);
                    await refreshSavedPositions(); // Refresh to show star indicator
                }
            } catch (error) {
                log('Error setting start position: ' + error);
            }
        }
        
        async function refreshSavedPositions() {
            try {
                const response = await apiCall('/api/camera/saved', 'GET');
                const tableBody = document.getElementById('savedPositionsTableBody');
                
                // Clear existing rows
                tableBody.innerHTML = '';
                
                if (response && response.positions) {
                    // Sort positions by name
                    const sortedPositions = Object.entries(response.positions)
                        .sort(([a], [b]) => a.localeCompare(b));
                    
                    if (sortedPositions.length === 0) {
                        // Show "no positions" message
                        const row = document.createElement('tr');
                        row.innerHTML = `
                            <td colspan="4" style="border: 1px solid #ddd; padding: 8px; text-align: center; font-style: italic; color: #666;">
                                No saved positions found
                            </td>
                        `;
                        tableBody.appendChild(row);
                    } else {
                        sortedPositions.forEach(([name, state]) => {
                            const row = document.createElement('tr');
                            const isStartPosition = response.startPosition === name;
                            const startIndicator = isStartPosition ? ' ⭐' : '';
                            
                            row.innerHTML = 
                                '<td style="border: 1px solid #ddd; padding: 8px;">' + name + startIndicator + '</td>' +
                                '<td style="border: 1px solid #ddd; padding: 8px;">' +
                                    'x: ' + state.position.x.toFixed(2) + '<br>' +
                                    'y: ' + state.position.y.toFixed(2) + '<br>' +
                                    'z: ' + state.position.z.toFixed(2) +
                                '</td>' +
                                '<td style="border: 1px solid #ddd; padding: 8px;">' +
                                    'pitch: ' + state.rotation.pitch.toFixed(1) + '°<br>' +
                                    'yaw: ' + state.rotation.yaw.toFixed(1) + '°<br>' +
                                    'roll: ' + state.rotation.roll.toFixed(1) + '°' +
                                '</td>' +
                                '<td style="border: 1px solid #ddd; padding: 4px; text-align: center;">' +
                                    '<button onclick="loadPosition(\'' + name + '\')" style="margin: 2px; padding: 4px 8px; font-size: 12px;">Load</button><br>' +
                                    '<button onclick="setPositionAsDefault(\'' + name + '\')" style="margin: 2px; padding: 4px 8px; font-size: 12px; background-color: #ff9800; color: white;">Set Default</button><br>' +
                                    '<button onclick="deletePosition(\'' + name + '\')" style="margin: 2px; padding: 4px 8px; font-size: 12px; background-color: #f44336; color: white;">✕</button>' +
                                '</td>';
                            tableBody.appendChild(row);
                        });
                    }
                    
                    log('Loaded ' + sortedPositions.length + ' saved positions');
                    
                    // Update start position label
                    if (response.startPosition) {
                        document.getElementById('startPositionLabel').textContent = 'Start position: ' + response.startPosition;
                    } else {
                        document.getElementById('startPositionLabel').textContent = 'No start position set';
                    }
                } else {
                    // Show "no positions" message
                    const row = document.createElement('tr');
                    row.innerHTML = `
                        <td colspan="4" style="border: 1px solid #ddd; padding: 8px; text-align: center; font-style: italic; color: #666;">
                            No saved positions found
                        </td>
                    `;
                    tableBody.appendChild(row);
                    log('No saved positions found');
                    document.getElementById('startPositionLabel').textContent = 'No start position set';
                }
            } catch (error) {
                log('Error loading saved positions: ' + error);
            }
        }
        
        // Start Position Functions
        
        async function clearStartPosition() {
            if (!confirm('Are you sure you want to clear the start position?')) {
                return;
            }
            
            try {
                const response = await apiCall('/api/camera/start-position', 'DELETE');
                if (response) {
                    log('Start position cleared');
                    document.getElementById('startPositionLabel').textContent = 'No start position set';
                    await refreshSavedPositions(); // Refresh to remove star indicators
                }
            } catch (error) {
                log('Error clearing start position: ' + error);
            }
        }

        async function unlockCamera() {
            try {
                const response = await apiCall('/api/camera/unlock', 'POST');
                if (response) {
                    log('Camera unlocked - VR tracking restored');
                }
            } catch (error) {
                log('Error unlocking camera: ' + error);
            }
        }
        
        async function resetCameraPosition() {
            try {
                const response = await apiCall('/api/camera/position', 'POST', { x: 0, y: 0, z: 0 });
                if (response) {
                    log('Camera position reset to origin (0, 0, 0)');
                    await updateCameraState();
                }
            } catch (error) {
                log('Error resetting camera position: ' + error);
            }
        }
        
        async function resetCameraRotation() {
            try {
                // Get current rotation to calculate reset values
                const state = await apiCall('/api/scene/info');
                if (state && state.camera) {
                    const currentPitch = state.camera.rotation.pitch;
                    const currentYaw = state.camera.rotation.yaw;
                    const currentRoll = state.camera.rotation.roll;
                    
                    // Set rotation to zero by rotating by negative current values
                    const response = await apiCall('/api/camera/rotate', 'POST', { 
                        pitch: -currentPitch, 
                        yaw: -currentYaw, 
                        roll: -currentRoll 
                    });
                    if (response) {
                        log('Camera rotation reset to (0°, 0°, 0°)');
                        await updateCameraState();
                    }
                } else {
                    log('Could not get current rotation for reset');
                }
            } catch (error) {
                log('Error resetting camera rotation: ' + error);
            }
        }
        
        async function adjustPosition(deltaX, deltaY, deltaZ) {
            try {
                // Use internally tracked position as base
                const newX = currentCameraPosition.x + deltaX;
                const newY = currentCameraPosition.y + deltaY;
                const newZ = currentCameraPosition.z + deltaZ;
                
                // Use direct position API
                const posResponse = await apiCall('/api/camera/position', 'POST', {
                    x: newX,
                    y: newY, 
                    z: newZ
                });
                
                if (posResponse) {
                    // Update internal tracking
                    currentCameraPosition.x = newX;
                    currentCameraPosition.y = newY;
                    currentCameraPosition.z = newZ;
                    
                    log('Position adjusted by (' + deltaX + ', ' + deltaY + ', ' + deltaZ + ') from (' + (newX-deltaX).toFixed(3) + ', ' + (newY-deltaY).toFixed(3) + ', ' + (newZ-deltaZ).toFixed(3) + ') to (' + newX.toFixed(3) + ', ' + newY.toFixed(3) + ', ' + newZ.toFixed(3) + ')');
                    await updateCameraState();
                } else {
                    log('Failed to adjust camera position');
                }
            } catch (error) {
                log('Error adjusting position: ' + error);
            }
        }
        
        async function adjustRotation(deltaPitch, deltaYaw, deltaRoll) {
            try {
                // Update internal tracking first
                currentCameraRotation.pitch += deltaPitch;
                currentCameraRotation.yaw += deltaYaw;
                currentCameraRotation.roll += deltaRoll;
                
                // Use the existing rotation API which adds to current rotation
                const response = await apiCall('/api/camera/rotate', 'POST', { pitch: deltaPitch, yaw: deltaYaw, roll: deltaRoll });
                if (response) {
                    log('Rotation adjusted by (pitch=' + deltaPitch + '°, yaw=' + deltaYaw + '°, roll=' + deltaRoll + '°) to (pitch=' + currentCameraRotation.pitch.toFixed(1) + '°, yaw=' + currentCameraRotation.yaw.toFixed(1) + '°, roll=' + currentCameraRotation.roll.toFixed(1) + '°)');
                    await updateCameraState();
                } else {
                    // Revert internal tracking if API call failed
                    currentCameraRotation.pitch -= deltaPitch;
                    currentCameraRotation.yaw -= deltaYaw;
                    currentCameraRotation.roll -= deltaRoll;
                    log('Failed to adjust camera rotation');
                }
            } catch (error) {
                log('Error adjusting rotation: ' + error);
            }
        }
        
        // Object Spawning Functions
        
        async function spawnRedCube() {
            await spawnObject('box', {red: 1, green: 0, blue: 0, alpha: 1}, {width: 1, height: 1, depth: 1});
        }
        
        async function spawnBlueSphere() {
            await spawnObject('sphere', {red: 0, green: 0, blue: 1, alpha: 1}, {radius: 0.5});
        }
        
        async function spawnGreenPlane() {
            await spawnObject('plane', {red: 0, green: 1, blue: 0, alpha: 1}, {width: 2, depth: 2});
        }
        
        async function spawnRandomObject() {
            const types = ['box', 'sphere', 'plane'];
            const colors = [
                {red: 1, green: 0, blue: 0, alpha: 1}, // Red
                {red: 0, green: 1, blue: 0, alpha: 1}, // Green
                {red: 0, green: 0, blue: 1, alpha: 1}, // Blue
                {red: 1, green: 1, blue: 0, alpha: 1}, // Yellow
                {red: 1, green: 0, blue: 1, alpha: 1}, // Magenta
                {red: 0, green: 1, blue: 1, alpha: 1}  // Cyan
            ];
            
            const type = types[Math.floor(Math.random() * types.length)];
            const color = colors[Math.floor(Math.random() * colors.length)];
            
            let size;
            switch (type) {
                case 'box':
                    size = {width: 0.5 + Math.random() * 1.5, height: 0.5 + Math.random() * 1.5, depth: 0.5 + Math.random() * 1.5};
                    break;
                case 'sphere':
                    size = {radius: 0.3 + Math.random() * 0.7};
                    break;
                case 'plane':
                    size = {width: 1 + Math.random() * 2, depth: 1 + Math.random() * 2};
                    break;
            }
            
            await spawnObject(type, color, size);
        }
        
        async function spawnObject(type, material, size) {
            if (!isReady) {
                log('App not ready yet');
                return;
            }
            
            try {
                // Get current camera position for spawning near player
                const sceneInfo = await apiCall('/api/scene/info');
                let spawnPos = {x: 0, y: 1, z: -2}; // Default spawn position
                
                if (sceneInfo && sceneInfo.camera) {
                    // Spawn 2-3 meters in front of camera with some randomization
                    const cam = sceneInfo.camera;
                    const randomX = (Math.random() - 0.5) * 2; // -1 to 1 meter
                    const randomZ = -2 - Math.random() * 2; // 2-4 meters forward
                    const randomY = Math.random() * 0.5; // 0-0.5 meters up
                    
                    spawnPos = {
                        x: cam.position.x + randomX,
                        y: cam.position.y + randomY + 1, // Add 1m base height
                        z: cam.position.z + randomZ
                    };
                }
                
                const request = {
                    type: type,
                    position: spawnPos,
                    size: size,
                    material: material
                };
                
                log(`Spawning ${'$'}{type} at (${'$'}{spawnPos.x.toFixed(2)}, ${'$'}{spawnPos.y.toFixed(2)}, ${'$'}{spawnPos.z.toFixed(2)})`);
                
                const response = await apiCall('/api/objects/spawn', 'POST', request);
                if (response && response.success) {
                    log(`${'$'}{type} spawned successfully with ID: ${'$'}{response.entityId}`);
                    refreshSpawnedObjects();
                }
            } catch (error) {
                log('Error spawning object: ' + error);
            }
        }
        
        async function deleteSelectedObject() {
            const select = document.getElementById('spawnedObjectsList');
            const entityId = select.value;
            
            if (!entityId) {
                log('Please select an object to delete');
                return;
            }
            
            try {
                const response = await apiCall(`/api/objects/${'$'}{entityId}`, 'DELETE');
                if (response) {
                    log(`Object ${'$'}{entityId} deleted`);
                    refreshSpawnedObjects();
                }
            } catch (error) {
                log('Error deleting object: ' + error);
            }
        }
        
        async function refreshSpawnedObjects() {
            try {
                const response = await apiCall('/api/objects/list', 'GET');
                const select = document.getElementById('spawnedObjectsList');
                
                // Clear existing options except the first one
                while (select.children.length > 1) {
                    select.removeChild(select.lastChild);
                }
                
                if (response && response.objects) {
                    response.objects.forEach(obj => {
                        const option = document.createElement('option');
                        option.value = obj.id;
                        option.textContent = `${'$'}{obj.name} (x:${'$'}{obj.position.x.toFixed(1)}, y:${'$'}{obj.position.y.toFixed(1)}, z:${'$'}{obj.position.z.toFixed(1)})`;
                        select.appendChild(option);
                    });
                    
                    log(`Found ${'$'}{response.objects.length} spawned objects`);
                } else {
                    log('No spawned objects found');
                }
            } catch (error) {
                log('Error loading spawned objects: ' + error);
            }
        }
        
        // App-specific action functions can be added here
        ${extensionRegistry.getAllWebUIJavaScript()}
        
        // Load app-specific content
        async function loadAppSpecificContent() {
            try {
                const response = await fetch(API_BASE + '/api/app/ui-content');
                if (response.ok) {
                    const data = await response.json();
                    if (data.content) {
                        document.getElementById('app-specific-content').innerHTML = data.content;
                    }
                }
            } catch (error) {
                // Ignore errors - app-specific content is optional
            }
        }
        
        // Check ready status every second
        // Log file management functions
        async function refreshLogs() {
            try {
                const response = await fetch(API_BASE + '/api/logs');
                const data = await response.json();
                
                const logFilesDiv = document.getElementById('logFiles');
                logFilesDiv.innerHTML = '';
                
                if (data.files && data.files.length > 0) {
                    const list = document.createElement('ul');
                    data.files.forEach(file => {
                        const item = document.createElement('li');
                        const sizeKB = (file.size / 1024).toFixed(2);
                        const date = new Date(file.lastModified).toLocaleString();
                        item.textContent = file.name + ' (' + sizeKB + ' KB) - Modified: ' + date;
                        list.appendChild(item);
                    });
                    logFilesDiv.appendChild(list);
                } else {
                    logFilesDiv.innerHTML = '<p>No log files found</p>';
                }
                
                log('Refreshed log files');
            } catch (error) {
                log('Error refreshing logs: ' + error);
            }
        }
        
        async function downloadLogs() {
            try {
                window.location.href = API_BASE + '/api/logs/download';
                log('Downloading log file...');
            } catch (error) {
                log('Error downloading logs: ' + error);
            }
        }
        
        async function clearLogs() {
            if (!confirm('Are you sure you want to clear all log files?')) {
                return;
            }
            
            try {
                const response = await fetch(API_BASE + '/api/logs/clear', { method: 'POST' });
                const data = await response.json();
                log('Logs cleared: ' + data.message);
                refreshLogs();
            } catch (error) {
                log('Error clearing logs: ' + error);
            }
        }
        
        setInterval(checkReady, 1000);
        checkReady();
        
        // Load app-specific content when ready
        setTimeout(loadAppSpecificContent, 1000);
        
        // Load log files on startup
        refreshLogs();
        
        log('VR Debug Control loaded');
        
        // Extension JavaScript
        ${extensionRegistry.getAllWebUIJavaScript()}
    </script>
</body>
</html>
        """.trimIndent()
        
        return newFixedLengthResponse(Response.Status.OK, MIME_HTML, html)
    }
    
    // Helper Functions
    
    private inline fun <reified T> parseJsonBody(session: IHTTPSession): T? {
        return try {
            val files = HashMap<String, String>()
            session.parseBody(files)
            val json = files["postData"]
            
            if (json.isNullOrEmpty()) {
                FileLogger.w(TAG, "Empty request body")
                return null
            }
            
            gson.fromJson(json, T::class.java)
        } catch (e: IOException) {
            FileLogger.e(TAG, "Failed to parse body", e)
            null
        } catch (e: JsonSyntaxException) {
            FileLogger.e(TAG, "Invalid JSON", e)
            null
        }
    }
    
    private fun createJsonResponse(data: Any): Response {
        val json = gson.toJson(data)
        return newFixedLengthResponse(Response.Status.OK, MIME_JSON, json)
    }
    
    private fun createErrorResponse(status: Response.Status, message: String): Response {
        val error = ErrorResponse(error = message)
        val json = gson.toJson(error)
        return newFixedLengthResponse(status, MIME_JSON, json)
    }
}