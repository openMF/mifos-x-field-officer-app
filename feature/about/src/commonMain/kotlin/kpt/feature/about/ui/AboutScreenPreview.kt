/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.about.ui

import androidx.compose.runtime.Composable
import kpt.core.designsystem.theme.KptTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

// i18n:skip — preview fixtures (the real values come from app-profile via BuildKonfig)
private val previewConfig = AboutConfig(
    appName = "MifosXDroid",
    versionName = "2026.10.24",
    orgName = "Mifos Initiative",
    orgEmail = "team@mifos.org",
    websiteUrl = "https://mifos.org",
    sourceUrl = "https://github.com/openMF/mifos-x-field-officer-app",
    communityUrl = "https://mifos.slack.com/",
    productUrl = "https://mifos-products.pages.dev/mobile/field-officer/",
)

@Preview
@Composable
internal fun AboutScreenPreview() {
    KptTheme {
        AboutContent(state = AboutState.of(previewConfig, year = 2026), onAction = {})
    }
}

/** A fork that declares no links: no section headings, no dead rows — just the identity. */
@Preview
@Composable
internal fun AboutScreenMinimalPreview() {
    KptTheme {
        AboutContent(
            state = AboutState.of(
                previewConfig.copy(
                    orgEmail = "",
                    websiteUrl = "",
                    sourceUrl = "",
                    licenseUrl = "",
                    communityUrl = "",
                    productUrl = "",
                ),
                year = 2026,
            ),
            onAction = {},
        )
    }
}
