#!/bin/bash

# TODO: get values from pom.xml

export APPNAME='gpxtrack'
export VERSION='0.1.0'
export MAIN_FUNC='gtb.main'
export JAR_FILE='gpxtrack.jar'

export TIMESTAMP=`date -Isec`

if [ -z "$COMMIT" ]; then
  export COMMIT=`git rev-parse HEAD`
fi

export CLASSES="tmp/classes"

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

clj -e "(set! *compile-path* \"${CLASSES}\") (compile '${MAIN_FUNC})" \
  && clj -A:uberjar \
  || exit 1

chmod +r tmp/*.jar

echo "start command:"
echo "  CONFIG_EDN=..conf/angara.edn java -cp ${JAR_FILE} ${MAIN_FUNC}"

#.
