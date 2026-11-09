package com.reelsgallery.player.data

import android.net.Uri

data class LocalMedia(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val dateAddedSeconds: Long,
    val relativePath: String,
    val durationMs: Long,
    val isVideo: Boolean
) {
    val folderLabel: String
        get() = relativePath.trimEnd('/').substringAfterLast('/').ifBlank { "Other" }
}

enum class GalleryFilter { ALL, VIDEOS, PHOTOS }
enum class MediaSort { NEWEST, OLDEST, NAME, SIZE, DURATION }
