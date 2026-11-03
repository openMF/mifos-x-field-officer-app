# `feature/settings/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 83 tracked files — 38× `.kt`, 20× `.xml`, 20× `.png`, 2× `.md`, 1× `.gitignore`

**38 Kotlin files** — 4 screen(s), 5 ViewModel(s).

Screens: `NotificationScreen`, `ServerConfigScreen`, `SettingsScreen`, `SyncAndDraftsScreen`

ViewModels: `ConflictInboxViewModel`, `ServerConfigViewModel`, `SettingsViewModel`, `SignOutViewModel`, `SyncAndDraftsViewModel`

**Consumes:** `core/store/auth`, `core/store/serverconfig`, `core/store/session`, `core/store/testing`, `core/store/user`

### Docs in the tree

| Path | |
|---|---|
| `feature/settings/MIGRATION.md` | feature/settings — migration ledger |
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

<!-- api-docs:begin module=feature/settings sha=f40ea3911ea84ee25946e1c33b5d53be0fe93ff9 -->
## API reference

_Generated from `feature/settings` at tree `f40ea3911ea8` by `scripts/docs/api-docs-gen.sh`._
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

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt`

```kotlin
fun LanguageDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel())
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsBody.kt:70</code></summary>

```kotlin
) {
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }

    SettingsScreenContent(
        onBackClick = onBackClick,
        onThemeCardClick = { showThemeDialog = true },
```

</details>

