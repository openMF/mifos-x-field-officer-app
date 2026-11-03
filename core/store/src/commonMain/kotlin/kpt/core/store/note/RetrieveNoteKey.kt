/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.store.note

/** Addresses one `retrieveNote` read. */
data class RetrieveNoteKey(
    /** The resource kind, as Fineract names it in the path (`loans`, `clients`, `savings`). */
    val resourceType: String,
    /** The id of the resource the records belong to. */
    val resourceId: Long,
    /** The note's server id. */
    val noteId: Long,
)
