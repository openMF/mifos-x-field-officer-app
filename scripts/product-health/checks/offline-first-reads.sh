#!/usr/bin/env bash
# offline-first-reads.sh — every read in this fork serves cache first, and keeps serving it offline.
#
# WHY THIS EXISTS
# A field officer works a full day out of signal: enrolling clients under a tree, recording
# repayments in villages with no bars. "Offline-first" here is the product, not an optimisation, and
# two easy edits silently destroy it while every test stays green:
#
#   1. A `validator = DefaultValidator.withTtl(...)` on a read store. Once the TTL elapses
#      `isValid()` returns false, Store5 treats the cached row as unusable and refetches — and
#      offline that fetch fails, so the officer's cached client list becomes an empty screen. TTLs
#      are declared on @StoreProvider for the FRESHNESS INDICATOR only ("updated 2h ago"); they must
#      never gate whether the cache is served. That is why no store passes `validator =`.
#   2. A `createMemoryStore` read. Its cache dies with the process, so a cold start offline has
#      nothing — the template documents MEMORY_ONLY as non-cache-first BY DEFINITION.
#
# CONTRACT
#   OF-1 no read store passes `validator =`            (TTL must not gate cache use)
#   OF-2 no read store uses createMemoryStore          (cache must survive process death)
#   OF-3 every read store declares a sourceOfTruth     (there IS a cache to serve)
#
# Exit 0 = PASS · 1 = FAIL (blocks). Pure bash + grep — runs without a build.
set -uo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
STORES="$ROOT/core/store/src/commonMain/kotlin/kpt/core/store"
fail=0

note() { printf '  %s\n' "$*"; }

# OF-1 — a validator would make an expired cache unusable, which offline means unusable full stop.
hits=$(grep -rn 'validator[[:space:]]*=' "$STORES" --include='*.kt' 2>/dev/null || true)
if [ -n "$hits" ]; then
  note "OF-1 FAIL: a read store wires a validator — an expired cache stops being served offline:"
  printf '    %s\n' "$hits"
  fail=1
fi

# OF-2 — memory caches do not survive a cold start, which is the offline case that matters.
hits=$(grep -rn 'createMemoryStore' "$STORES" --include='*.kt' 2>/dev/null || true)
if [ -n "$hits" ]; then
  note "OF-2 FAIL: createMemoryStore in a fork read store — cache dies with the process:"
  printf '    %s\n' "$hits"
  fail=1
fi

# OF-3 — every provider must have something to read back when the network is gone.
missing=""
while IFS= read -r f; do
  [ -z "$f" ] && continue
  grep -q 'StoreFactory.create' "$f" || continue
  grep -q 'sourceOfTruth' "$f" || missing="$missing\n    ${f#"$ROOT"/}"
done < <(find "$STORES" -name '*Store*.kt' 2>/dev/null)
if [ -n "$missing" ]; then
  note "OF-3 FAIL: store provider(s) with no sourceOfTruth — nothing to serve offline:"
  printf "%b\n" "$missing"
  fail=1
fi

if [ "$fail" -eq 0 ]; then
  n=$(grep -rl 'StoreFactory.create' "$STORES" --include='*.kt' 2>/dev/null | wc -l | tr -d ' ')
  echo "offline-first-reads: PASS (OF-1..OF-3 over $n store file(s))"
fi
exit "$fail"
