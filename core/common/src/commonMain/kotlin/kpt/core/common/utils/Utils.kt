/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

import kotlinx.datetime.LocalDate

object Utils {
    fun getStringOfDate(dateObj: List<Int?>): String {
        val year = dateObj.getOrNull(0) ?: return "Invalid Date"
        val month = dateObj.getOrNull(1) ?: return "Invalid Date"
        val day = dateObj.getOrNull(2) ?: return "Invalid Date"

        return try {
            val date = LocalDate(year, month, day)
            val monthFormatted = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
            "${date.day} $monthFormatted ${date.year}"
        } catch (e: Exception) {
            "Invalid Date"
        }
    }
}
