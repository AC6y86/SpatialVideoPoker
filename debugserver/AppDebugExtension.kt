package vr.debugserver

import fi.iki.elonen.NanoHTTPD

/**
 * Interface for app-specific debug server extensions.
 * Allows apps to register custom endpoints and functionality without modifying the core debug server.
 */
interface AppDebugExtension {
    
    /**
     * The namespace for this extension's endpoints (e.g., "gallery", "game", etc.)
     * All endpoints will be prefixed with /api/app/{namespace}/
     */
    val namespace: String
    
    /**
     * Initialize the extension with access to the debug system components
     */
    fun initialize(debugSystem: VRDebugSystem)
    
    /**
     * Handle a request for this extension's namespace
     * @param uri The URI path after /api/app/{namespace}/
     * @param method The HTTP method
     * @param session The HTTP session for accessing request data
     * @return The HTTP response, or null if the endpoint is not handled by this extension
     */
    fun handleRequest(uri: String, method: NanoHTTPD.Method, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response?
    
    /**
     * Get app-specific status information for the main debug UI
     * @return A map of status information to display, or null if no status to show
     */
    fun getAppStatus(): Map<String, Any>?
    
    /**
     * Get HTML content for app-specific controls in the web UI
     * @return HTML string for custom controls, or null if no custom UI
     */
    fun getWebUIContent(): String?
    
    /**
     * Get JavaScript content for app-specific functionality in the web UI
     * @return JavaScript string for custom functionality, or null if no custom JS
     */
    fun getWebUIJavaScript(): String?
    
    /**
     * Clean up resources when the extension is being destroyed
     */
    fun cleanup()
}