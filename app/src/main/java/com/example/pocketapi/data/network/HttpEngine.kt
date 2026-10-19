package com.example.pocketapi.data.network

import com.example.pocketapi.data.model.ApiRequest
import com.example.pocketapi.data.model.ApiResponse
import com.example.pocketapi.data.model.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class HttpEngine {

    private val standardClient: OkHttpClient = buildClient(bypassSsl = false)
    private val unsafeClient: OkHttpClient = buildClient(bypassSsl = true)

    private fun buildClient(bypassSsl: Boolean): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)

        if (bypassSsl) {
            try {
                val trustAllCerts = arrayOf<TrustManager>(
                    object : X509TrustManager {
                        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                        override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
                    }
                )
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())
                builder.sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                builder.hostnameVerifier { _, _ -> true }
            } catch (_: Exception) {}
        }

        return builder.build()
    }

    suspend fun executeRequest(apiRequest: ApiRequest, bypassSsl: Boolean = false): ApiResponse = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        var sanitizedUrl = apiRequest.url.trim()

        if (!sanitizedUrl.startsWith("http://", ignoreCase = true) &&
            !sanitizedUrl.startsWith("https://", ignoreCase = true)
        ) {
            sanitizedUrl = "https://$sanitizedUrl"
        }

        val httpUrl = sanitizedUrl.toHttpUrlOrNull()
        if (httpUrl != null) {
            val urlBuilder = httpUrl.newBuilder()
            for (param in apiRequest.queryParams) {
                if (param.isEnabled && param.key.isNotBlank()) {
                    urlBuilder.addQueryParameter(param.key.trim(), param.value)
                }
            }
            sanitizedUrl = urlBuilder.build().toString()
        }

        try {
            val requestBuilder = Request.Builder().url(sanitizedUrl)

            // Inject headers
            var hasContentType = false
            for (header in apiRequest.headers) {
                if (header.isEnabled && header.key.isNotBlank()) {
                    requestBuilder.addHeader(header.key.trim(), header.value.trim())
                    if (header.key.equals("Content-Type", ignoreCase = true)) {
                        hasContentType = true
                    }
                }
            }

            // Prepare Request Body
            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody: RequestBody? = when (apiRequest.method) {
                HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH -> {
                    if (!hasContentType) {
                        requestBuilder.addHeader("Content-Type", "application/json")
                    }
                    apiRequest.body.toRequestBody(mediaType)
                }
                HttpMethod.DELETE -> {
                    if (apiRequest.body.isNotBlank()) apiRequest.body.toRequestBody(mediaType) else null
                }
                HttpMethod.GET, HttpMethod.HEAD -> null
            }

            when (apiRequest.method) {
                HttpMethod.GET -> requestBuilder.get()
                HttpMethod.POST -> requestBuilder.post(requestBody ?: "".toRequestBody(mediaType))
                HttpMethod.PUT -> requestBuilder.put(requestBody ?: "".toRequestBody(mediaType))
                HttpMethod.PATCH -> requestBuilder.patch(requestBody ?: "".toRequestBody(mediaType))
                HttpMethod.DELETE -> if (requestBody != null) requestBuilder.delete(requestBody) else requestBuilder.delete()
                HttpMethod.HEAD -> requestBuilder.head()
            }

            val request = requestBuilder.build()
            val activeClient = if (bypassSsl) unsafeClient else standardClient

            activeClient.newCall(request).execute().use { response ->
                val endTime = System.currentTimeMillis()
                val responseTimeMs = endTime - startTime

                val headersMap = mutableMapOf<String, String>()
                for (name in response.headers.names()) {
                    headersMap[name] = response.header(name).orEmpty()
                }

                val responseBodyString = response.body?.string().orEmpty()
                val contentLength = if (response.body?.contentLength() != null && response.body!!.contentLength() >= 0) {
                    response.body!!.contentLength()
                } else {
                    responseBodyString.toByteArray().size.toLong()
                }

                ApiResponse(
                    statusCode = response.code,
                    statusMessage = response.message,
                    responseTimeMs = responseTimeMs,
                    contentLengthBytes = contentLength,
                    headers = headersMap,
                    body = responseBodyString,
                    isSuccess = response.isSuccessful,
                    errorMessage = null
                )
            }
        } catch (e: UnknownHostException) {
            val elapsed = System.currentTimeMillis() - startTime
            ApiResponse(
                statusCode = 0,
                statusMessage = "Host Not Found",
                responseTimeMs = elapsed,
                body = "",
                isSuccess = false,
                errorMessage = "Gagal terhubung: Host '${e.message}' tidak ditemukan. Periksa koneksi internet atau ejaan URL Anda."
            )
        } catch (e: SocketTimeoutException) {
            val elapsed = System.currentTimeMillis() - startTime
            ApiResponse(
                statusCode = 408,
                statusMessage = "Request Timeout",
                responseTimeMs = elapsed,
                body = "",
                isSuccess = false,
                errorMessage = "Request Timeout: Server membutuhkan waktu terlalu lama untuk merespons (lebih dari batas timeout)."
            )
        } catch (e: SSLHandshakeException) {
            val elapsed = System.currentTimeMillis() - startTime
            ApiResponse(
                statusCode = 0,
                statusMessage = "SSL Handshake Error",
                responseTimeMs = elapsed,
                body = "",
                isSuccess = false,
                errorMessage = "Sertifikat SSL bermasalah atau HTTPS tidak valid: ${e.localizedMessage}. Tip: Aktifkan 'Bypass SSL' di opsi jika menggunakan server lokal/self-signed cert."
            )
        } catch (e: IOException) {
            val elapsed = System.currentTimeMillis() - startTime
            ApiResponse(
                statusCode = 0,
                statusMessage = "Network Error",
                responseTimeMs = elapsed,
                body = "",
                isSuccess = false,
                errorMessage = "Terjadi kesalahan jaringan: ${e.localizedMessage ?: "I/O error"}"
            )
        } catch (e: Exception) {
            val elapsed = System.currentTimeMillis() - startTime
            ApiResponse(
                statusCode = 0,
                statusMessage = "Error",
                responseTimeMs = elapsed,
                body = "",
                isSuccess = false,
                errorMessage = "Terjadi error: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }
}
