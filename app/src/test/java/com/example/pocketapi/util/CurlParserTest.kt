package com.example.pocketapi.util

import com.example.pocketapi.data.model.HttpMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CurlParserTest {

    @Test
    fun testParseSimpleGetCurl() {
        val curl = "curl https://jsonplaceholder.typicode.com/posts/1"
        val request = CurlParser.parse(curl)

        assertEquals("https://jsonplaceholder.typicode.com/posts/1", request.url)
        assertEquals(HttpMethod.GET, request.method)
    }

    @Test
    fun testParsePostCurlWithHeadersAndBody() {
        val curl = """
            curl -X POST https://api.example.com/login \
            -H 'Content-Type: application/json' \
            -H 'Authorization: Bearer token123' \
            -d '{"username":"admin","password":"123"}'
        """.trimIndent()

        val request = CurlParser.parse(curl)

        assertEquals("https://api.example.com/login", request.url)
        assertEquals(HttpMethod.POST, request.method)
        assertEquals(2, request.headers.size)
        assertEquals("Content-Type", request.headers[0].key)
        assertEquals("application/json", request.headers[0].value)
        assertEquals("Authorization", request.headers[1].key)
        assertEquals("Bearer token123", request.headers[1].value)
        assertEquals("{\"username\":\"admin\",\"password\":\"123\"}", request.body)
    }

    @Test
    fun testParseCurlDefaultsToPostWhenDataPresent() {
        val curl = "curl https://httpbin.org/post --data 'name=pocketapi'"
        val request = CurlParser.parse(curl)

        assertEquals("https://httpbin.org/post", request.url)
        assertEquals(HttpMethod.POST, request.method)
        assertEquals("name=pocketapi", request.body)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseEmptyCurlThrowsException() {
        CurlParser.parse("   ")
    }
}
