# REELS GALLERY — MASTER BUILD DIRECTIVE FOR CLAUDE

**Tujuan:** Bangun aplikasi Android native bernama **Reels Gallery** yang siap dipasang pengguna melalui file APK. Pengguna tidak diminta menulis kode, mengedit file proyek, atau menjalankan perintah build secara manual. Claude harus mengerjakan implementasi, build, pengujian, dan menyiapkan file unduhan sejauh lingkungan yang tersedia mengizinkan.

> **PENTING — CARA MENJALANKAN DOKUMEN INI**  
> Gunakan dokumen ini di **Claude Code** (Claude Desktop bagian Code, Claude Code CLI, atau integrasi IDE yang menyediakan akses workspace dan terminal). Claude Code dapat membaca/mengubah file proyek serta menjalankan perintah dan tes. Chat Claude biasa tanpa akses filesystem/terminal tidak dapat dijamin menghasilkan APK yang benar-benar terkompilasi. Jangan mengklaim APK siap apabila file APK belum benar-benar dibuat dan diverifikasi.

---

## 0. PERINTAH UTAMA UNTUK CLAUDE

Bertindak sebagai **senior Android architect, Kotlin engineer, multimedia playback engineer, UI/UX designer, QA engineer, dan release/build engineer**.

Jangan berhenti pada proposal, tutorial, pseudocode, mockup, atau daftar file. **Kerjakan aplikasi secara nyata di workspace yang tersedia.** Buat semua source code, konfigurasi Gradle, resource, pengujian, dokumentasi, dan APK. Jalankan build dan perbaiki error sampai berhasil atau sampai ada blocker lingkungan yang benar-benar tidak dapat diatasi.

Ketentuan kerja:

1. Mulai dengan memeriksa workspace. Bila sudah ada source project Reels Gallery atau ZIP source project sebelumnya, audit dan lanjutkan proyek tersebut; jangan menghapus atau menimpa pekerjaan yang masih berguna. Bila workspace kosong, buat proyek baru.
2. Gunakan tool, filesystem, terminal, dan kemampuan build yang benar-benar tersedia. Jangan mengarang hasil command, log, test, screenshot, atau file keluaran.
3. Jangan meminta pengguna menulis kode atau mengedit file secara manual. Jika perlu persetujuan untuk instalasi toolchain, akses jaringan build-time, atau tindakan yang memang memerlukan izin, jelaskan secara singkat dan minta persetujuan melalui mekanisme Claude Code. Jangan meminta konfirmasi untuk keputusan teknis rutin yang dapat ditentukan secara aman.
4. Jangan melakukan publikasi, push ke repository publik, upload ke layanan pihak ketiga, atau membagikan source/keystore tanpa persetujuan eksplisit pengguna.
5. Bila satu dependency atau API sudah berubah/deprecated, periksa dokumentasi resmi versi terbaru dan pilih pengganti stabil. Jangan menebak nomor versi library.
6. Prioritaskan hasil APK yang dapat diinstal. Build dan tes harus dilakukan sebelum menyatakan pekerjaan selesai.
7. Jika build lokal tidak dimungkinkan, lakukan diagnosis, siapkan jalur build otomatis alternatif (misalnya workflow CI) bila integrasi dan otorisasi tersedia, lalu jelaskan persis langkah eksternal yang masih dibutuhkan. Jangan menyebut APK selesai sebelum APK benar-benar tersedia.
8. Pada akhir pekerjaan, berikan tautan/file unduhan APK, ZIP source project, laporan build/test, dan checksum SHA-256 apabila file-file tersebut berhasil dibuat.

**Instruksi eksekusi singkat:** Baca seluruh dokumen ini, buat rencana internal, lalu langsung implementasikan. Jangan hanya membalas dengan rencana. Jangan bertanya ulang mengenai spesifikasi yang sudah ditetapkan di sini.

---

## 1. IDENTITAS DAN TARGET PROYEK

| Item | Ketentuan |
|---|---|
| Nama aplikasi | **Reels Gallery** |
| Application ID / package | `com.reelsgallery.player` |
| Platform | Android native |
| Minimum Android | Android 14 / API 34 |
| `minSdk` | `34` |
| `targetSdk` | API stabil terbaru yang tersedia dan sesuai kebijakan Android saat build; jangan gunakan preview SDK |
| `compileSdk` | API stabil terbaru yang kompatibel dengan Android Gradle Plugin yang dipilih |
| Bahasa utama | Kotlin |
| UI | Jetpack Compose; gunakan Android Views untuk komponen multimedia bila lebih stabil/kompatibel |
| Arsitektur | Modular, testable, lifecycle-aware |
| Tema | Dark mode sebagai default; sediakan Light mode dan System mode |
| Orientasi | Portrait dan Landscape |
| Runtime internet | **Tidak diperlukan dan tidak boleh digunakan** |
| Monetisasi | Tidak ada iklan, tidak ada pembelian dalam aplikasi, tidak ada langganan |
| Analytics/tracking | Tidak ada |
| Penyimpanan | Lokal di perangkat |
| Distribusi utama | APK Android yang dapat diunduh dan dipasang langsung |

### Definisi “gratis dan offline”

