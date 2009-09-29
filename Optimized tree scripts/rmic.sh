#!/bin/bash
cd ${PEERFOLDER}/target/classes
rmic fr.inria.peerunit.rmi.coord.CoordinatorImpl
rmic fr.inria.peerunit.rmi.tester.TesterImpl
rmic fr.inria.peerunit.btree.BootstrapperImpl
rmic fr.inria.peerunit.btree.NodeImpl

cd ${PEERFOLDER}
mvn jar:jar


