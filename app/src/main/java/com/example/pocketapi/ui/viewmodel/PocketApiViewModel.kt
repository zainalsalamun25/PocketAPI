package com.example.pocketapi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pocketapi.data.local.RequestHistoryEntity
import com.example.pocketapi.data.model.ApiRequest
import com.example.pocketapi.data.model.ApiResponse
import com.example.pocketapi.data.model.HeaderItem
import com.example.pocketapi.data.model.HttpMethod
import com.example.pocketapi.data.repository.ApiRepository
import com.example.pocketapi.util.JsonFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PocketApiUiState(
    val url: String = "https://jsonplaceholder.typicode.com/posts/1",
    val method: HttpMethod = HttpMethod.GET,
    val headers: List<HeaderItem> = listOf(
        HeaderItem(key = "Accept", value = "application/json", isEnabled = true)
    ),
    val requestBody: String = "{\n  \"title\": \"foo\",\n  \"body\": \"bar\",\n  \"userId\": 1\n}",
    val isLoading: Boolean = false,
    val response: ApiResponse? = null,
    val selectedRequestTab: Int = 0,
    val selectedResponseTab: Int = 0
)

class PocketApiViewModel(
    private val repository: ApiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PocketApiUiState())
    val uiState: StateFlow<PocketApiUiState> = _uiState.asStateFlow()

    val historyList: StateFlow<List<RequestHistoryEntity>> = repository.allHistory
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

    fun beautifyRequestBody() {
        val currentBody = _uiState.value.requestBody
        val pretty = JsonFormatter.prettyPrint(currentBody)
        _uiState.update { it.copy(requestBody = pretty) }
    }

    fun sendRequest() {
        val state = _uiState.value
        if (state.url.isBlank()) return

        _uiState.update { it.copy(isLoading = true, response = null) }

        viewModelScope.launch {
            val request = ApiRequest(
                url = state.url,
                method = state.method,
                headers = state.headers,
                body = if (state.method in listOf(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH)) state.requestBody else ""
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

    fun loadPreset(name: String) {
        when (name) {
            "JSONPlaceholder GET" -> {
                _uiState.update {
                    it.copy(
                        url = "https://jsonplaceholder.typicode.com/posts/1",
                        method = HttpMethod.GET,
                        headers = listOf(HeaderItem(key = "Accept", value = "application/json")),
                        requestBody = ""
                    )
                }
            }
            "JSONPlaceholder POST" -> {
                _uiState.update {
                    it.copy(
                        url = "https://jsonplaceholder.typicode.com/posts",
                        method = HttpMethod.POST,
                        headers = listOf(
                            HeaderItem(key = "Content-Type", value = "application/json"),
                            HeaderItem(key = "Accept", value = "application/json")
                        ),
                        requestBody = "{\n  \"title\": \"Belajar Kotlin Modern\",\n  \"body\": \"Membuat PocketAPI Android dengan Jetpack Compose\",\n  \"userId\": 1\n}"
                    )
                }
            }
            "HttpBin IP Check" -> {
                _uiState.update {
                    it.copy(
                        url = "https://httpbin.org/ip",
                        method = HttpMethod.GET,
                        headers = listOf(HeaderItem(key = "Accept", value = "application/json")),
                        requestBody = ""
                    )
                }
            }
            "HttpBin Headers" -> {
                _uiState.update {
                    it.copy(
                        url = "https://httpbin.org/headers",
                        method = HttpMethod.GET,
                        headers = listOf(
                            HeaderItem(key = "User-Agent", value = "PocketAPI-Android/1.0"),
                            HeaderItem(key = "X-Custom-Token", value = "secret-token-12345")
                        ),
                        requestBody = ""
                    )
                }
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