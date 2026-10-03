/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.utils

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kpt.core.model.objects.error.MifosError

/**
 * Generic function to extract error messages from API responses
 */
suspend fun extractErrorMessage(response: HttpResponse): String {
    val responseText = response.bodyAsText()
    return try {
        val json = Json { ignoreUnknownKeys = true }
        val errorResponse = json.decodeFromString<MifosError>(responseText)
        errorResponse.errors.firstOrNull()?.defaultUserMessage
            ?: errorResponse.defaultUserMessage
            ?: "HTTP ${response.status.value} ${response.status.description}"
    } catch (e: Exception) {
        "HTTP ${response.status.value} ${response.status.description}"
    }
}
