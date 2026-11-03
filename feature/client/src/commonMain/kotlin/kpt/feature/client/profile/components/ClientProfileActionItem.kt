/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.client.profile.components

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kpt.feature.client.generated.resources.Res
import kpt.feature.client.generated.resources.contact_emergency
import kpt.feature.client.generated.resources.design_services
import kpt.feature.client.generated.resources.directions
import kpt.feature.client.generated.resources.feature_client_profile_address_subtitle
import kpt.feature.client.generated.resources.feature_client_profile_address_title
import kpt.feature.client.generated.resources.feature_client_profile_documents_subtitle
import kpt.feature.client.generated.resources.feature_client_profile_documents_title
import kpt.feature.client.generated.resources.feature_client_profile_family_subtitle
import kpt.feature.client.generated.resources.feature_client_profile_family_title
import kpt.feature.client.generated.resources.feature_client_profile_general_subtitle
import kpt.feature.client.generated.resources.feature_client_profile_general_title
import kpt.feature.client.generated.resources.feature_client_profile_identifiers_subtitle
import kpt.feature.client.generated.resources.feature_client_profile_identifiers_title
import kpt.feature.client.generated.resources.feature_client_profile_notes_subtitle
import kpt.feature.client.generated.resources.feature_client_profile_notes_title
import kpt.feature.client.generated.resources.feature_client_profile_pinpoint_location_subtitle
import kpt.feature.client.generated.resources.feature_client_profile_pinpoint_location_title
import kpt.feature.client.generated.resources.note_add
import kpt.feature.client.generated.resources.note_alt
import kpt.feature.client.generated.resources.people
import kpt.feature.client.generated.resources.sign_language
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

/**
 * Ported verbatim from `6b66e8a43:feature/client/clientProfile/ClientProfileActions.kt` — same
 * seven items, same titles, same subtitles, same drawables (copied across with it).
 */
sealed class ClientProfileActionItem(
    /** The row's title. */
    val title: StringResource,
    /** The line under the title saying what the row opens. */
    val subTitle: StringResource,
    /** The row's leading icon. */
    val icon: DrawableResource,
) {
    /** The client's general view: performance and accounts. */
    data object General : ClientProfileActionItem(
        title = Res.string.feature_client_profile_general_title,
        subTitle = Res.string.feature_client_profile_general_subtitle,
        icon = Res.drawable.design_services,
    )

    /** The client's postal addresses. */
    data object Address : ClientProfileActionItem(
        title = Res.string.feature_client_profile_address_title,
        subTitle = Res.string.feature_client_profile_address_subtitle,
        icon = Res.drawable.contact_emergency,
    )

    /**
     * Declared but NOT in [clientsActionItems] — the original declared it and left it out of the
     * list too, and its event branch is an empty `{}`. Kept exactly as found: the item exists, the
     * row does not render, so there is no dead control on screen.
     */
    data object FamilyMembers : ClientProfileActionItem(
        title = Res.string.feature_client_profile_family_title,
        subTitle = Res.string.feature_client_profile_family_subtitle,
        icon = Res.drawable.people,
    )

    /** The client's identity documents. */
    data object Identifiers : ClientProfileActionItem(
        title = Res.string.feature_client_profile_identifiers_title,
        subTitle = Res.string.feature_client_profile_identifiers_subtitle,
        icon = Res.drawable.sign_language,
    )

    /** The client's attached documents. */
    data object Documents : ClientProfileActionItem(
        title = Res.string.feature_client_profile_documents_title,
        subTitle = Res.string.feature_client_profile_documents_subtitle,
        icon = Res.drawable.note_add,
    )

    /** Notes on the client. */
    data object Notes : ClientProfileActionItem(
        title = Res.string.feature_client_profile_notes_title,
        subTitle = Res.string.feature_client_profile_notes_subtitle,
        icon = Res.drawable.note_alt,
    )

    /** Pin the client's location on a map. */
    data object PinpointLocation : ClientProfileActionItem(
        title = Res.string.feature_client_profile_pinpoint_location_title,
        subTitle = Res.string.feature_client_profile_pinpoint_location_subtitle,
        icon = Res.drawable.directions,
    )
}

/** The original's list and its order: General · Address · Identifiers · Documents · Notes · Pinpoint. */
internal val clientsActionItems: ImmutableList<ClientProfileActionItem> = persistentListOf(
    ClientProfileActionItem.General,
    ClientProfileActionItem.Address,
    ClientProfileActionItem.Identifiers,
    ClientProfileActionItem.Documents,
    ClientProfileActionItem.Notes,
    ClientProfileActionItem.PinpointLocation,
)
