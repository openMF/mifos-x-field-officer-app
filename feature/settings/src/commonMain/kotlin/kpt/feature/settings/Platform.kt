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

/** The platform the app is running on. */
expect fun getPlatform(): Platform

/** The platforms the app ships on. */
enum class Platform {
    /** Android phones and tablets. */
    Android,

    /** The desktop (JVM) app. */
    Desktop,

    /** iOS. */
    IOS,

    /** The Kotlin/JS web app. */
    JS,

    /** The Kotlin/Wasm web app. */
    Wasm,
}

/** True when the platform can derive the colour scheme from the wallpaper (Material You). */
expect fun supportsDynamicTheming(): Boolean
