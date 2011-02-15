dirpeerunit=`pwd`

# Hadoop dir
hadoophome=/home/michel/hadoop-0.20.2/

# Execution
java -Xmx1000m -Dcom.sun.management.jmxremote -Dhadoop.log.dir=$dirpeerunit/logs -Dhadoop.log.file=hadoop.log -Dhadoop.home.dir=$dirpeerunit -Dhadoop.id.str=hadoop -Dhadoop.root.logger=INFO,DRFA -classpath /usr/lib/jvm/java-6-sun/lib/tools.jar:$hadoophome:./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.TestRunner test.TestStartCluster &

#java -Xmx1000m -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote -Dhadoop.log.dir=/home/michel/hadoop-0.20.2/bin/../logs -Dhadoop.log.file=hadoop-jobtracker.log -Dhadoop.home.dir=$hadoophome -Dhadoop.id.str=michel -Dhadoop.root.logger=INFO,DRFA -Djava.library.path=$hadoophome/lib/native/Linux-i386-32 -Dhadoop.policy.file=hadoop-policy.xml -classpath $hadoophome/conf:/usr/lib/jvm/java-6-sun/lib/tools.jar:$hadoophome:$hadoophome/hadoop-0.20.2-core.jar:$hadoophome/lib/commons-cli-1.2.jar:$hadoophome/lib/commons-codec-1.3.jar:$hadoophome/lib/commons-el-1.0.jar:$hadoophome/lib/commons-httpclient-3.0.1.jar:$hadoophome/lib/commons-logging-1.0.4.jar:$hadoophome/lib/commons-logging-api-1.0.4.jar:$hadoophome/lib/commons-net-1.4.1.jar:$hadoophome/lib/core-3.1.1.jar:$hadoophome/lib/hsqldb-1.8.0.10.jar:$hadoophome/lib/jasper-compiler-5.5.12.jar:$hadoophome/lib/jasper-runtime-5.5.12.jar:$hadoophome/lib/jets3t-0.6.1.jar:$hadoophome/lib/jetty-6.1.14.jar:$hadoophome/lib/jetty-util-6.1.14.jar:$hadoophome/lib/junit-3.8.1.jar:$hadoophome/lib/kfs-0.2.2.jar:$hadoophome/lib/log4j-1.2.15.jar:$hadoophome/lib/mockito-all-1.8.0.jar:$hadoophome/lib/oro-2.0.8.jar:$hadoophome/lib/servlet-api-2.5-6.1.14.jar:$hadoophome/lib/slf4j-api-1.4.3.jar:$hadoophome/lib/slf4j-log4j12-1.4.3.jar:$hadoophome/lib/xmlenc-0.52.jar:$hadoophome/lib/jsp-2.1/jsp-2.1.jar:$hadoophome/lib/jsp-2.1/jsp-api-2.1.jar:./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.TestRunner test.TestStartCluster &

#org.apache.hadoop.mapred.JobTracker &
#java -Xmx1000m -Dcom.sun.management.jmxremote -Dhadoop.root.logger=INFO -Dhadoop.log.dir=./log/ -Dhadoop.log.file=hadoop-jobtracker.log -Djava.library.path=/home/michel/hadoop-0.20.2/bin/../lib/native/Linux-i386-32 -classpath ./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar:./hadoop-conf.jar org.apache.hadoop.mapred.JobTracker &
