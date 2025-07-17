package vr.debugserver

import com.meta.spatial.debugserver.utils.FileLogger
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
        
        // Validate extension
        if (!isValidNamespace(namespace)) {
            FileLogger.e(TAG, "Invalid namespace '$namespace': must contain only lowercase letters, numbers, and hyphens")
            return
        }
        
        if (extensions.containsKey(namespace)) {
            FileLogger.w(TAG, "Extension with namespace '$namespace' already registered, replacing")
        }
        
        extensions[namespace] = extension
        
        // Initialize the extension if debug system is ready
        debugSystem?.let { 
            try {
                extension.initialize(it)
                FileLogger.d(TAG, "Registered extension: ${extension.displayName} v${extension.version} (namespace: $namespace)")
            } catch (e: Exception) {
                FileLogger.e(TAG, "Failed to initialize extension '$namespace'", e)
                extensions.remove(namespace)
            }
        }
    }
    
    /**
     * Validate namespace format
     */
    private fun isValidNamespace(namespace: String): Boolean {
        return namespace.matches(Regex("^[a-z0-9-]+$")) && namespace.isNotEmpty()
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
     * Get extension metadata
     */
    fun getExtensionMetadata(): Map<String, Map<String, String>> {
        val metadata = mutableMapOf<String, Map<String, String>>()
        
        extensions.forEach { (namespace, extension) ->
            metadata[namespace] = mapOf(
                "displayName" to extension.displayName,
                "version" to extension.version,
                "namespace" to extension.namespace
            )
        }
        
        return metadata
    }
    
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
     * Get web UI CSS from all registered extensions
     */
    fun getAllWebUIStyles(): String {
        val css = StringBuilder()
        
        extensions.forEach { (namespace, extension) ->
            try {
                extension.getWebUIStyles()?.let { cssContent ->
                    css.append("/* Extension: $namespace */\n")
                    css.append(cssContent)
                    css.append("\n")
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Error getting web UI styles from extension '$namespace'", e)
            }
        }
        
        return css.toString()
    }
    
    /**
     * Get API documentation from all registered extensions
     */
    fun getAllApiDocumentation(): String {
        val docs = StringBuilder()
        
        extensions.forEach { (namespace, extension) ->
            try {
                extension.getApiDocumentation()?.let { docContent ->
                    docs.append("<h4>Extension: ${extension.displayName} (${extension.namespace})</h4>\n")
                    docs.append(docContent)
                    docs.append("\n")
                }
            } catch (e: Exception) {
                FileLogger.e(TAG, "Error getting API documentation from extension '$namespace'", e)
            }
        }
        
        return docs.toString()
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