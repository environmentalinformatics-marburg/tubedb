#!/bin/bash

response=$(curl --write-out %{http_code} --silent --output /dev/null --proxy '' --request POST http://localhost:8080/shutdown?token=stop)
if [ $response -eq 200 ] 
then 
	echo server stopped	
else
	echo server not running?
fi

exit 0

#curl --proxy '' --request POST http://localhost:8080/shutdown?token=stop

if curl --proxy '' --request POST http://localhost:8080/shutdown?token=stop; then
echo server stopped
else 
echo server not running: code $?
fi
