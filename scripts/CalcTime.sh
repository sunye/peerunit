#!/bin/bash
##############
# Script to measure the difference between machine times  
#       I have considered the following formula to calculate the difference: 
#	client_time - (( server_ini_time + server_end_time) / 2)
#
# Author : Eduardo Almeida
# Date : 16-mar-2006
# Update : 22-jun-2006
#############
PASSPARTOUT_HOME=${HOME}/workspace/Passpartout
HOST_FILE=${PASSPARTOUT_HOME}/scripts/hosts.txt
TIMER=${HOME}/workspace/Logpartout/bin/LogTimer.class
DATABASE=passpartout
DBHOST=172.16.9.221

echo "Header <IP Address> <Remote Time> <Remote Time in sec> <Server Time in sec> <Difference>"
#############
# Read the file with the machine list
#############

qtd_hosts=`cat ${HOST_FILE}|wc -l`
i=1
while [ ${i} -le ${qtd_hosts} ]
do
	##address[${i}]=`head -n $i ${HOST_FILE}| tail -n 1 |awk '{print $1}'`
	address[$i]=`head -n $i ${HOST_FILE}| tail -n 1 |awk '{print $1}'`
	echo `head -n $i ${HOST_FILE}| tail -n 1 |awk '{print $1}'`
	i=$((${i} + 1))
done
echo "VAI"
#############
# Handling zeros within the time variables  
#############

function clean_zero
{
        btime=`echo $1|cut -c1`
        if [ "$btime" = "0" ] ; then
                btime=`echo $1|cut -c2`
	else
		btime=$1
        fi
	return $btime
}

#############
# Calculating the time in seconds
#############

function base_time 
{
	data=$1
        hour=$(echo "$data"| cut -d: -f1)
        min=$(echo "$data"| cut -d: -f2)
	sec=$(echo "$data"| cut -d: -f3 | cut -d. -f1)
        clean_zero $hour 
	hour=$((${btime}*3600))
	clean_zero $min
        min=$((${btime}*60))
	clean_zero $sec
	sec=${btime}
       	btime=$((hour+min+sec))
	return $btime
}

#############
# Correcting the server time with the begin time and the end time
#############

function correct_time
{
        ctime=$((($1+$2)/2))
        return $ctime
}

#############
# Calculating the time difference 
#############

function time_diff
{
	correct_time $1 $2
	difftime=$(($3-$ctime))
	return $difftime
}

#############
# The core
#############

psql -d ${DATABASE} -h ${DBHOST} -c "drop table appatb02peertime"
psql -d ${DATABASE} -h ${DBHOST} -c "create table appatb02peertime(begintime timestamp,endtime timestamp,correcttime timestamp, peertime timestamp, peerid varchar(15), peername varchar(15), difference time, signal char(1))"
echo "OLAOA"
IP_ALT=172.16.9.22
USER2=almeida-e


for ip in "${address[@]}"; do
	begindata=`date +%H:%M:%S`
	base_time $begindata
	idata=$btime
	if [ ${ip} == ${IP_ALT} ] ; then
		USER2=almeida-e
	else
		USER2=almeida
	fi
	platform=`ssh ${USER2}@${ip} uname -s`
	if [ "$platform" = "Linux" ] ; then 
		dataremote=`ssh ${USER2}@${ip} date +%Y-%m-%d#%H:%M:%S.%N`
	else
		scp ${TIMER} ${USER2}@${ip}:/tmp
		ssh ${USER2}@${ip} java -cp /tmp/ LogTimer
		dataremote=`ssh ${USER2}@${ip} cat timer.txt`
	fi
 	serverday=$(echo "$dataremote"| cut -d# -f1)	
 	dataremote=$(echo "$dataremote"| cut -d# -f2)	
	base_time $dataremote $platform
	dataSec=$btime

	enddata=`date +%H:%M:%S`
        base_time $enddata
        edata=$btime
	time_diff $idata $edata $dataSec 
	
 	echo ${serverday} ${dataremote} "|" ${ip} "|"   ${dataSec} "|"  ${ctime}  "|" ${difftime}	
	psql -d ${DATABASE} -h ${DBHOST} -c "insert into appatb02peertime(begintime, endtime,peertime , peerid) values('${serverday} ${begindata}','${serverday} ${enddata}','${serverday} ${dataremote}','${ip}')"
done
exit 0
