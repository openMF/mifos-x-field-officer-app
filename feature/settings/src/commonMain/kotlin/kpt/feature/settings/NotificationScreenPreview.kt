/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier — see SettingsScreenPreview.kt for the
 * full rationale.
 *
 * `NotificationScreen` is the stateful wrapper; `NotificationScreenContent` is the stateless body
 * that carries the visuals, so it is what gets rendered here.
 */

@Preview
@Composable
internal fun NotificationScreenContentPreview() {
    KptTheme {
        NotificationScreenContent(onBackClick = {})
    }
}

@Preview
@Composable
internal fun NotificationScreenContentNarrowPreview() {
    // A narrow width is where the empty-state copy and the top bar title compete for space. The
    // default-width render above cannot show that wrap.
    KptTheme {
        NotificationScreenContent(
            modifier = Modifier.width(320.dp),
            onBackClick = {},
        )
    }
}
