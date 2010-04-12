#!/bin/bash
source ${HOME}/.bashrc
set -m

if [ $# -lt 3 ]
then
    echo Usage: $0 \<test\> \<ntesters\> \<coord\> 
    echo Ex: $0 test.SimpleTest 16 0 
else

echo "Cleaning old logs" 
rm *.log* 

# Command-line arguments
TEST=$1 
NTESTERS=$2 # 16 # Testers per node
COORD=$3 # (0) centralized, (1) btree

# Update configuration file
PROPERTIES="peerunit.properties"
((PEERS= $(uniq /var/lib/oar/${OAR_JOB_ID} | sed 1d | wc -l) * ${NTESTERS} ))
sed -i s/tester.peers=[[:alnum:]]*/tester.peers=${PEERS}/g ${PROPERTIES}
SERVER=`hostname -f`
sed -i s/tester.server=[[:alnum:]].*/tester.server=${SERVER}/g ${PROPERTIES}
sed -i s/test.coordination=[[:alnum:]].*/test.coordination=${COORD}/g ${PROPERTIES}

echo "Server: $SERVER  peers: $PEERS nodes: " $(( $PEERS/$NTESTERS )) 

# Execute the Coordinator on the first node
java -classpath ./target/Benchmark-1.0.jar fr.inria.peerunit.CoordinatorRunner peerunit.properties &

# for each node dismissing the first
for i in $(uniq /var/lib/oar/${OAR_JOB_ID} | sed 1d) ;
do 
    echo oarsh $i "cd ${PWD} \; ./runTesters.sh ${TEST}  ${NTESTERS}" 
    oarsh $i "cd ${PWD} ; ./runTesters.sh ${TEST} ${NTESTERS}" & 
done

# %1 - pois foi o primeiro processo a ser executado em background
fg %1

LOCAL=`echo $SERVER | awk -F. '{print $2}'`
#tar czvf logs/${LOCAL}_${PEERS}peers_${COORD}${BTREE}.tgz *.log OAR.${OAR_JOB_ID}*
DIR=logs/${LOCAL}_${PEERS}peers${NTESTERS}th_${COORD}${BTREE}-`date +%Y%m%d%H%M` 
mkdir -p $DIR
mv *.log OAR.${OAR_JOB_ID}* $DIR/ 

fi
