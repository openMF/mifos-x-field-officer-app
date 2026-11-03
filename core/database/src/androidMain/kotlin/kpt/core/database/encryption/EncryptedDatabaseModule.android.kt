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

import android.database.sqlite.SQLiteDatabaseCorruptException
import android.database.sqlite.SQLiteException
import co.touchlab.kermit.Logger
import com.russhwolf.settings.Settings
import kotlinx.coroutines.Dispatchers
import kpt.core.base.database.AppDatabaseFactory
import kpt.core.database.AppDatabase
import kpt.core.database.di.appDatabaseNaming
import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.driver.SQLCipherDriver
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.io.File
import java.security.SecureRandom

/**
 * Android: the Room database is encrypted at rest with SQLCipher (`net.zetetic:sqlcipher-android`,
 * whose `SQLCipherDriver` is an `androidx.sqlite.SQLiteDriver` — the only hook Room 3 offers, since
 * its builder has `setDriver` and no `openHelperFactory`).
 *
 * KEY. 32 random bytes from [SecureRandom], generated once per install and stored in the
 * `named("secure")` Settings (core-base/datastore `SecureSettingsFactory`): EncryptedSharedPreferences
 * whose master key is an AES256-GCM key held in AndroidKeyStore, so the passphrase is never on disk
 * in plaintext, never in source/BuildConfig, and never logged. Nothing clears that store (logout
 * only removes the auth-token keys), so the DB key survives logout. `allowBackup="false"`, so a
 * backup restore cannot strand a DB without its key; if the key is lost anyway the unreadable file
 * is moved aside and the app starts empty rather than crashing.
 *
 * EXISTING DATA. Devices in the field hold unsynced captures in a plaintext DB; on first launch it
 * is converted in place — see [prepareEncryptedDatabase] for the verify-before-replace contract.
 *
 * NOT ENCRYPTED: iOS, desktop, web. There is no official SQLCipher `androidx.sqlite` driver for any
 * of them (only Android ships `SQLCipherDriver`). iOS would need a custom `SQLiteDriver` over a
 * SQLCipher-linked sqlite3, and the app also links Firebase, which pulls in the SYSTEM sqlite3 — two
 * sqlite3 copies in one process is exactly what SQLCipher warns breaks. Those platforms keep the
 * template's `BundledSQLiteDriver` / web-worker driver untouched.
 *
 * The builder policy below mirrors core-base `platformDatabaseModule` line for line (destructive
 * fallback, no destructive downgrade, IO query context); only the driver differs. Keep them in step.
 */
