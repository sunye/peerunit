#!/bin/bash
if [ $# -lt 1 ]
then
	echo Usage: runTesters.sh test  '[testers]'
	echo Ex: runTesters.sh test.SimpleTest  10
else
	for (( i=1; i<= $2; i++));
	do
		java -classpath ./target/Benchmark-1.0.jar fr.inria.peerunit.TestRunner $1 &
	done
fi
