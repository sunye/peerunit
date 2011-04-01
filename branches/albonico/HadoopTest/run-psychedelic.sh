rm -Rf ./target/mutants/*

# Execution
java=/usr/lib/jvm/java-6-sun/bin/java
$java -classpath ./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.psychedelic.base.App target/classes/examples/PiEstimator.class target/mutants/ &
