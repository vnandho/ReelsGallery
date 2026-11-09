#!/usr/bin/env bash
# Lightweight Gradle bootstrapper. The official distribution is downloaded on first use.
set -euo pipefail
GRADLE_VERSION="9.7.0"
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/gradle-${GRADLE_VERSION}-bin/reels-gallery"
DIST="$BASE/gradle-${GRADLE_VERSION}"
GRADLE="$DIST/bin/gradle"
if [[ ! -x "$GRADLE" ]]; then
  mkdir -p "$BASE"
  ZIP="$BASE/gradle-${GRADLE_VERSION}-bin.zip"
  echo "Gradle ${GRADLE_VERSION} belum tersedia. Mengunduh distribusi resmi..."
  command -v curl >/dev/null 2>&1 || { echo "curl dibutuhkan untuk bootstrap Gradle." >&2; exit 1; }
  curl --fail --location --retry 2 "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip" --output "$ZIP"
  unzip -q "$ZIP" -d "$BASE"
  rm -f "$ZIP"
fi
exec "$GRADLE" "$@"
