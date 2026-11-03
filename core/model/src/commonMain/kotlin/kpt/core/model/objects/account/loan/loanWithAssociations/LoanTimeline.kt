/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan.loanWithAssociations

/** A loan timeline. */
data class LoanTimeline(
    /** The server id of the loan. */
    val loanId: Int? = null,
    /** The date the record was submitted. */
    val submittedOnDate: List<Int>? = null,
    /** The username of whoever submitted the record. */
    val submittedByUsername: String? = null,
    /** The first name of whoever submitted the record. */
    val submittedByFirstname: String? = null,
    /** The last name of whoever submitted the record. */
    val submittedByLastname: String? = null,
    /** The date the record was approved. */
    val approvedOnDate: List<Int>? = null,
    /** The username of whoever approved the record. */
    val approvedByUsername: String? = null,
    /** The first name of whoever approved the record. */
    val approvedByFirstname: String? = null,
    /** The last name of whoever approved the record. */
    val approvedByLastname: String? = null,
    /** The date the loan is expected to be disbursed. */
    val expectedDisbursementDate: List<Int>? = null,
    /** The date the loan was actually disbursed. */
    val actualDisburseDate: ActualDisbursementDate? = null,
    /** The date the loan was actually disbursed. */
    val actualDisbursementDate: List<Int?>? = null,
    /** The username of whoever disbursed the loan. */
    val disbursedByUsername: String? = null,
    /** The first name of whoever disbursed the loan. */
    val disbursedByFirstname: String? = null,
    /** The last name of whoever disbursed the loan. */
    val disbursedByLastname: String? = null,
    /** The date the record was closed. */
    val closedOnDate: List<Int>? = null,
    /** The date the loan is expected to mature. */
    val expectedMaturityDate: List<Int>? = null,
    /** The date the application was withdrawn. */
    val withdrawnOnDate: List<Int>? = null,
)
