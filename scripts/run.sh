#!/bin/bash
DIR_SOURCE=/tmp/${USER}
PASSPARTOUT_HOME=${HOME}/workspace/Passpartout
HOST_FILE=${PASSPARTOUT_HOME}/scripts/hosts.txt
DIR_DEST=${HOME}/logpartout
USER2=almeida-e
IP_ALT=172.16.9.22

# Reading hosts 
qtd_hosts=`cat ${HOST_FILE}|wc -l`
i=1
while [ ${i} -le ${qtd_hosts} ]
do
        address[$i]=`head -n $i ${HOST_FILE}| tail -n 1 |awk '{print $1}'`
	i=$((${i} + 1))
done

# Copying files


for ip in "${address[@]}"; do
	if [ ${ip} == ${IP_ALT} ] ; then
	        scp ${PASSPARTOUT_HOME}/scripts/exec.sh ${USER2}@${ip}:/tmp/${USER}
        	scp ${PASSPARTOUT_HOME}/scripts/kill.sh ${USER2}@${ip}:/tmp/${USER}
        	ssh ${USER2}@${ip} chmod 755 /tmp/${USER2}/exec.sh
        	ssh ${USER2}@${ip} chmod 755 /tmp/${USER2}/kill.sh
	else
        	scp ${PASSPARTOUT_HOME}/scripts/exec.sh ${USER}@${ip}:${DIR_SOURCE}
        	scp ${PASSPARTOUT_HOME}/scripts/kill.sh ${USER}@${ip}:${DIR_SOURCE}
        	ssh ${USER}@${ip} chmod 755 /tmp/${USER}/exec.sh
 	       	ssh ${USER}@${ip} chmod 755 /tmp/${USER}/kill.sh
	fi
done

#for ip in "${address[@]}"; do
	#nohup ssh ${USER}@${ip} . ${DIR_SOURCE}/exec.sh &
#done
