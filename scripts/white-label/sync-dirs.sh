#!/usr/bin/env bash
# bash4-required: EXCLUSIONS is an associative array load-bearing across 23 sites; see the guard below.

# scripts/white-label/sync-dirs.sh
# Script to sync directories and files from upstream repository

# ── bash 4+ REQUIRED — fail loudly rather than silently overwrite a fork's branding ──────────────
#
# This script's EXCLUSIONS map is what PRESERVES fork-owned files across a sync (branded Android
# res/, google-services.json, the iOS asset catalog, deployment config, secrets, …). It is an
# associative array, and bash 3.2 — still /bin/bash on macOS — does not have those.
#
# The old `#!/bin/bash` shebang pinned this script to 3.2 there, and the failure was SILENT and
# DESTRUCTIVE rather than loud: `declare -A` is rejected, but the `=( [k]=v … )` initializer still
# runs as an INDEXED assignment, so every key evaluates arithmetically to 0, all nine entries
# collide, and the last one wins. Measured: `${!EXCLUSIONS[@]}` yields the single key `0`, so 8 of
# 9 modules get NO exclusions and the sync overwrites exactly the files the map exists to protect.
#
# `env bash` picks up a Homebrew bash 5 where present; this guard catches the case where it does not.
if [ -z "${BASH_VERSINFO:-}" ] || [ "${BASH_VERSINFO[0]}" -lt 4 ]; then
    echo "error: sync-dirs.sh requires bash 4+ (found ${BASH_VERSION:-unknown} at $(command -v bash))." >&2
    echo "       macOS ships bash 3.2 as /bin/bash. Install a newer one and re-run, e.g.:" >&2
    echo "         brew install bash && exec \"\$(brew --prefix)/bin/bash\" \"$0\" \"\$@\"" >&2
    echo "       Refusing to continue: without associative arrays the exclusion map silently" >&2
    echo "       collapses and this sync would OVERWRITE fork-owned branded files." >&2
    exit 1
fi

# Repo root (this script lives at the template repo ROOT) — used to source the
# customization-surface contract library (white-label-template-completion E0/T3).
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ── SOURCE the contract library — without this the entire merge engine is dead code ─────────────
# Four sites in this file gate on `declare -F cs_match_g` / `declare -F cs_merge`, including the
# per-directory 3-way merge loop and merge_contract_root_files(). Nothing ever sourced the library,
# so every one of those guards was permanently FALSE and every `owner: merge` path was full-copied
# in silence — the exact clobber the contract's merge class exists to prevent, on every fork, on
# every sync. The guards read as defensive ("no-op on forks that don't ship the reader"); they were
# unconditional.
#
# Proven end-to-end 2026-09-10 by running the real sync on a clone of mbs/cappy: the CappyWidgets
# app-extension target vanished from project.pbxproj (40 refs → 0) and the fork's
# com.apple.security.application-groups entitlement was dropped, while the 12 widget source files
# stayed on disk as unbuildable orphans. No conflict, no warning, exit 0.
#
# A comment at the is_excluded() call site claimed the library must be a SUBPROCESS because it uses
# "bash-4 syntax". It does not: sourcing it under macOS's /bin/bash 3.2.57 defines cs_match_g and
# cs_merge correctly (the only `mapfile` matches are a local VARIABLE of that name, not the bash-4
# builtin). Sourcing is fail-soft — a fork without the reader keeps the old full-copy behaviour.
if [ -f "$SCRIPT_DIR/../customization-surface.sh" ]; then
    # shellcheck source=/dev/null
    . "$SCRIPT_DIR/../customization-surface.sh" >/dev/null 2>&1 || true
fi

# Capture the invocation args verbatim so the self-update re-exec (below) can restart the sync with
# the SAME flags (--dry-run / --only / --force …) on the freshly-materialized engine.
ORIGINAL_ARGS=("$@")

# ── Atomic self-guard (E0/T3, FIX-01-R2-ATOMIC) ──────────────────────────────
# The operative-contract flip preserves fork paths by reading customization-surface.yaml. If a consumer
# pulled this mechanism flip WITHOUT the T1 ownership fix (mid-atomic-window), the contract is wrong and
# preserving off it would clobber. HALT before any sync rather than silently clobber.
if [ -f "$SCRIPT_DIR/../customization-surface.sh" ]; then
    if ! bash "$SCRIPT_DIR/../customization-surface.sh" require-flip-preconditions >/dev/null 2>&1; then
        echo "❌ scripts/white-label/sync-dirs.sh HALT: customization-surface.yaml ownership rows (E0/T1) are not present in this tree." >&2
        echo "   You pulled the fork-preservation flip without the ownership fix. Re-sync to land both atomically." >&2
        exit 1
    fi
fi

# Colors and formatting
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BOLD='\033[1m'
NC='\033[0m'    # No Color
CHECKMARK='\xE2\x9C\x94'
CROSS='\xE2\x9C\x98'

# Default upstream URL
DEFAULT_UPSTREAM_URL="https://github.com/openMF/kmp-project-template.git"

# Script options
DRY_RUN=false
FORCE=false
ONLY_ITEMS=""   # --only <a,b,c>: restrict the sync to this subset of SYNC_DIRS/SYNC_FILES
LOG_FILE="sync-$(date +%d%m%Y-%H%M%S).log"

# Directories and files to sync
SYNC_DIRS=(
    "cmp-android"
    "cmp-desktop"
    "cmp-ios"
    "cmp-web"
    "cmp-shared"
    "cmp-navigation" # shared root-nav module (include(":cmp-navigation")) — was missing pre-2026-07
    "sync"           # shared :sync module (include(":sync")) — was missing pre-2026-07
    "core-base"
    "core"          # framework infra (incl. core/*/infra) syncs; **/demo/** + core/store seam preserved per-fork by sync_directory's convention block (base-branch re-assert)
    "build-logic"
    "deployment"     # 18-target deploy infra — FULL-COPY, zero exclusions (fork state → app-profile/deploy-targets.yaml; deploy history is framework-level deployment-layer/deploy-state/; E1/D-4)
    "fastlane"
    "fastlane-config"
    "spotless"       # shared copyright/format config
    "gradle/wrapper" # Gradle wrapper (properties + jar) — pins the version across forks; NOT gradle/ root (libs.versions.toml is consumer-local + auto-healed)
    "scripts"
    "config"
    "secrets"        # secrets SCAFFOLD only — secrets/sample/** placeholder tree + LAYOUT.yaml sync; real secrets/live/** is gitignored + fork-local + excluded below (never synced)
    "docs"           # blueprint documentation — architecture patterns, claude pattern guides, deploy/ios/secrets/setup references; fork-ADDED docs survive (checkout never deletes fork-only files), template docs refresh
    ".github"
    ".run"
    "feature/home"          # backbone (owner: template) — demo/** + generated/** excluded below (WS4/T5)
    "feature/profile"       # backbone (owner: template) — demo/** excluded below (WS4/T5)
    "feature/settings"      # backbone (owner: template) — demo/** excluded below (WS4/T5)
    "kotlin-js-store"       # JS lockfile tree (owner: template) — full copy (WS4/T5)
    "tools"                 # KSP processors (store/database/network/data) — the codegen engine
                            # the @StoreProvider / @DbEntity / @ApiBinding annotations depend on.
                            # Was unreachable AND unowned: no fork ever got a processor update.
    # app-profile — REQUIRED for the yaml-schema-merge to ever run. app.yaml + platforms/**/*.yaml
    # are `owner: merge / yaml-schema-merge` precisely so the template can deliver freshly-added
    # keys and demo access-points while the fork's identity scalars win. Unreachable, that merge
    # NEVER executed and every fork's schema silently skewed — the exact defect the blanket
    # `app-profile/** -> fork` glob was removed to fix. sync_directory runs cs_merge per-file for
    # owner:merge paths, and fork-owned subpaths (icons, media, app-content, store json, README)
    # carry explicit `fork` rules preserved by is_excluded.
    "app-profile"
    "legal"                 # generator + rendered output (owner: template); legal/*/template.md is
                            # the fork's real legal document and stays fork-owned by contract.
    "tests"                 # contract test harness (E0/T1)
    "META-INF"              # MANIFEST.MF
)

SYNC_FILES=(
    ".bundle/config"        # BUNDLE_PATH for the vendored Ruby toolchain (owner: template)
    "LICENSE"
    "CONTRIBUTING.md"
    "CODE_OF_CONDUCT.md"
    "lib-integrate.properties"
    "Gemfile"
    "Gemfile.lock"
    "ci-prepush.bat"
    "ci-prepush.sh"
    "gradlew"                       # wrapper launcher scripts — pin the Gradle version with gradle/wrapper
    "gradlew.bat"
    "compose_compiler_config.conf"  # compose compiler config referenced by AndroidCompose.kt
    "scripts/white-label/sync-dirs.sh"                  # self-propagate: each sync updates the consumer's own copy for next time
    # --- blueprint infra files (2026-07 audit: full white-label blueprint coverage) ---
    ".editorconfig"                 # shared formatting rules
    ".gitattributes"                # shared line-ending / linguist rules
    ".gitignore"                    # shared ignore baseline (build dirs, sync-*.log, secrets, local.properties)
    ".actrc"                        # act (local GitHub Actions) config — pairs with .github/
    ".ruby-version"                 # Ruby pin for Fastlane — pairs with Gemfile/Gemfile.lock
    ".kover-floor.yml"              # coverage-floor config — pairs with config/ (kover)
    ".claudeignore"                 # Claude tooling ignore baseline
    # --- blueprint setup / customization scripts (root-level, not under scripts/) ---
    "setup-project.sh"              # master fork setup script
    "scripts/white-label/fork-init.sh"                 # fork customization driver
    "scripts/white-label/keystore.sh"           # keystore generate/encode/add operations
    "scripts/white-label/firebase.sh"             # Firebase project configuration
    "generateModuleGraphs.sh"       # module dependency-graph generator
    # --- fork identity SCHEMA (the .template is committed + syncable; the filled-in
    #     gradle/fork.properties is gitignored + fork-local — NEVER synced) ---
    "gradle/fork.properties.template"
    "gradle/gradle-daemon-jvm.properties"  # pins the Gradle daemon JVM toolchain (17) + foojay URLs across forks
    # --- WS4/T6: close the missed-template-update gap for owner:template root paths ---
    "customization-surface.yaml"    # ownership contract — self-syncs (closes the false "self-syncs" claim in customization-surface.yaml)
    "build.gradle.kts"              # root Gradle build (owner: template, WS4/T6)
    "FEATURE_AUTHORING.md"          # in-template feature-authoring guide (owner: template) — was declared "synced" but had no SYNC_FILES vector (WS4/T6)
)