```kotlin
fun LanguageDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsBody.kt:70</code></summary>

```kotlin
) {
    var showThemeDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }

    SettingsScreenContent(
        onBackClick = onBackClick,
        onThemeCardClick = { showThemeDialog = true },
```

</details>

```kotlin
fun LanguageChooserRow(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier)
```
_No KDoc at source._

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/Platform.kt`

```kotlin
expect fun getPlatform(): Platform
```
_No KDoc at source._

```kotlin
enum class Platform
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/androidMain/kotlin/kpt/feature/settings/Platform.android.kt:15</code></summary>

```kotlin
import androidx.annotation.ChecksSdkIntAtLeast

actual fun getPlatform(): Platform {
    return Platform.Android
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
```

</details>

```kotlin
expect fun supportsDynamicTheming(): Boolean
```
_No KDoc at source._

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsBody.kt`

```kotlin
fun SettingsBody(
```
The STATEFUL settings body — the fork's replacement for the template's `kpt.feature.settings.demo.SettingsDemoBody`, which `customizer --clean` strips.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsDialog.kt`

```kotlin
fun SettingsDialog(onDismiss: () -> Unit, viewModel: SettingsViewModel = koinViewModel())
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsBody.kt:85</code></summary>

```kotlin

    if (showThemeDialog) {
        SettingsDialog(onDismiss = { showThemeDialog = false })
    }
    if (showLanguageDialog) {
        LanguageDialog(onDismiss = { showLanguageDialog = false })
    }
```

</details>

```kotlin
fun SettingsDialog(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsBody.kt:85</code></summary>

```kotlin

    if (showThemeDialog) {
        SettingsDialog(onDismiss = { showThemeDialog = false })
    }
    if (showLanguageDialog) {
        LanguageDialog(onDismiss = { showLanguageDialog = false })
    }
```

</details>

```kotlin
fun SettingsDialogThemeChooserRow(
```
_No KDoc at source._

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsRoute.kt`

```kotlin
data object SettingsRoute
```
_No KDoc at source._

```kotlin
data object NotificationRoute
```
_No KDoc at source._

```kotlin
data object SyncAndDraftsRoute
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSettings(navOptions: NavOptions? = null) = navigate(SettingsRoute, navOptions)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToNotification(navOptions: NavOptions? = null) = navigate(NotificationRoute, navOptions)
```
_No KDoc at source._

```kotlin
fun NavController.navigateToSyncAndDrafts(navOptions: NavOptions? = null) = navigate(SyncAndDraftsRoute, navOptions)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.settingsDestination(settingsBody: @Composable () -> Unit = {})
```
The settings backbone destination.

```kotlin
fun NavGraphBuilder.notificationDestination(onBackClick: () -> Unit)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.syncAndDraftsDestination(
```
`draftExtras` lets the app name its drafts and reopen their forms (`DraftRowExtras`): called once per screen, it returns the per-row lookup. Default = the template's raw form-key rows.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsViewModel.kt`

```kotlin
class SettingsViewModel(
```
Settings ViewModel — the toolkit's `local_only_prefs` demo, on the MVI `BaseViewModel` idiom (typed actions) with a Store5 read path.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:47</code></summary>

```kotlin

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
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:59</code></summary>

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

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsRoute.kt:61</code></summary>

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

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/detail/ChargeDetailScreen.kt:104</code></summary>

```kotlin
        title = stringResource(Res.string.feature_charge_details),
        onBackPressed = { onAction(ChargeDetailAction.NavigateBack) },
        modifier = modifier.testTag(TestTags.ChargeDetail.SCREEN),
        actions = {
            // Only with something to edit, and only where Fineract can update it.
            if (canEdit && charge is ScreenState.Content) {
                IconButton(
```

</details>

- `const val ABOUT_ROW: String = "settings_about_row"`
- `const val OFFLINE_SYNC_ROW: String = "settings_offline_sync_row"`
- `const val SCREEN: String = "settings_screen"` — Root scaffold — always rendered regardless of dialog state.
- `const val SCREEN: String = "notification_screen"` — Root scaffold — always rendered regardless of content state.
- `const val SCREEN: String = "sync_and_drafts_screen"` — Root scaffold — always rendered regardless of content state.
- `const val PRUNE: String = "sync_and_drafts_prune"` — The manual "Prune expired" button (rendered only when there is content).
- `const val OPEN: String = "sync_and_drafts_open"`

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/di/SettingsModule.kt`

```kotlin
val SettingsModule = module
```
_No KDoc at source._

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigRoute.kt`

```kotlin
data object ServerConfigRoute
```
_No KDoc at source._

```kotlin
fun NavController.navigateToServerConfig(navOptions: NavOptions? = null) =
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.serverConfigDestination(onBackClick: () -> Unit)
```
Server-config destination, installed on the **ROOT** graph — deliberately NOT annotated `@FeatureDestination`.

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigScreen.kt`

```kotlin
fun ServerConfigScreen(
```
Edits the Fineract server configuration — protocol, host, API path, port, tenant. Ported from `6b66e8a43:feature/settings/.../updateServer/UpdateServerConfigScreen.kt`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigRoute.kt:42</code></summary>

```kotlin
fun NavGraphBuilder.serverConfigDestination(onBackClick: () -> Unit) {
    composableWithPushTransitions<ServerConfigRoute> {
        ServerConfigScreen(onBackClick = onBackClick)
    }
}
```

</details>

```kotlin
object ServerConfigTestTags
```
Stable handles for the server-config screen — asserted by UI tests, never shown to a user.

- `const val SCREEN = "server-config:screen"`
- `const val LIVE = "server-config:live"`
- `const val LIVE_BADGE = "server-config:live-badge"`
- `const val BACK = "server-config:back"`
- `const val PROTOCOL = "server-config:protocol"`
- `const val ENDPOINT = "server-config:endpoint"`
- `const val API_PATH = "server-config:api-path"`
- `const val PORT = "server-config:port"`
- `const val TENANT = "server-config:tenant"`
- `const val INSTANCE_PREFIX = "server-config:instance:"`
- `const val DEMO_HINT = "server-config:demo-hint"`
- `const val INSTANCES = "server-config:instances"`
- `const val SWITCH_CONFIRM = "server-config:switch-confirm"`
- `const val SAVE = "server-config:save"`

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigViewModel.kt`

```kotlin
class ServerConfigViewModel(
```
Editor for the Fineract server configuration. Ported from `6b66e8a43:feature/settings/.../updateServer/UpdateServerConfigViewModel.kt`.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/di/SettingsModule.kt:26</code></summary>

```kotlin
    // core-base StoreModule, included via appStoreModule).
    viewModelOf(::ConflictInboxViewModel)
    viewModelOf(::ServerConfigViewModel)
    viewModelOf(::SignOutViewModel)
}
```

</details>

```kotlin
enum class ServerConfigField { Protocol, EndPoint, ApiPath, Port, Tenant }
```
The five editable fields, so failures can be attributed to one.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigScreen.kt:210</code></summary>

```kotlin
                onChange = { viewModel.actionChannel.trySend(ServerConfigAction.ProtocolChanged(it)) },
                label = stringResource(Res.string.feature_settings_server_protocol),
                failure = state.failures[ServerConfigField.Protocol],
                tag = ServerConfigTestTags.PROTOCOL,
            )
            Field(
                value = state.config.endPoint,
```

</details>

```kotlin
data class ServerConfigState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigScreen.kt:277</code></summary>

```kotlin
 */
@Composable
private fun InstancePicker(state: ServerConfigState, onSelect: (String) -> Unit) {
    val selected = state.selectedInstance
    Column(modifier = Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.instances.forEach { instance ->
            InstanceRow(
```

</details>

```kotlin
sealed interface ServerConfigAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigScreen.kt:145</code></summary>

```kotlin
            Surface(tonalElevation = 3.dp, shadowElevation = 3.dp) {
                Button(
                    onClick = { viewModel.actionChannel.trySend(ServerConfigAction.Submit) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
```

</details>

```kotlin
sealed interface ServerConfigEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigScreen.kt:127</code></summary>

```kotlin
                // Confirm, then leave — the original popped back with no confirmation, so a user
                // could not tell a successful save from a dismissed screen.
                ServerConfigEvent.Saved -> {
                    snackbarHostState.showSnackbar(savedMessage)
                    onBackClick()
                }
                ServerConfigEvent.Invalid -> snackbarHostState.showSnackbar(invalidMessage)
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/signout/SignOutDialog.kt`

```kotlin
fun SignOutDialog(state: SignOutState, onAction: (SignOutAction) -> Unit)
```
The sign-out choice, shown while `SignOutState.confirming`. Stateless — `SignOutViewModel` owns it.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBody.kt:122</code></summary>

```kotlin
    )
    if (signOut.confirming) {
        SignOutDialog(state = signOut, onAction = signOutViewModel::trySendAction)
    }
}

/** Stateless half — previews and UI tests drive it directly. */
```

</details>

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/signout/SignOutTestTags.kt`

```kotlin
object SignOutTestTags
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsScreen.kt:155</code></summary>

```kotlin
                    accentColor = MaterialTheme.colorScheme.error,
                    onClick = it,
                    modifier = Modifier.testTag(SignOutTestTags.ROW),
                )
            }
            Spacer(modifier = Modifier.fillMaxWidth().padding(sp.sm))
            VersionLabel(onLongClick = onFooterLongClick)
```

</details>

- `const val ROW = "settings.sign_out"`
- `const val CONFIRM = "settings.sign_out.confirm"`
- `const val KEEP_DATA = "settings.sign_out.keep_data"`
- `const val CLEAR_DATA = "settings.sign_out.clear_data"`
- `const val FAILED = "settings.sign_out.failed"`

### `feature/settings/src/commonMain/kotlin/kpt/feature/settings/signout/SignOutViewModel.kt`

```kotlin
class SignOutViewModel(
```
Sign-out, at the bottom of Settings (moved from the Profile tab, 2026-10-25). Local only, so it works offline — a field officer who cannot sign out without signal is stranded.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBody.kt:101</code></summary>

```kotlin
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
    signOutViewModel: SignOutViewModel = koinViewModel(),
) {
    val state by viewModel.profile.collectAsStateWithLifecycle()
    val signOut by signOutViewModel.stateFlow.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
```

</details>

```kotlin
enum class SignOutMode { SessionOnly, SessionAndData }
```
What sign-out removes. `SessionAndData` is the default: it deletes everything the app stored, unsynced work included. `SessionOnly` keeps cached clients and unsynced work for the next sign-in.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/signout/SignOutDialog.kt:59</code></summary>

```kotlin
                    Text(
                        stringResource(
                            if (state.mode == SignOutMode.SessionAndData) {
                                Res.string.feature_settings_sign_out_confirm_clear
                            } else {
                                Res.string.feature_settings_sign_out_confirm
                            },
```

</details>

```kotlin
data class SignOutState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/signout/SignOutDialog.kt:45</code></summary>

```kotlin
/** The sign-out choice, shown while [SignOutState.confirming]. Stateless — [SignOutViewModel] owns it. */
@Composable
fun SignOutDialog(state: SignOutState, onAction: (SignOutAction) -> Unit) {
    if (state.confirming) {
        AlertDialog(
            onDismissRequest = { onAction(SignOutAction.Cancelled) },
            title = { Text(stringResource(Res.string.feature_settings_sign_out)) },
```

</details>

```kotlin
sealed interface SignOutAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBody.kt:117</code></summary>

```kotlin
        onBack = onBack,
        onServerSettings = onServerSettings,
        onSignOut = { signOutViewModel.trySendAction(SignOutAction.Requested) },
        snackbarHostState = snackbar,
        modifier = modifier,
    )
    if (signOut.confirming) {
```

</details>

```kotlin
sealed interface SignOutEvent
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/settings/src/commonTest/kotlin/kpt/feature/settings/signout/SignOutViewModelTest.kt:59</code></summary>

```kotlin
        assertEquals(SignOutMode.SessionAndData, vm.stateFlow.value.mode)
        vm.trySendAction(SignOutAction.Confirmed)
        assertEquals(SignOutEvent.SignedOut, vm.eventFlow.first())
        assertEquals(listOf(true), auth.signOuts)
    }

    @Test
```

</details>

---

_29 type(s), 46 function(s)/property(ies); 26 carry KDoc at source; 0 authored example(s); 26 live call site(s)._
<!-- api-docs:end -->
