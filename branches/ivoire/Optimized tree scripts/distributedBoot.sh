#!/bin/bash

export CLASSPATH=$CLASSPATH:/home/akoita/PeerFolderr/:/home/akoita/PeerFolderr/ivoire/target/PeerUnit-1.0-SNAPSHOT.jar:/home/akoita/PeerFolderr/ivoire/config/:/home/akoita/PeerFolderr/Lib/FreePastry-2.1alpha3.jar:/home/akoita/PeerFolderr/Lib/junit-4.4.jar:/home/akoita/PeerFolderr/Lib/openchord_1.0.3.jar:/home/akoita/PeerFolderr/Lib/xmlpull_1_1_3_4c_all.zip:/home/akoita/PeerFolderr/Lib/xpp3-1.1.3.4d_b2.jar:/home/akoita/PeerFolderr/Lib/traceroute-1.2.jar


java -Djava.security.policy=rmi.policy  fr.inria.peerunit.btree.BootstrapperImpl