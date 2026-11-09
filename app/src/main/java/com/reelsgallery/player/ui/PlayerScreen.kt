package com.reelsgallery.player.ui

import android.content.Context
import android.content.ContentValues
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.media.AudioManager
import android.app.PictureInPictureParams
import android.os.Build
import android.util.Rational
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TrackSelectionDialogBuilder
import com.reelsgallery.player.data.LocalMedia
import com.reelsgallery.player.data.SettingsStore
import com.reelsgallery.player.player.GestureZone
import com.reelsgallery.player.player.PlayerGestureClassifier
import com.reelsgallery.player.player.SwipeAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    queue: List<LocalMedia>,
    startIndex: Int,
    settings: SettingsStore,
    activity: androidx.activity.ComponentActivity,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val window = activity.window
    val safeQueue = remember(queue) { queue.ifEmpty { emptyList() } }
    if (safeQueue.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            TextButton(onClick = onClose) { Text("Close player") }
        }
        return
    }

    val urisKey = remember(safeQueue) { safeQueue.joinToString("|") { it.uri.toString() } }
    val exoPlayer = remember(urisKey) { ExoPlayer.Builder(context).build() }
    var isPlaying by remember { mutableStateOf(true) }
    var muted by remember { mutableStateOf(false) }
    var lastCenterTap by remember { mutableLongStateOf(0L) }
    var controlsVisible by remember { mutableStateOf(true) }
    var repeatMode by remember { mutableIntStateOf(settings.repeatMode) }
    var pendingSeekMs by remember { mutableLongStateOf(0L) }
    val scope = rememberCoroutineScope()
    var verticalDragOffset by remember { mutableFloatStateOf(0f) }
    var adjacentPreview by remember { mutableStateOf<Bitmap?>(null) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var seekValue by remember { mutableFloatStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }
    var rotation by remember { mutableIntStateOf(0) }
    var speed by remember { mutableFloatStateOf(1f) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var fullscreen by remember { mutableStateOf(false) }
    var interactionId by remember { mutableIntStateOf(0) }
    var overlayMessage by remember { mutableStateOf<String?>(null) }
    var brightnessIndicator by remember { mutableStateOf<String?>(null) }
    var speedMenu by remember { mutableStateOf(false) }
    var aspectMenu by remember { mutableStateOf(false) }
    var moreMenu by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val originalScreenBrightness = remember(window) { window.attributes.screenBrightness }

    LaunchedEffect(exoPlayer.currentMediaItemIndex, urisKey) {
        adjacentPreview = withContext(Dispatchers.IO) {
            val index = exoPlayer.currentMediaItemIndex
            val next = safeQueue.getOrNull(index + 1) ?: safeQueue.getOrNull(index - 1)
            next?.let { extractPreviewFrame(context, it.uri) }
        }
    }

    BackHandler(onBack = onClose)

    // Keep Android status/navigation bars visible. Player controls use safeDrawing insets,
    // so menus and buttons never sit underneath the system bars.
    DisposableEffect(window) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            val attrs = window.attributes
            attrs.screenBrightness = originalScreenBrightness
            window.attributes = attrs
        }
    }

    DisposableEffect(exoPlayer, urisKey) {
        val mediaItems = safeQueue.map { Local -> MediaItem.fromUri(Local.uri) }
        val safeIndex = startIndex.coerceIn(0, mediaItems.lastIndex)
        val resumeAt = settings.position(safeQueue[safeIndex].uri.toString())
        exoPlayer.setMediaItems(mediaItems, safeIndex, resumeAt)
        exoPlayer.repeatMode = when (settings.repeatMode) { 1 -> Player.REPEAT_MODE_ONE; 2 -> Player.REPEAT_MODE_ALL; else -> Player.REPEAT_MODE_OFF }
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(playbackState: Int) {
                durationMs = exoPlayer.duration.takeIf { it > 0L } ?: 0L
                if (playbackState == Player.STATE_READY) errorMessage = null
            }
            override fun onPlayerError(error: PlaybackException) {
                errorMessage = "Can't play this file: ${error.errorCodeName}"
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            val index = exoPlayer.currentMediaItemIndex.coerceIn(0, safeQueue.lastIndex)
            settings.savePosition(safeQueue[index].uri.toString(), exoPlayer.currentPosition)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(exoPlayer, isSeeking) {
        var lastSavedIndex = -1
        var lastSavedPosition = 0L
        while (true) {
            if (!isSeeking) {
                val currentIndex = exoPlayer.currentMediaItemIndex
                positionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
                durationMs = exoPlayer.duration.takeIf { it > 0L } ?: 0L
                seekValue = positionMs.toFloat()
                if (currentIndex in safeQueue.indices &&
                    (currentIndex != lastSavedIndex || kotlin.math.abs(positionMs - lastSavedPosition) >= 2000L)
                ) {
                    settings.savePosition(safeQueue[currentIndex].uri.toString(), positionMs)
                    lastSavedIndex = currentIndex
                    lastSavedPosition = positionMs
                }
            }
            delay(350)
        }
    }

    LaunchedEffect(controlsVisible, interactionId, isSeeking, settings.autoHideSeconds) {
        if (controlsVisible && !isSeeking && settings.autoHideSeconds > 0) {
            delay(settings.autoHideSeconds * 1000L)
            controlsVisible = false
        }
    }

    LaunchedEffect(overlayMessage) {
        if (overlayMessage != null) {
            delay(1200)
            overlayMessage = null
        }
    }

    LaunchedEffect(brightnessIndicator) {
        if (brightnessIndicator != null) {
            delay(900)
            brightnessIndicator = null
        }
    }

    val subtitlePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
            val index = exoPlayer.currentMediaItemIndex.coerceIn(0, safeQueue.lastIndex)
            val currentPosition = exoPlayer.currentPosition
            val items = safeQueue.mapIndexed { itemIndex, media ->
                val builder = MediaItem.Builder().setUri(media.uri)
                if (itemIndex == index) {
                    val name = runCatching {
                        context.contentResolver.query(uri, arrayOf(android.provider.OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                            if (it.moveToFirst()) it.getString(0).orEmpty() else ""
                        }.orEmpty()
                    }.getOrDefault("")
                    val mime = if (name.endsWith(".ass", true) || name.endsWith(".ssa", true)) "text/x-ssa" else MimeTypes.APPLICATION_SUBRIP
                    val config = MediaItem.SubtitleConfiguration.Builder(uri)
                        .setMimeType(mime)
                        .setLabel(name.ifBlank { "External subtitle" })
                        .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                        .build()
                    builder.setSubtitleConfigurations(listOf(config))
                }
                builder.build()
            }
            exoPlayer.setMediaItems(items, index, currentPosition)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            overlayMessage = "External subtitle added"
        }
    }

    fun touchInteraction() {
        controlsVisible = true
        interactionId++
    }
    fun navigateNext() {
        if (exoPlayer.hasNextMediaItem()) { exoPlayer.seekToNextMediaItem(); exoPlayer.playWhenReady = true; overlayMessage = "Next video" }
        else overlayMessage = "End of queue"
        touchInteraction()
    }
    fun navigatePrevious() {
        if (exoPlayer.hasPreviousMediaItem()) { exoPlayer.seekToPreviousMediaItem(); exoPlayer.playWhenReady = true; overlayMessage = "Previous video" }
        else overlayMessage = "Start of queue"
        touchInteraction()
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        val pageHeight = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp
        if (adjacentPreview != null) {
            Image(
                bitmap = adjacentPreview!!.asImageBitmap(),
                contentDescription = "Adjacent video preview",
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    translationY = pageHeight.toPx() * (if (verticalDragOffset <= 0f) 1f else -1f) + verticalOffset.value
                }
            )
        }
        AndroidView(
            modifier = Modifier.fillMaxSize().graphicsLayer { translationY = verticalOffset.value },
            factory = { ctx ->
                PlayerView(ctx).apply {
                    useController = false
                    controllerAutoShow = false
                    this.resizeMode = resizeMode
                    setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    keepScreenOn = true
                    this.player = exoPlayer
                }
            },
            update = { view ->
                view.player = exoPlayer
                view.resizeMode = resizeMode
                view.rotation = rotation.toFloat()
            }
        )

        Box(
            Modifier.fillMaxSize().pointerInput(
                urisKey,
                settings.sideZonePercent,
                settings.brightnessGesture,
                settings.volumeGesture,
                settings.reverseGestures
            ) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = true)
                    val start = down.position
                    val zone = PlayerGestureClassifier.zoneOf(start.x, size.width.toFloat(), settings.sideZonePercent / 100f)
                    val startBrightness = window.attributes.screenBrightness.let { if (it < 0f) 0.5f else it }
                    val startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                    var moved = false
                    var navigated = false
                    var horizontalHandled = false
                var verticalHandled = false
                    val seekStart = exoPlayer.currentPosition
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        val dx = change.position.x - start.x
                        val dy = change.position.y - start.y
                        val isVertical = abs(dy) > viewConfiguration.touchSlop && abs(dy) > abs(dx) * 1.3f
                        if (abs(dx) > viewConfiguration.touchSlop || abs(dy) > viewConfiguration.touchSlop) moved = true
                        if (change.pressed && zone == GestureZone.CENTER && !isVertical && abs(dx) > viewConfiguration.touchSlop && abs(dx) > abs(dy) * 1.3f  ) {
                            val delta = (dx / size.width.toFloat() * (durationMs.coerceAtLeast(1L) * 0.5f)).toLong()
                            val target = (seekStart + delta).coerceIn(0L, durationMs.coerceAtLeast(1L))
                            pendingSeekMs = target
                            positionMs = target
                            overlayMessage = formatDuration(target)
                            horizontalHandled = true
                            touchInteraction()
                            change.consume()
                        }
                        if (change.pressed && isVertical) {
                            when (zone) {
                                GestureZone.LEFT -> if (settings.brightnessGesture) {
                                    val adjustment = if (settings.reverseGestures) dy else -dy
                                    val value = (startBrightness + adjustment / size.height.toFloat() * 1.25f).coerceIn(0.02f, 1f)
                                    val attrs = window.attributes
                                    attrs.screenBrightness = value
                                    window.attributes = attrs
                                    brightnessIndicator = "☼  ${(value * 100).roundToInt()}%"
                                    change.consume()
                                }
                                GestureZone.RIGHT -> if (settings.volumeGesture) {
                                    val travel = if (settings.reverseGestures) dy else -dy
                                    val delta = (travel / size.height.toFloat() * maxVolume * 1.5f).roundToInt()
                                    val value = (startVolume + delta).coerceIn(0, maxVolume)
                                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
                                    brightnessIndicator = "◖  ${(value * 100 / maxVolume)}%"
                                    change.consume()
                                }
                                GestureZone.CENTER -> {
                                    val action = PlayerGestureClassifier.classifySwipe(dx, dy, viewConfiguration.touchSlop)
                                    if (action != SwipeAction.NONE && abs(dy) > viewConfiguration.touchSlop) {
                                        verticalHandled = true
                                        verticalDragOffset = dy.coerceIn(-size.height.toFloat(), size.height.toFloat())
                                        change.consume()
                                    }
                                }
                            }
                        }
                        if (!change.pressed) {
                            if (horizontalHandled) exoPlayer.seekTo(pendingSeekMs.coerceIn(0L, durationMs.coerceAtLeast(1L)))
                            if (verticalHandled) {
                                val distance = verticalDragOffset
                                if (abs(distance) > size.height * 0.18f) {
                                    scope.launch {
                                        verticalDragOffset = if (distance < 0f) -size.height.toFloat() else size.height.toFloat()
                                        kotlinx.coroutines.delay(170)
                                        if (distance < 0f) navigateNext() else navigatePrevious()
                                        verticalDragOffset = 0f
                                    }
                                } else {
                                    scope.launch { verticalDragOffset = 0f }
                                }
                            }
                            if (!moved) {
                                val now = android.os.SystemClock.elapsedRealtime()
                                if (now - lastCenterTap < 320L) {
                                    when (zone) {
                                        GestureZone.LEFT -> {
                                            val target = (exoPlayer.currentPosition - settings.doubleTapSeekSeconds * 1000L).coerceAtLeast(0L)
                                            exoPlayer.seekTo(target); positionMs = target
                                            overlayMessage = "-${settings.doubleTapSeekSeconds}s"
                                        }
                                        GestureZone.RIGHT -> {
                                            val target = (exoPlayer.currentPosition + settings.doubleTapSeekSeconds * 1000L).coerceAtMost(durationMs.coerceAtLeast(1L))
                                            exoPlayer.seekTo(target); positionMs = target
                                            overlayMessage = "+${settings.doubleTapSeekSeconds}s"
                                        }
                                        GestureZone.CENTER -> {
                                            if (exoPlayer.isPlaying) { exoPlayer.pause(); overlayMessage = "Paused" }
                                            else { exoPlayer.play(); overlayMessage = "Playing" }
                                        }
                                    }
                                    lastCenterTap = 0L
                                } else {
                                    lastCenterTap = now
                                    if (zone == GestureZone.CENTER) { controlsVisible = !controlsVisible; interactionId++ }
                                }
                            }
                            break
                        }
                    }
                }
            }
        )

        if (controlsVisible) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    // Top frame: back, filename and overflow settings.
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onClose) {
                            Text("‹", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                        }
                        Column(Modifier.weight(1f).padding(start = 4.dp)) {
                            Text(
                                safeQueue.getOrNull(exoPlayer.currentMediaItemIndex)?.displayName ?: "Video",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1
                            )
                            Text(
                                "${exoPlayer.currentMediaItemIndex + 1} / ${safeQueue.size}",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Box {
                            TextButton(onClick = { moreMenu = true }) {
                                Text("⋮", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                            }
                            DropdownMenu(expanded = moreMenu, onDismissRequest = { moreMenu = false }) {
                                DropdownMenuItem(text = { Text("Audio track") }, onClick = {
                                    moreMenu = false
                                    runCatching { TrackSelectionDialogBuilder(context, "Audio track", exoPlayer, C.TRACK_TYPE_AUDIO).build().show() }
                                        .onFailure { overlayMessage = "Audio track selection unavailable" }
                                })
                                DropdownMenuItem(text = { Text("Embedded subtitles") }, onClick = {
                                    moreMenu = false
                                    runCatching { TrackSelectionDialogBuilder(context, "Subtitles", exoPlayer, C.TRACK_TYPE_TEXT).build().show() }
                                        .onFailure { overlayMessage = "Subtitle selection unavailable" }
                                })
                                DropdownMenuItem(text = { Text("Open external subtitle") }, onClick = {
                                    moreMenu = false
                                    subtitlePicker.launch(arrayOf("application/x-subrip", "text/x-ssa", "text/plain", "application/octet-stream"))
                                })
                                DropdownMenuItem(text = { Text("Playback speed: ${speed}×") }, onClick = { moreMenu = false; speedMenu = true })
                                DropdownMenuItem(text = { Text("Aspect ratio") }, onClick = { moreMenu = false; aspectMenu = true })
                            }
                        }
                    }
                    // Utility row directly below the header, as requested.
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { muted = !muted; exoPlayer.volume = if (muted) 0f else 1f; touchInteraction() }, modifier = Modifier.size(42.dp)) {
                            Text(if (muted) "◖̸" else "◖", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }
                        TextButton(onClick = { rotation = (rotation + 90) % 360; touchInteraction() }) {
                            Text("↻", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }
                        TextButton(onClick = {
                            repeatMode = when (repeatMode) {
                                Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
                                Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
                                else -> Player.REPEAT_MODE_OFF
                            }
                            exoPlayer.repeatMode = repeatMode
                            settings.repeatMode = when (repeatMode) { Player.REPEAT_MODE_ONE -> 1; Player.REPEAT_MODE_ALL -> 2; else -> 0 }
                            touchInteraction()
                        }) {
                            Text(if (repeatMode == Player.REPEAT_MODE_OFF) "⟲" else if (repeatMode == Player.REPEAT_MODE_ONE) "↻¹" else "↻", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }
                        TextButton(onClick = {
                                fullscreen = !fullscreen
                                val controller = WindowCompat.getInsetsController(window, window.decorView)
                                if (fullscreen) controller.hide(WindowInsetsCompat.Type.systemBars()) else controller.show(WindowInsetsCompat.Type.systemBars())
                                touchInteraction()
                            }) {
                            Text(if (fullscreen) "⛶" else "⤢", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }

                // Bottom: real-time elapsed, seek timeline, full duration, then transport.
                Column(
                    Modifier.fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.48f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = Color(0xFFFF8A80), modifier = Modifier.padding(bottom = 4.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(formatDuration(positionMs), color = Color.White, style = MaterialTheme.typography.labelSmall)
                        Slider(
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp).height(14.dp),
                            value = if (isSeeking) seekValue else positionMs.toFloat(),
                            onValueChange = { seekValue = it; isSeeking = true; exoPlayer.seekTo(it.toLong()); touchInteraction() },
                            onValueChangeFinished = { exoPlayer.seekTo(seekValue.toLong()); isSeeking = false; touchInteraction() },
                            valueRange = 0f..durationMs.coerceAtLeast(1L).toFloat()
                        )
                        Text(formatDuration(durationMs), color = Color.White, style = MaterialTheme.typography.labelSmall)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        
                        Surface(
                            onClick = {
                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                                touchInteraction()
                            },
                            shape = RoundedCornerShape(50),
                            color = Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(if (isPlaying) "Ⅱ" else "▶", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                            }
                        }

                    }

                }
            }

            // Playback speed and aspect-ratio popups are opened from the top-right menu.
            if (speedMenu) {
                DropdownMenu(expanded = true, onDismissRequest = { speedMenu = false }) {
                    listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f, 3f, 4f).forEach { value ->
                        DropdownMenuItem(text = { Text("${value}×") }, onClick = {
                            speed = value
                            exoPlayer.setPlaybackSpeed(value)
                            speedMenu = false
                            touchInteraction()
                        })
                    }
                }
            }
            if (aspectMenu) {
                DropdownMenu(expanded = true, onDismissRequest = { aspectMenu = false }) {
                    listOf(
                        "Fit" to AspectRatioFrameLayout.RESIZE_MODE_FIT,
                        "Fill / crop" to AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                        "Stretch" to AspectRatioFrameLayout.RESIZE_MODE_FILL,
                        "Fixed width" to AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    ).forEach { (label, mode) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = {
                            resizeMode = mode
                            aspectMenu = false
                            touchInteraction()
                        })
                    }
                }
            }
        }
        if (brightnessIndicator != null) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black.copy(alpha = 0.72f),
                shape = MaterialTheme.shapes.large
            ) {
                Text(brightnessIndicator!!, modifier = Modifier.padding(horizontal = 22.dp, vertical = 15.dp), color = Color.White)
            }
        }
        if (overlayMessage != null) {
            Surface(
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 76.dp),
                color = Color.Black.copy(alpha = 0.76f),
                shape = MaterialTheme.shapes.medium
            ) { Text(overlayMessage!!, modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp), color = Color.White) }
        }
    }
}

