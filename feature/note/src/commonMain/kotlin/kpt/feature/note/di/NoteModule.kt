/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/kmp-project-template/blob/main/LICENSE
 */
package kpt.feature.note.di

import kpt.core.data.note.NOTE_OUTBOX_QUALIFIER
import kpt.feature.note.ui.AddEditNoteViewModel
import kpt.feature.note.ui.NoteListViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Collected automatically. `:cmp-navigation:generateFeatureKoinBindings` scans each feature
 * module's `di` package and emits `GeneratedFeatureKoinBindings`, so there is no aggregate to edit
 * — but the package name is load-bearing: a module outside `di` is silently skipped and the
 * ViewModels simply fail to resolve at runtime, with a clean build.
 */
val NoteModule = module {
    viewModelOf(::NoteListViewModel)

    // Explicit rather than `viewModelOf`: the outbox is QUALIFIER-scoped. Koin indexes
    // `SubmitOutbox<*>` by its raw type, so an unqualified `get()` would resolve whichever outbox
    // was registered last and hand the note form someone else's serializer — a ClassCastException
    // at the first save, nowhere near its cause. See `OutboxQualifiers`.
    viewModel {
        AddEditNoteViewModel(
            repository = get(),
            commands = get(),
            outbox = get(qualifier = named(NOTE_OUTBOX_QUALIFIER)),
            savedStateHandle = get(),
        )
    }
}
