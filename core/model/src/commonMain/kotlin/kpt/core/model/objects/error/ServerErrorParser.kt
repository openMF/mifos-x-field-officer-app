/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.error

import kotlinx.serialization.json.Json

/**
 * Parses Fineract error responses into [AppError] — the ONE place a server error is decoded.
 *
 * Two sources carry the same JSON body:
 *  - a response read directly ([parse] with the body text), and
 *  - a failed request's exception ([parse] with the throwable). With `expectSuccess` Ktor raises
 *    `ClientRequestException` / `ServerResponseException` whose `message` is the request line
 *    followed by the body: `Client request(POST https://host/…/clients) invalid: 400 Bad Request.
 *    Text: "{…}"`. Showing that `message` showed an officer the URL, the tenant and the raw JSON.
 *
 * Callers rarely need the [AppError] itself — [serverErrorMessage] is the one-call form.
 */
object ServerErrorParser {

    /** The [AppError] in [body] — a bare JSON body, or text that contains one after Ktor's prefix. */
    fun parse(body: String): AppError? {
        val json = extractJsonObject(body) ?: return null
        return runCatching { decoder.decodeFromString(AppError.serializer(), json) }.getOrNull()
            ?.takeIf { it.userMessage != null }
    }

    /**
     * The [AppError] carried by [error] or by one of its causes — a repository that wraps the
     * transport exception does not hide the server's answer. Null for anything that is not a
     * Fineract body: no network, a timeout, a 500 with Spring's body, the app's own exceptions.
     */
    fun parse(error: Throwable?): AppError? =
        generateSequence(error) { it.cause }
            .take(MAX_CAUSE_DEPTH)
            .firstNotNullOfOrNull { cause -> cause.message?.let(::parse) }

    private fun extractJsonObject(text: String): String? {
        val bodyStart = text.indexOf(KTOR_BODY_MARKER).let { if (it < 0) 0 else it }
        val start = text.indexOf('{', bodyStart)
        val end = text.lastIndexOf('}')
        return if (start in 0 until end) text.substring(start, end + 1) else null
    }

    private val decoder = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /** Ktor's separator between the request line and the response body in an exception message. */
    private const val KTOR_BODY_MARKER = "Text: \""

    /** Bounds the cause walk; a cyclic or pathological chain cannot loop. */
    private const val MAX_CAUSE_DEPTH = 5
}

/**
 * The text to show an officer: the per-field [AppError.errors] messages (they say WHAT is wrong —
 * the top-level message is usually just "Validation errors exist."), else the top-level one.
 * Distinct, one per line; null when the response carries no user message at all.
 */
val AppError.userMessage: String?
    get() {
        val fieldMessages = errors.mapNotNull { it.defaultUserMessage.trim().ifEmpty { null } }
        val messages = fieldMessages.ifEmpty { listOfNotNull(defaultUserMessage?.trim()?.ifEmpty { null }) }
        return messages.distinct().joinToString(separator = "\n").ifEmpty { null }
    }

/**
 * The server's user-facing message for this failure, or null when it sent none — in which case
 * show the screen's own generic copy, never this throwable's `message` (a diagnostic, not copy).
 */
fun Throwable?.serverErrorMessage(): String? = ServerErrorParser.parse(this)?.userMessage
