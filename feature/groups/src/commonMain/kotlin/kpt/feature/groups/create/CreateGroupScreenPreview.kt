/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.create

import androidx.compose.runtime.Composable
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.office.Office
import org.jetbrains.compose.ui.tooling.preview.Preview

/* @Preview siblings for the device-free CMP render tier (see GroupsListScreenPreview). */

/** A fixed instant so the rendered dates never change between runs (2025-10-09). */
private const val PREVIEW_DATE_MILLIS = 1_760_000_000_000L

// i18n:skip — preview fixtures
private val previewOffices = listOf(Office(id = 1, name = "Head Office"), Office(id = 2, name = "Branch A"))

@Preview
@Composable
internal fun CreateGroupContentPreview() {
    KptTheme {
        CreateGroupContent(
            state = CreateGroupState(
                // i18n:skip — preview fixture
                name = "Sunrise Group",
                office = OfficeOption(id = 1, label = "Head Office"),
                submissionDateMillis = PREVIEW_DATE_MILLIS,
                activationDateMillis = PREVIEW_DATE_MILLIS,
            ),
            offices = ScreenState.Content(previewOffices),
            onAction = {},
        )
    }
}

@Preview
@Composable
internal fun CreateGroupContentValidationPreview() {
    // Submit pressed with nothing filled: both required-field errors show.
    KptTheme {
        CreateGroupContent(
            state = CreateGroupState(
                submissionDateMillis = PREVIEW_DATE_MILLIS,
                activationDateMillis = PREVIEW_DATE_MILLIS,
                nameError = true,
                officeError = true,
            ),
            offices = ScreenState.Content(previewOffices),
            onAction = {},
        )
    }
}
