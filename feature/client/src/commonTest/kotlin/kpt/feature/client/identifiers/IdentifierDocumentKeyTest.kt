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
import kotlin.test.Test
import kotlin.test.assertEquals

class IdentifierDocumentKeyTest {

    @Test
    fun theKeyIsTheOriginalsConcatenationByteForByte() {
        // Scans uploaded by the ORIGINAL app carry exactly this string in their description; a key
        // that differs by one character makes every one of them unfindable.
        assertEquals("PassportA1234Active", identifierDocumentKey("Passport", "A1234", Status.Active))
        assertEquals("PassportA1234Inactive", identifierDocumentKey("Passport", "A1234", Status.Inactive))
    }

    @Test
    fun anAbsentPartIsTheLiteralNullTheOriginalProduced() {
        // `item.documentType?.name + item.documentKey + status` with a null part yields "null".
        assertEquals("nullA1234null", identifierDocumentKey(null, "A1234", null))
    }

    @Test
    fun thePrefixDropsOnlyTheStatus() {
        assertEquals("PassportA1234", identifierDocumentKeyPrefix("Passport", "A1234"))
        val key = identifierDocumentKey("Passport", "A1234", Status.Active)
        kotlin.test.assertTrue(key.startsWith(identifierDocumentKeyPrefix("Passport", "A1234")))
    }
}
