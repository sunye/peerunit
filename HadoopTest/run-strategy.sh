# Coordenador
#~/jre1.6.0_20/bin/java -classpath ../PeerUnit/target/PeerUnit-1.1-SNAPSHOT.jar fr.inria.peerunit.CoordinatorRunner &
java=/usr/lib/jvm/java-6-sun/bin/java

$java -classpath ./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.CoordinatorRunner &

$java -classpath ./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.TestRunner fr.inria.peerunit.TestSimple &

$java -classpath ./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.peerunit.TestRunner fr.inria.peerunit.TestSimple &
#echo "Running remote command on $1"

#ssh $1 "cd ~/HadoopTest/ && ./run-local-tester.sh"
