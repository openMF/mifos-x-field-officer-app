/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.databaseobjects

/** A meeting date. */
data class MeetingDate(
    /** The year. */
    var year: Int = 0,

    /** The month (1–12). */
    var month: Int = 0,

    /** The day of the month. */
    var day: Int = 0,
)
