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

import kpt.core.base.store.submit.draft.FormDraftJson
import kotlin.test.Test
import kotlin.test.assertEquals

/** A client draft must restore the form exactly — a dropped field is typing the officer loses. */
class ClientFormSnapshotTest {

    private val filled = CreateNewClientState(
        texts = mapOf(CreateClientField.FIRST_NAME to "FOTest", CreateClientField.LAST_NAME to "Asha"),
        choices = mapOf(CreateClientField.OFFICE to ClientOption(1, "CENTRAL")),
        dataTableValues = mapOf(DataTableField("DATOS_CONTACTO", "telefono") to "5551234"),
        isActive = true,
        activationDate = 1_793_000_000_000,
        dateOfBirth = 631_152_000_000,
        isAddressActive = true,
    )

    @Test
    fun everyFieldSurvivesTheDraftRoundTrip() {
        val json = FormDraftJson.encode(ClientFormSnapshot.serializer(), ClientFormSnapshot.of(filled))
        val restored = FormDraftJson.decode(ClientFormSnapshot.serializer(), json).applyTo(CreateNewClientState())

        assertEquals(filled, restored)
    }

    @Test
    fun perVisitStateIsNotRestored() {
        val busy = filled.copy(isSubmitting = true, dialog = CreateClientDialog.PhotoOptions)
        val restored = ClientFormSnapshot.of(busy).applyTo(CreateNewClientState())

        assertEquals(false, restored.isSubmitting)
        assertEquals(null, restored.dialog)
    }
}
