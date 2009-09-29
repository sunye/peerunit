#!/bin/bash
#if [ $# -lt 4 ]
#then
#	echo Usage: runTesters.sh test @RMI OAR_JOB_ID
#	echo Ex: runTesters.sh test.SimpleTest 172.16.8.121 280247
#else
#	export OAR_JOB_ID=$3

	export CLASSPATH=$CLASSPATH:/home/akoita/PeerFolderr/:/home/akoita/PeerFolderr/ivoire/target/PeerUnit-1.0-SNAPSHOT.jar:/home/akoita/PeerFolderr/ivoire/config/:/home/akoita/PeerFolderr/Lib/FreePastry-2.1alpha3.jar:/home/akoita/PeerFolderr/Lib/junit-4.4.jar:/home/akoita/PeerFolderr/Lib/openchord_1.0.3.jar:/home/akoita/PeerFolderr/Lib/xmlpull_1_1_3_4c_all.zip:/home/akoita/PeerFolderr/Lib/xpp3-1.1.3.4d_b2.jar:/home/akoita/PeerFolderr/Lib/traceroute-1.2.jar

	echo $2 
        java    -Djava.rmi.server.hostname=$2  fr.inria.peerunit.TestRunner $1 &
#fi
