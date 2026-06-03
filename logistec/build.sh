#!/usr/bin/env bash
# ─────────────────────────────────────────────────────────────────────────────
#  LogísTEC — Build & Run script
#  Usage:
#    ./build.sh          → compile only
#    ./build.sh run      → compile + run GUI
#    ./build.sh test     → compile + run headless on data/caso_prueba.json
# ─────────────────────────────────────────────────────────────────────────────
set -e

SRC_DIR="src"
OUT_DIR="out"
MAIN_CLASS="logistec.Main"
JAR="logistec.jar"

echo "══ LogísTEC Build ══════════════════════════════════════════"
echo "  Java: $(java -version 2>&1 | head -1)"

SOURCES=$(find "$SRC_DIR" -name "*.java" | tr '\n' ' ')

mkdir -p "$OUT_DIR"
echo "  Compiling..."
javac -d "$OUT_DIR" --source-path "$SRC_DIR" $SOURCES
echo "  Compilation successful."

echo "  Creating JAR..."
cat > /tmp/MANIFEST.MF <<EOF
Main-Class: $MAIN_CLASS
EOF
jar cfm "$JAR" /tmp/MANIFEST.MF -C "$OUT_DIR" .
echo "  JAR created: $JAR"
echo "══ Build complete ══════════════════════════════════════════"

if [ "$1" = "run" ]; then
    echo "  Starting GUI..."
    java -jar "$JAR"
elif [ "$1" = "test" ]; then
    echo "  Running headless on data/caso_prueba.json..."
    java -jar "$JAR" data/caso_prueba.json
fi
