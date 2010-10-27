# Formata o HDFS
#~/hadoop-0.20.2/bin/hadoop namenode -format

#rm -Rf /home/ppginf/michela/GIT/albonico/HadoopTest/dfs/cohibadata/
#rm -Rf /home/ppginf/michela/GIT/albonico/HadoopTest/dfs/macalandata/
#rm -Rf /home/ppginf/michela/GIT/albonico/HadoopTest/dfs/dalmoredata/

# Teste
~/jdk1.6.0_21/bin/java -Dhadoop.log.dir=./log/ -Dhadoop.log.file=hadoop -classpath ./lib/org/apache/hadoop/hadoop-core/0.20.2/hadoop-core-0.20.2.jar:./lib/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar:../PeerUnit/target/PeerUnit-1.1-SNAPSHOT.jar:./target/HadoopTest-1.0-SNAPSHOT.jar:./lib/org/mortbay/jetty/jetty/6.1.25/jetty-6.1.25.jar:./lib/org/mortbay/jetty/jetty-util/6.1.25/jetty-util-6.1.25.jar:./lib/org/mortbay/jetty/servlet-api/2.5-20081211/servlet-api-2.5-20081211.jar:./lib/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar:./lib/org/apache/hadoop/hadoop-examples/0.20.2/hadoop-examples-0.20.2.jar fr.inria.peerunit.TestRunner load.TestStartCluster &
