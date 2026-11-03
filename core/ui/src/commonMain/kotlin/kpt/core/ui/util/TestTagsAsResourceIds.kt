/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.util

import androidx.compose.ui.Modifier

/**
 * Expose every `Modifier.testTag` below this node as an Android resource id, so device-level tools
 * (Maestro `id:` selectors, UI Automator) can find them. Compose keeps test tags private to its own
 * test APIs by default — without this, a Maestro flow written against ui.yaml's testTags finds nothing.
 * Android-only concept; a no-op on every other target.
 */
expect fun Modifier.exposeTestTagsAsResourceIds(): Modifier
