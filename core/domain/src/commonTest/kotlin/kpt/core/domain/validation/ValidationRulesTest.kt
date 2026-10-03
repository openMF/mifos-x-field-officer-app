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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Locks the seven rules carried over from the retired `core/domain` use-cases (`897ffdac1`).
 *
 * Boundaries are asserted AT the threshold, not near it, and against the published constant rather
 * than a literal — asserting `length == 4` passes for a rule that reads `< 5`, and a test written
 * against a hardcoded 4 gets "fixed" by editing the test when the threshold changes.
 */
class ValidationRulesTest {

    // ─── credentials ────────────────────────────────────────────────────────────

    @Test
    fun blankUsernameIsRequiredNotTooShort() {
        // Distinguishing these matters: "required" and "too short" are different messages.
        assertEquals(
            ValidationResult.Invalid(ValidationFailure.Required),
            CredentialRules.username(""),
        )
    }

    @Test
    fun usernameExactlyAtTheMinimumIsValid() {
        val atThreshold = "a".repeat(CredentialRules.MIN_USERNAME_LENGTH)
        assertTrue(CredentialRules.username(atThreshold).isValid)
    }

    @Test
    fun usernameOneBelowTheMinimumIsTooShort() {
        val below = "a".repeat(CredentialRules.MIN_USERNAME_LENGTH - 1)
        assertEquals(
            ValidationResult.Invalid(
                ValidationFailure.TooShort(CredentialRules.MIN_USERNAME_LENGTH),
            ),
            CredentialRules.username(below),
        )
    }

    @Test
    fun passwordBoundariesMatchItsOwnConstant() {
        val atThreshold = "p".repeat(CredentialRules.MIN_PASSWORD_LENGTH)
        val below = "p".repeat(CredentialRules.MIN_PASSWORD_LENGTH - 1)
        assertTrue(CredentialRules.password(atThreshold).isValid)
        assertTrue(CredentialRules.password(below) is ValidationResult.Invalid)
    }

    @Test
    fun usernameAndPasswordHaveDifferentMinimums() {
        // The original had 4 and 6; collapsing them to one constant would silently relax one rule.
        assertTrue(CredentialRules.MIN_USERNAME_LENGTH != CredentialRules.MIN_PASSWORD_LENGTH)
    }

    // ─── server config ──────────────────────────────────────────────────────────

    @Test
    fun endpointAcceptsAHostnameAndRejectsAUrl() {
        assertTrue(ServerConfigRules.endpoint("demo.mifos.io").isValid)
        // A scheme belongs in the protocol field, so a full URL must not pass as a hostname.
        assertTrue(ServerConfigRules.endpoint("https://demo.mifos.io") is ValidationResult.Invalid)
    }

    @Test
    fun endpointRejectsLeadingAndTrailingHyphenLabels() {
        assertTrue(ServerConfigRules.endpoint("-bad.example").isValid.not())
        assertTrue(ServerConfigRules.endpoint("bad-.example").isValid.not())
        // An interior hyphen is legitimate.
        assertTrue(ServerConfigRules.endpoint("a-b.example").isValid)
    }

    @Test
    fun portAcceptsBothEndsOfTheRangeAndRejectsOutside() {
        assertTrue(ServerConfigRules.port("${ServerConfigRules.PORT_RANGE.first}").isValid)
        assertTrue(ServerConfigRules.port("${ServerConfigRules.PORT_RANGE.last}").isValid)
        assertEquals(
            ValidationResult.Invalid(
                ValidationFailure.PortOutOfRange(ServerConfigRules.PORT_RANGE),
            ),
            ServerConfigRules.port("${ServerConfigRules.PORT_RANGE.last + 1}"),
        )
        assertTrue(ServerConfigRules.port("0") is ValidationResult.Invalid)
    }

    @Test
    fun nonNumericPortIsOutOfRangeNotRequired() {
        // `toIntOrNull()` returning null must not be mistaken for a blank field.
        assertTrue(ServerConfigRules.port("eighty") is ValidationResult.Invalid)
        assertEquals(
            ValidationResult.Invalid(ValidationFailure.Required),
            ServerConfigRules.port("   "),
        )
    }

    @Test
    fun protocolAcceptsOnlyTheTwoSchemesWithSeparator() {
        assertTrue(ServerConfigRules.protocol("http://").isValid)
        assertTrue(ServerConfigRules.protocol("https://").isValid)
        // Missing separator, and a scheme the original did not permit.
        assertTrue(ServerConfigRules.protocol("https") is ValidationResult.Invalid)
        assertTrue(ServerConfigRules.protocol("ftp://") is ValidationResult.Invalid)
    }

    @Test
    fun apiPathDistinguishesLeadingFromTrailingSlash() {
        assertTrue(ServerConfigRules.apiPath("/fineract-provider/api/v1/").isValid)
        assertEquals(
            ValidationResult.Invalid(ValidationFailure.ApiPathMissingLeadingSlash),
            ServerConfigRules.apiPath("fineract-provider/api/v1/"),
        )
        assertEquals(
            ValidationResult.Invalid(ValidationFailure.ApiPathMissingTrailingSlash),
            ServerConfigRules.apiPath("/fineract-provider/api/v1"),
        )
    }

    /**
     * Tenant admits hyphen and underscore — a recorded DEVIATION from the original's
     * `^[a-zA-Z0-9]+$`, which rejected `ServerConfig.DEFAULT.tenant` ("mifos-bank-1") and so made the
     * app unable to save its own shipped configuration. What the rule still guards is path and
     * scheme characters.
     */
    @Test
    fun tenantAdmitsHyphenAndUnderscoreButNotPathOrSchemeCharacters() {
        assertTrue(ServerConfigRules.tenant("default").isValid)
        assertTrue(ServerConfigRules.tenant("tenant1").isValid)
        assertTrue(ServerConfigRules.tenant("mifos-bank-1").isValid, "the shipped DEFAULT must be valid")
        assertTrue(ServerConfigRules.tenant("my_tenant").isValid)
        assertTrue(ServerConfigRules.tenant("my/tenant") is ValidationResult.Invalid)
        assertTrue(ServerConfigRules.tenant("https://t") is ValidationResult.Invalid)
        assertTrue(ServerConfigRules.tenant("my tenant") is ValidationResult.Invalid)
    }

    // ─── aggregation ────────────────────────────────────────────────────────────

    @Test
    fun firstFailureOrNullReportsTheEarliestFailureAndNullWhenAllValid() {
        assertEquals(
            ValidationFailure.Required,
            firstFailureOrNull(
                CredentialRules.username("abcd"),
                CredentialRules.password(""),
            ),
        )
        assertEquals(
            null,
            firstFailureOrNull(
                CredentialRules.username("abcd"),
                CredentialRules.password("abcdef"),
            ),
        )
    }

    @Test
    fun noRuleIsSuspendOrNeedsAResourceEnvironment() {
        // The point of the rewrite: these are callable from a plain unit test with no Compose
        // resource loader present. If a rule regains a `getString` dependency this file stops
        // compiling, which is the intended tripwire.
        assertTrue(CredentialRules.username("abcd").isValid)
        assertTrue(ServerConfigRules.tenant("default").isValid)
    }
}
