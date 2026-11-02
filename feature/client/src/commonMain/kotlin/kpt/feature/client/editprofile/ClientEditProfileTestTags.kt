/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.editprofile

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientEditProfileTestTags {
    const val SCREEN = "clientEditProfile:screen"
    const val IMAGE = "clientEditProfile:image"
    const val DELETE = "clientEditProfile:delete"
    const val UPLOAD = "clientEditProfile:upload"
    const val WRITE_ERROR_DIALOG = "clientEditProfile:writeError"
    const val SUCCESS_DIALOG = "clientEditProfile:uploadSucceeded"

    // The bottom-sheet options carry no tags: `AppBottomSheetOptionItem` takes no `modifier`, so
    // tagging them means widening a core/designsystem component — which would flow upstream per
    // RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001. Same call the signature sibling made.
}
