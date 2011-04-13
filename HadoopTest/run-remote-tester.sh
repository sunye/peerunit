echo "Running remote command on $1"

ssh $1 "cd ~/HadoopTest/ && ./run-local-tester.sh"
