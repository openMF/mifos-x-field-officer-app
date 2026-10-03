# feature/client — migration ledger

> Written at S3 per `FEATURE_LAYER_MIGRATION_SPEC.md` §7. Original reference: `6b66e8a43`.
> Retired `core/domain` reference: `897ffdac1`.

**Scope.** The largest slice: 163 `.kt` in `feature/client` plus the shared drill-downs it is the
first consumer of — `note` (8), `document` (8), `data-table` (20), `search-record` (4), `activate`
(5). **208 files, ~36% of the whole migration.**

**Status:** inventory complete. Port in progress, subdivided `3a` list → `3b` detail+charges →
`3c` actions → `3d` shared drill-downs.

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
| charges | `charges/**` | `ClientChargeRepository` + `ChargeCommandRepository` |
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

Client becomes a bottom-nav tab, so `TabRegistry.tabs` gains it **by hand** in slice order
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
| 3 | every authenticated call 401, while sign-in itself worked | `updateFineractUser` stored `"Basic $key"`, and `AuthScheme.BASIC.format` prefixes again → `Basic Basic …` | store the RAW credential (`ProjectPreferencesRepositoryImpl`) |
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
