#! /bin/sh

APP_HOME=$( cd "$( dirname "$0" )" && pwd )

VERSION=0.1.0-SNAPSHOT
UBERJAR=${APP_HOME}/lib/itunes-media-import-${VERSION}-standalone.jar

echo "Current Dir " $APP_HOME

java -Dapp.home=${APP_HOME} -jar ${UBERJAR}