# Define exclusions for directories and files
# Format: "path/to/exclude:type"
# type can be 'dir' or 'file'
# Use "root" key for files in the root directory
declare -A EXCLUSIONS=(
    # Android — consumer-branded resources (drawables, strings, mipmaps), Firebase config,
    # launcher icon, and the dependency-guard baseline directory are preserved across syncs.
    ["cmp-android"]="src/main/res:dir dependencies:dir src/main/ic_launcher-playstore.png:file google-services.json:file"
    # iOS — consumer-branded asset catalog (app icon, color palette) preserved across syncs.
    ["cmp-ios"]="iosApp/Assets.xcassets:dir Configuration/Config.xcconfig:file"
    ["cmp-web"]="src/jsMain/resources:dir src/wasmJsMain/resources:dir"
    # icons:dir stays fork-preserved; build.gradle.kts is REMOVED from the hardcoded exclusion so the
    # customization-surface.yaml declaration (owner: merge / kotlin-3way) governs — template desktop
    # build improvements reach forks via the 3-way merge loop while fork packaging identity survives.
    # (Was clobbering the merge: the restore-excluded block below copied the fork's original back.)
    ["cmp-desktop"]="icons:dir"
    ["fastlane-config"]="project_config.rb:file extract_config.rb:file"
    # Deployment — FULL-COPY, ZERO exclusions (E1 / D-3, epic pure-white-label-store5-network).
    # deployment/** is a pure TEMPLATE-OWNED module now: all fork DATA was relocated OUT —
    #   • per-target enabled/tier state  → app-profile/deploy-targets.yaml   (owner: fork)
    #   • append-only promotion history  → framework deployment-layer/deploy-state/ (E1/D-4; /idea-deploy-owned)
    #   • store listings + screenshots   → DERIVED from app-profile/ by `./gradlew syncForkConfig`
    #                                       (template ships placeholders; regenerated post-sync).
    # DEPLOYMENT_MANIFEST.yaml is a pure catalog of available targets (owner: template). With no
    # fork data left under deployment/, it full-copies exactly like core-base/**. See
    # customization-surface.yaml (deployment/** → template) + the mandatory post-sync
    # syncForkConfig regenerate step declared in /kmp-project-template-sync.
    ["deployment"]=""
    [".github"]="workflows/sync-dirs.yaml:file"
    # Secrets — sync the STRUCTURE (secrets/sample/** placeholder scaffold + LAYOUT.yaml)
    # so forks inherit the canonical layout, but NEVER the real values: secrets/live/ is
    # gitignored + fork-local (preserved-then-restored here as defense-in-depth even though
    # git checkout never touches gitignored paths), and .sync-meta.json is per-fork vault
    # sync state (SV33) — both excluded.
    ["secrets"]="live:dir .sync-meta.json:file"
    # ["root"]="secrets.env:file"  — REMOVED: secrets.env is retired (2026-06-23).
    # Keystore DN now lives in gradle/fork.properties — that file is GITIGNORED and
    # fork-local (each fork's filled-in identity), so it is NEVER synced. Only the
    # committed schema gradle/fork.properties.template syncs (see SYNC_FILES above).
    # Keystore passwords live in
    # secrets/live/android/keystores/ per-value files (gitignored, not synced).
    # DO NOT REMOVE — preserves consumer-specific flavor extensions across syncs.
    # Each downstream consumer app (mifos-mobile, mifos-pay, mifos-x-field-officer-app,
    # mifos-x-group-banking, mifos-x-open-banking, reels-downloader-new, ...) may
    # create build-logic/convention/src/main/kotlin/local/LocalFlavors.kt to add
    # their own flavors / dimensions / overrides on top of the synced base.
    # See docs/architecture/cross-cutting/flavors-extension.md for the pattern.
    ["build-logic"]="convention/src/main/kotlin/local:dir"
    # Feature backbone (WS4/T5) — home/profile/settings are owner:template and now sync,
    # but their demo/** showcases (and feature/home's generated/**) stay fork-owned. These
    # carve-outs preserve the fork's demo customizations when the backbone is widened into
    # the sync surface (RESEARCH.md Area 4 sync-widening risk: widening SYNC_DIRS must
    # preserve the demo/fork is_excluded carve-outs).
    ["feature/home"]="demo:dir generated:dir"
    ["feature/profile"]="demo:dir"
    ["feature/settings"]="demo:dir"
)

# Display help information
show_help() {
    echo -e "${BOLD}Usage:${NC} ./sync-dirs.sh [options]"
    echo
    echo -e "${BOLD}Description:${NC}"
    echo "  This script syncs directories and files from an upstream repository."
    echo "  It preserves excluded files and directories as defined in the script."
    echo
    echo -e "${BOLD}Options:${NC}"
    echo "  -h, --help      Display this help message and exit"
    echo "  --list          Print the canonical sync surface (SYNC_DIRS + SYNC_FILES) and exit"
    echo "  --dry-run       Show what would be done without making changes"
    echo "  -f, --force     Skip confirmation prompts and proceed automatically"
    echo "  --only <list>   Restrict the sync to a comma/space-separated subset of the declared"
    echo "                  SYNC_DIRS/SYNC_FILES (e.g. --only build-logic,gradle/wrapper,spotless)."
    echo "                  Use it when a diverged fork only wants specific infra refreshed."
    echo
    echo -e "${BOLD}Examples:${NC}"
    echo "  ./sync-dirs.sh              # Run with interactive prompts"
    echo "  ./sync-dirs.sh --dry-run    # Test run without changes"
    echo "  ./sync-dirs.sh --force      # Run with no prompts"
    echo "  ./sync-dirs.sh --only build-logic,gradle/wrapper --dry-run   # only that infra subset"
}

# Logging function
log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
    echo -e "$1"
}

# Error handling function
handle_error() {
    log_message "${RED}${CROSS} Error: $1${NC}"
    exit 1
}

# Print error message
print_error() {
    log_message "${RED}${CROSS} Error: $1${NC}"
}

# Simple progress indicator function
show_progress() {
    if [ "$DRY_RUN" = false ]; then
        echo -ne "${BLUE}[                    ]${NC}\r"
        echo -ne "${BLUE}[=====               ]${NC}\r"
        sleep 0.1
        echo -ne "${BLUE}[==========          ]${NC}\r"
        sleep 0.1
        echo -ne "${BLUE}[===============     ]${NC}\r"
        sleep 0.1
        echo -ne "${BLUE}[====================]${NC}"
        echo
    fi
}

# Fancy banner
print_banner() {
    echo -e "${BLUE}╔════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║${BOLD}        Project Directory Sync Tool         ${NC}${BLUE}║${NC}"
    echo -e "${BLUE}╚════════════════════════════════════════════╝${NC}"
    echo
}

# Print step with color and symbol
print_step() {
    log_message "${GREEN}${CHECKMARK} $1${NC}"
}

# Print warning with color
print_warning() {
    log_message "${YELLOW}⚠ $1${NC}"
}

# Function to generate unique branch name
get_sync_branch_name() {
    local date_suffix=$(date +%Y%m%d-%H%M%S)
    echo "sync/upstream-${date_suffix}"
}

# Print directories and files to be synced
print_items() {
    echo -e "${BLUE}Items to sync:${NC}"
    echo -e "${BOLD}Directories:${NC}"
    # Loop variables are declared local even in a pure-print helper — same dynamic-scope hazard that
    # made is_excluded() rewrite its caller's `dir`. This one is called at top level today, which is
    # exactly how it stays harmless until someone calls it from inside a function.
    local dir file
    for dir in "${SYNC_DIRS[@]}"; do
        echo -e "  ${BOLD}→${NC} $dir"
    done

    echo -e "\n${BOLD}Files:${NC}"
    for file in "${SYNC_FILES[@]}"; do
        echo -e "  ${BOLD}→${NC} $file"
    done
    echo
}

