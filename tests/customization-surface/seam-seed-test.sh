#!/usr/bin/env bash
# bash4-required: it runs the engine's own functions, which read the associative EXCLUSIONS map.
# Functional test: a FORK-owned file the template ships for the first time reaches the fork as a seed;
# one the fork deliberately deleted stays deleted. Runs the REAL sync_directory with the REAL ownership
# reader. Before the fix both were "Dropped upstream-added (fork removed it)": a fork never received
# spotless/license-header.txt, and spotlessApply then stripped the license header from ~2,500 files.
#
#   new fork-owned seam (absent at the last sync)         → seeded with the template's copy
#   fork-owned file present at the last sync, fork deleted → stays deleted
#   fork-owned file in a directory the fork lacks (a sample package scaffold) → not seeded
#   fork-owned Room schema (the fork's own lineage)                        → not seeded
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$HERE/../.." && pwd)"
ENGINE="$REPO/scripts/white-label/sync-dirs.sh"
WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

F="$WORK/fork"; mkdir -p "$F/scripts/white-label"
cp "$REPO/scripts/customization-surface.sh" "$F/scripts/"
printf 'version: 1\nrules:\n  - glob: "spotless/license-header.txt"\n    owner: fork\n  - glob: "spotless/old-seam.txt"\n    owner: fork\n  - glob: "spotless/samplepkg/**"\n    owner: fork\n  - glob: "spotless/schemas/**"\n    owner: fork\n  - glob: "**"\n    owner: template\n    default: true\n' > "$F/customization-surface.yaml"
printf 'SYNC_DIRS=(\n    "spotless"\n)\nSYNC_FILES=(\n)\n' > "$F/scripts/white-label/sync-dirs.sh"

# git, run in the fixture repo with a fixed identity.
g() { git -C "$F" -c user.email=t@t -c user.name=t "$@"; }
mkdir -p "$F/spotless"; echo keep > "$F/spotless/copyright.kt"; echo seam > "$F/spotless/old-seam.txt"
g init -q -b main; g add -A; g commit -q -m "last sync"; BASE="$(g rev-parse HEAD)"
g checkout -q -b fork; g rm -q spotless/old-seam.txt; g commit -q -m "fork deletes its old seam"
g checkout -q -b tpl main; mkdir -p "$F/spotless/samplepkg" "$F/spotless/schemas"; echo doc > "$F/spotless/samplepkg/README.md"; echo {} > "$F/spotless/schemas/12.json"; echo 'Copyright $YEAR $ORG_NAME' > "$F/spotless/license-header.txt"; g add -A; g commit -q -m "template: new fork seam"
g checkout -q fork

fnsrc="$WORK/fns.sh"
{
  echo 'print_step(){ :; }; print_warning(){ :; }; print_error(){ :; }; show_progress(){ :; }; declare -A EXCLUSIONS=(); SYNC_CONFLICTS=(); SYNC_FAILED=()'
  for fn in is_excluded resolve_merge_base remove_stale_path propagate_template_deletions mirror_template_owned sync_directory; do
    awk -v f="$fn" '$0 ~ "^"f"\\(\\) \\{" {p=1} p{print} p&&/^}/{p=0}' "$ENGINE"
  done
} > "$fnsrc"
grep -q 'sync_directory()' "$fnsrc" || { echo "  ❌ sync_directory not found in engine"; exit 1; }

( cd "$F" && SCRIPT_DIR="$F/scripts/white-label" BASE_BRANCH=main FORK_STATE=fork PREV_TEMPLATE_SHA="$BASE" \
    DRY_RUN=false MIRROR=true bash -c "$(cat "$fnsrc")
    sync_directory spotless tpl" ) >/dev/null 2>&1

[ -f "$F/spotless/license-header.txt" ] && pass "new fork-owned seam seeded from the template" \
  || bad "new fork-owned seam dropped (spotless/license-header.txt)"
[ ! -f "$F/spotless/old-seam.txt" ] && pass "fork-owned file the fork deleted stays deleted" \
  || bad "the fork's deletion was undone (spotless/old-seam.txt came back)"

[ ! -e "$F/spotless/samplepkg/README.md" ] && pass "sample package in a directory the fork lacks is not seeded" \
  || bad "a sample package scaffold was seeded (spotless/samplepkg/README.md)"
[ ! -e "$F/spotless/schemas/12.json" ] && pass "a Room schema is not seeded into the fork's lineage" \
  || bad "a template schema was seeded (spotless/schemas/12.json)"

[ "$fail" = 0 ] && echo "seam-seed-test: PASS" || echo "seam-seed-test: FAIL"
exit "$fail"
