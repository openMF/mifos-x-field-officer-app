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

import androidx.compose.runtime.Composable
import com.mobilebytelabs.kmptoolkit.appintents.compose.AppIntentsRegistry
import com.mobilebytelabs.kmptoolkit.intentlauncher.IntentOperation
import com.mobilebytelabs.kmptoolkit.intentlauncher.compose.rememberSupportsIntent
import com.mobilebytelabs.kmptoolkit.share.SharePayload
import com.mobilebytelabs.kmptoolkit.share.compose.ShareButton

// TEMPORARY probe: does a FEATURE module reach the three -compose libraries with no dependency
// of its own, purely through core-base/ui's `api`? Deleted after the check.
//
// ANSWER: yes — this file declares no dependency of its own and all three resolve.
@Composable
internal fun IntegrationProbe() {
    ShareButton(payload = SharePayload.Text("probe"))
    AppIntentsRegistry()
    @Suppress("UNUSED_VARIABLE")
    val supports = rememberSupportsIntent(IntentOperation.PickImage)
}
