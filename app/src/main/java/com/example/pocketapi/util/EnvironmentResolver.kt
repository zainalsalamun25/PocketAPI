package com.example.pocketapi.util

import com.example.pocketapi.data.model.ApiRequest
import com.example.pocketapi.data.model.Environment
import com.example.pocketapi.data.model.HeaderItem
import com.example.pocketapi.data.model.QueryParamItem

object EnvironmentResolver {

    fun resolveString(input: String, environment: Environment?): String {
        if (environment == null || input.isBlank() || environment.variables.isEmpty()) return input
        var resolved = input
        for (variable in environment.variables) {
            if (variable.isEnabled && variable.key.isNotBlank()) {
                val placeholder = "{{${variable.key.trim()}}}"
                resolved = resolved.replace(placeholder, variable.value)
            }
        }
        return resolved
    }

    fun resolveRequest(request: ApiRequest, environment: Environment?): ApiRequest {
        if (environment == null || environment.variables.isEmpty()) return request

        val resolvedUrl = resolveString(request.url, environment)
        val resolvedQueryParams = request.queryParams.map { param ->
            param.copy(
                key = resolveString(param.key, environment),
                value = resolveString(param.value, environment)
            )
        }
        val resolvedHeaders = request.headers.map { header ->
            header.copy(
                key = resolveString(header.key, environment),
                value = resolveString(header.value, environment)
            )
        }
        val resolvedBody = resolveString(request.body, environment)

        return request.copy(
            url = resolvedUrl,
            queryParams = resolvedQueryParams,
            headers = resolvedHeaders,
            body = resolvedBody
        )
    }
}
