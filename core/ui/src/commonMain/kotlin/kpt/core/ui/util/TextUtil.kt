/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/** Text with an optional style and colour. */
data class TextUtil(
    /** The text. */
    val text: String = "",
    /** Its style; the default when null. */
    val style: TextStyle? = null,
    /** Its colour; the default when null. */
    val color: Color? = null,
)
