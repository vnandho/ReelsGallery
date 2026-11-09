package com.reelsgallery.player.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import com.reelsgallery.player.data.SettingsStore

@Composable
fun SettingsScreen(settings: SettingsStore, onBack: () -> Unit, onAppearanceChange: (String) -> Unit) {
    BackHandler(onBack = onBack)
    var appearance by remember { mutableStateOf(settings.appearance) }
    var columns by remember { mutableIntStateOf(settings.gridColumns) }
    var jump by remember { mutableIntStateOf(settings.jumpSeconds) }
    var doubleTapSeek by remember { mutableIntStateOf(settings.doubleTapSeekSeconds) }
    var autoHide by remember { mutableIntStateOf(settings.autoHideSeconds) }
    var iconSize by remember { mutableIntStateOf(settings.iconSize) }
    var queueScope by remember { mutableStateOf(settings.queueScope) }
    var brightness by remember { mutableStateOf(settings.brightnessGesture) }
    var volume by remember { mutableStateOf(settings.volumeGesture) }
    var reverse by remember { mutableStateOf(settings.reverseGestures) }
    var zone by remember { mutableIntStateOf(settings.sideZonePercent) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface).padding(horizontal = 20.dp, vertical = 18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("‹  Back") }
            Spacer(Modifier.weight(1f))
            Text("Settings", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = {
                settings.reset()
                appearance = settings.appearance
                onAppearanceChange(appearance)
                columns = settings.gridColumns
                jump = settings.jumpSeconds
                doubleTapSeek = settings.doubleTapSeekSeconds
                autoHide = settings.autoHideSeconds
                iconSize = settings.iconSize
                queueScope = settings.queueScope
                brightness = settings.brightnessGesture
                volume = settings.volumeGesture
                reverse = settings.reverseGestures
                zone = settings.sideZonePercent
            }) { Text("Reset") }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(15.dp)) {
            Spacer(Modifier.height(4.dp))
            Text("APPEARANCE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("DARK" to "Dark", "LIGHT" to "Light", "SYSTEM" to "System").forEach { (value, label) ->
                    ChoiceButton(label, appearance == value) {
                        appearance = value
                        settings.appearance = value
                        onAppearanceChange(value)
                    }
                }
            }

            Text("GALLERY", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            SettingSlider("Grid columns", "$columns columns", columns.toFloat(), 2f, 5f) {
                columns = it.toInt().coerceIn(2, 5); settings.gridColumns = columns
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Text("PLAYBACK", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Text("Queue scope", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChoiceButton("Current folder", queueScope == "FOLDER") { queueScope = "FOLDER"; settings.queueScope = queueScope }
                ChoiceButton("All videos", queueScope == "ALL") { queueScope = "ALL"; settings.queueScope = queueScope }
            }
            Text("Double-tap seek interval", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(1, 2, 5, 10).forEach { value ->
                    ChoiceButton("${value}s", doubleTapSeek == value) { doubleTapSeek = value; settings.doubleTapSeekSeconds = value }
                }
            }
            Text("Double-tap left or right side of the video to jump backward or forward.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            Text("Seek interval", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(5, 10, 15, 30, 60).forEach { value ->
                    ChoiceButton("${value}s", jump == value) { jump = value; settings.jumpSeconds = value }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Text("PLAYER CONTROLS", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            SettingSlider("Global icon size", "$iconSize dp", iconSize.toFloat(), 18f, 56f) {
                iconSize = it.toInt().coerceIn(18, 56); settings.iconSize = iconSize
            }
            Text("Control auto-hide", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                listOf(0, 3, 5, 10).forEach { value ->
                    ChoiceButton(if (value == 0) "Off" else "${value}s", autoHide == value) {
                        autoHide = value; settings.autoHideSeconds = value
                    }
                }
            }
            Text("The player overlay hides after the selected delay. A tap on empty video space only shows or hides controls; it never toggles playback.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Text("GESTURES", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            SettingSwitch("Left-side brightness", "Drag up to increase brightness.", brightness) {
                brightness = it; settings.brightnessGesture = it
            }
            SettingSwitch("Right-side volume", "Drag up to increase media volume.", volume) {
                volume = it; settings.volumeGesture = it
            }
            SettingSwitch("Reverse gesture directions", "Invert the vertical adjustment direction.", reverse) {
                reverse = it; settings.reverseGestures = it
            }
            SettingSlider("Side gesture zone", "$zone% each side", zone.toFloat(), 20f, 40f) {
                zone = it.toInt().coerceIn(20, 40); settings.sideZonePercent = zone
            }
            Text("The remaining center area handles next/previous video swipes. Timeline and on-screen controls take priority over gestures.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            Text("PRIVACY", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Text("Reels Gallery reads media through Android's media access permissions. Files, thumbnails, playback positions and preferences are processed locally. The app does not request internet access and includes no ads or analytics.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(20.dp))
        }
        Text("Made with passion by @vnandho", modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Done") }
    }
}

@Composable
private fun SettingSlider(title: String, valueLabel: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Text(valueLabel, color = MaterialTheme.colorScheme.primary)
        }
        Slider(value = value, onValueChange = onChange, valueRange = min..max)
    }
}

@Composable
private fun SettingSwitch(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ChoiceButton(label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 7.dp)
    ) { Text(label, style = MaterialTheme.typography.labelMedium) }
}
