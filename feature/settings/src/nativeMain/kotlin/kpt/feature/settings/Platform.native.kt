/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.settings

/** Reports [Platform.IOS]. */
actual fun getPlatform(): Platform = Platform.IOS

/** False: only Android 12+ derives colours from the wallpaper. */
actual fun supportsDynamicTheming(): Boolean = false
