/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.designsystem.component

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kpt.core.designsystem.utility.PathState

class DrawingState {
    var usedColors: MutableState<Set<Color>> = mutableStateOf(setOf())
        private set
    var paths: SnapshotStateList<PathState> = mutableStateListOf()
        private set
    var currentPath: MutableState<Path> = mutableStateOf(Path())
        private set

    fun addUsedColor(color: Color) {
        usedColors.value = usedColors.value + color
    }

    fun addPath(pathState: PathState) {
        paths.add(pathState)
    }

    fun updateCurrentPath(action: (Path) -> Unit) {
        currentPath.value = currentPath.value.also(action)
    }

    fun resetCurrentPath() {
        currentPath.value = Path()
    }

    fun clear() {
        paths.clear()
        resetCurrentPath()
    }
}
