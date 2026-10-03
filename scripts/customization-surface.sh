#!/usr/bin/env bash
# customization-surface.sh — reader + validator for customization-surface.yaml
# ─────────────────────────────────────────────────────────────────────────────
# The single consumer-facing API for the fork-ownership contract. Pure bash + awk
# (no yq/python/jq dependency, so it runs on any fork out of the box).
#
# Library use (source it):
#     source scripts/customization-surface.sh
#     owner=$(cs_resolve_owner "cmp-android/src/main/AndroidManifest.xml")   # -> merge
#     strat=$(cs_resolve_strategy "cmp-android/src/main/AndroidManifest.xml") # -> manifest-union
#
# CLI use:
#     scripts/customization-surface.sh resolve <path>   # print owner (+ strategy)
#     scripts/customization-surface.sh report           # owner of every tracked path
#     scripts/customization-surface.sh verify            # CI: fail if any tracked
#                                                        # path only matches the default
#     scripts/customization-surface.sh --verify          # CI: fail if any ownership glob
#                                                        # matches ZERO real paths (dead-glob
#                                                        # / contract-rot guard; E0/T2)
# ─────────────────────────────────────────────────────────────────────────────
# NOTE: shell options are set only on direct execution (bottom), NOT on source —
# sourcing must not leak `set -u`/`pipefail` into a caller like scripts/white-label/sync-dirs.sh.

CS_SELF="${BASH_SOURCE[0]}"
CS_ROOT="$(cd "$(dirname "$CS_SELF")/.." && pwd)"
CS_CONTRACT="${CS_CONTRACT:-$CS_ROOT/customization-surface.yaml}"

