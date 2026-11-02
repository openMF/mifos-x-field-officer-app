/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.document.entity

import androidx.room3.Entity
import kpt.core.base.database.annotation.DbEntity

/**
 * One document's FILE — the bytes behind a [DocumentEntity] row — as the local source of truth for
 * the `documentContent` store.
 *
 * Every API flows network → database → store → data (operator rule, 2026-10-23), the download
 * included: it used to be fetched straight from `DocumentApi` in `core/data` and cached nowhere, so
 * a scan already viewed once could not be opened again offline.
 *
 * DATA PROTECTION: these are client documents (identity scans, signatures). The store is registered
 * for the logout purge (`@StoreProvider` default), so they leave the device at sign-out. The
 * database itself is not encrypted — tracked as `local-database-not-encrypted`.
 *
 * Size is bounded upstream: the upload forms refuse files over 1 MB, the original's limit.
 */
@DbEntity
@Entity(
    tableName = "document_contents",
    primaryKeys = ["entityType", "entityId", "documentId"],
)
class DocumentContentEntity(
    val entityType: String,
    val entityId: Int,
    val documentId: Int,
    val bytes: ByteArray,
)
