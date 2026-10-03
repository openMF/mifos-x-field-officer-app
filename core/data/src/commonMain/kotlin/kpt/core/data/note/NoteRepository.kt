/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.note

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.database.note.entity.NoteEntity

interface NoteRepository {
    /** Notes attached to a client, group or loan. */
    fun notesStream(resourceId: Long, scope: CoroutineScope): ScreenDataStream<List<NoteEntity>>
}
