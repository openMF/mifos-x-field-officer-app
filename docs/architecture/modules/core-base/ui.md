# `core-base/ui`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_UI.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 78 Kotlin files (13 test) · source sets: `androidInstrumentedTest`, `androidMain`, `commonMain`, `commonTest`, `desktopMain`, `jsCommonMain`, `nativeMain`, `nonAndroidMain`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

**Uses internally** (`implementation`) — not visible to consumers:

`core-base/designsystem`, `core-base/store`

**Consumed by** 2 module(s): `core/store`, `core/ui`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_UI.md`.

## Principal types

- **`AppInfo`** — The SINGLE common-code accessor for the app's user-facing display name.
- **`BackgroundEvent`** — Almost all the events in the app involve navigation or toasts. To prevent accidentally
- **`BaseMutationViewModel`** — The single base class for edit/mutation-screen ViewModels — the one a feature module extends.
- **`BaseViewModel`** — A base [ViewModel] that helps enforce the unidirectional data flow pattern and associated
- **`DefaultLottieAnimations`** — Convenience suspend loaders for the bundled default Lottie animations that ship with
- **`DisplayState`** — Visible-state representation derived from [FreshnessSignal.isRefreshing].
- **`DraftPickerItem`** — One selectable draft in the [DraftPickerList] — a **UI value object**, deliberately independent of
- **`KptFadeThrough`** — Fade-through enter/exit factories. Use for **sibling navigation** —
- **`KptSharedAxis`** — Shared-axis-X enter/exit factories, per Material Motion. Use for
- **`ListItemEnterMath`** — Pure-function math behind [Modifier.kptListItemEnter]. Extracted so the
- **`MutationAction`** — MVI actions emitted by mutation-flow screens (form edit, settings save, etc.).
- **`MutationMode`** — How a [BaseMutationViewModel] handles its submit — the single knob that replaces the old
- …and 6 more documented types

Undocumented: `DashboardProgressState`, `FreshnessTint`, `FreshnessVisual`, `LoadMoreFooterCopy`, `ScreenStateCta`, `ScreenStateDefaults`, `ScreenStateEmpty`, `ScreenStateError`, `ScreenStateLoading`, `ScreenStateNoNetwork`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

13 test file(s) under `core-base/ui/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * A base [ViewModel] that helps enforce the unidirectional data flow pattern and associated
 * responsibilities of a typical ViewModel:
 *
 * - Maintaining and emitting a current state (of type [S]) with the given `initialState`.
 * - Emitting one-shot events as needed (of type [E]). These should be rare and are typically
 *   reserved for things such as non-state based navigation.
 * - Receiving actions (of type [A]) that may induce changes in the current state, trigger an
 *   event emission, or both.
 */
abstract class BaseViewModel<S, E, A>(
    initialState: S,
) : ViewModel() {
    protected val mutableStateFlow: MutableStateFlow<S> = MutableStateFlow(initialState)
    private val eventChannel: Channel<E> = Channel(capacity = Channel.UNLIMITED)
    private val internalActionChannel: Channel<A> = Channel(capacity = Channel.UNLIMITED)

    /**
     * A helper that returns the current state of the view model.
     */
    protected val state: S get() = mutableStateFlow.value

    /**
     * A [StateFlow] representing state updates.
     */
    val stateFlow: StateFlow<S> = mutableStateFlow.asStateFlow()

    /**
     * A [Flow] of one-shot events. These may be received and consumed by only a single consumer.
     * Any additional consumers will receive no events.
     */
    val eventFlow: Flow<E> = eventChannel.receiveAsFlow()

    /**
     * A [SendChannel] for sending actions to the ViewModel for processing.
     */
    val actionChannel: SendChannel<A> = internalActionChannel

    init {
        viewModelScope.launch {
            internalActionChannel
                .consumeAsFlow()
                .collect { action ->
                    handleAction(action)
                }
        }
    // … (excerpt)
```

Source: [`src/commonMain/kotlin/kpt/core/base/ui/viewmodel/BaseViewModel.kt`](../../../../core-base/ui/src/commonMain/kotlin/kpt/core/base/ui/viewmodel/BaseViewModel.kt) — excerpt; read the file for the full implementation.

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