- Aplikasi tidak meminta biaya, langganan, akun, login, atau koneksi internet.
- Aplikasi tidak memuat iklan, SDK iklan, analytics, telemetry, remote config, crash-reporting berbasis jaringan, atau pelacak pihak ketiga.
- Pemindaian media, thumbnail, pemutaran, playlist, subtitle, pengaturan, dan seluruh fitur inti berfungsi tanpa internet.
- Dependency dapat perlu diunduh saat proses pengembangan/build pertama kali. Itu adalah kebutuhan build-time, bukan koneksi internet saat aplikasi dipakai. Jelaskan perbedaan ini dalam README.
- Jangan menambahkan permission `android.permission.INTERNET` ke manifest aplikasi. Periksa juga **merged manifest** agar dependency tidak memasukkan permission internet secara tidak sengaja. Jika sebuah library mewajibkan internet permission, cari konfigurasi atau dependency alternatif yang tetap memenuhi persyaratan; jangan diam-diam mengabaikannya.

### Batasan merek dan desain

Aplikasi harus memiliki identitas visual, ikon, layout, dan source code sendiri. Tujuannya adalah **paritas fitur pemutar media seperti MX Player**, bukan menyalin merek, logo, aset proprietary, source code, atau tampilan persis milik MX Player. Jangan memakai nama/logo MX Player di aplikasi atau listing.

---

## 2. PRIORITAS PRODUK

Urutan prioritas implementasi:

1. Pemutar video lokal yang stabil dan lengkap.
2. Gesture video yang akurat dan tidak salah memicu Pause/Play.
3. Panel kontrol pemutaran yang dapat dikustomisasi.
4. Gallery grid, album, folder, dan playlist.
5. Subtitle, audio track, decoder, aspect ratio, dan fitur lanjutan.
6. Pengujian perangkat nyata/emulator dan build APK.

Jangan mengorbankan stabilitas playback demi animasi atau dekorasi UI.

---

## 3. HALAMAN GALLERY

Buat halaman awal berupa gallery konvensional, bukan feed Reels.

### 3.1 Tampilan utama

- Grid thumbnail media lokal dengan desain rapi dan modern.
- Item video menampilkan badge durasi dan indikator video.
- Foto dapat dibedakan dari video secara visual.
- Pilihan filter: **Semua**, **Video**, dan **Foto**.
- Navigasi album/folder berdasarkan folder media lokal.
- Tampilan daftar folder dan jumlah item di tiap folder.
- Pencarian berdasarkan nama file.
- Sorting: terbaru, terlama, nama, ukuran, dan durasi video.
- Pilihan ukuran grid/kolom thumbnail.
- Refresh/rescan media saat pengguna kembali ke gallery atau menarik daftar ke bawah.
- Empty state dan permission-denied state yang jelas.

### 3.2 Perilaku saat video dipilih

- Ketika pengguna memilih sebuah video, buka player fullscreen.
- Bentuk antrean/playlist dari konteks asal video: secara default video lain di folder/album yang sama.
- Sediakan pengaturan cakupan antrean: folder saat ini, semua video yang dapat diakses, atau playlist aktif.
- Video berikut/sebelumnya mengikuti antrean dan urutan sorting yang sedang dipakai.
- Setelah kembali dari player, pertahankan posisi scroll, folder, filter, dan item yang dipilih.
- Jangan memulai video lain secara acak.

### 3.3 Foto

Gallery boleh membuka foto dengan image viewer sederhana. Gesture video dan kontrol player tidak boleh mengganggu perilaku foto.

### 3.4 Android 14 media permission

- Implementasikan akses media Android 14+ dengan benar, termasuk kemungkinan **akses sebagian** terhadap foto/video.
- Pertimbangkan `READ_MEDIA_VIDEO`, `READ_MEDIA_IMAGES`, dan `READ_MEDIA_VISUAL_USER_SELECTED` sesuai flow yang dibangun.
- Jangan mengasumsikan izin selalu penuh. Refresh hasil MediaStore ketika permission berubah atau aplikasi kembali ke foreground.
- Untuk pemilihan file/folder individual, gunakan Storage Access Framework / Photo Picker bila sesuai.
- Hindari `MANAGE_EXTERNAL_STORAGE` kecuali ada alasan kuat yang dijelaskan dan benar-benar dibutuhkan; pilihan default adalah permission media yang lebih sempit dan SAF.
- Aplikasi tidak boleh mengubah, memindahkan, atau menghapus file media tanpa aksi eksplisit pengguna.

---

## 4. VIDEO PLAYER — FITUR DAN PERILAKU WAJIB

Buat video player fullscreen dengan kontrol lengkap dan tampilan bersih. Sediakan kontrol sentuh yang mudah dijangkau, responsif, serta tidak menghalangi video secara permanen.

### 4.1 Kontrol dasar yang wajib tersedia

| Kontrol | Perilaku |
|---|---|
| Play / Pause | Memulai dan menjeda video |
| Repeat | Repeat off, repeat current video, dan repeat playlist bila relevan |
| Rotate | Rotasi tampilan/video 90° atau penguncian orientasi; tidak merusak orientasi asli file |
| Fullscreen | Mengaktifkan/keluar immersive fullscreen dengan benar |
| Backward | Melompat mundur sesuai interval yang dapat diatur |
| Forward | Melompat maju sesuai interval yang dapat diatur |
| Timeline / Seekbar | Menampilkan posisi dan durasi; dapat digeser untuk seek |
| Next video | Memainkan video berikutnya dalam antrean |
| Previous video | Memainkan video sebelumnya dalam antrean |
| Subtitle | Memilih, mengaktifkan, menonaktifkan, dan mengatur subtitle |
| Audio track | Memilih track audio yang tersedia |
| Playback speed | Mengubah kecepatan playback |
| Aspect ratio | Fit, Fill/Crop, Stretch, Original, dan pilihan yang masuk akal |

Interval default tombol forward/backward: 10 detik. Sediakan pilihan 5, 10, 15, 30, dan 60 detik.

