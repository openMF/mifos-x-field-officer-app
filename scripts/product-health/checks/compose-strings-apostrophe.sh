#!/usr/bin/env bash
# compose-strings-apostrophe.sh — no Android-style `\'` escape in Compose Multiplatform strings.
#
# WHY THIS EXISTS
# Android's resource compiler unescapes `\'`; Compose Multiplatform resources do NOT. The backslash
# reaches the screen: a render of feature/settings showed "You\'ll see bill reminders…", and a loan
# form showed "Could not load this product\'s terms." (found in mifos-x-field-officer-app
# 2026-10-23). It looks right in the XML and in an Android-only build, so nothing caught it — 64
# occurrences across 32 files of this template.
#
#   CS-1 a `\'` inside a composeResources/values*/strings.xml
#
# A plain `'` is valid XML text; that is the fix. Exit 0 = PASS · 1 = FAIL (blocks).
set -uo pipefail

ROOT="${1:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../../.." && pwd)}"
cd "$ROOT" 2>/dev/null || { echo "  ✗ CS-0 cannot enter $ROOT"; exit 2; }

# product-health/tests/** holds RED fixtures that are broken on purpose (see kdoc-comment-balance.sh).
FILES="$(find . -path '*/build' -prune -o -path './scripts/product-health/tests' -prune -o \
              -path '*/composeResources/values*/strings.xml' -type f -print 2>/dev/null)"
if [ -z "$FILES" ]; then
    echo "  ✓ CS-1 no composeResources strings.xml in this tree — nothing to check"
    exit 0
fi

HITS="$(printf '%s\n' "$FILES" | xargs grep -nF "\\'" 2>/dev/null | sed 's|^\./||' || true)"
if [ -n "$HITS" ]; then
    echo "  ✗ CS-1 \\' in Compose strings — Compose Multiplatform renders the backslash; use a plain '"
    printf '%s\n' "$HITS" | head -20 | sed 's/^/      /'
    n=$(printf '%s\n' "$HITS" | wc -l | tr -d ' ')
    [ "$n" -gt 20 ] && echo "      … and $((n - 20)) more"
    exit 1
fi
echo "  ✓ CS-1 no \\' escapes in $(printf '%s\n' "$FILES" | wc -l | tr -d ' ') Compose strings files"
exit 0
