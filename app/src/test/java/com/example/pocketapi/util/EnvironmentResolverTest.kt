package com.example.pocketapi.util

import com.example.pocketapi.data.model.ApiRequest
import com.example.pocketapi.data.model.Environment
import com.example.pocketapi.data.model.EnvironmentVariable
import com.example.pocketapi.data.model.HeaderItem
import com.example.pocketapi.data.model.QueryParamItem
import org.junit.Assert.assertEquals
import org.junit.Test

class EnvironmentResolverTest {

    @Test
    fun testResolveStringWithVariables() {
        val env = Environment(
            name = "TestEnv",
            variables = listOf(
                EnvironmentVariable(key = "baseUrl", value = "https://api.example.com", isEnabled = true),
                EnvironmentVariable(key = "userId", value = "42", isEnabled = true),
                EnvironmentVariable(key = "disabledVar", value = "secret", isEnabled = false)
            )
        )

        val input = "{{baseUrl}}/users/{{userId}}?disabled={{disabledVar}}"
        val result = EnvironmentResolver.resolveString(input, env)

        assertEquals("https://api.example.com/users/42?disabled={{disabledVar}}", result)
    }

    @Test
    fun testResolveRequestFull() {
        val env = Environment(
            name = "TestEnv",
            variables = listOf(
                EnvironmentVariable(key = "baseUrl", value = "https://jsonplaceholder.typicode.com", isEnabled = true),
                EnvironmentVariable(key = "token", value = "my-secret-token", isEnabled = true),
                EnvironmentVariable(key = "page", value = "2", isEnabled = true)
            )
        )

        val request = ApiRequest(
            url = "{{baseUrl}}/posts",
            queryParams = listOf(QueryParamItem(key = "page", value = "{{page}}")),
            headers = listOf(HeaderItem(key = "Authorization", value = "Bearer {{token}}")),
            body = "{\"token\": \"{{token}}\"}"
        )

        val resolved = EnvironmentResolver.resolveRequest(request, env)

        assertEquals("https://jsonplaceholder.typicode.com/posts", resolved.url)
        assertEquals("2", resolved.queryParams[0].value)
        assertEquals("Bearer my-secret-token", resolved.headers[0].value)
        assertEquals("{\"token\": \"my-secret-token\"}", resolved.body)
    }
}