### 4.2 Tap satu kali untuk menyembunyikan/menampilkan kontrol

Perilaku ini **wajib dan sangat penting**:

- Ketika kontrol terlihat, satu tap pada area video kosong menyembunyikan ikon dan panel kontrol.
- Ketika kontrol tersembunyi, satu tap pada area video kosong menampilkan kembali kontrol.
- Tap pada area video **tidak boleh otomatis Pause atau Play**.
- Play/Pause hanya dijalankan ketika tombol Play/Pause disentuh.
- Tap pada tombol kontrol menjalankan aksi tombol tersebut, bukan dianggap sebagai tap kosong pada video.
- Jangan memicu dua aksi sekaligus dari satu sentuhan (misalnya tombol Pause menjalankan Pause lalu overlay langsung tertutup secara tidak sengaja).
- Sediakan auto-hide kontrol yang dapat diatur: nonaktif, 3 detik, 5 detik, 10 detik. Default 3 detik setelah interaksi kontrol, tetapi tap kosong tetap dapat toggle manual.
- Ketika pengguna sedang menggeser timeline, mengubah slider, atau menekan kontrol, jangan auto-hide di tengah interaksi.

### 4.3 Gesture sisi kiri dan kanan

Gunakan gesture vertikal berbasis **zona awal sentuhan** agar tidak bentrok dengan swipe navigasi video.

Default zona:

- **Sisi kiri layar (sekitar 25–30% lebar):** drag vertikal mengatur brightness layar player.
- **Sisi kanan layar (sekitar 25–30% lebar):** drag vertikal mengatur volume media.
- **Area tengah:** swipe vertikal untuk berpindah video berikut/sebelumnya.

Arah default:

- Swipe/drag ke atas pada zona brightness: brightness naik.
- Swipe/drag ke bawah pada zona brightness: brightness turun.
- Swipe/drag ke atas pada zona volume: volume naik.
- Swipe/drag ke bawah pada zona volume: volume turun.

Tampilkan indikator overlay sementara saat brightness/volume berubah, misalnya ikon dan persentase. Gunakan kontrol volume media Android yang sesuai; brightness sebaiknya diterapkan pada window/player saat ini dan jangan mengubah pengaturan brightness global sistem tanpa kebutuhan dan izin yang sesuai.

Sediakan pengaturan untuk:

- Mengaktifkan/menonaktifkan gesture brightness.
- Mengaktifkan/menonaktifkan gesture volume.
- Membalik arah gesture.
- Mengatur lebar zona kiri/kanan.
- Mengatur sensitivitas gesture.

### 4.4 Swipe vertikal untuk Next / Previous Video

- Swipe **ke atas** pada zona tengah player: pindah ke video berikutnya.
- Swipe **ke bawah** pada zona tengah player: pindah ke video sebelumnya.
- Video tujuan harus langsung autoplay.
- Player tetap dalam mode fullscreen; tidak kembali ke gallery.
- Jangan memunculkan Pause hanya karena jari menyentuh layar.
- Jaga state player yang sesuai ketika berpindah (volume, brightness, preferensi subtitle, ukuran kontrol, dan mode fullscreen).
- Bila sudah di video pertama/terakhir, ikuti setting batas antrean: berhenti, wrap ke awal/akhir, atau tampilkan pesan halus. Default: berhenti di batas antrean.
- Tampilkan nama video/nomor antrean sesaat setelah navigasi, bukan dialog yang menghalangi video.

### 4.5 Pencegahan konflik gesture — acceptance-critical

Buat gesture recognizer/state machine yang membedakan tap, drag, swipe vertikal, dan interaksi timeline.

Aturan minimum:

- Tentukan zona gesture berdasarkan posisi pointer saat `ACTION_DOWN`/awal gesture, bukan posisi jari setelah bergerak.
- Bedakan tap dan swipe menggunakan touch slop/ambang gerak yang sesuai density perangkat.
- Swipe vertikal hanya sah jika perpindahan vertikal jelas lebih besar dari perpindahan horizontal (misalnya rasio arah yang dapat dikonfigurasi, sekitar 1.3:1).
- Gesture baru dianggap navigasi setelah ambang jarak tercapai; satu tap tidak boleh mengganti video.
- Timeline memiliki prioritas tertinggi saat disentuh langsung; drag seekbar tidak boleh mengubah brightness/volume atau berpindah video.
- Tombol kontrol memiliki prioritas tertinggi; sentuhan pada tombol tidak boleh diteruskan ke permukaan video.
- Sisi kiri dan kanan diprioritaskan untuk brightness/volume; area tengah untuk next/previous.
- Jangan memakai double-tap sebagai Pause/Play secara default. Pengguna sudah menetapkan bahwa tap kosong hanya toggle visibilitas ikon.
- Tambahkan unit test untuk klasifikasi gesture dan uji interaksi Compose/View bila memungkinkan.

### 4.6 Timeline

- Tampilkan waktu berjalan dan durasi total.
- Tampilkan progress/buffer bila tersedia.
- Bisa drag untuk seek ke posisi lain.
- Sediakan opsi selalu tampil ketika kontrol terlihat, dan jangan menangkap gesture selain saat pengguna menyentuh area timeline.
- Perbarui progress secara efisien tanpa membuat UI terus-menerus melakukan recomposition berat.

---

## 5. CUSTOM PLAYER CONTROL PANEL

Pengguna harus dapat mengatur tampilan kontrol player tanpa mengubah source code.

### 5.1 Tombol yang dapat dikustomisasi

Minimal:

- Repeat
- Previous video
- Backward
- Play/Pause
- Forward
- Next video
- Rotate
- Fullscreen
- Subtitle
- Audio track
- Playback speed
- Aspect ratio
- Timeline

### 5.2 Pengaturan tampilan

Sediakan halaman **Settings → Player Controls** yang memungkinkan:

- Mengubah ukuran ikon global.
- Mengubah ukuran ikon individual (opsional, jika implementasi stabil).
- Mengaktifkan/menonaktifkan tombol tertentu.
- Mengubah urutan tombol (drag-and-drop atau kontrol urutan sederhana).
- Memilih penempatan kelompok kontrol: atas/bawah, kiri/kanan, atau posisi default yang ergonomis.
- Mengubah ukuran area sentuh tombol terpisah dari ukuran gambar ikon agar tetap mudah digunakan.
- Memilih gaya ikon dari set ikon orisinal yang disediakan aplikasi (misalnya outline/filled), bukan mengambil ikon proprietary aplikasi lain.
- Mengatur transparansi/background panel kontrol.
- Mengatur warna ikon, warna aksen, dan ukuran timeline.
- Reset ke pengaturan default.

Rentang ukuran ikon awal yang direkomendasikan: 18–56 dp. Sediakan slider dan nilai numerik. Simpan pengaturan secara lokal menggunakan DataStore atau mekanisme lokal yang setara.

### 5.3 Tata letak default

Desain default harus mudah dipahami:

- Area atas: tombol kembali, judul/nama file, menu pengaturan atau informasi.
- Area tengah: kontrol previous/backward/play-pause/forward/next bila mode kontrol tengah aktif.
- Area bawah: timeline dan waktu berjalan/durasi.
- Area tambahan: repeat, rotate, fullscreen, subtitle, audio track, speed dan aspect ratio.

Layout harus responsif pada portrait dan landscape. Hindari tombol terlalu kecil atau berdekatan. Jangan membuat seluruh layar tertutup kontrol saat overlay tampil.

---

## 6. FITUR PEMUTARAN LANJUTAN

Implementasikan secara nyata, bukan hanya tombol dekoratif.

### 6.1 Format dan codec

Targetkan dukungan luas untuk format/container umum seperti MP4, MKV, MOV, AVI, WebM, FLV, 3GP, MPEG/TS, MP3, AAC, FLAC, WAV, dan format lain sejauh didukung oleh engine dan decoder perangkat.

**Jangan menjanjikan “semua codec pasti bisa diputar”.** Kemampuan akhir bergantung pada backend, versi library, lisensi, hardware/software decoder, dan kemampuan perangkat. Tampilkan pesan error yang informatif bila file/codec tidak didukung.

Buat abstraksi `VideoEngine`/`PlaybackEngine` sehingga engine dapat diganti tanpa menulis ulang UI dan gesture.

Evaluasi mesin pemutar berdasarkan:

1. Kemampuan format/container/codec.
2. Subtitle SRT dan ASS/SSA.
3. Pilihan audio track dan subtitle track.
4. Hardware decoding dan opsi software decoding bila tersedia.
5. Stabilitas, ukuran APK, ABI/native library support.
6. Lisensi dan kewajiban distribusi.
7. Kemampuan runtime offline tanpa permission internet.

LibVLC dapat dipertimbangkan untuk dukungan format yang lebih luas. Media3/ExoPlayer dapat dipertimbangkan untuk integrasi Android modern dan format/codec yang didukung. Periksa dokumentasi resmi dan lisensi **versi dependency yang benar-benar dipilih** sebelum implementasi. Jangan memasukkan seluruh aplikasi VLC atau aset/merek VLC; gunakan library yang sesuai dan penuhi kewajiban lisensinya. Jika memakai komponen LGPL/GPL, sertakan notice, teks lisensi, dan source/relokasi yang diwajibkan lisensi tersebut. Jangan mengabaikan kewajiban open-source.

### 6.2 Hardware/software decoding

- Default: Auto.
- Jika backend mendukung, sediakan pilihan Hardware, Software, dan Auto.
- Jelaskan bahwa software decoding bisa lebih berat pada CPU, baterai, dan suhu perangkat.
- Fallback ke decoder lain hanya bila aman; jangan crash jika decoder pilihan gagal.
- Informasikan nama/jenis decoder bila backend menyediakan metadata.

### 6.3 Subtitle

Target dukungan:

- Embedded subtitle track.
- External `.srt`.
- External `.ass` / `.ssa`.
- Format lain jika backend mendukung.
- Browse/open subtitle lokal melalui SAF.
- Subtitle on/off dan pemilihan track.
- Ukuran font, warna, outline/background, dan posisi subtitle.
- Subtitle delay/synchronization adjustment.
- Jangan memerlukan download subtitle dari internet.

### 6.4 Audio

- Pilihan audio track jika lebih dari satu.
- Audio delay/synchronization bila didukung engine.
- Volume player melalui sistem audio media Android.
- Pertahankan volume normal sistem saat pengguna keluar dari player.

### 6.5 Playback dan tampilan

- Playback speed: 0.25× sampai 4× jika backend mendukung; gunakan pilihan bertahap yang ergonomis.
- Aspect ratio: Original, Fit, Fill/Crop, Stretch, dan rasio manual bila memungkinkan.
- Rotasi manual 0°/90°/180°/270° jika engine mendukung; bedakan dari orientasi device.
- Fullscreen immersive yang pulih dengan baik setelah notifikasi, app switch, dan rotasi perangkat.
- Resume dari posisi terakhir per video, dengan opsi mulai dari awal.
- Repeat off / repeat one / repeat playlist.
- Playlist lokal dan antrean pemutaran.
- Lock controls opsional agar sentuhan tak sengaja tidak mengubah playback.
- Audio focus dan lifecycle Android ditangani dengan benar (pause/resume sesuai perilaku yang wajar ketika panggilan atau aplikasi lain mengambil audio focus).
- Background audio dapat dibuat opsional melalui pengaturan; default video playback tetap foreground.

