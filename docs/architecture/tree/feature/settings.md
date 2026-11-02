# `feature/settings/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 76 tracked files — 31× `.kt`, 21× `.png`, 20× `.xml`, 1× `.gitignore`, 1× `.md`

**31 Kotlin files** — 3 screen(s), 3 ViewModel(s).

Screens: `NotificationScreen`, `SettingsScreen`, `SyncAndDraftsScreen`

ViewModels: `ConflictInboxViewModel`, `SettingsViewModel`, `SyncAndDraftsViewModel`

**Consumes:** `core/store/user`

### Docs in the tree

| Path | |
|---|---|
| `feature/settings/README.md` | :feature:settings |

<!-- tree-scaffold:end -->
## Significance

The largest feature, and the one that touches the most cross-cutting state: theme, language, dynamic
colour, screen capture, app lock, the conflict inbox and the sync-and-drafts view.

Two of those are worth calling out. The **conflict inbox** is where a MUTABLE store's divergences
surface for a human to resolve — without it, a server-wins resolution silently discards a user's edit.
The **sync-and-drafts** screen makes the offline write queue visible, which is the difference between
"the app lost my change" and "the app is holding my change until it can send it".

`UserEditableSettings` is deliberately narrower than `UserData`, so a settings screen cannot write a
session or lock field it has no business touching.

<!-- api-docs:begin module=feature/settings sha=09309edba9e35f5e5289ef2721f67b3fd3f88c4f -->
## API reference

_Generated from `feature/settings` at tree `09309edba9e3` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/ConflictInboxViewModel.kt`

```kotlin
class ConflictInboxViewModel(
```
ViewModel for the Settings **Sync conflicts** screen — a live window over the `ConflictInbox` pending feed. Each row is a write the app made that the server diverged on (server-wins was applied, the user's version preserved).

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsScreen.kt:84</code></summary>

```kotlin
    draftExtras: @Composable () -> (DraftRecord) -> DraftRowExtras? = { { null } },
    viewModel: SyncAndDraftsViewModel = koinViewModel(),
    conflictViewModel: ConflictInboxViewModel = koinViewModel(),
) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val conflictState by conflictViewModel.stateFlow.collectAsStateWithLifecycle()
    val conflicts = (conflictState as? ConflictInboxUiState.Success)?.conflicts ?: emptyList()
