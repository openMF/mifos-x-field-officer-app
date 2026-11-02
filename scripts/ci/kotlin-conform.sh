#!/usr/bin/env bash
# kotlin-conform.sh [--check] <files…> — make Kotlin files Spotless-formatted and Detekt-clean, in
# seconds and without Gradle. The ONE implementation of the style rules outside Gradle: the git hooks
# (kotlin-checks.sh) and the framework's write-time hook both call it, so code is held to the same
# rules when it is written and when it is committed.
#
# For each .kt/.kts (paths relative to the repo root; build output and */tests/ fixtures skipped):
#   1. licence header — added when missing, rendered as build-logic LicenseHeader.kt does
#      (spotless/license-header.txt; $ORG_NAME ← app-profile/app.yaml#org.name; $YEAR ← this year)
#   2. ktlint at the version Spotless pins (Spotless.kt KTLINT_VERSION), IntelliJ IDEA code style —
#      identical to `spotlessCheck` on every core/feature file of the template (measured 2026-11-02);
#      build-logic keeps Spotless's override (no-unused-imports off: it cannot see Gradle's `assign`)
#   3. Detekt — the repo's config/detekt/detekt.yml + the Compose rules plugin, .kt only, build-logic
#      excluded as in Detekt.kt
# --check reports formatting instead of applying it (pre-push). Exit 0 clean · 1 findings (stderr).
#
# Why not Gradle: a module's spotlessApply costs ~15 s of configuration, the root project's ~3 min
# (its target trees are walked even for one file), and a whole-repo `detekt` 25 min. The tools here
# are the official release jars, cached once in ~/.cache/kpt-tools (KPT_TOOLS_DIR).
set -uo pipefail
CHECK=0; [ "${1:-}" = --check ] && { CHECK=1; shift; }
cd "$(git rev-parse --show-toplevel)" || exit 1
ROOT="$PWD"
files=(); for f in "$@"; do
  f="${f#"$ROOT"/}"
  case "$f" in *.kt|*.kts) ;; *) continue ;; esac
  case "$f" in */build/*|build/*|*/tests/*|tests/*) continue ;; esac
  [ -f "$f" ] && files+=("$f")
done
[ "${#files[@]}" -gt 0 ] || exit 0

TOOLS="${KPT_TOOLS_DIR:-$HOME/.cache/kpt-tools}"; mkdir -p "$TOOLS"
kv="$(sed -n 's/^const val KTLINT_VERSION = "\(.*\)"/\1/p' build-logic/convention/src/main/kotlin/org/convention/Spotless.kt)"
dv="$(sed -n 's/^detekt = "\(.*\)"/\1/p' gradle/libs.versions.toml)"
cv="$(sed -n 's/^twitter-detekt-compose = "\(.*\)"/\1/p' gradle/libs.versions.toml)"
KTLINT="$TOOLS/ktlint-$kv"; DETEKT="$TOOLS/detekt-cli-$dv-all.jar"; COMPOSE="$TOOLS/compose-rules-$cv"
fetch() { [ -s "$2" ] || { curl -fsSL -o "$2.part" "$1" && mv "$2.part" "$2"; } || { rm -f "$2.part"; echo "kotlin-conform: could not download $1" >&2; return 1; }; }
fetch "https://github.com/pinterest/ktlint/releases/download/$kv/ktlint" "$KTLINT" && chmod +x "$KTLINT" || exit 1
fetch "https://github.com/detekt/detekt/releases/download/v$dv/detekt-cli-$dv-all.jar" "$DETEKT" || exit 1
mkdir -p "$COMPOSE"
for a in detekt core-detekt core-common common; do
  fetch "https://repo1.maven.org/maven2/com/twitter/compose/rules/$a/$cv/$a-$cv.jar" "$COMPOSE/$a-$cv.jar" || exit 1
done

rc=0

# 1. licence header (a fix, so not in --check)
hdr=spotless/license-header.txt
if [ "$CHECK" = 0 ] && [ -s "$hdr" ]; then
  org="$(awk '/^[^[:space:]]/{o=($0 ~ /^org:/)} o && /^  name:/{sub(/^  name:[[:space:]]*/,""); sub(/[[:space:]]*#.*/,""); gsub(/^["\x27]|["\x27]$/,""); print; exit}' app-profile/app.yaml 2>/dev/null)"
  year="$(date +%Y)"
  for f in "${files[@]}"; do
    head -1 "$f" | grep -q '^/\*' && continue
    { echo "/*"
      sed -e :a -e '/^\n*$/{$d;N;ba' -e '}' "$hdr" | while IFS= read -r l; do
        l="${l//\$ORG_NAME/${org:-the project authors}}"; l="${l//\$YEAR/$year}"
        if [ -z "${l// /}" ]; then echo " *"; else echo " * $l"; fi
      done
      echo " */"; cat "$f"; } > "$f.kc" && mv "$f.kc" "$f"
  done
fi

# 2. ktlint — build-logic with Spotless's override, the rest with the plain style
ktlint_run() { # <editorconfig-or-empty> <files…>
  local ec="$1"; shift; [ "$#" -gt 0 ] || return 0
  local args=(--code-style=intellij_idea --relative); [ "$CHECK" = 0 ] && args+=(-F); [ -n "$ec" ] && args+=(--editorconfig="$ec")
  "$KTLINT" "${args[@]}" "$@" 2>/dev/null | grep -E '^[^ ].*:[0-9]+:[0-9]+:'
}
bl=(); rest=(); for f in "${files[@]}"; do case "$f" in build-logic/*) bl+=("$f") ;; *) rest+=("$f") ;; esac; done
ec="$(mktemp)"; printf '[*.{kt,kts}]\nktlint_standard_no-unused-imports = disabled\n' > "$ec"
out="$(ktlint_run "" "${rest[@]+"${rest[@]}"}"; ktlint_run "$ec" "${bl[@]+"${bl[@]}"}")"; rm -f "$ec"
[ -n "$out" ] && { printf 'ktlint (Spotless)%s:\n%s\n' "$([ "$CHECK" = 0 ] && echo ' — not auto-fixable')" "$out" >&2; rc=1; }

# 3. Detekt
kt=(); for f in "${files[@]}"; do case "$f" in *.kts|build-logic/*) ;; *) kt+=("$f") ;; esac; done
if [ "${#kt[@]}" -gt 0 ]; then
  out="$(java -jar "$DETEKT" --input "$(IFS=,; echo "${kt[*]}")" --config config/detekt/detekt.yml \
         --plugins "$(ls "$COMPOSE"/*.jar | paste -sd, -)" 2>&1 | grep -E '^/.*:[0-9]+:[0-9]+: ' | sed "s|^$ROOT/||")"
  [ -n "$out" ] && { printf 'Detekt:\n%s\n' "$out" >&2; rc=1; }
fi
exit "$rc"
