package com.example.pocketapi.util

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object JsonFormatter {
    private val prettyGson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    fun prettyPrint(raw: String): String {
        if (raw.isBlank()) return ""
        val trimmed = raw.trim()
        return try {
            val jsonElement = JsonParser.parseString(trimmed)
            prettyGson.toJson(jsonElement)
        } catch (_: Exception) {
            // If not valid JSON, return as-is
            raw
        }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 0 -> "0 B"
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }

    fun formatTimestamp(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }
}