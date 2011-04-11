echo "INFO: Please do not use ./path!"

#if [ $1 ] 
#then

	mn=$1

	echo "Path to class to mutate:"
	read nmutclasspath
	nmutclass=`echo $nmutclasspath | awk -F/ '{print $NF}'`

	echo "Path to code class .java:"
	read codeclass

	echo "Path to mutations:"
	read mutpath

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

		. apply-mutation-classes.sh $nmutclasspath $mutpath/$mut/ $codeclass $nmutclass

		scp target/HadoopTest-1.0-SNAPSHOT-jar-with-dependencies.jar michel@micro:/home/michel/HadoopTest/target/

		. run-coordinator.sh &
		. run-master-tester.sh &
		sleep 10
		. run-remote-tester.sh micro &

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
