#!/bin/bash
source ${HOME}/.bashrc
set -m

# if [ $# -lt 3 ]
# then
#     echo Usage: $0 \<test\> \<ntesters\> \<coord\> 
#     echo Ex: $0 test.SimpleTest 16 0 
# else

echo "Cleaning old logs" 
rm *.log* 

# Command-line arguments
#TEST=$1 
NTESTERS=$2 # 16 # Testers per node
#COORD=$3 # (0) centralized, (1) btree

# Update configuration file
PROPERTIES="peerunit.properties"
NTESTERS=`sed '/^\#/d'  peerunit.properties | grep 'tester.pernode' | tail -n 1 | sed 's/^.*=//'`
((PEERS= $(uniq /var/lib/oar/${OAR_JOB_ID} | sed 1d | wc -l) * ${NTESTERS} ))
sed -i s/tester.peers=[[:alnum:]]*/tester.peers=${PEERS}/g ${PROPERTIES}
SERVER=`hostname -f`
sed -i s/tester.server=[[:alnum:]].*/tester.server=${SERVER}/g ${PROPERTIES}
#sed -i s/test.coordination=[[:alnum:]].*/test.coordination=${COORD}/g ${PROPERTIES}
CLASSPATH=`sed '/^\#/d'  peerunit.properties | grep 'tester.classpath' | tail -n 1 | sed 's/^.*=//'`
TEST=`sed '/^\#/d'  peerunit.properties | grep 'tester.testcase' | tail -n 1 | sed 's/^.*=//'`


echo "Server: $SERVER  peers: $PEERS nodes: " $(( $PEERS/$NTESTERS )) 
echo "Classpath: ${CLASSPATH}"

# Execute the Coordinator on the first node
java -classpath ${CLASSPATH} fr.inria.peerunit.CoordinatorRunner peerunit.properties &
#./runTesters.sh ${TEST} 1 &

# for each node dismissing the first
for i in $(uniq /var/lib/oar/${OAR_JOB_ID} | sed 1d) ;
do 
    echo oarsh $i "cd ${PWD} \; ./runTesters.sh ${TEST}  ${NTESTERS} ${CLASSPATH}" 
    oarsh $i "cd ${PWD} ; ./runTesters.sh ${TEST} ${NTESTERS} ${CLASSPATH}" & 
done

# %1 - pois foi o primeiro processo a ser executado em background
fg %1

LOCAL=`echo $SERVER | awk -F. '{print $2}'`
#tar czvf logs/${LOCAL}_${PEERS}peers_${COORD}${BTREE}.tgz *.log OAR.${OAR_JOB_ID}*
DIR=logs/${TESTCASE}_${LOCAL}_${PEERS}peers${NTESTERS}th_${COORD}${BTREE}-`date +%Y%m%d%H%M` 
mkdir -p $DIR
mv *.log OAR.${OAR_JOB_ID}* $DIR/ 

#fi
