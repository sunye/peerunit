# Coordenador
#~/jre1.6.0_20/bin/java -classpath ../PeerUnit/target/PeerUnit-1.1-SNAPSHOT.jar fr.inria.peerunit.CoordinatorRunner &
java=/usr/lib/jvm/java-6-sun/bin/java
TESTE=fr.inria.peerunit.SimpleGlobalTestCase
TESTE=fr.inria.peerunit.TestPiEstimator

$java -classpath ./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.CoordinatorRunner &

$java -classpath ./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.TestRunner $TESTE &

$java -classpath ./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.TestRunner $TESTE &
#echo "Running remote command on $1"

#ssh $1 "cd ~/HadoopTest/ && ./run-local-tester.sh"
