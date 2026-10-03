/*
 * Copyright 2025 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.core.base.platform.url

/**
 * Opens a URL in whatever the platform considers the right handler.
 *
 * ## Why this is not part of `IntentManager` or `ShareManager`
 * OPENING a URL and SHARING one are different acts with different outcomes: opening hands the user
 * to a browser or a deep-linked app, sharing raises a chooser so they can send it elsewhere. They
 * used to sit on one interface, where `launchUri` was implemented with `Share.url(...)` — so
 * "launch this URI" actually raised a share sheet. Separating the interfaces makes that class of
 * mistake unrepresentable rather than merely fixed once.
 *
 * Every method is synchronous: the platform call returns as soon as the handler is dispatched, and
 * a suspend signature would imply this waits for the user, which it does not.
 */
interface UrlLauncher {

    /** Open [url] with the platform's default handler. Returns false if nothing could handle it. */
    fun open(url: String): Boolean

    /** Open [url] in a browser specifically, bypassing any app that claims the link. */
    fun openInBrowser(url: String): Boolean

    /** Whether [url] has a handler — check before offering the action, not after it fails. */
    fun canOpen(url: String): Boolean
}
