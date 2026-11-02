/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Icon-tile accents the Stitch home markup uses beyond the MD3 scheme (`mockups/home/stitch/0*`):
 * Tailwind `blue-50/700` and `purple-50/700` — the "Clients pending activation" / "New centre" and
 * "Pending reschedule" / "Run reports" tiles. Light values only; the home dashboard renders light.
 */
object AccentColors {
    val blueContainer = Color(0xFFEFF6FF)
    val blue = Color(0xFF1D4ED8)
    val purpleContainer = Color(0xFFFAF5FF)
    val purple = Color(0xFF7E22CE)

    // 02 "Collected today" hero: from-[#1e3a8a] via-[#2563eb] to-[#3b82f6], its blue-100/200 text,
    // amber-200 "Remains" and the emerald-300 → 400 progress fill.
    val heroStart = Color(0xFF1E3A8A)
    val heroMid = Color(0xFF2563EB)
    val heroEnd = Color(0xFF3B82F6)
    val heroText = Color(0xFFDBEAFE)
    val heroFaint = Color(0xFFBFDBFE)
    val heroRemains = Color(0xFFFDE68A)
    val heroFillStart = Color(0xFF6EE7B7)
    val heroFillEnd = Color(0xFF34D399)
}
