/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.data.testing

/**
 * Opt-in marker for `core/data` test doubles that ship in commonMain so consuming modules can use
 * them. Mirrors `ExperimentalScreenDataStreamTestingApi` in `core-base/store` — the call site has
 * to name it, which is what keeps a fake out of production code by accident.
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This API is for unit tests of core/data consumers only. Do not call from production code.",
)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalDataTestingApi
