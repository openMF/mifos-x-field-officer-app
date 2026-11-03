/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.clients

import kotlinx.serialization.Serializable

/**
 * Created by ishankhanna on 09/02/14.
 */
@Serializable
data class Page<T>(
    /** The number of records matching the query, across every page. */
    var totalFilteredRecords: Int = 0,

    /** The records on this page. */
    var pageItems: List<T> = emptyList(),
)
