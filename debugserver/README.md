# VR Debug Server

A reusable HTTP debug server for Meta Spatial SDK applications that enables remote control of VR input through a REST API.

## Features

- 🎮 Remote camera and controller control
- 🔘 Button press simulation
- 🌐 REST API with web UI
- 📦 Zero-dependency integration (just NanoHTTPD)
- 🔧 Modular design - easy to port to any project

## Quick Start

1. Copy this `debugserver` folder to your project root
2. Add NanoHTTPD dependency and configure source sets (see INTEGRATION.md)
3. Add 4 lines to your VR activity
4. Build and run!

## Files

- `VRDebugServer.kt` - HTTP server and API endpoints
- `VRInputSimulator.kt` - Input simulation logic
- `VRDebugSystem.kt` - Spatial SDK integration
- `DebugModels.kt` - Data models for API
- `DebugServerConfig.kt` - Configuration
- `INTEGRATION.md` - Detailed integration guide
- `test_debug_server.py` - Example Python client

## API Examples

```bash
# Wait for app ready
curl http://quest-ip:8080/api/app/ready

# Rotate camera 90 degrees
curl -X POST http://quest-ip:8080/api/camera/rotate \
  -H "Content-Type: application/json" \
  -d '{"yaw": 90}'

# Simulate trigger press
curl -X POST http://quest-ip:8080/api/input/trigger \
  -H "Content-Type: application/json" \
  -d '{"controller": "right", "action": "press"}'
```

## Web UI

Access `http://quest-ip:8080/` for a visual control interface.

## Future: Module Conversion

This folder is designed to be easily converted to a proper Gradle module:

1. Add a `build.gradle.kts` file
2. Move to a separate repository
3. Publish to Maven/JitPack
4. Include as a dependency

## License

Use freely in your Meta Spatial SDK projects!