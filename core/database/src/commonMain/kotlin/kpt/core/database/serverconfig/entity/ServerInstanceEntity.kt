/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.serverconfig.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/**
 * One server instance the officer can pick — the local source of truth for the `serverInstances`
 * store, filled from Supabase `app_server_config` (only this app's rows). Public configuration, not
 * the officer's data: it is kept on sign-out so sign-in can offer it offline.
 */
@DbEntity
@Entity(tableName = "server_instances")
data class ServerInstanceEntity(
    /** `endpoint/tenant` — the same pair the remote table keys on. */
    @PrimaryKey
    val id: String,
    /** The display name. */
    val name: String,
    /** `http` or `https`. */
    val protocol: String,
    /** The server's host name or address. */
    val endPoint: String,
    /** The Fineract API path on the server. */
    val apiPath: String,
    /** The TCP port. */
    val port: String,
    /** The Fineract tenant identifier. */
    val tenant: String,
    /** Serialized published demo sign-ins (`[{role, username, password}]`) — only ever read whole. */
    val demoAccounts: String,
    /** The remote `sort_order` position, so the picker keeps the table's order. */
    val position: Int,
    /** When this cached copy was fetched, in epoch milliseconds. */
    val fetchedAtMs: Long,
)
