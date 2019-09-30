#!/bin/bash

scp tmp/gpxtrack.jar pm2.json run.sh app:/app/gpxtrack/
ssh app pm2 restart gpxtrack

#.
