/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.center.create

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.office.Office
import org.jetbrains.compose.ui.tooling.preview.Preview

// i18n:skip — preview fixtures
private val offices = ScreenState.Content(listOf(Office(id = 1, name = "Head Office")))

@Preview
@Composable
internal fun CreateCenterScreenPreview() {
    KptTheme {
        CreateCenterContent(
            // i18n:skip — preview fixture
            state = CreateCenterState(
                name = "North Market",
                officeId = 1,
                officeName = "Head Office",
                active = true,
                activationDateMillis = 1_792_756_800_000L,
            ),
            offices = offices,
            onAction = {},
        )
    }
}

/** The original's validation errors, shown on Submit next to their fields. */
@Preview
@Composable
internal fun CreateCenterScreenErrorsPreview() {
    KptTheme {
        CreateCenterContent(
            // i18n:skip — preview fixture
            state = CreateCenterState(
                name = "Ab",
                nameError = CenterNameError.TOO_SHORT,
                officeMissing = true,
                activationDateMillis = 1_792_756_800_000L,
            ),
            offices = offices,
            onAction = {},
        )
    }
}
