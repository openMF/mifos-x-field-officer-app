/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.groups.detail

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.base.store.screen.ScreenState
import kpt.core.designsystem.theme.KptTheme
import kpt.core.model.group.Group
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The overflow menu. Every one of its three destinations had a working route, action, event and
 * ViewModel handler BEFORE the menu existed — so each test here is the proof that the chain is now
 * reachable, which is the half the port had been missing.
 */
@OptIn(ExperimentalTestApi::class)
class GroupsDetailScreenUiTest {

    private val group = ScreenState.Content(
        Group(id = 31, name = "Ankit", accountNo = "000000031", active = true),
    )

    @Test
    fun theMenuIsAbsentWhenNothingItOpensIsPorted() = runComposeUiTest {
        setContent {
            KptTheme {
                GroupsDetailContent(group = group, canActivate = false, onAction = {})
            }
        }
        // Not an empty menu — no icon at all. All three flags default to false.
        onNodeWithTag(GroupsDetailTestTags.OVERFLOW).assertDoesNotExist()
    }

    @Test
    fun eachRowAppearsOnlyWhenItsDestinationIsWired() = runComposeUiTest {
        setContent {
            KptTheme {
                GroupsDetailContent(
                    group = group,
                    canActivate = false,
                    canOpenDocuments = true,
                    canAddLoanAccount = false,
                    canOpenDataTables = false,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(GroupsDetailTestTags.OVERFLOW).assertIsDisplayed().performClick()

        onNodeWithTag(GroupsDetailTestTags.DOCUMENTS).assertExists()
        // `feature/savings` is S5 and the associate-clients list is unported, so those rows are
        // absent rather than drawn dead — the same rule the loan vertical's menus follow.
        onNodeWithTag(GroupsDetailTestTags.ADD_LOAN_ACCOUNT).assertDoesNotExist()
        onNodeWithTag(GroupsDetailTestTags.DATA_TABLES).assertDoesNotExist()
    }

    @Test
    fun addLoanAccountDispatchesTheActionTheViewModelAlreadyHandles() = runComposeUiTest {
        val sent = mutableListOf<GroupsDetailAction>()
        setContent {
            KptTheme {
                GroupsDetailContent(
                    group = group,
                    canActivate = false,
                    canAddLoanAccount = true,
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(GroupsDetailTestTags.OVERFLOW).performClick()
        onNodeWithTag(GroupsDetailTestTags.ADD_LOAN_ACCOUNT).performClick()

        assertEquals(listOf<GroupsDetailAction>(GroupsDetailAction.AddLoanAccountClicked), sent)
    }

    @Test
    fun documentsAndDataTablesDispatchTheirOwnActions() = runComposeUiTest {
        val sent = mutableListOf<GroupsDetailAction>()
        setContent {
            KptTheme {
                GroupsDetailContent(
                    group = group,
                    canActivate = false,
                    canOpenDocuments = true,
                    canOpenDataTables = true,
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(GroupsDetailTestTags.OVERFLOW).performClick()
        onNodeWithTag(GroupsDetailTestTags.DOCUMENTS).performClick()
        onNodeWithTag(GroupsDetailTestTags.OVERFLOW).performClick()
        onNodeWithTag(GroupsDetailTestTags.DATA_TABLES).performClick()

        // Two DIFFERENT actions. Both chains existed and neither had a caller until this menu.
        assertEquals(
            listOf<GroupsDetailAction>(
                GroupsDetailAction.DocumentsClicked,
                GroupsDetailAction.DataTablesClicked,
            ),
            sent,
        )
    }

    @Test
    fun theMenuClosesAfterAChoice() = runComposeUiTest {
        setContent {
            KptTheme {
                GroupsDetailContent(
                    group = group,
                    canActivate = false,
                    canOpenDocuments = true,
                    onAction = {},
                )
            }
        }
        onNodeWithTag(GroupsDetailTestTags.OVERFLOW).performClick()
        onNodeWithTag(GroupsDetailTestTags.DOCUMENTS).performClick()

        // The original set `showMenu = false` in every item's onClick; kept, because a menu left
        // open over the destination it just navigated to is the defect that pattern avoids.
        onNodeWithTag(GroupsDetailTestTags.DOCUMENTS).assertDoesNotExist()
    }

    @Test
    fun activateStillRendersBesideTheMenu() = runComposeUiTest {
        setContent {
            KptTheme {
                GroupsDetailContent(
                    group = group,
                    canActivate = true,
                    canAddLoanAccount = true,
                    onAction = {},
                )
            }
        }
        // Scrolled to: the button is below the group's fields. This assertion is what found that
        // the Column had no scroll at all, which made Activate unreachable rather than merely
        // off-window — see the screen's comment.
        onNodeWithTag(GroupsDetailTestTags.ACTIVATE).performScrollTo().assertIsDisplayed()
        onNodeWithTag(GroupsDetailTestTags.OVERFLOW).assertIsDisplayed()
    }
}
