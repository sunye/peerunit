hadoophome=$1
dirdata=$2
dirname=$3

jobhost=`cat hadoop.properties | grep hadoop.jobtracker= | cut -b 19-200`
jobport=`cat hadoop.properties | grep hadoop.jobtracker.port | cut -b 24-200`
namehost=`cat hadoop.properties | grep hadoop.namenode= | cut -b 17-200`
nameport=`cat hadoop.properties | grep hadoop.namenode.port | cut -b 22-200`

echo "
<configuration>
<property>
  <name>mapred.job.tracker</name>
  <value>$jobhost:$jobport</value>
</property>
</configuration>
" > $hadoophome/conf/mapred-site.xml

echo "
<configuration>
  <property>
    <name>fs.default.name</name>
    <value>hdfs://$namehost:$nameport</value>
  </property>
</configuration>
" > $hadoophome/conf/core-site.xml

echo "
<configuration>
  <property>
    <name>dfs.name.dir</name>
    <value>$dirname</value>
  </property>
  <property>
    <name>dfs.data.dir</name>
    <value>$dirdata</value>
  </property>
</configuration>
" > $hadoophome/conf/hdfs-site.xml
