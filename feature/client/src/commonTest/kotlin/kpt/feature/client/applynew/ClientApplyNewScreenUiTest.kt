/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.applynew

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.AppTheme
import kpt.core.model.client.Client
import kpt.core.model.client.ClientStatus
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class ClientApplyNewScreenUiTest {

    private fun client(status: String? = ClientStatus.STATUS_ACTIVE) = ScreenState.Content(
        Client(
            id = 55,
            displayName = "Ankit Sharma",
            status = status?.let { ClientStatus(value = it) },
        ),
    )

    private val ported = listOf(
        ApplyNewItem.LOAN,
        ApplyNewItem.SAVINGS,
        ApplyNewItem.RECURRING_DEPOSIT,
    )

    @Test
    fun onlyPortedAccountTypesAreDrawn() = runComposeUiTest {
        setContent {
            AppTheme {
                ClientApplyNewContent(client = client(), available = ported, onAction = {})
            }
        }
        ported.forEach { onNodeWithTag(ClientApplyNewTestTags.row(it)).assertExists() }
        // Share and fixed deposit are not ported, so their rows are ABSENT — the original drew all
        // five, so in this fork three of them would have opened nothing.
        onNodeWithTag(ClientApplyNewTestTags.row(ApplyNewItem.SHARE)).assertDoesNotExist()
        onNodeWithTag(ClientApplyNewTestTags.row(ApplyNewItem.FIXED_DEPOSIT)).assertDoesNotExist()
    }

    @Test
    fun aRowDispatchesItsOwnType() = runComposeUiTest {
        val sent = mutableListOf<ClientApplyNewAction>()
        setContent {
            AppTheme {
                ClientApplyNewContent(client = client(), available = ported, onAction = { sent += it })
            }
        }
        onNodeWithTag(ClientApplyNewTestTags.row(ApplyNewItem.RECURRING_DEPOSIT)).performClick()
        assertEquals(
            listOf<ClientApplyNewAction>(
                ClientApplyNewAction.OnItemClick(ApplyNewItem.RECURRING_DEPOSIT),
            ),
            sent,
        )
    }

    @Test
    fun anInactiveClientCannotApply() = runComposeUiTest {
        setContent {
            AppTheme {
                ClientApplyNewContent(
                    client = client(status = "Pending"),
                    available = ported,
                    onAction = {},
                )
            }
        }
        // Fineract refuses an application for a client that is not active.
        onNodeWithTag(ClientApplyNewTestTags.INACTIVE).assertIsDisplayed()
        ported.forEach { onNodeWithTag(ClientApplyNewTestTags.row(it)).assertDoesNotExist() }
    }

    @Test
    fun anUnknownStatusFallsToNotActive() = runComposeUiTest {
        setContent {
            AppTheme {
                ClientApplyNewContent(client = client(status = null), available = ported, onAction = {})
            }
        }
        // The safe direction: a status the read did not carry must not open an application the
        // server will refuse.
        onNodeWithTag(ClientApplyNewTestTags.INACTIVE).assertIsDisplayed()
    }

    @Test
    fun noPortedTypesSaysSoRatherThanShowingAnEmptyPage() = runComposeUiTest {
        setContent {
            AppTheme {
                ClientApplyNewContent(client = client(), available = emptyList(), onAction = {})
            }
        }
        onNodeWithTag(ClientApplyNewTestTags.NONE_AVAILABLE).assertExists()
    }
}
