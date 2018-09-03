#!/bin/bash
#
# This script updates timeseries data within TubeDB.
# Following sub-tasks are run:
# - stop a running TubeDB server.
# - create a backup-copy of TubeDB.
# - remove all timeseries data in TubeDB and import all timeseries data from data-source files.
# - start TubeDB server.
#
# For this update process it is assumed that the data-source files are updated before this skript is executed.
# e.g. there might exist another skript that updates data-source files that is run as cron-job at 03:00 each day 
# and this skrip may run as cron-job at 04:00 each day.
#
# (This script is called by cronjob.sh)


# shutdown web-server
echo "###				*** shutdown web-server *** $(date +%Y-%m-%dT%H:%M:%S)   ###"
./shutdown_server.sh
# TODO check if error 


# check server stopped
# TODO
#./check_server.sh


# create backup copy
echo "###				*** backup *** $(date +%Y-%m-%dT%H:%M:%S)   ###"
./backup_cron.sh
# TODO check if error


# clear and import database
echo "###				*** clear database and import timeseries files *** $(date +%Y-%m-%dT%H:%M:%S)   ###"
./tsdb.sh clear_import
# TODO check if error


# start-web-server
echo "###				*** start web-server *** $(date +%Y-%m-%dT%H:%M:%S)   ###"
./start_server_background.sh
# TODO check if error


# check server running
#./check_server.sh

echo "###				*** finished *** $(date +%Y-%m-%dT%H:%M:%S)   ###"
