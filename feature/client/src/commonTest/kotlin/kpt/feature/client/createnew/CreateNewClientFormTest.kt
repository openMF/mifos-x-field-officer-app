/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.createnew

import kpt.core.model.utils.ApiDateFormatter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Locks the two pure halves of the create-client form — what it refuses, and what it sends.
 *
 * Deliberately NOT a ViewModel test. The ViewModel's reads need
 * `ClientRepository.clientsTemplateStream` and `addressConfigurationStream`, which this port
 * requests and does not own (see the port report); `FakeClientRepository` grows those at the same
 * time. Validation and payload mapping are where the defects this port fixes actually live, and
 * they are testable now.
 */
class CreateNewClientFormTest {

    private val form = CreateClientForm(
        genders = listOf(ClientOption(3, "Female"), ClientOption(2, "Male")),
        clientTypes = listOf(ClientOption(11, "Corporate"), ClientOption(10, "Individual")),
        classifications = listOf(ClientOption(21, "Rural"), ClientOption(20, "Urban")),
        offices = listOf(ClientOption(5, "Bhopal Branch"), ClientOption(1, "Head Office")),
    )

    private val addressOptions = AddressFormOptions(
        types = listOf(ClientOption(31, "Permanent")),
        states = listOf(ClientOption(41, "Madhya Pradesh")),
        countries = listOf(ClientOption(51, "India")),
    )

    private fun filled() = CreateNewClientState(
        texts = mapOf(
            CreateClientField.FIRST_NAME to "Asha",
            CreateClientField.LAST_NAME to "Devi",
        ),
        choices = mapOf(CreateClientField.OFFICE to form.offices[0]),
    )

    @Test
    fun aCompleteFormIsAccepted() {
        assertTrue(filled().validate(hasAddressBlock = false).isEmpty())
    }

    @Test
    fun theThreeMandatoryFieldsAreRequired() {
        val errors = CreateNewClientState().validate(hasAddressBlock = false)

        assertEquals(CreateClientFieldError.Required, errors[CreateClientField.FIRST_NAME])
        assertEquals(CreateClientFieldError.Required, errors[CreateClientField.LAST_NAME])
        // ADDED by the port: `officeId` is mandatory on the payload and the original defaulted it
        // to 0, so an untouched dropdown posted office zero and the server's refusal then replaced
        // the whole form.
        assertEquals(CreateClientFieldError.Required, errors[CreateClientField.OFFICE])
        // Middle name is optional, as in the original.
        assertNull(errors[CreateClientField.MIDDLE_NAME])
    }

    @Test
    fun namesTakeLettersAndSpacesOnly() {
        val state = filled().copy(
            texts = filled().texts + mapOf(
                CreateClientField.FIRST_NAME to "Asha2",
                CreateClientField.MIDDLE_NAME to "O'Brien",
            ),
        )
        val errors = state.validate(hasAddressBlock = false)

        assertEquals(CreateClientFieldError.AlphabetsOnly, errors[CreateClientField.FIRST_NAME])
        // The original's rule, kept verbatim: an apostrophe is refused. Recorded as a gap — see
        // NON_ALPHABETIC's KDoc.
        assertEquals(CreateClientFieldError.AlphabetsOnly, errors[CreateClientField.MIDDLE_NAME])
    }

    @Test
    fun aNameOfNothingButSpacesIsEmpty() {
        val state = filled().copy(
            texts = filled().texts + (CreateClientField.FIRST_NAME to "   "),
        )
        // The original tested `isEmpty()` untrimmed, and spaces pass the alphabetic rule, so
        // `firstname = "   "` reached Fineract.
        assertEquals(
            CreateClientFieldError.Required,
            state.validate(hasAddressBlock = false)[CreateClientField.FIRST_NAME],
        )
    }

