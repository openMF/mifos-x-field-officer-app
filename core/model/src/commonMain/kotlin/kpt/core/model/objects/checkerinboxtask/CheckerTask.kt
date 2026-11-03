/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.checkerinboxtask

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
/** A checker task. */
@Serializable
data class CheckerTask(
    /** The server id of this record. */
    val id: Int,
    /** When the maker made the change, in epoch milliseconds. */
    val madeOnDate: Long,
    /** The result of processing the task. */
    val processingResult: String,
    /** The username of whoever made the change. */
    val maker: String,
    /** The action the task or permission covers (create, approve, …). */
    val actionName: String,
    /** The entity the task or permission covers (CLIENT, LOAN, …). */
    val entityName: String,
    /** The server id of the resource the request created or changed. */
    val resourceId: String,
) {

    /** True when the task is selected. */
    var selectedFlag = false

    /** The day the task was made, as `d Mon yyyy` in the device's time zone. */
    @OptIn(ExperimentalTime::class)
    fun getDate(): String {
        val instant = Instant.fromEpochMilliseconds(madeOnDate)
        val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        return "${localDate.day} ${
            localDate.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
        } ${localDate.year}"
    }
}
