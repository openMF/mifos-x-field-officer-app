/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.client.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kpt.core.base.database.annotation.DbEntity

/** Room row for a client identifier. */
@DbEntity
@Entity(
    tableName = "ClientIdentifier",
    indices = [],
    inheritSuperIndices = false,
    primaryKeys = [],
    foreignKeys = [],
    ignoredColumns = [],
)
data class ClientIdentifierEntity(
    /** The local id of the identifier, before it syncs. */
    @PrimaryKey(autoGenerate = true)
    val localId: Int = 0,
    /** The server id of this record. */
    val id: Int? = null,
    /** The server id of the client. */
    val clientId: Int? = null,
    /** The document type of the identifier. */
    val documentKey: String? = null,
    /** The name of the identity document type. */
    val documentTypeName: String? = null,
    /** The server id of the identity document type. */
    val documentTypeId: Int? = null,
    /** A free-text description. */
    val description: String? = null,
    /** The record's Fineract status. */
    val status: String? = null,
)
