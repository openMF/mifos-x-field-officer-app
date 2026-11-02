# feature/client — migration ledger

> Written at S3 per `FEATURE_LAYER_MIGRATION_SPEC.md` §7. Original reference: `6b66e8a43`.
> Retired `core/domain` reference: `897ffdac1`.

**Scope.** The largest slice: 163 `.kt` in `feature/client` plus the shared drill-downs it is the
first consumer of — `note` (8), `document` (8), `data-table` (20), `search-record` (4), `activate`
(5). **208 files, ~36% of the whole migration.**

**Status (re-measured 2026-10-22 — the line below is the CURRENT count, not an S3 snapshot).**
Port in progress, subdivided `3a` list → `3b` detail+charges → `3c` actions → `3d` shared
drill-downs → `S6-A1` deposit accounts → `S6-B` create/edit.

**30 of the original's 38 screens are covered; the other 8 are CLASSIFIED, not pending.** Two caveats the headline number hides:

- One of the 38 is a DEAD DUPLICATE — the original ships both `clientList/ClientListScreen.kt` and
  `clientsList/ClientListScreen.kt`.
- Three originals (`savingsAccounts`, `fixedDepositAccount`, `recurringDepositAccount`) collapsed
  into ONE parameterised `depositaccounts` destination, so screens-covered and files-written do not
  track each other.

> **This header has gone stale twice.** It read "inventory complete, port in progress" for weeks
> while six slices landed, and `CAPABILITY_GAPS`' own `client-vertical-partially-ported` entry was
> still quoting "18 .kt / 3 screens … remaining ~35" on 2026-10-22. The same drift hit
> `CURRENT_WORK.md` ("feature/** 1 of 20") and `signin/MIGRATION.md` ("port not started", for a
> module that has its brand mark and a 509-line screen). **Re-measure from disk before trusting any
> count in this file** — `git ls-tree 6b66e8a43 feature/client/` for the denominator, `find
> feature/client/src -name "*Screen.kt"` for the numerator.

### The remaining 8, and why each is not simply "to do" (2026-10-22)

Checked individually against the spec's caller rule. Only ONE is a screen this port
has chosen not to write yet:

| screen | verdict |
|---|---|
| `clientPinpoint` | **no caller** in the original; also blocked on a vaulted Maps key |
| `syncClientDialog` | **no caller** in the original |
| `clientSurveyList` / `…Question` / `…Submit` | route REGISTERED but never navigated to — all three unreachable |
| `clientList` | **dead duplicate** of `clientsList`; the graph opens the latter, so "38" overstates by one |
| ~~`documentPreviewScreen`~~ | **LANDED 2026-10-23** — see "Document preview" below |

Porting any of the first six would land a screen nothing opens. See
`client-vertical-remaining-screens-classified`. **With the preview in, every reachable client
screen of the original is ported.**

### Document preview + identifier VIEW_DOCUMENT (2026-10-23)

| what | where | note |
|---|---|---|
| shared viewer | `core/ui/components/AppDocumentViewer.kt` | PDF via `pdf-viewer-kmp` 0.3.0 (PDFium, every target), image via Coil, else "unsupported". Kind by file SIGNATURE first. Replaces the 5 `PdfPreview` actuals + `MifosViewPdf` (deleted `AppViewPdf`, zero callers) |
| preview screen | `documentpreview/` | by id (documents list "View") or by match key (identifiers list "View Document" — the original form's `VIEW_DOCUMENT` phase) |
| §6a deviation | — | the original sent PDFs to an EXTERNAL app; both kinds render in place now (operator decision) |
| not ported | — | the UPLOAD half of `DocumentPreviewScreen` (pick → Submit/Reject): the add-document form owns its file. In-app **Share**: `cmp-share` byte payloads are image-only on iOS/desktop — needs a KmpToolkit bytes payload first |

**Defects found and fixed on the way:**

- **Identifier scans were unfindable.** The add-identifier upload wrote the officer's free-text
  description into the document's `description`, which is the slot the list searches for its key.
  Both sides now use `identifierDocumentKey` (byte-compatible with the original), and the lookup
  also accepts the status-free prefix, so an identifier whose status changed still finds its scan.
- **Documents bypassed the layering (operator rule: network → database → store → data → VM).**
  `downloadDocument` called `DocumentApi` from `core/data` and cached nothing. Now:
  `document_contents` table (schema 14 → 15, AutoMigration) → `documentContent` store →
  repository — a viewed scan reopens offline (device-verified in airplane mode).
- **Writes never reached the store.** `DocumentCommandRepository` ended at the endpoint, so a
  scan uploaded seconds earlier read as missing (device finding). Every write now reconciles
  through the store; the list writer REPLACES rows (deletes used to linger); both document stores
  declare `deleteAll`, without which the logout purge left every row in the database.
- **`migration-ledger.yaml#baseline_units`** had ids fused by spaces, so `syncForkConfig`
  appended four bogus migrations the moment a real row existed. Fixed and re-run idempotently.

Device evidence (OnePlus CPH2423, md5-matched APK, force-stopped): image + 3-page PDF from the
documents list, identifier scan after upload, the same scan offline, 14 → 15 upgrade kept
pre-existing rows.

### Landed in S6-B (2026-10-22)

| screen | package | note |
|---|---|---|
| deposit accounts ×3 | `depositaccounts/` | one destination, keyed by `ServerTypes` |
| create client | `createnew/` | the list FAB's destination; **both** list call sites wired |
| edit details | `editdetails/` | details' `onUpdateDetails` |
| edit photo | `editprofile/` | details' `onUpdatePhoto` |
| default account | `defaultaccount/` | details' `onUpdateDefaultAccount` |

Five null-gated affordances went live. `onCreateStandingInstructions` stays `null` **permanently** —
it has no original at `6b66e8a43` at all, so it is not pending work.

### Landed in cluster C + D (2026-10-22)

| screen | package | note |
|---|---|---|
| add address | `addaddress/` | address list's "+" |
| add document | `adddocument/` | documents list's "+" |
| add identifier | `addidentifier/` | identifiers list's "+"; 2 of the original's 3 phases |
| share accounts | `shareaccounts/` | client-general's last account row |
| share application | `shareapplication/` | chooser's Share row + the share list's "+" |
| fixed deposit application | `fixeddepositapplication/` | chooser's Fixed row + the deposit list's FIXED "+" |

`clientUpcomingCharges` was **not** ported: it reads the same
`{resourceType}/{resourceId}/charges` endpoint as the already-ported charges screen, differing only
in paging, so its row points at that screen instead
(`upcoming-charges-is-the-same-list-as-charges`).

With both wizards in, the apply-new chooser offers **all five** product types — it had been
showing three, then four, purely because the destinations did not exist yet.

---

## 1. Original inventory

40 sub-packages under `6b66e8a43:feature/client/src/commonMain/kotlin/com/mifos/feature/client/`.
By size: `newFixedDepositAccount` 9 · `utils` 7 · `createShareAccount` 7 · `clientProfile` 5 ·
`clientDetailsProfile` 5 · `clientAddress` 5 · `clientGeneral` 4 · `charges` 4 · then 32 packages of
1–3 files each.

### Do NOT port — dead in the original

| package | files | evidence |
|---|---:|---|
| `clientList` (singular) | 3 — `ClientListScreen`, `ClientListUiState`, `ClientListViewModel` (76 lines) | **Zero references** anywhere at `6b66e8a43`. The live list is `clientsList` (plural, 349-line VM), which is what `ClientNavigation`, `ClientModule` and `SyncClientsDialogScreen` import. A near-identical name one character apart. |

Porting it would manufacture the `G-IMPL-DUP` defect by faithfully copying a defect that already
existed. **160 live `.kt`, not 163.**

### Feature-local repository — delete, do not port

`DocumentSelectAndUploadRepository` + `…Impl` live inside `feature/client`. A repository inside a
feature module is the same defect as `feature/settings`'s `SyncSurveysDialogRepository` (S0b closed
spec §11 on exactly this). Document upload belongs in `core/data` alongside
`DocumentCommandRepository`, which already exists.

