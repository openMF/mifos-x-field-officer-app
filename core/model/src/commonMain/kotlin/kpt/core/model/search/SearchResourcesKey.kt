/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.search

/**
 * Addresses one `searchResources` read: what a search screen asks `SearchRepository` for.
 *
 * In core/model, not beside the store that uses it as a key: a feature builds it, and a feature
 * depends on core/data + core/model only.
 */
data class SearchResourcesKey(
    /** The text searched for. */
    val query: String,
    /** The kinds of record to search. */
    val resource: String?,
    /** True to match the query exactly. */
    val exactMatch: Boolean?,
)