# Parse the YAML into "glob<TAB>owner<TAB>strategy<TAB>default" rows, in file order.
# Handles both block form (- glob:\n  owner:) and inline form (- { glob: x, owner: y }).
cs_parse_rules() {
  awk '
    function emit() {
      if (glob != "") printf "%s|%s|%s|%s\n", glob, owner, strat, def
      glob=""; owner=""; strat=""; def="0"
    }
    # inline form:  - { glob: "x", owner: y, strategy: z }
    /^[[:space:]]*-[[:space:]]*\{/ {
      emit()               # flush any pending block-form rule first
      line=$0
      g=line; sub(/.*glob:[[:space:]]*"?/,"",g); sub(/"?[[:space:]]*,.*/,"",g); sub(/"?[[:space:]]*}.*/,"",g)
      o=line; sub(/.*owner:[[:space:]]*/,"",o); sub(/[[:space:]]*,.*/,"",o); sub(/[[:space:]]*}.*/,"",o)
      glob=g; owner=o; strat=""; def="0"
      if (line ~ /default:[[:space:]]*true/) def="1"
      emit(); next
    }
    # block form start — extract the QUOTED value (drops any trailing # comment)
    /^[[:space:]]*-[[:space:]]*glob:/ {
      emit()
      g=$0; sub(/.*glob:[[:space:]]*"/,"",g); sub(/".*/,"",g)
      glob=g; next
    }
    # owner/strategy are single tokens — take the first word (drops trailing comments)
    /^[[:space:]]*owner:/    { o=$0; sub(/.*owner:[[:space:]]*/,"",o);    sub(/[[:space:]].*/,"",o); owner=o; next }
    /^[[:space:]]*strategy:/ { s=$0; sub(/.*strategy:[[:space:]]*/,"",s); sub(/[[:space:]].*/,"",s); strat=s; next }
    /^[[:space:]]*default:[[:space:]]*true/ { def="1"; next }
    END { emit() }
  ' "$CS_CONTRACT"
}

# Convert a contract glob to an anchored ERE.
#   **/x -> (.*/)?x   |   ** -> .*   |   * -> [^/]*   |   . escaped
cs_glob_to_regex() {
  local g="$1"
  local s1=$'\x01' s2=$'\x02'   # sentinels — never occur in a path glob
  g="${g//./\\.}"
  # Placeholder the multi-segment tokens FIRST so the single-`*` pass below can't
  # re-clobber the `*` inside their `.*` expansions (globstar → sentinel → restore).
  g="${g//\*\*\//$s1}"   # **/  (match zero+ segments)
  g="${g//\*\*/$s2}"     # **   (match anything)
  g="${g//\*/[^/]*}"     # *    (match within one segment)
  g="${g//$s1/(.*/)?}"
  g="${g//$s2/.*}"
  printf '^%s$' "$g"
}

# Load + compile the rules ONCE into parallel arrays (parse is expensive; matching
# is hot). Idempotent — safe to call before every resolve.
CS_LOADED=0
cs_load_rules() {
  [ "$CS_LOADED" = 1 ] && return
  CS_RX=(); CS_OWNER=(); CS_STRAT=(); CS_DEF=(); CS_GLOB=()
  local glob owner strat def
  while IFS='|' read -r glob owner strat def; do
    [ -z "$glob" ] && continue
    CS_RX+=("$(cs_glob_to_regex "$glob")")
    CS_OWNER+=("$owner"); CS_STRAT+=("$strat"); CS_DEF+=("$def"); CS_GLOB+=("$glob")
  done < <(cs_parse_rules)
  CS_LOADED=1
}

# ── --verify zero-match-rule guard (pure-white-label-store5-network E0/T2) ────────
# Every NON-default ownership glob MUST match >=1 real path in the tree — a rule that
# resolves to zero paths silently mis-owns whatever the author MEANT to cover (exactly
# how the 4 dead globs `core/store/{economic,banking}/**`, `appStoreModule*.kt`,
# `core/**/src/**/local/**` survived). This turns contract rot into a hard failure.
#
# "Real path" universe = git-tracked files UNION files present on disk (so gitignored-
# but-materialized fork paths — local.properties, gradle/fork.properties, secrets/live/**,
# secrets/.sync-meta.json — count as matches, never false-flagged). Heavy build output
# (build/, .gradle/, .kotlin/, node_modules/, .git/) is pruned.
#
# EXEMPT: `owner: generated` rules describe REGENERABLE artifacts (store og-images /
# screenshots / metadata) that are legitimately absent from a freshly-synced / bare
# template tree — flagging them would be a false positive by design (a fork regenerates
# them from app-profile/). The catch-all default `**` rule is skipped too.
#
# Returns 0 iff every enforced rule matches >=1 path; 1 (listing the offenders) otherwise.
cs_build_path_universe() {
  {
    git -C "$CS_ROOT" ls-files 2>/dev/null
    ( cd "$CS_ROOT" 2>/dev/null && \
      find . \( -path './.git' -o -path '*/build' -o -path '*/.gradle' \
                -o -path '*/.kotlin' -o -path '*/node_modules' \) -prune \
             -o -type f -print 2>/dev/null | sed 's#^\./##' )
  } | sort -u
}

cs_verify_zero_match() {
  cs_load_rules
  local universe rc=0 i n
  universe="$(mktemp)"
  cs_build_path_universe > "$universe"
  if [ ! -s "$universe" ]; then
    rm -f "$universe"
    echo "❌ --verify: could not enumerate any real paths under $CS_ROOT" >&2
    return 1
  fi
  for i in "${!CS_RX[@]}"; do
    [ "${CS_DEF[$i]}" = "1" ] && continue            # catch-all default — matches everything by design
    [ "${CS_OWNER[$i]}" = "generated" ] && continue  # regenerable artifacts, legitimately absent
    n="$(grep -cE "${CS_RX[$i]}" "$universe" 2>/dev/null || true)"
    if [ "${n:-0}" -eq 0 ]; then
      echo "❌ ZERO-MATCH RULE (owner=${CS_OWNER[$i]}, matches no real path): ${CS_GLOB[$i]}" >&2
      rc=1
    fi
  done
  rm -f "$universe"
  if [ "$rc" -eq 0 ]; then
    echo "✅ --verify: every ownership rule matches >=1 real path (generated carve-outs exempt)"
  else
    echo "❌ --verify: one or more ownership rules match ZERO real paths — dead/mis-declared contract rot" >&2
  fi
  return "$rc"
}

# First matching rule → set globals CS_M_OWNER / CS_M_STRAT / CS_M_DEFAULT.
# No subshell / no print (hot path — called once per tracked file in `verify`).
cs_match_g() {
  cs_load_rules
  local path="$1" i
  for i in "${!CS_RX[@]}"; do
    if [[ "$path" =~ ${CS_RX[$i]} ]]; then
      CS_M_OWNER="${CS_OWNER[$i]}"; CS_M_STRAT="${CS_STRAT[$i]}"; CS_M_DEFAULT="${CS_DEF[$i]}"
      return 0
    fi
  done
  # TEMPLATE-FIRST fallback. This repo IS the template: the default answer to "who owns this?" is
  # the template, and FORK territory is declared explicitly (`core/**`, `feature/**`, `app-profile/**`,
  # the branding carve-outs) rather than inferred from silence.
  #
  # It used to be `fork` — "never clobber unknown" — which is the safe default for a CONSUMER but the
  # wrong one for a template: a directory the template ADDS and nobody writes a rule for silently
  # becomes fork-owned and never syncs to anyone. `tools/**` (the KSP processors driving every
  # @StoreProvider / @DbEntity / @ApiBinding) and `.bundle/**` both sat in exactly that state.
  #
  # Flipping is safe BECAUSE fork territory is explicitly claimed: `core/store/.../quests/X.kt` and
  # `feature/quests/X.kt` resolve `fork` through their module catch-alls, not through this line.
  # Verified against the tree at the time of the change — all 2712 tracked files matched an explicit
  # rule, so ZERO existing paths change owner. Only genuinely-unclaimed NEW paths move, and by the
  # template-first principle those are the template's.
  CS_M_OWNER="template"; CS_M_STRAT=""; CS_M_DEFAULT="1"
}

cs_resolve_owner()    { cs_match_g "$1"; printf '%s' "$CS_M_OWNER"; }
cs_resolve_strategy() { cs_match_g "$1"; printf '%s' "$CS_M_STRAT"; }
cs_is_default()       { cs_match_g "$1"; [ "$CS_M_DEFAULT" = "1" ]; }

# 3-way merge driver for a `merge`-owned file. This is the general merge strategy
# behind manifest-union / catalog-3way / include-union / kotlin-3way / strings-union:
# git merge-file cleanly unions non-overlapping additions (e.g. a fork's extra
# <uses-permission> lines) and only emits conflict markers on a true overlap.
#
#   cs_merge_3way <ours> <base> <theirs> [<out>]
#     ours   = the fork's current file        base = common ancestor
#     theirs = the upstream/template file      out  = result (default: ours, in place)
#   returns  0 clean · 1 merged WITH conflict markers (needs review) · 2 error
cs_merge_3way() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}" rc tmp
  [ -f "$ours" ] && [ -f "$base" ] && [ -f "$theirs" ] || {
    echo "cs_merge_3way: need existing <ours> <base> <theirs>" >&2; return 2; }
  tmp="$(mktemp)"
  # -p prints the merged result (does not mutate inputs); exit = conflict count.
  git merge-file -p "$ours" "$base" "$theirs" > "$tmp"; rc=$?
  if [ "$rc" -ge 128 ]; then rm -f "$tmp"; echo "cs_merge_3way: git merge-file error ($rc)" >&2; return 2; fi
  mv "$tmp" "$out"
  [ "$rc" -eq 0 ] && return 0 || return 1
}

