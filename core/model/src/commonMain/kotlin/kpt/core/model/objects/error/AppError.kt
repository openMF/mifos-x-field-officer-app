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

import kotlinx.serialization.Serializable
/** An error the server returned for a request. */
@Serializable
data class AppError(
    /** The technical error message, for developers. */
    var developerMessage: String? = null,

    /** The HTTP status of the failed request. */
    var httpStatusCode: String? = null,

    /** The message Fineract suggests showing the user. */
    var defaultUserMessage: String? = null,

    /** The translation key of the user message. */
    var userMessageGlobalisationCode: String? = null,

    /** The per-field errors. */
    var errors: List<Errors> = emptyList(),
)
