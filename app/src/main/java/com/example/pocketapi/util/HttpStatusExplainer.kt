package com.example.pocketapi.util

data class HttpStatusInfo(
    val code: Int,
    val name: String,
    val category: String,
    val rfcStandard: String,
    val summary: String,
    val clientCause: String,
    val serverCause: String,
    val solutions: List<String>
)

object HttpStatusExplainer {

    private val statusDatabase: Map<Int, HttpStatusInfo> = mapOf(
        200 to HttpStatusInfo(
            code = 200,
            name = "OK",
            category = "2xx Sukses",
            rfcStandard = "RFC 9110, Section 15.3.1",
            summary = "Permintaan HTTP berhasil diproses oleh server.",
            clientCause = "Klien mengirim request dengan format, header, dan parameter yang valid.",
            serverCause = "Server berhasil menemukan atau memproses data dan mengirim respons kembali.",
            solutions = listOf(
                "Respons berhasil diterima, tidak diperlukan tindakan perbaikan.",
                "Periksa response body untuk memastikan format data sesuai ekspektasi klien."
            )
        ),
        201 to HttpStatusInfo(
            code = 201,
            name = "Created",
            category = "2xx Sukses",
            rfcStandard = "RFC 9110, Section 15.3.2",
            summary = "Permintaan berhasil dan entitas sumber daya baru telah dibuat di server.",
            clientCause = "Klien mengirim permintaan POST/PUT untuk membuat data baru.",
            serverCause = "Server berhasil menyimpan data baru ke dalam database dan biasanya mengembalikan data yang baru dibuat beserta URI lokasinya.",
            solutions = listOf(
                "Cek header 'Location' jika server mengembalikan URI entitas yang baru dibuat.",
                "Verifikasi ID entitas baru yang ada pada response body."
            )
        ),
        202 to HttpStatusInfo(
            code = 202,
            name = "Accepted",
            category = "2xx Sukses",
            rfcStandard = "RFC 9110, Section 15.3.3",
            summary = "Permintaan telah diterima untuk diproses, namun prosesnya belum selesai (asynchronous / background job).",
            clientCause = "Klien memicu tugas batch, ekspor data, atau antrean background task.",
            serverCause = "Server memasukkan request ke dalam antrean (queue) untuk dieksekusi secara asinkron.",
            solutions = listOf(
                "Simpan task ID atau polling URL dari response untuk memantau status pengerjaan.",
                "Jangan berasumsi bahwa perubahan data sudah selesai seketika."
            )
        ),
        204 to HttpStatusInfo(
            code = 204,
            name = "No Content",
            category = "2xx Sukses",
            rfcStandard = "RFC 9110, Section 15.3.5",
            summary = "Permintaan berhasil diproses, namun server sengaja tidak mengembalikan konten pada body respons.",
            clientCause = "Klien umumnya mengirim permintaan DELETE atau UPDATE di mana tidak dibutuhkan data kembalian.",
            serverCause = "Server telah mengeksekusi aksi dan mengonfirmasi keberhasilan tanpa perlu mengirim body respons.",
            solutions = listOf(
                "Jangan mencoba mem-parse JSON response body karena body kosong secara desain.",
                "Periksa status kode 204 sebagai konfirmasi bahwa entitas telah berhasil dihapus/diperbarui."
            )
        ),
        301 to HttpStatusInfo(
            code = 301,
            name = "Moved Permanently",
            category = "3xx Pengalihan",
            rfcStandard = "RFC 9110, Section 15.4.2",
            summary = "Sumber daya target telah dipindahkan secara permanen ke URL baru.",
            clientCause = "Klien mengakses URL versi lama (misal HTTP biasa, sedangkan server mewajibkan HTTPS, atau rute API lama).",
            serverCause = "Server merespons dengan header 'Location' yang mengarahkan ke URL baru.",
            solutions = listOf(
                "Perbarui URL endpoint di klien ke alamat baru yang tertera pada header 'Location'.",
                "Pastikan protokol (HTTP vs HTTPS) sudah sesuai dengan domain server."
            )
        ),
        304 to HttpStatusInfo(
            code = 304,
            name = "Not Modified",
            category = "3xx Pengalihan / Cache",
            rfcStandard = "RFC 9110, Section 15.4.5",
            summary = "Konten sumber daya belum berubah sejak permintaan terakhir berdasarkan header caching.",
            clientCause = "Klien mengirim header kondisional seperti 'If-None-Match' (ETag) atau 'If-Modified-Since'.",
            serverCause = "Server memeriksa ETag/Timestamp dan mendapati data masih identik, sehingga menghemat bandwidth tanpa mengirim ulang body.",
            solutions = listOf(
                "Gunakan salinan cache lokal yang sudah tersimpan di klien.",
                "Hapus header If-None-Match jika ingin memaksa pengambilan data terbaru."
            )
        ),
        400 to HttpStatusInfo(
            code = 400,
            name = "Bad Request",
            category = "4xx Kesalahan Klien",
            rfcStandard = "RFC 9110, Section 15.5.1",
            summary = "Server tidak dapat memproses permintaan karena kesalahan sintaks atau payload dari klien.",
            clientCause = "Format JSON tidak valid, field wajib hilang, tipe data salah (misal string di kolom integer), atau query parameter tidak sesuai schema.",
            serverCause = "Middleware validasi backend menolak payload sebelum masuk ke business logic.",
            solutions = listOf(
                "Periksa kembali struktur JSON di Tab Body (pastikan tanda kurung, koma, dan kutip valid).",
                "Periksa pesan error spesifik pada response body untuk melihat field yang gagal validasi.",
                "Pastikan header 'Content-Type: application/json' terpasang saat mengirim JSON."
            )
        ),
        401 to HttpStatusInfo(
            code = 401,
            name = "Unauthorized",
            category = "4xx Kesalahan Klien",
            rfcStandard = "RFC 9110, Section 15.5.2",
            summary = "Permintaan memerlukan autentikasi pengguna, namun token/kredensial hilang atau tidak valid.",
            clientCause = "Header Authorization belum diisi, Bearer Token sudah kedaluwarsa (expired), atau kredensial Basic Auth keliru.",
            serverCause = "Auth middleware (JWT/OAuth) gagal memverifikasi signature token atau sesi login telah habis masa berlakunya.",
            solutions = listOf(
                "Buka Tab Auth di PocketAPI dan periksa Bearer Token atau Basic Auth Anda.",
                "Gunakan fitur Inspeksi JWT di PocketAPI untuk memeriksa apakah klaim 'exp' sudah lewat waktu.",
                "Lakukan refresh token atau login ulang untuk mendapatkan access token baru."
            )
        ),
        403 to HttpStatusInfo(
            code = 403,
            name = "Forbidden",
            category = "4xx Kesalahan Klien",
            rfcStandard = "RFC 9110, Section 15.5.4",
            summary = "Server memahami identitas Anda, tetapi Anda tidak memiliki izin hak akses (authorization / permission) ke sumber daya ini.",
            clientCause = "Role atau permission pengguna tidak mencukupi (misal role 'user' mencoba mengakses endpoint admin), atau IP diblokir oleh WAF.",
            serverCause = "Access Control List (ACL) atau Role-Based Access Control (RBAC) menolak akses ke endpoint.",
            solutions = listOf(
                "Pastikan akun yang digunakan memiliki role atau hak akses yang memadai.",
                "Periksa apakah API Key atau token Anda memiliki cakupan (scope) yang diizinkan untuk endpoint ini.",
                "Hubungi administrator backend jika Anda merasa seharusnya memiliki izin akses."
            )
        ),
        404 to HttpStatusInfo(
            code = 404,
            name = "Not Found",
            category = "4xx Kesalahan Klien",
            rfcStandard = "RFC 9110, Section 15.5.5",
            summary = "Server tidak dapat menemukan sumber daya target atau endpoint rute yang diminta.",
            clientCause = "Typo pada URL endpoint, path URL salah, ID entitas tidak ada di database (misal /posts/999999), atau salah metode HTTP.",
            serverCause = "Router backend tidak memiliki handler untuk rute ini, atau query database menghasilkan record null.",
            solutions = listOf(
                "Periksa ketelitian URL dan path rute (contoh: /api/v1/users vs /users).",
                "Pastikan ID entitas yang dicari memang tersimpan di database.",
                "Periksa apakah trailing slash (/ di akhir URL) mempengaruhi routing server."
            )
        ),
        405 to HttpStatusInfo(
            code = 405,
            name = "Method Not Allowed",
            category = "4xx Kesalahan Klien",
            rfcStandard = "RFC 9110, Section 15.5.6",
            summary = "Metode HTTP yang digunakan (GET, POST, PUT, DELETE, dll.) tidak didukung oleh rute endpoint ini.",
            clientCause = "Mengirim POST ke endpoint yang hanya menerima GET, atau sebaliknya.",
            serverCause = "Server mengenali endpoint tetapi tidak mengizinkan HTTP method tersebut. Server biasanya menyertakan header 'Allow'.",
            solutions = listOf(
                "Periksa dokumentasi API untuk metode HTTP yang benar pada endpoint terkait.",
                "Periksa header 'Allow' pada response headers untuk melihat metode apa saja yang diizinkan.",
                "Ubah HTTP Method di pemilih metode PocketAPI."
            )
        ),
        408 to HttpStatusInfo(
            code = 408,
            name = "Request Timeout",
            category = "4xx Kesalahan Klien",
            rfcStandard = "RFC 9110, Section 15.5.9",
            summary = "Server memutuskan koneksi karena waktu tunggu klien untuk mengirim request melebihi batas waktu (timeout).",
            clientCause = "Koneksi internet lambat atau terputus saat payload request yang besar sedang diunggah.",
            serverCause = "Web server (Nginx/Apache) memiliki batas timeout untuk menerima body request.",
            solutions = listOf(
                "Periksa kestabilan koneksi internet perangkat Anda.",
                "Kurangi ukuran payload atau gunakan kompresi jika payload sangat besar."
            )
        ),
        409 to HttpStatusInfo(
            code = 409,
            name = "Conflict",
            category = "4xx Kesalahan Klien",
            rfcStandard = "RFC 9110, Section 15.5.10",
            summary = "Permintaan tidak dapat diproses karena terjadi konflik dengan status sumber daya saat ini di server.",
            clientCause = "Mencoba mendaftar dengan email/username yang sudah terdaftar, atau konflik versi data (optimistic locking concurrency).",
            serverCause = "Database mendeteksi pelanggaran Unique Constraint atau status state machine yang tidak valid.",
            solutions = listOf(
                "Gunakan nilai unik yang belum pernah didaftarkan (misal email atau username baru).",
                "Ambil data terbaru (GET) terlebih dahulu sebelum mengirim perubahan (PUT/PATCH)."
            )
        ),
        415 to HttpStatusInfo(
            code = 415,
            name = "Unsupported Media Type",
            category = "4xx Kesalahan Klien",
            rfcStandard = "RFC 9110, Section 15.5.16",
            summary = "Format payload atau tipe media yang dikirim tidak didukung oleh endpoint server.",
            clientCause = "Header 'Content-Type' tidak disetel, atau mengirim JSON sementara server mengharapkan multipart/form-data atau x-www-form-urlencoded.",
            serverCause = "Content negotiation parser di backend menolak MIME type pada header Content-Type.",
            solutions = listOf(
                "Pastikan header 'Content-Type: application/json' ditambahkan pada Tab Headers jika mengirim JSON.",
                "Periksa spesifikasi endpoint apakah memerlukan format form-data atau urlencoded."
            )
        ),
        422 to HttpStatusInfo(
            code = 422,
            name = "Unprocessable Entity",
            category = "4xx Kesalahan Klien",
            rfcStandard = "RFC 9110, Section 15.5.21",
            summary = "Sintaks request valid (JSON dapat di-parse), namun data payload mengandung kesalahan semantik atau gagal validasi bisnis.",
            clientCause = "Validasi form gagal: password terlalu pendek, format email tidak valid, atau nilai angka melebihi batas minimum/maksimum.",
            serverCause = "Validation library backend (seperti Laravel Validator, Joi, Zod, Hibernate Validator) menolak input.",
            solutions = listOf(
                "Baca rincian field error pada JSON response body untuk melihat field mana yang ditolak.",
                "Perbaiki nilai data sesuai aturan validasi bisnis server."
            )
        ),
        429 to HttpStatusInfo(
            code = 429,
            name = "Too Many Requests",
            category = "4xx Kesalahan Klien",
            rfcStandard = "RFC 6585, Section 4",
            summary = "Klien telah mengirim terlalu banyak permintaan dalam kurun waktu tertentu (rate limiting).",
            clientCause = "Klien memanggil API secara berulang dalam interval waktu yang sangat singkat.",
            serverCause = "Rate limiter server (Redis, Cloudflare, API Gateway) aktif untuk melindungi sistem dari spam atau overload.",
            solutions = listOf(
                "Periksa header 'Retry-After' untuk mengetahui berapa detik harus menunggu sebelum mencoba kembali.",
                "Terapkan jeda (debounce/throttle) pada pemanggilan request di aplikasi klien."
            )
        ),
        500 to HttpStatusInfo(
            code = 500,
            name = "Internal Server Error",
            category = "5xx Kesalahan Server",
            rfcStandard = "RFC 9110, Section 15.6.1",
            summary = "Server mengalami kegagalan internal atau unhandled exception saat memproses permintaan.",
            clientCause = "Biasanya bukan kesalahan langsung klien, meskipun input tidak terduga bisa memicu bug di server jika backend tidak menangani edge case.",
            serverCause = "Terjadi uncaught exception, NullPointerException, query database crash, atau bug logic pada kode backend.",
            solutions = listOf(
                "Periksa log server backend (error trace, stack trace) untuk menemukan baris kode yang melempar exception.",
                "Laporkan issue ini kepada pengembang backend dengan menyertakan payload request dan timestamp.",
                "Pastikan backend memiliki error handling yang rapi."
            )
        ),
        502 to HttpStatusInfo(
            code = 502,
            name = "Bad Gateway",
            category = "5xx Kesalahan Server",
            rfcStandard = "RFC 9110, Section 15.6.3",
            summary = "Server yang bertindak sebagai gateway atau reverse proxy menerima respons yang tidak valid dari server upstream.",
            clientCause = "Klien terhubung ke Reverse Proxy (seperti Nginx, Cloudflare), tetapi aplikasi backend di baliknya bermasalah.",
            serverCause = "Aplikasi backend (Node.js, Spring Boot, FastAPI, Go) sedang mati, crash, atau port tidak dapat dihubungi oleh Nginx.",
            solutions = listOf(
                "Periksa apakah proses daemon aplikasi backend aktif dan berjalan di port yang benar.",
                "Cek log reverse proxy (misal /var/log/nginx/error.log) untuk melihat upstream connection refused.",
                "Pastikan firewall server tidak memblokir komunikasi antar port internal."
            )
        ),
        503 to HttpStatusInfo(
            code = 503,
            name = "Service Unavailable",
            category = "5xx Kesalahan Server",
            rfcStandard = "RFC 9110, Section 15.6.4",
            summary = "Server saat ini sedang tidak dapat menangani permintaan karena pemeliharaan (maintenance) atau kelebihan beban (overloaded).",
            clientCause = "Klien mengakses server saat server sedang restart atau under maintenance.",
            serverCause = "Server sedang mengalami traffic spike yang sangat tinggi, CPU/RAM 100%, atau sengaja dinonaktifkan dalam mode maintenance.",
            solutions = listOf(
                "Tunggu beberapa saat dan lakukan retry secara berkala.",
                "Periksa dashboard monitoring server untuk status penggunaan CPU, RAM, dan koneksi database pool.",
                "Periksa header 'Retry-After' jika server mengindikasikan estimasi waktu perbaikan."
            )
        ),
        504 to HttpStatusInfo(
            code = 504,
            name = "Gateway Timeout",
            category = "5xx Kesalahan Server",
            rfcStandard = "RFC 9110, Section 15.6.5",
            summary = "Server proxy/gateway tidak menerima respons tepat waktu dari server backend upstream.",
            clientCause = "Permintaan memicu proses backend yang memakan waktu terlalu lama melebihi batas proxy timeout.",
            serverCause = "Query database lambat (slow query), query mengunci tabel (deadlock), atau third-party API lambat merespons backend.",
            solutions = listOf(
                "Optimalkan query database (tambahkan indeks, batasi LIMIT data).",
                "Tingkatkan nilai proxy_read_timeout pada Nginx jika proses memang membutuhkan waktu pemrosesan yang wajar.",
                "Pindahkan proses komputasi berat ke asynchronous queue (antrean latar belakang)."
            )
        )
    )

