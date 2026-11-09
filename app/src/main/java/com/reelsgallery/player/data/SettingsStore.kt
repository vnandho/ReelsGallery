package com.reelsgallery.player.data

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("reels_gallery_settings", Context.MODE_PRIVATE)

    var appearance: String
        get() = prefs.getString("appearance", "DARK") ?: "DARK"
        set(value) = prefs.edit().putString("appearance", value.takeIf { it in setOf("DARK", "LIGHT", "SYSTEM") } ?: "DARK").apply()

    var gridColumns: Int
        get() = prefs.getInt("grid_columns", 3).coerceIn(2, 5)
        set(value) = prefs.edit().putInt("grid_columns", value.coerceIn(2, 5)).apply()

    var jumpSeconds: Int
        get() = prefs.getInt("jump_seconds", 10).let { if (it in listOf(5, 10, 15, 30, 60)) it else 10 }
        set(value) = prefs.edit().putInt("jump_seconds", value).apply()

    var playerMuted: Boolean
        get() = prefs.getBoolean("player_muted", false)
        set(value) = prefs.edit().putBoolean("player_muted", value).apply()

    var repeatMode: Int
        get() = prefs.getInt("player_repeat_mode", 0).coerceIn(0, 2)
        set(value) = prefs.edit().putInt("player_repeat_mode", value.coerceIn(0, 2)).apply()

    var doubleTapSeekSeconds: Int
        get() = prefs.getInt("double_tap_seek_seconds", 5).let { if (it in listOf(1, 2, 5, 10)) it else 5 }
        set(value) = prefs.edit().putInt("double_tap_seek_seconds", value.takeIf { it in listOf(1, 2, 5, 10) } ?: 5).apply()

    var autoHideSeconds: Int
        get() = prefs.getInt("auto_hide_seconds", 3).coerceIn(0, 10)
        set(value) = prefs.edit().putInt("auto_hide_seconds", value.coerceIn(0, 10)).apply()

    var iconSize: Int
        get() = prefs.getInt("icon_size", 30).coerceIn(18, 56)
        set(value) = prefs.edit().putInt("icon_size", value.coerceIn(18, 56)).apply()

    var queueScope: String
        get() = prefs.getString("queue_scope", "FOLDER") ?: "FOLDER"
        set(value) = prefs.edit().putString("queue_scope", if (value == "ALL") "ALL" else "FOLDER").apply()

    var brightnessGesture: Boolean
        get() = prefs.getBoolean("brightness_gesture", true)
        set(value) = prefs.edit().putBoolean("brightness_gesture", value).apply()

    var volumeGesture: Boolean
        get() = prefs.getBoolean("volume_gesture", true)
        set(value) = prefs.edit().putBoolean("volume_gesture", value).apply()

    var reverseGestures: Boolean
        get() = prefs.getBoolean("reverse_gestures", false)
        set(value) = prefs.edit().putBoolean("reverse_gestures", value).apply()

    var sideZonePercent: Int
        get() = prefs.getInt("side_zone_percent", 28).coerceIn(20, 40)
        set(value) = prefs.edit().putInt("side_zone_percent", value.coerceIn(20, 40)).apply()

    fun savePosition(uri: String, positionMs: Long) {
        prefs.edit().putLong("resume_${uri.hashCode()}", positionMs.coerceAtLeast(0L)).apply()
    }

    fun position(uri: String): Long = prefs.getLong("resume_${uri.hashCode()}", 0L)

    fun reset() = prefs.edit().clear().apply()
}
