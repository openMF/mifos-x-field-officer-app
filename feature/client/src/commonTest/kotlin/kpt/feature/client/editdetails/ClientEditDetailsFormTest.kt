/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.editdetails

import kpt.core.common.utils.DateFormatPattern
import kpt.core.model.client.Client
import kpt.core.model.client.ClientGender
import kpt.core.model.client.ClientStatus
import kpt.core.model.shared.Timeline
import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The pure half of `clientEditDetails` — the prefill, the validation and the payload builder.
 *
 * These are the three places the original's defects lived, and all three are reachable without a
 * device, a Koin graph or a repository double. The ViewModel's own orchestration is not covered
 * here: `FakeClientRepository` has no `clientsTemplateStream` and `FakeClientCommandRepository`
 * errors on `updateClient`, and both live in `core/data` — see the slice report.
 */
class ClientEditDetailsFormTest {

    private val filled = ClientEditDetailsForm(
        firstName = "Ada",
        lastName = "Lovelace",
        staffId = 7,
        submissionDate = 1_000_000_000_000L,
        activationDate = 1_100_000_000_000L,
    )

    @Test
    fun theDeclaredDateFormatIsTheOneTheDateWasFormattedWith() {
        val payload = filled
            .copy(dateOfBirth = listOf(1990, 5, 15).toEpochMillisOrNull())
            .toPayload(locale = ApiDateFormatter.LOCALE, dateFormat = DateFormatPattern.FULL_MONTH)

        // The whole point of the single-parameter shape: the string on the wire and the
        // instruction for parsing it come from ONE value, so they cannot disagree.
        assertEquals("15 May 1990", payload.dateOfBirth)
        assertEquals(ApiDateFormatter.DATE_FORMAT, payload.dateFormat)
        assertEquals("en", payload.locale)
    }

    @Test
    fun noonAnchoringSurvivesTheRoundTripInAnyTimeZone() {
        // `toEpochMillisOrNull` anchors at NOON local and `formatForApi` reads back in the same
        // zone, so the calendar date is preserved whatever the host offset. The substrate's
        // `DateHelper.getDateAsLongFromList` anchors at midnight UTC, which at any negative offset
        // would return "14 May 1990" here.
        val millis = listOf(1990, 5, 15).toEpochMillisOrNull()
        assertEquals("15 May 1990", millis?.let { ApiDateFormatter.formatForApi(it) })
    }

    @Test
    fun aClientWithNoDateOfBirthGetsNullRatherThanToday() {
        assertNull(emptyList<Int?>().toEpochMillisOrNull())
        assertNull(listOf<Int?>(1990, null, 15).toEpochMillisOrNull())
        assertNull(listOf<Int?>(1990, 5).toEpochMillisOrNull())
        // Month 13 does not exist; the original's helper would also have returned "now".
        assertNull(listOf<Int?>(1990, 13, 15).toEpochMillisOrNull())

        // And the payload then omits it rather than writing today's date as a date of birth.
        val payload = filled
            .toPayload(locale = ApiDateFormatter.LOCALE, dateFormat = DateFormatPattern.FULL_MONTH)
        assertNull(payload.dateOfBirth)
    }

    @Test
    fun readOnlyFieldsAreNotWrittenBack() {
        val payload = filled
            .toPayload(locale = ApiDateFormatter.LOCALE, dateFormat = DateFormatPattern.FULL_MONTH)

        // `active` was ALWAYS false in the original, because the flag backing it was never
        // assigned — so every update declared an active client inactive.
        assertNull(payload.active)
        assertNull(payload.activationDate)
        assertNull(payload.submittedOnDate)
        // Never set by the original either; office changes are `clientTransfer`.
        assertNull(payload.officeId)
        // The client's loan officer IS carried through.
        assertEquals(7, payload.staffId)
    }

    @Test
    fun anUnselectedCodeValueIsOmittedNotSentAsZero() {
        val payload = filled
            .toPayload(locale = ApiDateFormatter.LOCALE, dateFormat = DateFormatPattern.FULL_MONTH)

        assertNull(payload.genderId)
        assertNull(payload.clientTypeId)
        assertNull(payload.clientClassificationId)
        assertNull(payload.legalFormId)

        val chosen = filled
            .copy(gender = EditOption(2, "Female"), legalForm = EditOption(1, "PERSON"))
            .toPayload(locale = ApiDateFormatter.LOCALE, dateFormat = DateFormatPattern.FULL_MONTH)
        assertEquals(2, chosen.genderId)
        assertEquals(1, chosen.legalFormId)
    }

