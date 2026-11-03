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
/** The validation errors of a failed request. */
@Serializable
data class Errors(
    /** The technical error message, for developers. */
    var developerMessage: String = "",

    /** The message Fineract suggests showing the user. */
    var defaultUserMessage: String = "",

    /** The translation key of the user message. */
    var userMessageGlobalisationCode: String = "",

    /** The name of the parameter. */
    var parameterName: String? = null,
)
