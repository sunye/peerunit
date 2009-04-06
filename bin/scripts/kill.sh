#!/bin/bash
##############
# Script to kill Passpartout
#
# Author : Eduardo Almeida
# Date : 8-mai-2007
#############
kill_pid=${1}
full_pid_file=pidfile.txt

ps aux |grep java  > ${full_pid_file}
ps aux |grep rmi  >> ${full_pid_file}


qtd=`cat ${full_pid_file} | wc -l`
j=1
while [ $j -le $qtd ]
do
        awk '{print $2}' ${full_pid_file} | head -n $j | tail -n 1 > teste
        ini=`cat teste`
	if [ $ini -ne $kill_pid ] ; then
                kill $ini
        fi
        j=`expr $j + 1`
done
rm /tmp/almeida/teste*
rm /tmp/almeida/Test*.log*
rm /tmp/almeida/peerunit.log*
rm -rf storage*/
rm /tmp/almeida/freepastry.test.TestUpdateOnShrink.peer*
