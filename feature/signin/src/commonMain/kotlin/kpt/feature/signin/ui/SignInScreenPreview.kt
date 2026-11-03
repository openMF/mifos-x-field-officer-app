/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.signin.ui

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier: `CommonComposablePreviewScanner`
 * discovers these from commonMain and renders them off `desktopTest`. `SignInScreen` resolves its
 * ViewModel through Koin, so the stateless `SignInContent` is previewed. Host and username are
 * fixture values, not app copy — `// i18n:skip`.
 */

// i18n:skip — preview fixtures
private val previewState = SignInState(instanceHost = "sandbox.mifos.community", instanceTenant = "default")

@Preview
@Composable
internal fun SignInContentPreview() {
    KptTheme {
        SignInContent(state = previewState, onAction = {}, onUpdateServerConfig = {})
    }
}

@Preview
@Composable
internal fun SignInContentRejectedPreview() {
    // The server said no: the error shows and the remaining attempts are counted down.
    KptTheme {
        SignInContent(
            // i18n:skip — preview fixture
            state = previewState.copy(
                username = "mifos",
                password = "wrong",
                error = SignInError.Rejected,
                failedAttempts = 2,
            ),
            onAction = {},
            onUpdateServerConfig = {},
        )
    }
}

@Preview
@Composable
internal fun SignInContentLockedOutPreview() {
    KptTheme {
        SignInContent(
            state = previewState.copy(
                username = "mifos",
                isLockedOut = true,
                failedAttempts = 5,
                error = SignInError.LockedOut,
            ),
            onAction = {},
            onUpdateServerConfig = {},
        )
    }
}

@Preview
@Composable
internal fun SignInContentBiometricUnlockPreview() {
    // A stored session on biometric-capable hardware offers the unlock affordance.
    KptTheme {
        SignInContent(
            state = previewState.copy(username = "mifos", hasStoredSession = true, biometricAvailable = true),
            onAction = {},
            onUpdateServerConfig = {},
        )
    }
}
