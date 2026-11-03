/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.note

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.objects.note.Note

/** Reads the notes of a resource. */
interface NoteRepository {
    /** Notes attached to a client, group or loan. */
    /** One owner's notes — [resourceType] is Fineract's path (`clients`, `groups`). */
    fun notesStream(resourceType: String, resourceId: Long, scope: CoroutineScope): ScreenDataStream<List<Note>>
}
