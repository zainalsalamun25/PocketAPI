package com.example.pocketapi.util

import com.example.pocketapi.data.model.ApiRequest
import com.example.pocketapi.data.model.HttpMethod

object CurlGenerator {

    /**
     * Converts an ApiRequest into a single-line or multi-line cURL command string.
     */
    fun generate(request: ApiRequest, multiline: Boolean = true): String {
        val separator = if (multiline) " \\\n  " else " "
        val sb = StringBuilder("curl")

        // Method
        sb.append(" -X ").append(request.method.name)

        // URL
        val targetUrl = if (request.url.startsWith("http://", ignoreCase = true) ||
            request.url.startsWith("https://", ignoreCase = true)) {
            request.url.trim()
        } else {
            "https://${request.url.trim()}"
        }
        sb.append(separator).append("\"").append(targetUrl).append("\"")

        // Headers
        for (header in request.headers) {
            if (header.isEnabled && header.key.isNotBlank()) {
                val escapedValue = header.value.replace("\"", "\\\"")
                sb.append(separator)
                    .append("-H \"")
                    .append(header.key.trim())
                    .append(": ")
                    .append(escapedValue)
                    .append("\"")
            }
        }

        // Body
        if (request.method in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH) && request.body.isNotBlank()) {
            val compactBody = request.body.trim().replace("\r\n", "").replace("\n", "")
            val escapedBody = compactBody.replace("\"", "\\\"")
            sb.append(separator)
                .append("-d \"")
                .append(escapedBody)
                .append("\"")
        }

        return sb.toString()
    }
}