# Function to check if a path is excluded
is_excluded() {
    local check_dir=$1
    local full_path=$2
    local check_type=$3  # 'file' or 'dir'

    # Remove ./ from the beginning of the path if it exists
    full_path="${full_path#./}"

    # ── Convention exclusions (showcase-framework-separation) ───────────────
    # core/ is synced framework-only. Never overwrite a fork's removed demo
    # (**/demo/**) or the per-fork core/store seam files (D8). These are matched
    # by convention (pattern), not the exact-path EXCLUSIONS map above.
    case "$full_path" in
        */demo/*)                       return 0 ;;  # demo showcase packages
        */demo)                         return 0 ;;  # a demo/ dir itself
        core/store/*AppScreenStateDefaults.kt) return 0 ;;  # fork seam (D8)
        core/store/*AppErrorMapper.kt)         return 0 ;;
        core/store/*ProjectErrorMapper.kt)      return 0 ;;
        core/store/*ProjectScreenStateDefaults.kt) return 0 ;;
        core/store/*StoreModule.kt)            return 0 ;;
        */schemas/*)                    return 0 ;;  # Room schema-export snapshots (per-fork
            # migration/version history — every fork's AppDatabase.VERSION + entity set diverges
            # from the template's own demo schema from v1, so these can never be template-owned or
            # merged; blind-preserved same as media/icons/store-content. Without this, a whole-dir
            # sync of `core` overwrites the fork's REAL schemas/N.json with the template's demo
            # schema (extra template-only versions incl. a phantom `samples` table), corrupting
            # Room's AutoMigration diff and breaking the next `kspAndroidMain` with an
            # "AutoMigration Failure: declare @RenameTable/@DeleteTable" error.
    esac

    # ── Contract-driven preservation (white-label-template-completion E0/T3, LD-2 operative flip) ──
    # customization-surface.yaml is now the OPERATIVE SoT: if the contract says this path is fork-owned,
    # preserve it — even without a hardcoded EXCLUSIONS entry. Closes the og-images/gradle.properties/
    # secrets-manifest clobber class. ADDITIVE — the hardcoded exclusions below still apply as a superset.
    # Invoked as a SUBPROCESS (not sourced) so customization-surface.sh's bash-4 syntax runs under its own
    # `bash` regardless of scripts/white-label/sync-dirs.sh's /bin/bash version (macOS 3.2 portability). Result memoized.
    if [ -x "$SCRIPT_DIR/../customization-surface.sh" ] || [ -f "$SCRIPT_DIR/../customization-surface.sh" ]; then
        local _cs_owner
        _cs_owner="$(bash "$SCRIPT_DIR/../customization-surface.sh" resolve "$full_path" 2>/dev/null | awk 'NR==1{print $2}')"
        [ "$_cs_owner" = "fork" ] && return 0   # contract says fork-owned → preserve
    fi

    # Check for root-level exclusions
    if [ -n "${EXCLUSIONS["root"]}" ] && [[ "$check_type" == "file" ]]; then
        local IFS=' '
        read -ra ROOT_EXCLUDE_ITEMS <<< "${EXCLUSIONS["root"]}"

        for item in "${ROOT_EXCLUDE_ITEMS[@]}"; do
            local IFS=':'
            read -ra PARTS <<< "$item"
            local exclude_path="${PARTS[0]}"
            local exclude_type="${PARTS[1]}"

            if [ "$exclude_type" = "$check_type" ] && [ "$full_path" = "$exclude_path" ]; then
                return 0  # Path is excluded
            fi
        done
    fi

    # Check directory-specific exclusions
    # `_ex_dir`, NOT `dir` — bash has DYNAMIC scope, so an undeclared loop variable here assigns to
    # the CALLER's `local dir`. sync_directory() calls is_excluded() from its convention-exclusion
    # loop, so this `for dir in …` silently rewrote sync_directory's own `dir` to the LAST key of
    # EXCLUSIONS, and every step after that point ran against the wrong directory:
    #   propagate_template_deletions "$dir"   → wrong dir
    #   the 3-way merge loop's `-- "$dir"`     → wrong dir
    #   the "Restore excluded files" block     → wrong EXCLUSIONS entry
    # Proven 2026-09-10: a real `--only cmp-ios` run printed "Syncing cmp-ios", then the merge loop
    # reported `dir=[feature/settings]` and merged 63 feature/settings strings files while cmp-ios
    # got ZERO merge passes — its project.pbxproj was full-copied (-404/+35), deleting the fork's
    # CappyWidgets target. The loop variable is now function-private and can never reach a caller.
    local _ex_dir
    for _ex_dir in "${!EXCLUSIONS[@]}"; do
        # Skip the root key as we've already checked it
        if [ "$_ex_dir" = "root" ]; then
            continue
        fi

        # Check if the path starts with the directory we're looking at
        if [[ "$full_path" == "$_ex_dir"* ]]; then
            local IFS=' '
            read -ra EXCLUDE_ITEMS <<< "${EXCLUSIONS[$_ex_dir]}"

            for item in "${EXCLUDE_ITEMS[@]}"; do
                local IFS=':'
                read -ra PARTS <<< "$item"
                local exclude_path="$_ex_dir/${PARTS[0]}"
                local exclude_type="${PARTS[1]}"

                # Remove any duplicate slashes
                exclude_path=$(echo "$exclude_path" | sed 's#/\+#/#g')
                full_path=$(echo "$full_path" | sed 's#/\+#/#g')

                if [ "$exclude_type" = "$check_type" ] && [ "$full_path" = "$exclude_path" ]; then
                    return 0  # Path is excluded
                fi
            done
        fi
    done
    return 1  # Path is not excluded
}

cleanup_temp_dirs() {
    print_step "Cleaning up temporary directories..."
    find . -type d -name "temp_*" -exec rm -rf {} +
    show_progress
}

# Function to preserve excluded paths
preserve_excluded_paths() {
    local dir=$1
    local destination=$2

    if [ -n "${EXCLUSIONS[$dir]}" ]; then
        local IFS=' '
        read -ra EXCLUDE_ITEMS <<< "${EXCLUSIONS[$dir]}"

        for item in "${EXCLUDE_ITEMS[@]}"; do
            local IFS=':'
            read -ra PARTS <<< "$item"
            local exclude_path="${PARTS[0]}"
            local exclude_type="${PARTS[1]}"
            local full_source_path="$dir/$exclude_path"
            local full_dest_path="$destination/$exclude_path"

            if [ -e "$full_source_path" ]; then
                print_step "Preserving excluded ${exclude_type}: ${BOLD}$exclude_path${NC}"
                mkdir -p "$(dirname "$full_dest_path")"
                cp -r "$full_source_path" "$(dirname "$full_dest_path")"
            fi
        done
    fi
}

# ── Contract-keyed merge strategies (pure-white-label-store5-network E0/T4) ──────────
# Two root config files carry template updates a fork MUST receive but that a blind
# copy would clobber. The customization-surface contract declares their strategy; these
# implement it for real (no stub) so template module-includes AND version bumps reach forks:
#
#   settings.gradle.kts        → include-union   (union the include(":module") declarations)
#   gradle/libs.versions.toml  → catalog-3way    (genuine BASE/OURS/THEIRS per-key merge)
#
# Both dispatch off the declared `strategy:` in customization-surface.yaml, so contract
# and engine agree. They REPLACE the old skip-of-settings.gradle.kts + the one-way
# heal_libs_versions_toml (kept below only as a fallback for forks without the reader).

# include-union: union the fork's and the template's `include(...)` module declarations.
# Start from the TEMPLATE file (it carries pluginManagement / dependencyResolutionManagement
# / structural updates), then append every include(...) line the fork has that the template
# lacks — so template-added modules reach the fork AND fork-local :feature:* includes survive.
#   merge_settings_include_union <ours> <base> <theirs> [<out>]
merge_settings_include_union() {
    local ours="$1" theirs="$3" out="${4:-$1}"
    [ -f "$ours" ]   || { [ -f "$theirs" ] && cp "$theirs" "$out"; return 0; }
    [ -f "$theirs" ] || { cp "$ours" "$out"; return 0; }
    local tmp fork_only last
    tmp="$(mktemp)"
    cp "$theirs" "$tmp"
    fork_only="$(comm -23 \
        <(grep -E '^[[:space:]]*include\(' "$ours"   | sed 's/[[:space:]]*$//' | sort -u) \
        <(grep -E '^[[:space:]]*include\(' "$theirs" | sed 's/[[:space:]]*$//' | sort -u))"
    if [ -n "$fork_only" ]; then
        last="$(grep -nE '^[[:space:]]*include\(' "$tmp" | tail -1 | cut -d: -f1)"
        if [ -n "$last" ]; then
            { head -n "$last" "$tmp"; printf '%s\n' "$fork_only"; tail -n +"$((last + 1))" "$tmp"; } > "${tmp}.2"
            mv "${tmp}.2" "$tmp"
        else
            printf '%s\n' "$fork_only" >> "$tmp"
        fi
        print_step "include-union: preserved $(printf '%s\n' "$fork_only" | grep -c . ) fork-local module include(s) in ${BOLD}$(basename "$out")${NC}"
    fi
    # Prune PHANTOM includes: the union starts from the TEMPLATE file, which declares the template's OWN
    # demo/app feature modules (feature/showcase, feature/loans, feature/rates, …) that a downstream fork
    # does NOT have and the sync does NOT copy in. Left in, they reference non-existent module dirs and
    # Gradle configuration hard-fails ("Configuring project ':feature:showcase' without an existing
    # directory"), blocking even syncForkConfig. Drop every include(":a:b") whose resolved dir a/b/ is
    # absent on disk (relative to the settings.gradle.kts dir) — keep the fork's real + synced modules.
    local _iu_root _iu_out _iu_dropped=0 _iu_coord
    _iu_root="$(cd "$(dirname "$out")" 2>/dev/null && pwd)"; [ -n "$_iu_root" ] || _iu_root="$(pwd)"
    _iu_out="$(mktemp)"
    while IFS= read -r _iu_line || [ -n "$_iu_line" ]; do
        _iu_coord="$(printf '%s' "$_iu_line" | sed -nE 's/^[[:space:]]*include\("?:([A-Za-z0-9:_.-]+)"?\).*/\1/p')"
        if [ -n "$_iu_coord" ] && [ ! -d "$_iu_root/${_iu_coord//://}" ]; then _iu_dropped=$((_iu_dropped + 1)); continue; fi
        printf '%s\n' "$_iu_line"
    done < "$tmp" > "$_iu_out"
    mv "$_iu_out" "$tmp"
    [ "$_iu_dropped" -gt 0 ] && print_step "include-union: pruned ${BOLD}$_iu_dropped${NC} phantom include(s) (module dir absent in this fork) from ${BOLD}$(basename "$out")${NC}"
    mv "$tmp" "$out"
    return 0
}

# catalog-3way: a genuine per-key 3-way merge of a Gradle version catalog across its
# [versions] / [libraries] / [plugins] / [bundles] sections.
#   base = last-synced template catalog · ours = fork · theirs = new template
# Per key (standard 3-way semantics, applied key-wise not line-wise so unrelated additions
# never spurious-conflict):
#   • key only in theirs, not in base  → template-ADDED  → add
#   • key only in ours                 → fork-added/pin  → keep
#   • both, ours == base (fork untouched), theirs != base → template BUMPED → take theirs (major surfaced)
#   • both, theirs == base (template untouched)          → fork PINNED     → keep ours
#   • both diverged (ours != base, theirs != base, ≠)    → CONFLICT        → keep ours + surface
# The fork's file formatting/comments are preserved (we walk OURS and only swap bumped values
# + append template-added keys). Returns 0 clean · 1 if a real conflict was surfaced.
# ── project.pbxproj — a STRUCTURAL 3-way, delegated to merge-pbxproj.rb ──────
# A line merge is not merely suboptimal on a pbxproj, it is unusable: the file is a serialized
# object graph, so "add a target" edits UUID arrays three levels from the object it adds, and
# git merge-file calls adjacent array insertions a conflict. Worse, conflict markers make the
# project UNOPENABLE — a human cannot even use Xcode to resolve what the merge produced. Measured
# on mbs/cappy: git merge-file emitted 6 conflict markers where the structural merge has 0.
#
# FALLBACK IS "KEEP OURS", NEVER "TAKE THEIRS". Without ruby the merge cannot run, and the two
# failure modes are not symmetric: taking the template's copy silently deletes the fork's targets
# (unrecoverable without git archaeology), while keeping the fork's copy merely means this sync
# skipped the Xcode project — visible, warned, and re-tried on the next run.
merge_pbxproj_3way() {
    local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
    [ -f "$ours" ] && [ -f "$theirs" ] || { [ -f "$theirs" ] && cp "$theirs" "$out"; return 0; }
    [ -f "$base" ] || cp "$ours" "$base"

    local repo_root; repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
    local merger="$repo_root/scripts/white-label/merge-pbxproj.rb"
    if [ ! -f "$merger" ]; then
        print_warning "merge-pbxproj.rb absent — KEPT the fork's $out (this sync skipped the Xcode project)"
        cp "$ours" "$out"; return 3
    fi

    # shellcheck source=/dev/null
    . "$repo_root/scripts/ruby-exec.sh" 2>/dev/null || true
    if ! declare -F ruby_exec >/dev/null 2>&1; then
        print_warning "ruby-exec.sh unavailable — KEPT the fork's $out (this sync skipped the Xcode project)"
        cp "$ours" "$out"; return 3
    fi

    local tmp_out; tmp_out="$(mktemp -d)/project.pbxproj"
    mkdir -p "$(dirname "$tmp_out")"

    # PLAIN ruby first, bundler only as a fallback. `xcodeproj` ships as a fastlane transitive, so on
    # a machine that has ever set up iOS deployment it is already installed for the pinned ruby and
    # loads with a bare `require` — measured 1.27.0 on both the template and a fresh fork clone.
    # Going through bundler FIRST breaks that: `bundle exec` demands a fully-installed Gemfile in the
    # FORK, and a fork that has never run `bundle install` gets bundler resolving against whatever
    # ruby it lands on. Observed on a real run — the merge died in bundler under macOS's system ruby
    # 2.6 with `uninitialized constant Gem::Resolver::APISet::GemParser`, nothing to do with the
    # merge, and the sync fell back to keep-ours for a file it could have merged cleanly.
    # merge-pbxproj.rb exits 2 (and only 2) when the gem is genuinely unavailable, so that is the
    # single signal worth retrying under bundler.
    ruby_exec "$merger" --ours "$ours" --base "$base" --theirs "$theirs" --out "$tmp_out" --report
    local _rc=$?
    if [ "$_rc" -eq 2 ] && declare -F ruby_bundle >/dev/null 2>&1; then
        ruby_bundle "$repo_root" -- exec ruby "$merger" \
            --ours "$ours" --base "$base" --theirs "$theirs" --out "$tmp_out" --report
        _rc=$?
    fi
    if [ "$_rc" -eq 0 ]; then
        cp "$tmp_out" "$out"; rm -rf "$(dirname "$tmp_out")"; return 0
    fi
    rm -rf "$(dirname "$tmp_out")"
    # Exit 1 = a difference the merger refuses to decide; exit 2 = the gem/toolchain is missing.
    print_warning "pbxproj merge did not complete — KEPT the fork's $out. Review the reason above;"
    print_warning "  a per-fork identity setting belongs in FORK_IDENTITY_KEYS in merge-pbxproj.rb."
    cp "$ours" "$out"
    return 1
}

