/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/** Brand colours outside the Material colour scheme. */
object AppColors {
    /** Outline of address cards. */
    val cardBorders = Color(0xFFD6E4F7)

    /** White, for content on a filled button or search bar. */
    val customWhite = Color(0xFFFFFFFF)

    /** Green of an active or enabled record. */
    val customEnable = Color(0xFF009325)

    /** Yellow of a pending record. */
    val customYellow = Color(0xFFAE9703)

    /** Red of a closed or rejected record. */
    val lightRed = Color(0xFFFF6E6E)

    /** Loan-summary status dot: active. */
    val loanIndicatorActive = Color.Green

    /** Loan-summary status dot: pending approval. */
    val loanIndicatorPending = Color.Yellow

    /** Loan-summary status dot: approved, waiting for disbursal. */
    val loanIndicatorWaitingForDisbursal = Color.Blue

    /** Loan-summary status dot: overpaid or closed. */
    val loanIndicatorOther = Color.Black

    /** Status colour: active. */
    val loanActiveStatus = Color(0xFF5CB85C)

    /** Status colour: pending. */
    val loanPendingStatus = Color(0xFFFFA500)

    /** Status colour: overpaid, or a rejected reschedule. */
    val loanOverpaidStatus = Color(0xFF800080)

    /** Status colour: unknown. */
    val loanUnknownStatus = Color(0xFF9E9E9E)
}
