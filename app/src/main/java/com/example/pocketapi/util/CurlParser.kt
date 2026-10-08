package com.example.pocketapi.util

import com.example.pocketapi.data.model.ApiRequest
import com.example.pocketapi.data.model.HeaderItem
import com.example.pocketapi.data.model.HttpMethod

object CurlParser {

    /**
     * Parses a raw cURL command string into an ApiRequest object.
     * Throws IllegalArgumentException if the command is completely invalid.
     */
    fun parse(rawCurl: String): ApiRequest {
        val cleaned = rawCurl
            .replace("\\\r\n", " ")
            .replace("\\\n", " ")
            .replace("\\\r", " ")
            .replace("^\r\n", " ")
            .replace("^\n", " ")
            .trim()

        if (cleaned.isBlank()) {
            throw IllegalArgumentException("Perintah cURL kosong.")
        }

        val tokens = tokenize(cleaned)
        if (tokens.isEmpty()) {
            throw IllegalArgumentException("Tidak dapat membaca argumen cURL.")
        }

        var startIndex = 0
        if (tokens[0].equals("curl", ignoreCase = true)) {
            startIndex = 1
        }

        var url: String? = null
        var method: HttpMethod? = null
        val headers = mutableListOf<HeaderItem>()
        val bodyBuilder = StringBuilder()

        var i = startIndex
        while (i < tokens.size) {
            val token = tokens[i]
            when {
                token == "-X" || token == "--request" -> {
                    if (i + 1 < tokens.size) {
                        method = HttpMethod.fromString(tokens[i + 1])
                        i += 2
                    } else {
                        i++
                    }
                }
                token == "-H" || token == "--header" -> {
                    if (i + 1 < tokens.size) {
                        val headerStr = tokens[i + 1]
                        val colonIndex = headerStr.indexOf(':')
                        if (colonIndex > 0) {
                            val key = headerStr.substring(0, colonIndex).trim()
                            val value = headerStr.substring(colonIndex + 1).trim()
                            headers.add(HeaderItem(key = key, value = value, isEnabled = true))
                        }
                        i += 2
                    } else {
                        i++
                    }
                }
                token == "-d" || token == "--data" || token == "--data-raw" || token == "--data-binary" || token == "--data-ascii" -> {
                    if (i + 1 < tokens.size) {
                        if (bodyBuilder.isNotEmpty()) {
                            bodyBuilder.append("&")
                        }
                        bodyBuilder.append(tokens[i + 1])
                        i += 2
                    } else {
                        i++
                    }
                }
                token == "--url" -> {
                    if (i + 1 < tokens.size) {
                        url = tokens[i + 1]
                        i += 2
                    } else {
                        i++
                    }
                }
                !token.startsWith("-") && url == null -> {
                    // Positional URL argument
                    url = token
                    i++
                }
                else -> {
                    // Skip unsupported flags/options (e.g. -k, -s, -L, --compressed)
                    i++
                }
            }
        }

        val finalUrl = url?.trim() ?: throw IllegalArgumentException("URL tidak ditemukan di dalam perintah cURL.")
        val bodyString = bodyBuilder.toString()

        // If method wasn't explicitly specified with -X, but body data is present, default to POST
        val finalMethod = method ?: if (bodyString.isNotBlank()) HttpMethod.POST else HttpMethod.GET

        return ApiRequest(
            url = finalUrl,
            method = finalMethod,
            headers = if (headers.isNotEmpty()) headers else listOf(HeaderItem(key = "Accept", value = "application/json")),
            body = bodyString
        )
    }

    private fun tokenize(command: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inSingleQuote = false
        var inDoubleQuote = false
        var isEscaped = false

        for (char in command) {
            if (isEscaped) {
                sb.append(char)
                isEscaped = false
                continue
            }

            if (char == '\\' && !inSingleQuote) {
                isEscaped = true
                continue
            }

            if (char == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote
                continue
            }

            if (char == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote
                continue
            }

            if (char.isWhitespace() && !inSingleQuote && !inDoubleQuote) {
                if (sb.isNotEmpty()) {
                    tokens.add(sb.toString())
                    sb.clear()
                }
                continue
            }

            sb.append(char)
        }

        if (sb.isNotEmpty()) {
            tokens.add(sb.toString())
        }

        return tokens
    }
}