private fun extractPreviewFrame(context: Context, uri: android.net.Uri): Bitmap? {
    val retriever = MediaMetadataRetriever()
    return try {
        retriever.setDataSource(context, uri)
        retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
    } catch (_: Exception) { null } finally { runCatching { retriever.release() } }
}

private fun captureVideoScreenshot(context: Context, uri: android.net.Uri, positionMs: Long): Boolean {
    val retriever = MediaMetadataRetriever()
    var bitmap: Bitmap? = null
    return try {
        retriever.setDataSource(context, uri)
        bitmap = retriever.getFrameAtTime(positionMs.coerceAtLeast(0L) * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)
        val frame = bitmap ?: return false
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "ReelsGallery_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ReelsGallery")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val resolver = context.contentResolver
        val outputUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false
        val written = resolver.openOutputStream(outputUri)?.use { stream ->
            frame.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        } ?: false
        if (Build.VERSION.SDK_INT >= 29) {
            val ready = ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }
            resolver.update(outputUri, ready, null, null)
        }
        if (!written) resolver.delete(outputUri, null, null)
        written
    } catch (_: Exception) {
        false
    } finally {
        bitmap?.recycle()
        runCatching { retriever.release() }
    }
}

@Composable
private fun PlayerAction(glyph: String, label: String, iconSize: Int, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 2.dp)) {
        TextButton(onClick = onClick, modifier = Modifier.size((iconSize + 25).dp)) {
            Text(glyph, color = Color.White, style = MaterialTheme.typography.headlineSmall)
        }
        Text(label, color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
    }
}
