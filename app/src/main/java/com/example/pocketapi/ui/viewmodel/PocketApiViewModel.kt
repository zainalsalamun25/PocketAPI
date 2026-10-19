package com.example.pocketapi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketapi.data.local.RequestHistoryEntity
import com.example.pocketapi.data.local.SavedRequestEntity
import com.example.pocketapi.data.model.ApiRequest
import com.example.pocketapi.data.model.ApiResponse
import com.example.pocketapi.data.model.AuthConfig
import com.example.pocketapi.data.model.Environment
import com.example.pocketapi.data.model.EnvironmentVariable
import com.example.pocketapi.data.model.HeaderItem
import com.example.pocketapi.data.model.HttpMethod
import com.example.pocketapi.data.model.QueryParamItem
import com.example.pocketapi.data.repository.ApiRepository
import com.example.pocketapi.util.CurlGenerator
import com.example.pocketapi.util.CurlParser
import com.example.pocketapi.util.EnvironmentResolver
import com.example.pocketapi.util.JsonFormatter
import com.example.pocketapi.util.PostmanCollectionParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PocketApiUiState(
    val url: String = "{{baseUrl}}/posts/{{postId}}",
    val method: HttpMethod = HttpMethod.GET,
    val queryParams: List<QueryParamItem> = listOf(
        QueryParamItem(key = "format", value = "json", isEnabled = false)
    ),
    val headers: List<HeaderItem> = listOf(
        HeaderItem(key = "Accept", value = "application/json", isEnabled = true)
    ),
    val authConfig: AuthConfig = AuthConfig(),
    val requestBody: String = "{\n  \"title\": \"foo\",\n  \"body\": \"bar\",\n  \"userId\": 1\n}",
    val environments: List<Environment> = listOf(
        Environment(
            name = "Development",
            variables = listOf(
                EnvironmentVariable(key = "baseUrl", value = "https://jsonplaceholder.typicode.com", isEnabled = true),
                EnvironmentVariable(key = "postId", value = "1", isEnabled = true)
            )
        )
    ),
    val activeEnvironmentId: String? = null,
    val bypassSsl: Boolean = false,
    val isLoading: Boolean = false,
    val response: ApiResponse? = null,
    val selectedRequestTab: Int = 0,
    val selectedResponseTab: Int = 0
)