## 2. Use-case classification

265 `DataState` references and 80 `UseCase` references — the densest in the app. Per spec §2 every
one resolves to a direct `core/data` call; **no use-case is revived.** The 26 `core.domain` imports
are all `DROP-to-repository`: `ActivateClientUseCase`, `CreateClientIdentifierUseCase`,
`DeleteIdentifierUseCase`, `AddClientPinpointLocationUseCase`, `DeleteClientAddressPinpointUseCase`,
`CreateSignatureUseCase`, `DownloadDocumentUseCase` and siblings are each a single repository call.

The one validator consumer — `clientAddress/addAddress/AddAddressScreen` used `ValidationResult` —
now reads `kpt.core.domain.validation` (spec §2).

## 3. Substrate mapping

| concern | original | new |
|---|---|---|
| client list | `ClientListRepository` + manual DB/API branch | `ClientRepository.clientsStream(scope)` |
| client detail | `ClientDetailsRepository` | `ClientRepository` + `ClientLiveReadRepository` **(deleted in S0b)** → the entity-returning `getClient` store |
| writes | 26 use-cases | `ClientCommandRepository` (already wired, `MutationGateway`) |
| charges | `charges/**` | ~~`ClientChargeRepository`~~ → moved to `feature/charge` (`ChargeRepository` + `ChargeCommandRepository`), 2026-10-23 |
| notes · documents · data-tables | feature-local + use-cases | `NoteRepository` · `DocumentRepository`/`DocumentCommandRepository` · `DataTableRepository` |
| identifiers, pinpoint, signature, collateral, survey | use-cases | `ClientCommandRepository`, `SurveyRepository` |

### The read-path defect inside the feature

`ClientListViewModel.loadClients()` reads
`userPreferencesRepository.userInfo.first().userStatus` and branches:

```kotlin
if (userStatus) processClientsFromDb() else processClientsFromApi()
```

Two code paths for one read, chosen by a persisted flag — S5-2 inside the feature rather than in
`core/data`. `clientsStream()` already serves cache-first with a background refresh, so **both
branches collapse into one** and the `userStatus` read disappears from the screen entirely. The
`DataState` → `ScreenState` change is not cosmetic here: it is what removes the branch.

## 4. Write policy

| mutation | policy | why |
|---|---|---|
| create / edit client, address, identifier, collateral | **`Optimistic`** | a field officer registers clients with no signal; queueing is the entire point of the app |
| activate client | **`Optimistic`** | same — activation is a field action |
| upload document / signature / image | **`OnlineRequired`** | binary payloads are not queueable through the outbox as it stands; queueing a multi-MB body with no bounded retry is a different feature. Recorded as a deviation, not an oversight. |
| client transfer, closure | **`OnlineRequired`** | both are irreversible server-side state changes a stale local view must not initiate |
| survey submit | **`Optimistic`** | user-authored content, same as `feature/settings` survey sync |

## 5. New-state decisions

| state | decision |
|---|---|
| `Empty` | "No clients in this office yet" + Create-client CTA. The original rendered an empty list indistinguishable from a failed load. |
| `NoNetwork` | cached clients still render with the stale-data banner; actions requiring connectivity disable rather than fail on tap |
| `Conflicted` | routes to the shell's `ConflictInboxViewModel` — a surface the original had no equivalent for |
| `Failed(rolledBack)` | Snackbar naming what was rolled back; the form keeps its input |
| image load | a client with no photo is `Empty`, not `Error` — the original's `fetchClientImage` treated both as failure |

## 6. Nav registration

`ClientNavigation.kt` registers ~38 destinations through one `clientNavGraph`. Under the template it
becomes `@FeatureDestination` on the graph builder; nested drill-downs stay nested and are NOT
annotated (spec §4 — only top-level entries are).

Client becomes a bottom-nav tab, so `ForkTabRegistry.tabs` gains it **by hand** in slice order
Home · Profile · Client · Centers · Groups — `@FeatureTab` collects alphabetically and tab order is a
layout decision.

Build edge: `settings.local.gradle.kts` include **plus** the root `feature-deps.gradle.kts` entry —
three steps, not two (spec §4).

## 7. Platform capabilities (§3a)

`feature/client` carries 25 of the 40 misplaced platform files — the largest concentration:

| capability | files | disposition |
|---|---:|---|
| `PlatformCameraLauncher` | 5 | → `core/platform`. Note `core/common` already wraps filekit's `openCameraPicker`; check for redundancy before writing a second bridge. |
| `PdfViewer` | 5 | → `core/platform`, unified with `savings`' copy (8 files total, duplicated across two features) |
| `OpenFileInDefaultExternalApp` | 5 | **delete** — `core-base/platform` already ships `IntentManager` + `UrlLauncher` |
| `PhoneNumberUtil` | 5 | → `core/common` if pure, else `core/platform` |
| `PinpointClientScreen.*` | 5 | expect/actual **Composable** → collapse to one commonMain composable over a `core/platform` bridge |

Plus two Android resource files that must travel WITH their capability:

- `androidMain/res/xml/feature_client_file_provider_paths.xml` — FileProvider paths for
  `filekit_cache`/`filekit_files`, manifest-referenced. Separated from `PlatformCameraLauncher` or its
  `<provider>` entry, camera capture fails at runtime while the build stays green.
- `androidMain/res/values/strings.xml` — **Android** resources, not `composeResources`. Must migrate
  to `commonMain/composeResources/values/strings.xml`.

## 8. Deviations

| deviation | reason |
|---|---|
| `clientList` (singular) not ported | dead package, zero references (§1) |
| `DocumentSelectAndUploadRepository` deleted | feature-local repository; `core/data` owns document writes |
| manual DB/API branch removed | the store serves both; keeping it would re-create S5-2 (§3) |
| binary uploads `OnlineRequired` | queueing multi-MB bodies needs bounded retry the outbox does not yet have (§4) |
| result rows in Home search gain their tap here | S2 deferred it so as not to ship a dead clickable; client detail is the first destination that can receive one |

---

## Audit verdict

**Not started; inventory complete and two defects caught before any code was written** — a dead
duplicate package that would have become `G-IMPL-DUP`, and a feature-local repository that would have
re-created the read-path split S0b just removed. The `userStatus` branch is the clearest evidence that
`DataState` → `ScreenState` is a behaviour change, not a rename.

