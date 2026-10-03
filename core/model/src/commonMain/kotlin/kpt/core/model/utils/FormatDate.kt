/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.utils

import kpt.core.common.utils.DateFormatPattern

/**
 * Format epoch milliseconds to API-compatible date string.
 *
 * Returns date in "dd MMMM yyyy" format (e.g., "18 February 2026")
 * which matches the standard Fineract API dateFormat parameter.
 *
 * @param millis Epoch milliseconds
 * @return Formatted date string for API usage
 * @see ApiDateFormatter for more formatting options
 */
fun formatDate(millis: Long): String {
    return ApiDateFormatter.formatForApi(millis)
}

/**
 * Format epoch milliseconds to date string using specified pattern.
 *
 * @param millis Epoch milliseconds
 * @param pattern The date format pattern to use
 * @return Formatted date string
 * @see ApiDateFormatter for more formatting options
 */
fun formatDate(millis: Long, pattern: DateFormatPattern): String {
    return ApiDateFormatter.formatForApi(millis, pattern)
}
