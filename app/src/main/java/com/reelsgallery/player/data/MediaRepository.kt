
package com.reelsgallery.player.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import androidx.core.content.ContextCompat

object MediaRepository {

    private val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.MIME_TYPE,
        MediaStore.MediaColumns.SIZE,
        MediaStore.MediaColumns.DATE_ADDED,
        MediaStore.MediaColumns.RELATIVE_PATH,
        MediaStore.Video.VideoColumns.DURATION,
        MediaStore.Files.FileColumns.MEDIA_TYPE
    )

    // Memilih izin sesuai versi Android
    fun requestedPermissions(): Array<String> {
        return when {
            Build.VERSION.SDK_INT >= 34 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
            )

            Build.VERSION.SDK_INT >= 33 -> arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )

            else -> arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    // Memeriksa apakah aplikasi memiliki akses media
    fun hasAnyMediaAccess(context: Context): Boolean {
        return when {
            Build.VERSION.SDK_INT >= 34 -> {
                val image = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

                val video = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED

                val selected = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED

                image || video || selected
            }

            Build.VERSION.SDK_INT >= 33 -> {
                val image = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

                val video = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED

                image || video
            }

            else -> {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            }
        }
    }

    fun hasFullImageAccess(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasFullVideoAccess(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun query(context: Context): List<LocalMedia> {
        val resolver = context.contentResolver
        val mediaUri = MediaStore.Files.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        )

        val selection =
            "${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR " +
            "${MediaStore.Files.FileColumns.MEDIA_TYPE}=?"

        val args = arrayOf(
            MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString(),
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString()
        )

        val output = ArrayList<LocalMedia>()

        resolver.query(
            mediaUri,
            projection,
            selection,
            args,
            "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        )?.use { cursor ->

            val idCol = cursor.getColumnIndexOrThrow(
                MediaStore.MediaColumns._ID
            )

            val nameCol = cursor.getColumnIndexOrThrow(
                MediaStore.MediaColumns.DISPLAY_NAME
            )

            val mimeCol = cursor.getColumnIndexOrThrow(
                MediaStore.MediaColumns.MIME_TYPE
            )

            val sizeCol = cursor.getColumnIndexOrThrow(
                MediaStore.MediaColumns.SIZE
            )

            val dateCol = cursor.getColumnIndexOrThrow(
                MediaStore.MediaColumns.DATE_ADDED
            )

            val pathCol = cursor.getColumnIndex(
                MediaStore.MediaColumns.RELATIVE_PATH
            )

            val durationCol = cursor.getColumnIndex(
                MediaStore.Video.VideoColumns.DURATION
            )

            val typeCol = cursor.getColumnIndexOrThrow(
                MediaStore.Files.FileColumns.MEDIA_TYPE
            )

            while (cursor.moveToNext()) {
                val type = cursor.getInt(typeCol)

                val isVideo =
                    type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO

                val id = cursor.getLong(idCol)

                val base = if (isVideo) {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val itemUri = ContentUris.withAppendedId(base, id)

                output += LocalMedia(
                    id = id,
                    uri = itemUri,
                    displayName = cursor.getString(nameCol) ?: "Untitled",
                    mimeType = cursor.getString(mimeCol)
                        ?: if (isVideo) "video/*" else "image/*",
                    sizeBytes = if (cursor.isNull(sizeCol)) {
                        0L
                    } else {
                        cursor.getLong(sizeCol)
                    },
                    dateAddedSeconds = if (cursor.isNull(dateCol)) {
                        0L
                    } else {
                        cursor.getLong(dateCol)
                    },
                    relativePath = if (
                        pathCol >= 0 && !cursor.isNull(pathCol)
                    ) {
                        cursor.getString(pathCol).orEmpty()
                    } else {
                        ""
                    },
                    durationMs = if (
                        isVideo && durationCol >= 0 && !cursor.isNull(durationCol)
                    ) {
                        cursor.getLong(durationCol)
                    } else {
                        0L
                    },
                    isVideo = isVideo
                )
            }
        }

        return output
    }

    fun thumbnail(
        context: Context,
        uri: android.net.Uri,
        edgePx: Int = 420
    ): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= 29) {
            context.contentResolver.loadThumbnail(
                uri,
                Size(edgePx, edgePx),
                null
            )
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }
}
