# feature/groups — migration ledger

**Source:** `6b66e8a43:feature/groups` — 13 `.kt` (4 screens, 4 ViewModels)
**Landed:** out of slice order, ahead of S6 — the dashboard's Groups TAB needed a destination
**Status:** read paths, activation and offline download complete, device-verified on the gateway
(2026-10-24). Create (`create/`, `CreateGroupRoute`) landed 2026-10-22 but has **no tests and no
device verification** — both owed before it counts as done. The §7 rows for create and sync are
superseded by those two.

> Module name is `groups` (plural) while its sibling is `center` (singular). That asymmetry is the
> ORIGINAL's and is kept deliberately — renaming would break every path reference in this ledger
> and the provenance trail with it.

---

## 1. Original inventory

| original package | files | migrated to | note |
|---|---|---|---|
| `groupList/` | 2 | `list/` | `GroupsListScreen` + ViewModel |
| `groupDetails/` | 3 | `detail/` | fields + activation; six overflow entries deferred (§7) |
| `createNewGroup/` | 3 | — | **DEFER** (§7) |
| `syncGroupDialog/` | 3 | — | **DEFER** (§7) |
| `navigation/GroupNavGraph.kt` | 1 | `navigation/GroupsNavigation.kt` + `GroupsTab.kt` | tab split out as a `@FeatureTab` |
| `di/GroupsModule.kt` | 1 | `di/GroupsModule.kt` | |

## 2. Use-case classification

| use case | kind | substrate |
|---|---|---|
| list groups | read, cache-first | `GroupRepository.groupsStream` |
| one group's details | read | selected from the same stream |
| activate a group | write, online-required | `GroupCommandRepository.activateGroup` |
| create a group | write | deferred (§7) |
| bulk sync groups | write | deferred (§7) |

## 3. Substrate mapping

Same three removals as `feature/center` §3: the `userStatus` connectivity branch, the manual
`loadMore()` paging, and the per-screen `*UiState` sealed classes. The list and detail ViewModels
mirror the center ones deliberately — three list screens over one substrate that behave differently
would be three screens to debug.

**Group detail is NOT the center detail renamed.** A center's detail exists to reach its GROUPS, so
the port folded `centerGroupList` into it. A group's detail is a leaf: all six of the original's
overflow entries led OUT of `feature/groups` into features that are not ported (§7).

## 4. Write policy

`activateGroup` is `OnlineRequired`. The payload's fields are left null so the SERVER dates the
activation; the original sent a client-side formatted date and so could activate with the device's
wrong clock.

Guarded against double-submit: activation is idempotent server-side, but a second in-flight request
produces a second failure path and a second error message. Pinned by `aDoubleTapSendsOneActivation`,
proven RED by removing the guard.

## 5. New-state decisions

| state | decision |
|---|---|
| `Empty` (list) | filter-matching-nothing, as in center |
| `Empty` (detail) | a group outside the caseload is Empty, never blank fields |
| write failure | rendered BESIDE the fields, not instead of them — a failed activation does not make the group's details untrue |
| `Conflicted` | treated as `Failed` |

## 6. Nav registration

`@FeatureTab GroupsTab` + `@FeatureDestination groupsListDestination`. Uses `Icons.Groups` rather
than the `People` the placeholder borrowed — `People` is the Clients tab's icon, and two tabs
sharing a glyph is genuinely confusing in a monochrome bar. With this tab landing,
`AuthenticatedNavBarTabItem` holds only `HomeTab`.

## 7. Deviations from the original

| deviation | reason |
|---|---|
| six overflow entries (add loan, add savings, documents, group clients, more info, notes) not ported | every one routes into a feature that is not migrated. Absent rather than inert (RULE-IMPL-DEAD-CLICKABLE-001); tracked in `CAPABILITY_GAPS.yaml`. |
| `createNewGroup` not ported | same picker-template blocker as `feature/center` §7 |
| `syncGroupDialog` not ported | belongs with Offline Sync (S8) |
| `canActivate` tests `active != true`, not `== false` | **found on device 2026-10-20.** Fineract leaves `active` NULL for a pending group — exactly the group an officer opens this screen to activate. Group "Ankit" (account 000000021) rendered "Inactive" and offered NO button, because the status text tested `!= true` while `canActivate` tested `== false`. One predicate now, pinned by `aGroupWithAnUnknownStatusStillOffersActivation`. |
| Activate button absent when inactive-only | an already-active group has no button at all rather than a disabled one — as the original did |

