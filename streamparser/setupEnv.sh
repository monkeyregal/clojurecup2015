#!/usr/bin/env zsh

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

export GNSDK_ARCH="mac_x86-64"
export GNSDK_JAVA="j2se"
export GNSDK_LIB="$(pwd)/vendor/lib/$GNSDK_ARCH"
#export GNSDK_JAR="$(pwd)/vendor/jar/$GNSDK_JAVA"
#export GNSDK_WRAPPER_LIB="$DIR/dev/gnsdk/wrappers/gnsdk_java/lib/$GNSDK_ARCH"
#export GNSDK_MANAGER_LIB="$DIR/dev/gnsdk/lib/$GNSDK_ARCH"

#export BOOT_JVM_OPTIONS="-client -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xmx2g -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Xverify:none -Djava.library.path=$GNSDK_WRAPPER_LIB"

export BOOT_JVM_OPTIONS="-Djava.library.path=$GNSDK_LIB"

source settings.sh
