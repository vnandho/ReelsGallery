package com.reelsgallery.player.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.reelsgallery.player.data.GalleryFilter
import com.reelsgallery.player.data.LocalMedia
import com.reelsgallery.player.data.MediaRepository
import com.reelsgallery.player.data.MediaSort
import com.reelsgallery.player.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReelsGalleryApp(activity: androidx.activity.ComponentActivity) {
    val settings = remember { SettingsStore(activity) }
    var appearance by remember { mutableStateOf(settings.appearance) }
    ReelsGalleryTheme(appearance) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        var media by remember { mutableStateOf(emptyList<LocalMedia>()) }
        var hasAccess by remember { mutableStateOf(MediaRepository.hasAnyMediaAccess(context)) }
        var refreshToken by remember { mutableIntStateOf(0) }
        var showSettings by remember { mutableStateOf(false) }
        var selectedMedia by remember { mutableStateOf<LocalMedia?>(null) }
        var queue by remember { mutableStateOf(emptyList<LocalMedia>()) }
        var queueStart by remember { mutableIntStateOf(0) }

        val permissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { results ->
            hasAccess = results.values.any { it } || MediaRepository.hasAnyMediaAccess(context)
            refreshToken++
        }

        LaunchedEffect(refreshToken, hasAccess) {
            if (hasAccess) {
                media = withContext(Dispatchers.IO) { runCatching { MediaRepository.query(context) }.getOrDefault(emptyList()) }
            }
        }

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    val accessNow = MediaRepository.hasAnyMediaAccess(context)
                    hasAccess = accessNow
                    if (accessNow) refreshToken++
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        BackHandler(enabled = selectedMedia != null || showSettings) {
            when {
                selectedMedia != null -> selectedMedia = null
                showSettings -> { showSettings = false; refreshToken++ }
            }
        }

        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            if (!hasAccess) {
                PermissionScreen(
                    onRequest = { permissionLauncher.launch(MediaRepository.requestedPermissions()) }
                )
            } else {
                GalleryScreen(
                    media = media,
                    settings = settings,
                    refreshToken = refreshToken,
                    onRefresh = { refreshToken++ },
                    onOpenSettings = { showSettings = true },
                    onOpenMedia = { item, orderedQueue ->
                        if (item.isVideo) {
                            queue = orderedQueue.ifEmpty { listOf(item) }
                            queueStart = queue.indexOfFirst { it.id == item.id }.coerceAtLeast(0)
                        }
                        selectedMedia = item
                    }
                )
            }

            if (showSettings && selectedMedia == null) {
                SettingsScreen(settings = settings, onBack = { showSettings = false; refreshToken++ }, onAppearanceChange = { appearance = it })
            }

            selectedMedia?.let { item ->
                if (item.isVideo) {
                    PlayerScreen(
                        queue = queue,
                        startIndex = queueStart,
                        settings = settings,
                        activity = activity,
                        onClose = { selectedMedia = null }
                    )
                } else {
                    PhotoViewer(item = item, onClose = { selectedMedia = null })
                }
            }
        }
    }
}

@Composable
private fun PermissionScreen(onRequest: () -> Unit) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Text("Your media, in one place", style = MaterialTheme.typography.headlineSmall)
        androidx.compose.foundation.layout.Spacer(Modifier.height(10.dp))
        androidx.compose.material3.Text(
            "Allow access to the photos and videos you choose. Your files stay on this device.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(20.dp))
        androidx.compose.material3.Button(onClick = onRequest) { androidx.compose.material3.Text("Grant media access") }
    }
}
