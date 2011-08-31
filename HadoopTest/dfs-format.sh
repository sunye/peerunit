# PeerUnit dir
dirpeerunit=$1

# Hadoop dir
HADOOP_HOME=`cat hadoop.properties | grep hadoop.dir.install | cut -b 20-200`

# Remove hadoop dirs
dirdata=`cat hadoop.properties | grep hadoop.dir.data | cut -b 17-200`
dirname=`cat hadoop.properties | grep hadoop.dir.name | cut -b 17-200`
rm -Rf $dirdata
rm -Rf $dirname

# Create hadoop conf files
./hadoop-create-conf.sh $HADOOP_HOME/ $dirdata $dirname

# HDFS format
#~/hadoop-0.21.0/bin/hdfs namenode -format
$HADOOP_HOME//bin/hadoop namenode -format

# Remove PI dir
rm -Rf $dirpeerunit/PiEstimator_TMP_3_141592654
