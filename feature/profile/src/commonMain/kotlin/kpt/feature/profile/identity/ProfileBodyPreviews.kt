/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.profile.identity

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import kpt.core.base.designsystem.theme.KptTheme as KptBaseTheme

// The gateway's own demo officer (users/32), measured 2026-10-27 — the identity the Stitch renders use.
private val irving = ProfileUiState(
    username = "fieldofficer",
    initials = "IZ",
    officeName = "CENTRAL",
    fullName = "IRVING ZEPEDA",
    email = "oficial.campo@gmail.com",
    roles = listOf("OFICIAL DE CAMPO"),
    name = NameState.Loaded,
    server = ServerSession(
        instanceName = "Mifos field officer gateway",
        known = true,
        url = "https://apis.mifos.community/1.0/field/v1/",
        tenant = "mifos-bank-1",
    ),
)

@Composable
private fun Render(state: ProfileUiState) = KptTheme(darkTheme = false) {
    Surface(color = KptBaseTheme.colorScheme.background) {
        ProfileBodyContent(
            state = state.copy(password = null),
            onAction = {},
            onBack = {},
            onServerSettings = {},
            onSignOut = {},
        )
    }
}

@Preview @Composable
internal fun ProfileContentPreview() = Render(irving)

@Preview @Composable
internal fun ProfileLoadingPreview() = Render(
    irving.copy(fullName = null, email = null, roles = emptyList(), initials = "F", name = NameState.Loading),
)

@Preview @Composable
internal fun ProfileErrorPreview() = Render(
    irving.copy(
        fullName = null,
        email = null,
        initials = "F",
        name = NameState.Failed("Server error (500). Try again."),
    ),
)

@Preview @Composable
internal fun ProfileOfflinePreview() = Render(irving.copy(online = false))

@Preview @Composable
internal fun ProfileStaffLinkedPreview() = Render(
    irving.copy(
        fullName = "Mariana Ortega",
        initials = "MO",
        username = "mortega",
        email = null,
        officeName = "Morelia Norte",
        hasStaffLink = true,
        staffName = "Mariana Ortega",
        isLoanOfficer = true,
    ),
)

@Preview @Composable
internal fun ProfileChangePasswordPreview() = KptTheme(darkTheme = false) {
    Surface(color = KptBaseTheme.colorScheme.surface) {
        PasswordSheetBody(form = PasswordForm(new = "Field#Office2026", repeat = "Field#Office20"), onAction = {})
    }
}

@Preview @Composable
internal fun ProfilePasswordRefusedPreview() = KptTheme(darkTheme = false) {
    Surface(color = KptBaseTheme.colorScheme.surface) {
        PasswordSheetBody(
            form = PasswordForm(
                new = "abc",
                repeat = "abc",
                error = PasswordError.Server(
                    "Password must be 12 to 50 characters long, containing at least one uppercase letter, " +
                        "one lowercase letter, one numeric digit, and one special character, " +
                        "with no spaces or consecutive repeating characters",
                ),
            ),
            onAction = {},
        )
    }
}
