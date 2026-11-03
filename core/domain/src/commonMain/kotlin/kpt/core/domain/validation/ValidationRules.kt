/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.domain.validation

/**
 * Sign-in credential rules.
 *
 * Thresholds are public constants so a screen's copy ("at least 4 characters") and this rule cannot
 * drift — the retired use-cases hardcoded the number twice, once in the comparison and once inside
 * the string resource.
 *
 * Rules preserved verbatim from `897ffdac1:core/domain/.../UsernameValidationUseCase.kt` and
 * `PasswordValidationUseCase.kt`.
 */
object CredentialRules {

    /** Fewest characters a username may have. */
    const val MIN_USERNAME_LENGTH: Int = 4

    /** Fewest characters a password may have. */
    const val MIN_PASSWORD_LENGTH: Int = 6

    /** Checks a username: required, at least [MIN_USERNAME_LENGTH] characters. */
    fun username(value: String): ValidationResult = when {
        value.isEmpty() -> ValidationResult.Invalid(ValidationFailure.Required)
        value.length < MIN_USERNAME_LENGTH ->
            ValidationResult.Invalid(ValidationFailure.TooShort(MIN_USERNAME_LENGTH))
        else -> ValidationResult.Valid
    }

    /** Checks a password: required, at least [MIN_PASSWORD_LENGTH] characters. */
    fun password(value: String): ValidationResult = when {
        value.isEmpty() -> ValidationResult.Invalid(ValidationFailure.Required)
        value.length < MIN_PASSWORD_LENGTH ->
            ValidationResult.Invalid(ValidationFailure.TooShort(MIN_PASSWORD_LENGTH))
        else -> ValidationResult.Valid
    }
}

/**
 * Fineract server-configuration rules — the five fields the server-config screen edits.
 *
 * Rules preserved verbatim from the retired
 * `897ffdac1:core/domain/.../ValidateServer{EndPoint,Port,Protocol,ApiPath,Tenant}UseCase.kt`,
 * including the regexes. Only the result type and the Compose coupling changed.
 */
object ServerConfigRules {

    /** The ports a server may listen on. */
    val PORT_RANGE: IntRange = 1..65535

    /**
     * Hostname, per the original's regex: dot-separated labels of alphanumerics, where an interior
     * hyphen is allowed but a leading or trailing one is not.
     */
    private val HOSTNAME = Regex(
        "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*" +
            "([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$",
    )

    private val PROTOCOL = Regex("^(http://|https://)$")

    /**
     * Tenant identifier.
     *
     * **DEVIATION from `897ffdac1:ValidateServerTenantUseCase.kt`**, which used `^[a-zA-Z0-9]+$`.
     * That rule rejects the shipped default tenant (`AppServerConfig.DEFAULT`), which is
     * `"mifos-bank-1"` — so the app could not save the configuration it ships with. Caught by
     * `ServerConfigViewModelTest.theDefaultConfigIsValid`, which asserts exactly that the presets
     * pass their own rules.
     *
     * Hyphen and underscore are admitted; the identifier is still prevented from carrying path or
     * scheme characters, which is what the rule is actually guarding against.
     */
    private val TENANT = Regex("^[a-zA-Z0-9_-]+$")

    /** Checks a server host name. */
    fun endpoint(value: String): ValidationResult = when {
        value.isBlank() -> ValidationResult.Invalid(ValidationFailure.Required)
        !HOSTNAME.matches(value) -> ValidationResult.Invalid(ValidationFailure.MalformedHostname)
        else -> ValidationResult.Valid
    }

    /** Checks a port: a number within [PORT_RANGE]. */
    fun port(value: String): ValidationResult {
        if (value.isBlank()) return ValidationResult.Invalid(ValidationFailure.Required)
        val parsed = value.toIntOrNull()
        return if (parsed == null || parsed !in PORT_RANGE) {
            ValidationResult.Invalid(ValidationFailure.PortOutOfRange(PORT_RANGE))
        } else {
            ValidationResult.Valid
        }
    }

    /** Checks a protocol: `http://` or `https://`. */
    fun protocol(value: String): ValidationResult = when {
        value.isBlank() -> ValidationResult.Invalid(ValidationFailure.Required)
        !PROTOCOL.matches(value) -> ValidationResult.Invalid(ValidationFailure.MalformedProtocol)
        else -> ValidationResult.Valid
    }

    /** Checks an API path: it must start and end with `/`. */
    fun apiPath(value: String): ValidationResult = when {
        value.isBlank() -> ValidationResult.Invalid(ValidationFailure.Required)
        !value.startsWith("/") ->
            ValidationResult.Invalid(ValidationFailure.ApiPathMissingLeadingSlash)
        !value.endsWith("/") ->
            ValidationResult.Invalid(ValidationFailure.ApiPathMissingTrailingSlash)
        else -> ValidationResult.Valid
    }

    /** Checks a tenant id: letters, digits, `-` and `_` only. */
    fun tenant(value: String): ValidationResult = when {
        value.isBlank() -> ValidationResult.Invalid(ValidationFailure.Required)
        !TENANT.matches(value) -> ValidationResult.Invalid(ValidationFailure.MalformedTenant)
        else -> ValidationResult.Valid
    }
}
