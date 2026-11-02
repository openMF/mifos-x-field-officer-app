/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.loan.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * The loan-application template, cached so an officer can start a loan application out of signal.
 *
 * Stored as a serialized payload rather than shredded into columns, for the same reason as
 * [kpt.core.database.recurringdeposit.entity.RecurringDepositTemplateEntity]: the template is a deep
 * read-only projection (product options, charge options, officer options, schedule defaults) that is
 * only ever handed to the application form whole. Columns would buy no query power and would need a
 * migration every time Fineract adds an option field.
 *
 * Keyed by the pair the request is made with — a template is only valid for one (client, product).
 */
@DbEntity
@Entity(tableName = "loan_application_templates", primaryKeys = ["clientId", "productId"])
data class LoanTemplateCacheEntity(
    val clientId: Int,
    /** `-1` when the caller asked for the product-less template. */
    val productId: Int,
    val payload: String,
    val fetchedAtMs: Long,
)

/**
 * The disbursement template for one loan — what the disburse form needs (expected amounts, payment
 * types, dates). Cached so a disbursement can be prepared offline and queued.
 */
@DbEntity
@Entity(tableName = "loan_disburse_templates")
data class LoanDisburseTemplateCacheEntity(
    @PrimaryKey
    val loanId: Int,
    val payload: String,
    val fetchedAtMs: Long,
)
