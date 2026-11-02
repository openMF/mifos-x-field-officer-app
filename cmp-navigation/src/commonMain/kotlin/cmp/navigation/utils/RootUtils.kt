/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package cmp.navigation.utils

import kotlin.reflect.KClass
import kpt.core.ui.navigation.toObjectKClassNavigationRoute as coreToObjectKClassNavigationRoute
import kpt.core.ui.navigation.toObjectNavigationRoute as coreToObjectNavigationRoute

/**
 * Gets the route string for an object.
 *
 * The implementation moved to `kpt.core.ui.navigation` so a FEATURE module can produce the route
 * strings its `@FeatureTab` must supply — `cmp-navigation` depends on the features, so nothing below
 * it could import this. These two keep the original import path working and delegate, rather than
 * carrying a second copy that could drift from the one features compile against.
 */
fun <T : Any> T.toObjectNavigationRoute(): String = coreToObjectNavigationRoute()

/**
 * Gets the route string for a [KClass] of an object.
 */
fun <T : Any> KClass<T>.toObjectKClassNavigationRoute(): String = coreToObjectKClassNavigationRoute()