# ── Info.plist / *.entitlements — key-level union, delegated to merge-plist.rb ───────────────
# Template and fork each add their OWN keys to a flat top-level <dict>, so the additions land at the
# same point in the text and a line merge calls non-overlapping changes a conflict. Both files are
# also ADD/ADD against a fork's older base (neither existed at mbs/cappy's b97eb73d), so there is no
# ancestor and EVERY line conflicts. A `<<<<<<<` in an XML plist does not parse — the build breaks.
# Same keep-ours fallback rationale as the pbxproj merger.
merge_plist_union() {
    local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
    [ -f "$ours" ] && [ -f "$theirs" ] || { [ -f "$theirs" ] && cp "$theirs" "$out"; return 0; }

    local repo_root; repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
    local merger="$repo_root/scripts/white-label/merge-plist.rb"
    if [ ! -f "$merger" ]; then
        print_warning "merge-plist.rb absent — KEPT the fork's $out (this sync skipped it)"
        cp "$ours" "$out"; return 3
    fi
    # shellcheck source=/dev/null
    . "$repo_root/scripts/ruby-exec.sh" 2>/dev/null || true
    if ! declare -F ruby_exec >/dev/null 2>&1; then
        print_warning "ruby-exec.sh unavailable — KEPT the fork's $out (this sync skipped it)"
        cp "$ours" "$out"; return 3
    fi
    # `-` tells the merger there is no ancestor for THIS FILE, which is different from the repo
    # having no merge base: a file added on both sides since the last sync has a valid repo base
    # and still no file base.
    local base_arg="$base"; [ -s "$base" ] || base_arg="-"
    local tmp; tmp="$(mktemp)"
    if ruby_exec "$merger" --ours "$ours" --base "$base_arg" --theirs "$theirs" --out "$tmp" --report; then
        cp "$tmp" "$out"; rm -f "$tmp"; return 0
    fi
    rm -f "$tmp"
    print_warning "plist union did not complete — KEPT the fork's $out. Review the reason above."
    cp "$ours" "$out"
    return 1
}

merge_libs_catalog_3way() {
    local ours="$1" base="$2" theirs="$3" out="${4:-$1}"
    [ -f "$ours" ] && [ -f "$theirs" ] || { [ -f "$theirs" ] && cp "$theirs" "$out"; return 0; }
    [ -f "$base" ] || cp "$ours" "$base"
    local merged notes rc=0
    merged="$(mktemp)"; notes="$(mktemp)"
    awk -v basef="$base" -v theirsf="$theirs" '
      function trim(s){ gsub(/^[ \t]+|[ \t]+$/,"",s); return s }
      function keyof(line,   p){ if(line ~ /^[ \t]*[A-Za-z0-9_.\-]+[ \t]*=/){ p=index(line,"="); return trim(substr(line,1,p-1)) } return "" }
      function valof(line,   p){ p=index(line,"="); return trim(substr(line,p+1)) }
      function major(v,   x){ x=v; sub(/^[^0-9]*/,"",x); sub(/[^0-9].*$/,"",x); return x }
      function flushadd(sec,   m,arr,j,kk,key){
        if(sec=="") return
        m=split(tkeys[sec],arr,SUBSEP)
        for(j=1;j<=m;j++){ kk=arr[j]; if(kk=="") continue; key=sec SUBSEP kk
          if(!(key in oursseen) && !(key in baseseen)) print theirsline[key] }
      }
      BEGIN{
        while((getline l < basef) > 0){
          if(l ~ /^[ \t]*\[/){ bsec=trim(l); continue }
          k=keyof(l); if(k!=""){ base[bsec SUBSEP k]=valof(l); baseseen[bsec SUBSEP k]=1 }
        }
        close(basef)
        while((getline l < theirsf) > 0){
          if(l ~ /^[ \t]*\[/){ tsec=trim(l); if(!(tsec in tsectseen)){ tsectseen[tsec]=1; tsorder[++tnsec]=tsec } continue }
          k=keyof(l); if(k!=""){ key=tsec SUBSEP k; theirs[key]=valof(l); theirsline[key]=l; theirsseen[key]=1; tkeys[tsec]=tkeys[tsec] SUBSEP k }
        }
        close(theirsf)
      }
      {
        line=$0
        if(line ~ /^[ \t]*\[/){ flushadd(cursec); cursec=trim(line); ourssect[cursec]=1; print line; next }
        k=keyof(line)
        if(k==""){ print line; next }
        key=cursec SUBSEP k; oursseen[key]=1; o=valof(line)
        if(key in theirsseen){
          t=theirs[key]; hasb=(key in baseseen); b=(hasb?base[key]:"")
          if(o==t){ print line }
          else if(hasb && o==b){ print theirsline[key]; if(major(o)!=major(t)) print "MAJOR\t" k "\t" o " -> " t > "/dev/stderr" }
          else if(hasb && t==b){ print line }
          else { print line; print "CONFLICT\t" k "\t" o " | " t > "/dev/stderr" }
        } else { print line }
        next
      }
      END{
        flushadd(cursec)
        for(i=1;i<=tnsec;i++){ s=tsorder[i]; if(!(s in ourssect)){ print s
          m=split(tkeys[s],arr,SUBSEP); for(j=1;j<=m;j++){ kk=arr[j]; if(kk=="") continue; key=s SUBSEP kk; if(!(key in baseseen)) print theirsline[key] } } }
      }
    ' "$ours" > "$merged" 2>"$notes"
    mv "$merged" "$out"
    grep -q '^CONFLICT' "$notes" 2>/dev/null && rc=1
    while IFS="$(printf '\t')" read -r kind key detail; do
        [ "$kind" = "MAJOR" ]    && print_warning "catalog-3way: MAJOR version bump on ${BOLD}$key${NC} ($detail) — review before release"
        [ "$kind" = "CONFLICT" ] && print_warning "catalog-3way: CONFLICT on ${BOLD}$key${NC} (fork kept: $detail) — reconcile manually"
    done < "$notes"
    rm -f "$notes"
    return $rc
}

# Drive the two contract-keyed root-file merges (settings.gradle.kts + libs.versions.toml)
# after the directory sync, replacing the old one-way heal_libs_versions_toml. Resolves each
# file's strategy from the contract; ours=BASE_BRANCH, theirs=TEMP_BRANCH, base=merge-base.
# Falls back to heal_libs_versions_toml on a fork that ships no contract reader.
merge_contract_root_files() {
    if ! declare -F cs_match_g >/dev/null 2>&1; then
        if [ -f "$SCRIPT_DIR/../customization-surface.sh" ]; then
            # shellcheck source=/dev/null
            source "$SCRIPT_DIR/../customization-surface.sh" 2>/dev/null || true
        fi
    fi
    if ! declare -F cs_match_g >/dev/null 2>&1; then
        print_warning "No customization-surface reader — falling back to one-way libs heal."
        heal_libs_versions_toml
        return
    fi

    echo -e "\n${BLUE}${BOLD}Contract-keyed root merges (settings include-union + catalog-3way)...${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

    # Anchor via resolve_merge_base, NOT `git merge-base` alone — a copied (non-forked) fork has no
    # shared history and the bare call returns empty, which silently degrades every merge below into
    # a full-copy. See resolve_merge_base().
    local mbase; mbase="$(resolve_merge_base "$BASE_BRANCH" "$TEMP_BRANCH")" || mbase=""
    local f strat o b t rc
    # gradle.properties joins these rather than SYNC_FILES: it is `owner: merge` and carries the
      # fork's own `fork.project.name`, so a full checkout would clobber it. A 3-way takes the
      # template's added build-tuning keys while the fork's values win.
      for f in "settings.gradle.kts" "gradle/libs.versions.toml" "gradle.properties"; do
        cs_match_g "$f"
        [ "${CS_M_OWNER:-}" = "merge" ] || continue
        git show "$TEMP_BRANCH:$f" >/dev/null 2>&1 || { print_warning "Template has no $f — skipping."; continue; }
        strat="${CS_M_STRAT:-3way}"
        o="$(mktemp)"; b="$(mktemp)"; t="$(mktemp)"
        git show "$BASE_BRANCH:$f" > "$o" 2>/dev/null || cp "$f" "$o" 2>/dev/null || : > "$o"
        git show "$TEMP_BRANCH:$f" > "$t" 2>/dev/null
        git show "${mbase}:$f"     > "$b" 2>/dev/null || cp "$o" "$b"
        mkdir -p "$(dirname "$f")"
        case "$strat" in
            include-union) merge_settings_include_union "$o" "$b" "$t" "$f"; rc=$? ;;
            catalog-3way)  merge_libs_catalog_3way      "$o" "$b" "$t" "$f"; rc=$? ;;
            pbxproj-3way)  merge_pbxproj_3way          "$o" "$b" "$t" "$f"; rc=$? ;;
            plist-union)   merge_plist_union           "$o" "$b" "$t" "$f"; rc=$? ;;
            *)             cs_merge "$strat" "$o" "$b" "$t" "$f"; rc=$? ;;
        esac
        case "${rc:-0}" in
            0) print_step "Merged (${strat}) ${BOLD}$f${NC} — template update + fork edits reconciled" ;;
            3) print_warning "SKIPPED (${strat}) ${BOLD}$f${NC} — kept the fork's copy; template changes NOT applied" ;;
            *) print_warning "Merged (${strat}) ${BOLD}$f${NC} WITH conflicts — resolve before committing the sync" ;;
        esac
        rm -f "$o" "$b" "$t"
    done
}

