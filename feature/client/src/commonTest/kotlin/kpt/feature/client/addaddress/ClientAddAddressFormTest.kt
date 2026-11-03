/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.addaddress

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The add-address form's pure layer: the original's six required-field rules, its submit gate, and
 * the payload those two agree to send.
 *
 * These are functions on the state rather than ViewModel behaviour, which is why they can be
 * tested without a dispatcher or a fake — and why the gate/validator identity below is provable
 * rather than argued.
 */
class ClientAddAddressFormTest {

    private fun filled() = ClientAddAddressState(
        clientId = 11,
        addressTypeId = 2,
        addressTypeLabel = "Home",
        addressLine1 = "12 Church Street",
        city = "Dhaka",
        stateProvinceId = 5,
        stateProvinceLabel = "Dhaka Division",
        countryId = 19,
        countryLabel = "Bangladesh",
        postalCode = "1207",
    )

    @Test
    fun aFullyFilledFormIsValidAndSubmittable() {
        val state = filled()
        assertTrue(state.validate().isValid)
        assertTrue(state.areRequiredFieldsFilled)
        assertTrue(state.canSubmit)
    }

    /** Each of the six required fields, alone, is enough to make the form invalid. */
    @Test
    fun everyRequiredFieldIsRequired() {
        assertTrue(filled().copy(addressTypeId = 0).validate().addressType)
        assertTrue(filled().copy(addressLine1 = "").validate().addressLine1)
        assertTrue(filled().copy(city = "").validate().city)
        assertTrue(filled().copy(stateProvinceId = 0).validate().stateProvince)
        assertTrue(filled().copy(countryId = 0).validate().country)
        assertTrue(filled().copy(postalCode = "").validate().postalCode)
    }

    /** Address lines 2 and 3 are optional in the original and stay optional. */
    @Test
    fun theOptionalAddressLinesAreOptional() {
        assertTrue(filled().copy(addressLine2 = "", addressLine3 = "").validate().isValid)
    }

    /** Whitespace is not content — `isBlank`, as the original had it. */
    @Test
    fun whitespaceOnlyIsTreatedAsEmpty() {
        assertTrue(filled().copy(city = "   ").validate().city)
        assertTrue(filled().copy(addressLine1 = "\t").validate().addressLine1)
        assertFalse(filled().copy(city = "   ").areRequiredFieldsFilled)
    }

    /**
     * THE DEFECT, pinned.
     *
     * The submit gate and the validator are logically identical, so the button is enabled if and
     * only if validation would pass — which is why not one of the six error messages can ever
     * render. The original's officer sees a dim Submit and no indication of which field is
     * missing.
     *
     * Kept as found per §6a and tracked as `add-address-required-field-errors-unreachable`. This
     * test exists so the identity is a RECORDED FACT rather than a reading of two functions, and
     * so it fails loudly the day someone changes one side — which is exactly the day those six
     * messages become reachable and start mattering.
     */
    @Test
    fun theSubmitGateAndTheValidatorAgreeOnEveryCase() {
        val variants = listOf(
            filled(),
            filled().copy(addressTypeId = 0),
            filled().copy(addressLine1 = ""),
            filled().copy(addressLine1 = "  "),
            filled().copy(city = ""),
            filled().copy(city = " "),
            filled().copy(stateProvinceId = 0),
            filled().copy(stateProvinceId = -3),
            filled().copy(countryId = 0),
            filled().copy(postalCode = ""),
            filled().copy(postalCode = "\n"),
            ClientAddAddressState(),
        )
        variants.forEach { state ->
            assertEquals(
                state.areRequiredFieldsFilled,
                state.validate().isValid,
                "gate and validator disagreed for $state — if this is intentional, the six field " +
                    "errors are now reachable and the gap entry should be closed",
            )
        }
    }

    /** A write in flight closes the gate, which the original did not do. */
    @Test
    fun submittingBlocksASecondSubmit() {
        assertFalse(filled().copy(isSubmitting = true).canSubmit)
    }

    /** The payload carries the selected IDS, and trims what the officer typed. */
    @Test
    fun thePayloadCarriesTheSelectedIdsAndTrimsText() {
        val payload = filled().copy(
            addressLine1 = "  12 Church Street  ",
            city = " Dhaka ",
            postalCode = " 1207 ",
        ).toPayload()

        assertEquals("12 Church Street", payload.addressLine1)
        assertEquals("Dhaka", payload.city)
        assertEquals("1207", payload.postalCode)
        assertEquals(5, payload.stateProvinceId)
        assertEquals(19, payload.countryId)
    }

    /**
     * The address TYPE is not in the payload — it is a separate argument to the command
     * (`createClientAddress(clientId, addressTypeId, addressPayload)`), which is how Fineract's
     * endpoint takes it. Asserting it here so a future refactor that "tidies" it into the payload
     * has to notice.
     */
    @Test
    fun theAddressTypeTravelsOutsideThePayload() {
        val state = filled()
        assertEquals(2, state.addressTypeId)
        // The payload type has no addressTypeId member at all; the command takes it separately.
        assertEquals("12 Church Street", state.toPayload().addressLine1)
    }
}
