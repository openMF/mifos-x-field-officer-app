# `core/ui`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_UI.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 14 Kotlin files (0 test) · source sets: `androidMain`, `commonMain`



## When implementing a feature

**Tier 6** of the codegen chain, written by **`kmp-ui-gen`**.

**Does your feature need this module?** Use when /kmp-client (the manager) needs a cross-feature UI primitive that is NOT feature-specific and NOT a design token.

**Where the code goes**

```
core/ui/src/commonMain/kotlin/kpt/core/ui/<your-domain>/
```

One package per domain — the template's own `PasswordStrengthIndicator.kt` sits at `kpt/core/ui/input/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

No annotation contract in this module — follow the DI convention already present in
its `di/` module, and prefer extending an existing seam over adding a new one.

**Next in the chain:** `feature/{f}` — the Screen + ViewModel.

## Position in the module graph

**Exposes transitively** (`api`) — a consumer of this module also sees these:

- [`core-base/ui`](../core-base/ui.md)

**Uses internally** (`implementation`) — not visible to consumers:

`core-base/store`, `core/common`, `core/designsystem`, `core/firebase`, `core/model`

**Consumed by** 10 module(s): `feature/add-to-watchlist`, `feature/alerts`, `feature/amortization`, `feature/bills`, `feature/crypto`, `feature/home`, `feature/loans`, `feature/macro`, `feature/settings`, `feature/watchlist`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_UI.md`.

## Principal types

- **`KptPullToRefreshState`** — Data class representing the pull-to-refresh state and behavior.
- **`NavigationItem`** — Represents a user-interactable item to navigate a user via the bottom app bar or navigation rail.
- **`RevealValue`** — Possible values of [RevealState].

Undocumented: `FloatingActionButtonContent`, `PasswordChecker`, `PasswordStrength`, `PasswordStrengthResult`, `PasswordStrengthState`, `RevealDirection`, `RevealState`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

No tests in this module. If you add behaviour here, add the test alongside it —
`CORE_TESTING.md` carries the shared idiom.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Represents a user-interactable item to navigate a user via the bottom app bar or navigation rail.
 */
interface NavigationItem {
    /**
     * The resource ID for the icon representing the tab when it is selected.
     */
    val selectedIcon: ImageVector

    /**
     * Resource id for the icon representing the tab.
     */
    val icon: ImageVector

    /**
     * Resource id for the label describing the tab.
     */
    val labelRes: StringResource

    /**
     * Resource id for the content description describing the tab.
     */
    val contentDescriptionRes: StringResource

    /**
     * Route of the tab's graph.
     */
    val graphRoute: String

    /**
     * Route of the tab's start destination.
     */
    val startDestinationRoute: String

    /**
     * The test tag of the tab.
     */
    val testTag: String

    /**
     * Whether this tab renders INLINE inside the navbar scaffold — its content swaps within the inner
     * NavHost so the bottom bar stays visible and the tab keeps its own back stack (exactly like the
     * backbone Home/Profile tabs) — versus a FULL-SCREEN destination pushed on the outer authenticated
     * graph (the bottom bar is hidden; e.g. an immersive focus-timer that declares
     * `bottom_navigation_visible: false`). Default `true` (inline). A full-screen tab overrides to `false`.
     *
    // … (excerpt)
```

Source: [`src/commonMain/kotlin/kpt/core/ui/navigation/NavigationItem.kt`](../../../../core/ui/src/commonMain/kotlin/kpt/core/ui/navigation/NavigationItem.kt) — excerpt; read the file for the full implementation.

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