# ── The 3-way merge BASE, for true forks AND copied ones ─────────────────────
# `owner: merge` is only worth declaring if the 3-way actually has a common ancestor to diff
# against. Two fork topologies exist and only one has it:
#
#   forked   — `gh repo fork` / `git clone` of the template. Shares history, so
#              `git merge-base $BASE_BRANCH $TEMP_BRANCH` returns a real commit.
#   copied   — the template TREE copied into a fresh repo (the common case in practice: measured
#              on mbs/cappy 2026-09-10 — different root commits, ZERO shared commits).
#              `git merge-base` returns EMPTY.
#
# An empty base is not a harmless degradation. `git merge-file ours <base> theirs` with base==ours
# sees every template difference as a clean addition onto an untouched base and takes ALL of theirs
# — so the merge silently produces exactly the full-copy that declaring `merge` was meant to prevent,
# with a reassuring "Merged (…) — fork edits preserved" line printed over the top of the loss.
#
# `.template-version#template_sha` is the anchor for the copied case, and its own header already
# says so ("anchors the next sync's 3-way merge base") — the merge loops simply were not reading it.
# It records the template commit this tree last synced FROM, which is precisely the common ancestor
# git cannot compute across unrelated histories.
#
# Order: real merge-base (most accurate) → PREV_TEMPLATE_SHA (correct for copied forks) → empty
# (first-ever sync of a copied fork with no anchor — the caller then falls back to ours, and the
# loop WARNS rather than claiming a merge it did not perform).
resolve_merge_base() {
    local a="$1" b="$2" mb
    mb="$(git merge-base "$a" "$b" 2>/dev/null)"
    if [ -n "$mb" ]; then printf '%s' "$mb"; return 0; fi
    if [ -n "${PREV_TEMPLATE_SHA:-}" ] && git cat-file -e "$PREV_TEMPLATE_SHA" 2>/dev/null; then
        printf '%s' "$PREV_TEMPLATE_SHA"; return 0
    fi
    return 1
}

# Function to sync directory with exclusions
# Propagate the template's OWN deletions into a fork that inherited the now-removed file from
# a previous sync (the `core/platform/notification/bill/**` class: the template consolidated its
# bill-reminder scheduler into worker-kmp and deleted the old per-platform WorkManager scheduler,
# but `git checkout $temp_branch -- $dir` only ever OVERWRITES files present in $temp_branch — it
# never deletes files the fork's tree has that $temp_branch doesn't, so the dead files sat in every
# fork's tree indefinitely, silently breaking the next dependency-surface change that touched them).
#
# Conservative by design — this NEVER deletes anything the fork customized:
#   1. Find files under $dir the template HAD at PREV_TEMPLATE_SHA but no longer has at $temp_branch
#      (a real template-side deletion since the fork's last sync).
#   2. Skip anything is_excluded() already treats as fork-owned/convention (demo/**, schemas/**, the
#      core/store seam, customization-surface.yaml owner:fork paths) — deletion-propagation only
#      ever applies to owner:template surfaces.
#   3. Skip anything the fork already doesn't have (nothing to do) or already customized — i.e. the
#      fork's current content differs from what PREV_TEMPLATE_SHA shipped. A diverged file is NEVER
#      auto-deleted; it's surfaced as a warning for a human to review.
#   4. Only auto-delete when the fork's copy is BYTE-IDENTICAL to the old template content — proof
#      the fork never touched it, so dropping it just mirrors the template's own completed migration.
propagate_template_deletions() {
    local dir=$1
    local temp_branch=$2

    [ -n "$PREV_TEMPLATE_SHA" ] || return 0

    local deleted_f
    while IFS= read -r deleted_f; do
        [ -z "$deleted_f" ] && continue
        [ -e "$deleted_f" ] || continue                                   # fork doesn't have it either
        is_excluded "$dir" "$deleted_f" "file" && continue                # fork-owned/convention path

        if git diff --quiet "$PREV_TEMPLATE_SHA" -- "$deleted_f" 2>/dev/null; then
            git rm -rf --quiet "$deleted_f" 2>/dev/null || rm -f "$deleted_f"
            print_step "Dropped template-removed ${BOLD}$deleted_f${NC} (unmodified since last sync)"
        else
            print_warning "Template removed ${BOLD}$deleted_f${NC} but your fork customized it — review manually (kept)."
        fi
    done < <(git diff --name-only --diff-filter=D "$PREV_TEMPLATE_SHA" "$temp_branch" -- "$dir" 2>/dev/null)
}

sync_directory() {
    local dir=$1
    local temp_branch=$2

    if [ -d "$dir" ]; then
        print_step "Syncing ${BOLD}$dir${NC}..."

        if [ "$DRY_RUN" = false ]; then
            # Create temporary directory for original content
            mkdir -p "temp_$dir"

            # Store original directory for excluded items
            if [ -d "$dir" ]; then
                # First handle directory exclusions
                if [ -n "${EXCLUSIONS[$dir]}" ]; then
                    local IFS=' '
                    read -ra EXCLUDE_ITEMS <<< "${EXCLUSIONS[$dir]}"

                    for item in "${EXCLUDE_ITEMS[@]}"; do
                        local IFS=':'
                        read -ra PARTS <<< "$item"
                        local exclude_path="$dir/${PARTS[0]}"
                        local exclude_type="${PARTS[1]}"

                        if [ "$exclude_type" = "dir" ] && [ -e "$exclude_path" ]; then
                            print_step "Preserving excluded directory: ${BOLD}${PARTS[0]}${NC}"
                            mkdir -p "$(dirname "temp_$exclude_path")"
                            cp -r "$exclude_path" "$(dirname "temp_$exclude_path")"
                        elif [ "$exclude_type" = "file" ] && [ -f "$exclude_path" ]; then
                            print_step "Preserving excluded file: ${BOLD}${PARTS[0]}${NC}"
                            mkdir -p "$(dirname "temp_$exclude_path")"
                            cp "$exclude_path" "temp_$exclude_path"
                        fi
                    done
                fi
            fi

            # Checkout from upstream
            git checkout "$temp_branch" -- "$dir" || {
                print_error "Failed to sync $dir"
                rm -rf "temp_$dir"
                return 1
            }

            # ── Convention exclusions on a DIRECTORY sync (the is_excluded rules:
            #    **/demo/** + the core/store seam). Without this, a whole-dir sync of
            #    `core`/`core-base` clobbers the fork's branded seam files and re-adds
            #    demo packages the fork removed via `customizer --clean`. Re-assert the
            #    fork's own state from the base branch (BASE_BRANCH) for every synced
            #    path the convention flags: restore the fork's version if it had one,
            #    else drop the upstream-added file the fork never had.
            while IFS= read -r conv_f; do
                [ -z "$conv_f" ] && continue
                if is_excluded "$dir" "$conv_f" "file"; then
                    if git cat-file -e "${BASE_BRANCH}:${conv_f}" 2>/dev/null; then
                        git checkout "$BASE_BRANCH" -- "$conv_f" 2>/dev/null \
                            && print_step "Preserved fork's ${BOLD}$conv_f${NC} (convention)"
                    else
                        git rm -f --quiet "$conv_f" 2>/dev/null || rm -f "$conv_f"
                        print_step "Dropped upstream-added ${BOLD}$conv_f${NC} (fork removed it)"
                    fi
                fi
            done < <(git diff --name-only "$BASE_BRANCH" -- "$dir" 2>/dev/null)

            # ── Deletion propagation (template-side removals the fork inherited) ──
            propagate_template_deletions "$dir" "$temp_branch"

            # ── 3-way merge for merge-owned files (customization-surface contract) ──
            # Replaces a blind upstream clobber with a real merge so fork edits (e.g.
            # AndroidManifest permissions) survive a template update. ours=fork
            # (BASE_BRANCH), theirs=upstream (temp_branch), base=merge-base. Guarded:
            # no-op on forks that don't ship the reader.
            if declare -F cs_match_g >/dev/null 2>&1 && declare -F cs_merge >/dev/null 2>&1; then
                local _mbase; _mbase="$(resolve_merge_base "$BASE_BRANCH" "$temp_branch")" || _mbase=""
                while IFS= read -r _mf; do
                    [ -z "$_mf" ] && continue
                    cs_match_g "$_mf"; [ "${CS_M_OWNER:-}" = "merge" ] || continue
                    # A line-based 3-way on a binary produces corruption that still LOOKS like a
                    # successful merge. The blanket `merge` rows cover whole platform shells, so a
                    # binary reaching here is expected (icons, .webp, .ico) rather than exceptional —
                    # most are carved out as `fork`, and this catches whatever is not.
                    if [ -f "$_mf" ] && ! LC_ALL=C grep -qI . "$_mf" 2>/dev/null \
                       && [ -s "$_mf" ]; then
                        print_warning "Binary ${BOLD}$_mf${NC} is merge-owned — took the template's copy (no line merge possible)"
                        continue
                    fi
                    local _o _b _t; _o="$(mktemp)"; _b="$(mktemp)"; _t="$(mktemp)"
                    if git show "$BASE_BRANCH:$_mf" > "$_o" 2>/dev/null \
                       && git show "$temp_branch:$_mf" > "$_t" 2>/dev/null; then
                        # ── The per-FILE ancestor, which is not the same as the repo having one ──
                        # A file ADDED on both sides since the last sync (add/add) has a valid repo
                        # merge base and NO base blob of its own. Seeding `base` with OURS in that
                        # case is not a neutral fallback — it actively inverts the result: every key
                        # the fork owns then looks like something present in the ancestor and deleted
                        # upstream, so the merge DROPS it and reports success.
                        #
                        # Measured 2026-09-10: cmp-ios/iosApp/iosApp.entitlements is add/add for
                        # mbs/cappy (absent at b97eb73d; the template added its own in 99083ed2, the
                        # fork added its own). With base:=ours the union emitted ONE key —
                        # keychain-access-groups — silently dropping the fork's
                        # com.apple.security.application-groups, the WidgetKit shared container.
                        #
                        # An EMPTY base states the truth: no common ancestor. A union strategy then
                        # unions (correct), and a line 3-way conflicts (honest, and visible) instead
                        # of quietly handing the file to the template.
                        if [ -n "$_mbase" ] && git show "${_mbase}:$_mf" > "$_b" 2>/dev/null; then
                            :
                        else
                            : > "$_b"
                            print_warning "No common ancestor for ${BOLD}$_mf${NC} (added on both sides) — merging as add/add"
                        fi
                        mkdir -p "$(dirname "$_mf")"
                        # Route the contract-declared strategy: the two structure-aware unions
                        # (include-union / catalog-3way) use their dedicated engines; everything
                        # else (manifest-union / kotlin-3way / strings-union / xml-union / 3way)
                        # goes through cs_merge.
                        local _ms="${CS_M_STRAT:-3way}" _mrc
                        case "$_ms" in
                            include-union) merge_settings_include_union "$_o" "$_b" "$_t" "$_mf"; _mrc=$? ;;
                            catalog-3way)  merge_libs_catalog_3way      "$_o" "$_b" "$_t" "$_mf"; _mrc=$? ;;
                            pbxproj-3way)  merge_pbxproj_3way          "$_o" "$_b" "$_t" "$_mf"; _mrc=$? ;;
                            plist-union)   merge_plist_union           "$_o" "$_b" "$_t" "$_mf"; _mrc=$? ;;
                            *)             cs_merge "$_ms" "$_o" "$_b" "$_t" "$_mf"; _mrc=$? ;;
                        esac
                        # THREE outcomes, not two. A strategy that could not run keeps the fork's
                        # copy and returns 3; reporting that as "Merged — fork edits preserved" is
                        # true about the file and false about what happened, and it is exactly the
                        # reassuring-success-over-a-silent-skip pattern this engine keeps being bitten
                        # by. The helper already warned WHY; this must not contradict it.
                        case "${_mrc:-0}" in
                            0) print_step "Merged (${_ms}) ${BOLD}$_mf${NC} — fork edits preserved" ;;
                            3) print_warning "SKIPPED (${_ms}) ${BOLD}$_mf${NC} — kept the fork's copy; template changes NOT applied" ;;
                            *) print_warning "CONFLICT in ${BOLD}$_mf${NC} (${_ms}) — resolve markers before committing the sync" ;;
                        esac
                    fi
                    rm -f "$_o" "$_b" "$_t"
                done < <(git diff --name-only "$BASE_BRANCH" "$temp_branch" -- "$dir" 2>/dev/null)
            fi

            # Restore excluded files and directories
            if [ -n "${EXCLUSIONS[$dir]}" ]; then
                local IFS=' '
                read -ra EXCLUDE_ITEMS <<< "${EXCLUSIONS[$dir]}"

                for item in "${EXCLUDE_ITEMS[@]}"; do
                    local IFS=':'
                    read -ra PARTS <<< "$item"
                    local exclude_path="$dir/${PARTS[0]}"
                    local exclude_type="${PARTS[1]}"
                    local temp_path="temp_$exclude_path"

                    if [ -e "$temp_path" ]; then
                        print_step "Restoring excluded ${exclude_type}: ${BOLD}${PARTS[0]}${NC}"
                        mkdir -p "$(dirname "$exclude_path")"
                        if [ "$exclude_type" = "dir" ]; then
                            rm -rf "$exclude_path"
                            cp -r "$temp_path" "$(dirname "$exclude_path")"
                        else
                            cp "$temp_path" "$exclude_path"
                        fi
                    fi
                done
            fi
        fi
    else
        print_warning "Directory ${BOLD}$dir${NC} not found. Creating it..."
        if [ "$DRY_RUN" = false ]; then
            mkdir -p "$dir"
            git checkout "$temp_branch" -- "$dir" || {
                handle_error "Failed to sync $dir"
                cleanup_temp_dirs
            }
        fi
    fi
    show_progress
}

