/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.shareapplication

import kpt.core.common.utils.DateFormatPattern
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The share wizard's pure layer: validation, step movement, and the payload.
 *
 * The payload cases are the ones that matter — the original reached it through four `!!` indexes
 * and an unguarded `toInt()`, and declared its date format from a constant separate from the one
 * that formatted the date.
 */
class ShareApplicationFormTest {

    private fun filled() = ShareApplicationState(
        clientId = 31,
        product = ShareOption(7, "Ordinary Shares"),
        savingsAccount = ShareOption(12, "000000012"),
        totalShares = "25",
        externalId = " EXT-9 ",
        // 2026-02-18T00:00:00Z
        submissionDateMillis = 1_771_372_800_000L,
        applicationDateMillis = 1_771_372_800_000L,
    )

    @Test
    fun aFilledDetailsPageIsValid() {
        assertTrue(filled().validate(ShareStep.DETAILS).isValid)
    }

    /** Each required field, alone, blocks the page. */
    @Test
    fun everyRequiredDetailFieldIsRequired() {
        assertTrue(filled().copy(product = null).validate(ShareStep.DETAILS).product)
        assertTrue(filled().copy(savingsAccount = null).validate(ShareStep.DETAILS).savingsAccount)
        assertTrue(filled().copy(totalShares = "").validate(ShareStep.DETAILS).totalShares)
    }

    /**
     * Shares must be a positive WHOLE number.
     *
     * The original called `state.totalShares.toInt()` straight into the payload with no guard, so
     * a blank or non-numeric value threw `NumberFormatException` out of the submit handler — the
     * same shape as the loan wizard's `toFloat()` defect. Zero and negatives are refused too: a
     * share application for zero shares is not an application.
     */
    @Test
    fun sharesMustBeAPositiveWholeNumber() {
        listOf("", "  ", "abc", "0", "-5", "2.5").forEach { bad ->
            assertTrue(
                filled().copy(totalShares = bad).validate(ShareStep.DETAILS).totalShares,
                "\"$bad\" should be refused as a share count",
            )
        }
        assertFalse(filled().copy(totalShares = " 25 ").validate(ShareStep.DETAILS).totalShares)
    }

    /** Only the DETAILS page carries required fields; the others must not block movement. */
    @Test
    fun theLaterPagesHaveNoRequiredFields() {
        val empty = ShareApplicationState()
        assertTrue(empty.validate(ShareStep.TERMS).isValid)
        assertTrue(empty.validate(ShareStep.CHARGES).isValid)
        assertTrue(empty.validate(ShareStep.PREVIEW).isValid)
    }

    @Test
    fun stepsAdvanceAndRetreatWithinBounds() {
        assertEquals(ShareStep.TERMS, ShareStep.DETAILS.next())
        assertEquals(ShareStep.DETAILS, ShareStep.TERMS.previous())
        // The ends clamp rather than wrapping or throwing.
        assertEquals(ShareStep.DETAILS, ShareStep.DETAILS.previous())
        assertEquals(ShareStep.PREVIEW, ShareStep.PREVIEW.next())
    }

    /**
     * THE PAIRING, pinned.
     *
     * The date on the wire must be formatted with the very pattern the payload DECLARES. The
     * original satisfied this by coincidence — `DateHelper.getDateAsStringFromLong` and
     * `DateHelper.SHORT_MONTH` happen to reference the same constant — while a second constant of
     * the same name (`DateFormatPattern.SHORT_MONTH`, `"dd MMM yyyy"`) carries a different format.
     * This asserts the two halves agree by CONSTRUCTION.
     */
    @Test
    fun theDeclaredDateFormatIsTheOneTheDatesWereFormattedWith() {
        val payload = filled().toPayload()
        val expected = ApiDateFormatter.formatForApi(
            1_771_372_800_000L,
            ShareApplicationViewModel.SUBMIT_DATE_FORMAT,
        )
        assertEquals(expected, payload.submittedDate)
        assertEquals(expected, payload.applicationDate)
        assertEquals(ShareApplicationViewModel.SUBMIT_DATE_FORMAT.pattern, payload.dateFormat)
    }

    /**
     * The wire format is UNCHANGED from the original's `DateHelper.SHORT_MONTH`.
     *
     * Restating the value, not just the pairing: the port renamed the source but must not have
     * altered what Fineract receives.
     */
    @Test
    fun theWireFormatIsStillNumericDash() {
        assertEquals("dd-MM-yyyy", ShareApplicationViewModel.SUBMIT_DATE_FORMAT.pattern)
        assertEquals(DateFormatPattern.NUMERIC_DASH, ShareApplicationViewModel.SUBMIT_DATE_FORMAT)
    }

    /** The payload carries the CHOSEN options' ids — never an index into a list. */
    @Test
    fun thePayloadCarriesTheChosenIds() {
        val payload = filled().toPayload()
        assertEquals(31, payload.clientId)
        assertEquals(7, payload.productId)
        assertEquals(12, payload.savingsAccountId)
        assertEquals(25, payload.requestedShares)
    }

    /** Blank optional text is omitted rather than sent as an empty string. */
    @Test
    fun blankOptionalsAreOmitted() {
        val payload = filled().copy(externalId = "   ").toPayload()
        assertNull(payload.externalId)
        assertNull(payload.minimumActivePeriod)
        assertNull(payload.lockinPeriodFrequency)
    }

    /** A filled external id is trimmed. */
    @Test
    fun theExternalIdIsTrimmed() {
        assertEquals("EXT-9", filled().toPayload().externalId)
    }

    /** The optional frequency types travel as ids when chosen, and are absent when not. */
    @Test
    fun theOptionalFrequencyTypesAreOptional() {
        val none = filled().toPayload()
        assertNull(none.minimumActivePeriodFrequencyType)
        assertNull(none.lockinPeriodFrequencyType)

        val chosen = filled().copy(
            minimumActivePeriod = "3",
            minimumActivePeriodType = ShareOption(2, "Months"),
            lockInPeriod = "6",
            lockInPeriodType = ShareOption(2, "Months"),
        ).toPayload()
        assertEquals(3, chosen.minimumActivePeriod)
        assertEquals(2, chosen.minimumActivePeriodFrequencyType)
        assertEquals(6, chosen.lockinPeriodFrequency)
        assertEquals(2, chosen.lockinPeriodFrequencyType)
    }

    /** The locale the original sent, unchanged. */
    @Test
    fun theLocaleIsSent() {
        assertEquals("en", filled().toPayload().locale)
    }
}
