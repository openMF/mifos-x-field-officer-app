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
    @PrimaryKey
    val id: Int,
    val madeOnDate: Long = 0,
    val processingResult: String? = null,
    val maker: String? = null,
    val actionName: String? = null,
    val entityName: String? = null,
    val resourceId: String? = null,
)
