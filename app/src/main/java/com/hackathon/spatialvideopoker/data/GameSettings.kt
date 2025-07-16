package com.hackathon.spatialvideopoker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

data class GameSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val musicVolume: Float = 0.5f,
    val autoHold: Boolean = false,
    val gameSpeed: GameSpeed = GameSpeed.NORMAL
) {
    enum class GameSpeed(val displayName: String, val delayMultiplier: Float) {
        SLOW("Slow", 1.5f),
        NORMAL("Normal", 1.0f),
        FAST("Fast", 0.5f)
    }
}

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_SOUND_VOLUME = "sound_volume"
        private const val KEY_MUSIC_VOLUME = "music_volume"
        private const val KEY_AUTO_HOLD = "auto_hold"
        private const val KEY_GAME_SPEED = "game_speed"
    }
    
    fun loadSettings(): GameSettings {
        return GameSettings(
            soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            musicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true),
            soundVolume = prefs.getFloat(KEY_SOUND_VOLUME, 1.0f),
            musicVolume = prefs.getFloat(KEY_MUSIC_VOLUME, 0.5f),
            autoHold = prefs.getBoolean(KEY_AUTO_HOLD, false),
            gameSpeed = GameSettings.GameSpeed.valueOf(
                prefs.getString(KEY_GAME_SPEED, GameSettings.GameSpeed.NORMAL.name) 
                    ?: GameSettings.GameSpeed.NORMAL.name
            )
        )
    }
    
    fun saveSettings(settings: GameSettings) {
        prefs.edit {
            putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            putBoolean(KEY_MUSIC_ENABLED, settings.musicEnabled)
            putFloat(KEY_SOUND_VOLUME, settings.soundVolume)
            putFloat(KEY_MUSIC_VOLUME, settings.musicVolume)
            putBoolean(KEY_AUTO_HOLD, settings.autoHold)
            putString(KEY_GAME_SPEED, settings.gameSpeed.name)
        }
    }
}