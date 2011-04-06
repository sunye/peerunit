# ./apply-mutation-classes.sh /path/to/without/mutations/class /path/to/mutation/class classname

if [ $4 ]
then 

	if [ ! -d bkp ]
	then
		mkdir bkp
	fi

	if [ -e $3 ]
	then

		mv $3 bkp/$4

	else

		echo "File $3 could not be read!"
		exit 0
	fi


	if [ -e $2 ]
	then

		cp $2 $1
	else
		
		echo "File $2 could not be read!"
		exit 0

	fi

	mvn install

else
	echo "Please use the follow command: ./apply-mutation-classes.sh /path/to/without/mutations/class /path/to/mutation/class /path/to/class/file/java classname"

	echo ""

	echo "Example: ./apply-mutation-classes.sh ./target/classes/examples/PiEstimator.class ./target/mutants/0/PiEstimator.class ./src/main/java/examples/PiEstimator.java PiEstimator.java"
fi
