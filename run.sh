#!/bin/bash

cd $(dirname "$0")
export CONFIG_EDN="../conf/angara.edn"
exec java -jar gpxtrack.jar

#.
