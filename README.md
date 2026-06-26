# TaskLite

TaskLite adalah aplikasi to-do list offline Android native berbasis Java dan XML Views.

## Base setup yang sudah tersedia

- Runtime dependency pihak ketiga dihapus dari aplikasi utama.
- `minSdk` 31, `targetSdk` 35, `versionName` 1.0.0.
- SQLite lokal melalui `SQLiteOpenHelper` dengan database `tasklite.db`.
- Tabel `tasks` dan `categories` mengikuti PRD, termasuk index dan foreign key `ON DELETE SET NULL`.
- Seed kategori awal: Pribadi, Kuliah, Pekerjaan.
- MainActivity menampilkan daftar tugas dari SQLite, ringkasan aktif/selesai, pencarian, filter Semua/Hari Ini/Terlambat/Selesai, empty state, dan tombol tambah.
- TaskFormActivity mendukung tambah/edit tugas, judul, deskripsi, kategori, prioritas, deadline, dan pengingat dasar.
- Checklist selesai/aktif kembali dan hapus tugas sudah tersambung ke database.
- Kerangka reminder, notification channel, boot receiver, settings, category, preferences, dan backup manager sudah disiapkan untuk fase berikutnya.

## Build

Gunakan JDK/JBR Android Studio. Jika terminal memakai Java 25, jalankan:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat assembleDebug --no-daemon
```

APK debug akan dibuat di:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Catatan

`compileSdk` masih 36 karena SDK lokal yang tersedia hanya Android 36. `targetSdk` sudah disesuaikan ke 35 seperti PRD.
