#!/usr/bin/env bash
# ── Zelus Launcher — cross-platform packaging script ─────────────────────────
# Usage:
#   ./package.sh            → build for current OS
#   ./package.sh --all      → build all platforms (requires Docker + cross-compile)
#
# Prerequisites:
#   - JDK 17+ (jpackage ships with JDK 14+)
#   - Gradle wrapper  (./gradlew)
#   - WiX Toolset (Windows .msi only)
#   - Platform icons in src/main/resources/
#       zelus.ico   (Windows)
#       zelus.icns  (macOS)
#       zelus.png   (Linux)

set -euo pipefail

VERSION="1.0.0"
JAR_NAME="zelus-launcher-${VERSION}.jar"
DIST_DIR="build/dist"
APP_NAME="ZelusLauncher"

echo "==> Building fat JAR..."
./gradlew shadowJar --quiet

mkdir -p "$DIST_DIR"

detect_os() {
    case "$(uname -s)" in
        Darwin*)  echo "mac"   ;;
        Linux*)   echo "linux" ;;
        MINGW*|MSYS*|CYGWIN*) echo "windows" ;;
        *)        echo "unknown" ;;
    esac
}

package_windows() {
    echo "==> Packaging for Windows (.exe installer)..."
    jpackage \
        --type exe \
        --name "$APP_NAME" \
        --app-version "$VERSION" \
        --vendor "Zelus" \
        --description "Zelus RSPS Launcher" \
        --input "build/libs" \
        --main-jar "$JAR_NAME" \
        --main-class "com.zelus.launcher.ZelusLauncher" \
        --dest "$DIST_DIR/windows" \
        --icon "src/main/resources/zelus.ico" \
        --java-options "-Xmx256m" \
        --java-options "-Dfile.encoding=UTF-8" \
        --win-menu \
        --win-shortcut \
        --win-dir-chooser
    echo "  → $DIST_DIR/windows/${APP_NAME}-${VERSION}.exe"
}

package_mac() {
    echo "==> Packaging for macOS (.dmg)..."
    jpackage \
        --type dmg \
        --name "$APP_NAME" \
        --app-version "$VERSION" \
        --vendor "Zelus" \
        --description "Zelus RSPS Launcher" \
        --input "build/libs" \
        --main-jar "$JAR_NAME" \
        --main-class "com.zelus.launcher.ZelusLauncher" \
        --dest "$DIST_DIR/mac" \
        --icon "src/main/resources/zelus.icns" \
        --java-options "-Xmx256m" \
        --java-options "-Dfile.encoding=UTF-8" \
        --mac-package-name "$APP_NAME"
    echo "  → $DIST_DIR/mac/${APP_NAME}-${VERSION}.dmg"
}

package_linux_deb() {
    echo "==> Packaging for Linux (.deb)..."
    jpackage \
        --type deb \
        --name "zelus-launcher" \
        --app-version "$VERSION" \
        --vendor "Zelus" \
        --description "Zelus RSPS Launcher" \
        --input "build/libs" \
        --main-jar "$JAR_NAME" \
        --main-class "com.zelus.launcher.ZelusLauncher" \
        --dest "$DIST_DIR/linux" \
        --icon "src/main/resources/zelus.png" \
        --java-options "-Xmx256m" \
        --java-options "-Dfile.encoding=UTF-8" \
        --linux-shortcut \
        --linux-menu-group "Games"
    echo "  → $DIST_DIR/linux/zelus-launcher_${VERSION}_amd64.deb"
}

package_appimage() {
    echo "==> Packaging AppImage (portable Linux)..."
    # Step 1: create app-image layout with jpackage
    jpackage \
        --type app-image \
        --name "$APP_NAME" \
        --app-version "$VERSION" \
        --input "build/libs" \
        --main-jar "$JAR_NAME" \
        --main-class "com.zelus.launcher.ZelusLauncher" \
        --dest "$DIST_DIR/appimage-stage" \
        --icon "src/main/resources/zelus.png" \
        --java-options "-Xmx256m"

    # Step 2: wrap with appimagetool (https://appimage.github.io/appimagetool/)
    if command -v appimagetool &>/dev/null; then
        ARCH="x86_64" appimagetool \
            "$DIST_DIR/appimage-stage/${APP_NAME}" \
            "$DIST_DIR/linux/${APP_NAME}-${VERSION}-x86_64.AppImage"
        echo "  → $DIST_DIR/linux/${APP_NAME}-${VERSION}-x86_64.AppImage"
    else
        echo "  [WARN] appimagetool not found — skipping AppImage. Install from https://appimage.github.io/appimagetool/"
    fi
}

# ── Main ──────────────────────────────────────────────────────────────────────
OS=$(detect_os)

if [[ "${1:-}" == "--all" ]]; then
    echo "Building for all platforms..."
    package_windows || true
    package_mac     || true
    package_linux_deb
    package_appimage
else
    case "$OS" in
        windows) package_windows ;;
        mac)     package_mac     ;;
        linux)   package_linux_deb; package_appimage ;;
        *)       echo "Unknown OS — cannot package. Run on Windows, macOS, or Linux."; exit 1 ;;
    esac
fi

echo ""
echo "==> Packaging complete. Artifacts in $DIST_DIR/"
ls -lh "$DIST_DIR"/**/* 2>/dev/null || true
