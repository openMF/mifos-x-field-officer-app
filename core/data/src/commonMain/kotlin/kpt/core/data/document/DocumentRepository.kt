/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.document

import kotlinx.coroutines.CoroutineScope
import kpt.core.base.store.screen.ScreenDataStream
import kpt.core.model.objects.noncoreobjects.Document

interface DocumentRepository {
    /** Documents attached to any Fineract entity — clients, loans, groups all share this surface. */
    fun documentsStream(
        entityType: String,
        entityId: Int,
        scope: CoroutineScope,
    ): ScreenDataStream<List<Document>>

    /**
     * One document's bytes, or `null` when it cannot be fetched.
     *
     * A one-shot blob, NOT a [ScreenDataStream] — same reasoning as `ClientRepository.clientImage`:
     * the caller either has the file or does not, and there is no Loading/Empty/Error for a screen
     * to render around it.
     */
    suspend fun downloadDocument(entityType: String, entityId: Int, documentId: Int): ByteArray?

    /**
     * The entity's documents as the SERVER has them now — fetched through the store, so the
     * database is updated first — falling back to the cached list offline; `null` only when
     * neither is available.
     *
     * For a one-shot LOOKUP, not a screen — use [documentsStream] to render a list. The stream's
     * first emission can be the cached list, which is the wrong answer for "is the scan I may have
     * just uploaded there?" (found on device 2026-10-23: a scan uploaded, then "no document attached").
     */
    suspend fun fetchDocuments(entityType: String, entityId: Int): List<Document>?
}
