package com.example.pocketapi.data.repository

import com.example.pocketapi.data.local.RequestHistoryDao
import com.example.pocketapi.data.local.RequestHistoryEntity
import com.example.pocketapi.data.local.SavedRequestDao
import com.example.pocketapi.data.local.SavedRequestEntity
import com.example.pocketapi.data.model.ApiRequest
import com.example.pocketapi.data.model.ApiResponse
import com.example.pocketapi.data.model.AuthConfig
import com.example.pocketapi.data.model.HeaderItem
import com.example.pocketapi.data.network.HttpEngine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

class ApiRepository(
    private val httpEngine: HttpEngine,
    private val historyDao: RequestHistoryDao,
    private val savedRequestDao: SavedRequestDao
) {
    private val gson = Gson()

    // History flows
    val allHistory: Flow<List<RequestHistoryEntity>> = historyDao.getAllHistory()
    val favoriteHistory: Flow<List<RequestHistoryEntity>> = historyDao.getFavoriteHistory()

    // Saved requests & collections flows
    val allSavedRequests: Flow<List<SavedRequestEntity>> = savedRequestDao.getAllSavedRequests()
    val allFolders: Flow<List<String>> = savedRequestDao.getAllFolders()

    suspend fun executeAndSave(request: ApiRequest, bypassSsl: Boolean = false): ApiResponse {
        val response = httpEngine.executeRequest(request, bypassSsl)

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

    suspend fun saveRequest(
        name: String,
        folderName: String,
        request: ApiRequest,
        authConfig: AuthConfig
    ): Long {
        val headersJson = gson.toJson(request.headers.filter { it.isEnabled && it.key.isNotBlank() })
        val authJson = gson.toJson(authConfig)
        val entity = SavedRequestEntity(
            name = name.ifBlank { "Untitled Request" },
            folderName = folderName.ifBlank { "General" },
            url = request.url,
            method = request.method.name,
            headersJson = headersJson,
            authJson = authJson,
            requestBody = request.body
        )
        return savedRequestDao.insert(entity)
    }

    suspend fun deleteSavedRequest(id: Long) {
        savedRequestDao.deleteById(id)
    }

    suspend fun deleteSavedFolder(folder: String) {
        savedRequestDao.deleteByFolder(folder)
    }

    fun parseHeaders(headersJson: String): List<HeaderItem> {
        return try {
            val type = object : TypeToken<List<HeaderItem>>() {}.type
            gson.fromJson<List<HeaderItem>>(headersJson, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun parseAuthConfig(authJson: String): AuthConfig {
        return try {
            gson.fromJson(authJson, AuthConfig::class.java) ?: AuthConfig()
        } catch (_: Exception) {
            AuthConfig()
        }
    }
}