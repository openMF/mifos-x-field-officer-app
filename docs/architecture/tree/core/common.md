# `core/common`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_COMMON.md`
> **Measured:** 21 Kotlin files, 1 test files

## Principal types

`AppLanguage`, `Constants`, `CurrencyFormatter`, `DateFormatPattern`, `DateHelper`, `FileKitUtil`, `MapDeserializer`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/common sha=86c207d5d3602bb4f50cf759a8b69a2fbaeb3fc2 -->
## API reference

_Generated from `core/common` at tree `86c207d5d360` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/common/src/commonMain/kotlin/kpt/core/common/enums/AppLanguage.kt`

```kotlin
enum class AppLanguage(val code: String, val displayName: String)
```
The languages the app can be shown in.

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

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/Constants.kt`

```kotlin
object Constants
```
Wire values and keys shared across features: Fineract entity names, search kinds, date formats, data tables.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/navigation/ActivateRoute.kt:38</code></summary>

```kotlin
/** Opens the activation screen for client [clientId]. */
fun NavController.navigateToActivateClient(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ActivateRoute(clientId, Constants.ACTIVATE_CLIENT), navOptions)

/** Opens the activation screen for centre [centerId]. */
fun NavController.navigateToActivateCenter(centerId: Int, navOptions: NavOptions? = null) =
    navigate(ActivateRoute(centerId, Constants.ACTIVATE_CENTER), navOptions)
```

</details>

- `const val ENTITY_TYPE_CLIENTS = "clients"` — Fineract's entity type for clients, as used in document and identifier paths.
- `const val ENTITY_TYPE_GROUPS = "groups"` — Fineract's entity type for groups.
- `const val ENTITY_TYPE_LOANS = "loans"` — Fineract's entity type for loans.
- `const val ENTITY_TYPE_SAVINGS = "savings"` — Fineract's entity type for savings accounts.
- `const val ENTITY_TYPE_CLIENT_IDENTIFIERS = "client_identifiers"` — Fineract's entity type for client identifiers (identity documents).
- `const val SEARCH_ENTITY_CLIENT = "CLIENT"` — The search API's entity type for clients.
- `const val SEARCH_ENTITY_GROUP = "GROUP"` — The search API's entity type for groups.
- `const val SEARCH_ENTITY_LOAN = "LOAN"` — The search API's entity type for loans.
- `const val SEARCH_ENTITY_SAVING = "SAVING"` — The search API's entity type for savings accounts.
- `const val SEARCH_ENTITY_CENTER = "CENTER"` — The search API's entity type for centres.
- `const val CLIENT_ID = "clientId"` — The `clientId` key in request bodies and navigation arguments.
- `const val CLIENTS = "clients"` — Fineract's `clients` resource name.
- `const val SAVINGS_ACCOUNT_TYPE = "savingsAccountType"` — The `savingsAccountType` key in navigation arguments.
- `const val DATE_FORMAT = "dateFormat"` — The `dateFormat` key Fineract expects beside every date in a request body.
  _…more members; read the file._

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/CurrencyFormatter.kt`

```kotlin
expect object CurrencyFormatter
```
Money, formatted for display.

<details><summary>Used in the template — <code>feature/charge/src/commonMain/kotlin/kpt/feature/charge/ui/ChargeUi.kt:44</code></summary>

```kotlin