---

## Documents + activation (2026-10-21)

Group detail gained two actions from its original's `groupNavGraph` edges, both previously in the
"six absent" list:

- **Documents** → `feature/document`'s generic list, scoped to this group
  (`Constants.ENTITY_TYPE_GROUPS`). That was `loadDocumentList` in the original.
- **Activate** → `feature/activate`, the shared screen. It used to be an INLINE write here posting
  an all-null `ActivatePayload`; see `feature/activate/MIGRATION.md` for why that was wrong and
  what moved.

`GroupsDetailState` emptied out as a result (the write-side fields went with the write), so the
ViewModel is now `BaseViewModel<Unit, E, A>` — the shape `ClientGeneralViewModel` uses.
`GroupWriteError`, `isActivating` and the `WRITE_ERROR` test tag are gone with it.

Group detail also gained **More info** → `feature/datatable`, scoped by
`Constants.DATA_TABLE_NAME_GROUP` (`m_group`) — the original's `loadGroupDataTables` edge. Three of
the "six absent" actions are now live (documents, datatables, activation); the remaining three are
group loan/savings accounts and the bulk sync dialog, which need S4/S5 and the offline slice.

## S4k — the overflow menu

Ported from `6b66e8a43:feature/groups/groupDetails/GroupDetailsScreen.kt:177`. See
`feature/loan/MIGRATION.md#s4k` for the full note — this menu was the CALLER the group loan
application needed, and porting it revealed that Documents and More-Group-Info already had their
entire chain (action → event → handler → nav edge → test tags) with **no control that dispatched
it**. Three items ship; Add Savings Account, Group Clients and Notes stay absent.

Also fixed here: the detail Column had no `verticalScroll`, so the Activate button could sit off a
short screen with no way to reach it.

## Offline download (2026-10-24)

The original's long-press selection + `SyncGroupDialog` is ported onto the shared
`OfflineDownloadRepository.downloadGroups`: long-press selects, a tap toggles while selecting, and the
selection bar downloads each group's clients and their ACTIVE loans and savings (with the templates a
repayment or deposit needs) through the stores the screens read. The group is marked downloaded, and
the group list writer keeps that flag across refreshes. The original's ~550-line ViewModel and the
center's ~790-line one were the same walk written twice; it is one implementation now (core/data).

## Audit pass (2026-10-25)

The three remaining group-detail edges are live: **Loan accounts**, **Savings accounts** and **Group clients** sections (store `GroupGetGroupAccounts` / `GroupGetGroupWithAssociations`), loan and savings rows opening their summaries, and **Notes** in the overflow (`feature/note`, owner `groups`). Member rows are display-only: client → groups already exists, and groups → client would make a cycle. Device-verified: MARIPOSAS MONARCAS (1 loan, 6 clients), Arin Savings (savings 39 with the group name in its header), and a note added and listed on the server under group 2.

Group clients now open the client profile (2026-10-26). feature/client depends on feature/groups, so this edge navigates by route NAME (`clientProfileRoute`). `ClientProfileRouteNameTest` (feature/client) pins that name to `ClientProfileRoute` so a rename fails the build, not the tap. Device-verified: MARIPOSAS MONARCAS → member → profile → Group chip → back to the group.

## Create drafts (2026-10-26)

Every create form here keeps a draft through core-base's `FormDraftSlot` (template: `submit/draft/`). An offline submit, or one that fails on the network, is kept automatically and replayed on reconnect. A refusal is kept only on **Save as draft**, as FAILED with the server's reason, and is not replayed. When the form opens on a slot that holds a draft, a Resume / Discard banner is shown. Drafts are listed in Settings › Sync & Drafts with an **Open** action that returns to the form.
