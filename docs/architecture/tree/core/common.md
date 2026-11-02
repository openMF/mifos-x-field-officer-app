# `core/common`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_COMMON.md`
> **Measured:** 3 Kotlin files, 0 test files

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/common sha=c8f4f67d7cfac4680b9f33ef551d8a7c909a66d3 -->
## API reference

_Generated from `core/common` at tree `c8f4f67d7cfa` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/common/src/commonMain/kotlin/kpt/core/common/format/FormatDate.kt`

```kotlin
fun formatDate(millis: Long): String
```
Formats an epoch-millis timestamp for display in the device's locale.

### `core/common/src/commonMain/kotlin/kpt/core/common/format/FormatDuration.kt`

```kotlin
fun formatTimeAgo(instant: Instant?): String?
```
Returns a human-readable "X ago" label for a past `instant`, e.g. "just now", "5m ago", "2h ago", "3d ago". Returns `null` when `instant` is null.

### `core/common/src/commonMain/kotlin/kpt/core/common/format/FormatNumber.kt`

```kotlin
fun Double.formatDecimal(places: Int): String
```
Formats to exactly `places` decimals, zero-padded so a column of figures stays aligned. Hand-rolled because `String.format` is JVM-only — it fails to link on Kotlin/JS and Native, so a shared UI cannot use it.

```kotlin
fun Double.formatGrouped(places: Int): String
```
Formats with `,` thousands separators and exactly `places` decimals — the money/quantity form.

```kotlin
fun Long.formatGrouped(): String
```
Formats a whole number with `,` thousands separators — counts and sizes, no fractional part.

---

_0 type(s), 5 function(s)/property(ies); 5 carry KDoc at source; 0 authored example(s); 0 live call site(s)._
<!-- api-docs:end -->
