#!/bin/bash

TARGET=target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar
MUTCLASSPATH=target/classes/fr/inria/peerunit/PiEstimator.class
MUTCLASS=PiEstimator.class
CODECLASS=src/main/java/fr/inria/peerunit/PiEstimator.java
MUTPATH=target/mutants

mkdir $MUTPATH

echo ./run-psychedelic.sh $MUTCLASSPATH $MUTPATH
./run-psychedelic.sh $MUTCLASSPATH $MUTPATH

mkdir bkp
cp $TARGET bkp

for MUT in `ls $MUTPATH`;
do
	echo "Applying mutation number $MUT!"

	echo . apply-mutation-classes.sh $MUTCLASSPATH $MUTPATH/$MUT/$MUTCLASS $CODECLASS $MUTCLASS
	. apply-mutation-classes.sh $MUTCLASSPATH $MUTPATH/$MUT/$MUTCLASS $CODECLASS $MUTCLASS

	echo mv target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar $MUTPATH/$MUT/
	mv $TARGET $MUTPATH/$MUT/
done
