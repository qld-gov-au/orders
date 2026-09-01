#!/usr/bin/env bash
# Quick script: Ensure Maven is using JDK 21


# Detect if running in zsh and emulate bash if sourced
if [ -n "$ZSH_VERSION" ]; then
    emulate bash
fi

#bamboo_capability_system_jdk_JDK_21
if [ -z ${bamboo_capability_system_jdk_JDK_21+x} ]; then
    echo "Not Bamboo Build, won't set variables";
else
    echo "Bamboo Build set JAVA_HOME, maven bin and Java";
    export JAVA_HOME=${bamboo_capability_system_jdk_JDK_21}
    export mvnBin=${bamboo_capability_system_builder_mvn3_Maven_3}/bin
    export javaBin=${bamboo_capability_system_jdk_JDK_21}/bin
    export PATH=${javaBin}:${mvnBin}:$PATH
    mvn -v
fi

update_jdk() {
# --- Step 2: Look for JDK 21 installation ---
POSSIBLE_JAVA_PATHS=(
  "/usr/lib/jvm/zulu-21-amd64"
  "/usr/lib/jvm/java-21-openjdk-amd64"
  "/usr/lib/jvm/java-21-openjdk"
  "/usr/lib/jvm/jdk-21"
  "/opt/homebrew/opt/openjdk@21"
  "/Library/Java/JavaVirtualMachines/microsoft-21.jdk/Contents/Home"
  "/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home"
  "/opt/java/openjdk-21"
)

FOUND_JAVA_HOME=""

for path in "${POSSIBLE_JAVA_PATHS[@]}"; do
  if [ -x "$path/bin/java" ]; then
    FOUND_JAVA_HOME="$path"
    break
  fi
done

# --- Step 3: If found, export environment vars ---
if [ -n "$FOUND_JAVA_HOME" ]; then
  #echo "Found JDK 17 at: $FOUND_JAVA_HOME"
  export JAVA_HOME="$FOUND_JAVA_HOME"
  export PATH="$JAVA_HOME/bin:$PATH"
  #echo "JAVA_HOME set to $JAVA_HOME"
  version=`eval "java -version 2>&1 | head -n 1"`
  echo "PATH updated. Java version now: $version"
  mvn -v | head -n 3
else
  echo "JDK 21 not found in known locations."
  echo "Please install it or update the script with your custom path."
  exit 1
fi

}

set -e

# --- Step 1: Check Maven's current Java version ---
CURRENT_JAVA_VERSION=$(mvn -v 2>/dev/null | grep -Eo 'Java version: [^ ]+' | awk '{print $3}' | cut -d'.' -f1)

if [ "$CURRENT_JAVA_VERSION" = "21" ]; then
  echo "Maven is already using JDK 21."
else
  echo "Maven is not using JDK 21 (detected: ${CURRENT_JAVA_VERSION:-unknown})"
  update_jdk
fi

# Detect if running in zsh and emulate zsh if sourced
if [ -n "$ZSH_VERSION" ]; then
    emulate zsh
fi
