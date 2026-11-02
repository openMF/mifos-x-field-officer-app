# feature/home — migration ledger

> Written at Phase 9 T3 per `FEATURE_LAYER_MIGRATION_SPEC.md` §7. Consumed by Phase 12 (S2).
> Original reference: `6b66e8a43`.

**Status:** audit complete. **There is no `feature/home` in the original app** — this is a template
shell that absorbs the original's `feature/search`, per the backbone-tab decision (spec §6).

Module resolution: `fs_path=feature/home  shape=flat  via=disk-unique`, exit 0.

---

## 1. Original inventory

**No original `feature/home` exists.** The original bottom bar was Search · Client · Centers · Groups
with no Home tab; `Search` was effectively the dashboard and the app's start destination.

The absorbed original is `6b66e8a43:feature/search/` — 9 `.kt`:

| original file | disposition |
|---|---|
| `SearchScreen.kt` · `SearchUiState.kt` · `SearchViewModel.kt` | **PORT** into `feature/home` as the Home tab's content |
| `components/SearchBox.kt` · `components/FilterDialog.kt` · `components/SearchScreenResult.kt` | **PORTED INLINE, not as files** — see §7 |
| `navigation/SearchNavigation.kt` · `navigation/SearchScreens.kt` | rewritten — Home is a backbone tab, not a `@FeatureDestination` |
| `di/SearchModule.kt` | merge into `feature/home/di/HomeModule.kt` |
| `composeResources/values/feature_search_strings.xml` | PORT |
| `composeResources/values-es/feature_search_strings.xml` | **PORT — existing Spanish localization.** Dropping it is a silent locale regression (RULE-IDEA-I18N-COMPLETENESS-001). |

## 2. Current shell inventory — 6 files, `owner: template`

`HomeScreen.kt` · `HomeScreenPreview.kt` · `HomeDestination.kt` · `ui/TestTags.kt` ·
`di/HomeModule.kt` · `commonTest/HomeScreenUiTest.kt`.

The shell is a placeholder screen. It is **not** annotated `@FeatureDestination` or `@FeatureTab` —
Home is a backbone tab, declared as `AuthenticatedNavBarTabItem.HomeTab` and added by hand in
`ForkTabRegistry.tabs`. S2 replaces the shell's content, not its registration.

## 3. Substrate mapping

| concern | new |
|---|---|
| search | `kpt.core.data.search.SearchRepository` — **does not exist yet**; added by Phase 11 T5. `SearchApi`, `SearchKeyedReadStores` and `kpt.core.model.objects.SearchedEntity` all exist. |

**S2 is blocked on Phase 11 T5.** Recorded here so it is not discovered mid-slice.

## 4. Write policy

No mutations. Search is read-only.

## 5. New-state decisions

| state | decision |
|---|---|
| `Empty` | a search returning no matches → "no results for '<query>'" with a clear-filters action. New: the original folded empty into its success state. |
| `NoNetwork` | search is server-side, so offline must say so rather than show an empty result — an empty list and an unreachable server are different facts and the original conflated them. |
| `Conflicted` | n/a — no writes |
| `Failed(rolledBack)` | n/a — no writes; a failed query surfaces as `Error` with Retry |
| blank query | returns empty **without** calling the server (asserted by Phase 11 T5's test) |

## 6. Nav registration

- Backbone tab — no `@FeatureTab`, no `@FeatureDestination`. `ForkTabRegistry.tabs` hand-lists
  `HomeTab` + `ProfileTab` then appends fork tabs; S2 appends Client · Centers · Groups in that order,
  because `@FeatureTab` collects alphabetically and tab order is a layout decision (spec §4).
- As an inline tab, Home's start destination must be registered on the navbar's **inner** NavHost via
  `ForkTabRegistry.extraInlineTabDestinations` — not needed for the backbone tabs, which the template
  already wires. Verify during S2 that replacing the content did not break that wiring.
- Build edge: `cmp-navigation/build.gradle.kts:43` already declares
  `implementation(projects.feature.home)` — template-owned, no fork edge needed.

## 7. Deviations from the original

| deviation | reason |
|---|---|
| Search becomes the Home tab rather than its own tab | backbone-tab decision (spec §6): the template always renders Home + Profile, and Search was the original's de-facto dashboard |
| `SearchNavigation`/`SearchScreens` not ported | Home is a backbone tab; a feature-owned nav graph would duplicate the shell's wiring |
| `Empty` and `NoNetwork` split out of the success state | substrate change (spec §3) — these states did not exist in `DataState` |
| the three `components/` files are reimplemented INSIDE `SearchBody.kt` rather than ported as files | their BEHAVIOUR is all present — filter chips, exact-match checkbox, result rows. What is not present is the file boundary. Recorded here because the row above said "PORT" and a reader comparing the file lists would otherwise score three files as missing. |
| **`MultiFloatingActionButton` has no caller** | the original's search screen carried a `+` FAB opening create-client / create-center / create-group. The COMPONENT migrated (it is in `core/ui`); its only consumer did not. None of the three create flows is ported, so the FAB would be three dead controls. Absent rather than inert (RULE-IMPL-DEAD-CLICKABLE-001). **Found by audit 2026-10-20** — the component had 0 references, which is how a migrated-but-unreachable widget hides. It returns with the first create flow. |
| search is EXPLICIT (a Search button), not search-as-you-type | the original's shape, kept. `SearchViewModelTest.typingAloneSendsNothing` pins it — otherwise every keystroke on a three-letter name hits Fineract. |
| `searchJob` replaced by `flatMapLatest` (2026-10-20) | the original cancelled and relaunched a job per submit and hand-lifted each launch into state. `flatMapLatest` IS that cancellation, declared rather than performed, so a slow first query can no longer paint results over a later one. Proven by reverting to `flatMapMerge`, which fails `aSupersededSearchCannotDeliverItsResults`. |

---

## Audit verdict

**New surface, absorbing a real original.** No original `feature/home` to compare against, so the
audit's job is to pin what `feature/search` contributes — including the `values-es` strings, which are
the one thing here that can be silently lost.

**Re-audited 2026-10-20.** `SearchRepository` landed; the search body is live against Fineract. Two
discrepancies between this ledger and the disk were found and are now recorded in §7: the three
`components/` files exist as behaviour but not as files, and `MultiFloatingActionButton` migrated
into `core/ui` with zero consumers. Neither is a silent drop any more.

## Audit pass (2026-10-25)

Search: centre results open the centre detail, and the **create FAB** (client / centre / group) is ported. **Bug fixed:** results and creates navigated on the INNER bottom-bar controller, where no feature destination is registered, so Create centre crashed the app. Both now go on `outerNavController`. Verified on the device: all three creates and all three result types open with no crash. The filter chips scroll instead of wrapping ("Savin / gs"), and the FAB items announce "Create client" etc. instead of the enum name.
