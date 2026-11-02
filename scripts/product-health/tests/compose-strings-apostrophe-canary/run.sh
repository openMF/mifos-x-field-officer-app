#!/usr/bin/env bash
# Canary for compose-strings-apostrophe.sh — RED must fail on CS-1 itself, GREEN must pass.
set -uo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
CHECK="$HERE/../../checks/compose-strings-apostrophe.sh"
rc=0
out="$(bash "$CHECK" "$HERE/red" 2>&1)"; r=$?
if [ $r -eq 1 ] && echo "$out" | grep -q '✗ CS-1'; then echo "  ✓ RED failed on CS-1"; else echo "  ✗ RED did not fail on CS-1 (exit $r)"; rc=1; fi
if bash "$CHECK" "$HERE/green" >/dev/null 2>&1; then echo "  ✓ GREEN passed"; else echo "  ✗ GREEN failed"; rc=1; fi
[ $rc = 0 ] && echo "CANARY: ✅ PASS" || echo "CANARY: ❌ FAIL"; exit $rc
