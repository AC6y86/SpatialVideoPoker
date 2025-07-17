package vr.debugserver

/**
 * Configuration holder for the VR Debug Server
 */
object DebugServerConfiguration {
    private var config = DebugServerConfig()
    
    fun getConfig(): DebugServerConfig = config
    
    fun updateConfig(block: DebugServerConfig.() -> Unit) {
        config = config.copy().apply(block)
    }
    
    fun reset() {
        config = DebugServerConfig()
    }
}