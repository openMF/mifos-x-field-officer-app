# `feature/profile/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 49 tracked files — 21× `.xml`, 14× `.kt`, 9× `.png`, 2× `.md`, 1× `.gitignore`

**14 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `ProfileScreen`

ViewModels: `ProfileViewModel`

**Consumes:** `core/store/auth`, `core/store/dashboard`, `core/store/serverconfig`, `core/store/session`, `core/store/staff`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/profile/MIGRATION.md` | feature/profile — migration ledger |
| `feature/profile/README.md` | :feature:profile |

<!-- tree-scaffold:end -->
## Significance

A signed-out placeholder, and a deliberate demonstration of where the seam is. `ProfileInfo` carries
only the app's display name, yet it is a real domain model rather than a raw `String` — because a fork
replaces the *fetcher*, not the screen. Swapping in a signed-in user means widening the model and
returning it from the store, with the ViewModel and composable untouched.

<!-- api-docs:begin module=feature/profile sha=661b010e3b83a0a02b1fe86cb01def3e165c9c98 -->
## API reference

_Generated from `feature/profile` at tree `661b010e3b83` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/ProfileRoute.kt`

```kotlin
data object ProfileRoute
```
The signed-in user's profile screen.

```kotlin
fun NavController.navigateToProfile(navOptions: NavOptions? = null) = navigate(ProfileRoute, navOptions)
```
Opens the profile screen.

```kotlin
fun NavGraphBuilder.profileDestination(profileBody: @Composable () -> Unit = {})
```
The profile backbone destination.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/TestTags.kt`

```kotlin
object TestTags
```
Test-tag registry for the profile screen; the Compose UI tests and Maestro flows select by these.

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

- `const val SCREEN: String = "profile_screen"` — Root scaffold — always rendered regardless of content state.
- `const val BODY: String = "profile.body"` — The fork body, supplied through ForkBackboneRegistry.profileBody.
- `const val USERNAME: String = "profile.username"` — The username line in the identity card.
- `const val SERVER: String = "profile_session"` — The server-session card.
- `const val OFFLINE_BANNER: String = "profile_offline_banner"` — The banner shown while the device has no network.
- `const val IDENTITY: String = "profile_identity"` — The identity card.
- `const val FULL_NAME: String = "profile_full_name"` — The user's full name.
- `const val STAFF_CHIP: String = "profile_staff_chip"` — The chip naming the user's staff role.
- `const val NAME_ERROR: String = "profile_name_error"` — The error shown when the user record failed to load.
- `const val RETRY: String = "profile_retry"` — The retry button beside the name error.
- `const val SCOPE: String = "profile_scope"` — The line saying whose book the user works on.
- `const val ROLES: String = "profile_roles"` — The user's role chips.
- `const val LIVE: String = "profile_live"` — The "Live" badge on a published instance.
- `const val SERVER_SETTINGS: String = "profile_server_settings"` — The link to the server settings.
  _…more members; read the file._

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/di/ProfileModule.kt`

```kotlin
val ProfileModule = module
```
Profile backbone DI. The template's demo profile body (`kpt.feature.profile.demo.**`) and its `core/store/profile` read port are `owner: demo` and were removed by `scripts/remove-demo.sh`.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBody.kt`

```kotlin
fun ProfileBody(
```
The officer's own account (screens/profile, Stitch 01–05). Supplied through `ForkBackboneRegistry.profileBody`, which only wires the navigation.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileViewModel.kt`

```kotlin
class ProfileViewModel(
```
The signed-in officer's own account (screens/profile): who they are, which office Home counts, which server they are on — and the one write, a password change.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/di/ProfileModule.kt:26</code></summary>

```kotlin
 */
val ProfileModule = module {
    viewModelOf(::ProfileViewModel)
}
```

</details>

```kotlin
data class ProfileUiState(
```
What the profile screen shows: the signed-in user, their staff link, the server session and the change-password sheet.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBody.kt:124</code></summary>

```kotlin
@Composable
internal fun ProfileBodyContent(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    onBack: () -> Unit,
    onServerSettings: () -> Unit,
    onSignOut: () -> Unit,
```

</details>

```kotlin
sealed interface NameState
```
The load state of the user's full name.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBody.kt:148</code></summary>

```kotlin
            if (!state.online) OfflineBanner()
            Identity(state)
            (state.name as? NameState.Failed)?.let { NameError(it.reason) { onAction(ProfileAction.Retry) } }
            OfficeAndScope(state)
            Roles(state)
            Session(state, onServerSettings)
            Account(state, onChangePassword = { onAction(ProfileAction.OpenChangePassword) }, onSignOut = onSignOut)
```

</details>

```kotlin
data class ServerSession(
```
The server session shown in the profile's server card.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBodyPreviews.kt:27</code></summary>

```kotlin
    roles = listOf("OFICIAL DE CAMPO"),
    name = NameState.Loaded,
    server = ServerSession(
        instanceName = "Mifos field officer gateway",
        known = true,
        url = "https://apis.mifos.community/1.0/field/v1/",
        tenant = "mifos-bank-1",
```

</details>

```kotlin
data class PasswordForm(
```
The change-password sheet's form.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBodyPreviews.kt:86</code></summary>

```kotlin
internal fun ProfileChangePasswordPreview() = KptTheme(darkTheme = false) {
    Surface(color = KptBaseTheme.colorScheme.surface) {
        PasswordSheetBody(form = PasswordForm(new = "Field#Office2026", repeat = "Field#Office20"), onAction = {})
    }
}

@Preview @Composable
```

</details>

```kotlin
sealed interface PasswordError
```
Why a password change failed.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBodyPreviews.kt:97</code></summary>

```kotlin
                new = "abc",
                repeat = "abc",
                error = PasswordError.Server(
                    "Password must be 12 to 50 characters long, containing at least one uppercase letter, " +
                        "one lowercase letter, one numeric digit, and one special character, " +
                        "with no spaces or consecutive repeating characters",
                ),
```

</details>

```kotlin
sealed interface ProfileAction
```
What the user did on the profile screen.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBody.kt:125</code></summary>

```kotlin
internal fun ProfileBodyContent(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    onBack: () -> Unit,
    onServerSettings: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
```

</details>

```kotlin
sealed interface ProfileEvent
```
One-off effects of the profile screen, for the screen to act on.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/identity/ProfileBody.kt:105</code></summary>

```kotlin
    EventsEffect(viewModel.eventFlow) { event ->
        when (event) {
            ProfileEvent.PasswordChanged ->
                snackbar.showSnackbar(getString(Res.string.feature_profile_password_changed))
        }
    }
    ProfileBodyContent(
```

</details>

---

_10 type(s), 18 function(s)/property(ies); 28 carry KDoc at source; 0 authored example(s); 9 live call site(s)._
<!-- api-docs:end -->
