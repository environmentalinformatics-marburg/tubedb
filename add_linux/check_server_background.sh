#!/bin/bash

screen -ls
#screen -ls | grep .tubedb
#netstat -ntulp | grep :8080 | grep /java

SERVER=localhost
PORT=8080
URL_BASE=""
TEST_PATH="/content"
MAX_WAIT=60

for i in {1..60}
do
   
   
   
if (: < /dev/tcp/$SERVER/$PORT) 2>/dev/null
then
    #echo --- OK --- some server is listening at port $PORT
	lineCount=$(wget --no-proxy --server-response localhost:8080$URL_BASE 2>&1 | grep ":8080$URL_BASE$TEST_PATH" | wc -l)
	#echo $lineCount
	if [ $lineCount -ne 0 ]
	then
		echo
		echo --- OK --- tubedb server is ready at [IP]:$PORT$URL_BASE
		echo
		exit 0
	else
		echo
		echo --- ERROR --- some server is listening at port $PORT but maybe NOT tubedb server
		echo
		exit 2
	fi
else
	#echo
    #echo --- ERROR --- server is NOT listening at port $PORT
	#echo
	#exit 1
	echo waiting $i of $MAX_WAIT seconds...
fi 
   
sleep 1

done

echo
echo --- ERROR --- server is NOT listening at port $PORT
echo
exit 1



