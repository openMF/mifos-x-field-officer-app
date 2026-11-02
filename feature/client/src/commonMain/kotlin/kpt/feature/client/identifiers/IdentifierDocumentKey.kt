/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.identifiers

import kpt.core.ui.components.Status

/**
 * How an identifier finds its scanned document.
 *
 * Fineract does not link the two: the scan is a document on entity `client_identifiers` keyed by
 * the CLIENT's id, and the original wrote `documentType + documentKey + status` into its
 * `description` so the identifiers list could search for it
 * (`6b66e8a43:.../clientIdentifiersAddUpdate/ClientIdentifiersAddUpdateViewModel.kt:539` writes it,
 * `.../clientIdentifiersList/ClientIdentifiersListScreen.kt:144` reads it).
 *
 * The WRITER and the READER live in different screens, and the port had already split them: the
 * add form wrote the officer's free-text description into that slot, so a scan uploaded through it
 * could never be found again. Both sides now build the key here.
 *
 * String templates on purpose — they reproduce the original's `+` concatenation byte for byte,
 * including `"null"` for an absent part, so documents uploaded by the original app still match.
 */
internal fun identifierDocumentKey(documentTypeName: String?, documentKey: String?, status: Status?): String =
    "$documentTypeName$documentKey$status"

/** The key without its status — what still names the identifier after its status changes. */
internal fun identifierDocumentKeyPrefix(documentTypeName: String?, documentKey: String?): String =
    "$documentTypeName$documentKey"
