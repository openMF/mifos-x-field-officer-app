# `core/designsystem`

> **Layer:** `core` — fork-owned implementation, and a **codegen target**.
> **Instruction surface:** `CORE_DESIGNSYSTEM.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 37 Kotlin files (4 test) · source sets: `androidMain`, `commonMain`, `commonTest`, `nonAndroidMain`



## When implementing a feature

**Tier 5** of the codegen chain, written by **`kmp-designsystem-gen`**.

**Does your feature need this module?** Use when /kmp-client (the manager) determines a feature needs brand-owned tokens above the framework-shared core-base/designsystem primitives.

**Where the code goes**

```
core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/<your-domain>/
```

One package per domain — the template's own `ChartTokens.kt` sits at `kpt/core/designsystem/chart/`. Do not flatten
everything into the module root, and do not add to `di/`, `config/` or `migrations/`:
those are infrastructure, not feature surface.

**How it wires**

No annotation contract in this module — follow the DI convention already present in
its `di/` module, and prefer extending an existing seam over adding a new one.

**Next in the chain:** tier 6 (`core/domain`, `core/ui`), then `feature/{f}`.

## Position in the module graph

**Exposes transitively** (`api`) — a consumer of this module also sees these:

- [`core-base/designsystem`](../core-base/designsystem.md)
- [`core/platform`](../core/platform.md)

**Uses internally** (`implementation`) — not visible to consumers:

`core/store`

**Consumed by** 2 module(s): `core/ui`, `feature/rates`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_DESIGNSYSTEM.md`.

## Principal types

- **`BarDatum`** — One bar of [KptBarChart]. [value] is unitless — the chart normalizes each
- **`Candle`** — One open-high-low-close bar for [KptCandlestick]. All values are in the
- **`ChartTokens`** — Shared visual tokens for every chart in `core/designsystem/chart/`.
- **`DonutSlice`** — One slice of [KptDonutChart]. [value] is unitless — the chart normalizes
- **`MoneyTone`** — Money tone — how a monetary amount should be colored regardless of the raw value's sign.
- **`StatusChipIntent`** — Semantic intent of a [StatusChip]. Maps to a (container, content) color pair derived

Undocumented: `AppIcons`, `Elevation`, `FinanceColors`, `NonLetterColorVisualTransformation`, `RateColors`, `RateDirection`, `Spacing`, `Urgency`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

4 test file(s) under `core/designsystem/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Semantic finance color palette — extends Material 3's [androidx.compose.material3.ColorScheme]
 * with money/rate/freshness/urgency tokens specific to financial UIs.
 *
 * Access from any Composable via [MaterialTheme.finance].
 *
 * **Fork override pattern** — to brand the toolkit without forking widgets:
 * ```
 * CompositionLocalProvider(LocalFinanceColors provides myForkFinanceColors()) {
 *     KptTheme { App() }
 * }
 * ```
 *
 * Hex values are chosen to satisfy WCAG AA contrast (≥4.5:1 normal text, ≥3.0:1 large text)
 * against the corresponding light/dark surface tokens in [Color.kt].
 */
@Immutable
data class FinanceColors(
    // ── Money semantic — sign-based ──────────────────────────────────────────
    /** Gain, income, available balance, positive delta. Use for amounts > 0. */
    val moneyPositive: Color,
    /** Loss, expense, owed, negative delta. Use for amounts < 0. */
    val moneyNegative: Color,
    /** Zero balance, no change. Use for amounts == 0. */
    val moneyNeutral: Color,
    /** Tinted background for positive-money containers (e.g. Surface behind a +$120 chip). */
    val moneyPositiveContainer: Color,
    /** Tinted background for negative-money containers. */
    val moneyNegativeContainer: Color,
    /** Tinted background for neutral/zero containers. */
    val moneyNeutralContainer: Color,
    /** Foreground text on [moneyPositiveContainer]. */
    val onMoneyPositive: Color,
    /** Foreground text on [moneyNegativeContainer]. */
    val onMoneyNegative: Color,

    // ── Rate semantic — directional ──────────────────────────────────────────
    /** Rate increased vs previous period. Reuses money palette by default. */
    val rateUp: Color,
    /** Rate decreased vs previous period. */
    val rateDown: Color,
    /** Rate unchanged. */
    val rateFlat: Color,
    val rateUpContainer: Color,
    val rateDownContainer: Color,
    val rateFlatContainer: Color,
    // … (excerpt)
```

Source: [`src/commonMain/kotlin/kpt/core/designsystem/theme/FinanceColors.kt`](../../../../core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/FinanceColors.kt) — excerpt; read the file for the full implementation.

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
