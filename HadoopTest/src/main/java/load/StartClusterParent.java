package load;

/**
 * @author albonico 

 * Parent Class to Start Cluster Hadoop.
 * All Exceptions will be deal by PeerUnit.
 */

// My classes
//import org.apache.hadoop.examples.BaileyBorweinPlouffe;
//import examples.BaileyBorweinPlouffe;
import examples.PiEstimator;
import util.ThreadUtilities;

// Hadoop classes
import org.apache.hadoop.mapred.JobTracker;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TaskTracker;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapreduce.Job;

// PeerUnit classes
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.util.TesterUtil;

// Java classes
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.Map;
import java.util.Properties;
import java.math.BigDecimal;
  
public class StartClusterParent {

    protected static Logger log = Logger.getLogger(StartClusterParent.class.getName());
    protected FileHandler fh;

    private int id;
    private GlobalVariables globals;
    protected TesterUtil defaults;
    protected int size;
    protected int sleep;
    protected int OBJECTS;

    //protected static JobConf job;
    protected static JobConf job;
    protected static JobTracker JTracker;
    protected static TaskTracker TTracker;
    protected static NameNode NNode;
    protected static Thread nnThread;
    protected static Thread ttThread;
    protected static Thread jtThread;
    protected static Thread dnThread;
    protected static Thread jobThread;
    protected static BigDecimal jobResult;
    protected static NameNode nn;
    protected static DataNode dn;
    protected static Process jtProcess;
    protected static Process ttProcess; 
    protected static startNameNode nnode;
    protected static startDataNode dnode;

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

    public void setLogger() throws IOException {

/*	    fh = new FileHandler("hadoop.log"); 
        log.addHandler(fh);
        System.setOut(new PrintStream("testelog.txt"));
*/ 
    }

   @BeforeClass(range = "*", timeout = 100000)
    public void bc() throws FileNotFoundException, IOException {
	   
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

	    setLogger();

    }

    public void readPropertiesHadoop() throws IOException, InterruptedException {

    	log.info("Starting Cluster Hadoop!");
    	
		Properties properties = new Properties();
		File file = new File("hadoop.properties");
		FileInputStream fis = null;
	
		log.info("Reading Hadoop configuration!");
	
		fis = new FileInputStream(file);
		properties.load(fis);
	
		/**
		* JobTracker and NameNode Properties
		*/
	
		// Read JobTracker and Namenode Addresses
		String nnaddr = properties.getProperty("hadoop.namenode");
		String jtaddr = properties.getProperty("hadoop.jobtracker");
	
		this.put(-1, nnaddr);
		this.put(-2, jtaddr);
	
		String nnport = properties.getProperty("hadoop.namenode.port");
		String jtport = properties.getProperty("hadoop.jobtracker.port");
	
		this.put(-3, nnport);
		this.put(-4, jtport);
	
		/**
		* Other Properties
		*/
	
		String dfsname = properties.getProperty("hadoop.dir.name");
		String dfsdata = properties.getProperty("hadoop.dir.data");
		String hadooptmp = properties.getProperty("hadoop.dir.tmp");
		String dfssnn = properties.getProperty("hadoop.dir.secnn");
		String hadooplog = properties.getProperty("hadoop.dir.log");
		String hadooprep = properties.getProperty("hadoop.dfs.replication");
		String javaopt = properties.getProperty("hadoop.java.options");
		String memtask = properties.getProperty("mapred.child.java.opts");
		String version = properties.getProperty("hadoop.version");
		String hadoopdir = properties.getProperty("hadoop.dir.install");
		
		this.put(-5, dfsname);
		this.put(-6, dfsdata);
		this.put(-7, hadooptmp);
		this.put(-8, dfssnn);
		this.put(-9, hadooplog);
		this.put(-10, hadooprep);	
		this.put(-11, javaopt);
		this.put(-12, memtask);
		this.put(-13, version);
		this.put(-14, hadoopdir);

    } 

    public Configuration getConfMR() throws IOException, InterruptedException {

		log.info("Reading MR configuration!");
	
		Thread.sleep(sleep);
		Configuration conf = new Configuration();
		String jthost = this.get(-2)+":"+this.get(-4);
		
		jthost = (String) jthost;
	
	    conf.set("mapred.job.tracker", jthost);
		//conf.set("mapreduce.jobtracker.address", jthost);
	
	    String joptions = (String) this.get(-11);
	    String memtask = (String) this.get(-12);
	    conf.set("mapred.child.java.opts", joptions);
	    conf.set("mapred.child.java.opts", memtask);	

	    return conf;

    }

