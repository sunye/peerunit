package load;

/**
 * @author albonico 
 * Parent Class to Start Cluster Hadoop.
 * All Exceptions will be deal by PeerUnit.
 */

// My classes
import examples.PiEstimator;

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
  
public class StartClusterParent {

    protected static Logger log = Logger.getLogger(StartClusterParent.class.getName());
    protected FileHandler fh;

    private int id;
    private GlobalVariables globals;
    protected TesterUtil defaults;
    protected int size;
    protected int sleep;
    protected int OBJECTS;

    protected static JobConf job;

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

        log.info("Starting Cluster Hadoop!");

    }

    public void readPropertiesHadoop() throws IOException, InterruptedException {

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
	
		this.put(-5, dfsname);
		this.put(-6, dfsdata);
		this.put(-7, hadooptmp);
		this.put(-8, dfssnn);
		this.put(-9, hadooplog);
		this.put(-10, hadooprep);	
		this.put(-11, javaopt);

    } 

    public Configuration getConfMR() throws IOException, InterruptedException {

		log.info("Reading MR configuration!");
	
		Thread.sleep(sleep);
		Configuration conf = new Configuration();
		String jthost = this.get(-2)+":"+this.get(-4);
		
		jthost = (String) jthost;
	
	    conf.set("mapred.job.tracker",jthost);
	
	    String joptions = (String) this.get(-11);
	    conf.set("mapred.child.java.opts",joptions);
	
		//conf.set("mapred.tasktracker.map.tasks.maximum","1");
		//conf.set("mapred.tasktracker.reduce.tasks.maximum","1");
	    //conf.set("mapred.job.reuse.jvm.num.tasks","1");	

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
	
		conf.set("fs.default.name", nnhost);
	    conf.set("dfs.name.dir",dirname);
	    conf.set("dfs.data.dir",dirdata);
	    conf.set("dfs.replication",replication);
		conf.set("hadoop.tmp.dir",dirtmp);
		conf.set("hadoop.log.dir",dirlog);
		conf.set("mapred.child.java.opts",joptions);
		conf.set("fs.checkpoint.dir",dirsnn);
	
	    //conf.set("dfs.name.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir1/");
	    //conf.set("dfs.data.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/dir1data/");
	    //conf.set("dfs.replication","1");
		//conf.set("hadoop.tmp.dir","/tmp/hadoop/");
		//conf.set("hadoop.log.dir","/home/ppginf/michela/GIT/albonico/HadoopTest/logs/");
	    // Sempre formata o Sistema de Arquivos...
	    //conf.set("dfs.namenode.startup","UPGRADE");
	
		return conf;

    }

   /**
   * Methods to initialize Hadoop Cluster / Create Threads
   *
   */

    public void initNN() throws IOException, InterruptedException {

		log.info("Starting NameNode!"); 
	
		readPropertiesHadoop();
		
		startNameNode nnode = new startNameNode();
	    Thread nnThread = new Thread(nnode);
	    nnThread.start();
		nnThread.join();

    }

    public void initSNN() throws IOException, InterruptedException {

		log.info("Starting Secondary NameNode!");
	
		startSecondaryNameNode snn = new startSecondaryNameNode();
	    Thread snnThread = new Thread(snn);
	    snnThread.start();
		snnThread.join();

    } 

    public void initJT() throws IOException, InterruptedException {
 
		log.info("Starting JobTracker!");
	
		startJobTracker jtracker = new startJobTracker();
	    Thread jtThread = new Thread(jtracker);
	    jtThread.start();
		jtThread.join();
	
    }

    public void initTT() throws IOException, InterruptedException {

		log.info("Starting TaskTracker!");
	
	    startTaskTracker ttracker = new startTaskTracker();
	    Thread ttThread = new Thread(ttracker);
	    ttThread.start();
		ttThread.join();
	
    }

    public void initDN() throws IOException, InterruptedException {

		log.info("Starting DataNode!"); 
	
	    startDataNode datanode = new startDataNode();
	    Thread dnThread = new Thread(datanode);
	    dnThread.start();
	    dnThread.join();
	
	}

    /**
    * Classes to create Runnable Objects
    *
    */
    public class startJobTracker implements Runnable {

		public void run() {
	
			try {
				
					Configuration conf = getConfMR();
				
					job = new JobConf(conf);
				   	JobTracker jobtracker = JobTracker.startTracker(job);
				} catch (IOException ioe) {
					
				} catch (InterruptedException ie) {
					
				}
		}
    
    }

   public class startNameNode implements Runnable {

		public void run() {
		
			try {
				
				Configuration conf = getConfHDFS();
			
				NameNode nn = new NameNode(conf);
				
			} catch (IOException ioe) {
				
			} catch (InterruptedException ie) {
				
			}
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

   public class startTaskTracker implements Runnable {

		public void run() {
		
			try {
				
				Configuration conf = getConfMR();
			    JobConf job = new JobConf(conf);
		    	TaskTracker tt = new TaskTracker(job);
		    	
		    } catch (IOException ioe) {
				
			} catch (InterruptedException ie) {
				
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
		
				DataNode dn = DataNode.createDataNode(args,cfg);
		
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
		       	//pi.setCfg(config); (This is the correct)
		       	String[] argumentos = {"4","20"};
		        pi.run(argumentos);
		        
	      	} catch (IOException ioe) {
				
			} catch (InterruptedException ie) {
				
			} catch (Exception e) {
				
			}
	   }

  }
   
}
