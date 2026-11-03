# `core/common`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_COMMON.md`
> **Measured:** 40 Kotlin files, 1 test files

**Defines annotations:** `@Dispatcher`

## Principal types

`AppDispatchers`, `AppLanguage`, `CommonFileUtils`, `Constants`, `CurrencyFormatter`, `DatabaseFetchException`, `DateFormatPattern`, `DateHelper`, `Dispatcher`, `FileKitUtil`, `FileUtils`, `LanguageHelper`, `MapDeserializer`, `Resource`  …and 3 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/common sha=70d784d04a487ddb2e5b1b4404032c8dbd6c44cf -->
## API reference

_Generated from `core/common` at tree `70d784d04a48` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/common/src/commonMain/kotlin/kpt/core/common/enums/AppLanguage.kt`

```kotlin
enum class AppLanguage(val code: String, val displayName: String)
```
_No KDoc at source._

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

### `core/common/src/commonMain/kotlin/kpt/core/common/model/user/Role.kt`

```kotlin
data class Role(
```
Created by ishankhanna on 09/02/14.

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreen.kt:443</code></summary>

```kotlin
                    .shadow(CARD_SHADOW, CircleShape)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onProfile)
                    .semantics { contentDescription = profileLabel }
                    .testTag(TestTags.Home.PROFILE)
                    .background(c.primaryContainer, CircleShape)
                    .border(0.5f.tw, c.surfaceContainerLowest, CircleShape),
```

</details>

### `core/common/src/commonMain/kotlin/kpt/core/common/model/user/User.kt`

```kotlin
class User
```
_No KDoc at source._

<details><summary>Used in the template — <code>feature/home/src/commonMain/kotlin/kpt/feature/home/dashboard/HomeDashboardScreenPreview.kt:95</code></summary>

```kotlin
        """{"developerMessage":"The user associated with credentials passed on this request does not have sufficient privileges to perform this action.",""" +
        """"httpStatusCode":"403","defaultUserMessage":"Insufficient privileges to perform this action.",""" +
        """"userMessageGlobalisationCode":"error.msg.not.authorized","errors":[{"developerMessage":"User has no authority to READ collectionsheets",""" +
        """"defaultUserMessage":"User has no authority to READ collectionsheets","userMessageGlobalisationCode":"error.msg.not.authorized","parameterName":"id","args":[]}]}""" + "\"",
)

private fun content(card: HomeCard): ScreenState<HomeCard> = ScreenState.Content(card)
```

</details>

### `core/common/src/commonMain/kotlin/kpt/core/common/network/AppDispatchers.kt`

```kotlin
annotation class Dispatcher(val mifosDispatcher: AppDispatchers)
```
_No KDoc at source._

```kotlin
enum class AppDispatchers
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/kpt/core/common/network/di/DispatchersModule.android.kt:22</code></summary>

```kotlin
actual val ioDispatcherModule: Module
    get() = module {
        single<CoroutineDispatcher>(named(AppDispatchers.IO.name)) { Dispatchers.IO }
    }

actual val ioContextModule: Module
    get() = module {
```

</details>

### `core/common/src/commonMain/kotlin/kpt/core/common/network/di/DispatchersModule.kt`

```kotlin
val DispatchersModule = module
```
_No KDoc at source._

```kotlin
expect val ioDispatcherModule: Module
```
_No KDoc at source._

```kotlin
expect val ioContextModule: Module
```
_No KDoc at source._

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/Constants.kt`

```kotlin
object Constants
```
Created by satya on 13/04/14.

<details><summary>Used in the template — <code>feature/activate/src/commonMain/kotlin/kpt/feature/activate/navigation/ActivateRoute.kt:34</code></summary>

```kotlin

fun NavController.navigateToActivateClient(clientId: Int, navOptions: NavOptions? = null) =
    navigate(ActivateRoute(clientId, Constants.ACTIVATE_CLIENT), navOptions)

fun NavController.navigateToActivateCenter(centerId: Int, navOptions: NavOptions? = null) =
    navigate(ActivateRoute(centerId, Constants.ACTIVATE_CENTER), navOptions)