    @Test
    fun addressTypeIsRequiredOnlyWhenTheAddressBlockIsOnScreen() {
        // THE defect: the original ran this check unconditionally, so on a tenant with the address
        // module disabled the id stayed 0, validation always failed, and create-client was
        // inoperable — with no visible message, because the screen hosted no snackbar.
        assertNull(filled().validate(hasAddressBlock = false)[CreateClientField.ADDRESS_TYPE])

        assertEquals(
            CreateClientFieldError.Required,
            filled().validate(hasAddressBlock = true)[CreateClientField.ADDRESS_TYPE],
        )
    }

    @Test
    fun anUnparseablePhoneNumberIsRefusedAndABlankOneIsNot() {
        assertNull(filled().validate(hasAddressBlock = false)[CreateClientField.MOBILE_NO])

        val typo = filled().copy(
            texts = filled().texts + (CreateClientField.MOBILE_NO to "call me"),
        )
        // The original dropped it from the payload instead, so the client was created without the
        // number and nothing said so.
        assertEquals(
            CreateClientFieldError.InvalidPhoneNumber,
            typo.validate(hasAddressBlock = false)[CreateClientField.MOBILE_NO],
        )
    }

    @Test
    fun theAndroidPhoneRuleHoldsOnEveryTarget() {
        // Accepted by Android's `isGlobalPhoneNumber`, dropped by the four stub actuals, which
        // allowed digits and `+` only.
        assertTrue("+1 (555) 123-4567".isGlobalPhoneNumber())
        assertTrue("9876543210".isGlobalPhoneNumber())
        assertTrue("+91 98765 43210".isGlobalPhoneNumber())
        assertFalse("call me".isGlobalPhoneNumber())
        // Dialable characters but no digit at all.
        assertFalse("+-- ()".isGlobalPhoneNumber())
        assertFalse("".isGlobalPhoneNumber())
    }

    @Test
    fun theChosenOptionsIdReachesThePayload() {
        val state = filled().copy(
            choices = mapOf(
                // "Bhopal Branch" is FIRST alphabetically and id 5 — the original displayed the
                // sorted list and read `officeList[index].id` out of the unsorted one, so picking
                // the first row stored whichever office sat at position 0 unsorted.
                CreateClientField.OFFICE to form.offices[0],
                CreateClientField.GENDER to form.genders[0],
                CreateClientField.CLIENT_TYPE to form.clientTypes[1],
                CreateClientField.CLASSIFICATION to form.classifications[1],
                CreateClientField.STAFF to ClientOption(77, "Bimal"),
            ),
        )

        val payload = state.toPayload(form, hasAddressBlock = false)

        assertEquals(5, payload.officeId)
        assertEquals(3, payload.genderId)
        assertEquals(10, payload.clientTypeId)
        assertEquals(20, payload.clientClassificationId)
        assertEquals(77, payload.staffId)
    }

    @Test
    fun untouchedDropdownsSendNothing() {
        val payload = filled().toPayload(form, hasAddressBlock = false)

        // Not `0`, which is what the original's `Int = 0` defaults would have meant had its guards
        // not caught them.
        assertNull(payload.genderId)
        assertNull(payload.clientTypeId)
        assertNull(payload.clientClassificationId)
        assertNull(payload.staffId)
        assertNull(payload.middlename)
        assertNull(payload.mobileNo)
        assertNull(payload.externalId)
        // Fineract's legal form for an individual person — the original's literal 1.
        assertEquals(1, payload.legalFormId)
        // The datatable prefill step is not ported; this is the value the original's other branch
        // sent.
        assertNull(payload.datatables)
    }

    @Test
    fun aDateFormatIsDeclaredOnlyAlongsideADate() {
        assertNull(filled().toPayload(form, hasAddressBlock = false).dateFormat)

        val withDob = filled().copy(dateOfBirth = DOB_MILLIS)
        val payload = withDob.toPayload(form, hasAddressBlock = false)
        // The date and the format come from the same helper, so they cannot disagree — the
        // mismatch class eight other screens in this migration carried.
        assertEquals(ApiDateFormatter.DATE_FORMAT, payload.dateFormat)
        assertEquals(ApiDateFormatter.LOCALE, payload.locale)
        assertTrue(payload.dateOfBirth?.isNotBlank() == true)

        // The activation date is sent only when the client is being activated — the original's
        // `if (isActive)`.
        assertNull(payload.activationDate)
        val active = filled().copy(isActive = true, activationDate = DOB_MILLIS)
        assertTrue(active.toPayload(form, hasAddressBlock = false).activationDate?.isNotBlank() == true)
    }

