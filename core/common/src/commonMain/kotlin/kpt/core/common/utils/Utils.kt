/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

import kotlinx.datetime.LocalDate

object Utils {
    private const val INVALID_DATE = "Invalid Date"

    fun getStringOfDate(dateObj: List<Int?>): String {
        val year = dateObj.getOrNull(0)
        val month = dateObj.getOrNull(1)
        val day = dateObj.getOrNull(2)

        return if (year == null || month == null || day == null) {
            INVALID_DATE
        } else {
            try {
                val date = LocalDate(year, month, day)
                val monthFormatted = date.month.name.lowercase().replaceFirstChar { it.uppercase() }
                "${date.day} $monthFormatted ${date.year}"
            } catch (e: Exception) {
                INVALID_DATE
            }
        }
    }
}