# Function to sync individual file with exclusions
sync_file() {
    local file=$1
    local temp_branch=$2

    # Check if file should be excluded (root-level or directory-specific)
    if is_excluded "$(dirname "$file")" "$file" "file"; then
        print_step "Skipping excluded file: ${BOLD}$file${NC}"
        return
    fi

    print_step "Syncing ${BOLD}$file${NC}..."
    if [ "$DRY_RUN" = false ]; then
        if [ -f "$file" ]; then
            # Create directory for excluded files if it doesn't exist
            mkdir -p "temp_files"
            # Store original file if it exists
            cp "$file" "temp_files/$(basename "$file")"
        fi

        if ! git checkout "$temp_branch" -- "$file"; then
            if [ -f "temp_files/$(basename "$file")" ]; then
                # Restore original file if checkout fails
                cp "temp_files/$(basename "$file")" "$file"
            fi
            print_error "Failed to sync $file"
            return 1
        fi
    fi
    show_progress
}

# Function to get default branch name
get_default_branch() {
    local default_branch
    default_branch=$(git remote show origin | grep 'HEAD branch' | cut -d' ' -f5)
    echo "$default_branch"
}

# Function to preserve root-level excluded files
preserve_root_files() {
    if [ -n "${EXCLUSIONS["root"]}" ] && [ "$DRY_RUN" = false ]; then
        print_step "Preserving root-level excluded files..."
        mkdir -p "temp_root"

        local IFS=' '
        read -ra ROOT_EXCLUDE_ITEMS <<< "${EXCLUSIONS["root"]}"

        for item in "${ROOT_EXCLUDE_ITEMS[@]}"; do
            local IFS=':'
            read -ra PARTS <<< "$item"
            local exclude_path="${PARTS[0]}"
            local exclude_type="${PARTS[1]}"

            if [ "$exclude_type" = "file" ] && [ -f "$exclude_path" ]; then
                print_step "Preserving root file: ${BOLD}$exclude_path${NC}"
                cp "$exclude_path" "temp_root/"
            fi
        done
    fi
}

# Function to restore root-level excluded files
restore_root_files() {
    if [ -n "${EXCLUSIONS["root"]}" ] && [ "$DRY_RUN" = false ]; then
        print_step "Restoring root-level excluded files..."

        local IFS=' '
        read -ra ROOT_EXCLUDE_ITEMS <<< "${EXCLUSIONS["root"]}"

        for item in "${ROOT_EXCLUDE_ITEMS[@]}"; do
            local IFS=':'
            read -ra PARTS <<< "$item"
            local exclude_path="${PARTS[0]}"
            local exclude_type="${PARTS[1]}"

            if [ "$exclude_type" = "file" ] && [ -f "temp_root/$(basename "$exclude_path")" ]; then
                print_step "Restoring root file: ${BOLD}$exclude_path${NC}"
                cp "temp_root/$(basename "$exclude_path")" "$exclude_path"
            fi
        done

        rm -rf "temp_root"
    fi
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        --list)
            # Print the canonical sync surface (one entry per line) and exit — read-only,
            # no git/side-effects. Consumed by framework-verify-sync-reachability (AC11) and
            # by maintainers auditing what the backbone-widened surface now propagates (WS4).
            printf '%s\n' "${SYNC_DIRS[@]}" "${SYNC_FILES[@]}"
            exit 0
            ;;
        --dry-run)
            DRY_RUN=true
            shift
            ;;
        -f|--force)
            FORCE=true
            shift
            ;;
        --only)
            ONLY_ITEMS="${2:-}"
            [ -z "$ONLY_ITEMS" ] && handle_error "--only requires a comma/space-separated list of dirs/files (e.g. --only build-logic,gradle/wrapper)"
            shift 2
            ;;
        *)
            handle_error "Unknown option: $1. Use --help for usage information."
            ;;
    esac
done

# --only: restrict SYNC_DIRS/SYNC_FILES to the named subset (targeted sync for diverged forks
# that only want specific infra refreshed). Each named item MUST already be a declared SYNC_DIR
# or SYNC_FILE — this narrows the canonical set, it never adds un-vetted paths.
if [ -n "$ONLY_ITEMS" ]; then
    ONLY_ITEMS="${ONLY_ITEMS//,/ }"
    declare -a _KEEP_DIRS=() _KEEP_FILES=()
    for _want in $ONLY_ITEMS; do
        _found=false
        for _d in "${SYNC_DIRS[@]}";  do [ "$_d" = "$_want" ] && { _KEEP_DIRS+=("$_want");  _found=true; break; }; done
        for _f in "${SYNC_FILES[@]}"; do [ "$_f" = "$_want" ] && { _KEEP_FILES+=("$_want"); _found=true; break; }; done
        [ "$_found" = false ] && handle_error "--only: '$_want' is not a declared SYNC_DIR or SYNC_FILE. Choose from: ${SYNC_DIRS[*]} ${SYNC_FILES[*]}"
    done
    SYNC_DIRS=("${_KEEP_DIRS[@]}")
    SYNC_FILES=("${_KEEP_FILES[@]}")
    echo -e "${YELLOW}${BOLD}Targeted sync (--only):${NC} ${SYNC_DIRS[*]} ${SYNC_FILES[*]}"
fi

# Check git configuration
if ! git config user.name > /dev/null || ! git config user.email > /dev/null; then
    handle_error "Git user.name or user.email not configured"
fi

# Main script
clear
print_banner
print_items

# Print configured exclusions
echo -e "${BLUE}Configured Exclusions:${NC}"
OLD_IFS="$IFS"  # Save original IFS
for dir in "${!EXCLUSIONS[@]}"; do
    echo -e "  ${BOLD}${dir}${NC}:"
    IFS=' '
    read -ra EXCLUDE_ITEMS <<< "${EXCLUSIONS[$dir]}"
    for item in "${EXCLUDE_ITEMS[@]}"; do
        IFS=':'
        read -ra PARTS <<< "$item"
        if [ "$dir" = "root" ]; then
            echo -e "    → ${PARTS[0]} (${PARTS[1]}) [root level]"
        else
            echo -e "    → ${PARTS[0]} (${PARTS[1]})"
        fi
    done
done
IFS="$OLD_IFS"  # Restore original IFS
echo

# Resolve the TEMPLATE remote. The sync source is ALWAYS the template repo — but a consumer app may use
# its own `upstream` remote for its OWN upstream (e.g. openMF/mifos-x-group-banking), NOT the template.
# So find whichever remote points at the template URL (backward-compatible: a direct fork's `upstream`
# IS the template); if none, add a dedicated `template` remote. Override name via TEMPLATE_REMOTE_NAME,
# URL via TEMPLATE_URL.
DEFAULT_UPSTREAM_URL="${TEMPLATE_URL:-$DEFAULT_UPSTREAM_URL}"
# Normalize a git URL to `host/owner/repo` so SSH, scp-like, and HTTPS forms of the SAME repo compare
# equal (e.g. git@github.com:openMF/kmp-project-template.git ≡ https://github.com/openMF/kmp-project-template).
# Portable sed only (no GNU \L) — runs on macOS (BSD sed) + Linux CI.
_norm_url() {
    echo "$1" | sed -E '
        s|\.git$||
        s|/$||
        s|^[a-zA-Z][a-zA-Z0-9+.-]*://||
        s|^[^@/]+@||
        s|^([^/:]+):|\1/|
    '
}
TEMPLATE_REMOTE=""
for _r in $(git remote); do
    if [ "$(_norm_url "$(git remote get-url "$_r" 2>/dev/null)")" = "$(_norm_url "$DEFAULT_UPSTREAM_URL")" ]; then
        TEMPLATE_REMOTE="$_r"; break
    fi
