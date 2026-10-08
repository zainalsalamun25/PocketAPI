package com.example.pocketapi.data.model

import android.util.Base64

enum class AuthType(val label: String) {
    NONE("No Auth"),
    BEARER("Bearer Token"),
    BASIC("Basic Auth"),
    API_KEY("API Key")
}

enum class ApiKeyLocation(val label: String) {
    HEADER("Header"),
    QUERY_PARAM("Query Parameter")
}

data class AuthConfig(
    val type: AuthType = AuthType.NONE,
    val bearerToken: String = "",
    val basicUsername: String = "",
    val basicPassword: String = "",
    val apiKeyName: String = "X-Api-Key",
    val apiKeyValue: String = "",
    val apiKeyLocation: ApiKeyLocation = ApiKeyLocation.HEADER
) {
    fun resolveHeaders(): List<HeaderItem> {
        return when (type) {
            AuthType.BEARER -> {
                if (bearerToken.isNotBlank()) {
                    listOf(HeaderItem(key = "Authorization", value = "Bearer ${bearerToken.trim()}"))
                } else emptyList()
            }
            AuthType.BASIC -> {
                if (basicUsername.isNotBlank() || basicPassword.isNotBlank()) {
                    val raw = "${basicUsername.trim()}:$basicPassword"
                    val encoded = Base64.encodeToString(raw.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                    listOf(HeaderItem(key = "Authorization", value = "Basic $encoded"))
                } else emptyList()
            }
            AuthType.API_KEY -> {
                if (apiKeyLocation == ApiKeyLocation.HEADER && apiKeyName.isNotBlank() && apiKeyValue.isNotBlank()) {
                    listOf(HeaderItem(key = apiKeyName.trim(), value = apiKeyValue.trim()))
                } else emptyList()
            }
            AuthType.NONE -> emptyList()
        }
    }

    fun resolveUrl(originalUrl: String): String {
        if (type == AuthType.API_KEY && apiKeyLocation == ApiKeyLocation.QUERY_PARAM && apiKeyName.isNotBlank() && apiKeyValue.isNotBlank()) {
            val separator = if (originalUrl.contains("?")) "&" else "?"
            return "$originalUrl$separator${apiKeyName.trim()}=${apiKeyValue.trim()}"
        }
        return originalUrl
    }
}