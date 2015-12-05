#!/bin/sh
export GNSDK_ARCH="mac_x86-64"
export GNSDK_JAVA="j2se"
export GNSDK_LIB="$(pwd)/vendor/lib/$GNSDK_ARCH"
export GNSDK_JAR="$(pwd)/vendor/jar/$GNSDK_JAVA"

export BOOT_JVM_OPTIONS="-client -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xmx2g -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -Xverify:none -Djava.library.path=$GNSDK_LIB"
