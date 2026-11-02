# `core/model`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_MODEL.md`
> **Measured:** 29 Kotlin files, 3 test files

## Principal types

`AlertDirection`, `AmortizationBreakdown`, `AmortizationRow`, `AppOrgLinks`, `AppPrivacyConfig`, `AppSecurityConfig`, `AuthState`, `BillCategory`, `BillReminder`, `CloudTodo`, `CoinDetail`, `CoinMarket`, `Country`, `CountryFlagUtils`  …and 22 more

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/model sha=78206772b94d6d434b91bd24821e153eb2cbe5ac -->
## API reference

_Generated from `core/model` at tree `78206772b94d` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/model/src/commonMain/kotlin/kpt/core/model/alerts/PriceAlert.kt`

```kotlin
data class PriceAlert(
```
A price alert configured by the user for a specific coin.

<details><summary>Used in the template — <code>feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/di/AlertsModule.kt:31</code></summary>

```kotlin
        AlertCreateViewModel(
            repository = get(),
            outbox = get(qualifier = AppOutboxQualifiers.PriceAlert),
        )
    }
}
```

</details>

```kotlin
enum class AlertDirection
```
Which way a price must cross the threshold to fire the alert (above or below).

<details><summary>Used in the template — <code>feature/alerts/src/commonMain/kotlin/kpt/feature/alerts/ui/AlertCreateScreen.kt:107</code></summary>

```kotlin
    onSubmitted: () -> Unit,
    onCoinIdChange: (String) -> Unit,
    onDirectionChange: (AlertDirection) -> Unit,
    onTargetValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit,
    onResume: () -> Unit,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/AmortizationRow.kt`

```kotlin
data class AmortizationRow(
```
A single monthly row in a reducing-balance amortization schedule. Every row satisfies: `payment` = `principal` + `interest` (within floating-point tolerance).

<details><summary>Used in the template — <code>feature/amortization/src/commonMain/kotlin/kpt/feature/amortization/ui/AmortizationScheduleScreen.kt:100</code></summary>

```kotlin

@Composable
internal fun AmortizationTable(rows: List<AmortizationRow>, modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item { ScheduleTableHeader() }
        item { HorizontalDivider(thickness = 1.5.dp) }
        itemsIndexed(rows) { index, row ->
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/BillReminder.kt`

```kotlin
data class BillReminder(
```
A recurring (or one-time) bill the user wants to be reminded about. Purely local — no remote sync, no calendar export. The "reminder" itself is delivered by the in-app notification surface; this record is the declarative configuration.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/di/BillsModule.kt:43</code></summary>

```kotlin
            scheduler = get(),
            billId = billId,
            outbox = get(qualifier = AppOutboxQualifiers.BillReminder),
        )
    } bind EditBillReminderViewModel::class
}
```

</details>

```kotlin
enum class Recurrence
```
How often a bill repeats.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/domain/BillReminderRecurrence.kt:60</code></summary>

```kotlin
        val anchorYear = today.year
        return when (bill.recurrence) {
            Recurrence.ONCE -> {
                // First viable date >= today using current month/year as the anchor.
                val candidate = clampedDate(anchorYear, anchorMonth.number, bill.dueDay)
                if (candidate >= today) candidate else null
            }
```

</details>

```kotlin
enum class BillCategory
```
Coarse spending category — drives icons and dashboard grouping.

<details><summary>Used in the template — <code>feature/bills/src/commonMain/kotlin/kpt/feature/bills/ui/AddOrEditBillReminderScreen.kt:357</code></summary>

```kotlin
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun CategorySection(selected: BillCategory, isSubmitting: Boolean, onChange: (BillCategory) -> Unit) {
    val sp = MaterialTheme.spacing
    AppCard {
        Column(
            modifier = Modifier
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/Loan.kt`

```kotlin
data class Loan(
```
A personal loan tracked by the user — purely local, no remote sync.

<details><summary>Used in the template — <code>feature/amortization/src/commonMain/kotlin/kpt/feature/amortization/ui/AmortizationScheduleViewModel.kt:81</code></summary>

```kotlin
 * Zero-interest edge case: all of each payment is principal, balance reduces linearly.
 */
internal fun computeSchedule(loan: Loan): List<AmortizationRow> {
    val monthlyRate = loan.annualRatePercent / 100.0 / 12.0
    val emi = loan.monthlyPayment
    var balance = loan.principalRemaining
    return buildList {
```

</details>

```kotlin
enum class LoanKind
```
High-level loan category used for grouping, icons, and analytics.

<details><summary>Used in the template — <code>feature/amortization/src/commonTest/kotlin/kpt/feature/amortization/ui/AmortizationScheduleViewModelTest.kt:173</code></summary>

```kotlin
    id = id,
    name = "Test Loan",
    kind = LoanKind.PERSONAL,
    principal = 10_000.0,
    principalRemaining = principalRemaining,
    annualRatePercent = annualRatePercent,
    tenureMonths = monthsRemaining,
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/banking/LoanCalcScenario.kt`

```kotlin
data class LoanCalcScenario(
```
Wizard payload — the serialized snapshot of the loan-calc wizard at the current step. Serializable so the offline-resilient `DraftSubmitHandler` can persist it across process death and the next session can resume in-place.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/di/CalculatorsModule.kt:79</code></summary>

```kotlin
    viewModel { (scenarioId: String?) ->
        LoanCalcWizardViewModel(
            outbox = get(qualifier = AppOutboxQualifiers.LoanCalcScenario),
            repository = get(),
            scenarioIdArg = scenarioId,
        )
    }
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/calc/AmortizationBreakdown.kt`

```kotlin
data class AmortizationBreakdown(
```
One amortization calculation: the per-installment `rows` and the `summary` totals.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/amortizationcalc/AmortizationViewModel.kt:53</code></summary>

```kotlin

    /** The stream backing the CURRENT key — retained so [onRetry] re-runs the live one. */
    private var currentStream: ScreenDataStream<AmortizationBreakdown>? = null

    /**
     * Schedule AND summary as ONE Store-backed `ScreenState`.
     *
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/cloudtodo/CloudTodo.kt`

```kotlin
data class CloudTodo(
```
A cloud-synced todo — the toolkit's MUTABLE (offline-write) Store5 archetype showcase.

<details><summary>Used in the template — <code>feature/cloudtodo/src/commonMain/kotlin/kpt/feature/cloudtodo/ui/CloudTodoScreen.kt:92</code></summary>

```kotlin
        onNavigationIconClick = onBackClick,
        title = stringResource(Res.string.screens_cloudtodo_detail_title),
        modifier = modifier.testTag(TestTags.CloudTodo.SCREEN),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/config/AppOrgLinks.kt`

```kotlin
object AppOrgLinks
```
The fork's own named organisation links — a community chat, a product site, a status page — for whatever surface the fork builds (an About screen, a help sheet). **SoT: `app-profile/app.yaml#org.links`** (`name: url`).

<details><summary>Example</summary>

```kotlin
AppOrgLinks["community"]?.let { url -> LinkRow("Community", url) }
```

</details>

<details><summary>Used in the template — <code>core/model/src/desktopTest/kotlin/kpt/core/model/config/AppOrgLinksTest.kt:24</code></summary>

```kotlin
    @Test
    fun everyDeclaredLinkIsReachableByName() {
        AppOrgLinks.all.forEach { (name, url) -> assertEquals(url, AppOrgLinks[name]) }
    }

    @Test
    fun anUndeclaredNameIsNull() {
```

</details>

- `val all: Map<String, String> get() = links` — Every declared link, in app-profile order.

### `core/model/src/commonMain/kotlin/kpt/core/model/config/AppPrivacyConfig.kt`

```kotlin
object AppPrivacyConfig
```
Consent defaults a fresh install starts with — analytics and crash reporting. **SoT: `app-profile/app.yaml#privacy`.** The block below is GENERATED by `./gradlew syncForkConfig`; change the YAML, not this file.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/PrivacySheet.kt:79</code></summary>

```kotlin
        onChangeCrashReportingConsent = viewModel::updateCrashReportingConsent,
        // No link when the fork has not set org.privacy_url, rather than one that opens nothing.
        onOpenPrivacyPolicy = AppPrivacyConfig.PRIVACY_POLICY_URL.takeIf { it.isNotEmpty() }?.let { url ->
            { urlLauncher.launchInApp(url) }
        },
    )
}
```

</details>

- `const val ANALYTICS_ENABLED_BY_DEFAULT: Boolean = false` — `privacy.analytics_enabled_by_default` — whether a fresh install shares usage analytics.
- `const val CRASH_REPORTING_ENABLED_BY_DEFAULT: Boolean = false` — `privacy.crash_reporting_enabled_by_default` — whether a fresh install sends crash reports.
- `const val PRIVACY_POLICY_URL: String =` — `org.privacy_url` — the privacy policy page, opened in the in-app browser; empty when not set.

### `core/model/src/commonMain/kotlin/kpt/core/model/config/AppSecurityConfig.kt`

```kotlin
object AppSecurityConfig
```
Security defaults a fresh install starts with. **SoT: `app-profile/app.yaml#security`.** The block below is GENERATED by `./gradlew syncForkConfig`; change the YAML, not this file.

<details><summary>Used in the template — <code>core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt:81</code></summary>

```kotlin
            useDynamicColor = false,
            appLanguage = LanguageConfig.DEFAULT,
            isAuthenticated = AppSecurityConfig.SIGNED_IN_BY_DEFAULT,
            isUnlocked = true,
            isPasscodeEnabled = false,
            isBiometricsEnabled = false,
            showOnboarding = false,
```

</details>

- `const val SCREEN_CAPTURE_ALLOWED_BY_DEFAULT: Boolean = false` — `security.screen_capture_allowed_by_default` — whether a fresh install allows screenshots + recording.
- `const val SIGNED_IN_BY_DEFAULT: Boolean = true` — `security.signed_in_by_default` — whether a fresh install starts signed in (false = the app has a login).

### `core/model/src/commonMain/kotlin/kpt/core/model/crypto/CoinMarket.kt`

```kotlin
data class CoinMarket(
```
One row of the coin market list — the fields a list item renders, nothing more.

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinMarketsScreen.kt:118</code></summary>

```kotlin

@Composable
internal fun CoinMarketRow(coin: CoinMarket, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
```

</details>

```kotlin
data class CoinDetail(
```
A single coin's full detail, as shown on its own screen.

<details><summary>Used in the template — <code>feature/crypto/src/commonMain/kotlin/kpt/feature/crypto/ui/CoinDetailViewModel.kt:44</code></summary>

```kotlin

    /** The repository-built stream — the screen renders it directly via `ScreenContent(stream)`. */
    val detail: ScreenDataStream<CoinDetail> = repository.coinDetailStream(
        coinId = coinId,
        scope = viewModelScope,
    )
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/currency/Country.kt`

```kotlin
data class Country(
```
A currency-bearing country: ISO code, display name and the currency it uses.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryPickerScreen.kt:108</code></summary>

```kotlin

@Composable
internal fun CountryRow(country: Country, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/currency/CountryFlagUtils.kt`

```kotlin
object CountryFlagUtils
```
Maps an ISO country code to its flag emoji, by offsetting ASCII letters into the Unicode regional-indicator range.

- `fun detectCountryFromPhoneNumber(phoneNumber: String): Country?` — Get country by phone number (auto-detect)
- `val cleanedNumber = phoneNumber.filter { it.isDigit() || it == '+' }`
- `val sortedCountries = worldCountries.sortedByDescending { it.phoneCode.length }`
- `val mobileRegex = Regex(country.mobilePattern)`
- `val landlineRegex = country.landlinePattern?.let { Regex(it) }`
- `fun getAllCountriesForSelection(): List<Country>` — Get all countries for dropdown/picker
- `fun getCountriesWithPopularFirst(): List<Country>` — Get popular countries first, then alphabetical
- `val popularCodes = listOf("US", "GB", "ES", "FR", "DE", "IT", "CA", "AU")`
- `val popular = worldCountries.filter { it.code in popularCodes }`
- `val others = worldCountries.filter { it.code !in popularCodes }`
- `fun searchCountries(query: String): List<Country>` — Search countries by name or code
- `val lowercaseQuery = query.lowercase()`
- `fun getFlagEmoji(countryCode: String): String?` — Get flag emoji by country code
- `fun getFlagResourceName(countryCode: String): String?` — Get flag resource name by country code

```kotlin
val worldCountries: List<Country> = listOf(
```
The static country/currency catalogue the picker offers. Static because it changes on the order of once a decade and a network round-trip for it would be absurd.

### `core/model/src/commonMain/kotlin/kpt/core/model/currency/ExchangeRates.kt`

```kotlin
data class ExchangeRates(
```
FX rates for one base currency on one day — the `rates` map is quote-code → rate.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/CurrencyRatesScreen.kt:183</code></summary>

```kotlin
    amount: String,
    targetCode: String,
    spotState: ScreenState<ExchangeRates>,
    onAmountChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
```

</details>

```kotlin
data class RateHistoryKey(
```
Store key for a historical series: currency pair plus window length. The window is PART of the key, so widening it is a different key and a full re-fetch. That is the `read_windowed_series` contract — a widened window is not a page append.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/RateHistoryViewModel.kt:34</code></summary>

```kotlin

    private val keyFlow = stateFlow.map { local ->
        RateHistoryKey(from = "USD", to = local.targetCurrency, days = local.periodDays)
    }.distinctUntilChanged()

    private val stream = currencyRepository.rateHistoryStream(
        keyFlow = keyFlow,
```

</details>

```kotlin
data class RateHistory(
```
A historical FX series for one pair over a date range.

<details><summary>Used in the template — <code>feature/currency-rates/src/commonMain/kotlin/kpt/feature/currencyrates/ui/RateHistoryScreen.kt:81</code></summary>

```kotlin

    Scaffold(
        modifier = modifier.testTag(TestTags.RateHistory.ROOT),
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Row(
```

</details>

```kotlin
data class RatePoint(
```
One (date, rate) sample within a `RateHistory`.

<details><summary>Used in the template — <code>core/database/src/commonMain/kotlin/kpt/core/database/currency/mapper/RateHistoryEntityMapper.kt:40</code></summary>

```kotlin
    endDate = endDate,
    rates = Json.decodeFromString<List<RatePointPair>>(ratesJson)
        .map { RatePoint(it.date, it.value) },
)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/economic/Country.kt`

```kotlin
data class Country(
```
Country reference for the Banking Utility Toolkit's macro-indicator screens. Distinct from `kpt.core.model.currency.Country` which is a phone-number-formatting model.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryPickerScreen.kt:108</code></summary>

```kotlin

@Composable
internal fun CountryRow(country: Country, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/economic/InterestRateSeries.kt`

```kotlin
data class InterestRateSeries(
```
Domain representation of an interest-rate time series sourced from FRED (Federal Reserve Economic Data). The default consumer is the Banking Utility Toolkit's "B7 Interest Rate Tracker" screen.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/demo/ui/HomeDashboardViewModelTest.kt:227</code></summary>

```kotlin
        key: InterestRateSeriesKey,
        scope: CoroutineScope,
    ): ScreenDataStream<InterestRateSeries> = screenDataStreamForTesting(
        state = MutableStateFlow(ScreenState.Loading),
        refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 64)
            .also { trigger -> trigger.onEach {}.launchIn(scope) },
    )
```

</details>

```kotlin
data class RateObservation(
```
Single observation in an interest-rate time series.

<details><summary>Used in the template — <code>feature/home/src/commonTest/kotlin/kpt/feature/home/demo/ui/HomeViewModelTest.kt:372</code></summary>

```kotlin
        current = current,
        unit = "%",
        observations = listOf(RateObservation(LocalDate(2026, 5, 23), current)),
        source = "FRED",
    )

    // endregion
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/economic/MacroIndicator.kt`

```kotlin
data class MacroIndicator(
```
Domain representation of a country-level macro indicator sourced from the World Bank Open Data API. The default consumer is the Banking Utility Toolkit's "B8 Country Macro Snapshot" screen.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreenPreview.kt:33</code></summary>

```kotlin
 */

private fun indicator(kind: IndicatorKind, value: Double) = MacroIndicator(
    countryCode = "US",
    countryName = "United States",
    indicator = kind,
    observations = listOf(
```

</details>

```kotlin
data class IndicatorObservation(
```
Single year-level macro observation.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/ui/CountryMacroScreenPreview.kt:38</code></summary>

```kotlin
    indicator = kind,
    observations = listOf(
        IndicatorObservation(year = 2024, value = value),
        IndicatorObservation(year = 2025, value = value * 1.02),
    ),
)
```

</details>

```kotlin
enum class IndicatorKind(val worldBankCode: String)
```
Macro indicators surfaced by the toolkit. Each kind maps to a stable World Bank indicator code via `worldBankCode`.

<details><summary>Used in the template — <code>feature/macro/src/commonMain/kotlin/kpt/feature/macro/di/MacroModule.kt:34</code></summary>

```kotlin
        CountryMacroViewModel(initialCountryCode = countryCode, repository = get())
    }
    viewModel { (countryCode: String, kind: IndicatorKind) ->
        MacroIndicatorDetailViewModel(
            countryCode = countryCode,
            indicatorKind = kind,
            repository = get(),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/emi/EmiResult.kt`

```kotlin
data class EmiResult(
```
Output of an EMI calculation: the monthly instalment plus the totals it implies.

<details><summary>Used in the template — <code>feature/calculators/src/commonMain/kotlin/kpt/feature/calculators/comparison/LoanComparisonScreen.kt:193</code></summary>

```kotlin
    index: Int,
    scenario: LoanScenario,
    result: EmiResult?,
    isCheapest: Boolean,
    onChange: (LoanScenario) -> Unit,
) {
    val sp = MaterialTheme.spacing
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/profile/ProfileInfo.kt`

```kotlin
data class ProfileInfo(
```
What the profile screen displays. In the template this carries only the app's display name — the demo profile is a local, signed-out placeholder.

<details><summary>Used in the template — <code>feature/profile/src/commonMain/kotlin/kpt/feature/profile/demo/ProfileDemoBody.kt:66</code></summary>

```kotlin
@Composable
private fun ProfileDemoContent(
    profile: ProfileInfo,
    modifier: Modifier = Modifier,
) {
    val sp = MaterialTheme.spacing
    Column(
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/AuthState.kt`

```kotlin
sealed class AuthState
```
Models high level auth state for the application.

### `core/model/src/commonMain/kotlin/kpt/core/model/user/DarkThemeConfig.kt`

```kotlin
enum class DarkThemeConfig(val configName: String, val osValue: Int)
```
The user's dark-mode preference: follow the system, or force light/dark. `osValue` maps to the platform's own night-mode constant, so the choice can be handed straight to the OS rather than re-interpreted per platform.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsDialog.kt:69</code></summary>

```kotlin
        onChangeThemeBrand = viewModel::updateThemeBrand,
        onChangeDynamicColorPreference = viewModel::updateDynamicColorPreference,
        onChangeDarkThemeConfig = viewModel::updateDarkThemeConfig,
    )
}

/** Settings dialog over explicit state — the testable overload. */
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/LanguageConfig.kt`

```kotlin
enum class LanguageConfig(
```
Every language the app can be switched to, in the user's OWN language. GENERATED from core/registries/LOCALE_REGISTRY.yaml by `core/scripts/language-picker-sync.sh --write` — DO NOT HAND-EDIT.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/LanguageDialog.kt:64</code></summary>

```kotlin
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onChangeLanguage: (language: LanguageConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/ThemeBrand.kt`

```kotlin
enum class ThemeBrand(val brandName: String)
```
The selected colour brand. A fork extends this to offer its own palettes.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/SettingsDialog.kt:67</code></summary>

```kotlin
        settingsState = settingsState,
        onRetry = viewModel::onRetry,
        onChangeThemeBrand = viewModel::updateThemeBrand,
        onChangeDynamicColorPreference = viewModel::updateDynamicColorPreference,
        onChangeDarkThemeConfig = viewModel::updateDarkThemeConfig,
    )
}
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UnlockType.kt`

```kotlin
enum class UnlockType
```
How the user unlocks the app — passcode or biometric.

### `core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt`

```kotlin
data class UserData(
```
Everything the app persists about the current user — theme, language, onboarding progress and the lock state.

<details><summary>Used in the template — <code>feature/settings/src/commonTest/kotlin/kpt/feature/settings/SettingsViewModelTest.kt:78</code></summary>

```kotlin
     */
    private class FakeUserDataRepository(
        private val initial: UserData,
        private val stateFlowOverride: Flow<ScreenState<UserData>>? = null,
        private val refreshTrigger: MutableSharedFlow<Unit> = MutableSharedFlow(extraBufferCapacity = 1),
    ) : UserDataRepository {
        val current = MutableStateFlow(initial)
```

</details>

### `core/model/src/commonMain/kotlin/kpt/core/model/watchlist/WatchlistItem.kt`

```kotlin
data class WatchlistItem(
```
Domain model for a personal-watchlist row (the `read_local_list` demo).

<details><summary>Used in the template — <code>feature/add-to-watchlist/src/commonTest/kotlin/kpt/feature/addtowatchlist/ui/AddToWatchlistViewModelTest.kt:118</code></summary>

```kotlin
    }

    override fun watchlistStream(scope: CoroutineScope): ScreenDataStream<List<WatchlistItem>> =
        error("watchlistStream is the read-side feature's concern; not used by AddToWatchlistViewModel")
}
```

</details>

---

_37 type(s), 21 function(s)/property(ies); 50 carry KDoc at source; 1 authored example(s); 34 live call site(s)._
<!-- api-docs:end -->
