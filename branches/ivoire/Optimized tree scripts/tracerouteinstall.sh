#!/bin/bash
cd ${PEERFOLDER}
mvn install:install-file -Dfile=/home/booba/workspace/Traceroute/traceroute.jar \
  -DpomFile=./pom.xml \
  -DgroupId=fr.inria.peerunit \
  -DartifactId=traceroute \
  -Dversion=1.0 \
  -Dpackaging=jar \
