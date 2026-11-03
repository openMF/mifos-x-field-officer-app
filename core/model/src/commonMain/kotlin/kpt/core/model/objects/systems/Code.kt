/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.systems

/**
 * Created by ishankhanna on 16/06/14.
 */
class Code {
    /** The server id of this record. */
    var id: Int? = null

    /** The display name. */
    var name: String? = null

    /** True when the value is built into Fineract. */
    var isSystemDefined: Boolean? = null
}
