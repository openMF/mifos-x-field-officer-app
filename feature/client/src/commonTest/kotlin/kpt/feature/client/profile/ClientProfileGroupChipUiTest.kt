/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.profile

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.client.Client
import kpt.core.model.group.Group
import kpt.feature.client.awaitResourceText
import kotlin.test.Test
import kotlin.test.assertEquals

/** The profile's Group chip reads Fineract's `groups` list — the scalar fields are never sent. */
@OptIn(ExperimentalTestApi::class)
class ClientProfileGroupChipUiTest {

    @Test
    fun aGroupMemberNamesItsGroupAndTheChipOpensIt() = runComposeUiTest {
        val actions = mutableListOf<ClientProfileAction>()
        val member = Client(id = 5, displayName = "Asha", groups = listOf(Group(id = 2, name = "MARIPOSAS MONARCAS")))
        setContent {
            KptTheme {
                ClientProfileContent(ClientProfileState(), ScreenState.Content(member), { actions += it }, emptyList())
            }
        }
        onNodeWithText("MARIPOSAS MONARCAS", substring = true).assertIsDisplayed().performClick()
        assertEquals(listOf<ClientProfileAction>(ClientProfileAction.OnGroupClick(2)), actions)
    }

    @Test
    fun aClientWithNoGroupNeverOpensGroupZero() = runComposeUiTest {
        val actions = mutableListOf<ClientProfileAction>()
        setContent {
            KptTheme {
                ClientProfileContent(
                    ClientProfileState(),
                    ScreenState.Content(Client(id = 5, displayName = "Asha")),
                    { actions += it },
                    emptyList(),
                )
            }
        }
        awaitResourceText("Group: N/A")
        onNodeWithText("Group: N/A").performClick()
        assertEquals(emptyList(), actions)
    }
}
