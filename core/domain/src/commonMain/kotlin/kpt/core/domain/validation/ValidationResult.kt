/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.domain.validation

/**
 * Why a value failed validation — a TYPED reason, never a message.
 *
 * The screen maps a failure to a localized string; the rule does not. The surface this replaces got
 * that backwards: each of the seven `*ValidationUseCase` classes in the retired `core/domain`
 * (readable at `897ffdac1`) called `getString(Res.string…)` from `core.domain.generated.resources`,
 * which made every rule `suspend`, welded Compose resources into a layer below the UI, and meant the
 * rule could not be unit tested without a resource environment. Four lines of rule inside fifteen of
 * plumbing.
 *
 * Failures carry the parameters a message needs ([TooShort.minLength], [PortOutOfRange.range]) so the
 * copy can say "at least 4 characters" without the screen duplicating the threshold.
 */
sealed interface ValidationFailure {

    /** The value was blank or empty. */
    data object Required : ValidationFailure

    /** Shorter than [minLength] characters. */
    data class TooShort(val minLength: Int) : ValidationFailure

    /** Not a syntactically valid hostname. */
    data object MalformedHostname : ValidationFailure

    /** Not an integer, or outside [range]. */
    data class PortOutOfRange(val range: IntRange) : ValidationFailure

    /** Not exactly `http://` or `https://`. */
    data object MalformedProtocol : ValidationFailure

    /** An API path must start with `/`. */
    data object ApiPathMissingLeadingSlash : ValidationFailure

    /** An API path must end with `/`. */
    data object ApiPathMissingTrailingSlash : ValidationFailure

    /** A tenant identifier must be alphanumeric. */
    data object MalformedTenant : ValidationFailure
}

/**
 * The outcome of one rule.
 *
 * Replaces the retired `ValidationResult(success: Boolean, message: String?)`: a boolean plus a
 * pre-localized string could not express WHICH rule failed without string matching, and forced the
 * Compose coupling described on [ValidationFailure].
 */
sealed interface ValidationResult {

    data object Valid : ValidationResult

    data class Invalid(val failure: ValidationFailure) : ValidationResult

    val isValid: Boolean get() = this is Valid
}

/** The first failure among [results], or null when all are [ValidationResult.Valid]. */
fun firstFailureOrNull(vararg results: ValidationResult): ValidationFailure? =
    results.filterIsInstance<ValidationResult.Invalid>().firstOrNull()?.failure
