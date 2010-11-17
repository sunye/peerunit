# Coordenador
java -classpath .:../PeerUnit/target/PeerUnit-1.1-SNAPSHOT.jar fr.inria.peerunit.CoordinatorRunner &
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
/usr/lib/jvm/java-6-sun/bin/java -classpath ../PeerUnit/target/PeerUnit-1.1-SNAPSHOT.jar:./target/HadoopTest-1.0-SNAPSHOT.jar:~/.m2/repository/org/apache/hadoop/hadoop-core/0.20.2/hadoop-core-0.20.2.jar fr.inria.peerunit.TestRunner load.TestStartCluster
