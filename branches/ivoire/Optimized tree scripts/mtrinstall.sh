#!/bin/bash
cd /home/booba/workspace/PeerUnit/
mvn install:install-file -Dfile=/home/booba/workspace/BuildTreeOnStation3WithMTR/Lib/mtr/mtr.jar \
  -DpomFile=./pom.xml \
  -DgroupId=fr.inria.peerunit \
  -DartifactId=mtrLib \
  -Dversion=1.0 \
  -Dpackaging=jar \
