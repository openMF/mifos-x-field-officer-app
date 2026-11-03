/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.account.loan

import kotlinx.serialization.Serializable
/**
 * The wire body for `POST savingsaccounts/{id}?command=approve`.
 *
 * Every field REQUIRED except the note, and [locale]/[dateFormat] un-defaulted deliberately: they
 * used to default to `DateConstants`, which let a caller set [approvedOnDate] without deciding how
 * it would be parsed — and the original's caller passed epoch MILLISECONDS
 * (`approvalDate.toString()`) against a `"dd MMMM yyyy"` format. Fineract parses with the format
 * you declare, so the two are one decision. Built only by
 * `SavingsCommandRepositoryImpl` from `SavingsApprovalInput`, which carries the same contract up to
 * the feature layer.
 */
@Serializable
data class SavingsApproval(
    val approvedOnDate: String,

    val locale: String,

    val dateFormat: String,

    /** A free-text note. */
    val note: String? = null,
)
