# Customization Surface — the fork-ownership contract

`customization-surface.yaml` (repo root) is the **single declared source of truth
for who owns each path** when a fork syncs template updates. It replaces ownership
knowledge that was previously implicit and scattered across `scripts/white-label/sync-dirs.sh`
(`SYNC_DIRS` + `EXCLUSIONS`), `scripts/white-label/fork-init.sh`, and the `syncForkConfig` copy map.

## Why it exists

A fork runs `scripts/white-label/sync-dirs.sh` to pull template updates while keeping its own branding
+ features. The hard question is *"which files may the sync overwrite?"* When that
answer lives only in an ad-hoc `EXCLUSIONS` list, a path with no exclusion gets
**blind-overwritten** — and that silently drops fork edits. The motivating case:

> `cmp-android/src/main/AndroidManifest.xml` is template-shipped **and**
> fork-extended (a fork adds `RECORD_AUDIO`, `FOREGROUND_SERVICE_MICROPHONE`, …).
> The `EXCLUSIONS` map preserved only `src/main/res`, so a full sync overwrote the
> manifest and **dropped the fork's permissions** — breaking the app.

The contract makes ownership **explicit, single-source, and machine-checkable**, so
that whole class of silent loss becomes impossible.

## The three ownership modes

| owner | meaning | sync behaviour |
|---|---|---|
| `template` | template is the sole author | **overwrite** the fork's copy. A fork edit here is drift — a genuine fix belongs **upstream** as a PR to `openMF/kmp-project-template`. |
| `fork` | the fork is the sole author | **never touch** it (branding, demo, the `core/store` seam, generated `Config.xcconfig`, local flavors, icons, store listings, fork identity). |
| `merge` | both author | **3-way merge**, never a blind copy (`AndroidManifest.xml`, `strings.xml`, `libs.versions.toml`, `settings.gradle.kts`, nav host). |

> The most-edited `owner: fork` files are the **white-label extension seams** — the registries a fork
> uses to add features, tabs, the home body, and startup hooks without touching template infra. They are
> documented in
> [`cmp-navigation/.../registry/README.md`](../../cmp-navigation/src/commonMain/kotlin/cmp/navigation/registry/README.md).

## Precedence

Rules are ordered **most-specific → most-general**, and the **first matching rule
wins**. So the narrow `merge`/`fork` carve-outs are declared *before* the broad
`template` module globs and correctly win over them. A trailing catch-all `**`
rule (`owner: fork`, `default: true`) means the runtime never clobbers an unknown
path — while `--verify` flags anything that reaches it so a human classifies it.

Glob syntax: `*` matches within one path segment; `**` matches across segments;
`**/x` matches `x` at any depth (including the repo root).

## The reader / validator — `scripts/customization-surface.sh`

Pure bash + awk (no `yq`/`jq`/`python` dependency — runs on any fork as-is).

```bash
# what owns this path?
scripts/customization-surface.sh resolve cmp-android/src/main/AndroidManifest.xml
#   → cmp-android/src/main/AndroidManifest.xml   merge  (strategy: manifest-union)

# owner of every tracked path
scripts/customization-surface.sh report

# CI canary — fail if any tracked path is unclassified (only matches the default)
scripts/customization-surface.sh verify
#   → ── customization-surface coverage: 1587/1587 classified ──
#   → ✅ every tracked path has an explicit owner
```

Source it as a library too:

```bash
source scripts/customization-surface.sh
cs_resolve_owner    "gradle/libs.versions.toml"   # → merge
cs_resolve_strategy "gradle/libs.versions.toml"   # → catalog-3way
```

## How `scripts/white-label/sync-dirs.sh` uses it

1. **Advisory report** — before the sync, `scripts/white-label/sync-dirs.sh` sources the reader and
   prints the `merge`-owned paths in the sync surface (fully guarded, cannot abort).
