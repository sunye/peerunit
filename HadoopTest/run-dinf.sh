# Coordenador
~/jre1.6.0_20/bin/java -classpath ../PeerUnit/target/PeerUnit-1.1-SNAPSHOT.jar fr.inria.peerunit.CoordinatorRunner &
# Bootstap OpenChord
#java -classpath .:target/PeerUnit-1.0-SNAPSHOT.jar:~/openchord/dist/openchord_1.0.5.jar openchord.Bootstrap &

# Testers
#java -classpath .:target/PeerUnit-1.0-SNAPSHOT.jar fr.inria.peerunit.TestRunner test.SimpleTest &
#java -classpath .:target/PeerUnit-1.0-SNAPSHOT.jar fr.inria.peerunit.TestRunner test.SimpleTest &
#java -classpath .:target/PeerUnit-1.0-SNAPSHOT.jar fr.inria.peerunit.TestRunner test.SimpleTest &
#java -classpath .:target/PeerUnit-1.0-SNAPSHOT.jar fr.inria.peerunit.TestRunner test.SimpleTest &
#java -classpath .:target/PeerUnit-1.0-SNAPSHOT.jar fr.inria.peerunit.TestRunner test.SimpleTest &
#~/hadoop-0.20.2/bin/hadoop jobtracker

# Teste
~/jdk1.6.0_21/bin/java -Dhadoop.log.dir=./log/ -Dhadoop.log.file=hadoop -classpath ~/.m2/repository/org/apache/hadoop/hadoop-core/0.20.2/hadoop-core-0.20.2.jar:./lib/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar:../PeerUnit/target/PeerUnit-1.1-SNAPSHOT.jar:./target/HadoopTest-1.0-SNAPSHOT.jar:./lib/org/mortbay/jetty/jetty/6.1.25/jetty-6.1.25.jar:./lib/org/mortbay/jetty/jetty-util/6.1.25/jetty-util-6.1.25.jar:./lib/org/mortbay/jetty/servlet-api/2.5-20081211/servlet-api-2.5-20081211.jar:./lib/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar fr.inria.peerunit.TestRunner load.TestStartCluster &

~/jdk1.6.0_21/bin/java -Dhadoop.log.dir=./log/ -Dhadoop.log.file=hadoop -classpath ~/.m2/repository/org/apache/hadoop/hadoop-core/0.20.2/hadoop-core-0.20.2.jar:./lib/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar:../PeerUnit/target/PeerUnit-1.1-SNAPSHOT.jar:./target/HadoopTest-1.0-SNAPSHOT.jar:./lib/org/mortbay/jetty/jetty/6.1.25/jetty-6.1.25.jar:./lib/org/mortbay/jetty/jetty-util/6.1.25/jetty-util-6.1.25.jar:./lib/org/mortbay/jetty/servlet-api/2.5-20081211/servlet-api-2.5-20081211.jar:./lib/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar fr.inria.peerunit.TestRunner load.TestStartCluster &
