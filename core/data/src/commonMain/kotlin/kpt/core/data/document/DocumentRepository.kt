/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
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
}