    fun getStatusInfo(statusCode: Int, statusMessage: String = ""): HttpStatusInfo {
        statusDatabase[statusCode]?.let { return it }

        // Fallback for codes not explicitly listed
        val category = when (statusCode) {
            in 100..199 -> "1xx Informasi"
            in 200..299 -> "2xx Sukses"
            in 300..399 -> "3xx Pengalihan"
            in 400..499 -> "4xx Kesalahan Klien"
            in 500..599 -> "5xx Kesalahan Server"
            else -> "Kesalahan Jaringan / Protokol"
        }

        val name = if (statusMessage.isNotBlank()) statusMessage else when (statusCode) {
            in 200..299 -> "Success"
            in 300..399 -> "Redirect"
            in 400..499 -> "Client Error"
            in 500..599 -> "Server Error"
            else -> "Network Error"
        }

        val summary = when (statusCode) {
            in 100..199 -> "Respons sementara dari server sebelum respons final dikirimkan."
            in 200..299 -> "Permintaan berhasil diterima, dipahami, dan diproses oleh server."
            in 300..399 -> "Tindakan lebih lanjut perlu diambil oleh klien untuk menyelesaikan permintaan."
            in 400..499 -> "Permintaan mengandung kesalahan sintaks atau tidak dapat diproses oleh server."
            in 500..599 -> "Server mengalami masalah internal saat berusaha memenuhi permintaan."
            else -> "Terjadi kendala jaringan, koneksi ditolak, atau host tidak dapat ditemukan."
        }

        val clientCause = when (statusCode) {
            in 400..499 -> "Periksa parameter, header, atau kredensial yang dikirim dalam request."
            else -> "Klien mengirim request ke server tujuan."
        }

        val serverCause = when (statusCode) {
            in 500..599 -> "Server mengalami gangguan atau overload saat memproses permintaan."
            else -> "Server memproses permintaan sesuai spesifikasi HTTP."
        }

        val solutions = when (statusCode) {
            in 400..499 -> listOf(
                "Periksa dokumentasi endpoint untuk spesifikasi format parameter dan headers.",
                "Periksa response body untuk pesan error lebih detail dari server."
            )
            in 500..599 -> listOf(
                "Periksa log backend server untuk error trace.",
                "Coba kembali setelah beberapa saat atau hubungi tim backend."
            )
            else -> listOf(
                "Pastikan koneksi internet stabil dan URL endpoint benar.",
                "Periksa apakah host dan port dapat diakses."
            )
        }

        return HttpStatusInfo(
            code = statusCode,
            name = name,
            category = category,
            rfcStandard = "RFC 9110 HTTP Semantics",
            summary = summary,
            clientCause = clientCause,
            serverCause = serverCause,
            solutions = solutions
        )
    }
}
