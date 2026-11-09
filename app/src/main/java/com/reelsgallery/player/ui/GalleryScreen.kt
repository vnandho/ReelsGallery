package com.reelsgallery.player.ui

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.reelsgallery.player.data.GalleryFilter
import com.reelsgallery.player.data.LocalMedia
import com.reelsgallery.player.data.MediaRepository
import com.reelsgallery.player.data.MediaSort
import com.reelsgallery.player.data.SettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    media: List<LocalMedia>,
    settings: SettingsStore,
    refreshToken: Int,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenMedia: (LocalMedia, List<LocalMedia>) -> Unit
) {
    var filter by remember { mutableStateOf(GalleryFilter.ALL) }
    var sort by remember { mutableStateOf(MediaSort.NEWEST) }
    var search by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf<String?>(null) }
    var folderMenu by remember { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }
    val currentGridColumns = remember(refreshToken) { settings.gridColumns }

    val folders = remember(media) { media.map { it.relativePath }.distinct().sorted() }
    val orderedVideos = remember(media, sort) {
        val videos = media.filter { it.isVideo }
        when (sort) {
            MediaSort.NEWEST -> videos.sortedByDescending { it.dateAddedSeconds }
            MediaSort.OLDEST -> videos.sortedBy { it.dateAddedSeconds }
            MediaSort.NAME -> videos.sortedBy { it.displayName.lowercase() }
            MediaSort.SIZE -> videos.sortedByDescending { it.sizeBytes }
            MediaSort.DURATION -> videos.sortedByDescending { it.durationMs }
        }
    }
    val visible = remember(media, filter, sort, search, selectedFolder) {
        val filtered = media.filter { item ->
            (filter == GalleryFilter.ALL || (filter == GalleryFilter.VIDEOS && item.isVideo) || (filter == GalleryFilter.PHOTOS && !item.isVideo)) &&
                (selectedFolder == null || item.relativePath == selectedFolder) &&
                (search.isBlank() || item.displayName.contains(search.trim(), ignoreCase = true))
        }
        when (sort) {
            MediaSort.NEWEST -> filtered.sortedByDescending { it.dateAddedSeconds }
            MediaSort.OLDEST -> filtered.sortedBy { it.dateAddedSeconds }
            MediaSort.NAME -> filtered.sortedBy { it.displayName.lowercase() }
            MediaSort.SIZE -> filtered.sortedByDescending { it.sizeBytes }
            MediaSort.DURATION -> filtered.sortedByDescending { it.durationMs }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("REELS GALLERY", style = MaterialTheme.typography.titleLarge)
                        Text("Your local media library", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    }
                    IconButton(onClick = onRefresh) { Text("↻", style = MaterialTheme.typography.headlineSmall) }
                    IconButton(onClick = onOpenSettings) { Text("⚙", style = MaterialTheme.typography.titleLarge) }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Search file name") },
                    leadingIcon = { Text("⌕") },
                    shape = RoundedCornerShape(15.dp)
                )
                Spacer(Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    GalleryFilter.entries.forEach { option ->
                        FilterChip(
                            selected = filter == option,
                            onClick = { filter = option },
                            label = { Text(option.label()) }
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Box {
                        TextButton(onClick = { folderMenu = true }) { Text(if (selectedFolder == null) "Folders ▾" else selectedFolder!!.trimEnd('/').substringAfterLast('/') + " ▾") }
                        DropdownMenu(expanded = folderMenu, onDismissRequest = { folderMenu = false }) {
                            DropdownMenuItem(text = { Text("All folders") }, onClick = { selectedFolder = null; folderMenu = false })
                            folders.forEach { path ->
                                val label = path.trimEnd('/').substringAfterLast('/').ifBlank { "Other" }
                                val count = media.count { it.relativePath == path }
                                DropdownMenuItem(
                                    text = { Text("$label  ·  $count") },
                                    onClick = { selectedFolder = path; folderMenu = false }
                                )
                            }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${visible.size} items", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.weight(1f))
                    Box {
                        TextButton(onClick = { sortMenu = true }) { Text("Sort: ${sort.label()} ▾") }
                        DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                            MediaSort.entries.forEach { option ->
                                DropdownMenuItem(text = { Text(option.label()) }, onClick = { sort = option; sortMenu = false })
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (visible.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (media.isEmpty()) "No media found" else "Nothing matches your search", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(
                    if (media.isEmpty()) "Add photos or videos to your device, then refresh the gallery." else "Try another filter or folder.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                Button(onClick = onRefresh) { Text("Refresh gallery") }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(currentGridColumns),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                items(visible, key = { "${it.uri}" }) { item ->
                    val queueForItem = if (settings.queueScope == "ALL") orderedVideos else orderedVideos.filter { it.relativePath == item.relativePath }
                    MediaTile(item = item, onClick = { onOpenMedia(item, queueForItem) })
                }
            }
        }
    }
}

@Composable
private fun MediaTile(item: LocalMedia, onClick: () -> Unit) {
    val context = LocalContext.current
    val bitmap: Bitmap? by produceState(initialValue = null, item.uri) {
        value = withContext(Dispatchers.IO) { MediaRepository.thumbnail(context, item.uri) }
    }
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(0.78f).clip(RoundedCornerShape(13.dp)).background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (bitmap != null) {
                Image(bitmap!!.asImageBitmap(), contentDescription = item.displayName, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (item.isVideo) "▶" else "▧", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (item.isVideo) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp),
                    color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.78f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(formatDuration(item.durationMs), modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall, color = androidx.compose.ui.graphics.Color.White)
                }
                Text("▶", modifier = Modifier.align(Alignment.TopStart).padding(7.dp), color = androidx.compose.ui.graphics.Color.White)
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(item.displayName, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun PhotoViewer(item: LocalMedia, onClose: () -> Unit) {
    androidx.activity.compose.BackHandler(onBack = onClose)
    Box(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black)) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context -> ImageView(context).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageURI(item.uri) } },
            update = { if (it.tag != item.uri) { it.setImageURI(item.uri); it.tag = item.uri } }
        )
        TextButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Text("‹  Back", color = androidx.compose.ui.graphics.Color.White)
        }
    }
}

private fun GalleryFilter.label() = when (this) {
    GalleryFilter.ALL -> "All"
    GalleryFilter.VIDEOS -> "Videos"
    GalleryFilter.PHOTOS -> "Photos"
}

private fun MediaSort.label() = when (this) {
    MediaSort.NEWEST -> "Newest"
    MediaSort.OLDEST -> "Oldest"
    MediaSort.NAME -> "Name"
    MediaSort.SIZE -> "File size"
    MediaSort.DURATION -> "Duration"
}

fun formatDuration(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%d:%02d".format(minutes, seconds)
}
