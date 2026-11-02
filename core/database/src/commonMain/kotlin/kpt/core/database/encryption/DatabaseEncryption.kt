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

import org.koin.core.module.Module

/**
 * Overrides the template's plaintext `AppDatabase` binding with an at-rest-encrypted one where the
 * platform supports it. Included from the fork-owned `ProjectDatabaseModule`, which Koin loads AFTER
 * the template's `DatabaseModule` (`KoinModules.allModules` order), so this definition wins.
 *
 * Done as an override rather than an edit of `di/DatabaseModule.android.kt` because that file is
 * `owner: template` (the customization-surface.yaml `di/` glob) and a template sync would
 * silently put the plaintext driver back. Only Android has an actual binding — see the Android
 * actual for why every other platform stays unencrypted.
 */
internal expect val encryptedDatabaseModule: Module

/** What is on disk at the database path, decided from the file's first 16 bytes. */
internal enum class DatabaseFileState { ABSENT, PLAINTEXT, ENCRYPTED }

/** Every plaintext SQLite file starts with this 16-byte magic; an SQLCipher file is random bytes. */
internal val SQLITE_PLAINTEXT_HEADER: ByteArray = "SQLite format 3\u0000".encodeToByteArray()

/**
 * Classifies a database file from its leading bytes. [header] is `null` when the file does not
 * exist. A zero-length file is ABSENT: SQLite treats it as an empty database, and Room creates the
 * schema into it under whatever key the driver supplies.
 */
internal fun classifyDatabaseFile(header: ByteArray?): DatabaseFileState = when {
    header == null || header.isEmpty() -> DatabaseFileState.ABSENT
    header.size >= SQLITE_PLAINTEXT_HEADER.size &&
        header.copyOf(SQLITE_PLAINTEXT_HEADER.size).contentEquals(SQLITE_PLAINTEXT_HEADER) ->
        DatabaseFileState.PLAINTEXT
    else -> DatabaseFileState.ENCRYPTED
}

private const val KEY_BYTES = 32
private val HEX_KEY = Regex("^[0-9a-f]{${KEY_BYTES * 2}}$")

/**
 * Returns the stored 256-bit database key (lower-case hex), generating and persisting one on first
 * use. A stored value that is not a well-formed 64-char hex key is treated as lost and replaced —
 * the database it belonged to is then unreadable and [prepareEncryptedDatabase] moves it aside.
 */
internal fun databaseKeyHex(
    stored: String?,
    randomBytes: (Int) -> ByteArray,
    persist: (String) -> Unit,
): String {
    if (stored != null && HEX_KEY.matches(stored)) return stored
    val bytes = randomBytes(KEY_BYTES)
    require(bytes.size == KEY_BYTES) { "key generator returned ${bytes.size} bytes, need $KEY_BYTES" }
    val hex = bytes.joinToString("") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }
    persist(hex)
    return hex
}

/**
 * SQLCipher raw-key literal. Passing `x'<64 hex>'` as the key makes SQLCipher use the bytes as the
 * AES key directly and SKIP its PBKDF2 derivation (256 000 iterations per connection open) — the
 * key is already 256 random bits, so stretching it buys nothing and costs every connection.
 */
internal fun sqlcipherRawKey(keyHex: String): String = "x'$keyHex'"

/** `PRAGMA user_version` plus the row count of every table: what a migration must preserve. */
internal data class DatabaseSnapshot(val userVersion: Long, val rowCounts: Map<String, Long>)

/** The file operations [prepareEncryptedDatabase] orchestrates; SQLCipher-backed on Android. */
internal interface EncryptedDatabaseFiles {
    fun state(): DatabaseFileState

    /** Snapshot of the plaintext database, read through its WAL. */
    fun plaintextSnapshot(): DatabaseSnapshot

    /** `ATTACH ... KEY` + `sqlcipher_export` + `user_version` copy into the temp file. */
    fun exportPlaintextToTemp(rawKey: String)

    /** Snapshot of the temp file opened with [rawKey] — proves the key opens it. */
    fun tempSnapshot(rawKey: String): DatabaseSnapshot

    /** `true` if the encrypted database opens with [rawKey]; `false` only when the key is wrong. */
    fun opensWith(rawKey: String): Boolean

    /** Replaces the plaintext database (and its -wal/-shm/-journal) with the verified temp file. */
    fun promoteTemp()

    /** Deletes the temp file and its sidecars, if any. */
    fun discardTemp()

    /** Renames the unreadable database and its sidecars out of the way. Never deletes. */
    fun moveAside()
}

internal enum class EncryptionOutcome {
    /** No database yet — Room creates an encrypted one. */
    FRESH,

    /** Already encrypted and the key opens it. */
    OPENED,

    /** A plaintext database was converted and verified. */
    MIGRATED,

    /** Encrypted, but the key cannot open it (Keystore key lost) — moved aside, starting empty. */
    RESET_UNREADABLE,

    /** Conversion failed or did not verify; plaintext kept untouched and opened as-is this launch. */
    MIGRATION_DEFERRED,
}

/**
 * Brings the database file into the state the encrypted driver can open, and says which key to use:
 * [rawKey] in every case except [EncryptionOutcome.MIGRATION_DEFERRED], where the plaintext file is
 * still in place and must be opened with the empty key (SQLCipher's "no encryption").
 *
 * The plaintext path holds unsynced offline captures and drafts, so it never destroys data: the
 * plaintext file is only replaced after the encrypted copy has been re-opened with the key AND its
 * `user_version` and every table's row count equal the original's. Any failure before that leaves
 * the plaintext file exactly as it was and is retried next launch. A crash mid-way is equally safe —
 * the plaintext file is still detected as plaintext and the stale temp file is discarded first.
 */
internal fun prepareEncryptedDatabase(
    files: EncryptedDatabaseFiles,
    rawKey: String,
    log: (String, Throwable?) -> Unit = { _, _ -> },
): EncryptionOutcome = when (files.state()) {
    DatabaseFileState.ABSENT -> EncryptionOutcome.FRESH
    DatabaseFileState.ENCRYPTED ->
        if (files.opensWith(rawKey)) {
            EncryptionOutcome.OPENED
        } else {
            log("encrypted database does not open with the stored key; moving it aside", null)
            files.moveAside()
            EncryptionOutcome.RESET_UNREADABLE
        }
    DatabaseFileState.PLAINTEXT -> try {
        files.discardTemp()
        val before = files.plaintextSnapshot()
        files.exportPlaintextToTemp(rawKey)
        val after = files.tempSnapshot(rawKey)
        if (after != before) {
            files.discardTemp()
            log("encrypted copy did not verify (before=$before after=$after); keeping plaintext", null)
            EncryptionOutcome.MIGRATION_DEFERRED
        } else {
            files.promoteTemp()
            EncryptionOutcome.MIGRATED
        }
    } catch (e: Exception) {
        runCatching { files.discardTemp() }
        log("plaintext -> encrypted conversion failed; keeping plaintext", e)
        EncryptionOutcome.MIGRATION_DEFERRED
    }
}
