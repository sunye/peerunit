echo "INFO: Please do not use ./path!"

#if [ $1 ] 
#then

#	mn=$1

#	echo "Path to class to mutate:"
#	read nmutclasspath
#	nmutclass=`echo $nmutclasspath | awk -F/ '{print $NF}'`

	nmutclasspath=target/classes/fr/inria/peerunit/WordCount.class
	nmutclass=WordCount.class

#	echo "Path to code class .java:"
#	read codeclass

	codeclass=src/main/java/fr/inria/peerunit/WordCount.java

#	echo "Path to mutations:"
#	read mutpath
	mutpath=target/mutants/

	#i=0

	#/run-psychedelic.sh $nmutclasspath $mutpath

	mutnumber=`ls $mutpath`

	rm -Rf ./logs/
	mkdir ./logs/

	for mut in $mutnumber;
	do
		
	#while [ $i -lt $mn ];
	#do

		echo "Applying mutation number $mut!"

		. apply-mutation-classes.sh $nmutclasspath $mutpath/$mut/$nmutclass $codeclass $nmutclass

		slaves=`cat slaves`

                for slv in $slaves;
		do

			echo "Sinc jar - $slv"
			scp target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar michel@$slv:/home/michel/HadoopTest/target/
		done

		. run-coordinator.sh &
		. run-master-tester.sh &
		sleep 10

		for slv in $slaves;
		do

			. run-remote-tester.sh $slv &

		done
		#echo "Job lists: "
		coordid=`jobs -l | grep run-coordinator.sh | grep -v grep | awk '{print $2}'`
		#coordid=`ps ax | grep CoordinatorRunner | grep -v grep | awk '{print $1}'`
		wait $coordid

		#sleep 30000;

		# Arquiving logs

	  	mkdir ./logs/$mut/	

		cp coordination.log ./logs/$mut/

		echo "" > coordinator.log

		cp Tester0.log ./logs/$mut/

		echo "" > Tester0.log
	
		pkill java
		
		for slv in $slaves;
		do
			. kill-remote-jvm.sh $slv
		done

		pkill ssh

#		if [ -e $mut/$nmutclass ]
#		then
#			echo "teste"
#		fi

		#i=$(( $i + 1 ))

	done

#else
#	echo "Please use the follow command: ./run-mutations mutationsnumber"
#fi
