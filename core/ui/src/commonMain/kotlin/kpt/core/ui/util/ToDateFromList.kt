/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.util

fun List<Int?>?.toDateString(): String {
    if (this == null) return ""
    return if (this.size >= 3) {
        val year = this[0]
        val month = this[1].toString().padStart(2, '0')
        val day = this[2].toString().padStart(2, '0')
        "$day-$month-$year"
    } else {
        ""
    }
}