---

## 7. PENGATURAN APLIKASI

Buat halaman Settings dengan kelompok berikut:

1. **Gallery:** ukuran grid, sorting default, filter default, folder yang disembunyikan, refresh/rescan.
2. **Player Controls:** ukuran ikon, visibilitas, urutan, posisi, gaya ikon, timeout auto-hide.
3. **Gestures:** brightness, volume, zona swipe next/previous, sensitivitas, pembalikan arah.
4. **Playback:** interval forward/backward, repeat default, resume playback, speed default, orientasi.
5. **Subtitles:** font, ukuran, warna, posisi, delay default, encoding bila diperlukan.
6. **Decoder:** Auto/Hardware/Software jika didukung engine.
7. **Appearance:** Dark/Light/System, warna aksen, skala kontrol.
8. **Privacy & Storage:** status permission, folder/SAF sources, penjelasan data lokal.
9. **About:** versi aplikasi, informasi library open-source dan lisensinya.

Semua setting harus tersimpan secara lokal dan bertahan setelah aplikasi ditutup/dibuka kembali.

---

## 8. DESAIN UI/UX

- Desain original, minimal, modern, dan nyaman untuk penggunaan satu tangan.
- Dark mode sebagai tampilan pertama, dengan kontras yang baik.
- Gunakan Material 3/Compose bila sesuai, namun jangan memaksakan Compose pada permukaan rendering video bila Android View/Surface lebih stabil.
- Gunakan ikon vector lokal; jangan mengunduh ikon saat runtime.
- Hindari animasi berat yang memengaruhi frame rate.
- Loading, error, empty state, permission state, dan unsupported file harus memiliki UI yang jelas.
- Nama file panjang harus ditampilkan secara aman (ellipsis dan akses detail).
- Pastikan status bar/navigation bar berperilaku benar ketika player fullscreen dan kembali ke gallery.
- Hormati ukuran font sistem dan accessibility settings.
- Tombol kontrol harus memiliki content description/talkback label.

---

## 9. ARSITEKTUR TEKNIS YANG DIREKOMENDASIKAN

Pilih arsitektur yang sederhana, modular, dan mudah dipelihara. Contoh pembagian package (boleh disesuaikan jika ada alasan teknis):

```text
com.reelsgallery.player/
  app/
  core/
    model/
    common/
    permissions/
    storage/
    database/
    settings/
    design/
  feature/
    gallery/
    albums/
    imageviewer/
    player/
    playlists/
    settings/
  playback/
    engine/
    vlc/             (jika dipilih)
    media3/          (jika dipilih)
    controls/
    gestures/
    subtitles/
```

### Komponen utama

- `MediaRepository`: sumber data foto/video dari MediaStore/SAF.
- `PlaybackQueue`: urutan video berdasarkan folder/filter/playlist.
- `PlaybackEngine`: abstraksi engine multimedia.
- `PlayerViewModel`: state player, queue, progress, repeat, track selection, speed.
- `PlayerGestureController`: klasifikasi tap, swipe, brightness, volume, dan timeline.
- `PlayerControlsConfig`: preferensi ikon, layout, size, order dan visibility.
- `SettingsRepository`: penyimpanan lokal DataStore.
- `FavoritesRepository` / database lokal: favorites dan data playlist jika dibutuhkan.
- `ThumbnailLoader`: thumbnail lokal; jangan melakukan network image loading.

Gunakan coroutine/Flow secara benar. Hindari query MediaStore di main thread. Gunakan lifecycle-aware playback dan pelepasan native player/Surface/resource untuk mencegah memory leak.

### Penyimpanan data

- Simpan konfigurasi dan preferensi secara lokal.
- Simpan metadata tambahan (favorite, playlist, posisi resume) di database lokal bila sesuai.
- Jangan mengunggah daftar file, nama file, thumbnail, subtitle, atau statistik pengguna.
- Jangan menyalin media pengguna ke folder aplikasi tanpa alasan; gunakan content URI dan persistable URI permission bila sesuai.

---

## 10. KEAMANAN, PERMISSION, DAN PRIVASI

Persyaratan wajib:

- Tidak meminta internet permission.
- Tidak ada iklan, analytics, tracking, attribution SDK yang membuat koneksi, remote config, login, atau server.
- Tidak meminta kontak, lokasi, kamera, mikrofon, atau permission yang tidak relevan.
- Gunakan permission media paling terbatas yang memenuhi kebutuhan.
- Gunakan SAF untuk folder atau file yang dipilih pengguna.
- Jangan meminta `MANAGE_EXTERNAL_STORAGE` secara default.
- Jangan membaca atau mengubah file di luar akses yang diberikan Android.
- Jangan mengirim data lokal ke pihak ketiga.
- Jangan menambahkan WebView yang memuat konten remote.
- Periksa dependency transitif dan merged manifest untuk menemukan permission/service/provider yang tidak diinginkan.
- Sertakan halaman privacy singkat yang menjelaskan bahwa media dan preferensi diproses lokal.

---

## 11. STRATEGI BUILD OTOMATIS — PENGGUNA TIDAK MENULIS KODE

