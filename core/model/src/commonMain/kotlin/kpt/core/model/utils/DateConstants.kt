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

/**
 * Standard date format constants for Fineract API.
 *
 * These constants ensure consistency between the date strings sent to the API
 * and the dateFormat parameter that specifies how they should be parsed.
 */
object DateConstants {
    /**
     * Standard date format for Fineract API: "dd MMMM yyyy"
     * Example: "18 February 2026"
     */
    const val DATE_FORMAT = "dd MMMM yyyy"

    /**
     * Standard locale for Fineract API
     */
    const val LOCALE = "en"

    /**
     * Date format with time: "dd MMMM yyyy HH:mm"
     * Example: "18 February 2026 14:30"
     */
    const val DATE_FORMAT_WITH_TIME = "dd MMMM yyyy HH:mm"
}
