# PeerUnit dir
dirpeerunit=`pwd`

# Hadoop dir
HADOOP_HOME=`cat hadoop.properties | grep hadoop.dir.install | cut -b 20-200`

# Remove hadoop dirs
dirdata=`cat hadoop.properties | grep hadoop.dir.data | cut -b 17-200`
dirname=`cat hadoop.properties | grep hadoop.dir.name | cut -b 17-200`
rm -Rf $dirdata
rm -Rf $dirname

# Create hadoop conf files
./hadoop-create-conf.sh $HADOOP_HOME/ $dirdata $dirname

# HDFS format
#~/hadoop-0.21.0/bin/hdfs namenode -format
$HADOOP_HOME//bin/hadoop namenode -format

# Remove PI dir
rm -Rf $dirpeerunit/PiEstimator_TMP_3_141592654

# Execution
java=/usr/lib/jvm/java-6-sun/bin/java
$java -Xmx1000m -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote -Dhadoop.log.dir=$HADOOP_HOME/bin/../logs -Dhadoop.log.file=hadoop-michel-jobtracker-note.log -Dhadoop.home.dir=$HADOOP_HOME/bin/.. -Dhadoop.id.str=michel -Dhadoop.root.logger=INFO,DRFA -Djava.library.path=$HADOOP_HOME/bin/../lib/native/Linux-i386-32 -Dhadoop.policy.file=hadoop-policy.xml -classpath $HADOOP_HOME/bin/../conf:/usr/lib/jvm/java-6-sun/lib/tools.jar:$HADOOP_HOME/bin/..:$HADOOP_HOME/bin/../hadoop-0.20.2-core.jar:$HADOOP_HOME/bin/../lib/commons-cli-1.2.jar:$HADOOP_HOME/bin/../lib/commons-codec-1.3.jar:$HADOOP_HOME/bin/../lib/commons-el-1.0.jar:$HADOOP_HOME/bin/../lib/commons-httpclient-3.0.1.jar:$HADOOP_HOME/bin/../lib/commons-logging-1.0.4.jar:$HADOOP_HOME/bin/../lib/commons-logging-api-1.0.4.jar:$HADOOP_HOME/bin/../lib/commons-net-1.4.1.jar:$HADOOP_HOME/bin/../lib/core-3.1.1.jar:$HADOOP_HOME/bin/../lib/hsqldb-1.8.0.10.jar:$HADOOP_HOME/bin/../lib/jasper-compiler-5.5.12.jar:$HADOOP_HOME/bin/../lib/jasper-runtime-5.5.12.jar:$HADOOP_HOME/bin/../lib/jets3t-0.6.1.jar:$HADOOP_HOME/bin/../lib/jetty-6.1.14.jar:$HADOOP_HOME/bin/../lib/jetty-util-6.1.14.jar:$HADOOP_HOME/bin/../lib/junit-3.8.1.jar:$HADOOP_HOME/bin/../lib/kfs-0.2.2.jar:$HADOOP_HOME/bin/../lib/log4j-1.2.15.jar:$HADOOP_HOME/bin/../lib/mockito-all-1.8.0.jar:$HADOOP_HOME/bin/../lib/oro-2.0.8.jar:$HADOOP_HOME/bin/../lib/servlet-api-2.5-6.1.14.jar:$HADOOP_HOME/bin/../lib/slf4j-api-1.4.3.jar:$HADOOP_HOME/bin/../lib/slf4j-log4j12-1.4.3.jar:$HADOOP_HOME/bin/../lib/xmlenc-0.52.jar:$HADOOP_HOME/bin/../lib/jsp-2.1/jsp-2.1.jar:$HADOOP_HOME/bin/../lib/jsp-2.1/jsp-api-2.1.jar:./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.TestRunner fr.inria.peerunit.TestPiEstimator &

#java -Xmx1000m -Dcom.sun.management.jmxremote -Dhadoop.log.dir=$dirpeerunit/logs -Dhadoop.log.file=hadoop.log -Dhadoop.home.dir=$dirpeerunit -Dhadoop.id.str=hadoop -Dhadoop.root.logger=INFO,DRFA -classpath /usr/lib/jvm/java-6-sun/lib/tools.jar:$hadoophome:./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.TestRunner test.TestStartCluster &