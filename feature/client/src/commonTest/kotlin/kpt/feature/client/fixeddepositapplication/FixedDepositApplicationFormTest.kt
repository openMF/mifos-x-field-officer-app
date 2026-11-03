/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.fixeddepositapplication

import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The fixed-deposit wizard's pure layer.
 *
 * The cases that matter are the ones the original got wrong: an option resolved by a `-1` index
 * through `get` rather than `getOrNull`, two unguarded numeric conversions, and a Submit two pages
 * past the last required field.
 */
class FixedDepositApplicationFormTest {

    private fun filled() = FixedDepositApplicationState(
        clientId = 44,
        product = FixedDepositOption(3, "Fixed Deposit Standard"),
        depositAmount = "50000",
        depositPeriod = "12",
        depositPeriodType = FixedDepositOption(2, "Months"),
        // 2026-02-18T00:00:00Z
        submissionDateMillis = 1_771_372_800_000L,
    )

    @Test
    fun aFullyFilledApplicationIsValid() {
        assertTrue(filled().validateAll().isValid)
    }

    /**
     * THE CRASH, prevented.
     *
     * The original reached `productOptions?.get(productSelected)` with `productSelected` defaulting
     * to `-1`, which throws rather than reporting a missing field — and `get` was used for exactly
     * two of its eleven option reads while the other nine used `getOrNull`. An unchosen product is
     * now a field error, and the payload carries a null id.
     */
    @Test
    fun anUnchosenProductIsAFieldErrorNotACrash() {
        val state = filled().copy(product = null)
        assertTrue(state.validateAll().product)
        assertNull(state.toPayload().productId)
    }

    /** Same for the deposit-period type, the original's other `get` call. */
    @Test
    fun anUnchosenDepositPeriodTypeIsAFieldErrorNotACrash() {
        val state = filled().copy(depositPeriodType = null)
        assertTrue(state.validateAll().depositPeriodType)
        assertNull(state.toPayload().depositPeriodFrequencyId)
    }

    /**
     * `depositAmount.toDouble()` and `depositPeriod.toInt()` ran unguarded at payload time, so a
     * blank or non-numeric entry threw out of the submit handler. Zero is refused too — a deposit
     * of nothing is not a deposit.
     */
    @Test
    fun theNumericTermsAreValidatedRatherThanParsedBlind() {
        listOf("", "   ", "abc", "0", "-100").forEach { bad ->
            assertTrue(
                filled().copy(depositAmount = bad).validateAll().depositAmount,
                "\"$bad\" should be refused as a deposit amount",
            )
        }
        listOf("", "  ", "x", "0", "-1", "1.5").forEach { bad ->
            assertTrue(
                filled().copy(depositPeriod = bad).validateAll().depositPeriod,
                "\"$bad\" should be refused as a deposit period",
            )
        }
        assertFalse(filled().copy(depositAmount = " 50000.50 ").validateAll().depositAmount)
    }

    /**
     * Submit validates EVERY gated page, not just the one on screen.
     *
     * Submit lives on PREVIEW, two pages past the last required field. Validating only the current
     * page would let an incomplete application through — which is how the original reached its
     * `get(-1)`.
     */
    @Test
    fun submitValidatesPagesTheOfficerHasAlreadyLeft() {
        val onPreview = filled().copy(step = FixedDepositStep.PREVIEW, product = null)
        // The PREVIEW page itself has no required fields…
        assertTrue(onPreview.validate(FixedDepositStep.PREVIEW).isValid)
        // …but the application as a whole is not submittable.
        assertTrue(onPreview.validateAll().product)
    }

    /** Only DETAILS and TERMS gate; the rest must not block forward movement. */
    @Test
    fun onlyTheTwoGatedPagesCarryRequiredFields() {
        val empty = FixedDepositApplicationState()
        assertTrue(empty.validate(FixedDepositStep.SETTINGS).isValid)
        assertTrue(empty.validate(FixedDepositStep.INTEREST).isValid)
        assertTrue(empty.validate(FixedDepositStep.CHARGES).isValid)
        assertTrue(empty.validate(FixedDepositStep.PREVIEW).isValid)
        assertTrue(empty.validate(FixedDepositStep.DETAILS).product)
    }

    @Test
    fun stepsClampAtBothEnds() {
        assertEquals(FixedDepositStep.TERMS, FixedDepositStep.DETAILS.next())
        assertEquals(FixedDepositStep.DETAILS, FixedDepositStep.DETAILS.previous())
        assertEquals(FixedDepositStep.PREVIEW, FixedDepositStep.PREVIEW.next())
        assertEquals(6, FixedDepositStep.entries.size)
    }

    /** The declared format is the one the date was formatted with. */
    @Test
    fun theDeclaredDateFormatIsTheOneTheDateWasFormattedWith() {
        val payload = filled().toPayload()
        assertEquals(
            ApiDateFormatter.formatForApi(
                1_771_372_800_000L,
                FixedDepositApplicationViewModel.SUBMIT_DATE_FORMAT,
            ),
            payload.submittedOnDate,
        )
        assertEquals(
            FixedDepositApplicationViewModel.SUBMIT_DATE_FORMAT.pattern,
            payload.dateFormat,
        )
        // Unchanged from the original's `DateHelper.SHORT_MONTH`.
        assertEquals("dd-MM-yyyy", payload.dateFormat)
    }

    /** The payload carries the CHOSEN ids. */
    @Test
    fun thePayloadCarriesTheChosenIds() {
        val payload = filled().toPayload()
        assertEquals(44, payload.clientId)
        assertEquals(3, payload.productId)
        assertEquals(50000.0, payload.depositAmount)
        assertEquals(12, payload.depositPeriod)
        assertEquals(2, payload.depositPeriodFrequencyId)
        assertEquals("en", payload.locale)
    }

    /**
     * The penal rate is only sent when the penalty APPLIES.
     *
     * A rate with the flag off is a number the server has no rule for, and the field is not even
     * drawn in that state — sending it anyway would be the form contradicting itself.
     */
    @Test
    fun thePenalRateIsSentOnlyWhenThePenaltyApplies() {
        val off = filled().copy(penalInterestApplicable = false, penalInterest = "2.5").toPayload()
        assertFalse(off.preClosurePenalApplicable)
        assertNull(off.preClosurePenalInterest)

        val on = filled().copy(penalInterestApplicable = true, penalInterest = "2.5").toPayload()
        assertTrue(on.preClosurePenalApplicable)
        assertEquals(2.5, on.preClosurePenalInterest)
    }

    /** Optional fields are omitted rather than sent empty or zero. */
    @Test
    fun optionalFieldsAreOmittedWhenBlank() {
        val payload = filled().toPayload()
        assertNull(payload.externalId)
        assertNull(payload.fieldOfficerId)
        assertNull(payload.lockinPeriodFrequency)
        assertNull(payload.lockinPeriodFrequencyType)
        assertNull(payload.interestCompoundingPeriodType)
    }
}