    @Test
    fun everyNameErrorIsReportedAtOnce() {
        // The original's `when` chain stopped at the first failure, so the middle and last names
        // were neither validated nor CLEARED while the first name was empty.
        val errors = ClientEditDetailsForm(
            firstName = "",
            middleName = "M4ry",
            lastName = "O'Hara",
        ).validate()

        assertEquals(FieldError.EMPTY, errors[EditField.FIRST_NAME])
        assertEquals(FieldError.NOT_ALPHABETIC, errors[EditField.MIDDLE_NAME])
        assertEquals(FieldError.NOT_ALPHABETIC, errors[EditField.LAST_NAME])
    }

    @Test
    fun aWhitespaceOnlyNameIsEmpty() {
        // The original checked `isEmpty()` and allowed spaces in its alphabetic rule, so " "
        // passed both and Fineract stored a client whose display name was whitespace.
        val errors = ClientEditDetailsForm(firstName = "   ", lastName = "   ").validate()
        assertEquals(FieldError.EMPTY, errors[EditField.FIRST_NAME])
        assertEquals(FieldError.EMPTY, errors[EditField.LAST_NAME])
    }

    @Test
    fun aMiddleNameIsOptionalButStillHasToBeAlphabetic() {
        assertTrue(ClientEditDetailsForm(firstName = "Ada", lastName = "Lovelace").validate().isEmpty())
        assertEquals(
            FieldError.NOT_ALPHABETIC,
            ClientEditDetailsForm(firstName = "Ada", middleName = "7", lastName = "Lovelace")
                .validate()[EditField.MIDDLE_NAME],
        )
    }

    @Test
    fun aMistypedPhoneNumberIsAnErrorRatherThanASilentDrop() {
        // The original's builder read `if (isGlobalPhoneNumber(mobileNumber)) { ... }` with no
        // else, so a bad number was dropped and the officer was told the update succeeded.
        assertEquals(
            FieldError.NOT_A_PHONE_NUMBER,
            filled.copy(mobileNumber = "call me").validate()[EditField.MOBILE],
        )

        // Android's own `[\+]?[0-9.-]+` rule, which these follow.
        assertTrue(isGlobalPhoneNumber("+919876543210"))
        assertTrue(isGlobalPhoneNumber("044-2345.6789"))
        assertTrue(filled.copy(mobileNumber = "+919876543210").validate().isEmpty())

        // Blank is not an error — it means "no change", which is the original's behaviour.
        assertTrue(filled.copy(mobileNumber = "").validate().isEmpty())
    }

    @Test
    fun aBlankOptionalFieldIsOmittedSoItIsNeverSentAsAnEmptyString() {
        val payload = filled
            .copy(middleName = "  ", emailAddress = "", externalId = "  ")
            .toPayload(locale = ApiDateFormatter.LOCALE, dateFormat = DateFormatPattern.FULL_MONTH)

        assertNull(payload.middlename)
        assertNull(payload.emailAddress)
        assertNull(payload.externalId)
    }

    @Test
    fun theClientPrefillsTheFormAndIdZeroMeansUnset() {
        val prefilled = Client(
            id = 42,
            firstname = "Ada",
            lastname = "Lovelace",
            accountNo = "000000042",
            officeName = "Head Office",
            staffName = "R. Maurya",
            staffId = 7,
            // A code value Fineract never sent: the entity's own default is id 0, which is not a
            // code value, and the original turned it into `genderId = 0` on the next save.
            gender = ClientGender(id = 0, name = null),
            legalForm = ClientStatus(id = 1, value = "PERSON"),
            dateOfBirth = listOf(1815, 12, 10),
            timeline = Timeline(submittedOnDate = listOf(2026, 1, 9)),
        ).asPrefill()

        assertEquals("Ada", prefilled.firstName)
        assertEquals("000000042", prefilled.accountNo)
        assertEquals("Head Office", prefilled.officeName)
        assertEquals(7, prefilled.staffId)
        assertNull(prefilled.gender)
        assertEquals(EditOption(1, "PERSON"), prefilled.legalForm)
        assertEquals(
            "10 December 1815",
            prefilled.dateOfBirth?.let { ApiDateFormatter.formatForApi(it) },
        )
        assertEquals(
            "09 January 2026",
            prefilled.submissionDate?.let { ApiDateFormatter.formatForApi(it) },
        )
        // No activation date on file, and nothing invents one.
        assertNull(prefilled.activationDate)
    }

    @Test
    fun anUnassignedStaffIsNotCarriedThroughAsIdZero() {
        // `Client.staffId` is a non-nullable `Int = 0`, so "unassigned" and "staff 0" are
        // the same value on the wire unless it is filtered here.
        assertNull(Client(id = 42, staffId = 0).asPrefill().staffId)
    }
}
