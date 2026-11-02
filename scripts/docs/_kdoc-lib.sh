#!/usr/bin/env bash
# scripts/docs/_kdoc-lib.sh — the ONE upward walk from a declaration to the line that closes its KDoc.
#
# Three call sites needed this and each had its own copy (`kdoc_summary` + `kdoc_example` in
# api-docs-gen.sh, `has_kdoc` in kdoc-coverage.sh). kdoc-coverage.sh even carried a comment promising
# it used "the SAME walk" — a promise nothing enforced, and the copies then diverged. One function,
# sourced by both scripts.
#
# What the walk has to cross to get from a declaration up to its doc:
#   blank lines                       — normal formatting
#   single-line annotations           — `@Serializable`
#   MULTI-LINE annotation arg lists   — `@CacheKey(\n  fn = "of",\n  …\n)`
#
# That last case is why the naive version failed: walking up from the declaration it met the `)` that
# closes `@CacheKey(`, which is neither blank nor `@`-prefixed, so it stopped and reported the symbol
# as undocumented. Four `@StoreProvider` factories in core/store were documented all along and still
# printed `_No KDoc at source._` in the published reference.
#
# It crosses such a group ONLY after confirming the matching opener is an annotation. A bare `)`
# closing the PREVIOUS declaration's parameter list must stop the walk — crossing it would attribute
# that declaration's KDoc to this one, which is a false pass, and a false pass is worse than the miss
# it replaces.

# kdoc_close_line <file> <declaration-line-no>
# Prints the 1-based line number that should hold the end of this declaration's KDoc (whatever is
# actually there — the caller decides whether it IS a KDoc). Prints 0 when the walk runs off the top.
kdoc_close_line() {
  local f="$1" i="$2" line t closes opens depth j jt
  i=$((i - 1))
  while [ "$i" -gt 0 ]; do
    t="$(sed -n "${i}p" "$f" | sed -E 's/^[[:space:]]+//; s/[[:space:]]+$//')"
    case "$t" in
      ""|"@"*) i=$((i - 1)); continue ;;
    esac
    closes="$(printf '%s' "$t" | tr -cd ')' | wc -c | tr -d ' ')"
    opens="$(printf '%s' "$t" | tr -cd '(' | wc -c | tr -d ' ')"
    if [ "$closes" -le "$opens" ]; then
      break                                  # an ordinary line — this is where the doc must end
    fi
    # Balance upward to the opener, then cross the group only if the opener is an annotation.
    depth=$((closes - opens)); j=$((i - 1))
    while [ "$j" -gt 0 ] && [ "$depth" -gt 0 ]; do
      jt="$(sed -n "${j}p" "$f")"
      depth=$((depth + $(printf '%s' "$jt" | tr -cd ')' | wc -c | tr -d ' ') \
                     - $(printf '%s' "$jt" | tr -cd '(' | wc -c | tr -d ' ')))
      [ "$depth" -le 0 ] && break
      j=$((j - 1))
    done
    [ "$j" -gt 0 ] || break
    case "$(sed -n "${j}p" "$f" | sed -E 's/^[[:space:]]+//')" in
      "@"*) i=$((j - 1)); continue ;;         # annotation group — keep walking above it
      *)    break ;;                          # someone else's parameter list — stop here
    esac
  done
  printf '%s' "$i"
}
