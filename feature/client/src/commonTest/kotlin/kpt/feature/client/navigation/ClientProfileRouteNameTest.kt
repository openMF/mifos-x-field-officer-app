/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.navigation

import kotlinx.serialization.serializer
import kpt.feature.groups.navigation.CLIENT_PROFILE_ROUTE_NAME
import kpt.feature.groups.navigation.clientProfileRoute
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * feature/groups opens the client profile by route NAME (it cannot depend on this module). Renaming
 * or moving [ClientProfileRoute] without updating that name would make a member row's tap crash with
 * "Destination … cannot be found" — this fails first.
 */
class ClientProfileRouteNameTest {

    @Test
    fun theGroupsModulesRouteNameIsTheClientProfileRoutesSerialName() {
        assertEquals(serializer<ClientProfileRoute>().descriptor.serialName, CLIENT_PROFILE_ROUTE_NAME)
    }

    @Test
    fun theRouteCarriesTheClientIdAsItsPathArgument() {
        assertEquals("$CLIENT_PROFILE_ROUTE_NAME/42", clientProfileRoute(42))
    }
}
