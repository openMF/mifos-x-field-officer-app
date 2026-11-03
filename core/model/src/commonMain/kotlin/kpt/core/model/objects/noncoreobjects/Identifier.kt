/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.noncoreobjects

import kotlinx.serialization.Serializable

/**
 * Created by ishankhanna on 03/07/14.
 */
@Serializable
data class Identifier(
    /** The server id of this record. */
    var id: Int? = null,

    /** The server id of the client. */
    var clientId: Int? = null,

    /** The document type of the identifier. */
    var documentKey: String? = null,

    /** The identity document type. */
    var documentType: DocumentType? = null,

    /** A free-text description. */
    var description: String? = null,

    /** The record's Fineract status. */
    var status: String? = null,
)
