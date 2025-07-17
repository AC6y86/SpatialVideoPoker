package vr.debugserver

import fi.iki.elonen.NanoHTTPD

/**
 * Interface for app-specific debug server extensions.
 * Allows apps to register custom endpoints and functionality without modifying the core debug server.
 */
interface AppDebugExtension {
    
    /**
     * The namespace for this extension's endpoints (e.g., "poker", "game", etc.)
     * All endpoints will be prefixed with /api/app/{namespace}/
     */
    val namespace: String
    
    /**
     * Display name for this extension shown in the web UI
     */
    val displayName: String
    
    /**
     * Version of this extension
     */
    val version: String
    
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
     * Get CSS styles for app-specific web UI components
     * @return CSS string for custom styles, or null if no custom CSS
     */
    fun getWebUIStyles(): String?
    
    /**
     * Get API documentation for this extension's endpoints
     * @return HTML documentation string, or null if no documentation
     */
    fun getApiDocumentation(): String?
    
    /**
     * Clean up resources when the extension is being destroyed
     */
    fun cleanup()
}