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

/**
 * Enum representing standard date format patterns used in Fineract API.
 *
 * The Fineract API accepts dates as strings with a dateFormat parameter
 * that specifies how the date should be parsed. This enum provides
 * standardized patterns to ensure consistency across the application.
 */
enum class DateFormatPattern(val pattern: String) {
    /**
     * Full month name format: "dd MMMM yyyy" (e.g., "18 February 2026")
     * This is the standard format used by Fineract API for most date fields.
     */
    FULL_MONTH("dd MMMM yyyy"),

    /**
     * Abbreviated month name format: "dd MMM yyyy" (e.g., "18 Feb 2026")
     * Used in some specific API endpoints.
     */
    SHORT_MONTH("dd MMM yyyy"),

    /**
     * Numeric format with dashes: "dd-MM-yyyy" (e.g., "18-02-2026")
     * Commonly used for display purposes.
     */
    NUMERIC_DASH("dd-MM-yyyy"),

    /**
     * Numeric format with slashes: "dd/MM/yyyy" (e.g., "18/02/2026")
     * Used for display purposes only, not recommended for API calls.
     */
    NUMERIC_SLASH("dd/MM/yyyy"),

    /**
     * ISO 8601 format: "yyyy-MM-dd" (e.g., "2026-02-18")
     * Standard international date format.
     */
    ISO("yyyy-MM-dd"),

    /**
     * Full month with time: "dd MMMM yyyy HH:mm" (e.g., "18 February 2026 14:30")
     * Used for timestamps with date and time.
     */
    FULL_MONTH_WITH_TIME("dd MMMM yyyy HH:mm"),
}
