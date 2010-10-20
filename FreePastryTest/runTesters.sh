#!/bin/bash
if [ $# -lt 2 ]
then
	echo Usage: runTesters.sh test OAR_JOB_ID '[testers]' classpath
	echo Ex: runTesters.sh test.SimpleTest 280247 10 test.jar
else
	for (( i=1; i<= $2; i++));
	do
		export OAR_JOB_ID=$2
		java -ea -classpath $4 fr.inria.peerunit.TestRunner $1 &
	done
fi
