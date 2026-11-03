/*
 * Copyright 2024 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.common.utils

/** Wire values and keys shared across features: Fineract entity names, search kinds, date formats, data tables. */
object Constants {

    /** Fineract's entity type for clients, as used in document and identifier paths. */
    const val ENTITY_TYPE_CLIENTS = "clients"

    /** Fineract's entity type for groups. */
    const val ENTITY_TYPE_GROUPS = "groups"

    /** Fineract's entity type for loans. */
    const val ENTITY_TYPE_LOANS = "loans"

    /** Fineract's entity type for savings accounts. */
    const val ENTITY_TYPE_SAVINGS = "savings"

    /** Fineract's entity type for client identifiers (identity documents). */
    const val ENTITY_TYPE_CLIENT_IDENTIFIERS = "client_identifiers"

    /** The search API's entity type for clients. */
    const val SEARCH_ENTITY_CLIENT = "CLIENT"

    /** The search API's entity type for groups. */
    const val SEARCH_ENTITY_GROUP = "GROUP"

    /** The search API's entity type for loans. */
    const val SEARCH_ENTITY_LOAN = "LOAN"

    /** The search API's entity type for savings accounts. */
    const val SEARCH_ENTITY_SAVING = "SAVING"

    /** The search API's entity type for centres. */
    const val SEARCH_ENTITY_CENTER = "CENTER"

    /** The `clientId` key in request bodies and navigation arguments. */
    const val CLIENT_ID = "clientId"

    /** Fineract's `clients` resource name. */
    const val CLIENTS = "clients"

    /** The `savingsAccountType` key in navigation arguments. */
    const val SAVINGS_ACCOUNT_TYPE = "savingsAccountType"

    /** The `dateFormat` key Fineract expects beside every date in a request body. */
    const val DATE_FORMAT = "dateFormat"

    /** The `locale` key Fineract expects beside every date or number in a request body. */
    const val LOCALE = "locale"

    /** The date pattern sent with [DATE_FORMAT]: `dd MMMM yyyy` (e.g. `26 October 2026`). */
    const val DATE_FORMAT_LONG = "dd MMMM yyyy"

    /** The activation screen's entity type for a client. */
    const val ACTIVATE_CLIENT = "activate_client"

    /** The activation screen's entity type for a centre. */
    const val ACTIVATE_CENTER = "activate_center"

    /** The activation screen's entity type for a group. */
    const val ACTIVATE_GROUP = "activate_group"

    /** The application table that extends savings accounts with data tables. */
    const val DATA_TABLE_NAME_SAVINGS = "m_savings_account"

    /** The application table that extends loans with data tables. */
    const val DATA_TABLE_NAME_LOANS = "m_loan"

    /** The application table that extends groups with data tables. */
    const val DATA_TABLE_NAME_GROUP = "m_group"

    /** The signed-in user is working online. */
    const val USER_ONLINE = false

    /** The signed-in user is working offline. */
    const val USER_OFFLINE = true

    /** The first address line field. */
    const val ADDRESS_LINE_1 = "addressLine1"

    /** The second address line field. */
    const val ADDRESS_LINE_2 = "addressLine2"

    /** The third address line field. */
    const val ADDRESS_LINE_3 = "addressLine3"

    /** The address city field. */
    const val CITY = "city"

    /** The address state or province field. */
    const val STATE = "state"

    /** The address country field. */
    const val COUNTRY = "country"

    /** The address postal-code field. */
    const val POSTAL_CODE = "postalCode"

    /** The `status` key in navigation arguments. */
    const val STATUS = "status"

    /** The navigation key a document is passed under. */
    const val DOCUMENT_KEY = "documentKey"
}
