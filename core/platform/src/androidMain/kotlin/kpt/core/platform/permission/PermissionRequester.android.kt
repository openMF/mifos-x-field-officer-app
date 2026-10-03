/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.platform.permission

import android.Manifest
import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kpt.core.base.platform.context.AppContext
import kotlin.coroutines.resume

/**
 * Android permission mechanics.
 *
 * Registers on the activity's own [androidx.activity.result.ActivityResultRegistry] rather than
 * through `rememberLauncherForActivityResult`. That keeps the whole requester outside composition,
 * which is what lets the dialog live once in commonMain instead of being an expect/actual composable
 * per platform. The launcher is unregistered as soon as the result arrives (and on cancellation), so
 * a screen that asks repeatedly does not leak a registration per attempt.
 */
actual class PermissionRequester actual constructor(private val context: AppContext) {

    /**
     * The hosting activity, or null when this context has none.
     *
     * Walks the [ContextWrapper] chain rather than a single `as? Activity` cast: Compose's
     * `LocalContext` is frequently a wrapper (a themed `ContextThemeWrapper`, or a wrapper the
     * Compose view hierarchy installs), and a one-step cast silently returns null there. The surface
     * this replaces used the single cast, so its rationale check quietly evaluated to `false` on any
     * wrapped context — permanently-denied looked identical to first-ask.
     */
    private val activity: ComponentActivity?
        get() {
            var candidate = context
            while (candidate is ContextWrapper) {
                if (candidate is ComponentActivity) return candidate
                candidate = candidate.baseContext
            }
            return null
        }

    actual fun isGranted(permissions: List<Permission>): Boolean =
        permissions.flatMap { it.manifestPermissions() }.all { manifest ->
            ContextCompat.checkSelfPermission(context, manifest) == PackageManager.PERMISSION_GRANTED
        }

    actual suspend fun request(permissions: List<Permission>): PermissionResult {
        val manifestPermissions = permissions.flatMap { it.manifestPermissions() }.distinct()
        if (manifestPermissions.isEmpty()) return PermissionResult.Granted
        if (isGranted(permissions)) return PermissionResult.Granted

        // No activity means no way to show the system dialog. Denied (not DeniedPermanently) —
        // this is a host problem the caller may retry from a real activity, not a user decision.
        val host = activity ?: return PermissionResult.Denied

        return suspendCancellableCoroutine { continuation ->
            // The callback needs to unregister the very launcher `register` is about to return, so it
            // captures a nullable var that is filled in immediately below. Everything AFTER the
            // registration uses the non-null `registered` val instead — a `?.` there would be
            // reading as if the launcher might be absent when it provably is not.
            var pending: ActivityResultLauncher<Array<String>>? = null
            val registered = host.activityResultRegistry.register(
                "kpt-permission-${registrationCounter++}",
                ActivityResultContracts.RequestMultiplePermissions(),
            ) { results ->
                pending?.unregister()
                continuation.resume(classify(host, manifestPermissions, results))
            }
            pending = registered

            continuation.invokeOnCancellation { registered.unregister() }
            registered.launch(manifestPermissions.toTypedArray())
        }
    }

    actual fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null),
        ).apply {
            // Required when starting from a non-activity context; harmless when there is one.
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    /**
     * Grant only when every permission came back true. For a refusal, `shouldShowRequestPermissionRationale`
     * distinguishes "asked and refused, may ask again" from "refused permanently": Android returns false
     * after a permanent denial *and* before the very first ask, but we only reach here after an answer,
     * so false here means permanent.
     */
    private fun classify(
        host: Activity,
        manifestPermissions: List<String>,
        results: Map<String, Boolean>,
    ): PermissionResult {
        if (manifestPermissions.all { results[it] == true }) return PermissionResult.Granted

        val refused = manifestPermissions.filter { results[it] != true }
        val anyStillAskable = refused.any { ActivityCompat.shouldShowRequestPermissionRationale(host, it) }
        return if (anyStillAskable) PermissionResult.Denied else PermissionResult.DeniedPermanently
    }

    private companion object {
        /** Registry keys must be unique per registration; a monotonic counter is enough. */
        private var registrationCounter = 0
    }
}

/**
 * The Android manifest permissions a [Permission] resolves to.
 *
 * [Permission.Storage] is version-dependent: API 33 replaced the broad external-storage pair with
 * scoped media permissions, and requesting the legacy pair on 33+ is auto-denied.
 */
private fun Permission.manifestPermissions(): List<String> = when (this) {
    Permission.Camera -> listOf(Manifest.permission.CAMERA)
    Permission.Microphone -> listOf(Manifest.permission.RECORD_AUDIO)
    Permission.Location -> listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    Permission.Contacts -> listOf(Manifest.permission.READ_CONTACTS)

    // POST_NOTIFICATIONS only exists on API 33+; below it, notifications need no runtime grant,
    // so an empty list correctly reports "already granted".
    Permission.Notifications -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyList()
    }

    Permission.Storage -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }
}
