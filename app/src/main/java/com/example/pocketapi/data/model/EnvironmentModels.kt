package com.example.pocketapi.data.model

import java.util.UUID

data class EnvironmentVariable(
    val id: String = UUID.randomUUID().toString(),
    val key: String = "",
    val value: String = "",
    val isEnabled: Boolean = true
)

data class Environment(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Development",
    val variables: List<EnvironmentVariable> = listOf(
        EnvironmentVariable(key = "baseUrl", value = "https://jsonplaceholder.typicode.com", isEnabled = true),
        EnvironmentVariable(key = "postId", value = "1", isEnabled = true)
    )
)
