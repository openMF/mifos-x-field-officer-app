/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.note.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import kotlinx.serialization.Serializable
import kpt.core.base.ui.nav.FeatureDestination
import kpt.core.base.ui.nav.composableWithPushTransitions
import kpt.core.base.ui.nav.popBackStackSafely
import kpt.core.common.utils.Constants
import kpt.feature.note.ui.AddEditNoteScreen
import kpt.feature.note.ui.NoteListScreen

/**
 * Notes attached to a client — ported from
 * `6b66e8a43:feature/note/src/commonMain/kotlin/com/mifos/feature/note/navigation/NoteNavigation.kt`.
 *
 * ## The owner is `(resourceType, resourceId)`
 *
 * The original routes carried `resourceId: Int` + `resourceType: String?` so one screen could serve
 * any owner, and it WAS used that way: client navigation passed `clients`, and the group graph wired
 * `loadNotes = navController::navigateToNoteScreen` (a method reference, which a text search for
 * call sites misses — an earlier note here said "two call sites, both clients", and was wrong).
 * So the routes carry the owner again, the `notes` store is keyed by it, and the `Note` table has a
 * `resourceType` column (v18). [navigateToClientNotes] stays for the client callers.
 *
 *
 * @property resourceType the Fineract resource the notes belong to (`clients`, `loans`, …)
 * @property resourceId the id of that resource
 */
@Serializable
data class NoteListRoute(val resourceType: String, val resourceId: Int)

/**
 * Add or edit one note.
 *
 * `noteId == null` means ADD. The original encoded the same thing and then branched on it in four
 * places inside the ViewModel; here it is read once into `editEnabled`.
 *
 * @property resourceType the Fineract resource the note belongs to
 * @property resourceId the id of that resource
 * @property noteId the note to edit, or null to add one
 */
@Serializable
data class AddEditNoteRoute(val resourceType: String, val resourceId: Int, val noteId: Long?)

/** One owner's notes — [resourceType] is Fineract's path (`clients`, `groups`). */
fun NavController.navigateToNotes(resourceType: String, resourceId: Int, navOptions: NavOptions? = null) =
    navigate(NoteListRoute(resourceType, resourceId), navOptions)

/** Opens client [clientId]'s notes. */
fun NavController.navigateToClientNotes(clientId: Int, navOptions: NavOptions? = null) =
    navigateToNotes(Constants.ENTITY_TYPE_CLIENTS, clientId, navOptions)

/** Opens the add-note form for one resource. */
fun NavController.navigateToAddNote(resourceType: String, resourceId: Int) =
    navigate(AddEditNoteRoute(resourceType, resourceId, noteId = null))

/** Opens the edit form for note [noteId] of one resource. */
fun NavController.navigateToEditNote(resourceType: String, resourceId: Int, noteId: Long) =
    navigate(AddEditNoteRoute(resourceType, resourceId, noteId))

/**
 * Register both note destinations into the caller's graph.
 *
 * The original exposed `noteDestination(navController, onBackPressed)` and `feature/client`
 * depended on `projects.feature.note` to call it. That dependency direction is preserved — client
 * is the hub that navigates into seven sibling feature modules, and reproducing that is what keeps
 * the remaining six slices mechanical.
 *
 * ## The list-refresh contract
 *
 * The original navigated BACK to the list with `popUpTo(inclusive) + launchSingleTop` so the list
 * would re-create and re-fetch after a write. That was necessary because its ViewModel fetched once
 * in `init` and had no way to be told the data changed.
 *
 * Here the add/edit screen just pops, and nothing re-reads on return: the write itself lands in
 * the `Note` table the list observes (`NoteCommandRepository` records it through the note stores),
 * and the list's stream stays live under the add/edit screen, so it already shows the change. An
 * earlier version passed a "note written" nav result to force a re-fetch, back when the commands
 * were cache-free.
 */
@FeatureDestination
fun NavGraphBuilder.noteDestinations(navController: NavController) {
    composableWithPushTransitions<NoteListRoute> {
        NoteListScreen(
            onBackClick = navController::popBackStackSafely,
            onAddNote = navController::navigateToAddNote,
            onEditNote = navController::navigateToEditNote,
        )
    }

    composableWithPushTransitions<AddEditNoteRoute> {
        AddEditNoteScreen(onBackClick = navController::popBackStackSafely)
    }
}
