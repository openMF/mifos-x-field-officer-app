/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.collectionsheet

/**
 * The full query is the key: a centre sheet is only meaningful for one (office, staff, meeting
 * date) triple, so date or staff changing must re-fetch rather than serve the previous sheet.
 */
data class CollectionSheetKey(
    /** The office's server id. */
    val officeId: Int,
    /** The staff member's server id. */
    val staffId: Int,
    /** Date of the meeting. */
    val meetingDate: String,
    /** Pattern the dates are written in. */
    val dateFormat: String = "dd MMMM yyyy",
    /** Locale the dates and numbers are written in. */
    val locale: String = "en",
)
