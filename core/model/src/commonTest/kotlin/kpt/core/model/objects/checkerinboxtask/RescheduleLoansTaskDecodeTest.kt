/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.checkerinboxtask

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RescheduleLoansTaskDecodeTest {

    // The app's network JSON (core-base KtorHttpClient), minus the logging-only options.
    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    // The shape of Fineract's `rescheduleloans?command=pending` (LoanRescheduleRequestData): NO
    // `actionName`, and no reason when none was given. Both were required fields, so the whole list
    // failed to decode.
    private val pending = """
        [
          {"id": 4, "loanId": 77, "clientId": 9, "clientName": "Asha Rahman", "loanAccountNumber": "000000077",
           "statusEnum": {"id": 100, "code": "loanStatusType.submitted.and.pending.approval"},
           "rescheduleFromInstallment": 3, "rescheduleFromDate": [2026, 11, 5],
           "rescheduleReasonCodeValue": {"id": 21, "name": "Crop failure", "active": true, "mandatory": false}},
          {"id": 5, "loanId": 78, "clientName": "Bimal Das", "loanAccountNumber": "000000078",
           "rescheduleFromDate": [2026, 12, 1]}
        ]
    """.trimIndent()

    @Test
    fun aPendingListWithoutActionNameOrReasonDecodes() {
        val tasks = json.decodeFromString<List<RescheduleLoansTask>>(pending)
        assertEquals(listOf(77, 78), tasks.map { it.loanId })
        assertEquals("Crop failure", tasks[0].rescheduleReasonCodeValue?.name)
        assertNull(tasks[1].rescheduleReasonCodeValue)
        assertEquals(listOf(2026, 12, 1), tasks[1].rescheduleFromDate)
    }
}
