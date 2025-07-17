package vr.debugserver

import com.gallery.artbrowser.utils.FileLogger
import fi.iki.elonen.NanoHTTPD

/**
 * Registry for managing app-specific debug server extensions
 */
class ExtensionRegistry {
    
    companion object {
        private const val TAG = "ExtensionRegistry"
    }
    
    private val extensions = mutableMapOf<String, AppDebugExtension>()
    private var debugSystem: VRDebugSystem? = null
    
    /**
     * Initialize the registry with the debug system
     */
    fun initialize(debugSystem: VRDebugSystem) {
        this.debugSystem = debugSystem
        FileLogger.d(TAG, "Extension registry initialized")
    }
    
    /**
     * Register an app-specific extension
     */
    fun registerExtension(extension: AppDebugExtension) {
        val namespace = extension.namespace
        
        if (extensions.containsKey(namespace)) {
            FileLogger.w(TAG, "Extension with namespace '$namespace' already registered, replacing")
        }
        
        extensions[namespace] = extension
        
        // Initialize the extension if debug system is ready
        debugSystem?.let { extension.initialize(it) }
        
        FileLogger.d(TAG, "Registered extension: $namespace")
    }
    
    /**
     * Unregister an extension
     */
    fun unregisterExtension(namespace: String) {
        extensions[namespace]?.let { extension ->
            extension.cleanup()
            extensions.remove(namespace)
            FileLogger.d(TAG, "Unregistered extension: $namespace")
        }
    }
    
    /**
     * Route a request to the appropriate extension
     * @param uri The full URI path starting with /api/app/
     * @param method The HTTP method
     * @param session The HTTP session
     * @return The response from the extension, or null if no extension handles it
     */
    fun handleExtensionRequest(uri: String, method: NanoHTTPD.Method, session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response? {
        // Extract namespace from URI: /api/app/{namespace}/{rest}
        val pathParts = uri.removePrefix("/api/app/").split("/")
        if (pathParts.isEmpty()) {
            return null
        }
        
        val namespace = pathParts[0]
        val extensionUri = pathParts.drop(1).joinToString("/")
        
        val extension = extensions[namespace]
        if (extension == null) {
            FileLogger.w(TAG, "No extension found for namespace: $namespace")
            return null
        }
        
        FileLogger.d(TAG, "Routing request to extension '$namespace': $extensionUri")
        
        return try {
            extension.handleRequest(extensionUri, method, session)
        } catch (e: Exception) {
            FileLogger.e(TAG, "Error handling request in extension '$namespace'", e)
            null
        }
    }
    
    /**
     * Get all registered extension namespaces
     */
    fun getRegisteredNamespaces(): Set<String> = extensions.keys.toSet()
    
    /**
     * Get app status from all registered extensions
     */
    fun getAllAppStatus(): Map<String, Map<String, Any>> {
        val status = mutableMapOf<String, Map<String, Any>>()
        
        extensions.forEach { (namespace, extension) ->
            try {
                extension.getAppStatus()?.let { appStatus ->
                    status[namespace] = appStatus
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Error getting status from extension '$namespace'", e)
            }
        }
        
        return status
    }
    
    /**
     * Get web UI content from all registered extensions
     */
    fun getAllWebUIContent(): String {
        val content = StringBuilder()
        
        extensions.forEach { (namespace, extension) ->
            try {
                extension.getWebUIContent()?.let { uiContent ->
                    content.append("<!-- Extension: $namespace -->\n")
                    content.append(uiContent)
                    content.append("\n")
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Error getting web UI content from extension '$namespace'", e)
            }
        }
        
        return content.toString()
    }
    
    /**
     * Get web UI JavaScript from all registered extensions
     */
    fun getAllWebUIJavaScript(): String {
        val js = StringBuilder()
        
        extensions.forEach { (namespace, extension) ->
            try {
                extension.getWebUIJavaScript()?.let { jsContent ->
                    js.append("// Extension: $namespace\n")
                    js.append(jsContent)
                    js.append("\n")
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Error getting web UI JavaScript from extension '$namespace'", e)
            }
        }
        
        return js.toString()
    }
    
    /**
     * Clean up all registered extensions
     */
    fun cleanup() {
        extensions.values.forEach { extension ->
            try {
                extension.cleanup()
            } catch (e: Exception) {
                FileLogger.e(TAG, "Error cleaning up extension", e)
            }
        }
        extensions.clear()
        FileLogger.d(TAG, "Extension registry cleaned up")
    }
}