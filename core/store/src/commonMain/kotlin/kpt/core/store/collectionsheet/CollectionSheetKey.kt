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
    val officeId: Int,
    val staffId: Int,
    val meetingDate: String,
    val dateFormat: String = "dd MMMM yyyy",
    val locale: String = "en",
)