```

</details>

- `const val DATABASE_NAME = "mifos_database"`
- `const val INSTANCE_URL_KEY = "instanceURL"`
- `const val INSTANCE_DOMAIN_KEY = "instanceDomain"`
- `const val INSTANCE_PORT_KEY = "instancePort"`
- `const val TENANT_IDENTIFIER_KEY = "tenant identifier"`
- `const val PROTOCOL_HTTP = "http://"`
- `const val PROTOCOL_HTTPS = "https://"`
- `const val API_PATH = "/mifosng-provider/api/v1"`
- `const val ENTITY_TYPE_CLIENTS = "clients"` — Entity Type, Like Clients, Groups, Staff, Loans, Savings and Client Identifiers
- `const val ENTITY_TYPE_GROUPS = "groups"`
- `const val ENTITY_TYPE_LOANS = "loans"`
- `const val ENTITY_TYPE_SAVINGS = "savings"`
- `const val ENTITY_TYPE_STAFF = "staff"`
- `const val ENTITY_TYPE_CLIENT_IDENTIFIERS = "client_identifiers"`
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

- `fun format(balance: Double?, currencyCode: String?, maximumFractionDigits: Int?): String`

```kotlin
fun <T> List<T>.toArrayList(): ArrayList<T>
```
_No KDoc at source._

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/DatabaseFetchException.kt`

```kotlin
class DatabaseFetchException(
```
_No KDoc at source._

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
_No KDoc at source._

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