# Semantic AndroidManifest union — the `manifest-union` strategy. A textual 3-way
# spurious-conflicts when fork + template both insert permission lines at the same
# spot, so union by android:name instead: take THEIRS (the template's structurally
# updated manifest) and inject every <uses-permission>/<uses-feature> line present
# in OURS whose android:name theirs lacks. Always clean (returns 0).
#
#   cs_merge_manifest <ours> <theirs> [<out>]
cs_merge_manifest() {
  local ours="$1" theirs="$2" out="${3:-$1}" tmp add
  [ -f "$ours" ] && [ -f "$theirs" ] || { echo "cs_merge_manifest: need <ours> <theirs>" >&2; return 2; }
  local theirs_names; theirs_names="$(grep -oE 'android:name="[^"]*"' "$theirs" 2>/dev/null | sort -u)"
  # Extract each uses-permission/uses-feature ELEMENT (robust to one-per-line AND
  # multiple-per-line manifests); add the ones whose android:name theirs lacks.
  add="$(grep -oE '<uses-(permission|feature)[^>]*/?>' "$ours" 2>/dev/null | while IFS= read -r el; do
        nm="$(printf '%s' "$el" | grep -oE 'android:name="[^"]*"' | head -1)"
        [ -n "$nm" ] || continue
        printf '%s\n' "$theirs_names" | grep -qxF "$nm" || printf '    %s\n' "$el"
      done)"
  if [ -z "$add" ]; then cp "$theirs" "$out"; return 0; fi
  local addf; addf="$(mktemp)"; printf '%s\n' "$add" > "$addf"
  tmp="$(mktemp)"
  # Read the injected lines from a file (awk -v cannot carry newlines).
  awk -v addf="$addf" '
    function dumpadd(   l) { while ((getline l < addf) > 0) print l; close(addf) }
    /<application/ && !ins { dumpadd(); ins=1 }
    /<\/manifest>/ && !ins { dumpadd(); ins=1 }
    { print }
  ' "$theirs" > "$tmp"
  mv "$tmp" "$out"; rm -f "$addf"
  return 0
}

# ── strings-union (localized composeResources strings.xml) ───────────────────
# Declared on 4 contract rules since the merge class was introduced, and until now it had NO
# implementation: `cs_merge` had no branch for it, so it fell through to `cs_merge_3way` — a plain
# `git merge-file`. The contract promised a union and the engine performed a line merge.
#
# That gap is invisible while a fork and the template share an ancestor for the file. It stops being
# invisible the moment they do not: locale files are typically ADD/ADD (a fork seeds its own via
# /idea-locale while the template ships its own), and with no base a line merge conflicts on the
# WHOLE FILE. Measured on a real mbs/cappy full sync against merged dev: 90 of 98 conflicts were
# strings-union, every one a whole-file marker starting at line 2.
#
# A resource file is a KEYED SET, not prose — `<string name="x">` entries are addressed by name and
# their order carries no meaning, so a union is both possible and obviously right:
#
#   key only in theirs        → take it (the template added a string; the fork needs it)
#   key only in ours          → keep it (the fork's own string, or its translation)
#   key in both, same         → one copy
#   key in both, different    → THE FORK WINS
#
# The fork winning is the whole point on this surface. The template ships SOURCE text — `app_name`
# is "Money Toolkit" upstream and "Cappy" downstream — so taking the template's value would rename
# the fork's app on every sync and silently discard real translations. A fork that wants the
# template's new wording deletes its own key.
#
# Entry-level, not line-level: `<string-array>` and `<plurals>` span lines, so each element is read
# from its opening tag to its matching close and carried whole.
cs_merge_strings() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
  [ -f "$ours" ] || { [ -f "$theirs" ] && cp "$theirs" "$out"; return 0; }
  [ -f "$theirs" ] || return 0

  local tmp; tmp="$(mktemp)"
  awk -v oursf="$ours" '
    # Emit "name\x01<entry text>" for every top-level resource element in FILE.
    function slurp(file, arr,    line, name, buf, depth, tag) {
      while ((getline line < file) > 0) {
        if (line ~ /<(string|string-array|plurals|bool|integer|color|dimen)[ >]/) {
          name = line; sub(/.*name="/, "", name); sub(/".*/, "", name)
          buf = line
          # self-closed or closed on the same line → done
          if (line ~ /\/>/ || line ~ /<\/(string|string-array|plurals|bool|integer|color|dimen)>/) {
            arr[name] = buf; continue
          }
          depth = 1
          while (depth > 0 && (getline line < file) > 0) {
            buf = buf "\n" line
            if (line ~ /<\/(string-array|plurals)>/) depth--
            else if (line ~ /<(string-array|plurals)[ >]/) depth++
            else if (line ~ /<\/string>/) depth--
          }
          arr[name] = buf
        }
      }
      close(file)
    }
    BEGIN { slurp(oursf, ourset) }
    # Walk THEIRS, replacing any entry the fork also defines with the fork version.
    {
      if ($0 ~ /<(string|string-array|plurals|bool|integer|color|dimen)[ >]/) {
        nm = $0; sub(/.*name="/, "", nm); sub(/".*/, "", nm)
        # consume the whole element from theirs
        buf = $0
        if (!($0 ~ /\/>/ || $0 ~ /<\/(string|string-array|plurals|bool|integer|color|dimen)>/)) {
          d = 1
          while (d > 0 && (getline nxt) > 0) {
            buf = buf "\n" nxt
            if (nxt ~ /<\/(string-array|plurals)>/) d--
            else if (nxt ~ /<(string-array|plurals)[ >]/) d++
            else if (nxt ~ /<\/string>/) d--
          }
        }
        if (nm in ourset) { print ourset[nm]; seen[nm] = 1 }
        else              { print buf }
        next
      }
      # Before the closing </resources>, append every fork-only entry.
      if ($0 ~ /<\/resources>/) {
        for (k in ourset) if (!(k in seen)) print ourset[k]
      }
      print
    }
  ' "$theirs" > "$tmp"

  # Never ship an empty or truncated resource file: if the union lost the root element, keep ours.
  if ! grep -q "</resources>" "$tmp"; then
    rm -f "$tmp"
    return 1
  fi
  mv "$tmp" "$out"
  return 0
}

