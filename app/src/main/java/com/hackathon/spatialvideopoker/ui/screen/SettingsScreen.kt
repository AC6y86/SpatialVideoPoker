package com.hackathon.spatialvideopoker.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hackathon.spatialvideopoker.data.GameSettings

@Composable
fun SettingsScreen(
    currentSettings: GameSettings,
    onSettingsChanged: (GameSettings) -> Unit,
    onDismiss: () -> Unit
) {
    var settings by remember { mutableStateOf(currentSettings) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1B5E20)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETTINGS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Yellow
                    )
                    
                    Button(
                        onClick = {
                            onSettingsChanged(settings)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.8f)
                        )
                    ) {
                        Text("SAVE & CLOSE", color = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Settings content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Sound Settings
                    SettingsSection(title = "AUDIO") {
                        // Sound Effects Toggle
                        SettingRow(
                            label = "Sound Effects",
                            content = {
                                Switch(
                                    checked = settings.soundEnabled,
                                    onCheckedChange = { 
                                        settings = settings.copy(soundEnabled = it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Yellow,
                                        checkedTrackColor = Color.Yellow.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        )
                        
                        // Sound Volume
                        if (settings.soundEnabled) {
                            VolumeSlider(
                                label = "Sound Volume",
                                value = settings.soundVolume,
                                onValueChange = { 
                                    settings = settings.copy(soundVolume = it)
                                }
                            )
                        }
                        
                        // Background Music Toggle
                        SettingRow(
                            label = "Background Music",
                            content = {
                                Switch(
                                    checked = settings.musicEnabled,
                                    onCheckedChange = { 
                                        settings = settings.copy(musicEnabled = it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Yellow,
                                        checkedTrackColor = Color.Yellow.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        )
                        
                        // Music Volume
                        if (settings.musicEnabled) {
                            VolumeSlider(
                                label = "Music Volume",
                                value = settings.musicVolume,
                                onValueChange = { 
                                    settings = settings.copy(musicVolume = it)
                                }
                            )
                        }
                    }
                    
                    // Game Settings
                    SettingsSection(title = "GAMEPLAY") {
                        // Auto Hold
                        SettingRow(
                            label = "Auto Hold",
                            description = "Automatically hold winning cards",
                            content = {
                                Switch(
                                    checked = settings.autoHold,
                                    onCheckedChange = { 
                                        settings = settings.copy(autoHold = it)
                                    },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.Yellow,
                                        checkedTrackColor = Color.Yellow.copy(alpha = 0.5f)
                                    )
                                )
                            }
                        )
                        
                        // Game Speed
                        SettingRow(
                            label = "Game Speed",
                            content = {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    GameSettings.GameSpeed.values().forEach { speed ->
                                        FilterChip(
                                            selected = settings.gameSpeed == speed,
                                            onClick = { 
                                                settings = settings.copy(gameSpeed = speed)
                                            },
                                            label = { Text(speed.displayName) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color.Yellow,
                                                selectedLabelColor = Color.Black
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Yellow,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    description: String? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.White
            )
            description?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        content()
    }
}

@Composable
private fun VolumeSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                text = "${(value * 100).toInt()}%",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = Color.Yellow,
                activeTrackColor = Color.Yellow,
                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            )
        )
    }
}