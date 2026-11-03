# `feature/home/`

> Part of [`feature/`](../feature.md)  
> **Measured:** 59 tracked files — 26× `.kt`, 21× `.xml`, 7× `.png`, 2× `.md`, 1× `.gitignore`

**26 Kotlin files** — 2 screen(s), 2 ViewModel(s).

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

<!-- api-docs:begin module=feature/home sha=b5a46e0a2ef9dc3430ca4c1bf1f02111d915e22b -->
## API reference

_Generated from `feature/home` at tree `b5a46e0a2ef9` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

This module is **fork-owned and writable** — the opposite of `core-base/**`. It is published
here as a WORKED EXAMPLE: this is the shape a generator should produce for a new feature,
with its real Screen / ViewModel / Route wiring. Call into `core/**` and `core-base/**`
rather than re-declaring what they already own.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/HomeDestination.kt`

```kotlin
data object HomeDestination
```
The home graph's route — what other features navigate to.

```kotlin
data object HomeRoute
```
The dashboard screen inside the home graph (its start destination).

```kotlin
fun NavController.navigateToHome(navOptions: NavOptions? = null)
```
Navigates to the home graph.

```kotlin
fun NavGraphBuilder.homeGraph(
```
The backbone home graph. `homeBody` is the fork-owned home content (default supplied by `cmp-navigation`'s `ForkBackboneRegistry.homeBody`); this template graph carries zero demo imports.

### `feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardModels.kt`

```kotlin
data class HomeHeader(
```
The greeting block — rendered from the session at once; date and name fill in as they load.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardHeader.kt:90</code></summary>

```kotlin

@Composable
internal fun Header(header: HomeHeader, style: HeaderStyle, onSettings: () -> Unit, onProfile: () -> Unit) {
    // A preview renders a fixed hour, so its golden does not change with the time of day it was recorded.
    val hour = if (LocalInspectionMode.current) {
        PREVIEW_HOUR
    } else {
```

</details>

```kotlin
enum class HomeCardId
```
Which remote card a slot is — also its retry key and its test tag.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:67</code></summary>

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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardCards.kt:112</code></summary>

```kotlin

@Composable
internal fun CollectionsBody(data: HomeCard.Collections) = Column(verticalArrangement = Arrangement.spacedBy(3.tw)) {
    if (data.clientsDue == 0 && data.collectedByCurrency.isEmpty()) {
        Text(
            stringResource(Res.string.feature_home_dashboard_collections_empty),
            style = KptTheme.typography.bodyMedium,
```

</details>

```kotlin
data class Money(val amount: Double, val currencyCode: String, val currencySymbol: String)
```
An amount in one currency — the dashboard never adds amounts across currencies.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:42</code></summary>

```kotlin
    clientsDue = 14,
    loansDue = 17,
    dueByCurrency = listOf(Money(18_450.0, "MXN", "$")),
    collectedByCurrency = listOf(Money(30_250.0, "MXN", "$")),
    overdueClients = 3,
)
```

</details>

```kotlin
data class MeetingToday(val centerId: Int, val name: String, val groups: Int, val time: String? = null)
```
A centre meeting due on the business date; `time` is `HH:mm` when the calendar carries one.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:57</code></summary>

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
One dashboard card in its own load state.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:146</code></summary>

```kotlin
    header = demoHeader,
    cards = listOf(
        HomeCardSlot(HomeCardId.COLLECTIONS, all ?: collections),
        HomeCardSlot(HomeCardId.WORK_QUEUE, all ?: content(demoQueue)),
        HomeCardSlot(HomeCardId.MEETINGS, all ?: content(demoMeetings)),
        HomeCardSlot(HomeCardId.PORTFOLIO, all ?: content(demoPortfolio)),
    ),
```

</details>

```kotlin
enum class QuickAction
```
Standalone creates and tools only — client-owned account creates start from the client profile.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardQuickActions.kt:68</code></summary>

```kotlin
import org.jetbrains.compose.resources.stringResource

internal val QuickAction.label: StringResource
    get() = when (this) {
        QuickAction.NEW_CLIENT -> Res.string.feature_home_dashboard_qa_new_client
        QuickAction.NEW_GROUP -> Res.string.feature_home_dashboard_qa_new_group
        QuickAction.NEW_CENTER -> Res.string.feature_home_dashboard_qa_new_center
```

</details>

```kotlin
data class HomeUiState(
```
Everything the home dashboard renders, as one immutable snapshot.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:66</code></summary>

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
What the user asked the dashboard to do.

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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardCards.kt:373</code></summary>

```kotlin

@Composable
internal fun WorkQueueBody(data: HomeCard.WorkQueue, draftCount: Int, onNavigate: (HomeNavigation) -> Unit) {
    val c = KptTheme.colorScheme
    // grid grid-cols-2 gap-2.5 — each tile carries its own Stitch tint
    Grid(columns = 2, gap = 2.5f.tw) {
        data.checkerTasks?.let { n ->
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
The stateless dashboard: header, the four remote cards each in its own load state, quick actions and recent records. Taps go to `onNavigate`; `onRetry` reloads one card and `onRefresh` (pull-to-refresh) reloads them all.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:160</code></summary>

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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:47</code></summary>

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
Opens the loan queue for the loans in Fineract `status`.

```kotlin
fun NavGraphBuilder.loanQueueDestination(onBack: () -> Unit, onLoanClick: (Long) -> Unit)
```
Registered by the fork's nav graph, which owns the loan-profile route the rows open.

```kotlin
class LoanQueueViewModel(
```
Backs the loan queue: the officer's own loans in the route's status, scoped by the same staff-or-office rule as the dashboard count.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/di/HomeModule.kt:22</code></summary>

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
Koin bindings for the home feature's view models: dashboard, search and loan queue.

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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/di/HomeModule.kt:20</code></summary>

```kotlin
/** Koin bindings for the home feature's view models: dashboard, search and loan queue. */
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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/search/SearchBody.kt:135</code></summary>

```kotlin
            ).testTag(TestTags.Home.SEARCH_FILTERS),
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
What the search screen shows: the query, the filter and whether to match exactly.

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
What the user did on the search screen.

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
One-off effects of the search screen, for the screen to act on.

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
- `const val HEADER: String = "home_header"` — The greeting block at the top of the dashboard.
- `const val SCOPE_CHIP: String = "home_scope_chip"` — The chip saying whose book the figures cover — "My portfolio" or the office.
- `const val SKELETON: String = "home_skeleton"` — The loading skeleton shown before any card has data.
- `const val COLLECTIONS: String = "home_collections"` — Today's collections card.
- `const val COLLECTIONS_HERO: String = "home_collections_hero"` — The collections card's per-currency hero figure.
- `const val OFFLINE_BANNER: String = "home_offline_banner"` — The banner shown while the device has no network.
- `const val RECENT: String = "home_recent"` — The recently-opened records card.
- `const val RECENT_ROW: String = "home_recent_row"` — One row of the recently-opened records card.
- `const val OPEN_COLLECTION_SHEET: String = "home_open_collection_sheet"` — The collections card's "open collection sheet" link.
- `const val WORK_QUEUE: String = "home_work_queue"` — The work-queue card.
- `const val QUEUE_CHECKER: String = "home_queue_checker"` — The work queue's checker-inbox tile.
  _…more members; read the file._

---

_21 type(s), 22 function(s)/property(ies); 43 carry KDoc at source; 0 authored example(s); 18 live call site(s)._
<!-- api-docs:end -->