class PocketApiViewModel(
    private val repository: ApiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PocketApiUiState().let { state ->
            state.copy(activeEnvironmentId = state.environments.firstOrNull()?.id)
        }
    )
    val uiState: StateFlow<PocketApiUiState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<RequestHistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedRequestsList: StateFlow<List<SavedRequestEntity>> = repository.allSavedRequests
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val foldersList: StateFlow<List<String>> = repository.allFolders
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setUrl(url: String) {
        _uiState.update { it.copy(url = url) }
    }

    fun setMethod(method: HttpMethod) {
        _uiState.update { it.copy(method = method) }
    }

    fun setRequestBody(body: String) {
        _uiState.update { it.copy(requestBody = body) }
    }

    fun setRequestTab(tab: Int) {
        _uiState.update { it.copy(selectedRequestTab = tab) }
    }

    fun setResponseTab(tab: Int) {
        _uiState.update { it.copy(selectedResponseTab = tab) }
    }

    // Query Params Handlers
    fun addQueryParam() {
        _uiState.update { current ->
            current.copy(queryParams = current.queryParams + QueryParamItem())
        }
    }

    fun updateQueryParam(index: Int, param: QueryParamItem) {
        _uiState.update { current ->
            val updated = current.queryParams.toMutableList()
            if (index in updated.indices) {
                updated[index] = param
            }
            current.copy(queryParams = updated)
        }
    }

    fun removeQueryParam(index: Int) {
        _uiState.update { current ->
            val updated = current.queryParams.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            current.copy(queryParams = updated)
        }
    }

    // Header Handlers
    fun addHeader() {
        _uiState.update { current ->
            current.copy(headers = current.headers + HeaderItem())
        }
    }

    fun updateHeader(index: Int, header: HeaderItem) {
        _uiState.update { current ->
            val updated = current.headers.toMutableList()
            if (index in updated.indices) {
                updated[index] = header
            }
            current.copy(headers = updated)
        }
    }

    fun removeHeader(index: Int) {
        _uiState.update { current ->
            val updated = current.headers.toMutableList()
            if (index in updated.indices) {
                updated.removeAt(index)
            }
            current.copy(headers = updated)
        }
    }

    // Environment Handlers
    fun setEnvironments(environments: List<Environment>) {
        _uiState.update { it.copy(environments = environments) }
    }

    fun setActiveEnvironment(id: String?) {
        _uiState.update { it.copy(activeEnvironmentId = id) }
    }

    fun beautifyRequestBody() {
        val currentBody = _uiState.value.requestBody
        val pretty = JsonFormatter.prettyPrint(currentBody)
        _uiState.update { it.copy(requestBody = pretty) }
    }

    fun importCurl(rawCurl: String): Result<Unit> {
        return try {
            val parsed = CurlParser.parse(rawCurl)
            _uiState.update {
                it.copy(
                    url = parsed.url,
                    method = parsed.method,
                    headers = parsed.headers,
                    requestBody = parsed.body,
                    selectedRequestTab = if (parsed.body.isNotBlank()) 2 else 0
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setAuthConfig(authConfig: AuthConfig) {
        _uiState.update { it.copy(authConfig = authConfig) }
    }

    fun generateCurlCommand(multiline: Boolean = true): String {
        val state = _uiState.value
        val effectiveUrl = state.authConfig.resolveUrl(state.url)
        val effectiveHeaders = state.headers + state.authConfig.resolveHeaders()
        val rawRequest = ApiRequest(
            url = effectiveUrl,
            method = state.method,
            queryParams = state.queryParams,
            headers = effectiveHeaders,
            body = if (state.method in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)) state.requestBody else ""
        )
        val activeEnv = state.environments.find { it.id == state.activeEnvironmentId }
        val resolvedRequest = EnvironmentResolver.resolveRequest(rawRequest, activeEnv)
        return CurlGenerator.generate(resolvedRequest, multiline)
    }

    fun setBypassSsl(bypass: Boolean) {
        _uiState.update { it.copy(bypassSsl = bypass) }
    }

    fun sendRequest() {
        val state = _uiState.value
        if (state.url.isBlank()) return

        _uiState.update { it.copy(isLoading = true, response = null) }

        viewModelScope.launch {
            val effectiveUrl = state.authConfig.resolveUrl(state.url)
            val effectiveHeaders = state.headers + state.authConfig.resolveHeaders()
            val rawRequest = ApiRequest(
                url = effectiveUrl,
                method = state.method,
                queryParams = state.queryParams,
                headers = effectiveHeaders,
                body = if (state.method in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)) state.requestBody else ""
            )

            val activeEnv = state.environments.find { it.id == state.activeEnvironmentId }
            val resolvedRequest = EnvironmentResolver.resolveRequest(rawRequest, activeEnv)

            val apiResponse = repository.executeAndSave(resolvedRequest, state.bypassSsl)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    response = apiResponse
                )
            }
        }
    }

    fun loadFromHistory(history: RequestHistoryEntity) {
        val headers = repository.parseHeaders(history.headersJson)
        _uiState.update {
            it.copy(
                url = history.url,
                method = HttpMethod.fromString(history.method),
                headers = if (headers.isNotEmpty()) headers else listOf(HeaderItem(key = "Accept", value = "application/json")),
                requestBody = history.requestBody
            )
        }
    }

    fun deleteHistory(id: Long) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun toggleFavorite(id: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, currentStatus)
        }
    }

    fun saveCurrentRequest(name: String, folderName: String) {
        val state = _uiState.value
        viewModelScope.launch {
            val request = ApiRequest(
                url = state.url,
                method = state.method,
                queryParams = state.queryParams,
                headers = state.headers,
                body = if (state.method in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)) state.requestBody else ""
            )
            repository.saveRequest(name, folderName, request, state.authConfig)
        }
    }

    fun importPostmanCollection(jsonString: String): Result<Int> {
        return try {
            val importedList = PostmanCollectionParser.parseCollection(jsonString)
            if (importedList.isEmpty()) {
                return Result.failure(IllegalArgumentException("Tidak ada request valid yang ditemukan dalam file Postman Collection."))
            }

            viewModelScope.launch {
                for (item in importedList) {
                    val request = ApiRequest(
                        url = item.url,
                        method = item.method,
                        headers = item.headers,
                        body = item.body
                    )
                    repository.saveRequest(
                        name = item.name,
                        folderName = item.folderName,
                        request = request,
                        authConfig = AuthConfig()
                    )
                }
            }

            Result.success(importedList.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportPostmanCollection(): String {
        val saved = savedRequestsList.value
        return PostmanCollectionParser.exportCollection(
            collectionName = "PocketAPI Export",
            savedRequests = saved,
            parseHeaders = { json -> repository.parseHeaders(json) }
        )
    }

    fun deleteSavedRequest(id: Long) {
        viewModelScope.launch {
            repository.deleteSavedRequest(id)
        }
    }

    fun deleteSavedFolder(folder: String) {
        viewModelScope.launch {
            repository.deleteSavedFolder(folder)
        }
    }

    fun loadSavedRequest(saved: SavedRequestEntity) {
        val headers = repository.parseHeaders(saved.headersJson)
        val auth = repository.parseAuthConfig(saved.authJson)
        _uiState.update {
            it.copy(
                url = saved.url,
                method = HttpMethod.fromString(saved.method),
                headers = if (headers.isNotEmpty()) headers else listOf(HeaderItem(key = "Accept", value = "application/json")),
                authConfig = auth,
                requestBody = saved.requestBody,
                selectedRequestTab = if (saved.requestBody.isNotBlank()) 2 else 0
            )
        }
    }

    fun runCrudAction(action: String) {
        val targetUrl: String
        val targetMethod: HttpMethod
        val targetHeaders: List<HeaderItem>
        val targetBody: String
        val tab: Int

        when (action) {
            "CREATE" -> {
                targetUrl = "https://jsonplaceholder.typicode.com/posts"
                targetMethod = HttpMethod.POST
                targetHeaders = listOf(
                    HeaderItem(key = "Content-Type", value = "application/json"),
                    HeaderItem(key = "Accept", value = "application/json")
                )
                targetBody = "{\n  \"title\": \"Postingan Baru\",\n  \"body\": \"Dibuat otomatis via 1-klik PocketAPI\",\n  \"userId\": 1\n}"
                tab = 2
            }
            "UPDATE" -> {
                targetUrl = "https://jsonplaceholder.typicode.com/posts/1"
                targetMethod = HttpMethod.PUT
                targetHeaders = listOf(
                    HeaderItem(key = "Content-Type", value = "application/json"),
                    HeaderItem(key = "Accept", value = "application/json")
                )
                targetBody = "{\n  \"id\": 1,\n  \"title\": \"Judul Berhasil Diupdate\",\n  \"body\": \"Data diperbarui via 1-klik PocketAPI\",\n  \"userId\": 1\n}"
                tab = 2
            }
            "DELETE" -> {
                targetUrl = "https://jsonplaceholder.typicode.com/posts/1"
                targetMethod = HttpMethod.DELETE
                targetHeaders = listOf(HeaderItem(key = "Accept", value = "application/json"))
                targetBody = ""
                tab = 0
            }
            "USERS" -> {
                targetUrl = "https://jsonplaceholder.typicode.com/users"
                targetMethod = HttpMethod.GET
                targetHeaders = listOf(HeaderItem(key = "Accept", value = "application/json"))
                targetBody = ""
                tab = 0
            }
            else -> { // READ
                targetUrl = "https://jsonplaceholder.typicode.com/posts/1"
                targetMethod = HttpMethod.GET
                targetHeaders = listOf(HeaderItem(key = "Accept", value = "application/json"))
                targetBody = ""
                tab = 0
            }
        }

        _uiState.update {
            it.copy(
                url = targetUrl,
                method = targetMethod,
                headers = targetHeaders,
                requestBody = targetBody,
                selectedRequestTab = tab,
                isLoading = true,
                response = null
            )
        }

        viewModelScope.launch {
            val request = ApiRequest(
                url = targetUrl,
                method = targetMethod,
                headers = targetHeaders,
                body = targetBody
            )
            val apiResponse = repository.executeAndSave(request)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    response = apiResponse
                )
            }
        }
    }
}

class PocketApiViewModelFactory(
    private val repository: ApiRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PocketApiViewModel::class.java)) {
            return PocketApiViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