done
if [ -z "$TEMPLATE_REMOTE" ]; then
    TEMPLATE_REMOTE="${TEMPLATE_REMOTE_NAME:-template}"
    print_warning "No remote points at the template — using dedicated remote '${BOLD}$TEMPLATE_REMOTE${NC}' → $DEFAULT_UPSTREAM_URL"
    # A git remote is config-only (no working-tree/history change) and is a read-side prerequisite for
    # computing the diff — so it is added even on --dry-run, which must fetch the template to show what
    # WOULD sync. Skipping it in dry-run would make the fetch below fail on a fork with no template remote.
    git remote get-url "$TEMPLATE_REMOTE" >/dev/null 2>&1 \
        || git remote add "$TEMPLATE_REMOTE" "$DEFAULT_UPSTREAM_URL" \
        || handle_error "Failed to add $TEMPLATE_REMOTE remote"
    show_progress
fi

# Fetch from the template
print_step "Fetching from template remote (${BOLD}$TEMPLATE_REMOTE${NC})..."
if ! git fetch "$TEMPLATE_REMOTE"; then
    handle_error "Failed to fetch from $TEMPLATE_REMOTE"
fi
show_progress

# Get default branch if dev doesn't exist
DEFAULT_BRANCH=$(get_default_branch)
BASE_BRANCH="dev"
if ! git rev-parse --verify "origin/dev" >/dev/null 2>&1; then
    print_warning "dev branch not found, using default branch: ${BOLD}$DEFAULT_BRANCH${NC}"
    BASE_BRANCH="$DEFAULT_BRANCH"
fi

# ── SELF-UPDATE + RE-EXEC — always run the LATEST engine (close the stale-engine chicken-and-egg) ──
# sync-dirs.sh is template-owned + self-propagating, but a fork runs its OWN copy, which only updates
# MID dir-loop (in `scripts/`) — too late for the current run. A fork carrying a stale engine (e.g. one
# missing the STEP-0 merge-protection bootstrap above) would clobber before it ever picks up the fix.
# So: if the template's engine differs from the running one, create the sync branch NOW, COMMIT the
# fresh engine + merge-protection contract onto it, and RE-EXEC — the whole run then executes the latest
# engine. Committing first keeps the working tree CLEAN across the branch dance (no mid-run edit of the
# running script → no bash re-read hazard, no `checkout` clobber). Guarded (SYNC_DIRS_SELF_UPDATED)
# against an infinite loop; requires a clean tree; skipped in --dry-run (warns instead of mutating).
_self_rel="scripts/white-label/sync-dirs.sh"
if [ "${SYNC_DIRS_SELF_UPDATED:-0}" != "1" ] && [ "$DRY_RUN" = false ] \
   && git cat-file -e "$TEMPLATE_REMOTE/$BASE_BRANCH:$_self_rel" 2>/dev/null; then
    _tpl_engine="$(git rev-parse "$TEMPLATE_REMOTE/$BASE_BRANCH:$_self_rel" 2>/dev/null || echo "")"
    _loc_engine="$(git hash-object "$_self_rel" 2>/dev/null || echo "")"
    if [ -n "$_tpl_engine" ] && [ "$_tpl_engine" != "$_loc_engine" ]; then
        if ! git diff --quiet 2>/dev/null || ! git diff --cached --quiet 2>/dev/null; then
            print_warning "sync engine is stale but the tree is dirty — skipping self-update; running the CURRENT engine (commit/clean the tree to auto-update next run)."
        else
            _su_branch="$(get_sync_branch_name)"
            print_step "Self-updating the sync engine from the template on ${BOLD}$_su_branch${NC}, then re-running on the latest…"
            if git checkout -b "$_su_branch" "$BASE_BRANCH" >/dev/null 2>&1; then
                for _up in "$_self_rel" scripts/customization-surface.sh customization-surface.yaml; do
                    git cat-file -e "$TEMPLATE_REMOTE/$BASE_BRANCH:$_up" 2>/dev/null || continue
                    mkdir -p "$(dirname "$_up")"
                    git show "$TEMPLATE_REMOTE/$BASE_BRANCH:$_up" > "$_up" 2>/dev/null
                done
                chmod +x "$_self_rel" scripts/customization-surface.sh 2>/dev/null || true
                git add "$_self_rel" scripts/customization-surface.sh customization-surface.yaml >/dev/null 2>&1
                git commit -q -m "chore(sync): self-update engine + merge-protection contract from template" >/dev/null 2>&1 || true
                SYNC_DIRS_SELF_UPDATED=1 SYNC_PREBUILT_BRANCH="$_su_branch" exec bash "$_self_rel" ${ORIGINAL_ARGS[@]+"${ORIGINAL_ARGS[@]}"}
            fi
            print_warning "self-update could not create its branch — proceeding on the CURRENT engine."
        fi
    fi
fi

# Create sync branch (or REUSE the one the self-update pre-built the fresh engine onto)
if [ -n "${SYNC_PREBUILT_BRANCH:-}" ]; then
    SYNC_BRANCH="$SYNC_PREBUILT_BRANCH"
    print_step "Reusing self-update sync branch: ${BOLD}$SYNC_BRANCH${NC}"
else
    SYNC_BRANCH=$(get_sync_branch_name)
    print_step "Creating sync branch: ${BOLD}$SYNC_BRANCH${NC}"
fi

if [ "$DRY_RUN" = false ]; then
    # Create sync branch from base branch — SKIP when the self-update already created + checked it out
    # (we are already on it, with the fresh engine committed and the tree clean).
    if [ -z "${SYNC_PREBUILT_BRANCH:-}" ] && ! git checkout -b "$SYNC_BRANCH" "$BASE_BRANCH"; then
        handle_error "Failed to create sync branch"
    fi
    show_progress

    # Create temporary branch for upstream changes
    TEMP_BRANCH="temp-${SYNC_BRANCH}"
    print_step "Creating temporary branch: ${BOLD}$TEMP_BRANCH${NC}"
    if ! git checkout -b "$TEMP_BRANCH" "$TEMPLATE_REMOTE/$BASE_BRANCH"; then
        handle_error "Failed to create temporary branch"
    fi
    show_progress

    # Switch back to sync branch
    print_step "Switching back to sync branch..."
    if ! git checkout "$SYNC_BRANCH"; then
        handle_error "Failed to switch to sync branch"
    fi
    show_progress

    # Preserve root-level excluded files
    preserve_root_files

    # ── Capture the PRIOR synced template commit (deletion-propagation anchor) ──
    # Read BEFORE any dir sync touches the tree — `.template-version` is fork-owned
    # (never clobbered by a dir sync) and still holds the sha from the LAST sync at this
    # point. Used by propagate_template_deletions() below to detect files the template
    # deleted since we last synced. Empty on a fork's first-ever sync (nothing to diff
    # against) — that's fine, deletion-propagation just no-ops.
    PREV_TEMPLATE_SHA=""
    if [ -f .template-version ]; then
        PREV_TEMPLATE_SHA="$(grep '^template_sha=' .template-version 2>/dev/null | cut -d= -f2)"
        if [ -n "$PREV_TEMPLATE_SHA" ] && ! git cat-file -e "$PREV_TEMPLATE_SHA" 2>/dev/null; then
            print_warning "recorded template_sha ($PREV_TEMPLATE_SHA) is not reachable locally — skipping deletion-propagation this run (fetch more template history to re-enable)."
            PREV_TEMPLATE_SHA=""
        fi
    fi

    # ── STEP 0 BOOTSTRAP — the merge-protection contract MUST be on disk before ANY dir syncs ──
    # is_excluded()'s fork-ownership check AND the 3-way merge engine (cs_merge) both silently
    # NO-OP when scripts/customization-surface.sh or the root customization-surface.yaml are absent.
    # Because `scripts/` sits mid-SYNC_DIRS (after cmp-android / cmp-shared / core) and
    # customization-surface.yaml is a SYNC_FILE (synced AFTER every dir), a fork that has not yet
    # committed the contract (a legacy / pre-white-label base — e.g. syncing onto a dev branch whose
    # white-label adoption is still on an unmerged PR) would sync cmp-android + ~14 dirs with ZERO
    # merge protection — blind-overwriting merge-owned files like AndroidManifest.xml and dropping
    # the fork's CAMERA / RECORD_AUDIO permissions. Materialize the contract from the template FIRST
    # (both files are owner:template / copy-exact, so this is idempotent + safe) so EVERY subsequent
    # dir gets full is_excluded + 3-way-merge protection. Self-bootstrap → auto-resolve; this is what
    # makes the sync run cleanly on a not-yet-white-labelled base instead of clobbering or halting.
    # The merge HELPERS bootstrap with the contract, for the same reason the contract does.
    # `scripts/` is SYNC_DIRS #18; cmp-ios is #3. So on a fork that does not yet carry
    # merge-pbxproj.rb / merge-plist.rb, cmp-ios (and cmp-android, cmp-desktop) are merged FIFTEEN
    # directories before `scripts/` delivers them — every one silently degrading to keep-ours with
    # only a warning, on the very first sync, which is precisely the sync that most needs them.
    # Measured 2026-09-11 on a real mbs/cappy clone against merged dev: "merge-pbxproj.rb absent —
    # KEPT the fork's project.pbxproj". Reordering SYNC_DIRS would be the fragile fix (it re-breaks
    # the moment someone sorts the list); materializing the helpers up front is the same move STEP 0
    # already makes for customization-surface.{yaml,sh}, and is equally idempotent — all four files
    # are owner:template / copy-exact.
    # ruby-exec.sh is in the set because the two mergers SOURCE it — bootstrapping them without it
    # just moves the failure one step ("ruby-exec.sh unavailable — KEPT the fork's project.pbxproj").
    # This list is a dependency CLOSURE, not a file list: anything STEP 0's consumers need before
    # `scripts/` syncs belongs here.
    for _bp in customization-surface.yaml scripts/customization-surface.sh \
               scripts/ruby-exec.sh \
               scripts/white-label/merge-pbxproj.rb scripts/white-label/merge-plist.rb; do
        if git cat-file -e "$TEMP_BRANCH:$_bp" 2>/dev/null; then
            mkdir -p "$(dirname "$_bp")"
            if git show "$TEMP_BRANCH:$_bp" > "$_bp" 2>/dev/null; then
                print_step "bootstrapped merge-protection contract: ${BOLD}$_bp${NC}"
            fi
        fi
    done
    [ -f scripts/customization-surface.sh ] && chmod +x scripts/customization-surface.sh 2>/dev/null || true
fi

