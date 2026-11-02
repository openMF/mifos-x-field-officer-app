# `feature/profile/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 35 tracked files — 21× `.xml`, 10× `.kt`, 1× `.gitignore`, 1× `.md`, 1× `.kts`

**10 Kotlin files** — 1 screen(s), 1 ViewModel(s).

Screens: `ProfileScreen`

ViewModels: `ProfileViewModel`

**Consumes:** `core/store/profile`

### Docs in the tree

| Path | |
|---|---|
| `feature/profile/README.md` | :feature:profile |

<!-- tree-scaffold:end -->
## Significance

A signed-out placeholder, and a deliberate demonstration of where the seam is. `ProfileInfo` carries
only the app's display name, yet it is a real domain model rather than a raw `String` — because a fork
replaces the *fetcher*, not the screen. Swapping in a signed-in user means widening the model and
returning it from the store, with the ViewModel and composable untouched.

<!-- api-docs:begin module=feature/profile sha=131c309fc7e646d3fa10d45b3f6f7d233027033a -->
## API reference

_Generated from `feature/profile` at tree `131c309fc7e6` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/ProfileRoute.kt`

```kotlin
data object ProfileRoute
```
Route for the profile screen.

```kotlin
fun NavController.navigateToProfile(navOptions: NavOptions? = null) = navigate(ProfileRoute, navOptions)
```
Navigates to the profile screen.

```kotlin
fun NavGraphBuilder.profileDestination(profileBody: @Composable () -> Unit = {})
```
The profile backbone destination.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/TestTags.kt`

```kotlin
object TestTags
```
Append-only test-tag registry for the profile feature. Consumed by Compose UI tests in `feature/profile/src/commonTest/`. APPEND-ONLY contract (RULE-KMP-COMPOSE-UITEST-001 CU-5).

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

- `const val SCREEN: String = "profile_screen"` — Root scaffold — always rendered regardless of content state.

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/demo/ProfileDemoBody.kt`

```kotlin
fun ProfileDemoBody(
```
The profile tab's fork-owned INNER content (the default demo body).

<details><summary>Used in the template — <code>feature/profile/src/commonTest/kotlin/kpt/feature/profile/ProfileDemoBodyUiTest.kt:65</code></summary>

```kotlin
        setContent {
            KptTheme {
                ProfileScreen(profileBody = { ProfileDemoBody(viewModel = ProfileViewModel(FakeProfileRepository())) })
            }
        }
        onNodeWithTag(TestTags.Profile.SCREEN).assertIsDisplayed()
    }
```

</details>

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/demo/ui/ProfileViewModel.kt`

```kotlin
class ProfileViewModel(
```
ViewModel for the profile DEMO body (`feature_profile.combo_id: static_content`).

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/demo/ProfileDemoBody.kt:47</code></summary>

```kotlin
fun ProfileDemoBody(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = retainedKoinViewModel(),
) {
    // Store5 read surface: the body consumes the repository-built STREAM directly, the same way
    // feature/watchlist and feature/macro's detail screen do — `ScreenContent(stream = …)` collects
    // with lifecycle awareness and wires `onRetry = stream::retry` itself, so there is no
```

</details>

### `feature/profile/src/commonMain/kotlin/kpt/feature/profile/di/ProfileModule.kt`

```kotlin
val ProfileModule = module
```
Koin bindings for the profile feature.

---

_3 type(s), 5 function(s)/property(ies); 8 carry KDoc at source; 0 authored example(s); 3 live call site(s)._
<!-- api-docs:end -->
