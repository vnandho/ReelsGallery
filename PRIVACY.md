# Privacy — Reels Gallery

Reels Gallery dirancang untuk pemakaian lokal dan offline.

- Foto, video, nama file, thumbnail, subtitle yang dipilih, posisi playback, dan preferensi diproses pada perangkat.
- Source manifest meminta `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_VISUAL_USER_SELECTED`, dan `MODIFY_AUDIO_SETTINGS` untuk pengaturan volume media. Tidak ada izin internet.
- Source manifest secara eksplisit menghapus `android.permission.INTERNET` pada proses manifest merge.
- Tidak ada akun, login, iklan, pembelian, analytics, telemetry, remote config, atau crash reporting berbasis jaringan dalam source project ini.
- Aplikasi tidak memindahkan, menghapus, atau memodifikasi media asli.
- Pengguna dapat memberi akses penuh atau hanya media yang dipilih sesuai dialog izin Android.

Catatan verifikasi: merged manifest dan lalu lintas jaringan runtime belum dapat diuji pada sesi pembuatan ini karena Android SDK dan dependency build tidak dapat diunduh. Klaim final harus diverifikasi lagi pada APK hasil build.
