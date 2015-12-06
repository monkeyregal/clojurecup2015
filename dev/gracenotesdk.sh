#!/usr/bin/env bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
pushd $DIR
if ls gnsdk-*.zip; then
    find ./ -name 'gnsdk-*.zip' -exec unzip {} -d gnsdk \;
    cp -R gnsdk/lib/* ../streamparser/vendor/lib/
    mkdir -p ../streamparser/vendor/lib
    mkdir -p ../streamparser/vendor/jar
    cp -R gnsdk/wrappers/gnsdk_java/lib/* ../streamparser/vendor/lib/
    cp -R gnsdk/wrappers/gnsdk_java/jar/* ../streamparser/vendor/jar/
    mvn install:install-file \
        -Dfile=../streamparser/vendor/jar/j2se/gnsdk.jar \
        -DgroupId=gnsdk \
        -DartifactId=gnsdk \
        -Dversion=3.07.7 \
        -Dpackaging=jar \
        -DgeneratePom=true
    mvn install:install-file \
        -Dfile=../streamparser/vendor/jar/j2se/gnsdk_helpers.jar \
        -DgroupId=gnsdk \
        -DartifactId=gnsdk-helpers \
        -Dversion=3.07.7 \
        -Dpackaging=jar \
        -DgeneratePom=true
else
    echo "Please download sdk at: https://developer.gracenote.com/system/files/gnsdk-3.07.7.3701o-20150714.zip"
fi
popd
