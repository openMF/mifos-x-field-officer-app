#!/usr/bin/env bash
# kotlin-checks.sh pre-commit|pre-push — the git hooks' Spotless / Detekt / dependency-guard checks,
# scoped to what the commit or push CHANGES. The hooks (pre-commit.sh / pre-push.sh) only call this, so
# a template sync updates the checks without reinstalling hooks.
#
# Why scoped: the old hooks ran `./gradlew spotlessCheck detekt dependencyGuard` over the whole repo on
# every commit AND again on every push — 25+ min on a fork with ~40 feature modules, and a fork's
# pre-existing Detekt findings blocked every push no matter what it touched. Now:
#   pre-commit  Spotless APPLIES to the staged .kt/.kts files and re-stages them; Detekt checks the
#               staged .kt files.
#   pre-push    Spotless checks and Detekt checks the .kt/.kts files the pushed commits change;
#               dependencyGuard runs only when a build file changed (warn-only: a local `.local=true`
#               lib-integrate makes it differ from the committed baselines; CI enforces it).
# A tree that passed is remembered (`.git/kpt-checked-trees`), so a push of commits the pre-commit hook
# already checked costs nothing. Fixtures under */tests/** are deliberately malformed and are skipped.
set -uo pipefail
stage="${1:?usage: kotlin-checks.sh pre-commit|pre-push}"
cd "$(git rev-parse --show-toplevel)" || exit 1
CACHE="$(git rev-parse --git-path kpt-checked-trees)"
ZERO=0000000000000000000000000000000000000000

branch="$(git rev-parse --abbrev-ref HEAD)"
if [ "$branch" = master ] || [ "$branch" = dev ]; then
  echo "🛑 On '$branch' — commit and push from a feature branch." >&2
  exit 1
fi

# checked <tree> — this tree already passed (pre-commit or a previous push).
checked() { [ -f "$CACHE" ] && grep -qxF -- "$1" "$CACHE"; }
# remember <tree> — record a tree that passed.
remember() { echo "$1" >> "$CACHE"; }

# kotlin_files — the existing .kt/.kts files among stdin's paths, fixtures and build output excluded.
kotlin_files() {
  grep -E '\.kts?$' | grep -vE '(^|/)(build|tests)/' | while IFS= read -r f; do [ -f "$f" ] && echo "$f"; done
}

# module_of <file> — the Gradle path of the project owning <file>: the nearest directory with a
# build.gradle.kts. build-logic/ (an included build) and root scripts belong to the root project,
# whose Spotless formats them.
module_of() {
  local d; d="$(dirname "$1")"
  case "$1" in build-logic/*) echo ":"; return ;; esac
  while [ "$d" != . ] && [ ! -f "$d/build.gradle.kts" ]; do d="$(dirname "$d")"; done
  if [ "$d" = . ]; then echo ":"; else echo ":${d//\//:}"; fi
}

# spotless <Apply|Check> <files…> — only those files, and only their modules' Spotless tasks. The bare
# `spotlessCheck` runs every module's task even when `-PspotlessFiles` matches one file: 8 min for one
# file on the template, against ~15 s scoped.
spotless() {
  local task="$1" re f; shift
  re="$(printf '%s\n' "$@" | sed "s|^|$PWD/|; s|[.]|\\\\.|g" | paste -sd, -)"
  local tasks=(); while IFS= read -r m; do tasks+=("${m%:}:spotless$task"); done \
    < <(for f in "$@"; do module_of "$f"; done | sort -u)
  ./gradlew "${tasks[@]}" -PspotlessFiles="$re" --daemon -q
}

# detekt <files…> — the whole-repo Detekt pass, narrowed to those .kt files (build-logic Detekt.kt).
detekt() {
  local kt; kt="$(printf '%s\n' "$@" | grep '\.kt$' | paste -sd, -)"
  [ -n "$kt" ] || return 0
  ./gradlew :cmp-android:detekt -Pkpt.detekt.files="$kt" --daemon -q
}

if [ "$stage" = pre-commit ]; then
  tree="$(git write-tree)"
  checked "$tree" && exit 0
  files=(); while IFS= read -r f; do files+=("$f"); done < <(git diff --cached --name-only --diff-filter=ACMR | kotlin_files)
  if [ "${#files[@]}" -gt 0 ]; then
    echo "▶ Spotless + Detekt on ${#files[@]} staged Kotlin file(s)"
    spotless Apply "${files[@]}" || { echo "💥 Spotless could not format the staged files." >&2; exit 1; }
    git add -- "${files[@]}"
    detekt "${files[@]}" || { echo "💥 Detekt found issues in the staged files (report: cmp-android/build/reports/detekt)." >&2; exit 1; }
  fi
  remember "$(git write-tree)"
  exit 0
fi

# pre-push: stdin carries `<local ref> <local sha> <remote ref> <remote sha>` per pushed ref.
rc=0
while read -r _lref lsha _rref rsha; do
  [ "$lsha" = "$ZERO" ] && continue                                   # deleting a remote ref
  tree="$(git rev-parse "$lsha^{tree}")"
  checked "$tree" && continue
  if [ "$rsha" != "$ZERO" ] && git cat-file -e "$rsha^{commit}" 2>/dev/null; then base="$rsha"
  else base="$(git merge-base "$lsha" refs/remotes/origin/HEAD 2>/dev/null || git rev-list --max-parents=0 "$lsha" | tail -1)"; fi
  changed="$(git diff --name-only --diff-filter=ACMR "$base" "$lsha")"
  files=(); while IFS= read -r f; do files+=("$f"); done < <(printf '%s\n' "$changed" | kotlin_files)
  if [ "${#files[@]}" -gt 0 ]; then
    echo "▶ Spotless + Detekt on ${#files[@]} pushed Kotlin file(s)"
    spotless Check "${files[@]}" || { echo "💥 Spotless: run ./gradlew spotlessApply and amend." >&2; rc=1; continue; }
    detekt "${files[@]}" || { echo "💥 Detekt found issues in the pushed files (report: cmp-android/build/reports/detekt)." >&2; rc=1; continue; }
  fi
  if printf '%s\n' "$changed" | grep -qE '\.kts$|^gradle/libs\.versions\.toml$|/dependencies/'; then
    ./gradlew dependencyGuard --daemon -q \
      || echo "⚠ dependencyGuard differs from the committed baselines — CI enforces it; refresh with ./gradlew dependencyGuardBaseline (lib-integrate .local=false) if intended." >&2
  fi
  remember "$tree"
done
exit "$rc"
