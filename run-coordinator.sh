# Coordenador
#~/jre1.6.0_20/bin/java -classpath ../PeerUnit/target/PeerUnit-1.1-SNAPSHOT.jar fr.inria.peerunit.CoordinatorRunner &
PEERUNIT_JAR=`cat peerunit.properties | grep tester.classpath | cut -b 18-200`

java=/usr/lib/jvm/java-6-sun/bin/java

#$java -classpath ~/.m2/repository/fr/inria/peerunit/PeerUnit/1.1-SNAPSHOT/PeerUnit-1.1-SNAPSHOT.jar fr.inria.peerunit.CoordinatorRunner 
echo $java -classpath $PEERUNIT_JAR fr.inria.peerunit.CoordinatorRunner 
$java -classpath $PEERUNIT_JAR fr.inria.peerunit.CoordinatorRunner 

