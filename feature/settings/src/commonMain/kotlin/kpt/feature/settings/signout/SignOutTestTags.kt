/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings.signout

/** Tags for the sign-out row and its confirm sheet. */
object SignOutTestTags {
    /** The settings row that starts sign-out. */
    const val ROW = "settings.sign_out"

    /** The confirm sheet's sign-out button. */
    const val CONFIRM = "settings.sign_out.confirm"

    /** The "keep my data" choice. */
    const val KEEP_DATA = "settings.sign_out.keep_data"

    /** The "remove my data" choice. */
    const val CLEAR_DATA = "settings.sign_out.clear_data"

    /** The message shown when sign-out did not finish. */
    const val FAILED = "settings.sign_out.failed"
}
