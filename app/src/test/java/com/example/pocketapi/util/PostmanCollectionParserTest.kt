package com.example.pocketapi.util

import com.example.pocketapi.data.local.SavedRequestEntity
import com.example.pocketapi.data.model.HeaderItem
import com.example.pocketapi.data.model.HttpMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PostmanCollectionParserTest {

    @Test
    fun testParsePostmanCollectionV21() {
        val json = """
            {
              "info": {
                "name": "Sample API Collection"
              },
              "item": [
                {
                  "name": "Get User Profile",
                  "request": {
                    "method": "GET",
                    "header": [
                      { "key": "Authorization", "value": "Bearer sample_token" }
                    ],
                    "url": { "raw": "https://api.example.com/user/profile" }
                  }
                }
              ]
            }
        """.trimIndent()

        val imported = PostmanCollectionParser.parseCollection(json)

        assertEquals(1, imported.size)
        val req = imported[0]
        assertEquals("Get User Profile", req.name)
        assertEquals("Sample API Collection", req.folderName)
        assertEquals("https://api.example.com/user/profile", req.url)
        assertEquals(HttpMethod.GET, req.method)
        assertEquals(1, req.headers.size)
        assertEquals("Authorization", req.headers[0].key)
        assertEquals("Bearer sample_token", req.headers[0].value)
    }

    @Test
    fun testExportCollection() {
        val savedRequests = listOf(
            SavedRequestEntity(
                id = 1,
                name = "Get Posts",
                folderName = "Blog API",
                url = "https://jsonplaceholder.typicode.com/posts",
                method = "GET",
                headersJson = "[{\"key\":\"Accept\",\"value\":\"application/json\"}]",
                authJson = "{}",
                requestBody = ""
            )
        )

        val exportedJson = PostmanCollectionParser.exportCollection(
            collectionName = "PocketAPI Export",
            savedRequests = savedRequests,
            parseHeaders = { listOf(HeaderItem(key = "Accept", value = "application/json")) }
        )

        assertTrue(exportedJson.contains("PocketAPI Export"))
        assertTrue(exportedJson.contains("Blog API"))
        assertTrue(exportedJson.contains("Get Posts"))
        assertTrue(exportedJson.contains("https://jsonplaceholder.typicode.com/posts"))
    }
}
