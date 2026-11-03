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

import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kpt.core.base.ui.viewmodel.BaseViewModel
import kpt.core.model.config.AppOrgLinks
import kpt.feature.about.BuildKonfig
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Ported from `6b66e8a43:feature/about` (a stateless screen + a hardcoded `aboutItems` list).
 *
 * What changed, and why:
 *  - **Identity comes from app-profile.** The original printed "MifosXDroid", "App Version 1.0.1",
 *    "©2016–2022 Mifos Initiative" and five URLs as literals. The version had not been 1.0.1 for
 *    years and the copyright had stopped in 2022. Here every value is the fork's own
 *    (`app-profile/app.yaml` → BuildKonfig, see `build.gradle.kts`), and the year is the current one.
 *  - **Links are data, and only real ones are drawn.** A row whose URL is not configured is
 *    absent, not dead — the original's Twitter row is gone because the fork declares no such
 *    account, and Contributors/License are derived only when the source repo is on GitHub.
 *  - **Taps are events.** The original called `ShareUtils.openUrl` inside the composable; routing
 *    them through the ViewModel makes "which link does this row open" a tested fact.
 */
internal class AboutViewModel(
    config: AboutConfig = AboutConfig.fromBuild(),
) : BaseViewModel<AboutState, AboutEvent, AboutAction>(AboutState.of(config)) {

    override fun handleAction(action: AboutAction) {
        when (action) {
            AboutAction.NavigateBack -> sendEvent(AboutEvent.NavigateBack)
            AboutAction.VersionClicked -> sendEvent(AboutEvent.OpenAppInfo)
            is AboutAction.LinkClicked -> sendEvent(AboutEvent.OpenUrl(action.link.url))
        }
    }
}

/** The build-time identity the screen shows. A plain value so tests and previews pass their own. */
internal data class AboutConfig(
    val appName: String,
    val versionName: String,
    val orgName: String,
    val orgEmail: String,
    val websiteUrl: String,
    val sourceUrl: String,
    /** Where an officer asks for help — the Mifos Slack, `#mobile` channel. */
    val communityUrl: String = "",
    /** The product's own site: guides, features and release information. */
    val productUrl: String = "",
    /** The license TEXT. Defaults to MPL-2.0's — the URL every source-file header here cites. */
    val licenseUrl: String = MPL_2_0_URL,
) {
    companion object {
        fun fromBuild() = AboutConfig(
            appName = BuildKonfig.APP_DISPLAY_NAME,
            versionName = BuildKonfig.VERSION_NAME,
            orgName = BuildKonfig.ORG_NAME,
            orgEmail = BuildKonfig.ORG_EMAIL,
            websiteUrl = BuildKonfig.WEBSITE_URL,
            sourceUrl = BuildKonfig.SOURCE_URL,
            communityUrl = AppOrgLinks["community"].orEmpty(),
            productUrl = AppOrgLinks["product"].orEmpty(),
            licenseUrl = BuildKonfig.LICENSE_URL,
        )
    }
}

/** Declaration order is the on-screen order — help first: it is what an officer opens About for. */
internal enum class AboutSection { HELP, ORGANISATION, OPEN_SOURCE }

internal enum class AboutLinkKind(val section: AboutSection) {
    PRODUCT_SITE(AboutSection.HELP),
    COMMUNITY(AboutSection.HELP),
    WEBSITE(AboutSection.ORGANISATION),
    CONTACT(AboutSection.ORGANISATION),
    SOURCE_CODE(AboutSection.OPEN_SOURCE),
    CONTRIBUTORS(AboutSection.OPEN_SOURCE),
    LICENSE(AboutSection.OPEN_SOURCE),
}

/** One row: what it is, the address it opens, and the text shown under its title. */
internal data class AboutLink(val kind: AboutLinkKind, val url: String, val detail: String? = null)

internal data class AboutState(
    val appName: String,
    val versionName: String,
    val orgName: String,
    val copyrightYear: Int,
    val links: List<AboutLink>,
) {
    companion object {
        @OptIn(ExperimentalTime::class)
        fun of(
            config: AboutConfig,
            year: Int = Clock.System.todayIn(TimeZone.currentSystemDefault()).year,
        ) = AboutState(
            appName = config.appName,
            versionName = config.versionName,
            orgName = config.orgName,
            copyrightYear = year,
            links = aboutLinks(config),
        )
    }
}

/**
 * The rows to draw, in order. Unconfigured values produce no row. Contributors is a GitHub
 * convention (`/graphs/contributors`), so it appears only for a GitHub source URL.
 *
 * License links the license TEXT, not a file in the repo: `/blob/HEAD/LICENSE` opened a 404 on
 * device (2026-10-24) — the default branch names it `LICENCE` — and any repo-file guess breaks on
 * the next rename.
 */
internal fun aboutLinks(config: AboutConfig): List<AboutLink> {
    val source = config.sourceUrl.trim().trimEnd('/')
    val onGitHub = source.startsWith("https://github.com/")
    return listOfNotNull(
        config.productUrl.trim().takeIf { it.isNotEmpty() }?.let { AboutLink(AboutLinkKind.PRODUCT_SITE, it) },
        config.communityUrl.trim().takeIf { it.isNotEmpty() }?.let { AboutLink(AboutLinkKind.COMMUNITY, it) },
        config.websiteUrl.trim().takeIf { it.isNotEmpty() }
            ?.let {
                AboutLink(
                    AboutLinkKind.WEBSITE,
                    it,
                    detail = it.removePrefix("https://").removePrefix("http://"),
                )
            },
        config.orgEmail.trim().takeIf { it.isNotEmpty() }
            ?.let { AboutLink(AboutLinkKind.CONTACT, "mailto:$it", detail = it) },
        source.takeIf { it.isNotEmpty() }?.let { AboutLink(AboutLinkKind.SOURCE_CODE, it) },
        source.takeIf { onGitHub }?.let { AboutLink(AboutLinkKind.CONTRIBUTORS, "$it/graphs/contributors") },
        config.licenseUrl.trim().takeIf { it.isNotEmpty() }?.let { AboutLink(AboutLinkKind.LICENSE, it) },
    )
}

internal sealed interface AboutEvent {
    data object NavigateBack : AboutEvent
    data object OpenAppInfo : AboutEvent
    data class OpenUrl(val url: String) : AboutEvent
}

internal sealed interface AboutAction {
    data object NavigateBack : AboutAction
    data object VersionClicked : AboutAction
    data class LinkClicked(val link: AboutLink) : AboutAction
}

internal const val MPL_2_0_URL = "https://mozilla.org/MPL/2.0/"
