package com.example.pocketapi.util

import android.util.Base64
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class DecodedJwt(
    val headerJson: String,
    val payloadJson: String,
    val signature: String,
    val algorithm: String?,
    val subject: String?,
    val issuer: String?,
    val issuedAt: Long?,
    val expiresAt: Long?,
    val isExpired: Boolean,
    val expiryDescription: String
)

object JwtDecoder {

    private val JWT_REGEX = Regex("([a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+\\.[a-zA-Z0-9_-]+)")

    fun isJwt(token: String): Boolean {
        val trimmed = token.trim()
        val parts = trimmed.split(".")
        return parts.size == 3 && parts[0].isNotBlank() && parts[1].isNotBlank() && parts[2].isNotBlank()
    }

    fun findJwtInText(text: String): String? {
        if (text.isBlank()) return null
        val match = JWT_REGEX.find(text.trim()) ?: return null
        val candidate = match.value
        return if (isJwt(candidate)) candidate else null
    }

    fun decode(rawToken: String): Result<DecodedJwt> {
        return try {
            val parts = rawToken.trim().split(".")
            if (parts.size != 3) {
                return Result.failure(IllegalArgumentException("Format JWT tidak valid. JWT harus terdiri dari 3 bagian yang dipisahkan titik."))
            }

            val headerString = decodeBase64Url(parts[0])
            val payloadString = decodeBase64Url(parts[1])
            val signature = parts[2]

            val headerPretty = JsonFormatter.prettyPrint(headerString)
            val payloadPretty = JsonFormatter.prettyPrint(payloadString)

            var algorithm: String? = null
            try {
                val headerObj = JSONObject(headerString)
                algorithm = headerObj.optString("alg", null)
            } catch (_: Exception) {}

            var subject: String? = null
            var issuer: String? = null
            var issuedAt: Long? = null
            var expiresAt: Long? = null
            var isExpired = false
            var expiryDescription = "Tidak ada tanggal kedaluwarsa (exp claim tidak ditemukan)"

            try {
                val payloadObj = JSONObject(payloadString)
                if (payloadObj.has("sub")) subject = payloadObj.optString("sub")
                if (payloadObj.has("iss")) issuer = payloadObj.optString("iss")
                if (payloadObj.has("iat")) issuedAt = payloadObj.optLong("iat") * 1000L

                if (payloadObj.has("exp")) {
                    val expEpochSec = payloadObj.optLong("exp")
                    val expMillis = expEpochSec * 1000L
                    expiresAt = expMillis
                    val now = System.currentTimeMillis()

                    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                    val formattedDate = sdf.format(Date(expMillis))

                    if (expMillis < now) {
                        isExpired = true
                        val diffMillis = now - expMillis
                        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
                        val timeAgo = when {
                            days > 0 -> "$days hari yang lalu"
                            hours > 0 -> "$hours jam yang lalu"
                            else -> "beberapa menit yang lalu"
                        }
                        expiryDescription = "Kedaluwarsa sejak $formattedDate ($timeAgo)"
                    } else {
                        isExpired = false
                        val diffMillis = expMillis - now
                        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
                        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
                        val timeLeft = when {
                            days > 0 -> "$days hari $hours jam lagi"
                            hours > 0 -> "$hours jam $minutes menit lagi"
                            else -> "$minutes menit lagi"
                        }
                        expiryDescription = "Berlaku hingga $formattedDate (Sisa $timeLeft)"
                    }
                }
            } catch (_: Exception) {}

            Result.success(
                DecodedJwt(
                    headerJson = headerPretty,
                    payloadJson = payloadPretty,
                    signature = signature,
                    algorithm = algorithm,
                    subject = subject,
                    issuer = issuer,
                    issuedAt = issuedAt,
                    expiresAt = expiresAt,
                    isExpired = isExpired,
                    expiryDescription = expiryDescription
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun decodeBase64Url(base64Url: String): String {
        val decodedBytes = Base64.decode(base64Url, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        return String(decodedBytes, Charsets.UTF_8)
    }
}