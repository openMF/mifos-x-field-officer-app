# Consuming `core/platform` in a feature

> A thin re-export boundary over `core-base/platform` (`G-CORE-BASE-ENCAP`) — depend on
> `core/platform`, never on `core-base/platform` directly. It also holds any fork-owned
> `expect`/`actual` platform bridge that `core-base/platform` doesn't already provide.

## Call sequence

1. **Depend on `core/platform`, not `core-base/platform`** — `implementation(projects.core.platform)`
   in your module's `build.gradle.kts`. The `api(projects.coreBase.platform)` declaration inside
   `core/platform` re-exports `platformModule`, `GarbageCollectionManager`, and `tryCollect`
   transitively, so you get the full `core-base/platform` surface through one dependency.
2. **Install `platformModule`** in your Koin graph wherever the app wires DI — it's the framework
   platform-services module re-exported through this boundary.
3. **Adding a NEW platform bridge** (a capability `core-base/platform` doesn't cover — a
   permission requester, biometric prompt, deep-link opener, share-sheet opener, calendar-event
   creator, notification scheduler):
   - Declare the contract as an `expect` function/class in this module's `commonMain`.
   - Provide the `actual` implementation per target source set
     (`src/androidMain`, `src/iosMain`, `src/desktopMain`, `src/wasmJsMain`/`src/jsMain`,
     `src/nativeMain` as applicable).
   - Consumers keep importing from `core/platform` — the boundary doesn't change.

## Where a bridge lives, and when it moves

A capability climbs a ladder. Each rung has an entry condition, so "which module?" is answered by
what is TRUE of the bridge, not by taste.

| Rung | Lives in | Entry condition |
|---|---|---|
| 1 | **`core/platform`** (here) | No commonMain API exists for it — not in kmptoolkit, not in `core-base/platform`. You write the `expect`/`actual` yourself. Fork-owned, so the template is not polluted by a capability one app needed. |
| 2 | **`core-base/platform`** | The bridge has proven generic: every target has a REAL `actual` (not a no-op), the contract has stopped changing, and a second consumer would want it unmodified. Promote it, and every fork gets it on the next sync. |
| 3 | **kmptoolkit** (`cmp-*`) | The capability is general beyond this template. Once the library ships per-target `actual`s, the wrapper here becomes a delegation — see below. |

**Promotion is earned, not scheduled.** The bar for rung 2 is *every target has a real
implementation*. `AppUpdateManager` is the counter-example: it was promoted early and sat in
`core-base/platform` as Play Core on Android beside two empty method bodies for every other target,
so iOS, desktop and web silently had no update path. A no-op `actual` is a bridge that has not
finished rung 1.

**Rung 3 does NOT mean the wrapper moves down to `core-base/common`.** That module is Tier 0 with 9
dependents and no Compose plugin; putting platform engines there pushes them onto everything.
"Available everywhere" is already true — `core/platform` declares `api(projects.coreBase.platform)`,
so one `implementation(projects.core.platform)` line gives a feature the whole surface.

The rung-3 question is whether the wrapper still earns its keep:

- **Keep it** when it carries a template-owned type in its signature (`ShareManager` takes our
  `MimeType`), when a fork needs a substitution seam, or when features need a fake in tests.
- **Drop it** when it is a pure passthrough. Let features depend on the `cmp-*` module directly —
  the library is commonMain, so that is "available everywhere" with no layer to keep in sync.

## Notes

- This module currently ships **zero Kotlin source** — it is purely the `build.gradle.kts`
  re-export declaration. That's by design, not a gap: it stays a pass-through until a fork or
  feature needs its own platform bridge, at which point step 3 above is where that code lands.
- Do not depend on `core-base/platform` directly from a `feature/{F}` or app-shell module — that
  bypasses the encapsulation boundary this module exists to enforce.

Canonical example: `cmp-navigation`/`cmp-shared` consuming `platformModule` through `core/platform`
for DI wiring; the bill-reminder scheduler (now `feature/bills` + the cross-platform sync worker
infra) is the historical precedent for a feature-triggered platform bridge that used to live here.

Symbols: platformModule, GarbageCollectionManager, tryCollect
