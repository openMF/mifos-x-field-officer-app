# `core-base/designsystem`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_DESIGNSYSTEM.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 36 Kotlin files (1 test) · source sets: `commonMain`, `commonTest`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

No module dependencies — this is a leaf.

**Consumed by** 2 module(s): `core-base/ui`, `core/designsystem`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_DESIGNSYSTEM.md`.

## Principal types

- **`BarGeometry`** — Pure-function math for bar chart composables. Normalizes each bar to a fraction
- **`ContainerColors`** — A collection of container colors and their corresponding content colors.
- **`DonutGeometry`** — Pure-function math for donut chart composables. Extracted from the Composable
- **`KptAnimationSpecs`** — Centralized animation specifications following Material Motion design guidelines.
- **`KptElevationDefaults`** — Predefined elevation configurations for common UI patterns.
- **`KptProgress`** — Sealed family of "something is in progress" UI variants. Callers pick the right
- **`KptSpacingDefaults`** — Predefined spacing combinations for common UI patterns.
- **`MotionSnapshot`** — Last-read snapshot of the active [Motion]. Updated as a side effect whenever any
- **`ProgressSize`** — T-shirt sizes for [KptProgress] variants. Maps to (diameter, stroke) dp pairs via
- **`ProgressSizeSpec`** — Canonical (diameter dp, stroke dp) values for each [ProgressSize]. Internal so
- **`SelectionVisibilityState`** — Describes the current selection state for the list pane within an adaptive layout.
- **`SparklineGeometry`** — Pure-function path geometry for sparkline / area chart composables.
- …and 2 more documented types

Undocumented: `AccessibilityProvider`, `Animatable`, `BreakpointConfiguration`, `Clickable`, `ComponentColors`, `ComponentComposer`, `ComponentConfiguration`, `ComponentConfigurationScope`, `ComponentElevation`, `ComponentFactory`, `ComponentRegistry`, `ComponentRenderer`, `ComponentState`, `ComponentStateHolder` …and 38 more

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

1 test file(s) under `core-base/designsystem/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * KptMaterialTheme provides Material3 integration for KptTheme.
 * This composable applies KptTheme values to MaterialTheme automatically,
 * making all Material3 components use KptTheme design tokens.
 *
 * @param theme KptThemeProvider instance containing design tokens
 * @param content The composable content that will have access to both KptTheme and MaterialTheme
 *
 * @sample KptMaterialThemeUsageExample
 */
@Composable
fun KptMaterialTheme(
    theme: KptThemeProvider = KptThemeProviderImpl(),
    content: @Composable () -> Unit,
) {
    // Convert KptTheme values to Material3 equivalents
    val materialColorScheme = theme.colors.toMaterial3ColorScheme()
    val materialTypography = theme.typography.toMaterial3Typography()
    val materialShapes = theme.shapes.toMaterial3Shapes()

    // Provide both KptTheme composition locals and MaterialTheme
    CompositionLocalProvider(
        LocalKptColors provides theme.colors,
        LocalKptTypography provides theme.typography,
        LocalKptShapes provides theme.shapes,
        LocalKptSpacing provides theme.spacing,
        LocalKptElevation provides theme.elevation,
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = materialTypography,
            shapes = materialShapes,
            content = content,
        )
    }
}
```

Source: [`src/commonMain/kotlin/kpt/core/base/designsystem/KptMaterialTheme.kt`](../../../../core-base/designsystem/src/commonMain/kotlin/kpt/core/base/designsystem/KptMaterialTheme.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
