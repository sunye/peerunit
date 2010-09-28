package load;

/**
 * @author albonico  
 */

import org.apache.hadoop.mapred.JobTracker;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TaskTracker;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.hadoop.hdfs.server.protocol.DatanodeRegistration;
import org.apache.hadoop.hdfs.server.protocol.DatanodeRegistration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.Server;
import org.apache.hadoop.ipc.RPC.VersionMismatch;
import org.apache.hadoop.net.DNSToSwitchMapping;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.net.NetworkTopology;
import org.apache.hadoop.net.Node;
import org.apache.hadoop.net.NodeBase;
import org.apache.hadoop.net.ScriptBasedMapping;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.util.HostsFileReader;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.VersionInfo;

import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
  
public class StartClusterParent {
    protected static Logger log = Logger.getLogger(StartClusterParent.class.getName());
    private int id;
    private GlobalVariables globals;
    protected TesterUtil defaults;
    protected int size;
    protected int sleep;
    protected int OBJECTS;

    @SetId
    public void setId(int i) {
        id = i;
    }

    public int getId() {
        return id;
    }

    @SetGlobals
    public void setGlobals(GlobalVariables gv) {
        globals = gv;
    }

    public GlobalVariables getGlobals() {
        return globals;
    }

    protected void put(int key, Object value) throws RemoteException {
	    globals.put(key, value);
    }

    protected Object get(int key) throws RemoteException {
        return globals.get(key);
    }

    protected int getName() {
        return id;
    }

    protected Map<Integer,Object> getCollection() throws RemoteException {
        return globals.getCollection();
    }

    protected void kill() {
        
    }

    protected void clear() {
        
    }

    // Same machine
    public static JobTracker jobtracker;
    public static NameNode namenode;

    public Configuration getConfMR() throws IOException, InterruptedException {

	// Definida manualmente pois o TestRunner nao le os arquivos de configuracao
	Thread.sleep(sleep);
	Configuration conf = new Configuration();
        conf.set("mapred.job.tracker","cohiba:9000");
	conf.set("mapred.tasktracker.map.tasks.maximum","1");
	conf.set("mapred.tasktracker.reduce.tasks.maximum","1");
	conf.set("mapred.child.java.opts","-Xmx512m");
	
	return conf;

    }

    public Configuration getConfHDFS() throws IOException, InterruptedException {
        
	Thread.sleep(sleep);
        Configuration conf = new Configuration();
	
	conf.set("fs.default.name","hdfs://cohiba:9001");
        conf.set("dfs.name.dir","/tmp/dfsname/");
        conf.set("dfs.data.dir","/tmp/dfsdata/");
        conf.set("dfs.replication","1");
	conf.set("hadoop.tmp.dir","/tmp/hadoop/");
	conf.set("mapred.child.java.opts","-Xmx512m");

        // Sempre formata o Sistema de Arquivos...
        //conf.set("dfs.namenode.startup","FORMAT");

	return conf;

    }

    @BeforeClass(range = "*", timeout = 100000)
    public void bc() throws FileNotFoundException {
        if (new File("peerunit.properties").exists()) {
            String filename = "peerunit.properties";
            FileInputStream fs = new FileInputStream(filename);
            defaults = new TesterUtil(fs);
        } else {
            defaults = TesterUtil.instance;
        }
        size = defaults.getObjects();
        sleep = defaults.getSleep();
        OBJECTS =defaults.getObjects();
        log.info("Starting Cluster Hadoop... ");
    }

    public void startJobTracker() throws IOException, InterruptedException {

	Configuration conf = getConfMR();
	
	JobConf job = new JobConf(conf);

	try {

	   jobtracker = JobTracker.startTracker(job);
        
	} catch (InterruptedException e) {

        }
    
   }

   public NameNode startNameNode() throws IOException, InterruptedException, RemoteException {

        Thread.sleep(sleep);
        log.info("Starting NameNode...");

	Configuration conf = getConfHDFS();	

	NameNode nn = new NameNode(conf);

	return nn;

   }

/*

   @TestStep(order = 3, timeout = 100000, range = "*")
   public void startTaskTracker() throws IOException, InterruptedException, RemoteException {

	Configuration conf = getConfMR();

	JobConf job = new JobConf(conf);

	Thread.sleep(sleep);
	log.info("Starting TaskTracker...");
        TaskTracker tt = new TaskTracker(job);
	log.info("Stopping TaskTracker...");
	//tt.shutdown();

   }

   @TestStep(order = 4, timeout = 100000, range = "*")
   public void startDataNode(String args[]) throws IOException, InterruptedException, RemoteException {


 	DatanodeRegistration dr = DatanodeRegistration();

        Thread.sleep(sleep);
        log.info("Starting DataNode...");
	Configuration cfg = getConfHDFS();
	DataNode dn = DataNode.createDataNode(args,cfg);
	// args deveria ser passado na execucao para saber quais diretorios ele trabalharah...
	String serveraddr = dn.getNamenode();
	log.info("Connected to NameNode: " + serveraddr); 


   }
*/
}