---

## S3b — detail, the tab, and the end-to-end defects that surfaced behind them

S3b set out to add client detail. Wiring the feature so a user could actually REACH it exposed a
chain of defects, each of which hid the next. None were introduced by this slice; every one was
found by driving the real app against the live Fineract demo (`apis.mifos.community`,
`fieldofficer`), and none was visible to `assembleProdDebug`.

### Delivered

| Artifact | Note |
|---|---|
| `detail/ClientDetailViewModel.kt` | one state, two independently-loading `ScreenState`s (identity · accounts); streams HELD so per-section Retry reaches the one that failed |
| `detail/ClientDetailScreen.kt` | stateless `ClientDetailContent` half; account rows clickable only when an id exists |
| `navigation/ClientDetailRoute` | type-safe `clientId` argument, nested in the feature graph and deliberately NOT `@FeatureDestination` |
| `navigation/ClientTab.kt` | `@FeatureTab` — what finally makes the module reachable |
| `navigation/clientTabGraph` | the tab's inner-NavHost graph; detail pushes on the OUTER controller so it covers the bottom bar |
| `detail/ClientDetailViewModelTest.kt` | 6 tests, incl. the route-id read and the independent-state invariant |

`ClientListScreen`'s `onClientClick` is no longer `null` — the S2/S3a deferral is closed.

### Defects found and fixed (all pre-existing)

| # | Symptom on device | Root cause | Fix (and where) |
|---|---|---|---|
| 1 | "Login failed. Check your credentials" for a valid credential | `base_path: fineract-provider/api/v1/` — the plain-Fineract path, not the field-officer gateway's | `app-profile/app.yaml` → `1.0/field/v1/`. Probed both: the first 404s, the second 401s |
| 2 | same message, in <1s, with NO HTTP log at all | no Content-Type, so `ContentNegotiation` refused the `@Body` and Ktor threw before opening a socket | declared `Content-Type`/`Accept: application/json` on the access point in app-profile |
| 3 | every authenticated call 401, while sign-in itself worked | `updateFineractUser` stored `"Basic $key"`, and `AuthScheme.BASIC.format` prefixes again → `Basic Basic …` | store the RAW credential (`ForkPreferencesRepositoryImpl`) |
| 4 | first sign-in after a sign-out bounced back to the login screen 52 ms later | sign-out sets `isUnlocked = false`; sign-in set only `isAuthenticated`, and this fork has no unlock screen | `updateFineractUser` also unlocks (provisional until the passcode flow lands in S7) |
| 5 | Clients tab on Loading forever, zero network requests | `CACHE_FIRST_SWR` reads `cached(refresh = false)` and leaves fetching to a band gate that cannot fire on a cold cache | the three client reads declare `NETWORK_WITH_CACHE` (`ClientRepositoryImpl`) |
| 6 | 200 from the server, then an empty list | two `Page<T>` classes; the APIs imported the one that is NOT `@Serializable` | deleted `core/common/utils/Page.kt`, repointed all six call sites at `core/model`'s |
| 7 | feature built, registered, and unreachable | nothing in the UI navigated to it | `@FeatureTab` + the inline tab graph |

Four test files encoded defects 3 and 6 rather than catching them, and four more
(`RuntimeHeaderTest`, `AuthSchemeTest`) had been asserting against an access-point id (`fineract`)
that no longer exists — they threw `NoSuchElementException` and nobody had run them. All corrected.

### Template gap, NOT fixed here

`@FeatureTab` and `NavigationItem` live in `core/ui` so a feature can declare its own tab, but the
only helper producing the route STRINGS they require (`toObjectNavigationRoute`) lived in
`cmp-navigation`, which depends on the feature modules — so no feature could use the seam it was
given. The helper now lives in `core/ui` with `cmp-navigation` delegating to it. Flows upstream per
RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001.

### Three more, found after the first seven were cleared

| # | Symptom | Root cause | Fix |
|---|---|---|---|
| 8 | 200 from the server, `fetched 100`, list still empty | six entities declared their own embedded detail table as the FOREIGN KEY **parent** of their id, so every insert failed `787 FOREIGN KEY constraint failed` — Client, Center, Group, SavingsAccountTransaction, LoanTimeline, LoanWithAssociations | constraints dropped; schema 13 → 14 in `migration-ledger.yaml` |
| 9 | ledger said `version: 1`, generated `ForkDatabaseConfig.VERSION` said 13 | `syncForkConfig` only rewrites `version:` when it APPENDS a template unit, so with none pending the two never reconciled | both set to 14 |
| 10 | sign-in OK, every authenticated call 401 | `AuthTokenSource` handed the bridge the repository's `StateFlow`; re-emitting the same values as a cold flow fixes it, reproducibly, same binary otherwise | `flow { emitAll(...) }` in `provideAuthTokenSource`, with the measurement recorded there |

Defect 8 is the keystone: with it, the offline cache of this app could never be written **at all** —
clients, centers, groups, savings transactions and loans alike. Store5 swallowed the SQLite failure,
so every one of those screens would have rendered "Nothing here yet" forever.

### Verified on device (2026-10-02, clean install, live `apis.mifos.community`)

sign-in → authenticated shell → Clients tab → **100 real clients** (names, account numbers, Active
status) → tap a row → **client detail** with identity (CENTRAL · Active) and the Loan accounts
section listing the client's real loan products. Zero crashes. 450 tests green.

### Still open

- **A server error renders as "Nothing here yet".** A 502 or a 401 reaches the screen as an empty
  list, because the HTTP client does not set `expectSuccess`, so a non-2xx body is deserialized onto
  a DTO's defaults and looks like a successful empty page. This is how defects 1, 3 and 8 all hid.
  It needs `expectSuccess` on the shared client (`core-base/network`) — raised for upstream.
- **Destructive migration did not fire** on the 13 → 14 bump even though
  `fallbackToDestructiveMigration(dropAllTables = true)` is configured; the app crashed until the
  package was uninstalled. No installed base here, but it would strand real users.
- The root cause behind defect 10 sits inside `AuthHeaderBridge`; the fork works around it at its own
  seam rather than editing `core-base/`.

---

# S3c — Stage A extraction (idea-first pipeline, spec §7.0)

Input to Stage B. Every row cites its original path at `6b66e8a43` (AC11). 16 packages, 46 `.kt`.

## A.1 Routes and arguments — as the original declares them