    public Configuration getConfHDFS() throws IOException, InterruptedException {

		log.info("Reading HDFS configuration!");
	
		Thread.sleep(sleep);
	    Configuration conf = new Configuration();
	
		String nnhost = "hdfs://"+this.get(-1)+":"+this.get(-3);	
	
		nnhost = (String) nnhost;
	
		String dirname = (String) this.get(-5);
		String dirdata = (String) this.get(-6);
		String dirtmp = (String) this.get(-7);
		String dirsnn = (String) this.get(-8);
		String dirlog = (String) this.get(-9);
		String replication = (String) this.get(-10);
		String joptions = (String) this.get(-11);
	
		//conf.set("fs.defaultFS", nnhost);
		conf.set("fs.default.name", nnhost);
	    conf.set("dfs.name.dir", dirname);
	    conf.set("dfs.data.dir", dirdata);
	    conf.set("dfs.replication", replication);
		conf.set("hadoop.tmp.dir", dirtmp);
		conf.set("hadoop.log.dir", dirlog);
		conf.set("mapred.child.java.opts", joptions);
		conf.set("fs.checkpoint.dir", dirsnn);
	
		return conf;

    }

   /**
   * Methods to initialize Hadoop Cluster / Create Threads
   *
   */

    public void initNN() throws IOException, InterruptedException {
    	
		log.info("Starting NameNode!"); 
	
		readPropertiesHadoop();
		
		nnode = new startNameNode();
	    nnThread = new Thread(nnode);
	    nnThread.start();
		nnThread.join();

    }
    
    public void stopNN() throws IOException, InterruptedException {
    	
    	log.info("Stopping NameNode...");
    	
    	nnode.done();
    	
    }

    /*
    public void initSNN() throws IOException, InterruptedException {

		log.info("Starting Secondary NameNode!");
	
		startSecondaryNameNode snn = new startSecondaryNameNode();
	    Thread snnThread = new Thread(snn);
	    snnThread.start();
		snnThread.join();

    } 
    */
    
    public void initJT() throws IOException, InterruptedException {
    	
    	readPropertiesHadoop();
    	
		log.info("Starting JobTracker!");
	
		startJobTracker jtracker = new startJobTracker();
	    jtThread = new Thread(jtracker);
	    jtThread.start();
		jtThread.join();
	    
    }

    public void initTT() throws IOException, InterruptedException {

		log.info("Starting TaskTracker!");
	
	    startTaskTracker ttracker = new startTaskTracker();
	    // Thread ttThread = new Thread(ttracker);
	    ttThread = new Thread(ttracker);
	    ttThread.start();
	    ttThread.join();
	    
    }

    public void initDN() throws IOException, InterruptedException {

		log.info("Starting DataNode!"); 
	
	    startDataNode datanode = new startDataNode();
	    dnThread = new Thread(datanode);
	    dnThread.start();
	    dnThread.join();
	    
	}
    
    public void runJob() throws IOException, InterruptedException {

		log.info("Starting DataNode!"); 

		runPiEstimator pi = new runPiEstimator();
		jobThread = new Thread(pi);
		jobThread.start();
		jobThread.join();
	    
	}

    /**
    * Classes to create Runnable Objects
    *
    */
    public class startNameNode implements Runnable {

    	private boolean threadDone = false;

        public void done() {
            threadDone = true;
        }
    	
		public void run() {
		
		//while (!threadDone) {
			
			try {
				
				Configuration conf = getConfHDFS();
			
				nn = new NameNode(conf);
				NNode = nn;
				
			} catch (IOException ioe) {
				
			} catch (InterruptedException ie) {
				
			}
			
		//}

		}
	
   }

   public class startSecondaryNameNode implements Runnable {

		public void run() {
			
			try {
				
				Configuration conf = getConfHDFS();
				SecondaryNameNode snn = new SecondaryNameNode(conf);
				
			} catch (IOException ioe) {
				
			} catch (InterruptedException ie) {
				
			}
		
		}
		
   }
   
   public class startJobTracker implements Runnable {

