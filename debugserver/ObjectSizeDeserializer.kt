package vr.debugserver

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

/**
 * Custom deserializer for ObjectSize sealed class
 * Determines the concrete type based on the fields present in the JSON
 */
class ObjectSizeDeserializer : JsonDeserializer<ObjectSize> {
    
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ObjectSize {
        val jsonObject = json.asJsonObject
        
        return when {
            // Check for BoxSize fields (width, height, depth)
            jsonObject.has("width") && jsonObject.has("height") && jsonObject.has("depth") -> {
                BoxSize(
                    width = jsonObject.get("width").asFloat,
                    height = jsonObject.get("height").asFloat,
                    depth = jsonObject.get("depth").asFloat
                )
            }
            
            // Check for SphereSize field (radius)
            jsonObject.has("radius") -> {
                SphereSize(
                    radius = jsonObject.get("radius").asFloat
                )
            }
            
            // Check for PlaneSize fields (width, depth)
            jsonObject.has("width") && jsonObject.has("depth") && !jsonObject.has("height") -> {
                PlaneSize(
                    width = jsonObject.get("width").asFloat,
                    depth = jsonObject.get("depth").asFloat
                )
            }
            
            else -> throw JsonParseException("Unable to determine ObjectSize type from JSON: $json")
        }
    }
}