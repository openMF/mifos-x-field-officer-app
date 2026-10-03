/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.util

import kpt.core.ui.generated.resources.Res
import kpt.core.ui.generated.resources.error_digits_only
import kpt.core.ui.generated.resources.error_field_empty
import kpt.core.ui.generated.resources.error_invalid_characters
import kpt.core.ui.generated.resources.error_invalid_number
import kpt.core.ui.generated.resources.error_number_zero
import org.jetbrains.compose.resources.StringResource

object TextFieldsValidator {

    fun stringValidator(input: String): StringResource? {
        return when {
            input.isBlank() -> Res.string.error_field_empty
            input.any { !it.isLetterOrDigit() && !it.isWhitespace() } -> Res.string.error_invalid_characters
            else -> null // valid
        }
    }

    fun optionalStringValidator(input: String): StringResource? {
        return when {
            input.any { !it.isLetterOrDigit() && !it.isWhitespace() } -> Res.string.error_invalid_characters
            else -> null // valid
        }
    }

    fun numberValidator(input: String): StringResource? {
        return when {
            input.isBlank() -> Res.string.error_field_empty
            input.any { !it.isDigit() } -> Res.string.error_digits_only
            input.toIntOrNull() == null -> Res.string.error_invalid_number
            input.toInt() == 0 -> Res.string.error_number_zero
            else -> null
        }
    }

    fun doubleNumberValidator(input: String): StringResource? {
        return when {
            input.isBlank() -> Res.string.error_field_empty
            input.count { it == '.' } > 1 -> Res.string.error_invalid_number
            input.any { !it.isDigit() && it != '.' } -> Res.string.error_digits_only
            input.toDoubleOrNull() == null -> Res.string.error_invalid_number
            input == "0" || input == "0.0" || input.toDoubleOrNull() == 0.0 -> Res.string.error_number_zero
            else -> null
        }
    }

    fun dropDownEmptyValidator(input: Boolean): StringResource? {
        return when {
            input -> Res.string.error_field_empty
            else -> null
        }
    }
}
