# `core/designsystem`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DESIGNSYSTEM.md`
> **Measured:** 37 Kotlin files, 4 test files

## Principal types

`AppIcons`, `BarDatum`, `Candle`, `ChartTokens`, `DonutSlice`, `Elevation`, `FinanceColors`, `MoneyTone`, `NonLetterColorVisualTransformation`, `RateColors`, `RateDirection`, `Spacing`, `StatusChipIntent`, `Urgency`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/designsystem sha=891491b55cffe4503b1e0b33f79fcaf6b3d593ee -->
## API reference

_Generated from `core/designsystem` at tree `891491b55cff` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/ChartTokens.kt`

```kotlin
object ChartTokens
```
Shared visual tokens for every chart in `core/designsystem/chart/`. Reads from `MaterialTheme.colorScheme`, `MaterialTheme.typography`, and `MaterialTheme.finance`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptAreaChart.kt:66</code></summary>

```kotlin
        drawPath(
            path = areaPath,
            brush = ChartTokens.areaFillBrush(fillColor),
        )

        // Top line.
        val linePath = Path().apply {
```

</details>

- `fun multiSeriesColors(): List<Color>` — Six distinguishable series hues, in a fixed order so the same series keeps its colour across recompositions and across charts.
- `val finance = MaterialTheme.finance`
- `fun axisLabelStyle(): TextStyle = MaterialTheme.typography.bodySmall.copy(` — Text style for axis labels.
- `fun gridlineColor(): Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)` — Gridline colour — outline-variant at half alpha, so gridlines sit behind the data rather than competing with it.
- `fun areaFillBrush(strokeColor: Color): Brush = Brush.verticalGradient(` — Vertical gradient from `strokeColor` at 24% alpha down to transparent, for the fill under a line series.
- `val defaultStrokeWidth = 1.5.dp` — Line width for a data series.
- `val defaultAxisStrokeWidth = 1.0.dp` — Line width for an axis — deliberately thinner than `defaultStrokeWidth`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptAreaChart.kt`

```kotlin
fun KptAreaChart(
```
Filled area chart for the detail-screen Hero. Same geometry contract as `KptSparkline` but adds a gradient fill under the line via `ChartTokens.areaFillBrush`.

<details><summary>Used in the template — <code>feature/rates/src/commonMain/kotlin/kpt/feature/rates/ui/InterestRateDetailScreen.kt:182</code></summary>

```kotlin
    AppCard {
        Box(modifier = Modifier.fillMaxWidth().height(220.dp).padding(sp.sm)) {
            KptAreaChart(
                values = series.observations.map { it.value },
                modifier = Modifier.fillMaxSize(),
            )
        }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptBarChart.kt`

```kotlin
data class BarDatum(
```
One bar of `KptBarChart`. `value` is unitless — the chart normalizes each bar to a fraction of the running max.

```kotlin
fun KptBarChart(
```
Vertical bar chart with rounded tops and optional axis labels. Practical input cap: ~24 bars (2 years monthly). For longer series, caller decimates or buckets. Empty input → renders nothing.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptCandlestick.kt`

```kotlin
data class Candle(
```
One open-high-low-close bar for `KptCandlestick`. All values are in the same unit (price, rate, etc.) and rendered against a shared y-axis covering the series' min/max.

<details><summary>Used in the template — <code>core/designsystem/src/commonTest/kotlin/kpt/core/designsystem/chart/KptCandlestickTest.kt:25</code></summary>

```kotlin
    @Test
    fun closeAboveOpenIsUp() {
        val candle = Candle(open = 100f, high = 110f, low = 95f, close = 108f)
        assertTrue(candle.isUp)
    }

    @Test
```

</details>

```kotlin
fun KptCandlestick(
```
Classic OHLC candlestick chart for price-like time series. Up candles (close ≥ open) use `upColor`; down candles use `downColor`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptDonutChart.kt`

```kotlin
data class DonutSlice(
```
One slice of `KptDonutChart`. `value` is unitless — the chart normalizes each slice to a fraction of the sum.

```kotlin
fun KptDonutChart(
```
Canvas-based donut chart. Renders concentric arcs around a hollow center, optionally hosting a centered Composable (e.g. summary number). Practical input cap: any size (rendering cost is per-slice, fixed).

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/chart/KptSparkline.kt`

```kotlin
fun KptSparkline(
```
Minimal Canvas-based sparkline — a one-pass connected polyline whose y-axis is normalized to the series's own `[min, max]` range. No external chart dependency.

<details><summary>Used in the template — <code>feature/rates/src/commonMain/kotlin/kpt/feature/rates/ui/InterestRatesScreen.kt:231</code></summary>

```kotlin
        }

        KptSparkline(
            values = series.observations.map { it.value },
            modifier = Modifier.weight(1f).fillMaxSize(),
        )
    }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/AmountDisplay.kt`

```kotlin
fun AmountDisplay(
```
Big-and-bold currency presentation used at the top of dashboards and detail screens. Layout: optional label (small, dimmed) → large amount → optional supporting metadata row.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/BillRemindersListScreen.kt:164</code></summary>

```kotlin
internal fun UpcomingSummaryHero(totalAmount: Double, upcomingCount: Int) {
    HeroCard {
        AmountDisplay(
            amountText = formatCurrency(totalAmount),
            label = stringResource(Res.string.screens_bills_list_summary_label),
            supporting = {
                Text(
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/MoneyText.kt`

```kotlin
fun MoneyText(
```
Currency text that picks its color from `MaterialTheme.finance` based on the amount's sign (or a forced `MoneyTone`). Use everywhere a monetary value is rendered so the app has a single visual grammar for money.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/BillRemindersListScreen.kt:222</code></summary>

```kotlin
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            MoneyText(
                text = formatCurrency(bill.amount),
                tone = MoneyTone.Negative,
                style = MaterialTheme.typography.titleMedium,
            )
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/MoneyTone.kt`

```kotlin
enum class MoneyTone
```
Money tone — how a monetary amount should be colored regardless of the raw value's sign.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/BillRemindersListScreen.kt:224</code></summary>

```kotlin
            MoneyText(
                text = formatCurrency(bill.amount),
                tone = MoneyTone.Negative,
                style = MaterialTheme.typography.titleMedium,
            )
        }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/RateBadge.kt`

```kotlin
fun RateBadge(delta: String, direction: RateDirection, modifier: Modifier = Modifier)
```
Compact rate-change indicator — directional icon + percentage / delta text, both colored from `MaterialTheme.finance` (rateUp / rateDown / rateFlat). Renders inside a tinted container so it reads as a single visual unit.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:721</code></summary>

```kotlin
        )
        Spacer(Modifier.size(MaterialTheme.spacing.sm))
        RateBadge(delta = delta, direction = direction)
    }
}

@Composable
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/RateDirection.kt`

```kotlin
enum class RateDirection
```
Direction of a rate / price / metric change relative to the prior period.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:564</code></summary>

```kotlin
                        rates.fedFundsPercent.formatDecimal(2),
                    ),
                    direction = RateDirection.Flat,
                    delta = stringResource(Res.string.screens_home_rates_delta_flat),
                )
                RateRow(
                    label = stringResource(Res.string.screens_home_rates_mortgage_30y_label),
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/SectionHeader.kt`

```kotlin
fun SectionHeader(
```
Section header — title (+ optional supporting text) + optional trailing action button. Use to break dashboards into scannable groups (Loans, Bills, Rates, Currencies).

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/demo/HomeDashboard.kt:200</code></summary>

```kotlin

        // ── Quick stats grid (Bills + Rates) ─────────────────────────────
        SectionHeader(title = stringResource(Res.string.screens_home_section_this_week))

        BillsQuickCard(
            state = state.bills,
            onSeeAll = onNavigateToBills,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/StatusChip.kt`

```kotlin
fun StatusChip(text: String, intent: StatusChipIntent, modifier: Modifier = Modifier)
```
Compact colored pill used to convey state at a glance — bill status, loan stage, rate direction, sync state. Stays one line; no icons (use `UrgencyDot` when you want a leading accent).

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/BillRemindersListScreen.kt:238</code></summary>

```kotlin
            horizontalArrangement = Arrangement.spacedBy(sp.sm),
        ) {
            StatusChip(text = shortDueLabel(diff), intent = statusIntent)
            StatusChip(
                text = bill.category.name.lowercase().replaceFirstChar { it.uppercase() },
                intent = StatusChipIntent.Neutral,
            )
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/StatusChipIntent.kt`

```kotlin
enum class StatusChipIntent
```
Semantic intent of a `StatusChip`. Maps to a (container, content) color pair derived from the active Material color scheme + finance palette.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/BillRemindersListScreen.kt:192</code></summary>

```kotlin
    }
    val statusIntent = when {
        diff < 0 -> StatusChipIntent.Danger
        diff <= 1 -> StatusChipIntent.Warning
        diff <= 7 -> StatusChipIntent.Info
        else -> StatusChipIntent.Neutral
    }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/Urgency.kt`

```kotlin
enum class Urgency
```
Due-date urgency tier — informs the color of a leading dot on a list row.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/BillRemindersListScreen.kt:186</code></summary>

```kotlin
    val diff = bill.dueDay - today
    val urgency = when {
        diff < 0 -> Urgency.Overdue
        diff == 0 -> Urgency.Today
        diff <= 7 -> Urgency.Upcoming
        else -> Urgency.Distant
    }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/UrgencyDot.kt`

```kotlin
fun UrgencyDot(urgency: Urgency, modifier: Modifier = Modifier, size: Dp = 10.dp)
```
Solid colored dot used as the leading accent on a list row (bill reminder, loan due, task). Pairs cheaply with any list-item layout to encode urgency at a glance without stealing focus from the row's text content.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/BillRemindersListScreen.kt:214</code></summary>

```kotlin
            horizontalArrangement = Arrangement.spacedBy(sp.md),
        ) {
            UrgencyDot(urgency = urgency)
            Text(
                text = bill.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardLoadingSkeleton.kt`

```kotlin
fun CardLoadingSkeleton(modifier: Modifier = Modifier)
```
Whole-card shimmer placeholder. Drop in for `AppCard` / `Card` while the underlying data loads. Renders 3 stacked shimmer bars to suggest a typical card layout (title + 2 body lines).

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:128</code></summary>

```kotlin

            // ── CardLoadingSkeleton ─────────────────────────────────────
            SectionHeader("CardLoadingSkeleton")
            CardLoadingSkeleton()

            // ── CardStateBox ────────────────────────────────────────────
            SectionHeader("CardStateBox — every variant")
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardStateBox.kt`

```kotlin
fun <T> CardStateBox(
```
State-aware Card wrapper. Renders the right component-scale UI for each `ScreenState` variant *inside* the Card bounds — no full-screen overlay. Use for individual cards on a dashboard, where each card resolves its own data independently.

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:132</code></summary>

```kotlin

            // ── CardStateBox ────────────────────────────────────────────
            SectionHeader("CardStateBox — every variant")
            Text(
                stringResource(Res.string.screens_showcase_state_gallery_loading),
                style = MaterialTheme.typography.labelMedium,
            )
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/ErrorChip.kt`

```kotlin
fun ErrorChip(message: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null)
```
M3 AssistChip styled for error reporting. Slightly more prominent than `InlineErrorPill` — adds an icon and uses chip semantics (clickable by default if `onClick` is non-null).

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:103</code></summary>

```kotlin
            // interactive rather than a dead placeholder.
            var errorChipTaps by remember { mutableIntStateOf(0) }
            LabelRow("ErrorChip") {
                ErrorChip(message = "Failed to load", onClick = { errorChipTaps++ })
                if (errorChipTaps > 0) {
                    Text(
                        text = stringResource(
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/InlineErrorPill.kt`

```kotlin
fun InlineErrorPill(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null)
```
Pill-shaped inline error chip — for component-scale failures (a single row failing in an otherwise-loaded list, a stale field in a form, a card-local fetch failure).

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:91</code></summary>

```kotlin

            // ── InlineErrorPill ─────────────────────────────────────────
            LabelRow("InlineErrorPill (no retry)") {
                InlineErrorPill(message = "Couldn't refresh balance")
            }
            LabelRow("InlineErrorPill (with retry)") {
                InlineErrorPill(message = "Network error", onRetry = {})
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/RowLoadingShimmer.kt`

```kotlin
fun RowLoadingShimmer(modifier: Modifier = Modifier)
```
Single-row shimmer placeholder. Use inside LazyColumn `items()` while paged data loads, or as a one-off row placeholder for a single record waiting on a network response. Layout: circle avatar + 2 stacked text bars (title + subtitle).

<details><summary>Used in the template — <code>feature/showcase/src/commonMain/kotlin/kpt/feature/showcase/stategallery/StateGalleryScreen.kt:117</code></summary>

```kotlin

            // ── RowLoadingShimmer ───────────────────────────────────────
            SectionHeader("RowLoadingShimmer")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    repeat(3) {
                        RowLoadingShimmer()
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/AppIcons.kt`

```kotlin
object AppIcons
```
The app's icon set, named by ROLE rather than by glyph.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/demo/ProfileDemoBody.kt:84</code></summary>

```kotlin
            ) {
                Icon(
                    imageVector = AppIcons.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                )
                Text(
```

</details>

- `val Language: ImageVector = Icons.Default.ArrowOutward` — `language` role — Material default `ArrowOutward`.
- `val CheckCircle: ImageVector = Icons.Filled.CheckCircle` — `check circle` role — Material filled `CheckCircle`.
- `val OutlinedInfo = Icons.Outlined.Info` — `outlined info` role — Material outlined `Info`.
- `val OutlinedLock = Icons.Outlined.Lock` — `outlined lock` role — Material outlined `Lock`.
- `val OutlinedNotifications = Icons.Outlined.Notifications` — `outlined notifications` role — Material outlined `Notifications`.
- `val ChevronRight: ImageVector = Icons.Filled.ChevronRight` — `chevron right` role — Material filled `ChevronRight`.
- `val QrCode: ImageVector = Icons.Filled.QrCode` — `qr code` role — Material filled `QrCode`.
- `val Close: ImageVector = Icons.Filled.Close` — `close` role — Material filled `Close`.
- `val AttachMoney: ImageVector = Icons.Filled.AttachMoney` — `attach money` role — Material filled `AttachMoney`.
- `val OutlinedVisibilityOff: ImageVector = Icons.Outlined.VisibilityOff` — `outlined visibility off` role — Material outlined `VisibilityOff`.
- `val OutlinedVisibility: ImageVector = Icons.Outlined.Visibility` — `outlined visibility` role — Material outlined `Visibility`.
- `val VisibilityOff: ImageVector = Icons.Filled.VisibilityOff` — `visibility off` role — Material filled `VisibilityOff`.
- `val Visibility: ImageVector = Icons.Filled.Visibility` — `visibility` role — Material filled `Visibility`.
- `val Check: ImageVector = Icons.Default.Check` — `check` role — Material default `Check`.
  _…more members; read the file._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Color.kt`

```kotlin
val primaryLight = Color(0xFF4338CA)
```
Material `primary` role, light scheme — Trust Indigo. The app's main brand role — key actions and selected states.

```kotlin
val onPrimaryLight = Color(0xFFFFFFFF)
```
Content colour on `primaryLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val primaryContainerLight = Color(0xFFE0E7FF)
```
Material `primary container` role, light scheme — Trust Indigo. The app's main brand role — key actions and selected states.

```kotlin
val onPrimaryContainerLight = Color(0xFF1F1D75)
```
Content colour on `primaryContainerLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val secondaryLight = Color(0xFF059669)
```
Material `secondary` role, light scheme — Emerald. Supporting role — positive deltas and secondary CTAs.

```kotlin
val onSecondaryLight = Color(0xFFFFFFFF)
```
Content colour on `secondaryLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val secondaryContainerLight = Color(0xFFD1FAE5)
```
Material `secondary container` role, light scheme — Emerald. Supporting role — positive deltas and secondary CTAs.

```kotlin
val onSecondaryContainerLight = Color(0xFF064E3B)
```
Content colour on `secondaryContainerLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val tertiaryLight = Color(0xFFD97706)
```
Material `tertiary` role, light scheme — Warm Amber. Accent role — highlights and badges; NOT an error signal.

```kotlin
val onTertiaryLight = Color(0xFFFFFFFF)
```
Content colour on `tertiaryLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val tertiaryContainerLight = Color(0xFFFEF3C7)
```
Material `tertiary container` role, light scheme — Warm Amber. Accent role — highlights and badges; NOT an error signal.

```kotlin
val onTertiaryContainerLight = Color(0xFF78350F)
```
Content colour on `tertiaryContainerLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val errorLight = Color(0xFFF87171)
```
Material `error` role, light scheme — warm red-orange. Failure role — destructive actions and validation failures.

```kotlin
val onErrorLight = Color(0xFFFFFFFF)
```
Content colour on `errorLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val errorContainerLight = Color(0xFFFFE4E1)
```
Material `error container` role, light scheme — warm red-orange. Failure role — destructive actions and validation failures.

```kotlin
val onErrorContainerLight = Color(0xFF9F1239)
```
Content colour on `errorContainerLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val backgroundLight = Color(0xFFFAFAFB)
```
Material `background` role, light scheme. The window behind all content.

```kotlin
val onBackgroundLight = Color(0xFF0F172A)
```
Content colour on `backgroundLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val surfaceLight = Color(0xFFFAFAFB)
```
Material `surface` role, light scheme. The base sheet components sit on.

```kotlin
val onSurfaceLight = Color(0xFF0F172A)
```
Content colour on `surfaceLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val surfaceVariantLight = Color(0xFFE2E8F0)
```
Material `surface variant` role, light scheme. The base sheet components sit on.

```kotlin
val onSurfaceVariantLight = Color(0xFF475569)
```
Content colour on `surfaceVariantLight` (light scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val outlineLight = Color(0xFF94A3B8)
```
Material `outline` role, light scheme. Borders and dividers.

```kotlin
val outlineVariantLight = Color(0xFFCBD5E1)
```
Material `outline variant` role, light scheme. Borders and dividers.

```kotlin
val scrimLight = Color(0xFF000000)
```
Material `scrim` role, light scheme. The dim behind a modal.

```kotlin
val inverseSurfaceLight = Color(0xFF1E293B)
```
Material `inverse surface` role, light scheme. Inverted pairing, for snackbars and tooltips over content.

```kotlin
val inverseOnSurfaceLight = Color(0xFFF1F5F9)
```
Material `inverse on surface` role, light scheme. Inverted pairing, for snackbars and tooltips over content.

```kotlin
val inversePrimaryLight = Color(0xFFA5B4FC)
```
Material `inverse primary` role, light scheme. Inverted pairing, for snackbars and tooltips over content.

```kotlin
val surfaceDimLight = Color(0xFFE2E8F0)
```
Material `surface dim` role, light scheme. The base sheet components sit on.

```kotlin
val surfaceBrightLight = Color(0xFFFAFAFB)
```
Material `surface bright` role, light scheme. The base sheet components sit on.

```kotlin
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
```
Material `surface container lowest` role, light scheme. The base sheet components sit on.

```kotlin
val surfaceContainerLowLight = Color(0xFFF8FAFC)
```
Material `surface container low` role, light scheme. The base sheet components sit on.

```kotlin
val surfaceContainerLight = Color(0xFFF1F5F9)
```
Material `surface container` role, light scheme. The base sheet components sit on.

```kotlin
val surfaceContainerHighLight = Color(0xFFE2E8F0)
```
Material `surface container high` role, light scheme. The base sheet components sit on.

```kotlin
val surfaceContainerHighestLight = Color(0xFFCBD5E1)
```
Material `surface container highest` role, light scheme. The base sheet components sit on.

```kotlin
val primaryDark = Color(0xFFA5B4FC)
```
Material `primary` role, dark scheme — Trust Indigo. The app's main brand role — key actions and selected states.

```kotlin
val onPrimaryDark = Color(0xFF1F1D75)
```
Content colour on `primaryDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val primaryContainerDark = Color(0xFF3730A3)
```
Material `primary container` role, dark scheme — Trust Indigo. The app's main brand role — key actions and selected states.

```kotlin
val onPrimaryContainerDark = Color(0xFFE0E7FF)
```
Content colour on `primaryContainerDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val secondaryDark = Color(0xFF6EE7B7)
```
Material `secondary` role, dark scheme — Emerald. Supporting role — positive deltas and secondary CTAs.

```kotlin
val onSecondaryDark = Color(0xFF064E3B)
```
Content colour on `secondaryDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val secondaryContainerDark = Color(0xFF065F46)
```
Material `secondary container` role, dark scheme — Emerald. Supporting role — positive deltas and secondary CTAs.

```kotlin
val onSecondaryContainerDark = Color(0xFFD1FAE5)
```
Content colour on `secondaryContainerDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val tertiaryDark = Color(0xFFFCD34D)
```
Material `tertiary` role, dark scheme — Warm Amber. Accent role — highlights and badges; NOT an error signal.

```kotlin
val onTertiaryDark = Color(0xFF78350F)
```
Content colour on `tertiaryDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val tertiaryContainerDark = Color(0xFF92400E)
```
Material `tertiary container` role, dark scheme — Warm Amber. Accent role — highlights and badges; NOT an error signal.

```kotlin
val onTertiaryContainerDark = Color(0xFFFEF3C7)
```
Content colour on `tertiaryContainerDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val errorDark = Color(0xFFFDBA74)
```
Material `error` role, dark scheme — warm red-orange. Failure role — destructive actions and validation failures.

```kotlin
val onErrorDark = Color(0xFF7C2D12)
```
Content colour on `errorDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val errorContainerDark = Color(0xFFC2410C)
```
Material `error container` role, dark scheme — warm red-orange. Failure role — destructive actions and validation failures.

```kotlin
val onErrorContainerDark = Color(0xFFFFEDD5)
```
Content colour on `errorContainerDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val backgroundDark = Color(0xFF0F172A)
```
Material `background` role, dark scheme. The window behind all content.

```kotlin
val onBackgroundDark = Color(0xFFF1F5F9)
```
Content colour on `backgroundDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val surfaceDark = Color(0xFF0F172A)
```
Material `surface` role, dark scheme. The base sheet components sit on.

```kotlin
val onSurfaceDark = Color(0xFFF1F5F9)
```
Content colour on `surfaceDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val surfaceVariantDark = Color(0xFF1E293B)
```
Material `surface variant` role, dark scheme. The base sheet components sit on.

```kotlin
val onSurfaceVariantDark = Color(0xFFCBD5E1)
```
Content colour on `surfaceVariantDark` (dark scheme) — text and icons drawn over it. Contrast-paired; do not substitute another colour here.

```kotlin
val outlineDark = Color(0xFF64748B)
```
Material `outline` role, dark scheme. Borders and dividers.

```kotlin
val outlineVariantDark = Color(0xFF334155)
```
Material `outline variant` role, dark scheme. Borders and dividers.

```kotlin
val scrimDark = Color(0xFF000000)
```
Material `scrim` role, dark scheme. The dim behind a modal.

```kotlin
val inverseSurfaceDark = Color(0xFFF1F5F9)
```
Material `inverse surface` role, dark scheme. Inverted pairing, for snackbars and tooltips over content.

```kotlin
val inverseOnSurfaceDark = Color(0xFF1E293B)
```
Material `inverse on surface` role, dark scheme. Inverted pairing, for snackbars and tooltips over content.

```kotlin
val inversePrimaryDark = Color(0xFF4338CA)
```
Material `inverse primary` role, dark scheme. Inverted pairing, for snackbars and tooltips over content.

```kotlin
val surfaceDimDark = Color(0xFF0F172A)
```
Material `surface dim` role, dark scheme. The base sheet components sit on.

```kotlin
val surfaceBrightDark = Color(0xFF374558)
```
Material `surface bright` role, dark scheme. The base sheet components sit on.

```kotlin
val surfaceContainerLowestDark = Color(0xFF020617)
```
Material `surface container lowest` role, dark scheme. The base sheet components sit on.

```kotlin
val surfaceContainerLowDark = Color(0xFF1E293B)
```
Material `surface container low` role, dark scheme. The base sheet components sit on.

```kotlin
val surfaceContainerDark = Color(0xFF243044)
```
Material `surface container` role, dark scheme. The base sheet components sit on.

```kotlin
val surfaceContainerHighDark = Color(0xFF2D3B52)
```
Material `surface container high` role, dark scheme. The base sheet components sit on.

```kotlin
val surfaceContainerHighestDark = Color(0xFF374558)
```
Material `surface container highest` role, dark scheme. The base sheet components sit on.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Elevation.kt`

```kotlin
data class Elevation(
```
Shared elevation tier scale — five named tiers matching Material 3 elevation guidance. Access from any Composable via `MaterialTheme.elevation`.

<details><summary>Used in the template — <code>core/designsystem/src/androidMain/kotlin/kpt/core/designsystem/theme/FinanceTokenPreview.kt:58</code></summary>

```kotlin
}

@Preview(name = "Elevation tiers", showBackground = true, widthDp = 360)
@Composable
private fun ElevationTiersPreview() {
    KptTheme(darkTheme = false) {
        Surface { ElevationSwatches() }
```

</details>

```kotlin
val LocalElevation = staticCompositionLocalOf { Elevation() }
```
CompositionLocal carrying the app's elevation scale. `static` because it changes only with the theme, so a read does not subscribe.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:159</code></summary>

```kotlin
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
            content()
        }
```

</details>

```kotlin
val MaterialTheme.elevation: Elevation
```
Resolve the active `Elevation` tier scale from composition.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/FinanceColors.kt`

```kotlin
data class FinanceColors(
```
Semantic finance color palette — extends Material 3's `androidx.compose.material3.ColorScheme` with money/rate/freshness/urgency tokens specific to financial UIs. Access from any Composable via `MaterialTheme.finance`.

<details><summary>Example</summary>

```kotlin
CompositionLocalProvider(LocalFinanceColors provides myForkFinanceColors()) {
    KptTheme { App() }
}
```

</details>

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:142</code></summary>

```kotlin

    val screenStateDefaults = appScreenStateDefaults()
    val financeColors = if (darkTheme) darkFinanceColors() else lightFinanceColors()

    KptMaterialTheme(theme = themeProvider) {
        // Provide the design-system token CompositionLocals app-wide so every widget
        // built on `core/designsystem/component/`, `chart/`, and `motion/` resolves
```

</details>

```kotlin
fun lightFinanceColors(): FinanceColors = FinanceColors(
```
Light-theme finance palette. Contrast-verified against `surfaceLight` / `surfaceContainerLight` etc.

```kotlin
fun darkFinanceColors(): FinanceColors = FinanceColors(
```
Dark-theme finance palette. Contrast-verified against `surfaceDark` / `surfaceContainerDark`. Mirrors light values but brightened for legibility on the deep navy `#13131B` background.

```kotlin
val LocalFinanceColors = staticCompositionLocalOf<FinanceColors>
```
CompositionLocal for the active `FinanceColors`. Provided by `KptTheme`. Direct access discouraged — use `MaterialTheme.finance` extension instead.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:156</code></summary>

```kotlin
        // core/store/AppScreenStateDefaults.kt — that's the single fork seam.
        CompositionLocalProvider(
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
```

</details>

```kotlin
val MaterialTheme.finance: FinanceColors
```
Resolve the active `FinanceColors` from composition.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt`

```kotlin
val lightScheme = lightColorScheme(
```
The assembled Material 3 light scheme, wired from the palette tokens in `Color.kt`.

```kotlin
val darkScheme = darkColorScheme(
```
The assembled Material 3 dark scheme, wired from the palette tokens in `Color.kt`.

```kotlin
fun KptTheme(
```
The main theme composable for the application. This composable uses KptMaterialTheme under the hood to provide seamless integration between KptTheme design tokens and Material3 theming system.

<details><summary>Used in the template — <code>feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/ui/AlertCreateScreenPreview.kt:39</code></summary>

```kotlin
    hasResumableDraft: Boolean = false,
) {
    KptTheme {
        AlertCreateScreenContent(
            form = form,
            screenState = ScreenState.Content(previewAlert()),
            submitState = submitState,
```

</details>

```kotlin
expect fun platformColorScheme(useDarkTheme: Boolean, dynamicColor: Boolean): ColorScheme
```
Resolves the active colour scheme per platform. `dynamicColor` is honoured only where the OS supplies one (Android 12+); every other target falls back to `lightScheme`/`darkScheme`, so a caller can request it unconditionally.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Spacing.kt`

```kotlin
data class Spacing(
```
Shared spacing scale — replaces raw `.dp` literals scattered across features with a disciplined 4 / 8 / 12 / 16 / 24 / 32 / 48 progression. Access from any Composable via `MaterialTheme.spacing`.

<details><summary>Used in the template — <code>core/designsystem/src/androidMain/kotlin/kpt/core/designsystem/theme/FinanceTokenPreview.kt:50</code></summary>

```kotlin
}

@Preview(name = "Spacing scale", showBackground = true, widthDp = 360)
@Composable
private fun SpacingScalePreview() {
    KptTheme(darkTheme = false) {
        Surface { SpacingSwatches() }
```

</details>

```kotlin
val LocalSpacing = staticCompositionLocalOf { Spacing() }
```
CompositionLocal carrying the app's spacing scale. Read this instead of writing literal `.dp`: the whole app's rhythm is retuned here, and a hardcoded value silently opts a screen out of that.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:158</code></summary>

```kotlin
            LocalFinanceColors provides financeColors,
            LocalMotion provides Motion(),
            LocalSpacing provides Spacing(),
            LocalElevation provides Elevation(),
            LocalScreenStateDefaults provides screenStateDefaults,
        ) {
            content()
```

</details>

```kotlin
val MaterialTheme.spacing: Spacing
```
Resolve the active `Spacing` scale from composition.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Type.kt`

```kotlin
val fontFamily: FontFamily
```
The app's font family, applied across the type scale. A fork overrides this to brand its typography without restating every style.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/utils/ModifierExt.kt`

```kotlin
fun Modifier.mirrorIfRtl() = composed
```
Horizontally mirrors content when the layout direction is RTL. For glyphs with a direction (a back chevron, a progress arrow). NOT for text or logos, which must not be flipped.

```kotlin
fun Modifier.tabNavigation() = composed
```
Makes the element reachable and actionable by keyboard tab — desktop and web, where a tap target alone leaves it unusable.

```kotlin
fun Modifier.onClick(
```
Click handling with the app's ripple and accessibility semantics already attached, so a clickable element is announced correctly without each call site remembering to say so.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/utils/NonLetterColorVisualTransformation.kt`

```kotlin
fun nonLetterColorVisualTransformation(): VisualTransformation
```
Tints digits and symbols differently from letters in a text field — used for passwords and codes, where character class is hard to read at a glance. Presentation only: it never alters the field's value.

---

_12 type(s), 124 function(s)/property(ies); 135 carry KDoc at source; 1 authored example(s); 27 live call site(s)._
<!-- api-docs:end -->
