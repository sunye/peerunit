# Formata o HDFS
~/hadoop-0.20.2/bin/hadoop namenode -format

rm -Rf /home/ppginf/michela/GIT/albonico/HadoopTest/dir1data/
rm -Rf /home/ppginf/michela/GIT/albonico/HadoopTest/dir2data/
rm -Rf /home/ppginf/michela/GIT/albonico/HadoopTest/dir3data/

rm -Rf /home/ppginf/michela/GIT/albonico/HadoopTest/PiEstimator_TMP_3_141592654

# Teste
~/jdk1.6.0_21/bin/java -Xmx1000m -Dhadoop.log.dir=./logs/ -Dhadoop.log.file=hadoop -classpath ./lib/org/apache/hadoop/hadoop-core/0.20.2/hadoop-core-0.20.2.jar:./lib/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar:../PeerUnit/target/PeerUnit-1.1-SNAPSHOT.jar:./target/HadoopTest-1.0-SNAPSHOT.jar:./lib/org/mortbay/jetty/jetty/6.1.25/jetty-6.1.25.jar:./lib/org/mortbay/jetty/jetty-util/6.1.25/jetty-util-6.1.25.jar:./lib/org/mortbay/jetty/servlet-api/2.5-20081211/servlet-api-2.5-20081211.jar:./lib/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar:./lib/org/apache/hadoop/hadoop-examples/0.20.2/hadoop-examples-0.20.2.jar:./lib/org/mortbay/jetty/jsp-2.1/6.1.14/jsp-2.1-6.1.14.jar:./lib/javax/servlet/jsp/jsp-api/2.1/jsp-api-2.1.jar:./lib/org/apache/ant/ant/1.7.1/ant-1.7.1.jar:./lib/ant/ant-launcher/1.6.5/ant-launcher-1.6.5.jar:./lib/jasperreports/jasperreports/3.1.2/jasperreports-3.1.2.jar fr.inria.peerunit.TestRunner test.TestStartCluster &


#sleep 20000

#~/hadoop-0.20.2/bin/hadoop tasktracker &