Tujuan akhir adalah pengguna hanya mengunduh dan memasang APK. Karena itu Claude harus mengerjakan toolchain dan build semaksimal mungkin.

### 11.1 Deteksi lingkungan

Periksa:

- OS dan arsitektur host.
- Java/JDK yang tersedia dan kompatibilitasnya dengan Android Gradle Plugin/Gradle yang dipilih.
- Android SDK, `sdkmanager`, platform, build-tools, dan emulator (jika ada).
- Ruang disk dan akses jaringan build-time.
- Apakah workspace dapat menghasilkan file yang bisa diunduh/di-attach.

Gunakan Gradle Wrapper di proyek; jangan mengandalkan Gradle global. Pilih JDK berdasarkan compatibility matrix resmi Android Gradle Plugin/Gradle saat build. Android API 34 memiliki Java core library level 17; verifikasi toolchain proyek dan bukan mengasumsikan semua versi plugin kompatibel.

### 11.2 Jika Android SDK belum tersedia

- Bila diizinkan, pasang Android command-line tools dan platform/build-tools stabil yang sesuai melalui kanal resmi.
- Tidak perlu meminta pengguna menginstal Android Studio atau menulis perintah manual bila Claude Code dapat menyiapkan toolchain di lingkungan kerja.
- Jangan mengunduh SDK dari situs tidak resmi.
- Bila toolchain tidak dapat disiapkan di host, siapkan workflow CI build Android yang reproducible. Hanya gunakan GitHub/CI bila integrasi sudah terhubung atau pengguna menyetujui tindakan autentikasi/publikasi yang diperlukan. Jangan membuat repository publik tanpa izin.

### 11.3 Build dan test wajib

Jalankan sekurang-kurangnya task yang tersedia dan relevan:

- `./gradlew --version`
- `./gradlew clean`
- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`
- `./gradlew assembleDebug`
- `./gradlew assembleRelease` bila signing/configuration tersedia

Perbaiki semua error yang dapat diperbaiki. Untuk tes instrumented/UI, jalankan jika emulator atau perangkat uji tersedia. Jangan menyebut tes berhasil apabila tidak dijalankan.

### 11.4 Verifikasi APK

Setelah build:

- Pastikan file APK benar-benar ada dan ukurannya lebih dari nol.
- Verifikasi APK signature dengan `apksigner verify` jika tool tersedia.
- Verifikasi metadata package, `minSdk`, dan `targetSdk` dengan `aapt`/`apkanalyzer`/tool setara.
- Periksa merged manifest dan pastikan tidak ada `android.permission.INTERNET`.
- Pastikan ABI/native library sesuai target perangkat; usahakan universal APK bila dependency mendukung ABI yang relevan.
- Jika emulator/perangkat tersedia, instal APK dan jalankan smoke test: launch, grant media permission, gallery load, open video, play/pause, seek, gesture, close/reopen.
- Buat checksum SHA-256.

### 11.5 APK yang harus disiapkan

Prioritas:

1. **APK debug yang ditandatangani otomatis oleh Android debug signing**, sebagai APK installable pertama yang tidak memerlukan keystore milik pengguna.
2. APK release bertanda tangan bila konfigurasi signing yang aman tersedia.
3. Jika tidak dapat membuat release signing secara aman, jangan mengarang signing key atau menyimpan password secara tidak aman. Jelaskan keterbatasannya; tetap serahkan APK debug yang benar-benar sudah berhasil dibuat bila tersedia.

Jangan mengirim keystore privat ke repository, log, atau layanan publik. Jangan menampilkan password/secret di laporan biasa. Jika membuat keystore release, simpan dengan aman dan jelaskan bahwa pengguna harus menyimpan backup untuk update aplikasi berikutnya.

### 11.6 Lokasi output

Letakkan deliverable pada folder `dist/` di root project:

```text
dist/
  ReelsGallery-v1.0.0-android14plus-debug.apk
  ReelsGallery-v1.0.0-android14plus-release.apk   (jika berhasil dibuat)
  ReelsGallery-source.zip
  BUILD_REPORT.md
  SHA256SUMS.txt
