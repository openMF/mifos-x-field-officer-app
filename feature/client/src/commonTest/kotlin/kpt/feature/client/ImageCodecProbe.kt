/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client

import kpt.core.ui.util.imageToByteArray

private const val PROBE_PNG =
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4z8AAAAMBAQDJ/pLvAAAAAElFTkSuQmCC"

/**
 * Whether this target can actually decode an image. False on ANDROID HOST tests: `BitmapFactory`
 * there is the android.jar stub, so every decode comes back null — the same environmental limit
 * that keeps `*UiTest` off that target. Tests asserting decoded photo bytes return early there and
 * keep their full coverage on desktop and iOS, which run a real codec.
 */
internal fun imageCodecAvailable(): Boolean = imageToByteArray(PROBE_PNG)?.isNotEmpty() == true