internal actual val encryptedDatabaseModule: Module = module {
    single<AppDatabase> {
        val app = androidApplication()
        val rawKey = sqlcipherRawKey(
            databaseKeyHex(
                stored = get<Settings>(named("secure")).getStringOrNull(DB_KEY_PREF),
                randomBytes = { n -> ByteArray(n).also(SecureRandom()::nextBytes) },
                persist = { get<Settings>(named("secure")).putString(DB_KEY_PREF, it) },
            ),
        )
        SQLCipherLoader.load()
        val dbFile = app.getDatabasePath(appDatabaseNaming.fileName)
        val outcome = prepareEncryptedDatabase(SqlCipherDatabaseFiles(dbFile), rawKey) { msg, e ->
            log.w(e) { msg }
        }
        log.i { "database encryption: $outcome" }
        val driverKey = if (outcome == EncryptionOutcome.MIGRATION_DEFERRED) "" else rawKey

        AppDatabaseFactory(app)
            .createDatabase<AppDatabase>(databaseName = appDatabaseNaming.fileName)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .fallbackToDestructiveMigrationOnDowngrade(false)
            .setDriver(SQLCipherDriver(driverKey.encodeToByteArray(), null, KeepFileOnCorruption))
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}

private const val DB_KEY_PREF = "kpt.database.sqlcipher_key.v1"
private val log = Logger.withTag("DatabaseEncryption")

/** sqlcipher-android ships `libsqlcipher.so` but never loads it itself — the caller must. */
private object SQLCipherLoader {
    private val loaded by lazy { System.loadLibrary("sqlcipher") }
    fun load() = loaded
}

/**
 * SQLCipher's DEFAULT error handler deletes the database file on any corruption report — and a wrong
 * key is reported as corruption. For a DB holding unsynced field captures that is the worst
 * possible reaction, so this one only logs; the exception still propagates to the caller.
 */
private object KeepFileOnCorruption : DatabaseErrorHandler {
    override fun onCorruption(db: SQLiteDatabase?, e: SQLiteException?) {
        log.e(e) { "SQLCipher reported corruption for ${db?.path}; file kept" }
    }
}

private val SIDECARS = listOf("-wal", "-shm", "-journal")

private class SqlCipherDatabaseFiles(private val db: File) : EncryptedDatabaseFiles {
    private val temp = File(db.path + ".encrypting")

    override fun state(): DatabaseFileState {
        if (!db.exists()) return classifyDatabaseFile(null)
        val header = db.inputStream().use { input ->
            val buf = ByteArray(SQLITE_PLAINTEXT_HEADER.size)
            buf.copyOf(input.read(buf).coerceAtLeast(0))
        }
        return classifyDatabaseFile(header)
    }

    override fun plaintextSnapshot() = open(db, key = "").use { plain ->
        // Fold the WAL into the main file first: Room runs in WAL mode and a live device had 4 MB of
        // un-checkpointed pages. After this the main file alone holds every row, so deleting the
        // old -wal in promoteTemp() can never drop a write.
        plain.rawQuery("PRAGMA wal_checkpoint(TRUNCATE)", arrayOf<String>()).use { it.moveToFirst() }
        plain.snapshot()
    }

    override fun exportPlaintextToTemp(rawKey: String) = open(db, key = "").use { plain ->
        val version = plain.version
        plain.rawExecSQL("ATTACH DATABASE '${temp.path}' AS encrypted KEY \"$rawKey\"")
        plain.rawExecSQL("SELECT sqlcipher_export('encrypted')")
        plain.rawExecSQL("PRAGMA encrypted.user_version = $version")
        plain.rawExecSQL("DETACH DATABASE encrypted")
    }

    override fun tempSnapshot(rawKey: String) = open(temp, rawKey).use { it.snapshot() }

    override fun opensWith(rawKey: String): Boolean = try {
        open(db, rawKey).use {
            it.rawQuery("SELECT count(*) FROM sqlite_master", arrayOf<String>()).use { c -> c.moveToFirst() }
        }
        true
    } catch (e: SQLiteDatabaseCorruptException) {
        // Only "wrong key" means unreadable. Anything else (I/O, locked) must not trigger a reset.
        false
    } catch (e: SQLiteException) {
        if (e.message.orEmpty().contains("not a database")) false else throw e
    }

    override fun promoteTemp() {
        SIDECARS.forEach { File(db.path + it).delete() }
        check(temp.renameTo(db)) { "could not move encrypted copy over ${db.name}" }
    }

    override fun discardTemp() {
        (listOf("") + SIDECARS).forEach { File(temp.path + it).delete() }
    }

    override fun moveAside() {
        val suffix = ".unreadable-${System.currentTimeMillis()}"
        (listOf("") + SIDECARS).forEach {
            File(db.path + it).takeIf(File::exists)?.renameTo(File(db.path + it + suffix))
        }
    }

    private fun open(file: File, key: String): SQLiteDatabase = SQLiteDatabase.openDatabase(
        file.path,
        key.encodeToByteArray(),
        null,
        SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY,
        KeepFileOnCorruption,
        null,
    )

    private fun SQLiteDatabase.snapshot(): DatabaseSnapshot {
        val tables = rawQuery("SELECT name FROM sqlite_master WHERE type = 'table' ORDER BY name", arrayOf<String>())
            .use { c -> buildList { while (c.moveToNext()) add(c.getString(0)) } }
        val counts = tables.associateWith { t ->
            rawQuery("SELECT count(*) FROM \"${t.replace("\"", "\"\"")}\"", arrayOf<String>()).use { c ->
                c.moveToFirst()
                c.getLong(0)
            }
        }
        return DatabaseSnapshot(version.toLong(), counts)
    }
}
