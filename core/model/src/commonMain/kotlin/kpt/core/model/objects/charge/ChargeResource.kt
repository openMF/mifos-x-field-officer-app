/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.model.objects.charge

/**
 * What a charge is levied ON. [path] is the Fineract path segment of
 * `/{resource}/{resourceId}/charges`.
 *
 * The three resources share the endpoint SHAPE but not its operations — read off the sandbox's
 * OpenAPI spec (`/fineract-provider/fineract.json`, 2026-10-23), which is why the capability lives
 * here rather than being assumed symmetric:
 *
 * | resource | list | template | create | update (PUT) | delete | commands      |
 * |----------|------|----------|--------|--------------|--------|---------------|
 * | clients  | page | yes      | yes    | **no**       | yes    | pay, waive    |
 * | loans    | list | yes      | yes    | yes          | yes    | pay, waive, adjustment |
 * | savings  | list | yes      | yes    | yes          | yes    | paycharge, waive, inactivate |
 *
 * `savings` is `savingsaccounts`, not the `"savings"` of `Constants.ENTITY_TYPE_SAVINGS` — that
 * constant is the DOCUMENTS/notes entity type and would request `/savings/{id}/charges`, which does
 * not exist.
 *
 * @property path the Fineract path segment of the resource
 */
enum class ChargeResource(val path: String) {
    /** A client. */
    CLIENT("clients"),

    /** A loan. */
    LOAN("loans"),

    /** A savings account. */
    SAVINGS("savingsaccounts"),
    ;

    /**
     * Whether an existing charge can be edited. `clients/{clientId}/charges/{chargeId}` has no PUT,
     * so the original answered an edit with "client charge update is not supported" AFTER the
     * officer had filled the form in. The edit is not offered instead.
     */
    val canUpdate: Boolean get() = this != CLIENT

    /** Constants and factories of `ChargeResource`. */
    companion object {
        /** `null` for anything else, so a malformed route cannot request a fourth resource. */
        fun fromPath(path: String?): ChargeResource? = entries.firstOrNull { it.path == path }
    }
}
