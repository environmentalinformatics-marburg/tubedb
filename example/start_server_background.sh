#!/bin/bash

sessions=$(screen -ls | grep tubedb | wc -l)

if [ $sessions -ne 0 ]
then
	echo
	echo "---WARNING--- screen session with tubedb already running: exiting old and restarting new..."
	echo
	screen -X -S tubedb quit
	#exit 1
fi

screen -S tubedb -d -m ./tsdb.sh server

./check_server_background.sh
