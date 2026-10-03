# `core-base/datastore`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_DATASTORE.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 17 Kotlin files (2 test) · source sets: `androidMain`, `commonMain`, `commonTest`, `desktopMain`, `desktopTest`, `jsCommonMain`, `nativeMain`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

No module dependencies — this is a leaf.

**Consumed by** 3 module(s): `core-base/data`, `core/data`, `core/datastore`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_DATASTORE.md`.

## Principal types

- **`CachedSecureSettings`** — A [Settings] view over an already-decrypted in-memory map.
- **`SecureBlobStorage`** — Seam over the browser key-value store.
- **`SecureSettingsFactory`** — Platform-specific factory that creates an encrypted [Settings] instance.
- **`SecureStoreCore`** — The web secure store's behaviour, independent of the browser.
- **`SyncStatePersister`** — Persistence seam for the [Synchronizer]'s per-feature last-synced version map.
- **`WebSecureStore`** — Process-wide backing store for web secure settings.

Undocumented: `BrowserStorage`, `ChangeListVersions`, `SecureCipher`, `SettingsSyncStatePersister`, `WebCryptoCipher`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

2 test file(s) under `core-base/datastore/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Seam over the browser key-value store.
 *
 * It exists so the store's LOGIC — legacy adoption, prefix discrimination, degraded mode — carries
 * no platform dependency. That is what lets this file live in commonMain and be tested on desktop,
 * iOS and JS alike, rather than only where a browser exists. The browser-backed implementation is
 * `BrowserStorage` in jsCommonMain.
 */
internal interface SecureBlobStorage {
    fun get(key: String): String?
    fun set(key: String, value: String)
    fun remove(key: String)
    fun keys(): List<String>
}

/** Seam over WebCrypto. [warmUp] returns false when crypto is unavailable rather than throwing. */
internal interface SecureCipher {
    suspend fun warmUp(): Boolean
    suspend fun encrypt(plaintext: String): String
    suspend fun decrypt(ciphertext: String): String
}
```

Source: [`src/commonMain/kotlin/kpt/core/base/datastore/SecureStoreCore.kt`](../../../../core-base/datastore/src/commonMain/kotlin/kpt/core/base/datastore/SecureStoreCore.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
