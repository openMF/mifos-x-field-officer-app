#!/usr/bin/env bash
#
# copy-compose-resources.sh — bundle Compose Multiplatform resources into the iOS app (E6).
#
# Invoked as an Xcode Run-Script build phase on the `iosApp` target, positioned AFTER the
# `Resources` (Copy Bundle Resources) phase so the `.app` bundle already exists.
#
# WHY THIS EXISTS
# The KMP `ComposeApp` framework is STATIC (isStatic=true) — it is linked into the app binary,
# so its `ComposeApp.framework/composeResources/` directory is NOT present in the runtime `.app`.
# But Compose Multiplatform's iOS resource reader resolves resources relative to the MAIN bundle:
#     <App>.app/compose-resources/composeResources/<qualified.pkg>.generated.resources/<type>/<file>
# Compose resources are copied by this script (there is no package-manager phase that would
# to place them there is gone — so fonts/images silently never ship and the UI crashes on first
# access with `org.jetbrains.compose.resources.MissingResourceException`. This phase restores that
# copy: it takes the composeResources OUT of the flavor-matched framework slice that
# embed-xcframework.sh already staged into FRAMEWORK_SEARCH_PATHS, and copies them into the .app.
set -euo pipefail

SRCROOT="${SRCROOT:-$(cd "$(dirname "$0")/.." && pwd)}"
REPO_ROOT="$(cd "$SRCROOT/.." && pwd)"
CONFIG="${CONFIGURATION:-prodRelease}"
SDK="${SDK_NAME:-iphoneos}"

# The framework slice embed-xcframework.sh staged for FRAMEWORK_SEARCH_PATHS
# (cmp-shared/build/xcode-frameworks/$CONFIGURATION/$SDK_NAME/ComposeApp.framework).
SRC="$REPO_ROOT/cmp-shared/build/xcode-frameworks/$CONFIG/$SDK/ComposeApp.framework/composeResources"

if [ ! -d "$SRC" ]; then
  echo "warning: [KMP] composeResources not found at $SRC — UI will crash MissingResourceException" >&2
  exit 0
fi

if [ -z "${TARGET_BUILD_DIR:-}" ] || [ -z "${WRAPPER_NAME:-}" ]; then
  echo "warning: [KMP] TARGET_BUILD_DIR / WRAPPER_NAME unset (not an Xcode build?) — skipping resource copy" >&2
  exit 0
fi

DEST="$TARGET_BUILD_DIR/$WRAPPER_NAME/compose-resources"
mkdir -p "$DEST"
# rsync the composeResources tree in → <App>.app/compose-resources/composeResources
rsync -a --delete "$SRC" "$DEST/"
echo "note: [KMP] copied composeResources → $DEST/composeResources ($(find "$DEST" -type f | wc -l | tr -d ' ') files)"
