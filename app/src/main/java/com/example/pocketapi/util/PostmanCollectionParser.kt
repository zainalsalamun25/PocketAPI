package com.example.pocketapi.util

import com.example.pocketapi.data.local.SavedRequestEntity
import com.example.pocketapi.data.model.HeaderItem
import com.example.pocketapi.data.model.HttpMethod
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class PostmanImportedRequest(
    val name: String,
    val folderName: String,
    val url: String,
    val method: HttpMethod,
    val headers: List<HeaderItem>,
    val body: String
)

object PostmanCollectionParser {

    private val gson = Gson()
    private val prettyGson = GsonBuilder().setPrettyPrinting().create()

    fun parseCollection(jsonString: String): List<PostmanImportedRequest> {
        val result = mutableListOf<PostmanImportedRequest>()
        val root = JsonParser.parseString(jsonString).asJsonObject

        val collectionName = if (root.has("info")) {
            val infoObj = root.getAsJsonObject("info")
            if (infoObj.has("name")) infoObj.get("name").asString else "Postman Import"
        } else {
            "Postman Import"
        }

        if (root.has("item")) {
            val itemsArray = root.getAsJsonArray("item")
            parseItems(itemsArray, collectionName, result)
        }

        return result
    }

    private fun parseItems(
        itemsArray: JsonArray,
        currentFolder: String,
        result: MutableList<PostmanImportedRequest>
    ) {
        for (element in itemsArray) {
            if (!element.isJsonObject) continue
            val itemObj = element.asJsonObject
            val name = if (itemObj.has("name")) itemObj.get("name").asString else "Untitled Request"

            if (itemObj.has("item")) {
                // Sub-folder item
                val subFolderName = if (currentFolder.isNotBlank()) "$currentFolder / $name" else name
                parseItems(itemObj.getAsJsonArray("item"), subFolderName, result)
            } else if (itemObj.has("request")) {
                // Request item
                val requestObj = itemObj.getAsJsonObject("request")
                val methodStr = if (requestObj.has("method")) requestObj.get("method").asString else "GET"
                val method = HttpMethod.fromString(methodStr)

                val urlStr = parseUrl(requestObj)
                val headersList = parseHeaders(requestObj)
                val bodyStr = parseBody(requestObj)

                result.add(
                    PostmanImportedRequest(
                        name = name,
                        folderName = currentFolder,
                        url = urlStr,
                        method = method,
                        headers = headersList,
                        body = bodyStr
                    )
                )
            }
        }
    }

    private fun parseUrl(requestObj: JsonObject): String {
        if (!requestObj.has("url")) return ""
        val urlElement = requestObj.get("url")
        return when {
            urlElement.isJsonPrimitive -> urlElement.asString
            urlElement.isJsonObject -> {
                val urlObj = urlElement.asJsonObject
                if (urlObj.has("raw")) {
                    urlObj.get("raw").asString
                } else {
                    ""
                }
            }
            else -> ""
        }
    }

    private fun parseHeaders(requestObj: JsonObject): List<HeaderItem> {
        val list = mutableListOf<HeaderItem>()
        if (requestObj.has("header")) {
            val headerElement = requestObj.get("header")
            if (headerElement.isJsonArray) {
                for (h in headerElement.asJsonArray) {
                    if (h.isJsonObject) {
                        val obj = h.asJsonObject
                        val key = if (obj.has("key")) obj.get("key").asString else ""
                        val value = if (obj.has("value")) obj.get("value").asString else ""
                        if (key.isNotBlank()) {
                            list.add(HeaderItem(key = key, value = value, isEnabled = true))
                        }
                    }
                }
            }
        }
        return list
    }

    private fun parseBody(requestObj: JsonObject): String {
        if (!requestObj.has("body")) return ""
        val bodyElement = requestObj.get("body")
        if (!bodyElement.isJsonObject) return ""
        val bodyObj = bodyElement.asJsonObject
        val mode = if (bodyObj.has("mode")) bodyObj.get("mode").asString else ""

        return when (mode) {
            "raw" -> if (bodyObj.has("raw")) bodyObj.get("raw").asString else ""
            else -> ""
        }
    }

    fun exportCollection(
        collectionName: String,
        savedRequests: List<SavedRequestEntity>,
        parseHeaders: (String) -> List<HeaderItem>
    ): String {
        val root = JsonObject()

        val info = JsonObject()
        info.addProperty("name", collectionName.ifBlank { "PocketAPI Collection" })
        info.addProperty("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json")
        root.add("info", info)

        val itemsArray = JsonArray()
        val groupedByFolder = savedRequests.groupBy { it.folderName }

        for ((folderName, requests) in groupedByFolder) {
            val folderObj = JsonObject()
            folderObj.addProperty("name", folderName)

            val folderItems = JsonArray()
            for (req in requests) {
                val reqObj = JsonObject()
                reqObj.addProperty("name", req.name)

                val requestDetail = JsonObject()
                requestDetail.addProperty("method", req.method)

                val headersArray = JsonArray()
                val parsedHeaders = parseHeaders(req.headersJson)
                for (h in parsedHeaders) {
                    if (h.isEnabled && h.key.isNotBlank()) {
                        val headerObj = JsonObject()
                        headerObj.addProperty("key", h.key)
                        headerObj.addProperty("value", h.value)
                        headersArray.add(headerObj)
                    }
                }
                requestDetail.add("header", headersArray)

                if (req.requestBody.isNotBlank()) {
                    val bodyObj = JsonObject()
                    bodyObj.addProperty("mode", "raw")
                    bodyObj.addProperty("raw", req.requestBody)
                    requestDetail.add("body", bodyObj)
                }

                val urlObj = JsonObject()
                urlObj.addProperty("raw", req.url)
                requestDetail.add("url", urlObj)

                reqObj.add("request", requestDetail)
                folderItems.add(reqObj)
            }

            folderObj.add("item", folderItems)
            itemsArray.add(folderObj)
        }

        root.add("item", itemsArray)
        return prettyGson.toJson(root)
    }
}
