/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.users

import kotlinx.serialization.Serializable

/**
 * Created by Rajan Maurya on 24/01/17.
 */
@Serializable
data class UserLatLng(
    val lat: Double = 0.0,
    val lng: Double = 0.0,
) {
    override fun toString(): String {
        return "{" +
            "lat=" + lat +
            ", lng=" + lng +
            '}'
    }
}

// @Serializable
// class UserLatLng : kpt.core.model.utils.Parcelable {
//    @SerialName("lat")
//    var lat: Double = 0.0
//
//    @SerialName("lng")
//    var lng: Double = 0.0
//    override fun toString(): String {
//        return "{" +
//            "lat=" + lat +
//            ", lng=" + lng +
//            '}'
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    override fun writeToParcel(dest: kpt.core.model.utils.Parcel, flags: Int) {
//        dest.writeValue(lat)
//        dest.writeValue(lng)
//    }
//
//    constructor(latitude: Double, longitude: Double) {
//        lat = latitude
//        lng = longitude
//    }
//
//    private constructor(parcel: kpt.core.model.utils.Parcel) {
//        lat = parcel.readValue(Double::class.java.classLoader) as Double
//        lng = parcel.readValue(Double::class.java.classLoader) as Double
//    }
//
//    companion object {
//        @JvmField
//        val CREATOR: Creator<core.mifos.core.objects.users.UserLatLng> = object : Creator<core.mifos.core.objects.users.UserLatLng> {
//            override fun createFromParcel(source: kpt.core.model.utils.Parcel): core.mifos.core.objects.users.UserLatLng {
//                return core.mifos.core.objects.users.UserLatLng(source)
//            }
//
//            override fun newArray(size: Int): Array<core.mifos.core.objects.users.UserLatLng> {
//                return arrayOf()
//            }
//        }
//    }
// }