```

</details>

```kotlin
sealed interface ConflictInboxAction
```
Per-row conflict actions accepted by `ConflictInboxViewModel`.

```kotlin
sealed interface ConflictInboxUiState
```
UI state for the Sync conflicts screen.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsScreen.kt:88</code></summary>

```kotlin
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val conflictState by conflictViewModel.stateFlow.collectAsStateWithLifecycle()
    val conflicts = (conflictState as? ConflictInboxUiState.Success)?.conflicts ?: emptyList()
    SyncAndDraftsScreenContent(
        uiState = uiState,
        conflicts = conflicts,
        onBackClick = onBackClick,
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/DevMenuEntry.kt`

```kotlin
data class DevMenuEntry(
```
DevMenuEntry — a generic row in the Settings screen's "Developer" section.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/demo/SettingsDemoBody.kt:55</code></summary>

```kotlin
    onSyncAndDraftsClick: () -> Unit,
    modifier: Modifier = Modifier,
    devMenuEntries: List<DevMenuEntry> = emptyList(),
    onRateAppClick: (() -> Unit)? = null,
) {
    val analyticsHelper = rememberAnalyticsHelper()
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt`

```kotlin
fun LanguageDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel())
```
Language picker, resolving its own ViewModel. The entry point a screen calls.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/demo/SettingsDemoBody.kt:60</code></summary>

```kotlin
    val analyticsHelper = rememberAnalyticsHelper()
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacySheet by rememberSaveable { mutableStateOf(false) }
    var showDevMenu by rememberSaveable { mutableStateOf(false) }

    if (showSettingsDialog) {
```

</details>

```kotlin
fun LanguageDialog(
```
Language picker over explicit state — the testable overload, and what the stateful one delegates to.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/demo/SettingsDemoBody.kt:60</code></summary>

```kotlin
    val analyticsHelper = rememberAnalyticsHelper()
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacySheet by rememberSaveable { mutableStateOf(false) }
    var showDevMenu by rememberSaveable { mutableStateOf(false) }

    if (showSettingsDialog) {
```

</details>

```kotlin
fun LanguageChooserRow(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)
```
One selectable language row.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/Platform.kt`

```kotlin
expect fun getPlatform(): Platform
```
Which platform the app is running on, for the About screen and for a platform-conditional branch that cannot be expressed as an `expect`/`actual` of its own.

```kotlin
enum class Platform
```
The targets this template builds for.

<details><summary>Used in the template — <code>feature/settings/src/androidMain/kotlin/kpt/feature/settings/Platform.android.kt:16</code></summary>

```kotlin

/** Identifies this target as Android, with its version where the platform exposes one. */
actual fun getPlatform(): Platform {
    return Platform.Android
}

/** Whether a system-derived palette is available. True on API 31+, where Material You exposes wallpaper colours. */
```

</details>

```kotlin
expect fun supportsDynamicTheming(): Boolean
```
Whether the platform exposes a system-derived palette, so the settings screen can hide the dynamic-colour toggle where it would do nothing.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/PrivacySheet.kt`

```kotlin
fun PrivacySheet(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel())
```
Settings → Privacy, as a bottom sheet: the user's consent to usage analytics and crash reporting, each one switch, plus the link to the hosted privacy policy.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/demo/SettingsDemoBody.kt:61</code></summary>

```kotlin
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacySheet by rememberSaveable { mutableStateOf(false) }
    var showDevMenu by rememberSaveable { mutableStateOf(false) }

    if (showSettingsDialog) {
        SettingsDialog(
```

</details>

```kotlin
fun PrivacySheet(
```
Privacy sheet over explicit state — the testable overload.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/demo/SettingsDemoBody.kt:61</code></summary>

```kotlin
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacySheet by rememberSaveable { mutableStateOf(false) }
    var showDevMenu by rememberSaveable { mutableStateOf(false) }

    if (showSettingsDialog) {
        SettingsDialog(
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsDialog.kt`

```kotlin
fun SettingsDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel())
```
Settings dialog, resolving its own ViewModel. The entry point a screen calls.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/demo/SettingsDemoBody.kt:59</code></summary>

```kotlin
) {
    val analyticsHelper = rememberAnalyticsHelper()
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacySheet by rememberSaveable { mutableStateOf(false) }
    var showDevMenu by rememberSaveable { mutableStateOf(false) }
```

</details>

```kotlin
fun SettingsDialog(
```
Settings dialog over explicit state — the testable overload.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/demo/SettingsDemoBody.kt:59</code></summary>

```kotlin
) {
    val analyticsHelper = rememberAnalyticsHelper()
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacySheet by rememberSaveable { mutableStateOf(false) }
    var showDevMenu by rememberSaveable { mutableStateOf(false) }
```

</details>

```kotlin
fun SettingsDialogThemeChooserRow(
```
One selectable theme row.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsRoute.kt`

```kotlin
data object SettingsRoute
```
Route for the settings screen.

```kotlin
data object NotificationRoute
```
Route for notification settings.

```kotlin
data object SyncAndDraftsRoute
```
Route for the sync-and-drafts screen — pending writes and saved drafts.

```kotlin
fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRoute, navOptions)
```
Navigates to settings.

```kotlin
fun NavController.navigateToNotification(navOptions: NavOptions? = null) = navigate(NotificationRoute, navOptions)
```
Navigates to notification settings.

```kotlin
fun NavController.navigateToSyncAndDrafts(navOptions: NavOptions? = null) = navigate(SyncAndDraftsRoute, navOptions)
```
Navigates to the sync-and-drafts screen.

```kotlin
fun NavGraphBuilder.settingsDestination(settingsBody: @Composable () -> Unit = {})
```
The settings backbone destination.

```kotlin
fun NavGraphBuilder.notificationDestination(onBackClick: () -> Unit)
```
Registers the notification-settings destination.

```kotlin
fun NavGraphBuilder.syncAndDraftsDestination(
```
`draftExtras` lets the app name its drafts and reopen their forms (`DraftRowExtras`): called once per screen, it returns the per-row lookup. Default = the template's raw form-key rows.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsViewModel.kt`

```kotlin
class SettingsViewModel(
```
Settings ViewModel — the toolkit's `local_only_prefs` demo, on the MVI `BaseViewModel` idiom (typed actions) with a Store5 read path.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:48</code></summary>

```kotlin
/** Language picker, resolving its own ViewModel. The entry point a screen calls. */
@Composable
fun LanguageDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val settingsState by viewModel.settingsState.collectAsStateWithLifecycle()
    LanguageDialog(
        onDismiss = onDismiss,
        settingsState = settingsState,
```

</details>

```kotlin
sealed interface SettingsAction
```
Preference-mutation intents accepted by `SettingsViewModel`.

```kotlin
data class UserEditableSettings(
```
The subset of preferences this screen owns — deliberately narrower than `UserData`, so a settings screen cannot write a lock or session field.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:61</code></summary>

```kotlin
@Composable
fun LanguageDialog(
    settingsState: ScreenState<UserEditableSettings>,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onChangeLanguage: (language: LanguageConfig) -> Unit,
    modifier: Modifier = Modifier,
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsScreen.kt`

```kotlin
data class DraftRowExtras(
```
What an app adds to one draft row: a readable `title` / `subtitle` in place of the raw form key, and `onOpen` — reopen the form the draft came from, so it can be resumed.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsRoute.kt:89</code></summary>

```kotlin
fun NavGraphBuilder.syncAndDraftsDestination(
    onBackClick: () -> Unit,
    draftExtras: @Composable () -> (DraftRecord) -> DraftRowExtras? = { { null } },
) {
    composableWithPushTransitions<SyncAndDraftsRoute> {
        SyncAndDraftsScreen(
            onBackClick = onBackClick,
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsViewModel.kt`

```kotlin
class SyncAndDraftsViewModel(
```
ViewModel for the template-level **Sync & Drafts** screen (Settings).

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsScreen.kt:83</code></summary>

```kotlin
    modifier: Modifier = Modifier,
    draftExtras: @Composable () -> (DraftRecord) -> DraftRowExtras? = { { null } },
    viewModel: SyncAndDraftsViewModel = koinViewModel(),
    conflictViewModel: ConflictInboxViewModel = koinViewModel(),
) {
    val uiState by viewModel.stateFlow.collectAsStateWithLifecycle()
    val conflictState by conflictViewModel.stateFlow.collectAsStateWithLifecycle()
```

</details>

```kotlin
sealed interface SyncAndDraftsAction
```
Cross-form draft actions accepted by `SyncAndDraftsViewModel`.

```kotlin
sealed interface SyncAndDraftsUiState
```
UI state for the Sync & Drafts screen.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SyncAndDraftsPreview.kt:35</code></summary>

```kotlin
    KptTheme {
        SyncAndDraftsScreenContent(
            uiState = SyncAndDraftsUiState.Success(
                drafts = listOf(
                    DraftRecord(3, "bill", "electricity", SubmitOutboxStatus.PENDING, 0, 0, null),
                    DraftRecord(4, "loan", "home-loan", SubmitOutboxStatus.PENDING, 0, 0, null),
                ),
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the settings feature. Consumed by Compose UI tests in `feature/settings/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

<details><summary>Used in the template — <code>feature/add-to-watchlist/src/commonMain/kotlin/kpt/feature/addtowatchlist/ui/AddToWatchlistStar.kt:51</code></summary>

```kotlin
    IconButton(
        onClick = viewModel::onToggle,
        modifier = modifier.testTag(TestTags.AddToWatchlist.STAR_PREFIX + coinId),
    ) {
        Icon(
            imageVector = if (isTracked) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = stringResource(
```

</details>

- `const val SCREEN: String = "settings_screen"` — Root scaffold — always rendered regardless of dialog state.
- `const val PRIVACY_ROW: String = "settings_privacy_row"` — The Privacy row that opens `PrivacySheet`.
- `const val SHEET: String = "privacy_sheet"` — The bottom sheet itself.
- `const val ANALYTICS_SWITCH: String = "privacy_analytics_switch"` — The usage-analytics consent switch row.
- `const val CRASH_REPORTS_SWITCH: String = "privacy_crash_reports_switch"` — The crash-reporting consent switch row.
- `const val POLICY_LINK: String = "privacy_policy_link"` — The link that opens the privacy policy in the in-app browser.
- `const val SCREEN: String = "notification_screen"` — Root scaffold — always rendered regardless of content state.
- `const val SCREEN: String = "sync_and_drafts_screen"` — Root scaffold — always rendered regardless of content state.
- `const val PRUNE: String = "sync_and_drafts_prune"` — The manual "Prune expired" button (rendered only when there is content).
- `const val OPEN: String = "sync_and_drafts_open"` — A draft row's "Open" button, which resumes the draft in its form.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/demo/SettingsDemoBody.kt`

```kotlin
fun SettingsDemoBody(
```
The settings tab's fork-owned INNER content (the default demo body).

<details><summary>Used in the template — <code>feature/settings/src/commonTest/kotlin/kpt/feature/settings/SettingsScreenUiTest.kt:37</code></summary>

```kotlin
            KptTheme {
                // In production cmp-navigation's BackboneRegistry.settingsBody supplies this body.
                SettingsDemoBody(
                    onBackClick = {},
                    onSyncAndDraftsClick = {},
                )
            }
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/di/SettingsModule.kt`

```kotlin
val SettingsModule = module
```
Koin bindings for the settings feature.

---

_16 type(s), 28 function(s)/property(ies); 44 carry KDoc at source; 0 authored example(s); 17 live call site(s)._
<!-- api-docs:end -->
