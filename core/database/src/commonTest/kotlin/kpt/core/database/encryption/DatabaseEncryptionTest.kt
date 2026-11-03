/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.database.encryption

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DatabaseEncryptionTest {

    // ── header detection ─────────────────────────────────────────────────────────────────────
    @Test
    fun missingOrEmptyFileIsAbsent() {
        assertEquals(DatabaseFileState.ABSENT, classifyDatabaseFile(null))
        assertEquals(DatabaseFileState.ABSENT, classifyDatabaseFile(ByteArray(0)))
    }

    @Test
    fun sqliteMagicIsPlaintext() {
        assertEquals(DatabaseFileState.PLAINTEXT, classifyDatabaseFile(SQLITE_PLAINTEXT_HEADER + ByteArray(84)))
    }

    @Test
    fun randomBytesAndNearMissesAreEncrypted() {
        assertEquals(DatabaseFileState.ENCRYPTED, classifyDatabaseFile(ByteArray(16) { (it * 37).toByte() }))
        // "SQLite format 3" WITHOUT the trailing NUL, and a header truncated below 16 bytes.
        assertEquals(DatabaseFileState.ENCRYPTED, classifyDatabaseFile("SQLite format 3 ".encodeToByteArray()))
        assertEquals(DatabaseFileState.ENCRYPTED, classifyDatabaseFile(SQLITE_PLAINTEXT_HEADER.copyOf(15)))
    }

    // ── key generation / reuse ───────────────────────────────────────────────────────────────
    @Test
    fun keyIsGeneratedOncePersistedAndReused() {
        var store: String? = null
        var generated = 0
        val gen = { n: Int ->
            generated++
            ByteArray(n) { it.toByte() }
        }
        val first = databaseKeyHex(store, gen) { store = it }
        val second = databaseKeyHex(store, gen) { error("must not re-persist") }
        assertEquals(first, second)
        assertEquals(1, generated)
        assertEquals("000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f", first)
    }

    @Test
    fun malformedStoredKeyIsReplaced() {
        var store: String? = "not-a-key"
        val key = databaseKeyHex(store, { n -> ByteArray(n) { 0xFF.toByte() } }) { store = it }
        assertEquals("ff".repeat(32), key)
        assertEquals(key, store)
        // Upper-case or short hex is also not a key this code wrote.
        assertEquals("00".repeat(32), databaseKeyHex("AB".repeat(32), { ByteArray(it) }) {})
        assertEquals("00".repeat(32), databaseKeyHex("ab".repeat(31), { ByteArray(it) }) {})
    }

    @Test
    fun shortRandomSourceIsRejected() {
        var store: String? = null
        assertFailsWith<IllegalArgumentException> { databaseKeyHex(null, { ByteArray(16) }) { store = it } }
        assertNull(store)
    }

    @Test
    fun rawKeyLiteralSkipsKdf() {
        assertEquals("x'${"ab".repeat(32)}'", sqlcipherRawKey("ab".repeat(32)))
    }

    // ── migration orchestration ──────────────────────────────────────────────────────────────
    private class FakeFiles(
        var state: DatabaseFileState,
        val plain: DatabaseSnapshot = DatabaseSnapshot(15, mapOf("Client" to 11L, "framework_submit_drafts" to 3L)),
        var exported: DatabaseSnapshot? = null,
        val opens: Boolean = true,
        val exportFails: Boolean = false,
    ) : EncryptedDatabaseFiles {
        val calls = mutableListOf<String>()
        override fun state() = state
        override fun plaintextSnapshot() = plain.also { calls += "snapshot" }
        override fun exportPlaintextToTemp(rawKey: String) {
            calls += "export"
            if (exportFails) error("disk full")
            if (exported == null) exported = plain
        }
        override fun tempSnapshot(rawKey: String) = exported!!.also { calls += "verify" }
        override fun opensWith(rawKey: String) = opens.also { calls += "probe" }
        override fun promoteTemp() {
            calls += "promote"
            state = DatabaseFileState.ENCRYPTED
        }
        override fun discardTemp() {
            calls += "discard"
        }
        override fun moveAside() {
            calls += "aside"
            state = DatabaseFileState.ABSENT
        }
    }

    @Test
    fun absentDatabaseIsFresh() {
        val f = FakeFiles(DatabaseFileState.ABSENT)
        assertEquals(EncryptionOutcome.FRESH, prepareEncryptedDatabase(f, "k"))
        assertEquals(emptyList(), f.calls)
    }

    @Test
    fun plaintextIsExportedVerifiedThenReplaced() {
        val f = FakeFiles(DatabaseFileState.PLAINTEXT)
        assertEquals(EncryptionOutcome.MIGRATED, prepareEncryptedDatabase(f, "k"))
        // stale temp cleared first; replace happens only after verify.
        assertEquals(listOf("discard", "snapshot", "export", "verify", "promote"), f.calls)
        assertEquals(DatabaseFileState.ENCRYPTED, f.state)
    }

    @Test
    fun rowCountMismatchKeepsPlaintext() {
        val f = FakeFiles(
            DatabaseFileState.PLAINTEXT,
            exported = DatabaseSnapshot(
                15,
                mapOf("Client" to 10L, "framework_submit_drafts" to 3L),
            ),
        )
        assertEquals(EncryptionOutcome.MIGRATION_DEFERRED, prepareEncryptedDatabase(f, "k"))
        assertEquals(false, "promote" in f.calls)
        assertEquals(DatabaseFileState.PLAINTEXT, f.state)
    }

    @Test
    fun userVersionMismatchKeepsPlaintext() {
        val f = FakeFiles(
            DatabaseFileState.PLAINTEXT,
            exported = DatabaseSnapshot(
                0,
                mapOf("Client" to 11L, "framework_submit_drafts" to 3L),
            ),
        )
        assertEquals(EncryptionOutcome.MIGRATION_DEFERRED, prepareEncryptedDatabase(f, "k"))
        assertEquals(false, "promote" in f.calls)
    }

    @Test
    fun exportFailureKeepsPlaintextAndCleansTemp() {
        val f = FakeFiles(DatabaseFileState.PLAINTEXT, exportFails = true)
        assertEquals(EncryptionOutcome.MIGRATION_DEFERRED, prepareEncryptedDatabase(f, "k"))
        assertEquals(listOf("discard", "snapshot", "export", "discard"), f.calls)
        assertEquals(DatabaseFileState.PLAINTEXT, f.state)
    }

    @Test
    fun encryptedThatOpensIsLeftAlone() {
        val f = FakeFiles(DatabaseFileState.ENCRYPTED, opens = true)
        assertEquals(EncryptionOutcome.OPENED, prepareEncryptedDatabase(f, "k"))
        assertEquals(listOf("probe"), f.calls)
    }

    @Test
    fun encryptedWithLostKeyIsMovedAsideNotDeleted() {
        val f = FakeFiles(DatabaseFileState.ENCRYPTED, opens = false)
        assertEquals(EncryptionOutcome.RESET_UNREADABLE, prepareEncryptedDatabase(f, "k"))
        assertEquals(listOf("probe", "aside"), f.calls)
    }
}