internal fun Charges.toUi(): ChargeUi {
    fun money(value: Double?) = value?.let { CurrencyFormatter.format(it, currency?.code, currency?.decimalPlaces) }
        ?: PLACEHOLDER
    return ChargeUi(
        id = id,
        definitionId = chargeId?.toString() ?: PLACEHOLDER,
```

</details>

- `fun format(balance: Double?, currencyCode: String?, maximumFractionDigits: Int?): String` — Formats `balance` as money in `currencyCode` with at most `maximumFractionDigits` decimals; a null balance formats as zero and an unusable code falls back to the locale's currency.

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/DateFormatPattern.kt`

```kotlin
enum class DateFormatPattern(val pattern: String)
```
Enum representing standard date format patterns used in Fineract API. The Fineract API accepts dates as strings with a dateFormat parameter that specifies how the date should be parsed.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/editdetails/ClientEditDetailsViewModel.kt:227</code></summary>

```kotlin
            val payload = state.form.toPayload(
                locale = ApiDateFormatter.LOCALE,
                dateFormat = DateFormatPattern.FULL_MONTH,
            )

            mutableStateFlow.value = when (
                val result = commands.updateClient(state.clientId, payload)
```

</details>

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/DateHelper.kt`

```kotlin
object DateHelper
```
Date conversions between Fineract's `[year, month, day]` arrays, display strings and epoch millis.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/ui/ActivateScreen.kt:158</code></summary>

```kotlin

            AppDatePickerTextField(
                value = DateHelper.getDateAsStringFromLong(activateDate),
                label = stringResource(Res.string.feature_activate_activation_date),
                openDatePicker = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth().testTag(ActivateTestTags.DATE),
            )
```

</details>

- `const val FULL_MONTH = "dd MM yyyy"` — This is the full month format for the date picker. "dd MM yyyy" is the format of the date picker.
- `const val SHORT_MONTH = "dd-MM-yyyy"` — This is the short month format for the date picker. "dd-MM-yyyy" is the format of the date picker.
- `fun getDateAsString(integersOfDate: List<Int>): String` — the result string uses the list given in a reverse order ([x, y, z] results in "z y x")
- `val stringBuilder = StringBuilder()`
- `fun getDateAsString(integersOfDate: List<Long>, pattern: String): String` — Formats a Fineract `[year, month, day]` date (as longs) with `pattern`.
- `val dateList = date.split(" ")`
- `val day = dateList[0].toInt()`
- `val month = getMonthNumber(dateList[1])`
- `val year = dateList[2].toInt()`
- `fun getSpecificFormat(format: String, dateString: String): String` — This Method converting the dd-MM-yyyy format type date string into dd MMMM yyyy
- `val pickerFormat = shortMonthFormat`
- `val finalFormat = LocalDateTime.Format { byUnicodePattern(format) }`
- `val pickerFormat = LocalDateTime.Format { byUnicodePattern(currentFormat) }`
- `val finalFormat = LocalDateTime.Format { byUnicodePattern(requiredFormat) }`
  _…more members; read the file._

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/FileKitUtil.kt`

```kotlin
object FileKitUtil
```
Do not pass the path or absolute path you get by using any picker, to PlatformFile class to create an instance of PlatformFile. It won't work. Use PlatformFile object returned by pickers directly.

<details><summary>Used in the template — <code>feature/client/src/commonMain/kotlin/kpt/feature/client/adddocument/ClientAddDocumentViewModel.kt:102</code></summary>

```kotlin
        viewModelScope.launch {
            mutableStateFlow.value = state.copy(isSourceSheetOpen = false)
            val picker = if (fromGallery) FileKitUtil.pickImage() else FileKitUtil.pickFile()
            val file = picker.first().getOrElse {
                Logger.e(tag = TAG, throwable = it) { "document picker failed" }
                mutableStateFlow.value = state.copy(error = AddDocumentError.PickFailed)
                return@launch
```

</details>

- `fun pickFile(` — Lets the user pick one file with one of `extensions`; emits the file, or null when the picker was dismissed, wrapped in a `Result` that carries a picker failure instead of throwing it.
- `val file = FileKit.openFilePicker(`
- `fun pickImage(): Flow<Result<PlatformFile?>> = flow` — Lets the user pick one image; emits it, or null when dismissed, as a `Result` like `pickFile`.
- `val image = FileKit.openFilePicker(`
- `fun takePhoto(): Flow<Result<PlatformFile?>> = platformTakePhoto()` — Opens the camera for one photo; platforms without a camera emit a failure.

```kotlin
expect fun platformTakePhoto(): Flow<Result<PlatformFile?>>
```
The platform's camera capture behind `FileKitUtil.takePhoto`.

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/MapDeserializer.kt`

```kotlin
object MapDeserializer : KSerializer<Map<String, Any>>
```
Deserialises an arbitrary JSON object into a `Map<String, Any>` of plain values (strings, numbers, booleans, nested lists and maps).

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/datatable/entity/DataTablePayload.kt:46</code></summary>

```kotlin

    /** The data. */
    @Serializable(with = MapDeserializer::class)
    @Contextual
    val data: Map<
        String,
        @Contextual
```

</details>

- `val jsonElement = (decoder as? JsonDecoder)?.decodeJsonElement()`
- `val result = read(jsonElement)`
- `val list = jsonElement.jsonArray.map { read(it) }`
- `val num = jsonElement.double`
- `val jsonObject = JsonObject(value.mapValues { (_, v) -> write(v) })`

---

_7 type(s), 45 function(s)/property(ies); 36 carry KDoc at source; 0 authored example(s); 6 live call site(s)._
<!-- api-docs:end -->