    @Test
    fun theAddressIsSentOnlyWithTheAddressBlock() {
        val state = filled().copy(
            isAddressActive = true,
            texts = filled().texts + mapOf(
                CreateClientField.ADDRESS_LINE_1 to "12 Mill Road",
                CreateClientField.ADDRESS_LINE_2 to "   ",
                CreateClientField.CITY to "Bhopal",
            ),
            choices = filled().choices + mapOf(
                CreateClientField.ADDRESS_TYPE to addressOptions.types[0],
                CreateClientField.STATE_PROVINCE to addressOptions.states[0],
                CreateClientField.COUNTRY to addressOptions.countries[0],
            ),
        )

        assertNull(state.toPayload(form, hasAddressBlock = false).address)

        val address = state.toPayload(form, hasAddressBlock = true).address?.single()
        assertEquals(31, address?.addressTypeId)
        assertEquals(41, address?.stateProvinceId)
        assertEquals(51, address?.countryId)
        assertEquals("12 Mill Road", address?.addressLine1)
        assertEquals("Bhopal", address?.city)
        assertTrue(address?.isActive == true)
        // Blank lines are omitted rather than sent as whitespace — the original's `ifBlank { null }`.
        assertNull(address?.addressLine2)
        assertNull(address?.postalCode)
    }

    @Test
    fun datatablesTravelInThePayloadAndAreAbsentWhenTheTenantHasNone() {
        assertNull(filled().toPayload(form, hasAddressBlock = false).datatables)

        val table = kpt.core.model.datatable.DataTable(
            registeredTableName = "client_extra",
            columnHeaderData = listOf(
                kpt.core.model.datatable.ColumnHeader(
                    dataTableColumnName = "household_size",
                    columnDisplayType = "INTEGER",
                    columnNullable = false,
                ),
            ),
        )
        val withTable = form.copy(dataTables = listOf(table))
        val state = filled().copy(dataTableValues = mapOf(DataTableField("client_extra", "household_size") to "4"))
        // The ORIGINAL sent `datatables = null` even after its datatable form: its list started null.
        val sent = state.toPayload(withTable, hasAddressBlock = false).datatables!!.single()
        assertEquals("client_extra", sent.registeredTableName)
        assertEquals(4L, sent.data["household_size"])
    }

    private companion object {
        /** 1990-06-15T00:00:00Z — any fixed instant; the assertions never read the rendered text. */
        const val DOB_MILLIS = 645_321_600_000L

        /** 2026-10-23T12:00Z — `toPickerMillis` of the template's `[2026, 10, 23]`. */
        const val SERVER_DAY_NOON_UTC = 1_792_756_800_000L
    }

    @Test
    fun activationDefaultsToTheServersBusinessDateNotTheDeviceClock() {
        // Device 2026-10-24 (IST) vs a server still on the 23rd — the template's date is the default.
        val serverDay = CreateClientForm(serverActivationDate = SERVER_DAY_NOON_UTC)
        val unpicked = filled().copy(isActive = true)
        assertEquals(SERVER_DAY_NOON_UTC, unpicked.activationDateOr(serverDay))
        assertEquals(
            ApiDateFormatter.formatForApi(SERVER_DAY_NOON_UTC),
            unpicked.toPayload(serverDay, hasAddressBlock = false).activationDate,
        )
        // The officer's own pick always wins over the default.
        assertEquals(DOB_MILLIS, unpicked.copy(activationDate = DOB_MILLIS).activationDateOr(serverDay))
    }
}
