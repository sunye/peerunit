#!/bin/bash
HADOOP_HOME=`cat hadoop.properties | grep ^hadoop.dir.install | cut -b 20-200`
#cd $HADOOP_HOME/logs
tail -f $HADOOP_HOME/logs/hadoop-hadooptest-jobtracker-note.log | while read line ; do if grep "Choosing" <<< "$line"; then pkill tail; fi; done
