/home/ppginf/michela/jdk1.6.0_21/bin/java -Xmx1000m -Dcom.sun.management.jmxremote -Dhadoop.log.dir=/home/ppginf/michela/hadoop-0.20.2/bin/../logs -Dhadoop.log.file=hadoop.log -Dhadoop.home.dir=/home/ppginf/michela/hadoop-0.20.2/bin/.. -Dhadoop.id.str= -Dhadoop.root.logger=INFO,console -Djava.library.path=/home/ppginf/michela/hadoop-0.20.2/bin/../lib/native/Linux-i386-32 -Dhadoop.policy.file=hadoop-policy.xml -classpath /home/ppginf/michela/hadoop-0.20.2/bin/../conf:/home/ppginf/michela/jdk1.6.0_21/lib/tools.jar:/home/ppginf/michela/hadoop-0.20.2/bin/..:/home/ppginf/michela/hadoop-0.20.2/bin/../hadoop-0.20.2-core.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/commons-cli-1.2.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/commons-codec-1.3.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/commons-el-1.0.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/commons-httpclient-3.0.1.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/commons-logging-1.0.4.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/commons-logging-api-1.0.4.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/commons-net-1.4.1.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/core-3.1.1.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/hsqldb-1.8.0.10.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/jasper-compiler-5.5.12.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/jasper-runtime-5.5.12.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/jets3t-0.6.1.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/jetty-6.1.14.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/jetty-util-6.1.14.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/junit-3.8.1.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/kfs-0.2.2.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/log4j-1.2.15.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/mockito-all-1.8.0.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/oro-2.0.8.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/servlet-api-2.5-6.1.14.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/slf4j-api-1.4.3.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/slf4j-log4j12-1.4.3.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/xmlenc-0.52.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/jsp-2.1/jsp-2.1.jar:/home/ppginf/michela/hadoop-0.20.2/bin/../lib/jsp-2.1/jsp-api-2.1.jar:.//target/HadoopTest-1.0-SNAPSHOT.jar test.JobTracker &