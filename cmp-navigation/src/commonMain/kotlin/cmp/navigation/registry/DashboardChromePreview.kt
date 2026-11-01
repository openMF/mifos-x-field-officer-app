/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.registry

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/*
 * @Preview siblings for the device-free CMP render tier (SCREENSHOT_TEST.md CMP-PRIMARY).
 *
 * The dashboard chrome is exactly the kind of surface this tier exists for. Its defect was a RENDER
 * defect — a bar drawn twice because the tab root already had one — invisible to a state assertion
 * and found by looking at a picture, so it gets a committed picture to compare against.
 *
 * The stateless halves are previewed; the shell that resolves a ViewModel through Koin is not.
 */

@Preview
@Composable
internal fun DashboardTopBarPreview() {
    KptTheme {
        // No `title` argument: the bar reads the fork's own display name from AppInfo, so the
        // preview renders exactly what ships rather than a fixture string.
        DashboardTopBar(
            onSearchIconClick = {},
            onNotificationIconClick = {},
        )
    }
}
