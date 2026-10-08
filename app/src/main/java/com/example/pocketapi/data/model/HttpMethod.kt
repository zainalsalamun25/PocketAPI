package com.example.pocketapi.data.model

import androidx.compose.ui.graphics.Color

enum class HttpMethod(val color: Color) {
    GET(Color(0xFF2E7D32)),      // Green
    POST(Color(0xFFE65100)),     // Deep Orange
    PUT(Color(0xFF1565C0)),      // Blue
    DELETE(Color(0xFFC62828)),   // Red
    PATCH(Color(0xFF6A1B9A)),    // Purple
    HEAD(Color(0xFF455A64));     // Blue Grey

    companion object {
        fun fromString(name: String): HttpMethod {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: GET
        }
    }
}