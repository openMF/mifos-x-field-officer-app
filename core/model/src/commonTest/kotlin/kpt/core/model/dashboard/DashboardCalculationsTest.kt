/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.dashboard

import kpt.core.model.objects.users.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DashboardCalculationsTest {

    private fun user(vararg codes: String) = User(username = "fo", permissions = codes.toList())

    @Test
    fun exactCodeGrants() {
        assertTrue(user("READ_LOAN").can("READ_LOAN"))
        assertFalse(user("READ_LOAN").can("READ_CLIENT"))
    }

    @Test
    fun allFunctionsGrantsEverything() {
        assertTrue(user("ALL_FUNCTIONS").can("CREATE_CLIENT"))
        assertTrue(user("ALL_FUNCTIONS").can("APPROVE_LOAN_CHECKER"))
    }

    @Test
    fun allFunctionsReadGrantsOnlyReads() {
        assertTrue(user("ALL_FUNCTIONS_READ").can("READ_COLLECTIONSHEET"))
        assertFalse(user("ALL_FUNCTIONS_READ").can("CREATE_CLIENT"))
    }

    @Test
    fun checkerSuperUserGrantsAnyChecker() {
        assertTrue(user("CHECKER_SUPER_USER").canAnyChecker())
        assertTrue(user("APPROVE_LOAN_CHECKER").canAnyChecker())
        assertFalse(user("READ_LOAN").canAnyChecker())
    }

    @Test
    fun reportingSuperUserGrantsReports() {
        assertTrue(user("READ_REPORT", "REPORTING_SUPER_USER").canRunReports())
        assertTrue(user("READ_REPORT", "READ_Client Listing").canRunReports())
        assertFalse(user("READ_REPORT").canRunReports())
    }

    private fun loan(
        officer: Long?,
        by: String?,
        outstanding: Double = 0.0,
        overdue: Double = 0.0,
        code: String = "MXN",
    ) =
        DashboardLoan(
            id = 1,
            accountNo = "1",
            clientName = "c",
            loanOfficerId = officer,
            submittedByUsername = by,
            inArrears = overdue > 0,
            principalOutstanding = outstanding,
            principalOverdue = overdue,
            totalOutstanding = outstanding,
            totalOverdue = overdue,
            currencyCode = code,
            currencySymbol = "$",
        )

    @Test
    fun ownLoansFollowStaffLinkElseSubmitter() {
        val loans = listOf(loan(1, "fo"), loan(2, "fo"), loan(null, "other"))
        assertEquals(1, loans.ownedBy(DashboardScope(officeId = 1, staffId = 1, username = "x")).size)
        assertEquals(2, loans.ownedBy(DashboardScope(officeId = 1, staffId = null, username = "fo")).size)
    }

    @Test
    fun portfolioIsPerCurrencyWithParFromPrincipal() {
        val p = portfolioOf(
            activeLoans = listOf(
                loan(1, null, outstanding = 8000.0, overdue = 1000.0),
                loan(1, null, outstanding = 2000.0),
                loan(1, null, outstanding = 500.0, overdue = 500.0, code = "USD"),
            ),
        )
        assertEquals(2, p.byCurrency.size)
        val mxn = p.byCurrency.first { it.currencyCode == "MXN" }
        assertEquals(10000.0, mxn.outstanding)
        assertEquals(1000.0, mxn.overdue)
        assertEquals(0.1, mxn.par)
        assertEquals(2, p.inArrears)
        assertEquals(3, p.activeLoans)
    }

    @Test
    fun parIsNullWithNothingOutstanding() {
        val totals = CurrencyTotals(
            "MXN",
            "$",
            outstanding = 0.0,
            overdue = 0.0,
            principalOutstanding = 0.0,
            principalOverdue = 0.0,
        )
        assertNull(totals.par)
    }
}
