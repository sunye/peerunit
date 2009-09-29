#!/bin/bash

HOST_FILE=/home/akoita/PeerFolderr/ivoire/config/allhosts.txt

#if [ $# -ne 2 ] 
#then 
#	echo Usage: runAllTesters.sh test  OAR_JOB_ID
#	echo Ex: runAllTesters.sh test.SimpleTest 280147
#else 

	i=0
	while read line;
	do
        	address[$i]=$line
		i=$((${i} + 1))
	done< ${HOST_FILE}
#	i=`expr $i - 1`
	cpt=0
        
        while [ $cpt -lt $i ]
        do
#          export OAR_JOB_ID=$2
           ssh akoita@${address[${cpt}]} /home/akoita/PeerFolderr/scripts/runTesters.sh $1 ${address[${cpt}]}  &
          cpt=`expr $cpt + 1`
	done
#fi      
