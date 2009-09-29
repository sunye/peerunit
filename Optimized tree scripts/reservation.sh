#!/bin/bash
if [ $# -ne 4 ]
then
	echo Usage: reservation.sh date hour  nodes walltime
	echo Ex: reservation.sh 2009-01-13 10:35:00 3 0:15:00
else 
	oarsub --reservation="$1 $2" -l nodes=$3,walltime=$4
fi
