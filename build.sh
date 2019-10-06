#!/bin/bash

export APPNAME=$(sed -n 's|^\s*<artifactId>\(.*\)</artifactId>\s*$|\1|p' pom.xml | head -1)
export VERSION=$(sed -n 's|^\s*<version>\(.*\)</version>\s*$|\1|p' pom.xml | head -1)
# export APPNAME='gpxtrack'
# export VERSION='0.1.0'
export MAIN_FUNC='gtb.main'
export JAR_FILE='gpxtrack.jar'

export TIMESTAMP=`date -Isec`

if [ -z "$COMMIT" ]; then
  export COMMIT=`git rev-parse HEAD`
fi

echo " building:" $APPNAME $VERSION
echo "timestamp:" $TIMESTAMP
echo "   commit:" $COMMIT
echo ""

export CLASSES="target/classes"

rm -r ${CLASSES}
mkdir -p ${CLASSES}
BUILD_EDN="${CLASSES}/build.edn"

echo "" > ${BUILD_EDN}
echo "{">>${BUILD_EDN}
echo ":appname \"${APPNAME}\"">>${BUILD_EDN}
echo ":version \"${VERSION}\"">>${BUILD_EDN}
echo ":commit \"${COMMIT}\"">>${BUILD_EDN}
echo ":timestamp \"${TIMESTAMP}\"">>${BUILD_EDN}
echo "}">>${BUILD_EDN}

# javac src/org/mindrot/jbcrypt/BCrypt.java

clj -e "(set! *compile-path* \"${CLASSES}\") (compile '${MAIN_FUNC})" && clj -A:uberjar || exit 1
chmod +r target/*.jar

echo "start command:"
echo "  CONFIG_EDN=..conf/angara.edn java -jar ${JAR_FILE}"

#.