# ── yaml-schema-merge (app-profile deep-merge) ───────────────────────────────
# PLACEHOLDER classifier — a fork scalar is TEMPLATE-owned (loses on merge) when its
# value still equals a template default OR its source line carries a `# PLACEHOLDER`
# marker. Only a NON-placeholder fork value at an AppProfile::MAP identity/org/store
# key path is allowed to win over the template.
CS_PLACEHOLDER_RE='(^|[[:space:]])#[[:space:]]*PLACEHOLDER|com\.example\.app|App Toolkit|https?://(demo|api|staging)\.example\.com|YOUR_|Your Organization|you@example\.com'

# yaml-schema-merge — deep-merge <theirs=template> INTO <ours=fork>, key-path by key-path.
#   template wins: the whole key STRUCTURE + every default + every new demo access-point
#                  (any key the fork lacks is inherited from the template schema).
#   fork wins:     any AppProfile::MAP identity/org/store scalar whose fork value is
#                  NON-placeholder (classified via the MAP key set + CS_PLACEHOLDER_RE);
#                  plus a fork's own rows in any UNION LIST win by identity, while template-only
#                  rows are appended (union-by-identity). Union lists + their identity field:
#                  network.access_points/id · core_store.stores/id · core_store.packages/id ·
#                  core_store.cache_keys/name|fn · database.packages/id.
#                  This was `access_points` ONLY: every other list came from the template wholesale,
#                  so a fork that declared its own store, cache key, DAO or package silently LOST it
#                  on the next sync — and the generated Kotlin then faithfully regenerated without it,
#                  with no merge conflict to notice.
# TRUE 3-WAY (diff3): the template supplies the schema/keys/defaults (THEIRS, emitted as the
# structure), and the fork WINS every leaf it changed from the BASE — the template state the fork
# LAST SYNCED FROM (.template-version#template_sha). That is: identity scalars (MAP) win as before,
# AND any other key the fork customized (OURS[path] != BASE[path]) is PRESERVED, while keys the fork
# left at the template default follow the template. So a fork's NON-identity customization survives a
# sync (the 2-way overlay used to revert it to the new template default). `base` absent → falls back
# to the identity-only 2-way (a never-synced fork has no ancestor). Union-by-identity unchanged.
#   cs_merge_yaml_schema <ours=fork> <base> <theirs=template> [<out>]
#   returns 0 merged-clean · 2 error
cs_merge_yaml_schema() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
  [ -f "$ours" ] && [ -f "$theirs" ] || {
    echo "cs_merge_yaml_schema: need <ours=fork> <base> <theirs=template>" >&2; return 2; }

  # Load the identity/org/store yaml key paths the fork is allowed to win — the RHS
  # (dotted yaml path) of every `"flat.key" => "dotted.path"` row in AppProfile::MAP.
  local cfgrb="$CS_ROOT/deployment/_shared/config.rb"
  local mapfile; mapfile="$(mktemp)"
  if [ -f "$cfgrb" ]; then
    awk -F'=>' '
      /"[^"]+"[[:space:]]*=>[[:space:]]*"[^"]+"/ {
        v=$2; sub(/#.*/,"",v); gsub(/[" ,]/,"",v); if (v!="") print v
      }' "$cfgrb" > "$mapfile"
  fi

  # ── 3-WAY: a fork also WINS any leaf it CHANGED from the BASE (template @ last-synced sha), not just
  # the MAP identity scalars — so a fork's non-identity customization survives. Append those dotted
  # paths to the forkwins set (mapfile). Base absent (never-synced fork) → skip → identity-only 2-way.
  if [ -f "$base" ]; then
    awk '
      function lead(s,   n){ n=0; while(substr(s,n+1,1)==" ") n++; return n }
      function keyof(s,   t){ t=s; sub(/^[ ]+/,"",t); sub(/:.*/,"",t); return t }
      function restof(s,   t){ t=s; sub(/^[ ]*[^:]+:/,"",t); sub(/^[ ]+/,"",t); sub(/[ \t]+#.*$/,"",t); sub(/[ \t]+$/,"",t); return t }
      function pathpush(ind,k,   p,i){ while(sp>0 && sind[sp]>=ind) sp--; sp++; sind[sp]=ind; skey[sp]=k; p=skey[1]; for(i=2;i<=sp;i++) p=p"."skey[i]; return p }
      FNR==1 { sp=0 }
      FNR==NR {                                   # BASE pass — index scalar leaves by dotted path
        if ($0 ~ /^[ ]*#/ || $0 ~ /^[ ]*$/) next; ind=lead($0); c=$0; sub(/^[ ]+/,"",c)
        if (c ~ /^- /) next
        if (c ~ /:/) { k=keyof($0); r=restof($0); p=pathpush(ind,k); if (r!="" && r !~ /^[|>]/) baseval[p]=r }
        next
      }
      {                                           # OURS pass — emit paths the fork changed from base
        if ($0 ~ /^[ ]*#/ || $0 ~ /^[ ]*$/) next; ind=lead($0); c=$0; sub(/^[ ]+/,"",c)
        if (c ~ /^- /) next
        if (c ~ /:/) { k=keyof($0); r=restof($0); p=pathpush(ind,k); if (r!="" && r !~ /^[|>]/ && (p in baseval) && baseval[p]!=r) print p }
      }
    ' "$base" "$ours" >> "$mapfile"
  fi

  local tmp; tmp="$(mktemp)"
  # Two-file awk: FIRST pass indexes the fork (scalar leaves by dotted path + its
  # union lists); SECOND pass emits the template as the schema base, overlaying fork-won
  # scalars and unioning every declared list by its identity field.
  awk -v mapf="$mapfile" -v ph="$CS_PLACEHOLDER_RE" '
    function spaces(n,   s){ s=""; while(n-->0) s=s" "; return s }
    function lead(s,   n){ n=0; while(substr(s,n+1,1)==" ") n++; return n }
    function keyof(s,   t){ t=s; sub(/^[ ]+/,"",t); sub(/:.*/,"",t); return t }
    function restof(s,   t){ t=s; sub(/^[ ]*[^:]+:/,"",t); sub(/^[ ]+/,"",t); return t }
    function stripc(v,   t){ t=v; sub(/[ \t]+#.*$/,"",t); sub(/[ \t]+$/,"",t); return t }
    function pathpush(ind,k,   p,i){
      while (sp>0 && sind[sp]>=ind) sp--
      sp++; sind[sp]=ind; skey[sp]=k
      p=skey[1]; for (i=2;i<=sp;i++) p=p"."skey[i]
      return p
    }
    # Identity field per union list. NOT every list keys on `id`: cache_keys are either a constant
    # (`name`) or a builder (`fn`).
    # Keyed on the FULL DOTTED PATH, not the leaf. Two different lists can share a leaf name —
    # `core_store.packages` and `database.packages` both exist — and keying on the leaf made them
    # share one fork buffer, so the second list emitted the rows of the first one as well. That is a
    # duplicate row in app-profile, i.e. a duplicate generated declaration. Matching whole paths
    # also means a NEW list named `packages` under a third parent is not silently swept in.
    function union_spec(p){
      if (p=="network.access_points") return "id"
      if (p=="core_store.stores")     return "id"
      if (p=="core_store.packages")   return "id"
      if (p=="core_store.cache_keys") return "name|fn"
      if (p=="database.packages")     return "id"
      return ""
    }
    # Row identity, for BOTH yaml shapes this file mixes: block rows (`- id: main`, as
    # access_points use) and inline maps (`- { id: alerts, owner: template }`, as every list added
    # later uses). Matching only the block form is why generalising by key name alone is not enough.
    function rowid(c, spec,   n, arr, i, f, v){
      if (spec=="*") { v=c; sub(/^-[ ]*/,"",v); return stripc(v) }
      n=split(spec, arr, "|")
      for (i=1;i<=n;i++) {
        f=arr[i]
        if (match(c, "[{,][ ]*" f "[ ]*:[ ]*[^,}]+")) {          # inline map
          v=substr(c, RSTART, RLENGTH); sub("^[{,][ ]*" f "[ ]*:[ ]*", "", v); return stripc(v)
        }
        if (match(c, "^-?[ ]*" f "[ ]*:")) {                      # block row / continuation
          v=c; sub("^-?[ ]*" f "[ ]*:[ ]*", "", v); return stripc(v)
        }
      }
      return ""
    }
    # flush the currently-buffered TEMPLATE list item, emitting it ONLY when its identity is not
    # already provided by the fork (so template-only rows are appended, fork rows win).
    function ap_flush(   i){
      if (nib>0) {
        if (!((apkey SUBSEP curid) in forkid)) for (i=1;i<=nib;i++) print ap_itembuf[i]
        nib=0; curid=""
      }
    }
    BEGIN { while ((getline k < mapf) > 0) if (k!="") forkwins[k]=1 }
    FNR==1 { sp=0; ap=0 }   # reset the indentation stack + ap-state per input file

    # ── FORK PASS — index scalar leaves + capture the access_points block ──
    FNR==NR {
      line=$0
      if (line ~ /^[ ]*#/ || line ~ /^[ ]*$/) next
      ind=lead(line); content=line; sub(/^[ ]+/,"",content)
      if (ap) {
        if (ind<=ap_ind) { ap=0 }              # dedent → end of this fork list
        else {
          forkbuf[apkey, ++nfb[apkey]]=line    # preserve the fork row verbatim
          idv=rowid(content, union_spec(apkey))
          if (idv!="") forkid[apkey, idv]=1
          next
        }
      }
      if (content ~ /^- /) next
      if (content ~ /:/) {
        k=keyof(line); r=restof(line); p=pathpush(ind,k)
        if (union_spec(p)!="") { ap=1; ap_ind=ind; apkey=p; next }
        if (r!="" && r !~ /^[|>]/) {           # scalar leaf
          forkval[p]=stripc(r)
          forkph[p]=(line ~ ph) ? 1 : 0
        }
      }
      next
    }

    # ── TEMPLATE PASS — emit schema base, overlay fork identity, union access_points ──
    {
      line=$0
      if (line ~ /^[ ]*#/ || line ~ /^[ ]*$/) { if (ap) next; print line; next }
      ind=lead(line); content=line; sub(/^[ ]+/,"",content)
      if (ap) {
        if (ind<=ap_ind) { ap_flush(); ap=0 }  # end of template list → fall through
        else {
          if (content ~ /^- /) {
            ap_flush(); ap_itembuf[++nib]=line
            idv=rowid(content, union_spec(apkey)); if (idv!="") curid=idv
          } else {
            ap_itembuf[++nib]=line
            idv=rowid(content, union_spec(apkey)); if (idv!="") curid=idv
          }
          next
        }
      }
      if (content ~ /:/) {
        k=keyof(line); r=restof(line); p=pathpush(ind,k)
        if (union_spec(p)!="") {
          print line                            # the list header
          for (i=1;i<=nfb[p];i++) print forkbuf[p, i]   # fork rows preserved (win by identity)
          ap=1; ap_ind=ind; apkey=p; nib=0; curid=""
          next
        }
        if (r!="" && r !~ /^[|>]/ && (p in forkwins) && (p in forkval) && forkph[p]==0) {
          print spaces(ind) k ": " forkval[p]   # fork identity scalar wins
          next
        }
      }
      print line
    }
    END { if (ap) ap_flush() }
  ' "$ours" "$theirs" > "$tmp"

  rm -f "$mapfile"
  mv "$tmp" "$out"
  return 0
}

# Semantic settings.gradle.kts merge — the `include-union` strategy. A textual 3-way unions the
# fork's + template's `include(":module")` lines, but the template also declares its OWN demo/app
# feature modules (feature/showcase, feature/loans, feature/rates, …) that a downstream fork does
# NOT have and that the sync does NOT copy in (they are not template-shared modules). The blind
# union therefore references non-existent module dirs and breaks Gradle configuration
# ("Configuring project ':feature:showcase' without an existing directory"). After the union, DROP
# every `include(":a:b")` whose resolved dir `a/b/` is absent on disk — keeping the fork's real
# modules + the shared modules the sync actually materialized, never a template-only phantom.
#   cs_merge_include_union <ours> <base> <theirs> [<out>]
cs_merge_include_union() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}" rc
  cs_merge_3way "$ours" "$base" "$theirs" "$out"; rc=$?
  local root; root="$(cd "$(dirname "$out")" 2>/dev/null && pwd)"; [ -n "$root" ] || root="$(pwd)"
  local tmp coord; tmp="$(mktemp)"
  while IFS= read -r line || [ -n "$line" ]; do
    coord="$(printf '%s' "$line" | sed -nE 's/^[[:space:]]*include\("?:([A-Za-z0-9:_.-]+)"?\).*/\1/p')"
    if [ -n "$coord" ] && [ ! -d "$root/${coord//://}" ]; then
      continue   # drop a phantom include — module dir does not exist in this fork
    fi
    printf '%s\n' "$line"
  done < "$out" > "$tmp"
  mv "$tmp" "$out"
  return "$rc"
}

# Strategy dispatcher used by scripts/white-label/sync-dirs.sh for a `merge`-owned file.
#   cs_merge <strategy> <ours> <base> <theirs> [<out>]
# cs_merge_properties <ours> <base> <theirs> [<out>] — KEY-LEVEL 3-way for flat `key=value` files
# (gradle.properties, *.properties). A line-based diff3 spuriously conflicts when a fork-changed line
# sits next to a template-changed line even though the KEYS are independent; this merges per key:
#   emit THEIRS (template) as structure/order/comments; per key — fork changed it (OURS != BASE, or no
#   base) → keep the FORK's value; else follow the template (its default / its change / its new key).
#   fork-only keys (absent from the template) are appended (preserved); template-removed keys the fork
#   left untouched drop out (not re-added). Base absent → fork's keys overlay the template (2-way).
cs_merge_properties() {
  local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
  [ -f "$ours" ] && [ -f "$theirs" ] || { echo "cs_merge_properties: need <ours> <base> <theirs>" >&2; return 2; }
  local tmp; tmp="$(mktemp)"
  awk -v ourf="$ours" -v basef="$base" '
    function kof(s,   t){ t=s; sub(/[ \t]*=.*/,"",t); gsub(/^[ \t]+|[ \t]+$/,"",t); return t }
    function vof(s,   t){ t=s; sub(/^[^=]*=/,"",t); return t }
    BEGIN{
      while((getline l < basef)>0){ if(l ~ /^[ \t]*[#!]|^[ \t]*$/) continue; k=kof(l); if(k!=""){ bv[k]=vof(l); bseen[k]=1 } }
      while((getline l < ourf)>0){  if(l ~ /^[ \t]*[#!]|^[ \t]*$/) continue; k=kof(l); if(k!=""){ ov[k]=vof(l); oseen[k]=1 } }
    }
    /^[ \t]*[#!]|^[ \t]*$/ { print; next }        # template comments/blanks preserved (structure)
    {
      k=kof($0); if(k==""){ print; next }
      tseen[k]=1
      if(k in oseen){
        if( (!(k in bseen)) || ov[k]!=bv[k] ){ print k "=" ov[k]; next }   # fork changed it → keep fork
      }
      print                                       # fork unchanged / fork-absent → template value
    }
    END{ for(k in oseen) if(!(k in tseen)) print k "=" ov[k] }             # fork-only keys preserved
  ' "$theirs" > "$tmp" && mv "$tmp" "$out"
  return 0
}

cs_merge() {
  local strat="$1" ours="$2" base="$3" theirs="$4" out="${5:-$2}"
  case "$strat" in
    manifest-union)    cs_merge_manifest      "$ours" "$theirs" "$out" ;;
    include-union)     cs_merge_include_union "$ours" "$base" "$theirs" "$out" ;;
    yaml-schema-merge) cs_merge_yaml_schema   "$ours" "$base" "$theirs" "$out" ;;
    properties-3way)   cs_merge_properties    "$ours" "$base" "$theirs" "$out" ;;
    strings-union)     cs_merge_strings       "$ours" "$base" "$theirs" "$out" ;;
    *)                 cs_merge_3way          "$ours" "$base" "$theirs" "$out" ;;
  esac
}

# ── CLI ──
# white-label-template-completion E0/T1 — the closed set of ownership rows the boundary flip depends on.
# Asserts each via cs_resolve_owner (no brittle count literal). Returns 0 iff all hold.
cs_require_flip_preconditions() {
  local bad=0
  _cs_expect() { # <path> <expected-owner>
    local got; got="$(cs_resolve_owner "$1")"
    [ "$got" = "$2" ] || { echo "❌ flip-precondition: $1 owner=$got, expected $2"; bad=1; }
  }
  # og-images are DERIVED store media (glob `deployment/**/og-images/** → generated`): regenerated from
  # app-profile, never hand-edited, so sync IGNORES them. Assert `generated` — this both reflects the
  # reclassification AND proves the generated-class row is present in the contract.
  _cs_expect "deployment/web/og-images/01_home.png"                generated
  _cs_expect "gradle.properties"                                   merge   # mixed fork-values + template-infra → key-level properties-3way (2026-08-20)
  _cs_expect "secrets-manifest.yaml"                               fork
  _cs_expect "secrets/live/keystore.jks"                           fork
  _cs_expect "tests/anything.sh"                                   template
  # AppStoreRegistry.kt is GONE — stores are declared with @StoreProvider, and the generated
  # AppStoreRegistry/AppCacheKeys/GeneratedStoreBindings are KSP build artifacts under
  # build/generated, so there is no committed file to own.
  # The surviving core/store fork seams are these two.
  # core/model's three-way split. `invoice/` stands for ANY undeclared package — a fork's own model,
  # which must resolve fork. Before the module catch-all existed it resolved `template` off the
  # `core/**` blanket, and a sync would have believed it could overwrite it.
  _cs_expect "core/model/src/commonMain/kotlin/kpt/core/model/invoice/Invoice.kt" fork
  _cs_expect "core/model/src/commonMain/kotlin/kpt/core/model/user/UserData.kt" template
  _cs_expect "core/model/src/commonMain/kotlin/kpt/core/model/banking/Loan.kt" demo-showcase
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/config/ProjectErrorMapper.kt" fork
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/config/ProjectScreenStateDefaults.kt" fork
  # REAL paths, not short synthetic ones. These two asserted `template` only because the short form
  # missed every `**/kpt/core/store/**` rule and fell through to the `core/**` blanket — the fixture
  # was testing a path that does not exist. The real files are showcase stores, so demo-showcase.
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/economic/impl/ExchangeRatesStore.kt" demo-showcase
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/economic/impl/InterestRateSeriesStore.kt" demo-showcase
  # The module catch-all: a fork's own store package must not be claimed by the core/** blanket.
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/invoice/impl/InvoiceStore.kt" fork
  _cs_expect "core/store/src/commonMain/kotlin/kpt/core/store/prefs/impl/UserDataStore.kt" template
  [ "$bad" -eq 0 ] && echo "✅ T1 flip preconditions hold (og-images generated · secrets-manifest/keystore + core/store Project* seams fork · core/model undeclared=fork · gradle.properties merge/properties-3way · tests/core-store-impl template)"
  return "$bad"
}

cs_main() {
  local cmd="${1:-}"; shift || true
  case "$cmd" in
    --verify|verify-zero-match)
      # E0/T2: zero-match-rule guard — exit 1 if ANY enforced glob matches zero real paths.
      cs_verify_zero_match
      ;;
    resolve)
      local p="${1:?usage: resolve <path>}"
      cs_match_g "$p"
      printf '%s\t%s%s\n' "$p" "$CS_M_OWNER" "${CS_M_STRAT:+  (strategy: $CS_M_STRAT)}"
      ;;
    resolve-owner)
      # Print ONLY the resolved owner token (template | fork | merge | generated |
      # demo-showcase) with no path/strategy decoration — the machine-readable twin of
      # `resolve`, so a gate can `[ "$(… resolve-owner <p>)" = demo-showcase ]` without
      # parsing the tab-delimited `resolve` line. (WS01/AC8 — the distinct demo-showcase
      # ownership class is a first-class owner value the contract now emits.)
      local p="${1:?usage: resolve-owner <path>}"
      cs_match_g "$p"
      printf '%s\n' "$CS_M_OWNER"
      ;;
    report)
      # Owner->count tally as index-matched parallel arrays; bash 3.2 (macOS /bin/bash) has no
      # associative arrays, and would silently collapse every owner onto index 0.
      local f; local _owners=(); local _counts=()
      while IFS= read -r f; do
        cs_match_g "$f"
        local _oi=0; local _found=0
        while [ "$_oi" -lt "${#_owners[@]}" ]; do
          if [ "${_owners[$_oi]}" = "$CS_M_OWNER" ]; then
            _counts[$_oi]=$(( ${_counts[$_oi]} + 1 )); _found=1; break
          fi
          _oi=$(( _oi + 1 ))
        done
        [ "$_found" = "0" ] && { _owners+=("$CS_M_OWNER"); _counts+=(1); }
        printf '%s\t%s\n' "$CS_M_OWNER" "$f"
      done < <(git -C "$CS_ROOT" ls-files) | sort
      local _ki=0
      while [ "$_ki" -lt "${#_owners[@]}" ]; do
        printf '# %-9s %d files\n' "${_owners[$_ki]}" "${_counts[$_ki]}" >&2; _ki=$(( _ki + 1 ))
      done
      ;;
    verify)
      local unclassified=0 total=0 f
      while IFS= read -r f; do
        total=$((total+1))
        cs_match_g "$f"
        if [ "$CS_M_DEFAULT" = "1" ]; then
          echo "UNCLASSIFIED (matches only the default rule): $f"
          unclassified=$((unclassified+1))
        fi
      done < <(git -C "$CS_ROOT" ls-files)
      echo "── customization-surface coverage: $((total-unclassified))/$total classified ──"
      if [ "$unclassified" -gt 0 ]; then
        echo "❌ $unclassified path(s) unclassified — add an explicit rule to customization-surface.yaml"
        return 1
      fi
      # white-label-template-completion E0/T1 flip-precondition assertions (closed-set, not a count literal)
      if ! cs_require_flip_preconditions; then return 1; fi
      echo "✅ every tracked path has an explicit owner + T1 flip preconditions hold"
      ;;
    require-flip-preconditions)
      # E0/T1 (FIX-01-R2-ATOMIC): exit 0 iff every T1 ownership row is present + correct — the atomic
      # self-guard T3's scripts/white-label/sync-dirs.sh flip calls before preserving, so a consumer that pulled the
      # mechanism flip WITHOUT the ownership fix HALTs (no clobber).
      cs_require_flip_preconditions
      ;;
    list-template-owners)
      # WS4/T7 (pure-white-label-100 phase 5): print every glob whose owner is `template`,
      # one per line, in contract order. The single SoT the sync-reachability gate
      # (framework-verify-sync-reachability.sh) consumes so it never hand-rolls YAML parsing.
      cs_load_rules
      local i
      for i in "${!CS_GLOB[@]}"; do
        [ "${CS_OWNER[$i]}" = "template" ] && printf '%s\n' "${CS_GLOB[$i]}"
      done
      ;;
    merge)
      cs_merge "$@"   # <strategy> <ours> <base> <theirs> [<out>]
      ;;
    *)
      echo "usage: customization-surface.sh {resolve <path>|resolve-owner <path>|report|verify|--verify|list-template-owners|merge <strategy> <ours> <base> <theirs> [out]}" >&2
      return 2
      ;;
  esac
}

# Run CLI only when executed directly (not when sourced as a library).
if [ "${BASH_SOURCE[0]}" = "${0}" ]; then
  set -uo pipefail
  cs_main "$@"
fi
