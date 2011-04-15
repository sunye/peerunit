nmutclasspath=target/classes/fr/inria/peerunit/PiEstimator.class
nmutclass=PiEstimator.class
mutpath=target/mutants/
codeclass=src/main/java/fr/inria/peeruni/PiEstimator.java
HADOOP_PATH=/home/michel/hadoop-0.20.2/
JAR=/home/michel/workspace-eclipse/albonico/HadoopTest/target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar
ARGUMENTS="4 100 note 9001"
JOBTRACKER_LOG=hadoop-michel-jobtracker-note.log
TASKTRACKER_LOCAL_LOG=hadoop-michel-tasktracker-note.log
NAMENODE_LOG=hadoop-michel-namenode-note.log
DATANODE_LOCAL_LOG=hadoop-michel-datanode-note.log
TASKTRACKER_REMOTE_LOG=hadoop-michel-tasktracker-
DATANODE_REMOTE_LOG=hadoop-michel-datanode-

	mutnumber=`ls $mutpath`

	rm -Rf ./logshadoop/
	mkdir ./logshadoop/

	echo "Starting Hadoop Cluster"

	SLAVES=`cat slaves`

        $HADOOP_PATH/bin/stop-all.sh

        rm -Rf /tmp/dfs/

        $HADOOP_PATH/bin/hadoop namenode -format

        for SLV in $SLAVES;
        do
                 ssh $SLV "rm -Rf /tmp/dfs/"
        done

        $HADOOP_PATH/bin/start-all.sh

        sleep 30


	for mut in $mutnumber;
	do

		echo "Applying mutation number $mut!"

		. apply-mutation-classes.sh $nmutclasspath $mutpath/$mut/$nmutclass $codeclass $nmutclass  > /dev/null 2>&1

#		applyid`jobs -l | grep aply-mutation-classes | grep -v`

#		wait $applyid

		mkdir ./logshadoop/$mut/	

		rm -Rf ~/Pi*
		$HADOOP_PATH/bin/hadoop dfs -rmr /user/michel/*

		$HADOOP_PATH/bin/hadoop jar $JAR fr.inria.peerunit.PiEstimator 4 20 note 9001 > hadoop-result.log 2>&1
#		hadoopjob=`jobs -l | grep fr.inria.peerunit.PiEstimator | grep -v greo | awk '{print $2}'`

#		wait hadoopjob
		cp hadoop-result.log ./logshadoop/$mut/

		cp $HADOOP_PATH/logs/$JOBTRACKER_LOG ./logshadoop/$mut/
		cp $HADOOP_PATH/logs/$NAMENODE_LOG ./logshadoop/$mut/
		cp $HADOOP_PATH/logs/$TASKTRACKER_LOCAL_LOG ./logshadoop/$mut/
		cp $HADOOP_PATH/logs/$DATANODE_LOCAL_LOG ./logshadoop/$mut/

		for SLV1 in $SLAVES;
		do
			scp -r $SLV1:$HADOOP_PATH/logs/$TASKTRACKER_REMOTE_LOG$SLV1.log ./logshadoop/$mut/
			scp -r $SLV1:$HADOOP_PATH/logs/$DATANODE_REMOTE_LOG$SLV1.log ./logshadoop/$mut/
		done

	done
