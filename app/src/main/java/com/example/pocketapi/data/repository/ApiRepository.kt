package com.example.pocketapi.data.repository

import com.example.pocketapi.data.local.RequestHistoryDao
import com.example.pocketapi.data.local.RequestHistoryEntity
import com.example.pocketapi.data.model.ApiRequest
import com.example.pocketapi.data.model.ApiResponse
import com.example.pocketapi.data.model.HeaderItem
import com.example.pocketapi.data.network.HttpEngine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

class ApiRepository(
    private val httpEngine: HttpEngine,
    private val historyDao: RequestHistoryDao
) {
    private val gson = Gson()

    val allHistory: Flow<List<RequestHistoryEntity>> = historyDao.getAllHistory()
    val favoriteHistory: Flow<List<RequestHistoryEntity>> = historyDao.getFavoriteHistory()

    suspend fun executeAndSave(request: ApiRequest): ApiResponse {
        val response = httpEngine.executeRequest(request)

        val headersJson = gson.toJson(request.headers.filter { it.isEnabled && it.key.isNotBlank() })
        val historyEntry = RequestHistoryEntity(
            url = request.url,
            method = request.method.name,
            headersJson = headersJson,
            requestBody = request.body,
            statusCode = response.statusCode,
            responseTimeMs = response.responseTimeMs,
            isFavorite = false,
            timestamp = System.currentTimeMillis()
        )
        historyDao.insert(historyEntry)

        return response
    }

    suspend fun deleteHistory(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearHistory() {
        historyDao.clearAll()
    }

    suspend fun toggleFavorite(id: Long, currentStatus: Boolean) {
        historyDao.updateFavorite(id, !currentStatus)
    }

    fun parseHeaders(headersJson: String): List<HeaderItem> {
        return try {
            val type = object : TypeToken<List<HeaderItem>>() {}.type
            gson.fromJson<List<HeaderItem>>(headersJson, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}