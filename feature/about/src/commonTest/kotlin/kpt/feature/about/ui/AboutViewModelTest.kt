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

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AboutViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val mifos = AboutConfig(
        appName = "MifosXDroid",
        versionName = "2026.10.24",
        orgName = "Mifos Initiative",
        orgEmail = "team@mifos.org",
        websiteUrl = "https://mifos.org",
        sourceUrl = "https://github.com/openMF/mifos-x-field-officer-app/",
        communityUrl = "https://mifos.slack.com/",
        productUrl = "https://mifos-products.pages.dev/mobile/field-officer/",
    )

    @Test
    fun theIdentityIsTheForksNotALiteral() {
        val state = AboutState.of(mifos, year = 2026)
        // The original printed "App Version 1.0.1" and "©2016–2022" no matter what shipped.
        assertEquals("2026.10.24", state.versionName)
        assertEquals("Mifos Initiative", state.orgName)
        assertEquals(2026, state.copyrightYear)
    }

    @Test
    fun linksAreDerivedFromTheConfiguredValuesInOrder() {
        val links = aboutLinks(mifos)
        assertEquals(
            listOf(
                // Help first — it is what an officer opens About for.
                AboutLinkKind.PRODUCT_SITE to "https://mifos-products.pages.dev/mobile/field-officer/",
                AboutLinkKind.COMMUNITY to "https://mifos.slack.com/",
                AboutLinkKind.WEBSITE to "https://mifos.org",
                AboutLinkKind.CONTACT to "mailto:team@mifos.org",
                AboutLinkKind.SOURCE_CODE to "https://github.com/openMF/mifos-x-field-officer-app",
                AboutLinkKind.CONTRIBUTORS to "https://github.com/openMF/mifos-x-field-officer-app/graphs/contributors",
                // The license TEXT — never a repo file, which 404'd on device (`LICENCE` vs `LICENSE`).
                AboutLinkKind.LICENSE to "https://mozilla.org/MPL/2.0/",
            ),
            links.map { it.kind to it.url },
        )
        assertEquals("mifos.org", links.single { it.kind == AboutLinkKind.WEBSITE }.detail)
    }

    @Test
    fun anUnconfiguredValueDrawsNoRowAndANonGitHubSourceGetsNoGitHubRows() {
        val bare = mifos.copy(
            orgEmail = "",
            websiteUrl = "",
            sourceUrl = "",
            licenseUrl = "",
            communityUrl = "",
            productUrl = "",
        )
        assertTrue(aboutLinks(bare).isEmpty())
        val gitlabOnly = mifos.copy(
            orgEmail = "",
            websiteUrl = "",
            communityUrl = "",
            productUrl = "",
            sourceUrl = "https://gitlab.com/mifos/app",
        )
        // /graphs/contributors is a GitHub path — elsewhere it would 404. The license still shows.
        assertEquals(
            listOf(AboutLinkKind.SOURCE_CODE, AboutLinkKind.LICENSE),
            aboutLinks(gitlabOnly).map { it.kind },
        )
    }

    @Test
    fun tapsBecomeTheRightEvents() = runTest(dispatcher) {
        val vm = AboutViewModel(mifos)
        vm.eventFlow.test {
            vm.trySendAction(
                AboutAction.LinkClicked(vm.stateFlow.value.links.first { it.kind == AboutLinkKind.LICENSE }),
            )
            runCurrent()
            assertEquals(AboutEvent.OpenUrl("https://mozilla.org/MPL/2.0/"), awaitItem())
            vm.trySendAction(
                AboutAction.LinkClicked(vm.stateFlow.value.links.first { it.kind == AboutLinkKind.COMMUNITY }),
            )
            runCurrent()
            assertEquals(AboutEvent.OpenUrl("https://mifos.slack.com/"), awaitItem())
            vm.trySendAction(AboutAction.VersionClicked)
            runCurrent()
            assertEquals(AboutEvent.OpenAppInfo, awaitItem())
            vm.trySendAction(AboutAction.NavigateBack)
            runCurrent()
            assertEquals(AboutEvent.NavigateBack, awaitItem())
        }
    }
}
