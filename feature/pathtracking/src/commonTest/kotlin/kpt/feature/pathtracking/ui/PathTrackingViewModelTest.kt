/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
@file:OptIn(kpt.core.data.testing.ExperimentalDataTestingApi::class)

package kpt.feature.pathtracking.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kpt.core.base.store.screen.ScreenState
import kpt.core.data.testing.FakePathTrackingRepository
import kpt.core.data.testing.FakeSessionRepository
import kpt.core.model.objects.users.User
import kpt.core.model.objects.users.UserLatLng
import kpt.core.model.objects.users.UserLocation
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PathTrackingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun readsTheSignedInUsersOwnPathsNotTheOffice() = runTest(dispatcher) {
        val repo = FakePathTrackingRepository(
            listOf(
                UserLocation(
                    staffId = 1,
                    latLng = """[{"lat":19.4,"lng":-99.1},{"lat":19.5,"lng":-99.2}]""",
                    date = "05 Mar 2026",
                ),
            ),
        )
        val vm = PathTrackingViewModel(FakeSessionRepository(User(userId = 32, officeId = 1)), repo)
        val paths = (vm.paths.first { it is ScreenState.Content } as ScreenState.Content).data
        // The original passed the OFFICE id (1) to this user-keyed read.
        assertEquals(listOf(32), repo.requested)
        assertEquals(listOf(UserLatLng(19.4, -99.1), UserLatLng(19.5, -99.2)), paths.single().points)
    }

    @Test
    fun aMalformedStoredPathIsAnEmptyRouteNotACrash() {
        assertTrue(UserLocation(latLng = "123,456").toTrackedPath().points.isEmpty())
        assertTrue(UserLocation(latLng = null).toTrackedPath().points.isEmpty())
    }

    @Test
    fun theRouteOpensAsStartToEndDirections() {
        val path = UserLocation(
            latLng = """[{"lat":1.5,"lng":2.5},{"lat":3.0,"lng":4.0},{"lat":5.5,"lng":6.5}]""",
        ).toTrackedPath()
        assertEquals("https://www.google.com/maps/dir/?api=1&origin=1.5,2.5&destination=5.5,6.5", path.mapsUrl())
        assertNull(UserLocation().toTrackedPath().mapsUrl())
    }
}
