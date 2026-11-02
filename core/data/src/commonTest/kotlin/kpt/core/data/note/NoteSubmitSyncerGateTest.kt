/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.note

import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkInfo
import io.github.mobilebytelabs.kmptoolkit.networkmonitor.NetworkStatus
import kpt.core.base.store.submit.RetryOnNetworkStatus
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The note-replay gate, pinned.
 *
 * Found on a device 2026-10-20, not by reasoning: the syncer is `createdAtStart`, so it fired at
 * app launch BEFORE `AuthHeaderBridge` had seeded the `Authorization` header. The replay went out
 * unauthenticated, Fineract refused it, and `OfflineSubmitSyncer.retryAll` marks FAILED on ANY
 * exception — so a good note written offline was burned to a terminal FAILED on the next launch.
 * Observed: `PENDING → RETRYING(attempt=1) → FAILED "note submit rejected"` with the device fully
 * online.
 *
 * These assert the condition through the SAME predicate the syncer uses
 * ([RetryOnNetworkStatus.OnlineOnly]), so the test cannot drift from the behaviour it protects.
 */
class NoteSubmitSyncerGateTest {

    /** `Available` carries NetworkInfo — it is a data class, not an object. */
    private fun online() = NetworkStatus.Available(NetworkInfo())

    private fun wouldSweep(status: NetworkStatus, authenticated: Boolean): Boolean =
        RetryOnNetworkStatus.OnlineOnly.shouldRetry(gateOnAuth(status, authenticated))

    @Test
    fun anUnauthenticatedAppNeverSweepsEvenWhenFullyOnline() {
        // THE regression. Online is not enough: the token arrives asynchronously after launch,
        // and a sweep before it burns every pending draft to FAILED.
        assertFalse(wouldSweep(online(), authenticated = false))
    }

    @Test
    fun anAuthenticatedOnlineAppSweeps() {
        assertTrue(wouldSweep(online(), authenticated = true))
    }

    @Test
    fun beingAuthenticatedDoesNotMakeAnOfflineAppSweep() {
        // The gate must only ever SUBTRACT. Passing the real status through when authenticated is
        // what keeps the original connectivity rule intact.
        assertFalse(wouldSweep(NetworkStatus.Unavailable, authenticated = true))
    }

    @Test
    fun theGateIsTransparentOnceSignedIn() {
        // Identity, not equality — a transformed status could silently drop information the
        // predicate (or a future one, e.g. OnlineOrCaptivePortal) depends on.
        val status = online()
        assertTrue(gateOnAuth(status, isAuthenticated = true) === status)
    }
}