# ── customization-surface advisory (fork-ownership contract) ──────────────────
# Non-breaking: reports which about-to-be-synced paths are `merge`-owned per
# customization-surface.yaml, so a maintainer can confirm the EXCLUSIONS map
# preserves fork edits (e.g. AndroidManifest permissions). The mechanical sync
# below is UNCHANGED — the contract is the declared source of truth the merge
# engine adopts next. Fully guarded so it can never abort a sync.
CS_READER="$SCRIPT_DIR/../customization-surface.sh"
if [ -f "$CS_READER" ] && [ -f "$SCRIPT_DIR/../../customization-surface.yaml" ]; then
    # shellcheck source=/dev/null
    source "$CS_READER" 2>/dev/null || true
    if declare -F cs_match_g >/dev/null 2>&1; then
        echo -e "\n${BLUE}${BOLD}Fork-ownership contract — merge-owned paths in the sync surface${NC}"
        cs_merge_hits=0
        while IFS= read -r _csf; do
            cs_match_g "$_csf" || continue
            if [ "${CS_M_OWNER:-}" = "merge" ]; then
                echo -e "  ${YELLOW}merge${NC} $_csf ${YELLOW}(${CS_M_STRAT:-3way} — do NOT blind-overwrite)${NC}"
                cs_merge_hits=$((cs_merge_hits + 1))
            fi
        done < <(git ls-files -- "${SYNC_DIRS[@]}" 2>/dev/null)
        [ "$cs_merge_hits" -gt 0 ] && echo -e "  ${YELLOW}⚠ $cs_merge_hits merge-owned path(s) — verify EXCLUSIONS preserves fork edits (permissions, catalog, nav).${NC}"
    fi
fi

# Sync directories
echo -e "\n${BLUE}${BOLD}Syncing directories...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
for dir in "${SYNC_DIRS[@]}"; do
    sync_directory "$dir" "$TEMP_BRANCH"
done

# Sync files
echo -e "\n${BLUE}${BOLD}Syncing files...${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"
for file in "${SYNC_FILES[@]}"; do
    sync_file "$file" "$TEMP_BRANCH"
done

# Self-heal libs.versions.toml — pull missing build-logic aliases from upstream.
# `build-logic/` is sync'd but `gradle/libs.versions.toml` is consumer-local (it
# carries project-specific package names + version overrides). When upstream adds
# new `libs.<X>` accessors in build-logic with their matching catalog entries,
# only the build-logic gets sync'd — the consumer's catalog falls behind and the
# synced build-logic references aliases that don't exist locally. This function
# detects that gap and pulls the missing alias entries (and their referenced
# version keys) directly from the upstream catalog (`$TEMP_BRANCH`).
heal_libs_versions_toml() {
    local libs_toml="gradle/libs.versions.toml"

    if [ ! -f "$libs_toml" ]; then
        return 0
    fi
    if [ ! -d "build-logic" ]; then
        return 0
    fi

    echo -e "\n${BLUE}${BOLD}Healing libs.versions.toml...${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}\n"

    local raw_refs
    raw_refs=$(grep -rhoE '\blibs\.[a-zA-Z][a-zA-Z0-9._]*' \
            --include='*.kt' --include='*.kts' \
            build-logic 2>/dev/null \
        | sed -E 's/^libs\.//' \
        | sort -u)

    # Method-call accessors are not alias references — they take string args.
    local method_re='^(findLibrary|findVersion|findBundle|findPlugin)([(.]|$)'

    # Cache upstream's catalog content once.
    local upstream_toml
    upstream_toml=$(git show "${TEMP_BRANCH}:${libs_toml}" 2>/dev/null || true)
    if [ -z "$upstream_toml" ]; then
        print_warning "Upstream has no ${libs_toml} — skipping heal."
        return 0
    fi

    # Helper: insert a line right after [section] in the consumer's catalog.
    insert_into_section() {
        local section_name="$1"
        local line="$2"
        local line_no
        line_no=$(grep -n "^\[${section_name}\]" "$libs_toml" | head -1 | cut -d: -f1)
        if [ -z "$line_no" ]; then
            print_warning "No [${section_name}] section header — skipping insert of '$line'"
            return 1
        fi
        { head -n "$line_no" "$libs_toml"; echo "$line"; tail -n +$((line_no + 1)) "$libs_toml"; } > "${libs_toml}.tmp"
        mv "${libs_toml}.tmp" "$libs_toml"
    }

    local healed=0
    local warnings=0
    while IFS= read -r ref; do
        [ -z "$ref" ] && continue
        [[ "$ref" =~ $method_re ]] && continue

        local section="libraries"
        local lookup="$ref"
        case "$ref" in
            plugins.*)
                section="plugins"
                lookup="${ref#plugins.}"
                ;;
            versions.*)
                section="versions"
                lookup="${ref#versions.}"
                [ "$lookup" = "toml" ] && continue
                ;;
        esac
        local key
        key=$(echo "$lookup" | sed 's/\./-/g')

        if grep -qE "^${key}[[:space:]]*=" "$libs_toml"; then
            continue
        fi

        local upstream_line
        upstream_line=$(echo "$upstream_toml" | grep -E "^${key}[[:space:]]*=" | head -1)
        if [ -z "$upstream_line" ]; then
            print_warning "Missing alias '$key' (for libs.$ref) not present in upstream catalog either — manual fix needed."
            warnings=$((warnings + 1))
            continue
        fi

        # Pull the version key too, if the alias version.refs something missing.
        local ref_version
        ref_version=$(echo "$upstream_line" | grep -oE 'version\.ref = "[^"]+"' | sed -E 's/version\.ref = "([^"]+)"/\1/' | head -1)
        if [ -n "$ref_version" ] && ! grep -qE "^${ref_version}[[:space:]]*=" "$libs_toml"; then
            local ver_line
            ver_line=$(echo "$upstream_toml" | grep -E "^${ref_version}[[:space:]]*=" | head -1)
            if [ -n "$ver_line" ]; then
                if insert_into_section "versions" "$ver_line"; then
                    echo -e "  ${GREEN}${CHECKMARK}${NC} Added version '${ref_version}' (referenced by '${key}') to [versions]"
                    healed=$((healed + 1))
                fi
            fi
        fi

        if insert_into_section "$section" "$upstream_line"; then
            echo -e "  ${GREEN}${CHECKMARK}${NC} Added '${key}' to [${section}] from upstream"
            healed=$((healed + 1))
        fi
    done <<< "$raw_refs"

    if [ $healed -gt 0 ]; then
        echo -e "\n${GREEN}${BOLD}🩹 Healed ${healed} entries in ${libs_toml} — they will be included in the sync.${NC}"
    elif [ $warnings -gt 0 ]; then
        print_warning "${warnings} missing aliases could not be auto-healed (not in upstream either). Manual fix needed."
    else
        echo -e "  ${GREEN}${CHECKMARK}${NC} libs.versions.toml already in sync with build-logic — no heal needed."
    fi
}

if [ "$DRY_RUN" = false ]; then
    # Restore root-level excluded files
    restore_root_files

    cleanup_temp_dirs
    rm -rf temp_files

    # E0/T4: contract-keyed root merges (settings.gradle.kts include-union +
    # gradle/libs.versions.toml catalog-3way) so template module-includes AND version
    # bumps reach the fork. Runs BEFORE we drop the temp branch (needs upstream's copies).
    # Replaces the old one-way heal_libs_versions_toml (retained as a no-reader fallback).
    merge_contract_root_files

    # Cleanup temporary branch
    print_step "Cleaning up temporary branch..."
    git branch -D "$TEMP_BRANCH" || handle_error "Failed to delete temporary branch"
    show_progress

    # Record which template commit this sync pulled from. This is the whole point of the
    # `.template-version` marker: a fork can tell whether it is behind the template (compare
    # template_sha against the template's latest release) and the NEXT sync can anchor its 3-way
    # merge base on this exact point instead of git's guessed common ancestor. Written here (before
    # the commit gate) so a version-only bump still commits; the file is fork-owned
    # (customization-surface.yaml owner: fork) so a template dir-copy never clobbers it.
    print_step "Recording template sync version (.template-version)..."
    _tv_sha="$(git rev-parse "$TEMPLATE_REMOTE/$BASE_BRANCH" 2>/dev/null || echo "")"
    _tv_tag="$(git describe --tags --exact-match "$_tv_sha" 2>/dev/null || echo "")"
    {
        echo "# .template-version — the openMF/kmp-project-template commit this tree last synced from."
        echo "# Managed by scripts/white-label/sync-dirs.sh — DO NOT hand-edit."
        echo "# Format: bash-parseable key=value (same convention as gradle/fork.properties)."
        echo "template_repo=openMF/kmp-project-template"
        echo "template_ref=$BASE_BRANCH"
        echo "template_sha=$_tv_sha"
        echo "template_tag=$_tv_tag"
        echo "synced_at=$(date -u +%Y-%m-%dT%H:%M:%SZ)"
    } > .template-version
    git add .template-version 2>/dev/null || true
    show_progress

    # Check for changes
    if ! git diff --quiet --exit-code --cached; then
        print_step "Committing changes..."
        git add "${SYNC_DIRS[@]}" "${SYNC_FILES[@]}"
        # Include any catalog heal additions made by heal_libs_versions_toml.
        if [ -f "gradle/libs.versions.toml" ]; then
            git add "gradle/libs.versions.toml"
        fi
        git commit -m "sync: Update directories and files from upstream

This PR syncs the following items with upstream:
- Directories: ${SYNC_DIRS[*]}
- Files: ${SYNC_FILES[*]}
- Auto-heal: gradle/libs.versions.toml (pulls missing build-logic aliases from upstream)" || handle_error "Failed to commit changes"
        show_progress

        if [ "$FORCE" = false ]; then
            echo -e "\n${YELLOW}${BOLD}Would you like to push the sync branch? (y/n)${NC}"
            read -r response
            if [[ "$response" =~ ^[Yy]$ ]]; then
                print_step "Pushing sync branch..."
                git push -u origin "$SYNC_BRANCH" || handle_error "Failed to push sync branch"
                show_progress
                echo -e "\n${GREEN}${BOLD}✨ Sync branch pushed successfully! ✨${NC}"
                echo -e "${YELLOW}Please create a pull request from branch ${BOLD}$SYNC_BRANCH${NC}${YELLOW} to ${BOLD}$BASE_BRANCH${NC}${YELLOW} in your repository.${NC}\n"
            else
                echo -e "\n${YELLOW}Changes committed but not pushed. You can push later with:${NC}"
                echo -e "${BOLD}git push -u origin $SYNC_BRANCH${NC}"
                echo -e "${YELLOW}Then create a pull request from ${BOLD}$SYNC_BRANCH${NC}${YELLOW} to ${BOLD}$BASE_BRANCH${NC}\n"
            fi
        else
            print_step "Pushing sync branch..."
            git push -u origin "$SYNC_BRANCH" || handle_error "Failed to push sync branch"
            show_progress
            echo -e "\n${GREEN}${BOLD}✨ Sync branch pushed successfully! ✨${NC}"
            echo -e "${YELLOW}Please create a pull request from branch ${BOLD}$SYNC_BRANCH${NC}${YELLOW} to ${BOLD}$BASE_BRANCH${NC}${YELLOW} in your repository.${NC}\n"
        fi
    else
        print_warning "No changes to commit"
        git checkout "$BASE_BRANCH"
        git branch -D "$SYNC_BRANCH"
    fi
else
    echo -e "\n${YELLOW}${BOLD}Dry run completed. No changes were made.${NC}\n"
fi
