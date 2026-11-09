# Reels Gallery

Aplikasi Android native untuk menelusuri media lokal dan memutar video secara offline. Identitas, ikon, dan UI dibuat sendiri; proyek ini tidak menyalin merek atau aset aplikasi pemutar lain.

## Status versi ini

Source project awal sudah disusun, tetapi **APK belum berhasil dibangun atau diuji** di lingkungan pembuatan saat ini. Tidak ada file APK yang disertakan sebagai placeholder. Lihat `BUILD_REPORT.md` untuk kondisi toolchain dan hasil pemeriksaan yang benar-benar dilakukan.

## Fitur yang sudah diimplementasikan di source

- Gallery grid untuk foto dan video lokal, filter All / Videos / Photos, pencarian nama file, folder, sorting newest / oldest / name / size / duration, dan pilihan kolom grid.
- Akses media Android 14+ termasuk permission `READ_MEDIA_VISUAL_USER_SELECTED` untuk akses media pilihan pengguna.
- Thumbnail lokal via MediaStore; tidak memakai layanan gambar daring.
- Player fullscreen berbasis AndroidX Media3 / ExoPlayer.
- Play / pause, timeline seek, forward / backward dengan interval 5 / 10 / 15 / 30 / 60 detik, previous / next video, repeat off / one / playlist, speed 0.25×–4×, pilihan audio track dan subtitle track, pemilihan file subtitle eksternal SRT / SSA/ASS, aspect ratio, rotasi tampilan, dan auto-hide kontrol.
- Tap pada area video kosong hanya menampilkan atau menyembunyikan overlay. Tap kosong tidak menjalankan Play/Pause.
- Drag vertikal sisi kiri untuk brightness, sisi kanan untuk volume, dan swipe vertikal area tengah untuk navigasi antrean. Zona gesture ditentukan oleh posisi awal sentuhan.
- Preferensi dan posisi resume disimpan lokal.
- Appearance Dark / Light / System tersimpan lokal.

## Fitur yang belum lengkap / belum diverifikasi

- Tidak ada build APK yang tervalidasi pada sesi ini.
- Tidak ada uji perangkat nyata atau emulator.
- Kustomisasi panel kontrol masih terbatas pada ukuran ikon global, bukan menyusun ulang / menyembunyikan setiap tombol secara individual.
- Preferensi decoder Auto / Hardware / Software belum disediakan.
- Subtitle style, subtitle delay, audio delay, dan subtitle encoding belum disediakan sebagai pengaturan UI.
- Tidak ada manajemen playlist bernama/favorit; antrean folder dan semua video tersedia.
- Viewer foto masih sederhana tanpa zoom/pan.
- Dukungan codec/container bergantung pada decoder yang tersedia di perangkat dan matriks dukungan Media3. Jangan menganggap semua FLV / AVI / MKV / codec akan selalu berfungsi.
- Merged manifest, APK signature, ABI, dan tidak adanya request jaringan runtime belum dapat diverifikasi lewat hasil build karena SDK/dependency tidak dapat diunduh pada host ini.

## Persyaratan teknis

- Android minimum: Android 14 / API 34.
- Compile / target SDK: Android 16 / API 36 (stable).
- Kotlin: 2.4.21.
- Android Gradle Plugin: 9.3.1.
- Gradle: 9.7.0.
- JDK: 21 tersedia pada host ini; proyek memakai Java/Kotlin target 17.
- UI: Jetpack Compose 1.11.4 + Material 3 1.4.0; rendering video memakai Android View `PlayerView`.
- Playback: AndroidX Media3 1.11.1.
- Runtime internet: tidak diminta di manifest aplikasi. Ada deklarasi `tools:node="remove"` untuk mencegah permission INTERNET dari manifest dependency masuk ke hasil merge.

Versi AGP / Gradle dipilih agar berada dalam compatibility range yang didokumentasikan untuk Kotlin Gradle Plugin 2.4.21. API 36 dipilih karena merupakan SDK stabil terbaru yang terverifikasi untuk konfigurasi ini; UI Compose 1.11.4 digunakan bersama Material 3 1.4.0 dan Media3 1.11.1. Referensi resmi diperiksa pada 9 November 2026.

## Build lokal (ketika jaringan dan Android SDK tersedia)

1. Pasang Android SDK platform `android-36` dan Build Tools `36.0.0` melalui Android SDK Manager resmi.
2. Pastikan JDK 21 tersedia.
3. Jalankan `./gradlew clean testDebugUnitTest lintDebug assembleDebug` pada Linux/macOS atau `gradlew.bat clean testDebugUnitTest lintDebug assembleDebug` pada Windows.
4. APK debug akan berada di `app/build/outputs/apk/debug/app-debug.apk` jika build berhasil.

Script `gradlew` di arsip ini adalah bootstrapper ringan: ia mengunduh distribusi Gradle resmi saat pertama dijalankan. Ini merupakan kebutuhan build-time, bukan koneksi internet saat aplikasi dipakai. Karena itu, host tanpa akses jaringan tidak dapat melakukan bootstrap.

## Build otomatis

Workflow manual ada di `.github/workflows/android-build.yml`. Setelah source dimasukkan ke repository GitHub privat milik pengguna, workflow dapat membangun APK debug, menjalankan unit test dan lint, lalu mengunggah APK sebagai workflow artifact. Workflow tidak membuat repository, tidak memublikasikan source, dan tidak berjalan sampai pengguna sendiri mengaktifkannya di repository mereka.

## Privasi dan instalasi

- Aplikasi hanya meminta izin foto/video yang relevan.
- Aplikasi tidak meminta login, internet, kontak, lokasi, kamera, atau mikrofon.
- File asli tidak dipindahkan, dihapus, atau diubah.
- Setelah APK tersedia: download APK, buka file di Android, izinkan instalasi dari sumber tersebut bila diminta, install, buka Reels Gallery, lalu berikan akses media yang dipilih.
