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

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kpt.core.common.utils.DateFormatPattern

/**
 * Centralized date formatter for Fineract API requests.
 *
 * This object provides standardized date formatting methods to ensure
 * consistency between the date strings sent to the API and the dateFormat
 * parameter that specifies how they should be parsed.
 *
 * Usage:
 * ```kotlin
 * val payload = ClientPayload(
 *     activationDate = ApiDateFormatter.formatForApi(dateMillis),
 *     dateFormat = ApiDateFormatter.DATE_FORMAT,
 *     locale = ApiDateFormatter.LOCALE
 * )
 * ```
 */
object ApiDateFormatter {

    /**
     * Standard date format for Fineract API: "dd MMMM yyyy"
     * Example: "18 February 2026"
     */
    const val DATE_FORMAT = DateConstants.DATE_FORMAT

    /**
     * Standard locale for Fineract API
     */
    const val LOCALE = DateConstants.LOCALE

    /**
     * Format epoch milliseconds to API date string using standard format.
     *
     * @param millis Epoch milliseconds
     * @return Date string in "dd MMMM yyyy" format (e.g., "18 February 2026")
     */
    fun formatForApi(millis: Long): String {
        return formatForApi(millis, DateFormatPattern.FULL_MONTH)
    }

    /**
     * Format epoch milliseconds to date string using specified pattern.
     *
     * @param millis Epoch milliseconds
     * @param pattern The date format pattern to use
     * @return Formatted date string
     */
    fun formatForApi(millis: Long, pattern: DateFormatPattern): String {
        val dateTime = Instant
            .fromEpochMilliseconds(millis)
            .toLocalDateTime(TimeZone.currentSystemDefault())

        return formatDateTime(dateTime, pattern)
    }

    /**
     * Format LocalDate to API date string using standard format.
     *
     * @param date LocalDate to format
     * @return Date string in "dd MMMM yyyy" format (e.g., "18 February 2026")
     */
    fun formatForApi(date: LocalDate): String {
        return formatForApi(date, DateFormatPattern.FULL_MONTH)
    }

    /**
     * Format LocalDate to date string using specified pattern.
     *
     * @param date LocalDate to format
     * @param pattern The date format pattern to use
     * @return Formatted date string
     */
    fun formatForApi(date: LocalDate, pattern: DateFormatPattern): String {
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.month
        val monthNumber = date.monthNumber.toString().padStart(2, '0')
        val year = date.year

        return when (pattern) {
            DateFormatPattern.FULL_MONTH -> "$day ${month.toFullName()} $year"
            DateFormatPattern.SHORT_MONTH -> "$day ${month.toShortName()} $year"
            DateFormatPattern.NUMERIC_DASH -> "$day-$monthNumber-$year"
            DateFormatPattern.NUMERIC_SLASH -> "$day/$monthNumber/$year"
            DateFormatPattern.ISO -> "$year-$monthNumber-$day"
            DateFormatPattern.FULL_MONTH_WITH_TIME -> "$day ${month.toFullName()} $year 00:00"
        }
    }

    /**
     * Format date components (day, month, year) to API date string.
     *
     * @param day Day of month (1-31)
     * @param month Month number (1-12)
     * @param year Year
     * @return Date string in "dd MMMM yyyy" format
     */
    fun formatForApi(day: Int, month: Int, year: Int): String {
        val dayStr = day.toString().padStart(2, '0')
        val monthName = getMonthName(month)
        return "$dayStr $monthName $year"
    }

    /**
     * Format date from list of integers [year, month, day] to API date string.
     *
     * @param dateComponents List containing [year, month, day]
     * @return Date string in "dd MMMM yyyy" format
     */
    fun formatFromList(dateComponents: List<Int>): String {
        require(dateComponents.size >= 3) { "Date components list must have at least 3 elements" }
        val year = dateComponents[0]
        val month = dateComponents[1]
        val day = dateComponents[2]
        return formatForApi(day, month, year)
    }

    /**
     * Get the date format pattern string for a given pattern enum.
     *
     * @param pattern The DateFormatPattern enum value
     * @return The pattern string to send as dateFormat parameter
     */
    fun getDateFormatString(pattern: DateFormatPattern): String {
        return pattern.pattern
    }

    private fun formatDateTime(
        dateTime: kotlinx.datetime.LocalDateTime,
        pattern: DateFormatPattern,
    ): String {
        val day = dateTime.dayOfMonth.toString().padStart(2, '0')
        val month = dateTime.month
        val monthNumber = dateTime.monthNumber.toString().padStart(2, '0')
        val year = dateTime.year
        val hour = dateTime.hour.toString().padStart(2, '0')
        val minute = dateTime.minute.toString().padStart(2, '0')

        return when (pattern) {
            DateFormatPattern.FULL_MONTH -> "$day ${month.toFullName()} $year"
            DateFormatPattern.SHORT_MONTH -> "$day ${month.toShortName()} $year"
            DateFormatPattern.NUMERIC_DASH -> "$day-$monthNumber-$year"
            DateFormatPattern.NUMERIC_SLASH -> "$day/$monthNumber/$year"
            DateFormatPattern.ISO -> "$year-$monthNumber-$day"
            DateFormatPattern.FULL_MONTH_WITH_TIME -> "$day ${month.toFullName()} $year $hour:$minute"
        }
    }

    private fun kotlinx.datetime.Month.toFullName(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun kotlinx.datetime.Month.toShortName(): String {
        return name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    }

    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> throw IllegalArgumentException("Invalid month: $month")
        }
    }
}
