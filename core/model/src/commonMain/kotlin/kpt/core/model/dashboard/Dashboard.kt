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

import kotlinx.serialization.Serializable
import kpt.core.model.objects.users.User

/**
 * Whose book the dashboard shows. A user linked to a staff record sees their own portfolio; an
 * unlinked user (the gateway demo user) sees their office, and "own" loans fall back to the ones
 * they submitted.
 */
data class DashboardScope(val officeId: Long, val staffId: Long?, val username: String)

/** The signed-in user's own Fineract record — the fields the dashboard and Profile read. */
@Serializable
data class DashboardUser(
    val id: Long = 0,
    val firstname: String? = null,
    val lastname: String? = null,
    val officeId: Long = 0,
    val officeName: String? = null,
    val staffId: Long? = null,
    val staffName: String? = null,
    val email: String? = null,
    /** `selectedRoles[].name` — the session saved at sign-in does not carry roles. */
    val roles: List<String> = emptyList(),
)

/** One loan as the dashboard needs it: ownership, arrears and the summary sums. */
@Serializable
data class DashboardLoan(
    val id: Long,
    val accountNo: String,
    val clientName: String,
    val loanOfficerId: Long?,
    val submittedByUsername: String?,
    val inArrears: Boolean,
    val principalOutstanding: Double,
    val principalOverdue: Double,
    val totalOutstanding: Double,
    val totalOverdue: Double,
    val currencyCode: String,
    val currencySymbol: String,
    val productName: String? = null,
    val clientId: Long? = null,
    /** Requested (or approved) principal — the amount a not-yet-disbursed loan is for. */
    val principal: Double = 0.0,
)

/** One savings account's balance, for the portfolio total. */
@Serializable
data class DashboardSavings(
    val fieldOfficerId: Long?,
    val balance: Double,
    val currencyCode: String,
    val currencySymbol: String,
)

/** Money sums for one currency — never mixed across currencies. */
data class CurrencyTotals(
    val currencyCode: String,
    val currencySymbol: String,
    val outstanding: Double,
    val overdue: Double,
    val principalOutstanding: Double,
    val principalOverdue: Double,
) {
    /** Portfolio at risk: overdue principal over outstanding principal. Null when nothing is outstanding. */
    val par: Double? get() = if (principalOutstanding > 0.0) principalOverdue / principalOutstanding else null
}

data class LoanPortfolio(val activeLoans: Int, val inArrears: Int, val byCurrency: List<CurrencyTotals>)

/**
 * A loan's installment due on the business date, read from its repayment schedule: [due] is what is
 * still outstanding on it, [paid] what has already been paid against it.
 */
@Serializable
data class DueTodayLoan(
    val loanId: Long,
    val clientId: Long?,
    val currencyCode: String,
    val currencySymbol: String,
    val due: Double,
    val paid: Double,
)

/** Loans that belong to [scope]: by loan officer when staff-linked, else by who submitted them. */
fun List<DashboardLoan>.ownedBy(scope: DashboardScope): List<DashboardLoan> =
    if (scope.staffId != null) {
        filter { it.loanOfficerId == scope.staffId }
    } else {
        filter { it.submittedByUsername == scope.username }
    }

/**
 * Computed from the active-loan list, not from a stretchy report: the Portfolio at Risk / Active
 * Loans reports fail on some Fineract databases (BadSqlGrammar on the PostgreSQL sandbox).
 */
fun portfolioOf(activeLoans: List<DashboardLoan>): LoanPortfolio = LoanPortfolio(
    activeLoans = activeLoans.size,
    inArrears = activeLoans.count { it.inArrears },
    byCurrency = activeLoans.groupBy { it.currencyCode }.map { (code, loans) ->
        CurrencyTotals(
            currencyCode = code,
            currencySymbol = loans.first().currencySymbol,
            outstanding = loans.sumOf { it.totalOutstanding },
            overdue = loans.sumOf { it.totalOverdue },
            principalOutstanding = loans.sumOf { it.principalOutstanding },
            principalOverdue = loans.sumOf { it.principalOverdue },
        )
    },
)

// ── Permissions ──────────────────────────────────────────────────────────────────────────────
// One predicate for every gated affordance: a card or action the user cannot use is not shown, so a
// 403 is prevented at the affordance instead of discovered after the tap.

private const val ALL_FUNCTIONS = "ALL_FUNCTIONS"
private const val ALL_FUNCTIONS_READ = "ALL_FUNCTIONS_READ"
private const val CHECKER_SUPER_USER = "CHECKER_SUPER_USER"
private const val REPORTING_SUPER_USER = "REPORTING_SUPER_USER"

fun User.can(code: String): Boolean =
    code in permissions ||
        ALL_FUNCTIONS in permissions ||
        (code.startsWith("READ_") && ALL_FUNCTIONS_READ in permissions) ||
        (code.endsWith("_CHECKER") && CHECKER_SUPER_USER in permissions)

/** Any maker-checker approval right. */
fun User.canAnyChecker(): Boolean =
    ALL_FUNCTIONS in permissions || CHECKER_SUPER_USER in permissions || permissions.any { it.endsWith("_CHECKER") }

/** Running reports needs READ_REPORT plus at least one report read (or the reporting super user). */
fun User.canRunReports(): Boolean =
    can("READ_REPORT") && (
        ALL_FUNCTIONS in permissions ||
            REPORTING_SUPER_USER in permissions ||
            permissions.any { it.startsWith("READ_") && it.contains(' ') }
        )