- `const val FULL_MONTH = "dd MM yyyy"` — /* Copyright 2024 Mifos Initiative This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
- `const val SHORT_MONTH = "dd-MM-yyyy"` — /* Copyright 2024 Mifos Initiative This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
- `const val MONTH_FORMAT = "dd MMMM"`
- `fun getDateAsString(integersOfDate: List<Int>): String` — the result string uses the list given in a reverse order ([x, y, z] results in "z y x")
- `val stringBuilder = StringBuilder()`
- `fun getDateAsString(integersOfDate: List<Long>, pattern: String): String`
- `fun getMonthNumber(monthName: String): Int`
- `fun getDateAsList(date: String): List<Int>`
- `val dateList = date.split(" ")`
- `val day = dateList[0].toInt()`
- `val month = getMonthNumber(dateList[1])`
- `val year = dateList[2].toInt()`
- `fun getSpecificFormat(format: String, dateString: String): String` — This Method converting the dd-MM-yyyy format type date string into dd MMMM yyyy
- `val pickerFormat = shortMonthFormat`
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

- `fun pickFile(`
- `val file = FileKit.openFilePicker(`
- `fun pickImage(`
- `val image = FileKit.openFilePicker(`
- `suspend fun pickDirectory(): PlatformFile? = platformPickDirectory()`
- `fun writeFileToCache(` — Android filesDir: Maps to context.filesDir, which is the app’s private internal storage cacheDir: Maps to context.cacheDir, which is the app’s private cache directory databasesDir: Maps to a databases subdirectory in the app’s internal storage iOS filesDir: Maps to the app’s Documents directory, which is backed up with iCloud cacheDir: Maps to the app’s Caches directory, which isn’t backed up and may be cleared by the system databasesDir: Maps to a databases subdirectory in the app’s Documents directory macOS filesDir: Maps to ~/Library/Application Support/<app-id>/, requiring FileKit initialization with an app ID cacheDir: Maps to ~/Library/Caches/<app-id>/ databasesDir: Maps to a databases subdirectory in the application support directory JVM (Desktop) filesDir: Maps to platform-specific app data locations: Linux: ~/.local/share/<app-id>/ macOS: ~/Library/Application Support/<app-id>/ Windows: %APPDATA%/<app-id>/ cacheDir: Maps to platform-specific cache locations: Linux: ~/.cache/<app-id>/ macOS: ~/Library/Caches/<app-id>/ Windows: %LOCALAPPDATA%/<app-id>/Cache/ databasesDir: Maps to a databases subdirectory within filesDir
- `fun writeFileToApplicationPrivateInternalStorage(`
- `fun writeFileToApplicationInternalStorage(`
- `fun writeToSelectedDirectory(`
- `suspend fun deleteFile(`
- `fun takePhoto(): Flow<Result<PlatformFile?>> = platformTakePhoto()`

```kotlin
expect suspend fun platformPickDirectory(): PlatformFile?
```
_No KDoc at source._

```kotlin
expect fun platformWriteFileToCache(
```
_No KDoc at source._

```kotlin
expect fun platformWriteFileToApplicationPrivateInternalStorage(
```
_No KDoc at source._

```kotlin
expect fun platformWriteFileToApplicationInternalStorage(
```
_No KDoc at source._

```kotlin
expect fun platformWriteToSelectedDirectory(
```
_No KDoc at source._

```kotlin
expect suspend fun platformDeleteFile(
```
_No KDoc at source._

```kotlin
expect fun platformTakePhoto(): Flow<Result<PlatformFile?>>
```
_No KDoc at source._

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/FileUtils.kt`

```kotlin
interface FileUtils
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/kpt/core/common/utils/FileUtils.android.kt:12</code></summary>

```kotlin
package kpt.core.common.utils

actual fun createPlatformFileUtils(): FileUtils = CommonFileUtils()
```

</details>

- `suspend fun writeInputStreamDataToFile(inputStream: ByteArray, filePath: String): Boolean`
- `val logger = Logger.withTag("FileUtils")`

```kotlin
expect fun createPlatformFileUtils(): FileUtils
```
_No KDoc at source._

```kotlin
class CommonFileUtils : FileUtils
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/common/src/androidMain/kotlin/kpt/core/common/utils/FileUtils.android.kt:12</code></summary>

```kotlin
package kpt.core.common.utils

actual fun createPlatformFileUtils(): FileUtils = CommonFileUtils()
```

</details>

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/LanguageHelper.kt`

```kotlin
object LanguageHelper
```
_No KDoc at source._

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/MapDeserializer.kt`

```kotlin
object MapDeserializer : KSerializer<Map<String, Any>>
```
_No KDoc at source._

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/datatable/entity/DataTablePayload.kt:40</code></summary>

```kotlin
    val registeredTableName: String? = null,

    @Serializable(with = MapDeserializer::class)
    @Contextual
    val data: Map<
        String,
        @Contextual
```

</details>

- `val jsonElement = (decoder as? JsonDecoder)?.decodeJsonElement()`
- `val result = read(jsonElement)`
- `fun read(jsonElement: JsonElement): Any?`
- `val list = jsonElement.jsonArray.map { read(it) }`
- `val num = jsonElement.double`
- `val jsonObject = JsonObject(value.mapValues { (_, v) -> write(v) })`

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/Resource.kt`

```kotlin
sealed class Resource<T>(val data: T? = null, val message: String? = null)
```
Created by Aditya Gupta on 11/02/24.

<details><summary>Used in the template — <code>feature/about/src/commonMain/kotlin/kpt/feature/about/ui/AboutScreen.kt:116</code></summary>

```kotlin
) {
    AppScaffold(
        title = stringResource(Res.string.feature_about_title),
        onBackPressed = { onAction(AboutAction.NavigateBack) },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
```

</details>

```kotlin
fun <T> Flow<T>.asResourceFlow(): Flow<Resource<T>> =
```
_No KDoc at source._

### `core/common/src/commonMain/kotlin/kpt/core/common/utils/Utils.kt`

```kotlin
object Utils
```
_No KDoc at source._

- `fun getStringOfDate(dateObj: List<Int?>): String`
- `val year = dateObj.getOrNull(0)`
- `val month = dateObj.getOrNull(1)`
- `val day = dateObj.getOrNull(2)`
- `val date = LocalDate(year, month, day)`
- `val monthFormatted = date.month.name.lowercase().replaceFirstChar { it.uppercase() }`

---

_17 type(s), 72 function(s)/property(ies); 17 carry KDoc at source; 0 authored example(s); 12 live call site(s)._
<!-- api-docs:end -->
