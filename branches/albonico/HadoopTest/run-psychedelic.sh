notmutclass=$1
mutpath=$2

if [ $2 ]
then

	rm -Rf ./target/mutants/*

	# Execution
	java=/usr/lib/jvm/java-6-sun/bin/java
	$java -classpath ./target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar fr.inria.psychedelic.base.App $1 $2 &

else

	echo "Please use the follow command: ./run-psychedelic.sh /path/to/not/mutated/class /path/to/mutants/"

fi
