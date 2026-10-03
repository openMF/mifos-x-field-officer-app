# feature/settings — migration ledger

> Written at Phase 9 T3 per `FEATURE_LAYER_MIGRATION_SPEC.md` §7. Consumed by Phase 10 (S1,
> server-config half) and Phase 16 (S7, the rest).
> Original reference: `6b66e8a43`. Deleted `core/domain` reference: `897ffdac1`.

**Status:** audit complete. This module is a **RECONCILE, not a port** — the template shell is larger
than the original and supersedes parts of it.

Module resolution (Phase 9 T3): `fs_path=feature/settings  gradle_path=:feature:settings  shape=flat
via=disk-unique`, exit 0. One candidate, so the risk is **overwriting the shell's 29 files**, not
picking the wrong path.

---

## 1. Original inventory — `6b66e8a43:feature/settings/` (12 `.kt` + strings)

| original file | disposition |
|---|---|
| `settings/SettingsScreen.kt` · `settings/SettingsViewModel.kt` | **RECONCILE** against the shell's `SettingsScreen.kt`/`SettingsViewModel.kt` — per-setting comparison, §7 below |
| `updateServer/UpdateServerConfigScreen.kt` · `UpdateServerConfigViewModel.kt` · `UpdateServerConfigEvent.kt` | **PORT → S1** (login links to it; no template equivalent) |
| `navigation/SettingsNavigation.kt` | rewritten as `@FeatureDestination` builders |
| `syncSurvey/SyncSurveysDialog.kt` · `SyncSurveysDialogUiState.kt` · `SyncSurveysDialogViewModel.kt` | **PORT → S7** |
| `syncSurvey/SyncSurveysDialogRepository.kt` · `SyncSurveysDialogRepositoryImp.kt` | **DELETE — do not port.** A feature-local repository. `core/data` already provides `SurveyRepository` + `SurveyCommandRepository`. |
| `di/SettingsModule.kt` | merge into the shell's `di/SettingsModule.kt` |
| `composeResources/values/strings.xml` | merge into the shell's strings |

**This resolves spec §11's open question.** `SurveyRepository`/`SurveyCommandRepository` are not
orphaned substrate built ahead of a non-existent feature — they exist because the original's
`feature/settings/syncSurvey` needed survey sync, and the substrate correctly hoisted that
feature-local repository into `core/data`. §11 can be closed.

## 2. Current shell inventory — 29 `.kt`, all `owner: template`

| shell surface | relation to the original |
|---|---|
| `SyncAndDraftsScreen.kt` · `SyncAndDraftsViewModel.kt` · previews | **SUPERSEDES** the original's sync UI; see also `feature/offline` (S8) |
| `ConflictInboxViewModel.kt` | **NEW capability** — the original had no offline write queue, so no conflicts existed |
| `NotificationScreen.kt` + preview | NEW |
| `LanguageDialog.kt` | NEW — the original had `values-es` resources but no in-app switcher |
| `DevMenuEntry.kt` · `IntegrationProbe.kt` | template dev affordances; keep, not original |
| `SettingsScreen.kt` · `SettingsViewModel.kt` · `SettingsDialog.kt` | the reconcile target |
| `SettingsRoute.kt` · `TestTags.kt` | template-idiomatic; keep |
| `Platform.kt` + 5 actuals (android/desktop/js/native/wasmJs) | **AUDIT under §3a** — is this a genuine platform capability or an expect/actual smell? Resolve in S7. |
| 5 `commonTest` + 1 `desktopTest` | keep; extend, do not replace |

## 3. Substrate mapping

| concern | new |
|---|---|
| server config persistence | `kpt.core.datastore` preferences |
| survey sync | `kpt.core.data.survey.{SurveyRepository, SurveyCommandRepository}` |
| user/session | `kpt.core.datastore.prefs.UserPreferencesRepository` |
| sync queue / conflicts | `kpt.core.data.sync.OfflineQueueRepository` (shell already consumes it) |

## 4. Write policy

| mutation | policy | why |
|---|---|---|
| update server config | **local write**, not a `MutationGateway` mutation | device configuration, not server state. It changes which server subsequent calls target, so it must take effect before the next request — queuing it would be meaningless. |
| survey sync | **`Optimistic`** | surveys are user-authored content; queueing offline and syncing later is the intended field-officer behaviour |
| language / notification prefs | local write | device state |

## 5. New-state decisions

| state | decision |
|---|---|
| `Empty` | survey list empty → "no surveys to sync" with a Sync CTA |
| `NoNetwork` | server-config is editable offline (it is local); **survey sync must show offline and queue**, not fail |
| `Conflicted` | routes to the shell's existing `ConflictInboxViewModel` — the surface the original could not have |
| `Failed(rolledBack)` | Snackbar with the cause; the edited field keeps its value |
| server-config validation | typed reasons from `kpt.core.domain.validation` (§2) — endpoint, port `1..65535`, protocol, API path, tenant |

## 6. Nav registration

- `settingsScreen` / `serverConfigGraph` → `@FeatureDestination` on the authenticated graph.
- Server-config is **also** reachable pre-authentication from login — the same destination registered
  on the authenticated graph is reached from the auth graph by the shell's navController. Confirm during
  S1 T4 that a logged-out user can actually reach it; if the authenticated-graph registration makes it
  unreachable while unauthenticated, it needs a second registration in `authNavGraph`.
- No tab. `settings` appeared in the original's drawer, which the template backbone does not have.
- Build edge: add `"commonMainImplementation"(project(":feature:settings"))` to root
  `feature-deps.gradle.kts` — **not present today**; `cmp-navigation/build.gradle.kts:45` declares
  `implementation(projects.feature.settings)` directly because settings is a template-shell module.
  Adding fork code to it does not change that, so no new edge is needed. Verify before assuming.

## 7. Deviations from the original

| deviation | reason |
|---|---|
| `SyncSurveysDialogRepository`/`Imp` deleted rather than ported | duplicates `core/data`'s `SurveyRepository`; a feature-local repository is the S5-2 read-path defect in miniature |
| original sync UI superseded by `SyncAndDraftsScreen` | the template surface is store-backed and conflict-aware; the original predates the write queue. Per-setting parity still required — see OPEN below. |
| drawer entry becomes an authenticated-graph destination | template backbone has tabs, not a drawer (spec §6 shell decision) |
| **OPEN — per-setting parity not yet enumerated** | the original `SettingsScreen` has not been read line-by-line against the shell's. Until that is done this ledger cannot assert every original setting is present, superseded, or waived. **S7 T1 must produce that table**; S1 touches only `updateServer/**` and must not modify `SettingsScreen.kt`. |
| **OPEN — `Platform.kt` expect/actual** | 6 files of expect/actual in a feature module. Under §3a that is either a genuine platform capability belonging in `core/platform`, or the expect/actual-Composable smell. Not yet read. Resolve in S7. |

---

## Audit verdict

**Partially present, not reconciled.** The template shell supersedes the original's sync UI and adds
three capabilities it never had; the original contributes server-config (S1) and survey sync (S7).
Two items need reading rather than porting: per-setting parity, and the `Platform.kt` expect/actual.
S1's scope is strictly `updateServer/**`.
