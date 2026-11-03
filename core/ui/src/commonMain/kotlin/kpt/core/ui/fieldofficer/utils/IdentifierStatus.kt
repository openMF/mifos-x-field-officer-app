/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.fieldofficer.utils

import kpt.core.ui.components.Status

/** The listing status of an identifier's status text, or null when there is none. */
fun getClientIdentifierStatus(status: String?): Status? {
    return if (status != null) {
        if (status.lowercase().endsWith("inactive")) {
            Status.Inactive
        } else if (status.lowercase()
                .endsWith("active")
        ) {
            Status.Active
        } else {
            Status.Pending
        }
    } else {
        null
    }
}
