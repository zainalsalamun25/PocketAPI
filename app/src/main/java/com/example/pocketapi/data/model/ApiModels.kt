package com.example.pocketapi.data.model

data class ApiRequest(
    val url: String = "https://jsonplaceholder.typicode.com/posts/1",
    val method: HttpMethod = HttpMethod.GET,
    val headers: List<HeaderItem> = emptyList(),
    val body: String = ""
)

data class ApiResponse(
    val statusCode: Int = 0,
    val statusMessage: String = "",
    val responseTimeMs: Long = 0,
    val contentLengthBytes: Long = 0,
    val headers: Map<String, String> = emptyMap(),
    val body: String = "",
    val isSuccess: Boolean = false,
    val errorMessage: String? = null
) {
    val isError: Boolean get() = errorMessage != null || statusCode >= 400 || statusCode == 0
}