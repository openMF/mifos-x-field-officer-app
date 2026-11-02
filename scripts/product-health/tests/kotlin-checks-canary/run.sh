#!/usr/bin/env bash
# kotlin-checks-canary — the git hooks check exactly what changed, in the right Gradle projects.
# (The Gradle side — module-scoped Spotless, -Pkpt.detekt.files — is too slow for a canary; this pins
# the pure logic that decides WHAT gets checked.)
#
#   K-1 module_of maps a file to the nearest project with a build.gradle.kts (nested modules too)
#   K-2 build-logic/ and root scripts map to the root project (":")
#   K-3 kotlin_files keeps existing .kt/.kts only, dropping build output and */tests/ fixtures
set -uo pipefail
HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$HERE/../../../.." && pwd)"
SB="$(mktemp -d)"; trap 'rm -rf "$SB"' EXIT
rc=0; ok() { echo "   ✅ $1"; }; bad() { echo "   ❌ $1"; rc=1; }

mkdir -p "$SB/core/ui/src/x" "$SB/feature/a/b/src" "$SB/build-logic/convention/src" "$SB/core/ui/build/gen" "$SB/scripts/tests"
touch "$SB/build.gradle.kts" "$SB/core/ui/build.gradle.kts" "$SB/feature/a/b/build.gradle.kts" \
      "$SB/core/ui/src/x/A.kt" "$SB/feature/a/b/src/B.kt" "$SB/build-logic/convention/src/C.kt" \
      "$SB/core/ui/build/gen/G.kt" "$SB/scripts/tests/F.kt" "$SB/core/ui/src/x/notes.md"
fns="$(awk '/^(module_of|kotlin_files)\(\) \{/{p=1} p{print} p&&/^}/{p=0}' "$ROOT/scripts/ci/kotlin-checks.sh")"
cd "$SB" || exit 1
eval "$fns"

[ "$(module_of core/ui/src/x/A.kt)" = ":core:ui" ] && [ "$(module_of feature/a/b/src/B.kt)" = ":feature:a:b" ] \
  && ok "K-1 file → nearest project" || bad "K-1 got $(module_of core/ui/src/x/A.kt) / $(module_of feature/a/b/src/B.kt)"
[ "$(module_of build-logic/convention/src/C.kt)" = ":" ] && [ "$(module_of build.gradle.kts)" = ":" ] \
  && ok "K-2 build-logic + root scripts → root project" || bad "K-2 got $(module_of build-logic/convention/src/C.kt)"
got="$(printf '%s\n' core/ui/src/x/A.kt core/ui/build/gen/G.kt scripts/tests/F.kt core/ui/src/x/notes.md gone/Missing.kt build.gradle.kts | kotlin_files | tr '\n' ' ')"
[ "$got" = "core/ui/src/x/A.kt build.gradle.kts " ] && ok "K-3 only existing Kotlin, no build output or fixtures" || bad "K-3 got '$got'"
exit "$rc"