| Package | Route | Args | Registered by |
|---|---|---|---|
| clientIdentifiersList | `ClientIdentifiersListRoute` | `clientId: Int = -1` | `clientIdentifiersListDestination` |
| clientIdentifiersAddUpdate | `ClientIdentitiesAddUpdateRoute` | `clientId`, `feature: String`, `uniqueKeyForHandleDocument: String?` | `clientIdentifiersAddUpdateDestination` |
| clientDocuments | `ClientDocumentsRoute` | `clientId: Int = -1` | `clientDocumentsDestination` |
| clientAddDocuments | `AddDocumentRoute` | *(object — no args)* | `clientAddDocumentGraphRoute` |
| documentPreviewScreen | `DocumentPreviewScreenRoute` | *(object — no args)* | `createDocumentPreviewRoute` |
| charges | `ChargesRoute` | `resourceId: Int`, `resourceType: String` | `chargesDestination` |
| clientUpcomingCharges | `ClientUpcomingChargesRoute` | `resourceId`, `resourceType` | `clientUpcomingChargesDestination` |
| clientClosure | `ClientClosureRoute` | `id: Int = -1` | `clientClosureDestination` |
| clientTransfer | `ClientTransferRoute` | `id: Int = -1` | `clientTransferDestination` |
| clientSignature | `ClientSignatureRoute` | `clientId`, `name: String`, `accountNo: String` | `clientSignatureDestination` |
| clientCollateral | `ClientCollateralRoute` | `clientId: Int = -1` | `clientCollateralDestination` |
| clientCollateralDetails | `ClientCollateralDetailRoute` | `clientId: Int = -1` | `clientCollateralDetailDestination` |
| clientSurveyList / Question / Submit | **string routes**, not `@Serializable` | `ClientScreens.ClientSurveyListScreen` etc. | `clientSurveyListRoute`, `clientSurveyQuestionRoute` |
| clientPinpoint | **string route** `ClientScreens.ClientPinPointScreen` | `clientId` | `clientPinPointRoute` |

The survey and pinpoint screens predate the type-safe-route migration and still use
`ClientScreens(val route: String)` with `{clientId}` placeholders. They port to `@Serializable`
routes — a deviation recorded in §deviations, not a silent rewrite.

## A.2 Substrate mapping, measured from each ViewModel's constructor

| Screen | Depends on |
|---|---|
| clientIdentifiersList | `deleteClientIdentifierUseCase`, `getDocumentListUseCase`, `removeDocumentUseCase` |
| clientIdentifiersAddUpdate | `clientIdentifiersRepository`, `createClientIdentifierUseCase`, `downloadDocumentUseCase`, `getDocumentListUseCase` |
| clientDocuments | `documentSelectAndUploadRepository`, `documentsRepository` |
| clientAddDocuments · documentPreview | `documentSelectAndUploadRepository` |
| charges | `createChargesUseCase`, `getChargeTemplateUseCase` |
| clientPinpoint | `add/update/delete/getClientPinpointLocationsUseCase` (4) |
| clientSignature | `create/updateSignatureUseCase`, `downloadDocumentUseCase`, `getDocumentListUseCase`, `removeDocumentUseCase` |
| clientUpcomingCharges · clientClosure · clientTransfer · clientCollateral · clientCollateralDetails · survey (3) | **no repository or use-case injected** |

Six screens injecting nothing is a finding, not an omission in this table: they are either
pure-UI, or read through a parent, or unfinished in the original. Each is classified per §2 in
Stage A.3 before any of them is materialized — a screen that turns out to be a shell must be
recorded as such rather than regenerated as a working one.

`DocumentSelectAndUploadRepository` lives INSIDE the original feature module
(`feature/client/.../DocumentSelectAndUploadRepository.kt` + `Impl`). That is the S5-2
feature-local-repository defect the S0b fold removed everywhere else; it hoists to `core/data`,
matching what was done for `SyncSurveysDialogRepository` (see `feature/settings/MIGRATION.md` §1).

## A.3 Two findings that change delivered work

### F1 — the list→detail edge does not match the original

The original has **two distinct client screens reached by two different entry points**:

```
Clients list tab  --onClientSelect-->  ClientProfileScreen     (clientProfile/)
search / nav shell --------------->    ClientDetailsScreen     (clientDetails/)
```

`ClientNavigation.kt:133` wires `clientListScreenRoute(onClientSelect =
navController::navigateToClientProfileRoute)`, while `ClientDetailsScreen` is reached from
`cmp-navigation/.../AuthenticatedNavigation.kt:72` (`client.id?.let {
navController.navigateClientDetailsScreen(it) }`) and from the navbar's `onSavings`.

**S3b wired the Clients list row to the ported `ClientDetailsScreen`.** That edge is the
original's search edge, not its list edge. Both screens are real and both are needed; what is
wrong is which one the list opens — and `clientProfile/` (5 files, incl. `ProfileCard.kt` and
`ClientProfileActions.kt`) is not ported at all.

This was invisible to the S3b device walkthrough because *a* plausible detail screen appeared with
correct live data. Only reading the original's graph surfaced it. It is the first concrete
argument for §7.0: the port was faithful to a file and unfaithful to the app.

### F2 — the ported detail screen carries 4 of 13 actions

> **Superseded 2026-10-22.** S6-B wired update-details, update-photo and update-default-account,
> and established that `onCreateStandingInstructions` has no original to port. Re-read the
> destination block in `ClientNavigation.kt` for the live count rather than trusting this heading.

`clientDetailRoute` declares thirteen: `addLoanAccount`, `addSavingsAccount`, `charges`,
`documents`, `identifiers`, `moreClientInfo`, `notes`, `pinpointLocation`, `survey`,
`uploadSignature`, `loanAccountSelected`, `savingsAccountSelected`, `activateClient`.

The S3b port exposes four (`onLoanClick`, `onSavingsClick`, `onNotesClick`, `onDocumentsClick`),
all passed `null` pending later slices. Nine are absent from the signature entirely, so nothing
would ever have reported them missing. S3c supplies `charges`, `identifiers`, `pinpointLocation`,
`survey` and `uploadSignature`; `activateClient` and `moreClientInfo` belong to S3d; the loan and
savings edges to S4/S5.

## A.4 Platform capabilities (§3a)

`clientPinpoint/PinpointClientScreen.android.kt` is an `androidMain` file — a map/location surface.
Per §3a it is classified before materialization, not carried into the port as a platform source
set. `clientSignature` draws to a canvas and writes a file; its capability classification is
decided with it.

## A.5 Still open in Stage A

Per-screen STATES and the `action_contract` for each `on_click` come from reading the 16 Screen
composables; routes, args, substrate deps and the nav edges above are complete. Stage B does not
start until A.5 closes — materializing from a partial inventory is how a screen acquires invented
behaviour.

---

## S3c — client action screens (2026-10-21)

Four of the ten action packages ported end to end: **staff · closure · transfer · collateral**.
Each is ViewModel + carried-over Compose screen (§6a) + TestTags + nav + Koin + tests.

### Substrate work this slice needed first

| | |
|---|---|
| `ClientRepository` | +5 streams — `pinpointLocationsStream`, `clientCollateralStream`, `collateralItemsStream`, `clientCloseTemplateStream`, `clientTemplateStream` |
| `core/model/client/` | `StaffOption` + `ClientCloseTemplateResponse`/`Narration` MOVED out of `core/network`; new `ClientTemplateOptions` + `SavingAccountOption` |
| `ClientCommandRepository` | 7 commands changed from `MutationResult<HttpResponse>` to `MutationResult<Unit>` |
| `core/data/testing/` | new `FakeClientRepository`; `FakeClientCommandRepository` gained real impls for those 7 |

