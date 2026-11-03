/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.checkerinbox.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * A maker-checker task awaiting approval.
 *
 * Cached so a field officer can REVIEW the pending queue offline; the approve/reject actions
 * themselves stay online-only (D1 — an authorisation decision must reach Fineract to be real).
 */
@DbEntity
@Entity(tableName = "checker_tasks")
data class CheckerTaskEntity(
    /** The server id of this record. */
    @PrimaryKey
    val id: Int,
    /** When the maker made the change, in epoch milliseconds. */
    val madeOnDate: Long = 0,
    /** The result of processing the task. */
    val processingResult: String? = null,
    /** The username of whoever made the change. */
    val maker: String? = null,
    /** The action the task or permission covers (create, approve, …). */
    val actionName: String? = null,
    /** The entity the task or permission covers (CLIENT, LOAN, …). */
    val entityName: String? = null,
    /** The server id of the resource the request created or changed. */
    val resourceId: String? = null,
)
