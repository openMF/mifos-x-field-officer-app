# `core-base/common`

> **Layer:** `core-base` — framework-shared. Generators **consume** these contracts and
> **never write here**; a fix belongs upstream in the template, not in a fork.
> **Instruction surface:** `CORE_BASE_COMMON.md` — the generator-facing instruction for this module,
> held in the framework at `training-layer/instructions/stream-first/latest/`. This guide
> is the architecture SoT; that surface is how it reaches codegen, and
> `/kmp-project-template-retrain` keeps the two in step.
> **Shape:** 10 Kotlin files (0 test) · source sets: `androidMain`, `commonMain`, `nonAndroidMain`



## When implementing a feature

**You do not write here.** `core-base` is framework-shared: a feature consumes these
contracts and never modifies them. If a feature seems to need a change here, that is a
TEMPLATE change — it flows upstream as a draft PR (RULE-TEMPLATE-MODULE-FIX-UPSTREAM-001),
never a local edit, because every fork shares this code and a local fix is drift.

What a feature *does* do is import from here and satisfy the contracts this module
defines. The module guides under `../core/` show where the feature-side code goes.

## Position in the module graph

No module dependencies — this is a leaf.

**Consumed by** 4 module(s): `core-base/data`, `core/common`, `core/data`, `core/datastore`

## Codegen contracts

**None.** Nothing here is declared by annotation, so there is no aggregate to
generate and no propagation target. A generator writing into this module takes its
idiom from `CORE_BASE_COMMON.md`.

## Principal types

Undocumented: `DispatcherManager`, `DispatcherManagerImpl`, `Parcel`, `Parcelable`, `Parceler`

## Demo showcase exposure

**None.** No `demo/` package and no `// demo:begin` fence — `remove-demo.sh` does not
touch this module, so a stripped fork keeps it verbatim.

## Tests

No tests in this module. If you add behaviour here, add the test alongside it —
`CORE_TESTING.md` carries the shared idiom.



## Sample implementation

Real code from this module — the shape a generator should follow here.

```kotlin
/**
 * Extension function to convert ByteArray to Base64 string
 */
@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.toBase64(): String {
    return Base64.encode(this)
}
```

Source: [`src/commonMain/kotlin/kpt/core/base/common/ImageExtension.kt`](../../../../core-base/common/src/commonMain/kotlin/kpt/core/base/common/ImageExtension.kt).

<!-- scaffold:end -->

## Notes

_Authored prose below this marker is preserved by the scaffolder._
