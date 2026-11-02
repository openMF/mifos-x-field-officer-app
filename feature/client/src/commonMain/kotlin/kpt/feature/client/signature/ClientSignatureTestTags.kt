/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.signature

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientSignatureTestTags {
    const val SCREEN = "clientSignature:screen"
    const val IMAGE = "clientSignature:image"
    const val DELETE = "clientSignature:delete"
    const val UPLOAD = "clientSignature:upload"

    // The bottom-sheet options carry no tags: `AppBottomSheetOptionItem` takes no `modifier`.
    // Tagging them means widening that component, which is core/designsystem and would flow
    // upstream — deferred rather than done for test convenience.
    const val WRITE_ERROR_DIALOG = "clientSignature:writeError"
}
