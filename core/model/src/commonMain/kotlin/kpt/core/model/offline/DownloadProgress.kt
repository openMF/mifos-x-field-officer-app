/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.offline

/**
 * Progress of an offline download — centers or groups the officer selected, walked down to every
 * client and active account they hold, so those screens open with no connection.
 *
 * [completed] / [total] count the SELECTED items; [records] counts everything fetched under them
 * (members, accounts, templates). A failure never stops the download: it is recorded in [failures]
 * and the walk moves on, as the original's sync did.
 */
data class DownloadProgress(
    val completed: Int = 0,
    val total: Int = 0,
    /** The selected item currently being downloaded; null before the first and after the last. */
    val current: String? = null,
    val records: Int = 0,
    val failures: List<DownloadFailure> = emptyList(),
    val finished: Boolean = false,
)

/** One thing that could not be downloaded: what it was, and the server's reason when it gave one. */
data class DownloadFailure(val item: String, val reason: String?)

/** A selected center or group: its id, and the name progress and failures are reported under. */
data class DownloadTarget(val id: Int, val name: String)
