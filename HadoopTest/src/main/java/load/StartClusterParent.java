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
import org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode;

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
import java.io.FileOutputStream;
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
    public static JobConf job;

    public void readPropertiesHadoop() throws IOException {

	Properties properties = new Properties();
	
	File file = new File("hadoop.properties");

	FileInputStream fis = null;

	try {

		//fis = new FileOutputStream(file);
		fis = new FileInputStream(file);

		properties.load(fis);

		//fis.close(0);

		// Read JobTracker and Namenode Addresses
		String nnaddr = properties.getProperty("hadoop.namenode");
		String jtaddr = properties.getProperty("hadoop.jobtracker");

		System.out.println("Endereco do NameNode: " + nnaddr);


		this.put(-1, nnaddr);
		this.put(-2, jtaddr);

		String nnport = properties.getProperty("hadoop.namenode.port");
		String jtport = properties.getProperty("hadoop.jobtracker.port");

		this.put(-3, nnport);
		this.put(-4, jtport);
		
	} catch(IOException e) {

		System.out.println("----------------------------");

		System.out.println(e.toString());

		System.out.println("----------------------------");

	}


    } 

    public Configuration getConfMR() throws IOException, InterruptedException {

	// Definida manualmente pois o TestRunner nao le os arquivos de configuracao
	Thread.sleep(sleep);
	Configuration conf = new Configuration();
	String jthost = this.get(-2)+":"+this.get(-4);
	
	jthost = (String) jthost;

        conf.set("mapred.job.tracker",jthost);
	//conf.set("mapred.tasktracker.map.tasks.maximum","1");
	//conf.set("mapred.tasktracker.reduce.tasks.maximum","1");
	conf.set("mapred.child.java.opts","-Xmx1024m -XX:-UseGCOverheadLimit");
        //conf.set("mapred.job.reuse.jvm.num.tasks","1");	
	return conf;

    }

    public Configuration getConfHDFS() throws IOException, InterruptedException {
        
	Thread.sleep(sleep);
        Configuration conf = new Configuration();

	String nnhost = "hdfs://"+this.get(-1)+":"+this.get(-3);	

	nnhost = (String) nnhost;

	conf.set("fs.default.name", nnhost);
        conf.set("dfs.name.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir1/");
        conf.set("dfs.data.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir1data/");
        conf.set("dfs.replication","1");
	conf.set("hadoop.tmp.dir","/tmp/hadoop/");
	conf.set("mapred.child.java.opts","-Xmx512m");
	conf.set("fs.checkpoint.dir","/home/michel/GIT/albonico/HadoopTest/dir3/");

        // Sempre formata o Sistema de Arquivos...
        //conf.set("dfs.namenode.startup","UPGRADE");

	return conf;

    }

    public void startJobTracker() throws IOException, InterruptedException {

	Configuration conf = getConfMR();
	
	job = new JobConf(conf);

	try {

	   jobtracker = JobTracker.startTracker(job);
        
	} catch (InterruptedException e) {

        }
    
   }

   public NameNode startNameNode() throws IOException, InterruptedException, RemoteException {

	// test
	readPropertiesHadoop();


        Thread.sleep(sleep);
        log.info("Starting NameNode...");

	Configuration conf = getConfHDFS();	

	NameNode nn = new NameNode(conf);

	return nn;

   }


   public SecondaryNameNode startSecondaryNameNode() throws IOException, InterruptedException, RemoteException {

	Thread.sleep(sleep);

	log.info("Starting Secondary NameNode");

	Configuration conf = getConfHDFS();

	SecondaryNameNode snn = new SecondaryNameNode(conf);

	return snn;

   }

   public class startTaskTracker implements Runnable {

	public void run() {
   
		try {
			Configuration conf = getConfMR();
	
        		JobConf job = new JobConf(conf);

        		Thread.sleep(sleep);
        		log.info("Starting TaskTracker...");
        		TaskTracker tt = new TaskTracker(job);

		} catch(IOException e) {


		} catch(InterruptedException ee) {


		}
	
	}

   }

   public void startDataNode() throws IOException, InterruptedException, RemoteException {


        Thread.sleep(sleep);
        log.info("Starting DataNode...");
	Configuration cfg = getConfHDFS();

	if (hostname.equals(masterhost)) {
        	cfg.set("dfs.name.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir1/");
        	cfg.set("dfs.data.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir1data/");
	} else {
		cfg.set("dfs.name.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir2/");
                cfg.set("dfs.data.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir2data/");
		/*
		if (hostname.equals("macalan.c3sl.ufpr.br")) {
			cfg.set("dfs.name.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dfs/macalanname/");
                	cfg.set("dfs.data.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dfs/macalandata/");
		} else {
		
			if (hostname.equals("dalmore.c3sl.ufpr.br")) {
				cfg.set("dfs.name.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dfs/dalmorename/");
                		cfg.set("dfs.data.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dfs/dalmoredata/");
			}

		} 
		*/

	}

        } catch (java.net.UnknownHostException uhe) {

        }

	// Testar em uma unica maquina
/*
	if (testando == 1) {
		cfg.set("dfs.name.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir1/");
		cfg.set("dfs.data.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir1data/");
	} else {
		cfg.set("dfs.name.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir2/");
		cfg.set("dfs.data.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir2data/");
	}
*/
	//Fim teste

	String[] teste = {"-rollback"};

	DataNode dn = DataNode.createDataNode(teste,cfg);
	// args deveria ser passado na execucao para saber quais diretorios ele trabalharah...
	String serveraddr = dn.getNamenode();
	log.info("Connected to NameNode: " + serveraddr); 
   }

   public void runExample(String nameExample) throws IOException, RemoteException, InterruptedException, Exception {

        Configuration config = getConfMR();

        log.info("Starting " + nameExample);

	if (nameExample.equals("PiEstimator")) {
        
		PiEstimator pi = new PiEstimator();

        	String masteraddr = (String) this.get(-2);
        	String masterport = (String)this.get(-4);

       		pi.setCfg(masteraddr,masterport);
        	//pi.setCfg(config); (This is the correct)

        	String[] argumentos = {"4","20"};

        	pi.run(argumentos);
 
	}

   }
}
