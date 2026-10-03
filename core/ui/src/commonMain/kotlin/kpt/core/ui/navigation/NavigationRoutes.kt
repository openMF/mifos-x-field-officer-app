/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.ui.navigation

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * The navigation route string for a `@Serializable` route object.
 *
 * TEMPLATE GAP this closes: [NavigationItem] and [FeatureTab] live here, in `core/ui`, so a feature
 * module can declare its own bottom-nav tab — but [NavigationItem.graphRoute] and
 * [NavigationItem.startDestinationRoute] are `String`s and the only helper that produced one lived
 * in `cmp-navigation/utils/RootUtils.kt`. `cmp-navigation` depends on the feature modules, so a
 * feature cannot import it, which left `@FeatureTab` declarable only from the one module it was
 * designed to let features avoid editing. Found on mifos-x-field-officer-app 2026-10-01 while giving
 * `feature/client` a tab; `RootUtils` now delegates here so both call sites agree by construction.
 *
 * Flows upstream per RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001.
 */
@OptIn(InternalSerializationApi::class)
fun <T : Any> T.toObjectNavigationRoute(): String = this::class.toObjectKClassNavigationRoute()

/** The route string for a route object's [KClass] — its serial name. */
@OptIn(InternalSerializationApi::class)
fun <T : Any> KClass<T>.toObjectKClassNavigationRoute(): String = this.serializer().descriptor.serialName
