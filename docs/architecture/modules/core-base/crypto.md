# `core-base/crypto`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_CRYPTO.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 22 Kotlin files (4 test) · source sets: `androidMain`, `commonMain`, `desktopMain`, `jsCommonMain`, `jsCommonTest`, `jsMain`, `jsTest`, `nativeMain`, `wasmJsMain`, `wasmJsTest`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

No module dependencies — this is a leaf.

**Consumed by** 1 module(s): `core/database`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_CRYPTO.md`.

## Principal types

- **`FieldEncryptor`** — Platform-specific AES-256-GCM field encryption for sensitive data.
- **`SecureKeyProvider`** — Platform-specific secure key storage and retrieval.
- **`SecureRandom`** — Platform-specific cryptographically secure random number generator.
- **`WebSecureCrypto`** — Web AES-GCM backed by WebCrypto, with a **non-extractable** key held in IndexedDB.

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

4 test file(s) under `core-base/crypto/src/commonTest/`. 
Shared idiom: `CORE_TESTING.md`.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Platform-specific AES-256-GCM field encryption for sensitive data.
 *
 * Encrypts individual fields BEFORE they are stored in Room or Settings.
 * Keys are managed by [SecureKeyProvider] using hardware-backed storage
 * where available (Android Keystore, iOS Keychain, etc.).
 */
expect class FieldEncryptor {
    /**
     * Encrypts plaintext to a Base64-encoded ciphertext string.
     * The returned value is prefixed with "ENC:" for format detection.
     */
    fun encrypt(plaintext: String): String

    /** Decrypts a Base64-encoded ciphertext string back to plaintext. */
    fun decrypt(ciphertext: String): String

    /** Encrypts raw bytes. */
    fun encrypt(data: ByteArray): ByteArray

    /** Decrypts raw bytes. */
    fun decrypt(data: ByteArray): ByteArray
}
    // … (excerpt)
```

Source: [`src/commonMain/kotlin/kpt/core/base/crypto/FieldEncryptor.kt`](../../../../core-base/crypto/src/commonMain/kotlin/kpt/core/base/crypto/FieldEncryptor.kt) — excerpt; read the file for the full implementation.

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
