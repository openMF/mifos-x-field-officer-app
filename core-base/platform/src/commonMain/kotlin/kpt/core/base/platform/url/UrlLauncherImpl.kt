/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.url

import com.mobilebytelabs.kmptoolkit.openurl.canOpen
import com.mobilebytelabs.kmptoolkit.openurl.openInBrowser
import com.mobilebytelabs.kmptoolkit.openurl.openUrl

/**
 * The one [UrlLauncher], for every target.
 *
 * `cmp-open-url` carries the per-target `actual`s, so there is no source-set split here.
 */
class UrlLauncherImpl : UrlLauncher {
    override fun open(url: String): Boolean = openUrl(url)
    override fun openInBrowser(url: String): Boolean = openInBrowser(url)
    override fun canOpen(url: String): Boolean = canOpen(url)
}
