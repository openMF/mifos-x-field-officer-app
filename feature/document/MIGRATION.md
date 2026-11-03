# feature/document — migration ledger

Ported from `6b66e8a43:feature/document/` (8 `.kt`). The GENERIC document list + upload form, for
any Fineract entity type.

## Not a duplicate of `feature/client/documents`

The original ships **both**, with different callers, so both are ported:

| | opens | scope |
|---|---|---|
| `feature/client/documents` (from `clientDocuments`) | client profile's Documents row | clients only, with its own search bar |
| `feature/document` (this) | group detail (`groupNavGraph(loadDocumentList = …)`) | any entity type, via the route |

Loan and savings reach this one when those slices land.

## Registered as a `@FeatureDestination`

Same reasoning as `feature/activate`: the list has no single parent graph to nest under (group
today, loan/savings next), so registering it per-parent would declare `DocumentListRoute` twice in
one NavHost. Annotating registers it once. Registration is the usual two halves — the
`settings.local.gradle.kts` include and the `feature-deps.gradle.kts` build edge.

## Defects fixed

- **`isRefreshing` was never observably true.** The original set it `true`, launched the load, then
  set it `false` on the next line — synchronously, before the load could run. The pull-to-refresh
  spinner could never appear. Driven by the stream's state now.
- **Two latching booleans meant each snackbar fired at most once.** `downloadDocumentState` and
  `removeDocumentState` were set `true` on success and never reset, so a second download in the
  same screen visit showed nothing. One `notice` field that clears after it is shown.
- **A failed download could report success.** The flag was only set on success but the screen read
  it unconditionally once set. The notice now carries the real outcome
  (`Downloaded` / `DownloadFailed`).
- **`document.name.toString()`** printed the literal `"null"` for a nameless document, which
  Fineract permits. `orEmpty()`.
- **Validation lived in the screen.** The upload form's three fields and three error booleans sat
  in `remember`, validated inside the submit lambda — so the form died on rotation and none of it
  was testable. State and `validated()` are in the ViewModel; 8 tests cover the rules.
- **The file picker could not distinguish failure from cancel.** The original caught, printed a
  stack trace and returned `null` for both; a failure is logged, and a cancel no longer clears a
  file already chosen.

## Carried over, NOT fixed: Download saves nothing

`downloadDocument` fetches the attachment's bytes and discards them. No file is written, nothing is
opened, and the officer is told "Download successful". Fixing it means choosing where a file lands
on four platforms — a product decision, not a port — so the affordance is left exactly as the
original draws it (§6a) and tracked as `document-download-saves-nothing`.

The port does narrow the lie slightly: a fetch that FAILS now says so, where the original could
still show the success message.

## Changed from the original

The upload form was a DIALOG over the list; it is a full screen here, because its route is a
navigation destination like every other form in this port. Fields, copy and order are unchanged.

Route arguments lost their defaults (`entityType = ""`, `entityId = -1`), which would have produced
a screen asking Fineract for documents of entity `-1` under an empty type. `documentId` keeps a
`-1` sentinel because a nav argument cannot be absent; the ViewModel narrows it to `null`.

17 tests.
