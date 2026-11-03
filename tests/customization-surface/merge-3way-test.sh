#!/usr/bin/env bash
# Fixture test for cs_merge_3way — proves the AndroidManifest permission-loss class
# is closed: a fork's added <uses-permission> survives a template manifest update.
set -uo pipefail

HERE="$(cd "$(dirname "$0")" && pwd)"
READER="$HERE/../../scripts/customization-surface.sh"
# shellcheck source=/dev/null
source "$READER"

WORK="$(mktemp -d)"; trap 'rm -rf "$WORK"' EXIT
fail=0; pass() { echo "  ✅ $1"; }; bad() { echo "  ❌ $1"; fail=1; }

manifest() { # $1 = extra permission lines
  cat <<XML
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
$1    <application android:label="@string/app_name" />
</manifest>
XML
}

echo "── Case A: additive union (fork perms + template perms) ──"
manifest '' > "$WORK/base.xml"
# ours = fork added RECORD_AUDIO + FOREGROUND_SERVICE_MICROPHONE
manifest '    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
' > "$WORK/ours.xml"
# theirs = template added POST_NOTIFICATIONS
manifest '    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
' > "$WORK/theirs.xml"

# The loss class: a BLIND copy of theirs would drop the fork's RECORD_AUDIO.
if grep -q RECORD_AUDIO "$WORK/theirs.xml"; then
  bad "precondition: theirs unexpectedly already has RECORD_AUDIO"
else
  pass "precondition: blind-copy(theirs) would DROP fork's RECORD_AUDIO (the loss class)"
fi

# manifest-union strategy (semantic union, not textual — additions at the same
# insertion point must NOT spurious-conflict).
cs_merge manifest-union "$WORK/ours.xml" "$WORK/base.xml" "$WORK/theirs.xml" "$WORK/out.xml"
rc=$?
[ "$rc" -eq 0 ] && pass "merge returned clean (rc=0)" || bad "merge rc=$rc (expected 0)"
grep -q RECORD_AUDIO "$WORK/out.xml"                  && pass "fork RECORD_AUDIO preserved"            || bad "RECORD_AUDIO lost"
grep -q FOREGROUND_SERVICE_MICROPHONE "$WORK/out.xml" && pass "fork FOREGROUND_SERVICE_MICROPHONE preserved" || bad "FOREGROUND_SERVICE_MICROPHONE lost"
grep -q POST_NOTIFICATIONS "$WORK/out.xml"            && pass "template POST_NOTIFICATIONS picked up"  || bad "template POST_NOTIFICATIONS missing"
grep -q '<<<<<<<' "$WORK/out.xml"                     && bad "unexpected conflict markers in clean merge" || pass "no conflict markers"

echo "── Case B: true conflict surfaces (same line changed both sides) ──"
printf '<application android:label="BASE" />\n'   > "$WORK/b2.xml"
printf '<application android:label="FORK" />\n'   > "$WORK/o2.xml"
printf '<application android:label="UPSTREAM" />\n'> "$WORK/t2.xml"
cs_merge_3way "$WORK/o2.xml" "$WORK/b2.xml" "$WORK/t2.xml" "$WORK/out2.xml"
rc=$?
[ "$rc" -eq 1 ] && pass "conflict returned rc=1 (surfaced, not silently shipped)" || bad "conflict rc=$rc (expected 1)"
grep -q '<<<<<<<' "$WORK/out2.xml" && pass "conflict markers present for review" || bad "conflict markers missing"

echo
if [ "$fail" -eq 0 ]; then echo "✅ merge-3way: all assertions passed"; exit 0
else echo "❌ merge-3way: failures above"; exit 1; fi
