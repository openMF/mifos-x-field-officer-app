/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.util

import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.error_digits_only
import kpt.core.ui.generated.resources.error_field_empty
import kpt.core.ui.generated.resources.error_invalid_number
import kpt.core.ui.generated.resources.error_number_zero
import org.jetbrains.compose.resources.StringResource

/** Validators for form text fields; each returns the error message, or null when the input is valid. */
object TextFieldsValidator {
    /** Requires a whole number greater than zero. */
    fun numberValidator(input: String): StringResource? {
        return when {
            input.isBlank() -> Res.string.error_field_empty
            input.any { !it.isDigit() } -> Res.string.error_digits_only
            input.toIntOrNull() == null -> Res.string.error_invalid_number
            input.toInt() == 0 -> Res.string.error_number_zero
            else -> null
        }
    }
}
