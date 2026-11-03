# feature/note — migration ledger

**Source:** `6b66e8a43:feature/note` — 8 `.kt`
**Landed:** S3d (shared drill-down), alongside its first consumer `feature/client`
**Status:** complete. All 8 original files accounted for.

---

## 1. Original inventory

| original | migrated to | note |
|---|---|---|
| `notes/NoteScreen.kt` | `ui/NoteListScreen.kt` | renamed for symmetry with `NoteListViewModel` |
| `notes/NoteViewModel.kt` | `ui/NoteListViewModel.kt` | |
| `notes/NoteRoute.kt` | `navigation/NoteNavigation.kt` | route declarations folded into the nav file |
| `addEditNotes/AddEditNoteScreen.kt` | `ui/AddEditNoteScreen.kt` | |
| `addEditNotes/AddEditNoteViewModel.kt` | `ui/AddEditNoteViewModel.kt` | |
| `addEditNotes/AddEditNoteRoute.kt` | `navigation/NoteNavigation.kt` | as above |
| `navigation/NoteNavigation.kt` | `navigation/NoteNavigation.kt` | |
| `di/NoteModule.kt` | `di/NoteModule.kt` | |

Four route files collapse to one because the template declares routes beside the graph that
registers them; keeping a file per route would have produced two one-line files.

## 2. Use-case classification

| use case | kind | substrate |
|---|---|---|
| list a client's notes | read, cache-first | `NoteRepository.notesStream` |
| add a note | write, online-required | `NoteCommandRepository.addNewNote` |
| edit a note | write, online-required | `NoteCommandRepository.updateNote` |
| delete a note | write, online-required | `NoteCommandRepository.deleteNote` |

## 3. Substrate mapping

`DataState` → `ScreenState` via `ScreenDataStream`. The original's single-note read
(`retrieveNote`) is gone: the list is already in the cache, so the edit screen selects from it
rather than issuing a second request for a row it already has.

## 4. Write policy

All three writes are `MutationPolicy.OnlineRequired`. A note is a record of a conversation that
happened at a known time; queuing one offline and replaying it later would attach it to the wrong
moment, and the original did not queue them either.

## 5. New-state decisions

| state | decision |
|---|---|
| `Empty` | "No notes yet" — distinct from a failed read |
| `NoNetwork` | last known notes stay on screen, with the offline signal |
| write failure | rendered ABOVE the list, never replacing it (see §7) |
| `Conflicted` | treated as `Failed` — notes have no merge semantics |

## 6. Nav registration

`noteDestinations(navController)` is registered by `feature/client`'s graph, not annotated
`@FeatureDestination` — notes are a DRILL-DOWN, reachable only from a client, so registering them
as a top-level entry would have added an unreachable root route. `feature/client` therefore
declares a build edge on `projects.feature.note`, exactly as the original did.

## 7. Deviations from the original

| deviation | reason |
|---|---|
| write errors render above the list, not instead of it | a failed delete must not blank notes that are still correct. Folding it into `ScreenState.Error` would replace good content because an unrelated mutation failed. Covered by a preview and a UI test. |
| single-note read (`retrieveNote`) not ported | the list is cached; a second request for a row already in hand is the S5-2 split-read defect in miniature |
| `submitted` latch added to the add/edit screen | found by `aDoubleTapSendsOneWrite`: the nav transition is a few hundred ms and a second tap inside it wrote the note TWICE. The latch is set only on SUCCESS, so a failed write stays retryable. |
| list refresh on write via an explicit nav result | `NoteCommandRepository` is cache-free, so the Store does NOT invalidate on write. An earlier attempt used `LocalLifecycleOwner`, which resolves to the ACTIVITY inside a NavHost — `ON_RESUME` never fired and the list never refreshed. |
| derived `count` lives on the screen, not the state holder | moved when the stream became a `StateFlow` (2026-10-20). Same invariant — one source, so the header cannot disagree with the rows. |

## 8. Replay protection (2026-10-21)

`Idempotency-Key` is sent on create but **apis.mifos.community ignores it** — measured by pinning
the key across two creates; both notes landed. (A first reading said the opposite; it was reading a
stale list, the same missing re-read that made delete look broken.) Replay-after-unseen-success is
therefore prevented client-side: `submitDraft` re-reads the resource's notes before replaying a
CREATE and skips the POST when an identical text is already there. Fails open — an unreadable list
sends the note rather than dropping it. 5 tests in `core/data` `NoteDraftReplayTest`, RED-proved.
Tracked as `idempotency-key-ignored-by-gateway` in `CAPABILITY_GAPS.yaml`.

## Owner-generic notes (2026-10-25, DB v18)

Notes are keyed `(resourceType, resourceId)`, not just a client id: `NoteListRoute` / `AddEditNoteRoute` take the owner type, `NoteEntity.resourceType` defaults to `clients` (migration `fork-note-resource-type`, 17 → 18), and group 7's notes are not client 7's (`aGroupNoteIsRecordedUnderTheGroupNotTheClientWithTheSameId`). Groups and loans now open it; `navigateToClientNotes` keeps the client callers.
