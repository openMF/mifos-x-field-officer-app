/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.cache.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * One row per cached read — the offline backing for reads that have no table of their own.
 *
 * The app's entity reads (clients, centers, loans, savings) each own a real Room table, because the
 * app queries and relates them. The long tail does not: account summaries, option lists, per-loan
 * schedules, report catalogues. Those are read-through projections handed to a screen whole and
 * never queried by column, so a table each would be ~70 schemas earning nothing, and a migration
 * every time Fineract adds a field.
 *
 * Giving them a shared blob table is what makes "offline-first by default" affordable for ALL
 * reads rather than only the ones worth modelling — a field officer sees the last-known answer with
 * no signal, instead of an error, whichever screen they open.
 *
 * [cacheKey] is the store's own `@CacheKey` string, so the key that identifies a stream is the key
 * that identifies its row — there is no second keying scheme to keep in step.
 */
@DbEntity
@Entity(tableName = "api_response_cache")
data class ApiResponseCacheEntity(
    @PrimaryKey
    val cacheKey: String,
    val payload: String,
    val fetchedAtMs: Long,
)
