/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kpt.core.base.platform.context.LocalContext
import kpt.core.platform.permission.Permission
import kpt.core.platform.permission.PermissionRequester
import kpt.core.platform.permission.PermissionResult

/**
 * Gates [onGranted] behind runtime [permissions], explaining itself with a rationale dialog and
 * offering the settings screen once the user has refused permanently.
 *
 * ONE commonMain composable, deliberately. The surface this replaces was an `expect fun PermissionBox`
 * with five platform actuals — an expect/actual *Composable*, which CORE_PLATFORM.md names as a code
 * smell: Compose Multiplatform already runs from commonMain, so only the permission *mechanics* need
 * splitting, and those now live in `kpt.core.platform.permission.PermissionRequester`. Four of those
 * five actuals were `TODO()` and threw at runtime.
 *
 * Asks once on first composition, then only when the user confirms the rationale — never on a
 * lifecycle ON_START observer. The old implementation re-launched the system dialog on every
 * ON_START while un-granted, so backgrounding the app re-prompted indefinitely.
 */
@Composable
fun PermissionBox(
    permissions: List<Permission>,
    title: String,
    confirmButtonText: String,
    dismissButtonText: String,
    description: String? = null,
    onGranted: @Composable (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val requester = remember(context) { PermissionRequester(context) }
    val scope = rememberCoroutineScope()

    var granted by remember(permissions) { mutableStateOf(requester.isGranted(permissions)) }
    var showRationale by remember(permissions) { mutableStateOf(false) }
    var showSettingsPrompt by remember(permissions) { mutableStateOf(false) }

    fun ask() = scope.launch {
        when (requester.request(permissions)) {
            PermissionResult.Granted -> {
                granted = true
                showRationale = false
                showSettingsPrompt = false
            }
            // Refused but askable — explain, and let the user opt into a second attempt.
            PermissionResult.Denied -> showRationale = true
            // Only Settings can undo this; re-requesting shows the user nothing.
            PermissionResult.DeniedPermanently -> showSettingsPrompt = true
        }
    }

    // Ask once per permission set, not on every recomposition and not on every ON_START.
    LaunchedEffect(permissions) {
        if (!granted) ask()
    }

    MifosDialogBox(
        showDialogState = showRationale,
        onDismiss = { showRationale = false },
        title = title,
        message = description,
        confirmButtonText = confirmButtonText,
        onConfirm = {
            showRationale = false
            ask()
        },
        dismissButtonText = dismissButtonText,
    )

    MifosDialogBox(
        showDialogState = showSettingsPrompt,
        onDismiss = { showSettingsPrompt = false },
        title = title,
        message = description,
        confirmButtonText = confirmButtonText,
        onConfirm = {
            showSettingsPrompt = false
            // A navigation side effect, fired from a click handler rather than from composition.
            // The old code called startActivity() directly in the composable body, so it re-opened
            // the settings screen on every recomposition while the flag was set.
            requester.openAppSettings()
        },
        dismissButtonText = dismissButtonText,
    )

    if (granted) {
        onGranted?.invoke()
    }
}
