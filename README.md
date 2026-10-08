# 🚀 PocketAPI - Mobile REST API Client

PocketAPI adalah aplikasi Android open-source yang berfungsi sebagai **REST API Client ringan (Mini Postman)** langsung dari smartphone Anda. Dibuat dengan **Kotlin Modern**, **Jetpack Compose (Material 3)**, **OkHttp 4**, dan **Room Database** dengan arsitektur **MVVM (Model-View-ViewModel)**.

---

## 📱 Fitur Utama

* **Dynamic Request Builder**:
  * Mendukung HTTP Method: `GET`, `POST`, `PUT`, `DELETE`, `PATCH`, `HEAD`.
  * Input URL fleksibel dengan fitur paste dan auto-prefix `https://`.
  * **Headers Editor**: Menambah, mengubah, menonaktifkan, atau menghapus custom header (*Key-Value*).
  * **Dedicated Auth Tab**: Dukungan autentikasi otomatis (*No Auth*, *Bearer Token / JWT*, *Basic Auth Base64*, dan *API Key Header / Query Param*).
  * **JSON Body Editor**: Editor request body untuk method POST/PUT dengan tombol **"Format JSON"** (Pretty Print).
* **Preset Endpoint Bawaan**:
  * 1-tap testing menggunakan endpoint publik gratis (JSONPlaceholder, HttpBin).
* **cURL Import & Export**:
  * **Import cURL**: Tempel perintah cURL dari browser/Postman dan otomatis parsing ke URL, Method, Headers, dan Body.
  * **Copy as cURL**: Salin request aktif ke clipboard sebagai perintah terminal cURL yang valid.
* **Real-time Response Inspector**:
  * Status Code badge dengan indikator warna standar HTTP (2xx Hijau, 4xx Oranye, 5xx Merah).
  * Metrik performa: **Response Time (ms)** dan **Payload Size (KB/B)**.
  * Tab navigasi respon: **Formatted Body**, **Response Headers**, dan **Raw Body**.
  * Tombol **Salin ke Clipboard** 1-tap.
* **Offline History & Favorites (Room DB)**:
  * Setiap request yang dijalankan otomatis tersimpan di database lokal.
  * Klik item riwayat untuk memuat ulang konfigurasi request ke layar editor.
  * Fitur bookmark/bintang (*Favorite*) dan pembersihan riwayat.

---

## 🛠️ Tech Stack & Library

* **Bahasa**: [Kotlin 2.0](https://kotlinlang.org/)
* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Networking**: [OkHttp 4.12.0](https://square.github.io/okhttp/) (Dynamic Request Engine)
* **Local Database**: [Room 2.6.1](https://developer.android.com/training/data-storage/room) & SQLite
* **Asynchronous**: Kotlin Coroutines & `StateFlow`
* **JSON Processing**: Gson & Org.Json
* **Arsitektur**: MVVM (Model - View - ViewModel) + Repository Pattern

---

## 📁 Struktur Direktori

```
PocketAPI/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/example/pocketapi/
│   │   │   ├── MainActivity.kt               # Entrypoint UI
│   │   │   ├── PocketApiApp.kt               # Application class & DI
│   │   │   ├── data/
│   │   │   │   ├── model/                    # HttpMethod, ApiRequest, ApiResponse, HeaderItem
│   │   │   │   ├── local/                    # Room Database, DAO, Entity
│   │   │   │   ├── network/                  # OkHttp dynamic execution engine
│   │   │   │   └── repository/               # ApiRepository coordinating network & DB
│   │   │   ├── ui/
│   │   │   │   ├── components/               # Badges, KeyValueEditor, JsonCodeViewer
│   │   │   │   ├── screens/                  # MainScreen, RequestSection, ResponseSection, HistoryBottomSheet
│   │   │   │   ├── theme/                    # Color, Theme, Typography
│   │   │   │   └── viewmodel/                # PocketApiViewModel & Factory
│   │   │   └── util/                         # JsonFormatter & Date utility
│   │   └── res/                              # XML Values & Themes
│   └── build.gradle.kts                      # App module dependencies
├── build.gradle.kts                          # Root build script
├── settings.gradle.kts                       # Repository management
├── gradle.properties
└── gradlew.bat
```

---

## 🚀 Cara Menjalankan Proyek di Android Studio

1. Buka aplikasi **Android Studio**.
2. Pilih menu **File** $\rightarrow$ **Open...**
3. Arahkan ke folder: `D:\project\PocketAPI` lalu klik **OK**.
4. Tunggu beberapa saat hingga Android Studio menyelesaikan proses **Gradle Sync**.
5. Pilih perangkat emulator Android atau hubungkan smartphone fisik Anda via kabel USB (USB Debugging aktif).
6. Klik tombol hijau **Run 'app'** (atau tekan `Shift + F10`).
7. Aplikasi siap digunakan! Coba klik salah satu tombol preset seperti `GET Post #1` lalu tekan tombol **Send** untuk melihat respon secara langsung.

---

## 💡 Ide Pengembangan Lanjutan (Next Level)

Jika ingin meningkatkan portofolio ini lebih jauh:
1. **OAuth2 / Token Manager**: Fitur simpan environment variables (misal: `{{token}}` atau `{{baseUrl}}`).
2. **Export / Import cURL**: Membaca format `curl` command dan men-generate cURL dari request yang dibuat.
3. **GraphQL Support**: Menambahkan query editor untuk endpoint GraphQL.