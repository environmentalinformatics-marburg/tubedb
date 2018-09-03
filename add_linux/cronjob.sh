#!/bin/bash
#
# This script starts "cronjob_internal.sh" with log-file output.
# See details at that skript.
#
# (This script is called by cronjob_wrapper.sh)
#
# This script depents on following scripts:
# - cronjob_internal.sh
# - shutdown_server.sh
# - backup_cron.sh
# - tsdb.sh
# - start_server_background.sh

#root=$(dirname $0)
#echo working directory: $root

log=log_cron

mkdir --parents $log

./cronjob_internal.sh 2>&1 | tee $log/$(date +%Y_%m_%d___%H_%M__%S).log

