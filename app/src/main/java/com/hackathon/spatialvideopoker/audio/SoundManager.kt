package com.hackathon.spatialvideopoker.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.annotation.RawRes
import com.hackathon.spatialvideopoker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoundManager(private val context: Context) {
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<SoundEffect, Int>()
    private var mediaPlayer: MediaPlayer? = null
    
    private var soundEnabled = true
    private var musicEnabled = true
    private var soundVolume = 1.0f
    private var musicVolume = 0.5f
    
    enum class SoundEffect {
        CARD_FLIP,
        CARD_DEAL,
        BUTTON_CLICK,
        WIN_SMALL,
        WIN_BIG,
        COIN_INSERT,
        CASH_OUT
    }
    
    init {
        initializeSoundPool()
    }
    
    private fun initializeSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
            
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
        
        // Load sound effects (we'll add the actual resource files later)
        // soundIds[SoundEffect.CARD_FLIP] = soundPool.load(context, R.raw.card_flip, 1)
        // soundIds[SoundEffect.CARD_DEAL] = soundPool.load(context, R.raw.card_deal, 1)
        // soundIds[SoundEffect.BUTTON_CLICK] = soundPool.load(context, R.raw.button_click, 1)
        // soundIds[SoundEffect.WIN_SMALL] = soundPool.load(context, R.raw.win_small, 1)
        // soundIds[SoundEffect.WIN_BIG] = soundPool.load(context, R.raw.win_big, 1)
        // soundIds[SoundEffect.COIN_INSERT] = soundPool.load(context, R.raw.coin_insert, 1)
        // soundIds[SoundEffect.CASH_OUT] = soundPool.load(context, R.raw.cash_out, 1)
    }
    
    fun playSound(effect: SoundEffect) {
        if (!soundEnabled) return
        
        soundIds[effect]?.let { soundId ->
            soundPool?.play(soundId, soundVolume, soundVolume, 1, 0, 1.0f)
        }
    }
    
    fun startBackgroundMusic(@RawRes musicResId: Int? = null) {
        if (!musicEnabled) return
        
        stopBackgroundMusic()
        
        try {
            mediaPlayer = MediaPlayer().apply {
                // Use a default music resource if provided
                // setDataSource(context, Uri.parse("android.resource://${context.packageName}/${musicResId ?: R.raw.background_music}"))
                isLooping = true
                setVolume(musicVolume, musicVolume)
                prepareAsync()
                setOnPreparedListener { start() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stopBackgroundMusic() {
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }
    
    fun setMusicEnabled(enabled: Boolean) {
        musicEnabled = enabled
        if (!enabled) {
            stopBackgroundMusic()
        } else {
            startBackgroundMusic()
        }
    }
    
    fun setSoundVolume(volume: Float) {
        soundVolume = volume.coerceIn(0f, 1f)
    }
    
    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(musicVolume, musicVolume)
    }
    
    fun release() {
        soundPool?.release()
        soundPool = null
        stopBackgroundMusic()
        soundIds.clear()
    }
}