/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.store.mutation

import kpt.core.base.store.mutation.conflict.ConflictInbox
import kpt.core.base.store.mutation.delete.DeleteSync
import org.mobilenativefoundation.store.store5.Bookkeeper
import org.mobilenativefoundation.store.store5.MutableStore
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import kotlin.time.Clock

/**
 * The default [MutationGateway] — composes the existing Store5 write machinery.
 *
 * Optimistic writes go through `MutableStore.write` (which persists to the Room source of truth, drives
 * the `Updater`, and records failures in the `Bookkeeper` for retry). Online-required writes await the
 * network first and are [MutationResult.Blocked] offline. Deletes clear the local store and sync via the
 * caller-supplied delete endpoint (Store5 has no network-DELETE path). Command mutations run the caller's
 * endpoint, apply optimistic local state, and record server conflicts in the [ConflictInbox].
 *
 * @param isOnline connectivity probe (DI wires it to the app's `NetworkMonitor`); kept as a lambda so the
 *   gateway is decoupled from the network API and trivially testable.
 * @param conflictInbox durable sink for write conflicts surfaced in Settings.
 * @param now monotonic-millis provider for pending-delete tombstones (injectable for deterministic tests).
 */
class DefaultMutationGateway(
    private val isOnline: suspend () -> Boolean,
    private val conflictInbox: ConflictInbox,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) : MutationGateway {

    override suspend fun <K : Any, V : Any> upsert(
        store: MutableStore<K, V>,
        key: K,
        value: V,
        policy: MutationPolicy,
    ): MutationResult<V> {
        if (policy is MutationPolicy.OnlineRequired && !isOnline()) {
            return MutationResult.Blocked(BlockReason.OFFLINE)
        }
        return try {
            store.write(StoreWriteRequest.of<K, V, Any>(key = key, value = value))
            // Optimistic offline write is persisted locally + queued in the Bookkeeper (synced=false);
            // online (or online-required) writes reach the network now (synced=true).
            MutationResult.Applied(value = value, synced = isOnline())
        } catch (t: Throwable) {
            MutationResult.Failed(cause = t, rolledBack = false)
        }
    }

    override suspend fun <K : Any, V : Any> delete(
        store: MutableStore<K, V>,
        key: K,
        deleteEndpoint: suspend (K) -> Unit,
        bookkeeper: Bookkeeper<K>,
        policy: MutationPolicy,
    ): MutationResult<Unit> {
        // OnlineRequired offline → nothing is written or tombstoned.
        if (policy is MutationPolicy.OnlineRequired && !isOnline()) {
            return MutationResult.Blocked(BlockReason.OFFLINE)
        }
        return try {
            if (policy is MutationPolicy.OnlineRequired) {
                // Network-first: confirm the server DELETE before touching local state; never tombstone.
                deleteEndpoint(key)
                store.clear(key)
                MutationResult.Applied(value = Unit, synced = true)
            } else {
                // Optimistic: clear the local row now (it disappears from every read), sync the network
                // DELETE, and — offline or on failure — record a pending-delete tombstone in the
                // [bookkeeper] so the feature's SyncOrchestrator retries it on reconnect. Composes the
                // DeleteSync primitive (behaviour unit-tested in DeleteSyncTest); clearing the row is the
                // only op that can throw here.
                val deleteSync = DeleteSync(
                    clearLocal = { store.clear(it) },
                    deleteEndpoint = deleteEndpoint,
                    bookkeeper = bookkeeper,
                    isOnline = isOnline,
                    now = now,
                )
                MutationResult.Applied(value = Unit, synced = deleteSync.delete(key))
            }
        } catch (t: Throwable) {
            MutationResult.Failed(cause = t, rolledBack = false)
        }
    }

    override suspend fun <P : Any, R : Any> command(
        spec: CommandSpec<P, R>,
        policy: MutationPolicy,
    ): MutationResult<R> {
        if (policy is MutationPolicy.OnlineRequired && !isOnline()) {
            return MutationResult.Blocked(BlockReason.OFFLINE)
        }
        // Optimistic: apply the local pre-state before the call so the UI updates instantly.
        if (policy is MutationPolicy.Optimistic) {
            runCatching { spec.localApply?.invoke(null) }
        }
        return try {
            val result = spec.endpoint(spec.payload)
            spec.localApply?.invoke(result) // ingest the real server record (server-wins / key-remap)
            val report = spec.conflictOf?.invoke(spec.payload, result)
            if (report != null) {
                val id = conflictInbox.record(
                    entity = report.entity,
                    key = report.key,
                    localPayloadJson = report.localPayloadJson,
                    serverPayloadJson = report.serverPayloadJson,
                    formRoute = report.formRoute,
                )
                MutationResult.Conflicted(conflictId = id, server = result)
            } else {
                MutationResult.Applied(value = result, synced = true)
            }
        } catch (t: Throwable) {
            val rolledBack = runCatching { spec.rollback?.invoke() }.isSuccess && spec.rollback != null
            MutationResult.Failed(cause = t, rolledBack = rolledBack)
        }
    }

    override suspend fun localMutation(table: String, mutate: suspend () -> Unit): MutationResult<Unit> =
        try {
            mutate()
            MutationResult.Applied(value = Unit, synced = true)
        } catch (t: Throwable) {
            MutationResult.Failed(cause = t, rolledBack = false)
        }
}
