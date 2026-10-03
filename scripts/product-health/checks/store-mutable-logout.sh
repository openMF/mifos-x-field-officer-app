#!/usr/bin/env bash
# store-mutable-logout.sh — a MutableStore provider must declare `logout = false`.
#
# WHY THIS EXISTS
# `StoreCacheManager.register` takes `Store<*, *>`, and Store5 5.1 keeps `MutableStore` OUTSIDE that
# hierarchy. `@StoreProvider` defaults `logout = true`, so a MutableStore provider that says nothing
# gets a generated `mgr.register(get(AppStoreRegistry.X))` — which COMPILES, because Koin's `get()`
# is reified against the parameter type, and then fails at runtime when Koin looks for a
# `Store<*, *>` under a qualifier bound to a `MutableStore`. That block is `createdAtStart = true`,
# so the failure is a crash ON LAUNCH, nowhere near the annotation that caused it.
#
# `logout = false` is not a privacy hole PROVIDED the queue's table is purged by a paired read store
# over the same rows (see core/store/queue/OfflineQueueStores.kt — every `*Queue` has a `pending*`
# sibling that IS registered). That pairing is the thing to preserve when adding a queue.
#
# CONTRACT
#   ML-1 every provider returning MutableStore<...> declares `logout = false`
#
# Exit 0 = PASS · 1 = FAIL (blocks). Pure bash + awk — runs without a build.
set -uo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)"
STORES="$ROOT/core/store/src/commonMain/kotlin/kpt/core/store"
fail=0

# Walk each file, pairing the nearest @StoreProvider above a MutableStore-returning provider.
while IFS= read -r f; do
  [ -z "$f" ] && continue
  bad=$(awk '
    /@StoreProvider\(/ { ann = $0; annline = NR }
    /^fun .*: *MutableStore</ || /\): *MutableStore</ {
      if (ann != "" && ann !~ /logout *= *false/) {
        printf "%d:%s\n", annline, ann
        ann = ""
      }
    }
  ' "$f")
  if [ -n "$bad" ]; then
    echo "  ML-1 FAIL: ${f#"$ROOT"/}"
    echo "$bad" | sed 's/^/      /'
    fail=1
  fi
done < <(find "$STORES" -name '*.kt' 2>/dev/null)

if [ "$fail" -eq 0 ]; then
  n=$(grep -rc ': MutableStore<' "$STORES" --include='*.kt' 2>/dev/null | awk -F: '{s+=$2} END{print s+0}')
  echo "store-mutable-logout: PASS (ML-1 over $n MutableStore provider(s))"
else
  echo "  fix: add logout = false, and pair the queue with a registered read store over the same table"
fi
exit "$fail"
