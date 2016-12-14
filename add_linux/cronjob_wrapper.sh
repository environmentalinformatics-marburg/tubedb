#!/bin/bash
#
# Helper script for cronjob.sh to execute in correct working directory if called from outside of working directory (e.g. as cronjob).
#
# use this script as linux cron job entry

# change following absolute path to correct TubeDB working directory
cd /path_to_tubedb

./cronjob.sh
