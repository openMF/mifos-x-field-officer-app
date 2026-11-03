# `core/domain`

> **Layer:** core — fork-owned; a codegen target
> **Corpus surface:** `CORE_DOMAIN.md`
> **Measured:** 3 Kotlin files, 1 test files

## Principal types

`CredentialRules`, `ServerConfigRules`, `ValidationFailure`, `ValidationResult`

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._

<!-- api-docs:begin module=core/domain sha=ee7c50a817ea88cd83f94740a01bcce45e6df793 -->
## API reference

_Generated from `core/domain` at tree `ee7c50a817ea` by `scripts/docs/api-docs-gen.sh`._
_Do not hand-edit inside this block — re-run the generator. Authored prose belongs outside it._

### `core/domain/src/commonMain/kotlin/kpt/core/domain/validation/ValidationResult.kt`

```kotlin
sealed interface ValidationFailure
```
Why a value failed validation — a TYPED reason, never a message. The screen maps a failure to a localized string; the rule does not.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigScreen.kt:398</code></summary>

```kotlin
    onChange: (String) -> Unit,
    label: String,
    failure: ValidationFailure?,
    tag: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    AppOutlinedTextField(
```

</details>

```kotlin
sealed interface ValidationResult
```
The outcome of one rule.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigViewModel.kt:193</code></summary>

```kotlin
    private fun MutableMap<ServerConfigField, ValidationFailure>.putFailure(
        field: ServerConfigField,
        result: ValidationResult,
    ) {
        if (result is ValidationResult.Invalid) put(field, result.failure)
    }
}
```

</details>

```kotlin
fun firstFailureOrNull(vararg results: ValidationResult): ValidationFailure? =
```
The first failure among `results`, or null when all are `ValidationResult.Valid`.

### `core/domain/src/commonMain/kotlin/kpt/core/domain/validation/ValidationRules.kt`

```kotlin
object CredentialRules
```
Sign-in credential rules.

<details><summary>Used in the template — <code>feature/signin/src/commonMain/kotlin/kpt/feature/signin/ui/SignInViewModel.kt:258</code></summary>

```kotlin

    private fun validate(username: String, password: String): SignInError? = when {
        CredentialRules.username(username) is ValidationResult.Invalid -> SignInError.UsernameTooShort
        CredentialRules.password(password) is ValidationResult.Invalid -> SignInError.PasswordTooShort
        else -> null
    }
}
```

</details>

- `const val MIN_USERNAME_LENGTH: Int = 4`
- `const val MIN_PASSWORD_LENGTH: Int = 6`
- `fun username(value: String): ValidationResult = when`
- `fun password(value: String): ValidationResult = when`

```kotlin
object ServerConfigRules
```
Fineract server-configuration rules — the five fields the server-config screen edits.

<details><summary>Used in the template — <code>feature/settings/src/commonMain/kotlin/kpt/feature/settings/serverconfig/ServerConfigViewModel.kt:184</code></summary>

```kotlin
     */
    private fun validate(config: ServerConfig): Map<ServerConfigField, ValidationFailure> = buildMap {
        putFailure(ServerConfigField.Protocol, ServerConfigRules.protocol(config.protocol))
        putFailure(ServerConfigField.EndPoint, ServerConfigRules.endpoint(config.endPoint))
        putFailure(ServerConfigField.ApiPath, ServerConfigRules.apiPath(config.apiPath))
        putFailure(ServerConfigField.Port, ServerConfigRules.port(config.port))
        putFailure(ServerConfigField.Tenant, ServerConfigRules.tenant(config.tenant))
```

</details>

- `val PORT_RANGE: IntRange = 1..65535`
- `fun endpoint(value: String): ValidationResult = when`
- `fun port(value: String): ValidationResult`
- `val parsed = value.toIntOrNull()`
- `fun protocol(value: String): ValidationResult = when`
- `fun apiPath(value: String): ValidationResult = when`
- `fun tenant(value: String): ValidationResult = when`

---

_4 type(s), 12 function(s)/property(ies); 5 carry KDoc at source; 0 authored example(s); 4 live call site(s)._
<!-- api-docs:end -->
