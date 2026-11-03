# `core/designsystem`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DESIGNSYSTEM.md`
> **Measured:** 67 Kotlin files, 4 test files

## Principal types

`AccentColors`, `AppButtonDefaults`, `AppColors`, `AppElevation`, `AppIcons`, `AppPadding`, `AppPasswordToggle`, `AppPullToRefreshState`, `AppShapes`, `AppSizes`, `AppSpacing`, `AppStrokes`, `AppTextFieldConfig`, `AppTypography`  …and 19 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/designsystem sha=3830a6625b423297d32899d00cb9a0067c3bd7f2 -->
## API reference

_Generated from `core/designsystem` at tree `3830a6625b42` by `scripts/docs/api-docs-gen.sh`._
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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/AmountDisplay.kt`

```kotlin
fun AmountDisplay(
```
Big-and-bold currency presentation used at the top of dashboards and detail screens. Layout: optional label (small, dimmed) → large amount → optional supporting metadata row.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/MoneyText.kt`

```kotlin
fun MoneyText(
```
Currency text that picks its color from `MaterialTheme.finance` based on the amount's sign (or a forced `MoneyTone`). Use everywhere a monetary value is rendered so the app has a single visual grammar for money.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/LoanQueue.kt:188</code></summary>

```kotlin
            )
        }
        MoneyText(
            CurrencyFormatter.format(loan.principal, loan.currencyCode, 2),
            tone = MoneyTone.Neutral,
            style = KptTheme.typography.titleSmall,
        )
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/MoneyTone.kt`

```kotlin
enum class MoneyTone
```
Money tone — how a monetary amount should be colored regardless of the raw value's sign.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/LoanQueue.kt:190</code></summary>

```kotlin
        MoneyText(
            CurrencyFormatter.format(loan.principal, loan.currencyCode, 2),
            tone = MoneyTone.Neutral,
            style = KptTheme.typography.titleSmall,
        )
    }
}
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/RateBadge.kt`

```kotlin
fun RateBadge(delta: String, direction: RateDirection, modifier: Modifier = Modifier)
```
Compact rate-change indicator — directional icon + percentage / delta text, both colored from `MaterialTheme.finance` (rateUp / rateDown / rateFlat). Renders inside a tinted container so it reads as a single visual unit.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/RateDirection.kt`

```kotlin
enum class RateDirection
```
Direction of a rate / price / metric change relative to the prior period.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/RateBadge.kt:44</code></summary>

```kotlin
 */
@Composable
fun RateBadge(delta: String, direction: RateDirection, modifier: Modifier = Modifier) {
    val (container, content, icon) = resolveRateColors(direction)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/SectionHeader.kt`

```kotlin
fun SectionHeader(
```
Section header — title (+ optional supporting text) + optional trailing action button. Use to break dashboards into scannable groups (Loans, Bills, Rates, Currencies).

<details><summary>Used in the template — <code>feature/notification/src/commonMain/kotlin/kpt/feature/notification/ui/NotificationsScreen.kt:152</code></summary>

```kotlin
    ) {
        if (fresh.isNotEmpty()) {
            item(key = "new") { SectionHeader(Res.string.feature_notification_section_new) }
            items(fresh, key = { it.id }) { NotificationRow(it, onOpen) }
        }
        if (earlier.isNotEmpty()) {
            item(key = "earlier") { SectionHeader(Res.string.feature_notification_section_earlier) }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/StatusChip.kt`

```kotlin
fun StatusChip(text: String, intent: StatusChipIntent, modifier: Modifier = Modifier)
```
Compact colored pill used to convey state at a glance — bill status, loan stage, rate direction, sync state. Stays one line; no icons (use `UrgencyDot` when you want a leading accent).

<details><summary>Used in the template — <code>feature/offlinesync/src/commonMain/kotlin/kpt/feature/offlinesync/ui/OfflineSyncPicker.kt:230</code></summary>

```kotlin
        }
        if (onDevice) {
            StatusChip(
                AppIcons.RightTick,
                stringResource(Res.string.feature_offlinesync_on_device),
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/StatusChipIntent.kt`

```kotlin
enum class StatusChipIntent
```
Semantic intent of a `StatusChip`. Maps to a (container, content) color pair derived from the active Material color scheme + finance palette.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/StatusChip.kt:33</code></summary>

```kotlin
 */
@Composable
fun StatusChip(text: String, intent: StatusChipIntent, modifier: Modifier = Modifier) {
    val (container, content) = resolveChipColors(intent)
    Text(
        text = text,
        modifier = modifier
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/Urgency.kt`

```kotlin
enum class Urgency
```
Due-date urgency tier — informs the color of a leading dot on a list row.

<details><summary>Used in the template — <code>core/designsystem/src/androidMain/kotlin/kpt/core/designsystem/theme/FinanceTokenPreview.kt:86</code></summary>

```kotlin
        SwatchRow("freshnessUpdating", f.freshnessUpdating)
        SwatchRow("freshnessOffline", f.freshnessOffline)
        SectionLabel("Urgency")
        SwatchRow("urgencyOverdue", f.urgencyOverdue)
        SwatchRow("urgencyToday", f.urgencyToday)
        SwatchRow("urgencyUpcoming", f.urgencyUpcoming)
        SwatchRow("urgencyDistant", f.urgencyDistant)
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/UrgencyDot.kt`

```kotlin
fun UrgencyDot(urgency: Urgency, modifier: Modifier = Modifier, size: Dp = 10.dp)
```
Solid colored dot used as the leading accent on a list row (bill reminder, loan due, task). Pairs cheaply with any list-item layout to encode urgency at a glance without stealing focus from the row's text content.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardLoadingSkeleton.kt`

```kotlin
fun CardLoadingSkeleton(modifier: Modifier = Modifier)
```
Whole-card shimmer placeholder. Drop in for `AppCard` / `Card` while the underlying data loads. Renders 3 stacked shimmer bars to suggest a typical card layout (title + 2 body lines).

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardStateBox.kt:46</code></summary>

```kotlin
) {
    when (state) {
        is ScreenState.Loading -> CardLoadingSkeleton(modifier = modifier)
        is ScreenState.Empty -> Card(modifier = modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                contentAlignment = Alignment.Center,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardStateBox.kt`

```kotlin
fun <T> CardStateBox(
```
State-aware Card wrapper. Renders the right component-scale UI for each `ScreenState` variant *inside* the Card bounds — no full-screen overlay. Use for individual cards on a dashboard, where each card resolves its own data independently.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/ErrorChip.kt`

```kotlin
fun ErrorChip(message: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null)
```
M3 AssistChip styled for error reporting. Slightly more prominent than `InlineErrorPill` — adds an icon and uses chip semantics (clickable by default if `onClick` is non-null).

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/InlineErrorPill.kt`

```kotlin
fun InlineErrorPill(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null)
```
Pill-shaped inline error chip — for component-scale failures (a single row failing in an otherwise-loaded list, a stale field in a form, a card-local fetch failure).

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/CardStateBox.kt:64</code></summary>

```kotlin
                contentAlignment = Alignment.Center,
            ) {
                InlineErrorPill(message = state.error.message ?: "Something went wrong")
            }
        }
        is ScreenState.NoNetwork -> Card(modifier = modifier.fillMaxWidth()) {
            Box(
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/component/state/RowLoadingShimmer.kt`

```kotlin
fun RowLoadingShimmer(modifier: Modifier = Modifier)
```
Single-row shimmer placeholder. Use inside LazyColumn `items()` while paged data loads, or as a one-off row placeholder for a single record waiting on a network response. Layout: circle avatar + 2 stacked text bars (title + subtitle).

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppAlertDialog.kt`

```kotlin
fun AppDialogBox(
```
Confirm/dismiss dialog with an optional text message, shown while `showDialogState` is true.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppDesignSystemAllPreviews.kt:34</code></summary>

```kotlin
private fun AppDialogBoxWithMessage() {
    AppTheme {
        AppDialogBox(
            title = "Delete Item",
            showDialogState = true,
            confirmButtonText = "Yes",
            dismissButtonText = "No",
```

</details>

```kotlin
fun AppDialogBox(
```
Confirm/dismiss dialog whose message is composable content, shown while `showDialogState` is true.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppDesignSystemAllPreviews.kt:34</code></summary>

```kotlin
private fun AppDialogBoxWithMessage() {
    AppTheme {
        AppDialogBox(
            title = "Delete Item",
            showDialogState = true,
            confirmButtonText = "Yes",
            dismissButtonText = "No",
```

</details>

```kotlin
fun AppRadioButtonDialog(
```
Dialog listing `items` as radio buttons, with `selectedItem` checked.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppDesignSystemAllPreviews.kt:70</code></summary>

```kotlin
@Preview
@Composable
private fun AppRadioButtonDialog() {
    AppTheme {
        AppRadioButtonDialog(
            title = "Choose Option",
            selectedItem = "Option 2",
```

</details>

```kotlin
fun AppCustomDialog(
```
Bare dialog around caller-supplied content.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppDesignSystemAllPreviews.kt:84</code></summary>

```kotlin
@Preview
@Composable
private fun AppCustomDialog() {
    AppTheme {
        AppCustomDialog(
            onDismiss = {},
            content = {
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppBasicDialog.kt`

```kotlin
fun AppBasicDialog(
```
Message dialog with a single dismiss button; nothing is shown while `visibilityState` is hidden.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/details/ClientDetailsScreen.kt:425</code></summary>

```kotlin
        null -> Unit

        is ClientDetailsState.DialogState.UnAssignStaff -> AppBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = stringResource(Res.string.feature_client_dialog_unassign_message),
                title = stringResource(Res.string.feature_client_title_unassign_staff),
            ),
```

</details>

```kotlin
fun AppBasicDialog(
```
Message dialog with confirm and dismiss buttons; nothing is shown while `visibilityState` is hidden.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/details/ClientDetailsScreen.kt:425</code></summary>

```kotlin
        null -> Unit

        is ClientDetailsState.DialogState.UnAssignStaff -> AppBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = stringResource(Res.string.feature_client_dialog_unassign_message),
                title = stringResource(Res.string.feature_client_title_unassign_staff),
            ),
```

</details>

```kotlin
fun AppBasicDialog(
```
Titled dialog around caller-supplied content, with confirm and dismiss buttons.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/details/ClientDetailsScreen.kt:425</code></summary>

```kotlin
        null -> Unit

        is ClientDetailsState.DialogState.UnAssignStaff -> AppBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = stringResource(Res.string.feature_client_dialog_unassign_message),
                title = stringResource(Res.string.feature_client_title_unassign_staff),
            ),
```

</details>

```kotlin
sealed class BasicDialogState
```
Models display of a `AppBasicDialog`.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/details/ClientDetailsScreen.kt:426</code></summary>

```kotlin

        is ClientDetailsState.DialogState.UnAssignStaff -> AppBasicDialog(
            visibilityState = BasicDialogState.Shown(
                message = stringResource(Res.string.feature_client_dialog_unassign_message),
                title = stringResource(Res.string.feature_client_title_unassign_staff),
            ),
            onConfirm = { onAction(ClientDetailsAction.ConfirmUnAssignStaff(dialog.staffId)) },
```

</details>

```kotlin
class BasicDialogStatePreviewProvider : PreviewParameterProvider<BasicDialogState>
```
Sample dialog states for previews.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppDesignSystemAllPreviews.kt:106</code></summary>

```kotlin
@Composable
private fun AppBasicDialog(
    @PreviewParameter(BasicDialogStatePreviewProvider::class)
    state: BasicDialogState,
) {
    AppTheme {
        AppBasicDialog(
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppBottomSheet.kt`

```kotlin
fun AppBottomSheet(
```
Modal bottom sheet around caller-supplied content.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientScreen.kt:811</code></summary>

```kotlin
@Composable
private fun PhotoOptions(hasPhoto: Boolean, onAction: (CreateNewClientAction) -> Unit) {
    AppBottomSheet(onDismiss = { onAction(CreateNewClientAction.OnDismissDialog) }) {
        Row(
            modifier = Modifier.padding(
                start = KptTheme.spacing.md,
                end = KptTheme.spacing.md,
```

</details>

```kotlin
fun AppBottomSheetOptionItem(
```
Bottom-sheet option: an icon on a tinted tile above its label.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientScreen.kt:820</code></summary>

```kotlin
            horizontalArrangement = Arrangement.Start,
        ) {
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_createnew_gallery),
                icon = AppIcons.Gallery,
                onClick = { onAction(CreateNewClientAction.OpenImagePicker) },
            )
```

</details>

```kotlin
fun AppBottomSheetOptionItem(
```
Bottom-sheet option: an icon on an elevated tile above its label.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientScreen.kt:820</code></summary>

```kotlin
            horizontalArrangement = Arrangement.Start,
        ) {
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_createnew_gallery),
                icon = AppIcons.Gallery,
                onClick = { onAction(CreateNewClientAction.OpenImagePicker) },
            )
```

</details>

```kotlin
fun AppBottomSheetOptionItem(
```
Bottom-sheet option: an icon beside its label, in one row.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/CreateNewClientScreen.kt:820</code></summary>

```kotlin
            horizontalArrangement = Arrangement.Start,
        ) {
            AppBottomSheetOptionItem(
                label = stringResource(Res.string.feature_client_createnew_gallery),
                icon = AppIcons.Gallery,
                onClick = { onAction(CreateNewClientAction.OpenImagePicker) },
            )
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppButton.kt`

```kotlin
fun AppButton(
```
App button with generic content slot. Wraps Material 3 `Button`.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:166</code></summary>

```kotlin
            Spacer(modifier = Modifier.height(KptTheme.spacing.md))

            AppButton(
                onClick = {
                    onAction(
                        ActivateAction.Submit(
                            // The original's payload, field for field — and it pairs the date with
```

</details>

```kotlin
fun AppButton(
```
App button with generic content slot. Wraps Material 3 `Button`.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:166</code></summary>

```kotlin
            Spacer(modifier = Modifier.height(KptTheme.spacing.md))

            AppButton(
                onClick = {
                    onAction(
                        ActivateAction.Submit(
                            // The original's payload, field for field — and it pairs the date with
```

</details>

```kotlin
fun AppOutlinedButton(
```
App outlined button with generic content slot. Wraps Material 3 `OutlinedButton`.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addaddress/ClientAddAddressScreen.kt:271</code></summary>

```kotlin
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AppOutlinedButton(
                onClick = { onAction(ClientAddAddressAction.NavigateBack) },
                modifier = Modifier.weight(1f).testTag(ClientAddAddressTestTags.BACK),
            ) {
                Text(stringResource(Res.string.feature_client_btn_back))
```

</details>

```kotlin
fun AppOutlinedButton(
```
App outlined button with text and optional leading icon.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/addaddress/ClientAddAddressScreen.kt:271</code></summary>

```kotlin
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            AppOutlinedButton(
                onClick = { onAction(ClientAddAddressAction.NavigateBack) },
                modifier = Modifier.weight(1f).testTag(ClientAddAddressTestTags.BACK),
            ) {
                Text(stringResource(Res.string.feature_client_btn_back))
```

</details>

```kotlin
fun AppTextButton(
```
App text button with generic content slot. Wraps Material 3 `TextButton`.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/closure/ClientClosureScreen.kt:227</code></summary>

```kotlin
                )
                Spacer(Modifier.padding(KptTheme.spacing.sm))
                AppTextButton(
                    onClick = { onAction(ClientClosureAction.OnSubmit(selected.id)) },
                    enabled = !state.isSubmitting,
                    leadingIcon = {
                        Icon(
```

</details>

```kotlin
fun AppTextButton(
```
App text button with text and icon content slots.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/closure/ClientClosureScreen.kt:227</code></summary>

```kotlin
                )
                Spacer(Modifier.padding(KptTheme.spacing.sm))
                AppTextButton(
                    onClick = { onAction(ClientClosureAction.OnSubmit(selected.id)) },
                    enabled = !state.isSubmitting,
                    leadingIcon = {
                        Icon(
```

</details>

```kotlin
object AppButtonDefaults
```
App button default values.

- `val OutlinedButtonBorderWidth = 1.dp` — Border width of an outlined button.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppCard.kt`

```kotlin
fun AppCard(
```
Card with the app's default shape, elevation and colours; clickable when `onClick` is set.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/profile/components/ProfileCard.kt:61</code></summary>

```kotlin
    onGroupClick: () -> Unit = {},
) {
    AppCard(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = KptTheme.colorScheme.primary,
            contentColor = KptTheme.colorScheme.onPrimary,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppMenuDropdown.kt`

```kotlin
fun AppMenuDropDownItem(
```
One text option of a dropdown menu.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/detail/CenterDetailScreen.kt:162</code></summary>

```kotlin
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            AppMenuDropDownItem(
                                option = stringResource(Res.string.feature_center_add_savings_account),
                                onClick = {
                                    expanded = false
                                    onAction(CenterDetailAction.AddSavingsAccountClicked)
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppOutlinedTextField.kt`

```kotlin
fun AppOutlinedTextField(
```
Outlined text field over a `TextFieldValue`, with an optional leading icon and error text.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/create/CreateCenterScreen.kt:133</code></summary>

```kotlin
                    )
                }
                AppOutlinedTextField(
                    value = state.name,
                    onValueChange = { onAction(CreateCenterAction.NameChanged(it)) },
                    label = stringResource(Res.string.feature_center_center_name),
                    isError = state.nameError != null,
```

</details>

```kotlin
fun AppOutlinedTextField(
```
Outlined text field with placeholder, error text and an optional password visibility toggle.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/create/CreateCenterScreen.kt:133</code></summary>

```kotlin
                    )
                }
                AppOutlinedTextField(
                    value = state.name,
                    onValueChange = { onAction(CreateCenterAction.NameChanged(it)) },
                    label = stringResource(Res.string.feature_center_center_name),
                    isError = state.nameError != null,
```

</details>

```kotlin
fun AppOutlinedTextField(
```
Outlined text field that shows `error` beneath it when non-null.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/create/CreateCenterScreen.kt:133</code></summary>

```kotlin
                    )
                }
                AppOutlinedTextField(
                    value = state.name,
                    onValueChange = { onAction(CreateCenterAction.NameChanged(it)) },
                    label = stringResource(Res.string.feature_center_center_name),
                    isError = state.nameError != null,
```

</details>

```kotlin
fun AppDatePickerTextField(
```
Read-only field showing a date; tapping it calls `openDatePicker`.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:157</code></summary>

```kotlin
            }

            AppDatePickerTextField(
                value = DateHelper.getDateAsStringFromLong(activateDate),
                label = stringResource(Res.string.feature_activate_activation_date),
                openDatePicker = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().testTag(ActivateTestTags.DATE),
```

</details>

```kotlin
fun AppOutlinedTextField(
```
Outlined text field configured by an `AppTextFieldConfig`.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/create/CreateCenterScreen.kt:133</code></summary>

```kotlin
                    )
                }
                AppOutlinedTextField(
                    value = state.name,
                    onValueChange = { onAction(CreateCenterAction.NameChanged(it)) },
                    label = stringResource(Res.string.feature_center_center_name),
                    isError = state.nameError != null,
```

</details>

```kotlin
data class AppPasswordToggle(
```
Password visibility toggle state. `isDisplayed` left null shows the toggle for password fields (`KeyboardType.Password`) only.

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInScreen.kt:367</code></summary>

```kotlin
        // while there is no error, an error icon instead when there is one. Disabling
        // the toggle is what hands the slot over.
        passwordToggle = AppPasswordToggle(
            isVisible = passwordVisible,
            onClick = { onVisibleChange(it) },
            isDisplayed = !passwordInvalid,
        ),
```

</details>

```kotlin
data class AppTextFieldConfig(
```
Behaviour and decoration of an `AppOutlinedTextField`.

<details><summary>Used in the template — <code>core/ui/src/commonMain/kotlin/kpt/core/ui/components/AddChargeDialog.kt:128</code></summary>

```kotlin
                    onValueChange = onAmountChange,
                    label = stringResource(Res.string.amount),
                    config = AppTextFieldConfig(
                        isError = amountError != null,
                        errorText = amountError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppScaffold.kt`

```kotlin
fun AppScaffold(
```
Screen scaffold with a titled top bar and back button, optional FAB, snackbar host and pull-to-refresh.

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:115</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_about_title),
        onBackPressed = { onAction(AboutAction.NavigateBack) },
        modifier = modifier,
    ) { padding ->
```

</details>

```kotlin
fun AppScaffold(
```
Screen scaffold over caller-supplied bars, with snackbar host and pull-to-refresh.

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:115</code></summary>

```kotlin
    modifier: Modifier = Modifier,
) {
    AppScaffold(
        title = stringResource(Res.string.feature_about_title),
        onBackPressed = { onAction(AboutAction.NavigateBack) },
        modifier = modifier,
    ) { padding ->
```

</details>

```kotlin
data class FloatingActionButtonContent(
```
The floating action button of an `AppScaffold`.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/address/ClientAddressScreen.kt:104</code></summary>

```kotlin
        // a real device, and it passed `contentDescription = null` so it was unlabelled too.
        floatingActionButtonContent = onAddAddress?.let {
            FloatingActionButtonContent(
                onClick = { viewModel.trySendAction(ClientAddressAction.ShowAddressForm) },
                contentColor = KptTheme.colorScheme.onPrimaryContainer,
                content = {
                    Icon(
```

</details>

```kotlin
data class AppPullToRefreshState(
```
Pull-to-refresh state of an `AppScaffold`.

```kotlin
fun rememberAppPullToRefreshState(
```
Remembers an `AppPullToRefreshState` for these values.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppSweetError.kt`

```kotlin
fun AppSweetError(
```
Full-screen error message with an optional retry button.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormScreen.kt:128</code></summary>

```kotlin
            )

            state.typesError -> AppSweetError(
                message = stringResource(Res.string.feature_charge_types_failed),
                onclick = { onAction(ChargeFormAction.OnRetry) },
                modifier = Modifier.fillMaxSize().padding(padding).testTag(TestTags.ChargeForm.TYPES_ERROR),
            )
```

</details>

```kotlin
fun AppPaginationSweetError(
```
Inline error shown when loading the next page failed, with a retry action.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppDesignSystemAllPreviewsPart3.kt:47</code></summary>

```kotlin
private fun AppPaginationSweetErrorPreview() {
    AppTheme {
        AppPaginationSweetError(
            onclick = {},
        )
    }
}
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppTableRow.kt`

```kotlin
fun AppTableRow(
```
One table row: `cells` laid out at `widths`, with optional borders.

<details><summary>Used in the template — <code>feature/loan/src/commonMain/kotlin/kpt/feature/loan/schedule/LoanScheduleScreen.kt:184</code></summary>

```kotlin
                        .horizontalScroll(scrollState),
                ) {
                    AppTableRow(
                        cells = groupHeaders.mapIndexed { index, (text, _) ->
                            {
                                TableHeaderCell(
                                    text = text,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppTextFieldDropdown.kt`

```kotlin
fun AppTextFieldDropdown(
```
Text field that opens a dropdown of `options`; read-only unless `readOnly` is false.

<details><summary>Used in the template — <code>feature/center/src/commonMain/kotlin/kpt/feature/center/create/CreateCenterScreen.kt:141</code></summary>

```kotlin
                    modifier = Modifier.fillMaxWidth().testTag(CreateCenterTestTags.NAME),
                )
                AppTextFieldDropdown(
                    value = state.officeName,
                    onOptionSelected = { index, label ->
                        officeList.getOrNull(index)?.let { onAction(CreateCenterAction.OfficeSelected(it.id, label)) }
                    },
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppTopBar.kt`

```kotlin
fun AppTopBar(
```
Top app bar with a title, back button and optional actions.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/fieldofficer/component/AppDesignSystemAllPreviewsPart3.kt:74</code></summary>

```kotlin
private fun AppTopBarPreview() {
    AppTheme {
        AppTopBar(
            topBarTitle = "Sample Title",
            backPress = {},
        )
    }
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/AppIcons.kt`

```kotlin
object AppIcons
```
The app's icon set, referenced by name: Material icons plus the fork's own vectors in this package. Screens read icons from here, never from `Icons.*` directly, so a rebrand swaps one entry.

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:263</code></summary>

```kotlin
        }
        Icon(
            imageVector = AppIcons.ChevronRight,
            contentDescription = null,
            tint = KptTheme.colorScheme.onSurfaceVariant,
        )
    }
```

</details>

- `val Add = Icons.Rounded.Add` — `Add` icon (Icons.Rounded.Add).
- `val Menu = Icons.Default.Menu` — `Menu` icon (Icons.Default.Menu).
- `val DateRange = Icons.Outlined.DateRange` — `DateRange` icon (Icons.Outlined.DateRange).
- `val Person = Icons.Rounded.PersonOutline` — `Person` icon (Icons.Rounded.PersonOutline).
- `val Group = Icons.Outlined.Group` — `Group` icon (Icons.Outlined.Group).
- `val EventRepeat = Icons.Outlined.EventRepeat` — `EventRepeat` icon (Icons.Outlined.EventRepeat).
- `val Date = Icons.Outlined.DateRange` — `Date` icon (Icons.Outlined.DateRange).
- `val ArrowBack1 = Icons.Rounded.ArrowBackIosNew` — `ArrowBack1` icon (Icons.Rounded.ArrowBackIosNew).
- `val DoneAll = Icons.Default.DoneAll` — `DoneAll` icon (Icons.Default.DoneAll).
- `val KeyboardArrowDown = Icons.Rounded.KeyboardArrowDown` — `KeyboardArrowDown` icon (Icons.Rounded.KeyboardArrowDown).
- `val Link = Icons.Default.Link` — `Link` icon (Icons.Default.Link).
- `val Server = Icons.Default` — `Server` icon (Icons.Default).
- `val AddLink = Icons.Default.AddLink` — `AddLink` icon (Icons.Default.AddLink).
- `val Search = Icons.Rounded.Search` — `Search` icon (Icons.Rounded.Search).
  _…more members; read the file._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/ForkDesignIcons.kt`

```kotlin
val Calender: ImageVector
```
Hand-drawn `Calender` vector glyph.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/AppIcons.kt:463</code></summary>

```kotlin

    /** `Calendar` icon (Calender). */
    val Calendar = kpt.core.designsystem.icon.Calender

    /** `Folder` icon (Folder). */
    val Folder = kpt.core.designsystem.icon.Folder
```

</details>

```kotlin
val Edit: ImageVector
```
Hand-drawn `Edit` vector glyph.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/detail/ChargeDetailScreen.kt:87</code></summary>

```kotlin
    ChargeDetailContent(
        charge = charge,
        canEdit = viewModel.canEdit,
        onAction = remember(viewModel) { { viewModel.trySendAction(it) } },
        modifier = modifier,
    )
}
```

</details>

```kotlin
val Folder: ImageVector
```
Hand-drawn `Folder` vector glyph.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/AppIcons.kt:466</code></summary>

```kotlin

    /** `Folder` icon (Folder). */
    val Folder = kpt.core.designsystem.icon.Folder

    /** `GrowthChart` icon (GrownChart). */
    val GrowthChart = GrownChart
```

</details>

```kotlin
val ShieldOutlined: ImageVector
```
Hand-drawn `ShieldOutlined` vector glyph.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/AppIcons.kt:475</code></summary>

```kotlin

    /** `ShielOutlined` icon (ShieldOutlined). */
    val ShielOutlined = ShieldOutlined

    /** `Edit` icon (Edit). */
    val Edit = kpt.core.designsystem.icon.Edit
```

</details>

```kotlin
val GrownChart: ImageVector
```
Hand-drawn `GrownChart` vector glyph.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/AppIcons.kt:469</code></summary>

```kotlin

    /** `GrowthChart` icon (GrownChart). */
    val GrowthChart = GrownChart

    /** `PickDocument` icon (ClipDocument). */
    val PickDocument = ClipDocument
```

</details>

```kotlin
val Wallet: ImageVector
```
Hand-drawn `Wallet` vector glyph.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/applynew/ClientApplyNewScreen.kt:241</code></summary>

```kotlin
    ApplyNewItem.SHARE -> AppIcons.GrowthChart
    ApplyNewItem.RECURRING_DEPOSIT -> AppIcons.EventRepeat
    ApplyNewItem.FIXED_DEPOSIT -> AppIcons.Wallet
}

/** Stable handles for UI tests and Maestro flows — see RULE-KMP-COMPOSE-UITEST-001. */
internal object ClientApplyNewTestTags {
```

</details>

```kotlin
val GalleryIcon: ImageVector
```
Hand-drawn `GalleryIcon` vector glyph.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/AppIcons.kt:273</code></summary>

```kotlin

    /** `Gallery` icon (GalleryIcon). */
    val Gallery = GalleryIcon

    /** `ArrowDropDown` icon (Icons.Default.ArrowDropDown). */
    val ArrowDropDown = Icons.Default.ArrowDropDown
```

</details>

```kotlin
val ClipDocument: ImageVector
```
Hand-drawn `ClipDocument` vector glyph.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/AppIcons.kt:472</code></summary>

```kotlin

    /** `PickDocument` icon (ClipDocument). */
    val PickDocument = ClipDocument

    /** `ShielOutlined` icon (ShieldOutlined). */
    val ShielOutlined = ShieldOutlined
```

</details>

```kotlin
val RightTick: ImageVector
```
Hand-drawn `RightTick` vector glyph.

<details><summary>Used in the template — <code>feature/offlinesync/src/commonMain/kotlin/kpt/feature/offlinesync/ui/OfflineSyncPicker.kt:231</code></summary>

```kotlin
        if (onDevice) {
            StatusChip(
                AppIcons.RightTick,
                stringResource(Res.string.feature_offlinesync_on_device),
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.onSecondaryContainer,
            )
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/icon/ForkIcons.kt`

```kotlin
object ForkIcons
```
The field officer app's own Material glyph set, beside the template's `AppIcons`.

<details><summary>Used in the template — <code>feature/groups/src/commonMain/kotlin/kpt/feature/groups/detail/GroupDetailSections.kt:149</code></summary>

```kotlin
        if (onClick != null) {
            Icon(
                ForkIcons.ArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
```

</details>

- `val Language: ImageVector = Icons.Default.ArrowOutward` — Material `Icons.Default.ArrowOutward`.
- `val CheckCircle: ImageVector = Icons.Filled.CheckCircle` — Material `Icons.Filled.CheckCircle`.
- `val OutlinedInfo = Icons.Outlined.Info` — Material `Icons.Outlined.Info`.
- `val OutlinedLock = Icons.Outlined.Lock` — Material `Icons.Outlined.Lock`.
- `val OutlinedNotifications = Icons.Outlined.Notifications` — Material `Icons.Outlined.Notifications`.
- `val ChevronRight: ImageVector = Icons.Filled.ChevronRight` — Material `Icons.Filled.ChevronRight`.
- `val QrCode: ImageVector = Icons.Filled.QrCode` — Material `Icons.Filled.QrCode`.
- `val Close: ImageVector = Icons.Filled.Close` — Material `Icons.Filled.Close`.
- `val AttachMoney: ImageVector = Icons.Filled.AttachMoney` — Material `Icons.Filled.AttachMoney`.
- `val OutlinedVisibilityOff: ImageVector = Icons.Outlined.VisibilityOff` — Material `Icons.Outlined.VisibilityOff`.
- `val OutlinedVisibility: ImageVector = Icons.Outlined.Visibility` — Material `Icons.Outlined.Visibility`.
- `val VisibilityOff: ImageVector = Icons.Filled.VisibilityOff` — Material `Icons.Filled.VisibilityOff`.
- `val Visibility: ImageVector = Icons.Filled.Visibility` — Material `Icons.Filled.Visibility`.
- `val Check: ImageVector = Icons.Default.Check` — Material `Icons.Default.Check`.
  _…more members; read the file._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/AccentColors.kt`

```kotlin
object AccentColors
```
Icon-tile accents the Stitch home markup uses beyond the MD3 scheme (`mockups/home/stitch/0*`): Tailwind `blue-50/700` and `purple-50/700` — the "Clients pending activation" / "New centre" and "Pending reschedule" / "Run reports" tiles.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardCards.kt:251</code></summary>

```kotlin
    val pct = percent(fraction.toDouble())
    val on = KptTheme.colorScheme.onPrimary
    val muted = AccentColors.heroText
    val shape = KptTheme.shapes.large
    Box(
        Modifier
            .fillMaxWidth()
```

</details>

- `val blueContainer = Color(0xFFEFF6FF)` — Tint behind a blue icon tile.
- `val blue = Color(0xFF1D4ED8)` — Blue icon-tile foreground.
- `val purpleContainer = Color(0xFFFAF5FF)` — Tint behind a purple icon tile.
- `val purple = Color(0xFF7E22CE)` — Purple icon-tile foreground.
- `val heroStart = Color(0xFF1E3A8A)` — "Collected today" hero gradient: start.
- `val heroMid = Color(0xFF2563EB)` — "Collected today" hero gradient: middle.
- `val heroEnd = Color(0xFF3B82F6)` — "Collected today" hero gradient: end.
- `val heroText = Color(0xFFDBEAFE)` — Hero body text.
- `val heroFaint = Color(0xFFBFDBFE)` — Hero secondary text.
- `val heroRemains = Color(0xFFFDE68A)` — Hero "Remains" amount.
- `val heroFillStart = Color(0xFF6EE7B7)` — Hero progress fill: start.
- `val heroFillEnd = Color(0xFF34D399)` — Hero progress fill: end.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/AppBackground.kt`

```kotlin
fun AppBackground(
```
The main background for the app. Uses `LocalBackgroundTheme` to set the color and tonal elevation of a `Surface`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/AppColors.kt`

```kotlin
object AppColors
```
Brand colours outside the Material colour scheme.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/address/ClientAddressScreen.kt:239</code></summary>

```kotlin
        border = BorderStroke(
            width = DesignToken.strokes.thin,
            color = AppColors.cardBorders,
        ),
    ) {
        Column(modifier = Modifier.padding(KptTheme.spacing.md)) {
            Text(
```

</details>

- `val cardBorders = Color(0xFFD6E4F7)` — Outline of address cards.
- `val customWhite = Color(0xFFFFFFFF)` — White, for content on a filled button or search bar.
- `val customEnable = Color(0xFF009325)` — Green of an active or enabled record.
- `val customYellow = Color(0xFFAE9703)` — Yellow of a pending record.
- `val lightRed = Color(0xFFFF6E6E)` — Red of a closed or rejected record.
- `val loanIndicatorActive = Color.Green` — Loan-summary status dot: active.
- `val loanIndicatorPending = Color.Yellow` — Loan-summary status dot: pending approval.
- `val loanIndicatorWaitingForDisbursal = Color.Blue` — Loan-summary status dot: approved, waiting for disbursal.
- `val loanIndicatorOther = Color.Black` — Loan-summary status dot: overpaid or closed.
- `val loanActiveStatus = Color(0xFF5CB85C)` — Status colour: active.
- `val loanPendingStatus = Color(0xFFFFA500)` — Status colour: pending.
- `val loanOverpaidStatus = Color(0xFF800080)` — Status colour: overpaid, or a rejected reschedule.
- `val loanUnknownStatus = Color(0xFF9E9E9E)` — Status colour: unknown.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/AppType.kt`

```kotlin
object AppTypography
```
The app's text styles, in the app font.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/form/ChargeFormScreen.kt:138</code></summary>

```kotlin
            !state.isEdit && state.chargeTypes.isEmpty() -> Text(
                text = stringResource(Res.string.feature_charge_none_available),
                style = AppTypography.bodyMedium,
                modifier = Modifier.fillMaxSize().padding(padding).padding(KptTheme.spacing.md)
                    .testTag(TestTags.ChargeForm.NO_TYPES),
            )
```

</details>

- `val displayLarge: TextStyle` — Material display large text style.
- `val displayMedium: TextStyle` — Material display medium text style.
- `val displaySmall: TextStyle` — Material display small text style.
- `val headlineLarge: TextStyle` — Material headline large text style.
- `val headlineMedium: TextStyle` — Material headline medium text style.
- `val headlineSmall: TextStyle` — Material headline small text style.
- `val headlineSmallEmphasized: TextStyle` — Material headline small text style, emphasized (heavier weight).
- `val titleLarge: TextStyle` — Material title large text style.
- `val titleLargeEmphasized: TextStyle` — Material title large text style, emphasized (heavier weight).
- `val titleMedium: TextStyle` — Material title medium text style.
- `val titleMediumEmphasized: TextStyle` — Material title medium text style, emphasized (heavier weight).
- `val titleSmall: TextStyle` — Material title small text style.
- `val titleSmallEmphasized: TextStyle` — Material title small text style, emphasized (heavier weight).
- `val labelLarge: TextStyle` — Material label large text style.
  _…more members; read the file._

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/BackgroundTheme.kt`

```kotlin
data class BackgroundTheme(
```
A class to model background color and tonal elevation values for Now in Android.

```kotlin
val LocalBackgroundTheme = staticCompositionLocalOf { BackgroundTheme() }
```
A composition local for `BackgroundTheme`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/AppBackground.kt:34</code></summary>

```kotlin
    content: @Composable () -> Unit,
) {
    val color = LocalBackgroundTheme.current.color
    val tonalElevation = LocalBackgroundTheme.current.tonalElevation
    Surface(
        color = if (color == Color.Unspecified) Color.Transparent else color,
        tonalElevation = if (tonalElevation == Dp.Unspecified) KptTheme.elevation.level0 else tonalElevation,
```

</details>

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Color.kt`

```kotlin
val primaryLight = Color(0xFF4338CA)
```
_No KDoc at source._

```kotlin
val onPrimaryLight = Color(0xFFFFFFFF)
```
Material `onPrimary` colour role, light scheme.

```kotlin
val primaryContainerLight = Color(0xFFE0E7FF)
```
Material `primaryContainer` colour role, light scheme.

```kotlin
val onPrimaryContainerLight = Color(0xFF1F1D75)
```
Material `onPrimaryContainer` colour role, light scheme.

```kotlin
val secondaryLight = Color(0xFF059669)
```
Material `secondary` colour role, light scheme.

```kotlin
val onSecondaryLight = Color(0xFFFFFFFF)
```
Material `onSecondary` colour role, light scheme.

```kotlin
val secondaryContainerLight = Color(0xFFD1FAE5)
```
Material `secondaryContainer` colour role, light scheme.

```kotlin
val onSecondaryContainerLight = Color(0xFF064E3B)
```
Material `onSecondaryContainer` colour role, light scheme.

```kotlin
val tertiaryLight = Color(0xFFD97706)
```
Material `tertiary` colour role, light scheme.

```kotlin
val onTertiaryLight = Color(0xFFFFFFFF)
```
Material `onTertiary` colour role, light scheme.

```kotlin
val tertiaryContainerLight = Color(0xFFFEF3C7)
```
Material `tertiaryContainer` colour role, light scheme.

```kotlin
val onTertiaryContainerLight = Color(0xFF78350F)
```
Material `onTertiaryContainer` colour role, light scheme.

```kotlin
val errorLight = Color(0xFFF87171)
```
Material `error` colour role, light scheme.

```kotlin
val onErrorLight = Color(0xFFFFFFFF)
```
Material `onError` colour role, light scheme.

```kotlin
val errorContainerLight = Color(0xFFFFE4E1)
```
Material `errorContainer` colour role, light scheme.

```kotlin
val onErrorContainerLight = Color(0xFF9F1239)
```
Material `onErrorContainer` colour role, light scheme.

```kotlin
val backgroundLight = Color(0xFFFAFAFB)
```
Material `background` colour role, light scheme.

```kotlin
val onBackgroundLight = Color(0xFF0F172A)
```
Material `onBackground` colour role, light scheme.

```kotlin
val surfaceLight = Color(0xFFFAFAFB)
```
Material `surface` colour role, light scheme.

```kotlin
val onSurfaceLight = Color(0xFF0F172A)
```
Material `onSurface` colour role, light scheme.

```kotlin
val surfaceVariantLight = Color(0xFFE2E8F0)
```
Material `surfaceVariant` colour role, light scheme.

```kotlin
val onSurfaceVariantLight = Color(0xFF475569)
```
Material `onSurfaceVariant` colour role, light scheme.

```kotlin
val outlineLight = Color(0xFF94A3B8)
```
Material `outline` colour role, light scheme.

```kotlin
val outlineVariantLight = Color(0xFFCBD5E1)
```
Material `outlineVariant` colour role, light scheme.

```kotlin
val scrimLight = Color(0xFF000000)
```
Material `scrim` colour role, light scheme.

```kotlin
val inverseSurfaceLight = Color(0xFF1E293B)
```
Material `inverseSurface` colour role, light scheme.

```kotlin
val inverseOnSurfaceLight = Color(0xFFF1F5F9)
```
Material `inverseOnSurface` colour role, light scheme.

```kotlin
val inversePrimaryLight = Color(0xFFA5B4FC)
```
Material `inversePrimary` colour role, light scheme.

```kotlin
val surfaceDimLight = Color(0xFFE2E8F0)
```
Material `surfaceDim` colour role, light scheme.

```kotlin
val surfaceBrightLight = Color(0xFFFAFAFB)
```
Material `surfaceBright` colour role, light scheme.

```kotlin
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
```
Material `surfaceContainerLowest` colour role, light scheme.

```kotlin
val surfaceContainerLowLight = Color(0xFFF8FAFC)
```
Material `surfaceContainerLow` colour role, light scheme.

```kotlin
val surfaceContainerLight = Color(0xFFF1F5F9)
```
Material `surfaceContainer` colour role, light scheme.

```kotlin
val surfaceContainerHighLight = Color(0xFFE2E8F0)
```
Material `surfaceContainerHigh` colour role, light scheme.

```kotlin
val surfaceContainerHighestLight = Color(0xFFCBD5E1)
```
Material `surfaceContainerHighest` colour role, light scheme.

```kotlin
val primaryDark = Color(0xFFA5B4FC)
```
Material `primary` colour role, dark scheme.

```kotlin
val onPrimaryDark = Color(0xFF1F1D75)
```
Material `onPrimary` colour role, dark scheme.

```kotlin
val primaryContainerDark = Color(0xFF3730A3)
```
Material `primaryContainer` colour role, dark scheme.

```kotlin
val onPrimaryContainerDark = Color(0xFFE0E7FF)
```
Material `onPrimaryContainer` colour role, dark scheme.

```kotlin
val secondaryDark = Color(0xFF6EE7B7)
```
Material `secondary` colour role, dark scheme.

```kotlin
val onSecondaryDark = Color(0xFF064E3B)
```
Material `onSecondary` colour role, dark scheme.

```kotlin
val secondaryContainerDark = Color(0xFF065F46)
```
Material `secondaryContainer` colour role, dark scheme.

```kotlin
val onSecondaryContainerDark = Color(0xFFD1FAE5)
```
Material `onSecondaryContainer` colour role, dark scheme.

```kotlin
val tertiaryDark = Color(0xFFFCD34D)
```
Material `tertiary` colour role, dark scheme.

```kotlin
val onTertiaryDark = Color(0xFF78350F)
```
Material `onTertiary` colour role, dark scheme.

```kotlin
val tertiaryContainerDark = Color(0xFF92400E)
```
Material `tertiaryContainer` colour role, dark scheme.

```kotlin
val onTertiaryContainerDark = Color(0xFFFEF3C7)
```
Material `onTertiaryContainer` colour role, dark scheme.

```kotlin
val errorDark = Color(0xFFFDBA74)
```
Material `error` colour role, dark scheme.

```kotlin
val onErrorDark = Color(0xFF7C2D12)
```
Material `onError` colour role, dark scheme.

```kotlin
val errorContainerDark = Color(0xFFC2410C)
```
Material `errorContainer` colour role, dark scheme.

```kotlin
val onErrorContainerDark = Color(0xFFFFEDD5)
```
Material `onErrorContainer` colour role, dark scheme.

```kotlin
val backgroundDark = Color(0xFF0F172A)
```
Material `background` colour role, dark scheme.

```kotlin
val onBackgroundDark = Color(0xFFF1F5F9)
```
Material `onBackground` colour role, dark scheme.

```kotlin
val surfaceDark = Color(0xFF0F172A)
```
Material `surface` colour role, dark scheme.

```kotlin
val onSurfaceDark = Color(0xFFF1F5F9)
```
Material `onSurface` colour role, dark scheme.

```kotlin
val surfaceVariantDark = Color(0xFF1E293B)
```
Material `surfaceVariant` colour role, dark scheme.

```kotlin
val onSurfaceVariantDark = Color(0xFFCBD5E1)
```
Material `onSurfaceVariant` colour role, dark scheme.

```kotlin
val outlineDark = Color(0xFF64748B)
```
Material `outline` colour role, dark scheme.

```kotlin
val outlineVariantDark = Color(0xFF334155)
```
Material `outlineVariant` colour role, dark scheme.

```kotlin
val scrimDark = Color(0xFF000000)
```
Material `scrim` colour role, dark scheme.

```kotlin
val inverseSurfaceDark = Color(0xFFF1F5F9)
```
Material `inverseSurface` colour role, dark scheme.

```kotlin
val inverseOnSurfaceDark = Color(0xFF1E293B)
```
Material `inverseOnSurface` colour role, dark scheme.

```kotlin
val inversePrimaryDark = Color(0xFF4338CA)
```
Material `inversePrimary` colour role, dark scheme.

```kotlin
val surfaceDimDark = Color(0xFF0F172A)
```
Material `surfaceDim` colour role, dark scheme.

```kotlin
val surfaceBrightDark = Color(0xFF374558)
```
Material `surfaceBright` colour role, dark scheme.

```kotlin
val surfaceContainerLowestDark = Color(0xFF020617)
```
Material `surfaceContainerLowest` colour role, dark scheme.

```kotlin
val surfaceContainerLowDark = Color(0xFF1E293B)
```
Material `surfaceContainerLow` colour role, dark scheme.

```kotlin
val surfaceContainerDark = Color(0xFF243044)
```
Material `surfaceContainer` colour role, dark scheme.

```kotlin
val surfaceContainerHighDark = Color(0xFF2D3B52)
```
Material `surfaceContainerHigh` colour role, dark scheme.

```kotlin
val surfaceContainerHighestDark = Color(0xFF374558)
```
Material `surfaceContainerHighest` colour role, dark scheme.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/DesignToken.kt`

```kotlin
object DesignToken
```
The current design tokens, read from the composition.

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:156</code></summary>

```kotlin
        // a dark cross — a different mark. Brand marks sit on the background they were drawn for.
        Surface(
            shape = DesignToken.shapes.extraLargeIncreased,
            color = Color.White,
            shadowElevation = 2.dp,
        ) {
            Image(
```

</details>

- `val spacing: AppSpacing` — The current spacing tokens.
- `val padding: AppPadding` — The current padding tokens.
- `val shapes: AppShapes` — The current shape tokens.
- `val elevation: AppElevation` — The current elevation tokens.
- `val sizes: AppSizes` — The current size tokens.
- `val strokes: AppStrokes` — The current stroke-width tokens.

```kotlin
data class AppSpacing(
```
Defines standardized spacing values used throughout the application for consistent layout spacing.

<details><summary>Example</summary>

```kotlin
Column(
    verticalArrangement = Arrangement.spacedBy(DesignTokens.spacing.medium)
) {
    Text("First item")
    Text("Second item")
}
```

</details>

```kotlin
data class AppPadding(
```
Defines standardized padding values for consistent internal spacing within components.

<details><summary>Example</summary>

```kotlin
Card(
    modifier = Modifier.padding(DesignTokens.padding.card)
) {
    Text(
        text = "Content",
        modifier = Modifier.padding(DesignTokens.padding.content)
    )
}
```

</details>

```kotlin
data class AppShapes(
```
Defines standardized shape values for consistent corner radii and border shapes across components. This immutable data class provides shape definitions that ensure visual consistency across all UI components.

<details><summary>Example</summary>

```kotlin
Card(
    shape = DesignTokens.shapes.card
) {
    // Card content
}

Button(
    shape = DesignTokens.shapes.button,
    onClick = { }
) {
    Text("Button")
}
```

</details>

```kotlin
data class AppElevation(
```
Defines standardized elevation values for consistent shadow depths and layering across components. This immutable data class provides elevation values that create a consistent visual hierarchy and depth perception throughout the application.

<details><summary>Example</summary>

```kotlin
Card(
    elevation = CardDefaults.cardElevation(
        defaultElevation = DesignTokens.elevation.card
    )
) {
    // Card content
}

FloatingActionButton(
    elevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = DesignTokens.elevation.fab
    ),
    onClick = { }
) {
    Icon(Icons.Default.Add, contentDescription = "Add")
}
```

</details>

```kotlin
data class AppSizes(
```
Defines standardized size values for consistent component dimensions across the application.

<details><summary>Example</summary>

```kotlin
Icon(
    imageVector = Icons.Default.Home,
    contentDescription = "Home",
    modifier = Modifier.size(DesignTokens.sizes.iconMedium)
)

Button(
    onClick = { },
    modifier = Modifier.height(DesignTokens.sizes.buttonHeight)
) {
    Text("Action")
}

AsyncImage(
    model = userAvatarUrl,
    contentDescription = "User Avatar",
    modifier = Modifier.size(DesignTokens.sizes.avatarLarge)
)
```

</details>

```kotlin
data class AppStrokes(
```
Stroke-width tokens.

```kotlin
val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }
```
CompositionLocal provider for accessing `AppSpacing` values throughout the composition tree. This provides a default instance of `AppSpacing` that can be overridden by providing a different value through `DesignTokenTheme`.

```kotlin
val LocalPadding = staticCompositionLocalOf { AppPadding() }
```
CompositionLocal provider for accessing `AppPadding` values throughout the composition tree. This provides a default instance of `AppPadding` that can be overridden by providing a different value through `DesignTokenTheme`.

```kotlin
val LocalShapes = staticCompositionLocalOf { AppShapes() }
```
CompositionLocal provider for accessing `AppShapes` values throughout the composition tree. This provides a default instance of `AppShapes` that can be overridden by providing a different value through `DesignTokenTheme`.

```kotlin
val LocalAppElevation = staticCompositionLocalOf { AppElevation() }
```
CompositionLocal provider for accessing `AppElevation` values throughout the composition tree. This provides a default instance of `AppElevation` that can be overridden by providing a different value through `DesignTokenTheme`.

```kotlin
val LocalSizes = staticCompositionLocalOf { AppSizes() }
```
CompositionLocal provider for accessing `AppSizes` values throughout the composition tree. This provides a default instance of `AppSizes` that can be overridden by providing a different value through `DesignTokenTheme`.

```kotlin
val LocalStrokes = staticCompositionLocalOf { AppStrokes() }
```
Provides the current `AppStrokes`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Elevation.kt`

```kotlin
data class Elevation(
```
Shared elevation tier scale — five named tiers matching Material 3 elevation guidance. Access from any Composable via `MaterialTheme.elevation`.

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:158</code></summary>

```kotlin
            shape = DesignToken.shapes.extraLargeIncreased,
            color = Color.White,
            shadowElevation = 2.dp,
        ) {
            Image(
                painter = painterResource(Res.drawable.feature_about_logo),
                contentDescription = stringResource(Res.string.feature_about_logo_description, state.appName),
```

</details>

```kotlin
val LocalElevation = staticCompositionLocalOf { Elevation() }
```
Provides the current `Elevation`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:77</code></summary>

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

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:60</code></summary>

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

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:74</code></summary>

```kotlin
        // core/store/KptScreenStateDefaults.kt — that's the single fork seam.
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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/ForkTheme.kt`

```kotlin
val lightScheme = lightColorScheme(
```
The assembled Material 3 light scheme, wired from the palette tokens in `Color.kt`.

```kotlin
val darkScheme = darkColorScheme(
```
The assembled Material 3 dark scheme, wired from the palette tokens in `Color.kt`.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt`

```kotlin
fun KptTheme(
```
The main theme composable for the application. This composable uses KptMaterialTheme under the hood to provide seamless integration between KptTheme design tokens and Material3 theming system.

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:125</code></summary>

```kotlin
                .padding(padding)
                .testTag(AboutTestTags.SCREEN),
            verticalArrangement = Arrangement.spacedBy(KptTheme.spacing.md),
        ) {
            item { BrandHeader(state = state, onAction = onAction) }

            AboutSection.entries.forEach { section ->
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

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardCards.kt:283</code></summary>

```kotlin
                        style = KptTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = WIDER,
                        color = muted,
                    )
                }
                Chip(
```

</details>

```kotlin
val LocalSpacing = staticCompositionLocalOf { Spacing() }
```
Provides the current `Spacing`.

<details><summary>Used in the template — <code>core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/KptTheme.kt:76</code></summary>

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

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Theme.kt`

```kotlin
fun AppTheme(
```
Applies the app's colours and typography: the platform scheme when dynamic theming is on, else the brand scheme.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/createnew/ClientDataTableSectionsPreview.kt:45</code></summary>

```kotlin
@Composable
internal fun ClientDataTableSectionsPreview() {
    AppTheme {
        Surface {
            Column(Modifier.padding(KptTheme.spacing.md)) {
                DataTableSections(
                    tables = listOf(previewTable),
```

</details>

```kotlin
expect fun colorScheme(useDarkTheme: Boolean, dynamicColor: Boolean): ColorScheme
```
The platform's colour scheme: dynamic colour where the platform has it, else the brand scheme.

### `core/designsystem/src/commonMain/kotlin/kpt/core/designsystem/theme/Type.kt`

```kotlin
val fontFamily: FontFamily
```
Outfit — `design-tokens.yaml#typography.font_family`, the face every Stitch mockup is set in (`kmp-designsystem-gen --project-theme`).

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

_31 type(s), 235 function(s)/property(ies); 264 carry KDoc at source; 6 authored example(s); 71 live call site(s)._
<!-- api-docs:end -->
