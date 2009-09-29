#!/bin/bash

#export OAR_JOB_ID=$2

export CLASSPATH=$CLASSPATH:/home/akoita/PeerFolderr/:/home/akoita/PeerFolderr/ivoire/target/PeerUnit-1.0-SNAPSHOT.jar:/home/akoita/PeerFolderr/ivoire/config/:/home/akoita/PeerFolderr/Lib/FreePastry-2.1alpha3.jar:/home/akoita/PeerFolderr/Lib/junit-4.4.jar:/home/akoita/PeerFolderr/Lib/openchord_1.0.3.jar:/home/akoita/PeerFolderr/Lib/xmlpull_1_1_3_4c_all.zip:/home/akoita/PeerFolderr/Lib/xpp3-1.1.3.4d_b2.jar:/home/akoita/PeerFolderr/Lib/traceroute-1.2.jar

#OAR_JOB_ID=$2  oarsh -i /tmp/oargrid//oargrid_ssh_key_akoita_20209  

java -Djava.rmi.server.hostname=$1 -Djava.security.policy=rmi.policy  fr.inria.peerunit.onstree.testerTree.RemoteTesterTreeBuilderImpl
