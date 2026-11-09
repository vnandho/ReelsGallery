# Project Architecture

## Platform and major dependencies

- Android native, Kotlin, Jetpack Compose UI.
- Android `PlayerView` embedded through `AndroidView` for video rendering.
- AndroidX Media3 ExoPlayer 1.11.1 for playback, seeking, queues, track selection, repeat and speed.
- MediaStore for local media discovery and thumbnails.
- SharedPreferences as local key/value persistence for user settings and resume positions.
- Android Activity Result APIs for runtime media permission and local subtitle document selection.

## Source packages

```text
com.reelsgallery.player
  MainActivity.kt
  data/
    MediaItem.kt
    MediaRepository.kt
    SettingsStore.kt
  player/
    PlayerGestureClassifier.kt
  ui/
    Theme.kt
    ReelsGalleryApp.kt
    GalleryScreen.kt
    PlayerScreen.kt
    SettingsScreen.kt
```

## Data flow

1. `MainActivity` hosts `ReelsGalleryApp`.
2. `MediaRepository` queries `MediaStore.Files` on `Dispatchers.IO` and returns local `LocalMedia` records.
3. `GalleryScreen` applies filter, search, folder and sort state; thumbnail loading uses `ContentResolver.loadThumbnail` locally.
4. Opening a video creates a queue from the selected folder or all accessible videos, in the currently selected sort order.
5. `PlayerScreen` creates a Media3 `ExoPlayer`, supplies the queue, restores the start item's saved position, and releases the player on disposal.
6. Player gestures are classified by `PlayerGestureClassifier`: the starting X coordinate selects left / center / right; dominant vertical motion determines next / previous in the center zone.
7. `SettingsStore` persists appearance, gallery/player preferences, and resume positions using app-private SharedPreferences.

## Gesture rules implemented

- Start position defines the zone (default 28% on each side).
- Side zones only handle brightness / media volume when their setting is enabled.
- Center zone only navigates after a sufficiently long dominant vertical swipe.
- Empty-surface tap toggles controls; Play/Pause is only bound to its explicit button.
- The seek slider and Compose buttons are placed above the gesture surface so direct controls receive input first.

## Boundaries

There is no named playlist database, no software decoder engine, no custom per-button arrangement model, no custom subtitle styling/delay UI, and no background playback service. Theme mode currently supports Dark / Light / System. These are follow-up implementation areas, not completed features. Actual format support depends on Media3 extractors and the codecs provided by the Android device.
