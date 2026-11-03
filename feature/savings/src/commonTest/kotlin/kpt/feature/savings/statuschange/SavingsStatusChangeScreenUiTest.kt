/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.savings.statuschange

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import kpt.core.designsystem.theme.KptTheme
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SavingsStatusChangeScreenUiTest {

    @Test
    fun approvalOffersANoteAndActivationDoesNot() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsStatusChangeContent(
                    state = SavingsStatusChangeState(change = SavingsStatusChange.APPROVE),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsStatusChangeTestTags.DATE).assertIsDisplayed()
        onNodeWithTag(SavingsStatusChangeTestTags.NOTE).assertExists()
    }

    @Test
    fun activationHasNoNoteField() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsStatusChangeContent(
                    state = SavingsStatusChangeState(change = SavingsStatusChange.ACTIVATE),
                    onAction = {},
                )
            }
        }
        // Fineract's activate command takes no note, so offering a field whose value would be
        // dropped would be the dead-input defect this port keeps finding.
        onNodeWithTag(SavingsStatusChangeTestTags.NOTE).assertDoesNotExist()
        onNodeWithTag(SavingsStatusChangeTestTags.DATE).assertIsDisplayed()
    }

    @Test
    fun submitDispatches() = runComposeUiTest {
        val sent = mutableListOf<SavingsStatusChangeAction>()
        setContent {
            KptTheme {
                SavingsStatusChangeContent(
                    state = SavingsStatusChangeState(),
                    onAction = { sent += it },
                )
            }
        }
        onNodeWithTag(SavingsStatusChangeTestTags.SUBMIT).performClick()
        assertEquals(listOf<SavingsStatusChangeAction>(SavingsStatusChangeAction.OnSubmit), sent)
    }

    @Test
    fun submitIsBlockedWhileOneIsInFlight() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsStatusChangeContent(
                    state = SavingsStatusChangeState(isSubmitting = true),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsStatusChangeTestTags.SUBMIT).assertIsNotEnabled()
    }

    @Test
    fun theFormSurvivesAFailure() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsStatusChangeContent(
                    state = SavingsStatusChangeState(
                        note = "Verified passbook",
                        dialogState = DialogState.Failed(offline = true, msg = null),
                    ),
                    onAction = {},
                )
            }
        }
        // Dialog ON TOP of the form, not instead of it — the original replaced the whole screen.
        onNodeWithTag(SavingsStatusChangeTestTags.STATUS_DIALOG).assertExists()
        onNodeWithTag(SavingsStatusChangeTestTags.DATE).assertExists()
        onNodeWithTag(SavingsStatusChangeTestTags.NOTE).assertExists()
    }

    @Test
    fun successIsStatedRatherThanLeavingSilently() = runComposeUiTest {
        setContent {
            KptTheme {
                SavingsStatusChangeContent(
                    state = SavingsStatusChangeState(dialogState = DialogState.Succeeded),
                    onAction = {},
                )
            }
        }
        onNodeWithTag(SavingsStatusChangeTestTags.STATUS_DIALOG).assertExists()
    }
}
