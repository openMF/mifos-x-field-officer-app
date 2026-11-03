/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.network.mifos

import kotlinx.serialization.json.Json
import kpt.core.network.mifos.loan.dto.LoanWithAssociationsDto
import kpt.core.network.mifos.loan.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A GROUP loan, as `loans/{id}?associations=all` returns it — no `clientId`, the owner in `group`.
 * Shape from gateway loan 38 (group MARIPOSAS MONARCAS), 2026-10-25; trimmed to the fields that matter.
 */
class GroupLoanDecodeTest {

    private val json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val groupLoan = """
        {"id": 38, "accountNo": "000000038", "loanProductId": 5,
         "loanProductName": "APOYO COLECTIVO CRÉDITOS A LA PALABRA PARA MUJERES",
         "group": {"id": 2, "accountNo": "000000002", "name": "MARIPOSAS MONARCAS"},
         "principal": 10000.0, "approvedPrincipal": 10000.0, "netDisbursalAmount": 10000.0,
         "termFrequency": 12, "numberOfRepayments": 12, "annualInterestRate": 24.0}
    """

    @Test
    fun aGroupLoanDecodesAndNamesItsGroupAsTheOwner() {
        val loan = json.decodeFromString<LoanWithAssociationsDto>(groupLoan).toDomain()
        assertEquals(38, loan.id)
        assertEquals(0, loan.clientId, "no client on a group loan")
        assertEquals("MARIPOSAS MONARCAS", loan.clientName)
    }
}