		public void run() {
	
			try {
				
					String hadoopdir = (String) get(-14);
				    //String command = "/home/michel/hadoop-0.20.2/bin/start-job.sh";
				    String command = "java -Xmx1000m -Dcom.sun.management.jmxremote" +
				    		" -Dcom.sun.management.jmxremote" +
				    		" -Dhadoop.log.dir="+hadoopdir+"/logs" +
				    		" -Dhadoop.log.file=hadoop-jobtracker.log" +
				    		" -Dhadoop.home.dir="+hadoopdir+
				    		" -Dhadoop.id.str=michel -Dhadoop.root.logger=INFO,DRFA" +
				    		" -Djava.library.path="+hadoopdir+"/lib/native/Linux-i386-32" +
				    		" -Dhadoop.policy.file=hadoop-policy.xml" +
				    		" -classpath "+hadoopdir+"/conf:" +
				    		"/usr/lib/jvm/java-6-sun/lib/tools.jar:"+hadoopdir+":" +
				    		hadoopdir+"/hadoop-0.20.2-core.jar:" +
				    		hadoopdir+"/lib/commons-cli-1.2.jar:" +
				    		hadoopdir+"/lib/commons-codec-1.3.jar:" +
				    		hadoopdir+"/lib/commons-el-1.0.jar:" +
				    		hadoopdir+"/lib/commons-httpclient-3.0.1.jar:" +
				    		hadoopdir+"/lib/commons-logging-1.0.4.jar:" +
				    		hadoopdir+"/lib/commons-logging-api-1.0.4.jar:" +
				    		hadoopdir+"/lib/commons-net-1.4.1.jar:" +
				    		hadoopdir+"/lib/core-3.1.1.jar:" +
				    		hadoopdir+"/lib/hsqldb-1.8.0.10.jar:" +
				    		hadoopdir+"/lib/jasper-compiler-5.5.12.jar:" +
				    		hadoopdir+"/lib/jasper-runtime-5.5.12.jar:" +
				    		hadoopdir+"/lib/jets3t-0.6.1.jar:" +
				    		hadoopdir+"/lib/jetty-6.1.14.jar:" +
				    		hadoopdir+"/lib/jetty-util-6.1.14.jar:" +
				    		hadoopdir+"/lib/junit-3.8.1.jar:" +
				    		hadoopdir+"/lib/kfs-0.2.2.jar:" +
				    		hadoopdir+"/lib/log4j-1.2.15.jar:" +
				    		hadoopdir+"/lib/mockito-all-1.8.0.jar:" +
				    		hadoopdir+"/lib/oro-2.0.8.jar:" +
				    		hadoopdir+"/lib/servlet-api-2.5-6.1.14.jar:" +
				    		hadoopdir+"/lib/slf4j-api-1.4.3.jar:" +
				    		hadoopdir+"/lib/slf4j-log4j12-1.4.3.jar:" +
				    		hadoopdir+"/lib/xmlenc-0.52.jar:" +
				    		hadoopdir+"/lib/jsp-2.1/jsp-2.1.jar:" +
				    		hadoopdir+"/lib/jsp-2.1/jsp-api-2.1.jar " +
				    		"org.apache.hadoop.mapred.JobTracker";
				    
				    jtProcess = Runtime.getRuntime().exec(command);
				
		            /*
					Configuration conf = getConfMR();
					job = new JobConf(conf);
				   	JobTracker jobtracker = JobTracker.startTracker(job);
				   	JTracker = jobtracker;
				   	*/
		            
				} catch (IOException ioe) {
					
				} 
		}
   
   }

   public class startTaskTracker implements Runnable {

