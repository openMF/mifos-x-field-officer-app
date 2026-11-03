# `feature/home/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 51 tracked files — 21× `.xml`, 18× `.kt`, 7× `.png`, 2× `.md`, 1× `.gitignore`

**18 Kotlin files** — 2 screen(s), 2 ViewModel(s).

Screens: `HomeDashboardScreen`, `HomeScreen`

ViewModels: `HomeViewModel`, `SearchViewModel`

**Consumes:** `core/store/center`, `core/store/checkerinbox`, `core/store/collectionsheet`, `core/store/config`, `core/store/dashboard`, `core/store/group`, `core/store/recent`, `core/store/search`, `core/store/serverconfig`, `core/store/session`, `core/store/testing`

### Docs in the tree

| Path | |
|---|---|
| `feature/home/MIGRATION.md` | feature/home — migration ledger |
| `feature/home/README.md` | :feature:home |

<!-- tree-scaffold:end -->
## Significance

The dashboard, and the **4-way `combineScreenStates` fan-in**: loans summary, upcoming bills, rates and
an exchange rate, each an independent card with its own loading, empty, error and content state.

It is also a **fork seam**. `HomeDashboard` is public rather than internal because `cmp-navigation`'s
`ForkBackboneRegistry.homeBody` renders it as the default home body, and it lives in the fork-owned `demo/`
package so `--clean` removes it together with that default. A fork replaces the body without touching
the shell.

<!-- api-docs:begin module=feature/home sha=f7ba7cffe2bba1408391d5d25879743ea2de1cd7 -->
## API reference

