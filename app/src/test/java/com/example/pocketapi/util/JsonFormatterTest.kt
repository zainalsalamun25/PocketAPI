package com.example.pocketapi.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonFormatterTest {

    @Test
    fun testPrettyPrintValidJson() {
        val compact = "{\"name\":\"PocketAPI\",\"version\":1}"
        val pretty = JsonFormatter.prettyPrint(compact)

        assertTrue(pretty.contains("\n"))
        assertTrue(pretty.contains("\"name\": \"PocketAPI\""))
        assertTrue(pretty.contains("\"version\": 1"))
    }

    @Test
    fun testPrettyPrintInvalidJsonReturnsRaw() {
        val invalid = "Hello World"
        val result = JsonFormatter.prettyPrint(invalid)

        assertEquals("Hello World", result)
    }

    @Test
    fun testFormatBytes() {
        assertEquals("0 B", JsonFormatter.formatBytes(-5))
        assertEquals("500 B", JsonFormatter.formatBytes(500))
        assertEquals("1.5 KB", JsonFormatter.formatBytes(1536))
        assertEquals("2.00 MB", JsonFormatter.formatBytes(2 * 1024 * 1024))
    }
}