**Why the DTOs moved.** `ScreenDataStream` has an `internal` constructor, so a feature cannot
narrow a stream's type itself — the projection must happen at or below the store. A repository
returning `GetClientsPageItemsResponse` would therefore have forced `core:network` onto every
feature rendering a staff dropdown, the layering this port already rejected once. `StaffOption` and
`ClientCloseTemplateResponse` are plain serializable values a SCREEN consumes, so they belong on the
domain side; the client-template store now projects the envelope to `ClientTemplateOptions` in its
own `fetch`.

**Why the commands return `Unit`.** Their Fineract endpoints are declared `: HttpResponse` because
the bodies differ per endpoint and nothing reads them, so the raw Ktor response was passed straight
through `core/data`'s contract. That made all seven unfakeable — a test cannot build an
`HttpResponse` without a MockEngine — so no S3c write path could be tested at all. The status check
they used to need is already done by `expectSuccess`, so `Applied` already means "accepted".

### One store, three screens

`getClientStaffOptions` / `getSavingsAccounts` / `getShareAccounts` were three repository methods
that each called `getClientTemplate(clientId)` and kept one field — three identical uncached
requests. They share one store now; each screen projects its field.

### Defects FIXED rather than carried

§6a keeps the UI verbatim "including anything believed wrong". These are not UI choices — two are
crashes and two are writes the server rejects, so fidelity to them is not fidelity.

1. **Date contradicted its declared format** (closure + transfer). `closureDate`/`transferDate`
   were `DateHelper.getDateAsStringFromLong` (`dd-MM-yyyy`) while the request declared
   `ApiDateFormatter.DATE_FORMAT` (`dd MMMM yyyy`) — Fineract was told to parse a string it could
   not. Both now use `ApiDateFormatter.formatForApi`, which exists for exactly this pairing. The
   DISPLAYED date keeps the original's format.
2. **`quantity.toInt()` unguarded** (collateral). Threw on every keystroke after clearing the field.
   The `isEmpty()` checks on the next lines look like they cover it; `toInt()` has already thrown.
3. **Totals were derived state stored as fields** (collateral). Recomputed only on a quantity
   keystroke, so switching collateral TYPE left the previous type's figures on screen — which the
   screen papered over by re-firing `OnQuantityChange` from the dropdown. Derived now.
4. **Index held against a list the ViewModel no longer owns.** Every screen read
   `state.options[state.currentSelectedIndex]` at submit. The rows belong to the stream now, so the
   action carries the chosen id, and the screen clamps the index for display.

Also per-status dialog buttons: the original gave the SUCCESS and FAILURE dialogs the same single
button wired to "navigate forward", so acknowledging a failure also navigated.

### Deliberately NOT ported

**`clientPinpoint`** — blocked on Google Maps. Its UI is an `expect`/`actual` pair whose only real
implementation is a 411-line Android actual on `maps-compose`; the fork has none of the three
required deps and no Maps API key (which must come through the vault, per
RULE-SECRETS-VAULT-001). The route is ABSENT rather than stubbed. Layers below it — the stream and
the three write commands — are done and unblocked. Tracked as
`client-pinpoint-blocked-on-maps-key`.

### Remaining in S3c

`clientSignature` (446-line VM, 299-line screen) · `clientSurveyList` + `clientSurveySubmit` +
`clientSurveyQuestion` · `clientCollateralDetails`.

---

## Client profile UI — the rebuild REVERTED (2026-10-21)

**Operator-flagged:** the client detail UI had been changed; it must be exactly the original's.

It had. The screen the client list opens was a **rebuild, not a port** — a `Scaffold` with a
`TopAppBar`, raw `Text` identity fields, hand-rolled "Charges"/"Notes" rows, and a loan/savings
accounts section. None of that is in the original.

### What the original actually is

The list navigates to **`clientProfile/ClientProfileScreen.kt`** (222 lines), *not* to
`clientDetails/ClientDetailsScreen.kt` (884 lines) — that one is a separate legacy destination the
list never opened, which is how the earlier pass went wrong about what "client detail" means.

```
"Profile"  label
ProfileCard          ← primary-coloured, avatar + name + "Acc. No. X" + office + group chip + chevron
"Actions"  label
MifosRowCard × 6     ← General · Address · Identifiers · Documents · Notes · Pinpoint
```

**Accounts are not on this screen at all.** They are behind the General row, in `clientGeneral` —
which is why the accounts section was not just restyled but moved.

### Ported now

| | |
|---|---|
| `profile/` | `ClientProfileScreen` + `ClientProfileViewModel` + `components/ProfileCard` + `components/ClientProfileActions` |
| `general/` | `ClientGeneralScreen` + `ClientGeneralViewModel` + `components/ClientGeneralActions` |
| drawables | 14 copied from the original (7 profile action icons + 7 general action icons) |
| deleted | the whole `detail/` package and its test |
| route | `ClientDetailRoute` → **`ClientProfileRoute`**, matching the original's `ClientProfileRoute` |
| `ClientRepository` | `+ clientImage(clientId): String?` — the avatar, a one-shot blob |

### Kept verbatim, including what looks wrong

- `FamilyMembers` is declared by the sealed class and deliberately absent from the rendered list —
  the original left it out too, and its event branch is an empty `{}`.
- `clientGeneral`'s subtitles are wrong in the original ("Loan Accounts" → *"View or edit the
  linked address"*; Shares and Collateral share *"Check or update the account notes"*). Copy is UI;
  §6a keeps it.
- The empty `rightValues` slot on every general row, reserved for counts no endpoint supplies.
- `lastLoanAmount` is always `null` — the original's own comment says nothing computes it, so the
  row reads "Not Available" rather than showing an invented figure.
- "No. Of Loan Cycles" SUMS `loanCycle` across active loans (it is not a count), and "Total
  Savings" excludes recurring deposits and closed accounts (so it does not equal the accounts
  list's sum). Both pinned by tests so neither changes silently.

### Fixed, because they are not UI choices

- Every navigation callback read `state.client?.id ?: -1` **at the call site** — 7 sites on the
  profile screen and 7 more on general — so any tap while the load was in flight navigated to
  client `-1`. The events carry the id now.
- The avatar `ByteArray` sat in a `data class`, which compares by identity: every decode of the
  same photo read as new state. `equals`/`hashCode` are by content.
- Performance history was stored beside the accounts and computed once; it is derived now, so it
  cannot disagree with them.

106 tests green across `feature/client` + `core/data`.

### Still to port from this cluster