		public void run() {
		
			try {
				
		    	log.info("Starting TaskTracker!");
		    	
		    	String hadoopdir = (String) get(-14);
		    	
		        //String command = "/home/michel/hadoop-0.20.2/bin/start-track.sh";
		        String command = "java -Xmx1000m" +
		        		" -Dhadoop.log.dir="+hadoopdir+"/logs" +
		        		" -Dhadoop.log.file=hadoop-michel-tasktracker-note.log" +
		        		" -Dhadoop.home.dir="+hadoopdir+
		        		" -Dhadoop.id.str=michel -Dhadoop.root.logger=INFO,DRFA" +
		        		" -Djava.library.path="+hadoopdir+"/lib/native/Linux-i386-32" +
		        		" -Dhadoop.policy.file=hadoop-policy.xml" +
		        		" -classpath "+hadoopdir+"/conf:" +
		        		"/usr/lib/jvm/java-6-sun/lib/tools.jar:" +
		        		hadoopdir+":" +
		        		hadoopdir+"/hadoop-0.20.2-core.jar:" +
		        		hadoopdir+"/lib/commons-cli-1.2.jar:" +
		        		hadoopdir+"/lib/commons-codec-1.3.jar:" +
		        		hadoopdir+"/lib/commons-el-1.0.jar:" +
		        		hadoopdir+"/lib/commons-httpclient-3.0.1.jar:" +
		        		hadoopdir+"/lib/commons-logging-1.0.4.jar:" +
		        		hadoopdir+"/lib/commons-logging-api-1.0.4.jar:" +
		        		hadoopdir+"/lib/commons-net-1.4.1.jar:" +
		        		hadoopdir+"/lib/core-3.1.1.jar:" +
		        		hadoopdir+"/lib/hsqldb-1.8.0.10.jar:" +
		        		hadoopdir+"/lib/jasper-compiler-5.5.12.jar:" +
		        		hadoopdir+"/lib/jasper-runtime-5.5.12.jar:" +
		        		hadoopdir+"/lib/jets3t-0.6.1.jar:" +
		        		hadoopdir+"/lib/jetty-6.1.14.jar:" +
		        		hadoopdir+"/lib/jetty-util-6.1.14.jar:" +
		        		hadoopdir+"/lib/junit-3.8.1.jar:" +
		        		hadoopdir+"/lib/kfs-0.2.2.jar:" +
		        		hadoopdir+"/lib/log4j-1.2.15.jar:" +
		        		hadoopdir+"/lib/mockito-all-1.8.0.jar:" +
		        		hadoopdir+"/lib/oro-2.0.8.jar:" +
		        		hadoopdir+"/lib/servlet-api-2.5-6.1.14.jar:" +
		        		hadoopdir+"/lib/slf4j-api-1.4.3.jar:" +
		        		hadoopdir+"/lib/slf4j-log4j12-1.4.3.jar:" +
		        		hadoopdir+"/lib/xmlenc-0.52.jar:" +
		        		hadoopdir+"/lib/jsp-2.1/jsp-2.1.jar:" +
		        		hadoopdir+"/lib/jsp-2.1/jsp-api-2.1.jar" +
		        		" org.apache.hadoop.mapred.TaskTracker";
		    	ttProcess = Runtime.getRuntime().exec(command);
				
		        /*
				Configuration conf = getConfMR();
			    JobConf job = new JobConf(conf);
		    	TaskTracker tt = new TaskTracker(job);
		        */
		    } catch (IOException ioe) {
		    	
		    }
		    
		}

   }

   public class startDataNode implements Runnable {
	
		public void run() {
		
			try {
			
				Configuration cfg = getConfHDFS();
				String dirname = (String) get(-5);
				String dirdata = (String) get(-6);
				cfg.set("dfs.name.dir",dirname);
				cfg.set("dfs.data.dir",dirdata);
				
				String[] args = {"-rollback"};
		
				dn = DataNode.createDataNode(args,cfg);
		
				String serveraddr = dn.getNamenode();
				log.info("DataNode connected with NameNode: " + serveraddr); 
				
			} catch (IOException ioe) {
			
			} catch (InterruptedException ie) {
				
			}
				
		}
		
   }

   public class runPiEstimator implements Runnable {

	   public void run() {

		   try {
			   
			    log.info("Starting PiEstimator!");
			    
				PiEstimator pi = new PiEstimator();
		       	String masteraddr = (String) get(-2);
		       	String masterport = (String) get(-4);
		       	pi.setCfg(masteraddr,masterport);
		       	//pi.setCfg(config); (This is correct)
		       	String[] argumentos = {"4","20"};
		        pi.run(argumentos);
		        
		        jobResult = pi.getResult();
		        
			    /*
			    BaileyBorweinPlouffe pi = new BaileyBorweinPlouffe();
			    String[] args = {"1","6","4","/pi"};
			    pi.run(args);
		        */
		        
	      	} catch (IOException ioe) {
				
			} catch (Exception e) {
				
			}
	   }
	   
	

  }
   
}
