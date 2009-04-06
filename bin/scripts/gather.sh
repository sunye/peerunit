#!/bin/bash
DIR_SOURCE=/tmp/${USER}
PASSPARTOUT_HOME=${HOME}/workspace/Passpartout
HOST_FILE=${PASSPARTOUT_HOME}/scripts/hosts.txt
DIR_DEST=${HOME}/logpartout

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
	scp ${USER}@${ip}:${DIR_SOURCE}/*.log* ${DIR_DEST}
done

