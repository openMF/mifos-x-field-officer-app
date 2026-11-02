#!/usr/bin/env bash
# kotlin-checks-canary — the git hooks check exactly the Kotlin files that changed. (The tools themselves
# are pinned by the framework's kotlin-write-conform-canary; this pins the selection.)
#
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
fns="$(awk '/^(kotlin_files)\(\) \{/{p=1} p{print} p&&/^}/{p=0}' "$ROOT/scripts/ci/kotlin-checks.sh")"
cd "$SB" || exit 1
eval "$fns"

got="$(printf '%s\n' core/ui/src/x/A.kt core/ui/build/gen/G.kt scripts/tests/F.kt core/ui/src/x/notes.md gone/Missing.kt build.gradle.kts | kotlin_files | tr '\n' ' ')"
[ "$got" = "core/ui/src/x/A.kt build.gradle.kts " ] && ok "K-3 only existing Kotlin, no build output or fixtures" || bad "K-3 got '$got'"
exit "$rc"