`clientDetailsProfile` (the ProfileCard's tap target — 388-line screen, 347-line VM) ·
`clientAddress` · `clientDocuments` · the `identifiers` SCREEN (its ViewModel and tests landed in
S3b but no screen was ever written, so the row is `null` and absent).

---

## The profile cluster completed (2026-10-21)

Three more screens, closing the loop from the client list down to the action forms.

### `identifiers/ClientIdentifiersScreen.kt`

The missing half of S3b. The ViewModel and its 6 tests had shipped with **no screen** — so it had
no consumer and was not even in the Koin module. Ported from `clientIdentifiersList`: header with
count, `MifosEmptyCard`, one `MifosActionsIdentifierListingComponent` per identifier with its
expanding menu, the composite `uniqueKeyForHandleDocument`, the `reversed()` ordering.

The menu lists **Delete only**. The original listed ViewDocument and UploadAgain too and swallowed
them in an `else -> {}` — a menu entry that does nothing is exactly the dead control
RULE-IMPL-DEAD-CLICKABLE-001 forbids. They return with `feature/document`.

### `details/` — the ProfileCard's tap target

`clientDetailsProfile`: avatar header, the Update Photo / Update Signature pair, three derived
detail cards, the "Scroll for More Options" hint, and eight action rows. **This is the screen that
makes the four S3c action forms reachable** — Closure, Transfer, Assign Staff and Create Collateral
all route from here, plus Add Charge.

Its one piece of real logic is carried over exactly: **the AssignStaff row does two different
things.** On an unstaffed client it opens the assign form; on an already-staffed one it raises the
unassign confirmation instead, and confirming sends `unassignStaff`. The dialog's copy explains why
(Fineract needs a few minutes before a re-assign takes).

Eleven action items are declared, **eight render** — the original commented three out of its list
and left their event branches empty, so they stay declared-but-unlisted rather than drawn dead.

### `general/` — where the accounts live

Already covered above; the profile's General row now routes to it.

### Nav graph, end to end

```
Clients tab → list → ClientProfileRoute ─┬→ General      → ClientGeneralRoute → Collateral form
                                         ├→ Identifiers  → ClientIdentifiersRoute
                                         ├→ Notes        → feature/note
                                         └→ ProfileCard  → ClientDetailsRoute ─┬→ Closure
                                                                               ├→ Transfer
                                                                               ├→ Assign Staff
                                                                               ├→ Create Collateral
                                                                               └→ Add Charge
```

Every other row and button is `null`, so it is absent rather than inert.

### Fixed, same class as before

Both screens' navigation callbacks read `state.client?.id ?: -1` at the call site — and
`clientDetailsProfile` also did `state.client!!.staffId` at confirm time, a `!!` on a nullable field
of a record the ViewModel does not own. The events carry what the screen already holds.

117 tests green across `feature/client` + `core/data`.

---

## Address + Documents — the profile's last two rows (2026-10-21)

Every row on the client profile now routes somewhere real.

### `address/`

The LIST half of the original's `clientAddress`. Its form half (`addAddress`, 610 lines) is S3d, so
the template read and `createClientAddress` are not here — the "+" is absent.

**The connectivity observer it deletes was leaking.** `observeNetwork()` collected
`networkMonitor.isOnline` and ran both loads on every `true` — and it was called from `init` **and
again from `handleRetry()`, with no cancellation**. Each retry started another collector on the same
flow, so after three retries one reconnect fired four parallel address loads. `ScreenState.NoNetwork`
removes the observer, the `networkConnection` field, the `NetworkError` screen-state and the leak
together. Pinned by `retryDoesNotStackCollectorsTheWayTheOriginalDid`.

Also deleted: the screen's `LaunchedEffect(Unit) { viewModel.loadClientAddress() }`, which ran on
top of the `init` load — opening the screen fetched the list twice.

`items(key = ...)` changed from `address.indexOf(it)` (an O(n) scan per item that returns the FIRST
match, so two identical addresses collide and LazyColumn mis-recycles) to `addressId`.

### `documents/`

**The search filter destroyed the list it filtered.** The original overwrote `clientDocuments` in
the state with the filtered result, so the unfiltered set was gone — clearing the query could not
restore it and `Refresh` was the only way back. The rows stay the stream's here and the committed
query combines over them (`applyQuery`, built on core-base `combineContent` as is
`ClientListViewModel.applyFilters`), so closing the search bar restores everything. Pinned by
`closingTheSearchBarRestoresTheWholeList`.

Search also widened from `name` alone to name + fileName + description: a document the officer
remembers by its FILE name was unfindable while visible on screen.

`pullDownRefresh` was a `Boolean` in the state that nothing reset on the error path — the
`PullToRefreshBox` reads `ScreenState` now.

Delete refreshes `forceFresh` for the same reason the note list had to: the command repository is
cache-free, so the deleted row would otherwise stay on screen.

### Not ported, absent rather than inert

`ViewDocument` / `OpenExternalPdfViewer` download the file into a platform PDF viewer (five
`expect`/`actual` pairs under `feature/client/utils`) — that is the document-preview feature, S3d.
The View entry is omitted from the row menu; the original listed it and ended its handler in
`else -> null`. The add-document form is S3d too.

132 tests green across `feature/client` (13 suites) + `core/data`.

---

## De-branding: `Mifos*` → `App*` (2026-10-21)

**Operator instruction:** remove the `Mifos*` prefix, make the symbols generic, and place each one
in the module whose context it belongs to.

### The naming rule, and why not `Kpt*`

**1,398 occurrences / 171 symbols / 121 files.** Two generic prefixes already exist here and they
mean different things:

| prefix | owner | where |
|---|---|---|
| `Kpt*` | the TEMPLATE | `core-base/**` + template-owned `core/**` |
| `App*` | the FORK / consumer | `core/designsystem` (`AppColors`, `AppSpacing`, `AppShapes`, …) |

The `Mifos*` components are fork-level wrappers ABOVE the template primitives — `AppOutlinedButton`
takes `text:`/`leadingIcon:` slots that `KptOutlinedButton` does not. Renaming them `Kpt*` would
squat the template's namespace and collide on the next `/kmp-project-template-sync`. So the rule is
**`Mifos<X>` → `App<X>`**.

Nine symbols would have collided with existing `Kpt*` names (`MifosButton`, `MifosScaffold`,
`MifosTopAppBar`, `MifosTypography`, …) — which is the evidence for the rule, not an obstacle to it.

### Two genuine collisions, resolved by ownership

`MifosIcons` and `MifosCard` collided with existing `App*` symbols, so each needed a decision rather
than a prefix swap:

- **`MifosIcons` (117 icons) vs `AppIcons` (59).** Both live, both used. Merging was rejected: they
  overlap on 31 names and **13 of those have different values** — `ArrowBack` is a real back-arrow
  in one and `ChevronLeft` in the other, `Language` is `ArrowOutward` vs `Translate`. Merging would
  silently restyle 13 icons app-wide. Resolved by ownership instead: the template's catalog became
  **`KptIcons`** (template namespace, 6 consumers updated) and the fork's became **`AppIcons`**.
- **`MifosCard` vs `AppCard`.** Different modules (`core/designsystem` vs `core-base/designsystem`),
  different base component (`Card` vs `ElevatedCard`) and different defaults. Not interchangeable,
  so the fork's became `AppCard` in its own package. No file imports both, so no aliasing.

### Placement: nothing needed moving

The instruction's second half was to re-place components by context. Audited against the ORIGINAL's
usage, which is the only evidence that generalises past today's half-ported state:

| component | features using it in `6b66e8a43` |
|---|---|
| `AppEmptyCard` · `AppBreadcrumbNavBar` | client · loan · note · recurringDeposit · savings |
| `AppErrorComponent` | client · loan · note · recurringDeposit |
| `AppRowCard` | client · loan |
| `AppActionsIdentifierListingComponent` · `AppAddressCard` | client · search-record |

All already in `core/ui`, all genuinely cross-feature — **correctly placed.** They look
client-only today only because `feature/client` is the sole deeply-ported feature; moving them into
it would have to be undone at S4/S5. The listing components also share one file with
`AppActionsLoanListingComponent` / `…SavingsListingComponent`, which belong to features not yet
ported.

### No upstream PR

RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001 does not bind: the `Mifos*` components were **fork-added**
into template-derived directories, not template code that was fixed. Verified against
`kmp-project-template` — its `core/**` and `core-base/**` carry no `Mifos*` symbols at all. (Its
only two mentions are KDoc *examples* of a brand identifier, `e.g. "MifosDatabase"`, in
`core-base/database` — a template-level nit, unrelated to this change, and our fork's copies of
those files are already clean.)

### Brand text, not just symbols

- `core_designsystem_app_title` = `"Mifos"` — **deleted**: zero consumers, and the live app title
  already comes from `AppInfo.appDisplayName` (app-profile → BuildKonfig).
- `drawer_profile_name` = `"Mifos"` — the drawer header's profile name was a hardcoded brand
  string. Now reads `AppInfo.appDisplayName`, the same source `DashboardTopBar` uses.
- `feature_signin_cd_logo` = `"Mifos logo"` → `"App logo"` (a content description, so user-facing).
- `core-base/ui`'s `NavTransitions` KDoc said "pre-wires Mifos's shared-axis" — a brand reference in
  the FRAMEWORK-SHARED layer, which is a leak rather than a fact about this fork. Genericised.
- 10 `<!-- Mifos … -->` section comments in `core/ui` strings.

**Preserved deliberately:** MPL licence headers ("Copyright 2026 Mifos Initiative"), the
`openMF` upstream URLs, every `6b66e8a43:` provenance line, and every `com.mifos.*` path — those
name the ORIGINAL codebase, and renaming them would misreport what was ported. The rename skipped
any line matching those, which is why `Mifos` still appears in 59 files' KDoc.

**319 tests green** across every module, including the Roborazzi device-free render tier.

---

## S3c tail (2026-10-21)

### `collateraldetails` — and a divergence I had introduced

The read-only list of collateral a client has pledged. Porting it surfaced a wiring mistake of my
own: client-general's "Collateral Data" row had been pointed at the add-collateral FORM, where the
original points it at this list (`clientProfileGeneralDestination(collateralData =
navigateToClientCollateralDetailRoute)`). The form is reached from client details' "Create
Collateral". Corrected.

Its ViewModel also took `networkMonitor.isOnline.first()` — a ONE-SHOT read, so a load that began
offline reported an error and never recovered when the connection returned. `ScreenState` reacts to
reconnection for every screen.

### `signature`

A signature turns out to be a DOCUMENT under the reserved name `clientSignature`, so the original's
five use-cases (`GetDocumentsList`, `DownloadDocument`, `CreateSignature`, `UpdateSignature`,
`RemoveDocument`) collapse to one stream plus three commands. The id is **derived** from the
document list rather than stored: the original wrote it in one step and read it back in another,
where the second silently did nothing if the first had not landed (`state.signatureId?.let { … }`
with no else).

Needed new infrastructure: `krop` + `compose-signature` in the catalog, `AppImageCropperDialog` and
`AppSignatureDrawDialog` ported into `core/ui`, and `downloadDocument` on `DocumentRepository` (a
one-shot blob — an attachment has no useful TTL, and the document LIST is what the store holds).

Reached from client details' "Update Signature", which was `null` until now.

### `survey` — deliberately NOT ported

Its only caller is the dead `clientDetailRoute`. Porting it would add three screens no live screen
can open. Tracked as `client-survey-unreachable-in-original`; the substrate (`surveysStream` +
`submitScore`) is ready.

### Nav smoke test

`NavigationGraphSmokeTest`'s KDoc said a `parameterisedRoutesInstantiateWithDefaults` test would
"return when there is something to instantiate". There are now 14 arg-carrying routes, so it is
back — constructing each with explicit arguments, which is what keeps sentinel-default routes
(`ActivateRoute(id = -1, type = "")`) from reappearing.

**329 tests green** across every module.


---

# S4e — the client's loan accounts

Ported out of client-slice order, because it is the loan PROFILE's only caller in the original
(`clientLoanAccountsDestination(navigateToViewAccount = navigateToLoanAccountProfileScreen)`). The
spec's new "Verify the PATH into the screen" rule says to port a screen's caller in the same slice;
this is the first port to follow it, and it discharges `onLoanAccounts = null` on the client General
screen at the same time.

Both of its card actions are live: View Account → the loan profile, Make Repayment → the repayment
form. It is the first screen in this port where a card's whole action menu works.

## FIXED: the filters destroyed the list they filter

The original stored the SAME list in both fields:

```kotlin
val loanAccounts = repository.getClientAccounts(clientId).loanAccounts
    .filter { it.accountNo?.contains(state.searchText.trim()) == true }
…copy(loanAccounts = loanAccounts, unfilteredLoanAccounts = loanAccounts)
```

`unfilteredLoanAccounts` was already search-filtered, so "clear filters" restored the SEARCH result
and a status filter applied on top of a search could never be widened back. Both the query and the
status set are now a derivation over the one stream, via `combineContent` — the substrate's own
filter/sort seam, so Loading / NoNetwork / Error / Unauthenticated pass through untouched and only
Content is filtered. Nothing is stored twice, so nothing can disagree. Pinned by
`clearingTheFilterRestoresTheFULLListNotTheSearchResult`.

## FIXED: a refresh silently cleared the officer's status filter

Every load did `selectedStatus = emptySet()`. Against a stream that re-reads on reconnect and on
pull-to-refresh, that wipes the filter repeatedly and apparently at random. Because the filter is a
derivation now, a refresh re-applies it.

## FIXED: the header count reported the FILTER's own result

It counted the filtered list, so the one number that could have told the officer their filter was
too narrow read "0 item". It is the unfiltered total now — and the header's search/add/filter
controls are drawn from that total too, where the original hid them when the FILTERED list was
empty. Filtering everything out used to remove the only way to clear the filter.

## Two empty states, where there was one

"This client has no loan accounts" offers to create one; "nothing matches your search or filter"
must not — creating an account would not reveal the accounts the filter is hiding. The original
showed one message for both and offered the create button in each.

## Smaller fixes

- `symbol + (if (pending) "N/A" else amount.toString())` rendered **"₹Not Available"** — a currency
  symbol glued to the words. The symbol belongs to an actual amount.
- the count line was `count + " " + stringResource(client_savings_item)` — concatenation no
  translation can reorder, and the string it reused was the SAVINGS screen's "item".
- the search matched case-SENSITIVELY on account numbers that can carry a letter prefix.
- closing the search bar left the query in place, so a hidden query kept filtering with nothing on
  screen to explain why. It clears now.
- `onActionClicked` passed `loan.id ?: 0`, and loan 0 is a request for a loan that does not exist.
  A card whose loan has no id offers no actions.
- `AddAccount` fetched the client a SECOND time at tap time just to read `accountNo` into the event
  — un-guarded, so a tap while offline threw out of `handleAction`. The event carries the client id;
  the create screen can read what else it needs.
- the `networkMonitor.isOnline.first()` gate refused to even try when it believed itself offline,
  showing "No internet connection, Try Again" over a blank screen while a good cached copy sat in
  the database.

## Carried over: the four filters do not cover every status

`waitingForDisbursal` matches none of ACTIVE / PENDING / CLOSED / OVERPAID, so an
approved-but-undisbursed loan disappears under ANY active filter. The original's behaviour; adding
a fifth bucket is a product decision. Pinned by a test so the hole is visible rather than
surprising, and logged as `client-loan-accounts-filters-do-not-cover-awaiting-disbursal`.

## A recurring hazard, noted twice now

`stateIn(..., WhileSubscribed)` hands back its INITIAL value whenever nothing is collecting. On the
repayment screen that was a real defect — the WRITE read two such flows, so the double-payment
guard read `false` and the account number read `null`; both are `Eagerly` now. Here `totalCount` is
render-only, so `WhileSubscribed` is right and it was the TEST that had to collect rather than read
`.value`. The rule that falls out: **a flow read by anything other than the composition must not be
`WhileSubscribed`.**

**28 tests** — 19 ViewModel, 9 render.

## S4l — the loan-accounts "add account" edge

`onAddAccount` went from `null` to `navController::navigateToNewLoanApplication` — one line, because
the whole chain (action → event → callback → test tag) was already ported and correctly `null`-gated
so the affordance was **absent rather than drawn dead**. See `feature/loan/MIGRATION.md#s4l`.

Worth contrasting with S4k, where `feature/groups` had the inverse: a live destination with no
control. Both are caught by the §6a path rule, but only this one is caught by the two-grep check.

## S5d — twelve screens had no top bar

Draining the action-dispatch sweep to zero surfaced the largest single defect in this feature: **all
twelve `feature/client` push destinations rendered with no title and no back affordance.** The shell
draws a top bar only for tab roots —

```kotlin
topBar = { if (!isTabRoot) return@KptRootScaffold; DashboardTopBar(…) }
```

— and only `ClientChargesScreen` had a scaffold of its own. So an officer on the client profile, its
accounts, addresses, documents, identifiers, signature, loan accounts or details saw an untitled
page with no way back except the system gesture, which does not exist on desktop or web.

That is exactly *why* nine client ViewModels declared a `NavigateBack` the sweep could not find a
dispatcher for: the control that would send it had never been drawn. Same inverse shape as the
group-detail overflow menu — a complete chain with no control — and invisible to both the
dead-clickable audit and the §6a two-grep check, because both start from the route.

Each stateful half now wraps its content in `AppScaffold` with a title and a back that **dispatches**
the action; the stateless half takes the insets as a defaulted `scaffoldPadding`, so tests and
previews still render it alone.

Also fixed here: the identifiers "+" was a bare `Icon` with **no clickable modifier** — officers
tapped it and nothing happened — while `AddIdentifierClicked` was handled all the way through to the
nav callback. Now an `IconButton` with a real `contentDescription` (it was `null`). And
`ClientLoanAccountsAction.Refresh` was deleted: dead code from the refactor that dropped the
hand-rolled `PullToRefreshBox`.

## S5e — "Apply for a new account"

Ported from `6b66e8a43:feature/client/clientApplyNewApplications/` (201-line screen + 57-line
ViewModel). Five account types, each row opening that type's application.

Ported now rather than as its own slice because it is the original's real entry point for all five —
and because it fixes a gap in S5c: that slice landed the savings wizard serving both owners but
wired only group detail's caller, leaving the **entire client path unreachable**. The caller rule
applies per *path*, not per screen.

- **Only ported rows are drawn.** Loan, savings and recurring deposit; share and fixed deposit are
  absent. The original drew all five unconditionally, so three would have opened nothing here.
- **The status gate reads the client** rather than taking it as a nav argument. The original carried
  `status` in the route — a snapshot, so activating a client elsewhere and returning left the gate
  refusing an application the server would now accept.
- The original reused the **loan** subtitle under the Share row, so that option described the wrong
  product.
- Its stateless content took a `NavController` to render a breadcrumb that is not ported, which is
  what made the screen untestable without one. A scaffold title stands in.

`onApplyNewApplication` on client details went from `null` to live — the row was already gated and
waiting.

## Charges moved to `feature/charge` (2026-10-23)

`charges/ClientChargesScreen.kt` + `ClientChargesViewModel.kt` (and `ClientChargesViewModelTest`),
`ClientChargesRoute`, `navigateToClientCharges` and the three `feature_client_charges_*` strings are
**deleted** — the client, loan and savings charge screens are now ONE module keyed by
`(resourceType, resourceId)`. See `feature/charge/MIGRATION.md`.

- "Upcoming Charges" (client General) → `navigateToCharges(ChargeResource.CLIENT, clientId)`.
- "Add Charge" (client Details) went from `null` (row absent — there was no client-charge create) to
  `navigateToAddCharge(ChargeResource.CLIENT, clientId)`, which is where the original's
  `navigateToChargesRoute(it, clients)` landed: on the add form.
- Edit is not offered on a client charge: Fineract has no `PUT clients/{id}/charges/{chargeId}`
  (the original offered it and then refused it).

`feature/client` now depends on `feature/charge` (one-directional).

## Datatables in create-client (2026-10-24)

The original's datatable step (`feature/data-table/dataTableList`) is now part of the create-client
form: one section per client datatable, one field per column (text, number, decimal, date, boolean,
code lookup), NOT NULL columns required, sent in `ClientPayload.datatables` with the create
(`createnew/ClientDataTables.kt`). The original's form was inert — every field hardwired to
`value = ""` / `onValueChange = {}` — and its payload list started `null`, so `?.add` never added and
the client was created with `datatables = null` regardless.

## Audit pass (2026-10-25)

The profile's group chip opens the group detail (client → groups dependency). The loan and deposit account lists format money with `CurrencyFormatter` ("MX$0" rather than "$0.0"). Add charge opens and correctly reports that the gateway has no client charge types.

Bug fixed (2026-10-26): the profile's Group chip read the scalar `groupName`/`groupId`, which Fineract never sends, so every member showed "Group: N/A". It also defaulted `groupId` to 0, so with no group the chip would have opened group 0. It now reads `groups.first()` (`ClientProfileGroupChipUiTest`).

## Create drafts (2026-10-26)

Every create form here keeps a draft through core-base's `FormDraftSlot` (template: `submit/draft/`). An offline submit, or one that fails on the network, is kept automatically and replayed on reconnect. A refusal is kept only on **Save as draft**, as FAILED with the server's reason, and is not replayed. When the form opens on a slot that holds a draft, a Resume / Discard banner is shown. Drafts are listed in Settings › Sync & Drafts with an **Open** action that returns to the form.

## Activate client + create group (2026-10-26)

A pending client's details show **Activate Client** at the bottom, as in the original; `navigateToActivateClient` had no caller. `AppScaffold` dropped every `bottomBar`, and now forwards it. Activate failures show the server's reason. An online create group opens the new group, as the original did.