2. **3-way merge of `merge`-owned files** — after checking out the upstream copy of
   a directory, for every file that changed between the fork base and upstream and
   resolves to `owner: merge`, `scripts/white-label/sync-dirs.sh` runs `cs_merge <strategy> ours base
   theirs` instead of taking the blind upstream copy:
   - `ours` = fork's current file (`BASE_BRANCH`)
   - `theirs` = upstream file (`temp_branch`)
   - `base` = `resolve_merge_base BASE_BRANCH temp_branch` — **not** `git merge-base` alone

   **The base is the whole promise.** Two fork topologies exist and only one gives git a common
   ancestor: a **forked** repo (clone / `gh repo fork`) shares history, while a **copied** one — the
   template tree dropped into a fresh repo, the common case in practice — has different root commits
   and **zero** shared commits, so `git merge-base` returns empty. With an empty base the engine used
   `ours`, and `git merge-file` then reads every template difference as a clean addition onto an
   untouched base and takes **all of theirs** — reproducing exactly the full-copy that `merge` exists
   to prevent, while printing *"Merged (…) — fork edits preserved"* over the top of the loss.

   `resolve_merge_base()` therefore falls back to **`.template-version#template_sha`**, the commit the
   tree last synced FROM — precisely the ancestor git cannot compute across unrelated histories, and
   what that file's own header always claimed it was for. With neither available (a copied fork's
   first-ever sync) the engine still falls back to `ours` but **warns per file** instead of reporting
   a merge it did not perform. Pinned by `MB-1`–`MB-3` in
   `scripts/product-health/checks/sync-merge-base.sh`.

   `pbxproj-3way` (`**/*.xcodeproj/project.pbxproj`) delegates to
   `scripts/white-label/merge-pbxproj.rb`, because a line merge on an Xcode project is not merely
   worse — it is unusable. A pbxproj is a serialized object graph keyed by 24-hex UUIDs, so adding a
   target rewrites UUID arrays three levels from the object it adds, and `git merge-file` reads
   adjacent array insertions as a conflict. A conflict marker inside a pbxproj makes the project
   **unopenable**, so a human cannot even use Xcode to resolve it. It is tractable because the UUIDs
   are stable across the fork boundary (measured: 33 of 36 objects shared), which turns the text
   merge into a keyed 3-way: UUID arrays union, `buildSettings` merge per key, and the declared
   `FORK_IDENTITY_KEYS` (`DEVELOPMENT_TEAM`, `PRODUCT_BUNDLE_IDENTIFIER`, `CODE_SIGN_*`, …) go to the
   fork when both sides moved. Anything it cannot decide FAILS the merge rather than guessing, and
   the engine's fallback when ruby is unavailable is **keep-ours**, never take-theirs — losing a
   sync is recoverable, losing the fork's targets is not. Uses the `xcodeproj` gem, already resolved
   in `Gemfile.lock` as a fastlane transitive. Pinned by `scripts/product-health/checks/pbxproj-merge.sh`
   against a real captured fixture in `tests/fixtures/pbxproj-3way-canary/`.

   `manifest-union` runs a **semantic** union (union `<uses-permission>` /
   `<uses-feature>` by `android:name`, keeping the template's structural update) so a
   fork's `RECORD_AUDIO` survives a template manifest change. The other strategies
   (`catalog-3way`, `include-union`, `kotlin-3way`, `strings-union`) run `git
   merge-file`, which cleanly unions non-overlapping edits and emits conflict markers
   **only on a true overlap** — surfaced with a `CONFLICT` warning for review, never
   silently shipped.

### The platform shells are `merge`, not `template`

`build-logic/**` and the four platform **application shells** — `cmp-android/**`, `cmp-ios/**`,
`cmp-desktop/**`, `cmp-web/**` — are mostly template content, which makes `template` + full-copy the
tempting classification. It is the wrong one: a platform shell is the one place where fork-specific
**platform wiring** has nowhere else to live. `core/<mod>/module-deps.gradle.kts` gives a fork a seam
for dependencies; there is no equivalent seam for an Xcode target, an entitlement, or a plist key.

Measured on a real consumer (2026-09-10), every one of these sat under the old `template` blanket and
would have been destroyed silently by a full-copy:

| Path | Fork content a full-copy deletes |
|---|---|
| `cmp-ios/iosApp/Info.plist` | `CFBundleURLTypes` for the Google Sign-In OAuth redirect — no build failure; OAuth just never returns |
| `cmp-ios/iosApp/iosApp.entitlements` | the WidgetKit app-group. The template half (`keychain-access-groups`, required by `core-base/datastore`) is **also** needed — neither side may win, so it must be a union |
| `cmp-ios/iosApp.xcodeproj/project.pbxproj` | an entire app-extension target the template has no concept of |
| `cmp-ios/iosApp/iOSApp.swift` | scene-phase `AppBackground` / `AppForeground` hooks |

A 3-way takes the template's evolution — the SwiftPM migration landed 5 `XCLocalSwiftPackageReference`
entries plus the `KotlinMultiplatformLinkedPackage` tree this way — while the fork's additions survive,
and a genuine overlap surfaces as a conflict marker rather than a silent loss.

These blankets carry **no `strategy:`**: they span `.kts` / `.swift` / `.plist` / `.xml` / `.pbxproj` /
`.xcconfig` / `.js` / `.html`, and `cs_merge`'s default `git merge-file` is the only strategy that
handles all of them. Structure-aware strategies stay on the specific rows that name one filetype
(`AndroidManifest.xml` still resolves `merge (strategy: manifest-union)`). Binaries under these
directories are carved out `fork` (`res/**`, `Assets.xcassets/**`, `cmp-desktop/icons/**`, the
`cmp-web` resources), and the engine additionally refuses to line-merge a binary.

`cmp-shared/**` and `cmp-navigation/**` stay `template` — they carry no platform project files, and
their fork surface is already served by named seams (`ForkWorkerDeclarations.kt`, `registry/**`,
`di/*.kt`, `RootNav*.kt`).

The `template`/`fork` mechanical behaviour (`SYNC_DIRS` + `EXCLUSIONS`) is unchanged;
the contract adds the `merge` path handling that previously didn't exist. Guarded so
forks that don't ship the reader are unaffected.

Proof: `tests/customization-surface/merge-3way-test.sh` — a fork's `RECORD_AUDIO` +
`FOREGROUND_SERVICE_MICROPHONE` survive a template update that adds
`POST_NOTIFICATIONS`, clean, no markers; a true same-line conflict returns rc=1 with
markers.

## Recommended CI wiring

Add a coverage gate so a new template file can never land unclassified (and thus
never silently clobber a fork later):

```yaml
- name: Verify customization-surface coverage
  run: bash scripts/customization-surface.sh verify
```

## Roadmap (follow-ups)

1. ~~Merge engine adoption~~ — **done**: `scripts/white-label/sync-dirs.sh` 3-way merges `merge`-owned
   files (`manifest-union` semantic + `git merge-file` for the rest). Remaining:
   fold the `fork`/`template` decisions into the same contract lookup and retire the
   hand-maintained `EXCLUSIONS` map (the reader can already answer `fork` → skip /
   `template` → copy; `EXCLUSIONS` becomes redundant).
2. **`syncForkConfig` alignment** — the plugin reads the same contract to know
   which generated files it owns vs must not clobber.

## Editing the contract

- A new **template-owned** module → add a `template` rule (and, if forks extend a
  specific file inside it, a narrower `merge`/`fork` rule *above* it).
- A new **fork-branded** path → add a `fork` rule.
- A file both sides edit → add a `merge` rule with a `strategy:`.
- Always run `scripts/customization-surface.sh verify` after editing.
