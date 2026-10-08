package com.example.pocketapi.data.model

import java.util.UUID

data class HeaderItem(
    val id: String = UUID.randomUUID().toString(),
    val key: String = "",
    val value: String = "",
    val isEnabled: Boolean = true
)