```

Nama file boleh disesuaikan dengan versi aktual. Jangan menaruh placeholder atau file APK kosong.

---

## 12. PENGUJIAN DAN ACCEPTANCE CRITERIA

Aplikasi belum boleh dinyatakan selesai hanya karena proyek berhasil dikompilasi. Pastikan kriteria berikut diimplementasikan dan dites sejauh lingkungan memungkinkan.

### Gallery

- [ ] Aplikasi terbuka tanpa internet.
- [ ] Permission flow Android 14+ berjalan dan menjelaskan akses penuh/sebagian.
- [ ] Grid memuat thumbnail lokal.
- [ ] Filter All/Video/Photo bekerja.
- [ ] Folder/album dapat dibuka.
- [ ] Search dan sorting bekerja.
- [ ] Kembali dari player mempertahankan scroll/filter/folder.

### Player dan tap

- [ ] Video diputar fullscreen.
- [ ] Tap kosong ketika kontrol terlihat menyembunyikan kontrol.
- [ ] Tap kosong ketika kontrol tersembunyi menampilkan kontrol.
- [ ] Tap kosong tidak Pause/Play.
- [ ] Tombol Play/Pause benar-benar mengontrol playback.
- [ ] Tap tombol kontrol tidak diteruskan sebagai tap kosong.
- [ ] Auto-hide dapat dikonfigurasi dan tidak mengganggu drag/seek.

### Gesture

- [ ] Swipe up di area tengah pindah ke video berikutnya dan autoplay.
- [ ] Swipe down di area tengah pindah ke video sebelumnya dan autoplay.
- [ ] Drag vertikal sisi kiri mengatur brightness, bukan berpindah video.
- [ ] Drag vertikal sisi kanan mengatur volume, bukan berpindah video.
- [ ] Drag timeline hanya melakukan seek.
- [ ] Tap biasa tidak dianggap swipe.
- [ ] Sentuhan pada ikon tidak memicu gesture permukaan video.
- [ ] Batas antrean video ditangani sesuai setting.

### Custom controls

- [ ] Ukuran ikon dapat diubah dan tersimpan.
- [ ] Ikon dapat ditampilkan/disembunyikan.
- [ ] Urutan/posisi dapat disesuaikan sesuai fitur yang dibuat.
- [ ] Tombol repeat, rotate, fullscreen, forward, backward, dan timeline berfungsi nyata.
- [ ] Tampilan kontrol tetap baik di portrait dan landscape.

### Playback lanjutan

- [ ] Resume playback bekerja.
- [ ] Repeat one/all/off bekerja.
- [ ] Speed control bekerja pada backend yang dipakai.
- [ ] Audio track selection bekerja bila media memiliki beberapa track.
- [ ] Subtitle SRT dan ASS/SSA diuji dengan sample yang legal dan tersedia lokal.
- [ ] Aspect ratio/zoom bekerja.
- [ ] Error decoder/file unsupported tidak menyebabkan crash.

### Offline/privacy

- [ ] App tidak meminta internet permission.
- [ ] Tidak ada SDK iklan/analytics/tracking.
- [ ] Aplikasi tetap dapat digunakan dalam airplane mode setelah terpasang.
- [ ] Tidak ada request jaringan runtime dari aplikasi.

### Release/build

- [ ] APK berhasil dibuat dan tidak kosong.
- [ ] APK signature diverifikasi bila tool tersedia.
- [ ] APK dapat diinstal pada Android 14+ atau emulator API 34+ bila perangkat uji tersedia.
- [ ] Build report dan checksum dibuat.
- [ ] Kegagalan tes yang belum terselesaikan dicatat secara jujur.

---

## 13. SAMPLE TEST MEDIA

Untuk pengujian lokal, buat atau gunakan file uji yang legal dan boleh didistribusikan. Jika sample tidak disertakan, jangan mengunduh konten berhak cipta secara sembarangan. Uji setidaknya:

- MP4 H.264/AAC.
- MP4 H.265/HEVC jika perangkat/engine mendukung.
- MKV dengan beberapa audio/subtitle track bila tersedia.
- Sample SRT eksternal.
- Sample ASS/SSA eksternal.
- File dengan durasi pendek dan panjang.
- File landscape dan portrait.
- File tanpa metadata durasi atau yang rusak untuk menguji error handling.

Jangan memasukkan sample media besar ke APK produksi. Sample dapat berada pada folder test assets terpisah dan dihapus dari deliverable produksi jika lisensi atau ukuran menjadi masalah.

---

## 14. DOKUMENTASI YANG HARUS DIBUAT

Claude harus membuat:

- `README.md`: fitur, cara instal APK, batasan, dan penjelasan offline.
- `BUILD_REPORT.md`: environment, dependency utama, command yang dijalankan, status build, test yang benar-benar dijalankan, lokasi APK, ukuran APK, checksum, dan known limitations.
- `LICENSES/`: lisensi dan notices dependency pihak ketiga yang wajib disertakan.
- `PRIVACY.md`: penjelasan bahwa data/media diproses lokal dan tidak ada tracking/network runtime.
- `PROJECT_ARCHITECTURE.md`: ringkasan modul dan alur playback/gesture.
- `CHANGELOG.md`: versi dan perubahan.

README harus menjelaskan instalasi dengan bahasa awam:

1. Download APK.
2. Buka file APK di Android.
3. Izinkan instalasi dari sumber tersebut jika Android memintanya.
4. Install dan buka Reels Gallery.
5. Berikan akses media yang dipilih agar gallery dapat menampilkan koleksi lokal.

Jangan meminta pengguna membuka Android Studio atau menjalankan Gradle.

---

## 15. FORMAT LAPORAN AKHIR KEPADA PENGGUNA

Setelah implementasi, balas menggunakan bahasa Indonesia dan format berikut:

1. **Status:** selesai / sebagian selesai / terblokir.
2. **APK:** tautan file APK yang benar-benar ada dan sudah diverifikasi. Jelaskan apakah debug atau release.
3. **Source project:** tautan ZIP source.
4. **Build dan tes:** sebutkan command/test yang benar-benar berhasil/gagal/tidak dijalankan.
5. **Fitur:** ringkas fitur yang sudah bekerja dan fitur yang belum.
6. **Batasan:** codec/perangkat atau kemampuan yang belum bisa diverifikasi.
7. **Instalasi:** instruksi singkat untuk pengguna awam.

**Larangan laporan:**

- Jangan mengklaim semua fitur MX Player tersedia jika belum diimplementasikan.
- Jangan mengklaim dukungan semua format/codec.
- Jangan menyebut build sukses tanpa APK yang benar-benar dihasilkan.
- Jangan menampilkan sandbox/file link yang tidak diverifikasi.
- Jangan menyembunyikan kegagalan test atau blocker.

---

## 16. KEPUTUSAN ENGINE VIDEO DAN LISENSI

Sebelum memilih backend, lakukan riset singkat pada dokumentasi resmi terkini dan tulis keputusan pada `PROJECT_ARCHITECTURE.md`.

- Pertimbangkan LibVLC Android bila dukungan codec luas menjadi prioritas. Gunakan library yang tepat, audit lisensi artifact, ABI, ukuran APK, dan kewajiban distribusi. Repositori resmi VideoLAN membedakan lisensi aplikasi VLC Android dari lisensi engine LibVLC; jangan menganggap semua komponen memiliki lisensi identik.
- Pertimbangkan AndroidX Media3/ExoPlayer bila format yang diperlukan didukung dan integrasi Android native lebih sesuai. Perhatikan bahwa dukungan format/container bergantung pada sample format dan decoder yang tersedia; dokumentasi Android menjelaskan matriks dukungannya.
- Jangan memilih hanya berdasarkan popularitas. Lakukan proof-of-concept playback, subtitle, seek, track selection, hardware/software decoder, serta uji permission offline sebelum menetapkan backend.
- Jika menggunakan dua backend, buat satu abstraksi API dan pilih backend per file/setting secara jelas. Jangan menambah ukuran APK besar tanpa manfaat yang terukur.
- Sertakan lisensi, attribution, dan source material yang diwajibkan. Jangan memakai library bajakan atau menghapus notice lisensi.

---

## 17. URUTAN PELAKSANAAN WAJIB

Kerjakan dalam urutan berikut tanpa meminta pengguna mengulang spesifikasi:

### Fase A — Audit dan desain teknis

- Periksa workspace dan proyek lama jika ada.
- Verifikasi dokumentasi terkini untuk Android API, Gradle, Kotlin, player engine, dan izin media.
- Tetapkan struktur project, backend, dan dependency stabil.
- Catat risiko teknis dan lisensi.

### Fase B — Bangun fondasi aplikasi

- Buat proyek Android native.
- Konfigurasi package, SDK, theme, navigation, permissions, MediaStore, SAF, DataStore, dan database jika diperlukan.
- Pastikan manifest tidak memuat internet permission.

### Fase C — Gallery

- Implementasikan grid, thumbnail, folder, filter, search, sorting, permission state, dan queue.

### Fase D — Playback engine

- Implementasikan engine sebenarnya, lifecycle, surface, queue, progress, seek, track selection, subtitle, speed, decoder preferences, aspect ratio, dan repeat.

### Fase E — Custom controls dan gesture

- Implementasikan semua tombol.
- Buat settings kustomisasi ikon.
- Buat gesture state machine dengan pemisahan zona.
- Implementasikan tap-to-toggle overlay tanpa tap-to-pause.
- Tambahkan unit/UI tests.

### Fase F — Quality assurance

- Jalankan test, lint, build, permission/manifest audit, offline audit, APK verification, dan smoke test bila emulator/perangkat tersedia.
- Perbaiki error yang ditemukan.

### Fase G — Packaging

- Buat APK installable.
- Buat source ZIP.
- Buat BUILD_REPORT dan SHA256SUMS.
- Pastikan file output benar-benar tersedia.

### Fase H — Serah terima

- Berikan unduhan APK terlebih dahulu.
- Berikan ZIP source sebagai cadangan.
- Berikan instruksi instalasi sederhana.
- Jelaskan dengan jujur fitur yang selesai dan yang belum.

---

## 18. PERINTAH PEMBUKA UNTUK DIKIRIM KE CLAUDE CODE

Setelah membuka folder workspace di Claude Code, kirim pesan ini:

> Baca file `Reels_Gallery_Master_Prompt_Claude.md` sampai selesai dan jadikan sebagai spesifikasi serta instruksi kerja utama. Implementasikan Reels Gallery secara nyata di workspace ini. Jangan hanya membuat rencana atau tutorial. Audit proyek yang sudah ada bila tersedia, lanjutkan tanpa menghapus pekerjaan yang berguna, lalu buat/selesaikan seluruh source code, jalankan test dan build, perbaiki error, verifikasi APK, dan letakkan APK installable serta source ZIP di folder `dist/`. Saya bukan programmer: jangan meminta saya menulis atau mengedit kode. Minta izin hanya untuk tindakan yang memang memerlukan otorisasi. Jangan pernah mengklaim APK berhasil dibuat sebelum file APK benar-benar ada dan tervalidasi. Ikuti seluruh acceptance criteria dan ketentuan offline/no-ads dalam dokumen.

---

## 19. REFERENSI RESMI

Gunakan tautan ini sebagai titik awal dan cek dokumentasi versi terbaru saat implementasi:

- Anthropic — Claude Code common developer use cases: https://support.claude.com/en/articles/14553517-claude-code-common-developer-use-cases
- Anthropic — Claude Code FAQ: https://support.claude.com/en/articles/14554922-claude-code-user-faq
- Anthropic — Claude Code product page: https://claude.com/product/claude-code
- Android Developers — Media3 supported formats: https://developer.android.com/media/media3/exoplayer/supported-formats
- Android Developers — Android 14 behavior changes / partial photo and video access: https://developer.android.com/about/versions/14/behavior-changes-14
- Android Developers — Java versions in Android builds: https://developer.android.com/build/jdks
- VideoLAN — LibVLC Android README and licensing notes: https://github.com/videolan/vlc-android/blob/master/README.md

---

**HASIL AKHIR YANG DIHARAPKAN:** Reels Gallery adalah aplikasi gallery dan media player Android native yang dapat dipasang melalui APK; bebas iklan, tidak memerlukan internet saat digunakan, memiliki video player kaya fitur, kontrol yang dapat dikustomisasi, gesture sisi kiri/kanan untuk brightness/volume, dan swipe vertikal di area tengah untuk Next/Previous Video. Pengguna cukup mengunduh APK dan memasangnya — bukan mengerjakan coding.
