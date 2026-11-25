# Panduan Build & Signing di Android Studio (SCM dan EII Departemen)

Folder `release/` ini disediakan sebagai lokasi penyimpanan output build rilis dan panduan signing saat Anda membuka project ini di Android Studio.

## 1. Debug Keystore (`debug.keystore`)
- File `debug.keystore` di root project sudah sesuai dengan konfigurasi `signingConfigs.debugConfig` di `app/build.gradle.kts` serta SHA-1/SHA-256 yang terdaftar pada `app/google-services.json` untuk Firebase Google Sign-In.
- **Jangan menimpa atau mengganti `debug.keystore`** jika Anda masih ingin menggunakan proyek Firebase yang sama untuk login Google pada build debug.
- Jika Anda mengunduh lewat Git dan file `debug.keystore` terabaikan oleh `.gitignore`, pulihkan dari `debug.keystore.base64` melalui terminal di root project:
  - **Linux / macOS**: `base64 -d debug.keystore.base64 > debug.keystore`
  - **Windows PowerShell**: `[IO.File]::WriteAllBytes("debug.keystore", [Convert]::FromBase64String((Get-Content -Raw debug.keystore.base64)))`

## 2. Release Key (`my-upload-key.jks`)
Konfigurasi `signingConfigs.release` di `app/build.gradle.kts` menggunakan environment variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`) dengan default path `${rootDir}/my-upload-key.jks` dan `keyAlias = "upload"`.

Untuk membuat APK/AAB rilis di Android Studio:
1. Pilih menu **Build > Generate Signed Bundle / APK...** di Android Studio.
2. Pilih **APK** atau **Android App Bundle**, lalu klik **Create new...** untuk membuat keystore rilis Anda (simpan misalnya ke `release/my-upload-key.jks` atau `${rootDir}/my-upload-key.jks` dengan alias `upload`).
3. Pilih folder tujuan output ke folder `release/` ini.
4. Catatan: Jika menjalankan `gradle assembleRelease` lewat CLI, set environment variable `KEYSTORE_PATH`, `STORE_PASSWORD`, dan `KEY_PASSWORD` sebelum build (atau ekspor APK/AAB langsung dari menu pengaturan AI Studio).
5. Jika menggunakan keystore rilis baru untuk Google Sign-In, jangan lupa daftarkan SHA-1 & SHA-256 dari keystore rilis tersebut di Firebase Console -> Project Settings -> Android App.
