# PeerUnit dir
dirpeerunit=`pwd`

# Hadoop dir
hadoophome=/home/michel/hadoop-0.20.2/

# Remove hadoop dirs
rm -Rf /tmp/dfs*
rm -Rf /tmp/Jetty*

# HDFS format
#~/hadoop-0.21.0/bin/hdfs namenode -format
$hadoophome/bin/hadoop namenode -format

# Remove PI dir
rm -Rf $dirpeerunit/PiEstimator_TMP_3_141592654

# Execution
java -Xmx1000m -Dcom.sun.management.jmxremote -Dhadoop.log.dir=$dirpeerunit/logs -Dhadoop.log.file=hadoop.log -Dhadoop.home.dir=$dirpeerunit -Dhadoop.id.str=hadoop -Dhadoop.root.logger=INFO,DRFA -classpath /usr/lib/jvm/java-6-sun/lib/tools.jar:$hadoophome:./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.TestRunner test.TestStartCluster &