_Generated from `feature/home` at tree `f7ba7cffe2bb` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/HomeDestination.kt`

```kotlin
data object HomeDestination
```
_No KDoc at source._

```kotlin
data object HomeRoute
```
_No KDoc at source._

```kotlin
fun NavController.navigateToHome(navOptions: NavOptions? = null)
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.homeGraph(
```
The backbone home graph. `homeBody` is the fork-owned home content (default supplied by `cmp-navigation`'s `ForkBackboneRegistry.homeBody`); this template graph carries zero demo imports.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardModels.kt`

```kotlin
data class HomeHeader(
```
The greeting block — rendered from the session at once; date and name fill in as they load.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:359</code></summary>

```kotlin

@Composable
private fun Header(header: HomeHeader, style: HeaderStyle, onSettings: () -> Unit, onProfile: () -> Unit) {
    val hour = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour
    val greeting = when {
        hour < NOON -> Res.string.feature_home_dashboard_greeting_morning
        hour < EVENING -> Res.string.feature_home_dashboard_greeting_afternoon
```

</details>

```kotlin
enum class HomeCardId { COLLECTIONS, WORK_QUEUE, MEETINGS, PORTFOLIO }
```
Which remote card a slot is — also its retry key and its test tag.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:246</code></summary>

```kotlin
fun HomeDashboardScreenContent(
    state: HomeUiState,
    onRetry: (HomeCardId) -> Unit,
    onRefresh: () -> Unit,
    onNavigate: (HomeNavigation) -> Unit,
    modifier: Modifier = Modifier,
) {
```

</details>

```kotlin
sealed interface HomeCard
```
One remote card's payload. Each card owns its own `ScreenState`, so one failure never blanks the rest.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:304</code></summary>

```kotlin
private fun ContentState(
    state: HomeUiState,
    states: List<ScreenState<HomeCard>>,
    onRetry: (HomeCardId) -> Unit,
    onNavigate: (HomeNavigation) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.tw)) {
```

</details>

```kotlin
data class Money(val amount: Double, val currencyCode: String, val currencySymbol: String)
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:39</code></summary>

```kotlin
private val demoQuickActions = QuickAction.entries.toList()

private val demoCollections = HomeCard.Collections(clientsDue = 14, loansDue = 17, dueByCurrency = listOf(Money(18_450.0, "MXN", "$")), collectedByCurrency = listOf(Money(30_250.0, "MXN", "$")), overdueClients = 3)

private val demoQueue = HomeCard.WorkQueue(
    checkerTasks = 2,
    awaitingApproval = 4,
```

</details>

```kotlin
data class MeetingToday(val centerId: Int, val name: String, val groups: Int, val time: String? = null)
```
A centre meeting due on the business date; `time` is `HH:mm` when the calendar carries one.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:51</code></summary>

```kotlin
private val demoMeetings = HomeCard.Meetings(
    listOf(
        MeetingToday(centerId = 10, name = "Centro Arin", groups = 3, time = "09:30"),
        MeetingToday(centerId = 18, name = "Arin Savings", groups = 2, time = "14:00"),
    ),
)
```

</details>

```kotlin
data class HomeCardSlot(val id: HomeCardId, val state: ScreenState<HomeCard>)
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:488</code></summary>

```kotlin
@Composable
private fun Section(
    slot: HomeCardSlot,
    draftCount: Int,
    businessDate: String?,
    onNavigate: (HomeNavigation) -> Unit,
    onRetry: () -> Unit,
```

</details>

```kotlin
enum class QuickAction { NEW_CLIENT, NEW_GROUP, NEW_CENTER, COLLECTION_SHEET, RUN_REPORTS, SEARCH, PATH_TRACKER }
```
Standalone creates and tools only — client-owned account creates start from the client profile.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:976</code></summary>

```kotlin
// ── #quick_actions (02 content · 04 error · 05 offline) ────────────────────────────────────────

private val QuickAction.label: StringResource
    get() = when (this) {
        QuickAction.NEW_CLIENT -> Res.string.feature_home_dashboard_qa_new_client
        QuickAction.NEW_GROUP -> Res.string.feature_home_dashboard_qa_new_group
        QuickAction.NEW_CENTER -> Res.string.feature_home_dashboard_qa_new_center
```

</details>

```kotlin
data class HomeUiState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:245</code></summary>

```kotlin
@Composable
fun HomeDashboardScreenContent(
    state: HomeUiState,
    onRetry: (HomeCardId) -> Unit,
    onRefresh: () -> Unit,
    onNavigate: (HomeNavigation) -> Unit,
    modifier: Modifier = Modifier,
```

</details>

```kotlin
sealed interface HomeAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt:81</code></summary>

```kotlin
    private val serverConfigRepository: ServerConfigRepository,
    private val recentRecordsRepository: RecentRecordsRepository,
) : BaseViewModel<HomeUiState, Nothing, HomeAction>(HomeUiState()) {

    /** The live streams behind each card, by source — what that card's retry re-requests. */
    private val cardStreams = HomeCardId.entries.associateWith { mutableMapOf<String, ScreenDataStream<*>>() }
```

</details>

```kotlin
sealed interface HomeNavigation
```
Where a tap goes; the nav graph owns the destinations.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:228</code></summary>

```kotlin
@Composable
fun HomeDashboardScreen(
    onNavigate: (HomeNavigation) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
```

</details>

### `feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt`

```kotlin
fun HomeDashboardScreen(
```
The Home tab body, built from the Stitch markup — one composition per state, each from its own `idea-layer/mockups/home/stitch/0N-home-<state>/code.html`: 01 loading · 02 content · 03 partial_failure · 04 error · 05 offline Measurements are the HTML's Tailwind units, read through `tw` (one unit = `spacing.xs` = 4 dp), so `p-3.5` is `3.5f.tw` and `w-11` is `11.tw`; colours are the theme slots the Tailwind names map to (indigo-50 → primaryContainer, emerald → secondary, amber → tertiary, rose → the finance negative pair, slate-100 → surfaceContainer). Every tap leaves through `onNavigate`.

```kotlin
fun HomeDashboardScreenContent(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:118</code></summary>

```kotlin
    // Light, on the app background — the scheme every Stitch render is drawn in.
    Surface(color = KptBaseTheme.colorScheme.background) {
        HomeDashboardScreenContent(state = state, onRetry = {}, onRefresh = {}, onNavigate = {})
    }
}

/** 01-home-loading */
```

</details>

### `feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeViewModel.kt`

```kotlin
class HomeViewModel(
```
The field officer's day — the dashboard fan-out shape (FEATURE_LAYER.md): one `ScreenState` slot per card, each from its own repository stream, so one slow or failing source never blanks the others.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:230</code></summary>

```kotlin
    onNavigate: (HomeNavigation) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    HomeDashboardScreenContent(
        state = state,
```

</details>

### `feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/LoanQueue.kt`

```kotlin
data class LoanQueueRoute(val status: Int)
```
The officer's own loans in one Fineract status (100 submitted · 200 approved) — the work-queue tap-through.

```kotlin
fun NavController.navigateToLoanQueue(status: Int) = navigate(LoanQueueRoute(status))
```
_No KDoc at source._

```kotlin
fun NavGraphBuilder.loanQueueDestination(onBack: () -> Unit, onLoanClick: (Long) -> Unit)
```
Registered by the fork's nav graph, which owns the loan-profile route the rows open.

```kotlin
class LoanQueueViewModel(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/di/HomeModule.kt:21</code></summary>

```kotlin
    viewModelOf(::SearchViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::LoanQueueViewModel)
}
```

</details>

### `feature/home/src/commonMain/kotlin/kpt/feature/home/di/HomeModule.kt`

```kotlin
val HomeModule = module
```
_No KDoc at source._

### `feature/home/src/commonMain/kotlin/kpt/feature/home/search/SearchBody.kt`

```kotlin
fun SearchBody(
```
The Home tab's body: Fineract resource search. Supplied to the shell through `ForkBackboneRegistry.homeBody`, which is the template's fork seam and was EMPTY — which is why Home rendered as a blank screen on device before S2.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/search/SearchViewModel.kt`

```kotlin
class SearchViewModel(
```
Fineract resource search — the Home tab's content. Ported from `6b66e8a43:feature/search/.../SearchViewModel.kt`.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/di/HomeModule.kt:19</code></summary>

```kotlin

val HomeModule = module {
    viewModelOf(::SearchViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::LoanQueueViewModel)
}
```

</details>

```kotlin
enum class SearchFilter(val apiValue: String)
```
A search filter. `apiValue` is a plain constant, NOT a string resource.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/search/SearchBody.kt:133</code></summary>

```kotlin
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).testTag(TestTags.Home.SEARCH_FILTERS),
        ) {
            SearchFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.filter == filter,
                    // Tapping the selected chip clears it — the original's filter was nullable and
                    // there was no other way back to "all resource types".
```

</details>

```kotlin
data class SearchState(
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/search/SearchBody.kt:97</code></summary>

```kotlin
@Composable
internal fun SearchBodyContent(
    state: SearchState,
    results: ScreenState<List<SearchedEntity>>,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
    onResultClick: ((SearchedEntity) -> Unit)? = null,
```

</details>

```kotlin
sealed interface SearchAction
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/search/SearchBody.kt:99</code></summary>

```kotlin
    state: SearchState,
    results: ScreenState<List<SearchedEntity>>,
    onAction: (SearchAction) -> Unit,
    modifier: Modifier = Modifier,
    onResultClick: ((SearchedEntity) -> Unit)? = null,
) {
    Column(
```

</details>

```kotlin
sealed interface SearchEvent
```
_No KDoc at source._

### `feature/home/src/commonMain/kotlin/kpt/feature/home/ui/TestTags.kt`

```kotlin
object TestTags
```
Test-tag registry for the home / bottom-nav dashboard. Consumed by: - `HomeScreen` via `Modifier.testTag(...)`.

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

- `const val SCREEN: String = "home.screen"` — Root `Scaffold` surface of `HomeScreen` — always rendered regardless of dashboard loading state. Stable target for Compose UI tests.
- `const val DASHBOARD_SCROLL: String = "home_dashboard_scroll"` — The vertically-scrollable dashboard `Column`.
- `const val DASHBOARD: String = "home_dashboard"` — The field officer dashboard — the Home body supplied through ForkBackboneRegistry.homeBody. Values are the `testTag`s declared in `idea-layer/screens/home/ui.yaml`, which the Maestro flows assert against; change one there, change it here.
- `const val HEADER: String = "home_header"`
- `const val SCOPE_CHIP: String = "home_scope_chip"`
- `const val SKELETON: String = "home_skeleton"`
- `const val COLLECTIONS: String = "home_collections"`
- `const val COLLECTIONS_HERO: String = "home_collections_hero"`
- `const val OFFLINE_BANNER: String = "home_offline_banner"`
- `const val RECENT: String = "home_recent"`
- `const val RECENT_ROW: String = "home_recent_row"`
- `const val OPEN_COLLECTION_SHEET: String = "home_open_collection_sheet"`
- `const val WORK_QUEUE: String = "home_work_queue"`
- `const val QUEUE_CHECKER: String = "home_queue_checker"`
  _…more members; read the file._

---

_21 type(s), 22 function(s)/property(ies); 18 carry KDoc at source; 0 authored example(s); 18 live call site(s)._
<!-- api-docs:end -->
