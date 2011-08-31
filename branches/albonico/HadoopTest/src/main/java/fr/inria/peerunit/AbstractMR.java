package fr.inria.peerunit;

/**
 * @author albonico
 *
 */

// Hadoop classes
import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ModificationWatchpointRequest;

import java.util.logging.Level;
import org.apache.hadoop.mapred.JobTracker;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.TaskTracker;
import org.apache.hadoop.hdfs.server.namenode.NameNode;
import org.apache.hadoop.hdfs.server.datanode.DataNode;
import org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapred.OutputLogFilter;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileUtil;

// PeerUnit classes
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.util.TesterUtil;
import fr.inria.peerunit.tester.Assert;

// Java classes
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.FileHandler;
import java.util.Map;
import java.util.Properties;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

// Java reflection
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;
import java.util.List;

public class AbstractMR {

    // PeerUnit vars
    protected static Logger log = Logger.getLogger(AbstractMR.class.getName());
    protected FileHandler fh;
    private int id;
    private GlobalVariables globals;
    protected TesterUtil defaults;
    protected int size;
    protected int sleep;
    protected int OBJECTS;


    // HadoopTest vars
    protected static JobConf job;
    protected static JobTracker JTracker;
    protected static TaskTracker TTracker;
    protected static NameNode NNode;
    protected static Thread jobThread;
    protected static BigDecimal jobResult;
    protected static double jobDuration;
    protected static NameNode nn;
    protected static DataNode dn;
    protected static Process nnProcess;
    protected static Process dnProcess;
    protected static Process jtProcess;
    protected static Process ttProcess;
    protected static startNameNode nnode;
    protected static startDataNode dnode;
    protected static startNameNodeByHadoop nnodeHadoop;
    protected static startDataNodeByHadoop dnodeHadoop;
    protected static ArrayList mutationOutputList;
    protected static Thread rwcThread;

    // Lower Tester vars
    protected static String CLASS_NAME;
    protected static String FIELD_NAME;

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

    protected Map<Integer, Object> getCollection() throws RemoteException {
        return globals.getCollection();
    }

    protected void kill() {
    }

    protected void clear() {
    }

    @BeforeClass(range = "*", timeout = 100000)
    public void bc() throws IOException, FileNotFoundException, InterruptedException {

    	setEnvironmentProperties();

        readPropertiesHadoop();

    }

    public void setEnvironmentProperties() throws FileNotFoundException, IOException, InterruptedException {

        if (new File("peerunit.properties").exists()) {
            String filename = "peerunit.properties";
            FileInputStream fs = new FileInputStream(filename);
            defaults = new TesterUtil(fs);
        } else {
            defaults = TesterUtil.instance;
        }
        size = defaults.getObjects();
        sleep = defaults.getSleep();
        OBJECTS = defaults.getObjects();

    }

    synchronized public void readPropertiesHadoop() throws IOException, InterruptedException {

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
        String mutantclass = properties.getProperty("mutant.class");
        String mutantoutputdir = properties.getProperty("mutant.output.dir");
        String pivalue = properties.getProperty("pi.value");
        String pinmaps = properties.getProperty("pi.nMaps");
        String pinsamples = properties.getProperty("pi.nSamples");
        String inputdir = properties.getProperty("wordcount.input");
        String outputdir = properties.getProperty("wordcount.output");
        String wfile = properties.getProperty("wordcount.file");
        String wcsleep = properties.getProperty("wordcount.sleep");

        String jobJar = properties.getProperty("job.jar");
        String jobClass = properties.getProperty("job.class");
        String jobParameters = properties.getProperty("job.parameters");

        String ltPort = properties.getProperty("lower.tester.port");
        String ltClass = properties.getProperty("lower.tester.class");
        String ltField = properties.getProperty("lower.tester.field");

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
        this.put(-15, jobJar);
        this.put(-16, jobClass);
        this.put(-17, jobParameters);
        this.put(-18, mutantclass);
        this.put(-19, mutantoutputdir);

        // PiEstimator arguments
        this.put(-20, pivalue);
        this.put(-21, pinmaps);
        this.put(-22, pinsamples);

        //WordCount
        this.put(-23, inputdir);
        this.put(-24, outputdir);
        this.put(-25, wfile);
        this.put(-26, wcsleep);

        //JPDA - Lower Tester
        this.put(-30, ltPort);
        this.put(-31, ltClass);
        this.put(-32, ltField);

        // File to logging results
        String resultFile = properties.getProperty("job.result.logfile");
        this.put(-33,resultFile);
        String regexChar = properties.getProperty("job.result.regex");
        this.put(-34, regexChar);
        String likeWord = properties.getProperty("job.result.like");
        this.put(-35, likeWord);
        String resultPosition = properties.getProperty("job.result.position");
        this.put(-36, resultPosition);
    }

    public Configuration getConfMR() throws IOException, InterruptedException {

        log.info("Reading MR configuration!");

     //   Thread.sleep(sleep);
        Configuration conf = new Configuration();
        String jthost = this.get(-2) + ":" + this.get(-4);

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

       // Thread.sleep(sleep);
        Configuration conf = new Configuration();

        String nnhost = "hdfs://" + this.get(-1) + ":" + this.get(-3);

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
     * Classes to create Runnable Objects
     *
     */
   public class startNameNodeByHadoop implements Runnable {

       public void run() {

           try {

                // Hadoop dir
                String hadoopdir = (String) get(-14);

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
                "org.apache.hadoop.hdfs.server.namenode.NameNode";

                nnProcess = Runtime.getRuntime().exec(command);

            } catch (IOException ioe) {
                log.warning(ioe.toString());
            }

       }

    }

    public class startNameNode implements Runnable {

        public void run() {

           try {

                readPropertiesHadoop();

                Configuration conf = getConfHDFS();

                nn = new NameNode(conf);
                NNode = nn;

                Thread.sleep(5000);

              //  Thread.currentThread().join();

            } catch (IOException ioe) {

            } catch (InterruptedException ie) {

            }

       }

     }

    public class startDataNodeByHadoop implements Runnable {

        public void run() {

            try {

                // Hadoop dir
                String hadoopdir = (String) get(-14);

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
                "org.apache.hadoop.hdfs.server.datanode.DataNode";

                dnProcess = Runtime.getRuntime().exec(command);
            } catch (IOException ioe) {
                log.warning(ioe.toString());
            }

        }
    }

    public class startDataNode implements Runnable {

        public void run() {

            try {

                Configuration cfg = getConfHDFS();
                String dirname = (String) get(-5);
                String dirdata = (String) get(-6);
                cfg.set("dfs.name.dir", dirname);
                cfg.set("dfs.data.dir", dirdata);

                String[] args = {"-rollback"};

                dn = DataNode.createDataNode(args, cfg);

                String serveraddr = dn.getNamenode();
                log.log(Level.INFO, "DataNode connected with NameNode: {0}", serveraddr);

                Thread.currentThread().join();

            } catch (IOException ioe) {
            } catch (InterruptedException ie) {
            }

       }

    }

    // Is not used
    /*
    public class startSecondaryNameNode implements Runnable {

        public void run() {

            try {

                Configuration conf = getConfHDFS();
                SecondaryNameNode snn = new SecondaryNameNode(conf);

            } catch (IOException ioe) {
                log.info(ioe.toString());
            } catch (InterruptedException ie) {
                log.info(ie.toString());
            }

        }
    }
     *
     */

    // Start JobTraker by Hadoop
    public class startJobTrackerByHadoop implements Runnable {

        public void run() {

            try {

                log.info("Starting JobTracker!");

                String hadoopdir = (String) get(-14);

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

            } catch (IOException ioe) {
                log.warning(ioe.toString());
            }

        }

    }

    public class startJobTracker implements Runnable {

        public void run() {

            try {

                log.info("Starting JobTracker!");

                Configuration conf = getConfMR();
                job = new JobConf(conf);
                JTracker = JobTracker.startTracker(job);
                JTracker.offerService();

            //    Thread.sleep(5000);

             //   Thread.currentThread().join();

             //   Thread.currentThread().yield();


            } catch (IOException ioe) {
                log.info(ioe.toString());
            } catch (InterruptedException ie) {
                log.info(ie.toString());
            }
        }

    }

    public class startTaskTrackerByHadoop implements Runnable {

        public void run() {

            try {

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

            } catch (IOException ioe) {
                log.warning(ioe.toString());
            }

        }

    }

    public class startTaskTracker implements Runnable {

        public void run() {

            try {


                log.info("Starting TaskTracker!");

                Configuration conf = getConfMR();
                JobConf job = new JobConf(conf);
                TTracker = new TaskTracker(job);
                TTracker.run();

            } catch (IOException ioe) {
                log.info(ioe.toString());
            } catch (InterruptedException ie) {
                log.info(ie.toString());
            }

        }
    }

    /**
     * Methods to initialize Hadoop Cluster / Create Threads
     *
     */

    
    public Thread initNN() throws IOException, InterruptedException {

        log.info("Starting NameNode!");

        readPropertiesHadoop();

        nnode = new startNameNode();
        Thread nnT = new Thread(nnode);

        return nnT;
        
        //nnThread.start();
        //nnThread.join();

     }

    public Thread initNNByHadoop() throws IOException, InterruptedException {

        log.info("Starting NameNode!");

        nnodeHadoop = new startNameNodeByHadoop();
        Thread nnT = new Thread(nnodeHadoop);

        return nnT;

     }

    
    public Thread initDN() throws IOException, InterruptedException {

        log.info("Starting DataNode!");

        startDataNode datanode = new startDataNode();
        Thread dnT = new Thread(datanode);

        return dnT;

    }

    public Thread initDNByHadoop() throws IOException, InterruptedException {

        log.info("Starting DataNode!");

        startDataNodeByHadoop datanode = new startDataNodeByHadoop();
        Thread dnT = new Thread(datanode);

        return dnT;

    }

    
    public Thread initJT() throws IOException, InterruptedException {

        log.info("Starting JobTracker!");

        startJobTracker jtracker = new startJobTracker();
        Thread jtT = new Thread(jtracker);

        return jtT;

    }

    public Thread initJTByHadoop() throws IOException, InterruptedException {

        log.info("Starting JobTracker!");

        startJobTrackerByHadoop jtracker = new startJobTrackerByHadoop();
        Thread jtT = new Thread(jtracker);

        return jtT;

    }

    
    public Thread initTT() throws IOException, InterruptedException {

        log.info("Starting TaskTracker!");

        startTaskTracker ttracker = new startTaskTracker();
        // Thread ttThread = new Thread(ttracker);
        Thread ttT = new Thread(ttracker);


        return ttT;

    }

    public Thread initTTByHadoop() throws IOException, InterruptedException {

        log.info("Starting TaskTracker!");

        startTaskTrackerByHadoop ttracker = new startTaskTrackerByHadoop();
        Thread ttT = new Thread(ttracker);

        return ttT;

    }

    public void putFileHDFS(String file, String dir) {

        try {

            String hadoopdir = (String) get(-14);

            String command = hadoopdir + "/bin/hadoop dfs -put " + file + " " + dir + "teste";
            log.info("Command: " + command);
            Process putProcess = Runtime.getRuntime().exec(command);
            putProcess.waitFor();

        } catch (RemoteException re) {
            log.info(re.toString());
        } catch (IOException ioe) {
            log.info(ioe.toString());
        } catch (InterruptedException ie) {
            log.info(ie.toString());
        }

    }

    public void deleteFile(String file) {

        try {

            String command = "/bin/rm -Rf " + file;
            log.info("Command: " + command);
            Process putProcess = Runtime.getRuntime().exec(command);
            putProcess.waitFor();

        } catch (IOException ioe) {
            log.info(ioe.toString());
        } catch (InterruptedException ie) {
            log.info(ie.toString());
        }

    }

    public void classLoader(String classPath, String className, String method, String[] arg) {

        File file = new File(classPath); // .class dir

        try {

            //URL classUrl = ClassLoader.getSystemResource(classPath);//file.toURL();
            URL classUrl = file.toURL();
            URL[] classUrls = {classUrl};
            URLClassLoader ucl = new URLClassLoader(classUrls);
            Class c = ucl.loadClass(className);

            for (Method m : c.getDeclaredMethods()) {
                System.out.println(m.getName());
            }

            //ClassLoader loader = URLClassLoader.newInstance(classUrls, this.getClass().getClassLoader());
            //Class c = loader.loadClass(className);

            System.out.println("Teste antes");
            Object o = c.forName(className).newInstance();
            System.out.println("Teste depois");

            if (arg == null) {

                Method m = o.getClass().getMethod(method, null);
                m.invoke(o, null);

            } else {
                //Method m = o.getClass().getMethod(method, new Class[] {String[].class});
                //m.invoke(o, new Object[] {arg});
            }

            //} catch (MalformedURLException mue) {
            //	System.out.println(mue.toString());
        } catch (ClassNotFoundException cnfe) {
            log.info(cnfe.toString());
        } catch (Exception e) {
            log.info(e.toString());
        }
    }



    /*
     * Loading the Lower Tester
     */
    public void lowerTester() throws InterruptedException, RemoteException, IOException {

	readPropertiesHadoop();

	log.info("Starting lower tester...");

         // Connect
        String ltPort = (String) this.get(-30);

        VMAcquirer vma = new VMAcquirer();

	log.info("Trying to connect at remote JPDA server on port " + ltPort);
        vma.connect(Integer.parseInt(ltPort));
	log.info("Debugger connected!");

        // Select class to monitor
        CLASS_NAME = (String) this.get(-31);
        FIELD_NAME = (String) this.get(-32);

	log.info("Class: " + CLASS_NAME + " e Field: " + FIELD_NAME);

        VirtualMachine vm = vma.getVM();
        log.info("aaaa" + vm.name()); 
        List<ReferenceType> referenceTypes = vm.classesByName(CLASS_NAME);

        // Select fields
        for (ReferenceType refType : referenceTypes) {
          addFieldWatch(vm, refType);
        }

        // watch for loaded classes
        addClassWatch(vm);

        log.info("Class " + CLASS_NAME + " has been watching!");

        // resume the vm
        vm.resume();

         // process events
        EventQueue eventQueue = vm.eventQueue();
        while (true) {
          EventSet eventSet = eventQueue.remove();
          for (Event event : eventSet) {
            if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
              // exit
              return;
            } else if (event instanceof ClassPrepareEvent) {
              // watch field on loaded class
              ClassPrepareEvent classPrepEvent = (ClassPrepareEvent) event;
              ReferenceType refType = classPrepEvent.referenceType();
              addFieldWatch(vm, refType);
            } else if (event instanceof ModificationWatchpointEvent) {
              // a Test.foo has changed
              ModificationWatchpointEvent modEvent = (ModificationWatchpointEvent) event;
              System.out.println("old=" + modEvent.valueCurrent());
              System.out.println("new=" + modEvent.valueToBe());
              System.out.println();
              String num = modEvent.valueToBe().toString();

              System.out.println("hahahahahaha");

              int comp = Integer.valueOf(num);

              if (comp == 10) {
                    System.out.println("Suspendendo a execução!");
                    vm.suspend();
              }
            }
          }
          eventSet.resume();
        }
        
    }

    private static void addClassWatch(VirtualMachine vm) {
        EventRequestManager erm = vm.eventRequestManager();
        ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
        classPrepareRequest.addClassFilter(CLASS_NAME);
        classPrepareRequest.setEnabled(true);
    }

    private static void addFieldWatch(VirtualMachine vm,
        ReferenceType refType) {
        EventRequestManager erm = vm.eventRequestManager();
        Field field = refType.fieldByName(FIELD_NAME);
        ModificationWatchpointRequest modificationWatchpointRequest = erm.createModificationWatchpointRequest(field);
        modificationWatchpointRequest.setEnabled(true);
   }

  /*
   *  Fim Lower Tester
   */

  public void sendJob() throws InterruptedException, RemoteException {

    runSendJob sjob = new runSendJob();
    Thread sjThread = new Thread(sjob);
    sjThread.start();
    sjThread.join();

  }

    public class runSendJob implements Runnable {

        public void run() {

            try {

                   Thread.sleep(60000);

                   String hadoopdir = (String) get(-14);

                   String jar = (String) get(-15);
                   String job = (String) get(-16);
                   String param = (String) get(-17);
                   String logFile = (String) get(-33);

                  // String command = hadoopdir + "/bin/hadoop jar " + jar + " " + job + " " + param + " > " + logFile + " 2>";
                   String command = hadoopdir + "/bin/hadoop jar " + jar + " " + job + " " + param;
                   log.info("Running: " + command );
                   Process jobProcess = Runtime.getRuntime().exec(command);
                   jobProcess.waitFor();

                   // Getting waht job output on stdout
                   /*
                   OutputStream ops = new FileOutputStream(logFile);
                   ops = jobProcess.getOutputStream();
                   ByteArrayOutputStream bArray = new ByteArrayOutputStream();
                   ops.write(bArray.toByteArray());
                   String s=bArray.toString();
                   log.info("Job stdout: " + s);
                    *
                    */

                   // Getting Result
                   BufferedReader br = new BufferedReader(new InputStreamReader(jobProcess.getInputStream()));
                   StringBuffer sb = new StringBuffer();
                   String line;
                   String result = "";
                   String[] lineSplitted;

                   while ((line = br.readLine()) != null) {
                        // Splitting the line
                        String regex = (String) get(-34);
                        if (regex.isEmpty() || regex.equals("\" \"") || regex.equals("")) {
                            lineSplitted = line.split(" ");
                        } else {
                            lineSplitted = line.split(regex);
                        }

                        // Comparing the first line word
                        if (lineSplitted[0].equals(new String((String) get(-35)))) {
                            result = lineSplitted[Integer.valueOf((String) get(-36))];
                        }

                        // Append the line to String Buffer
                        sb.append(line).append("\n");
                   }

                   String answer = sb.toString();

                   log.info("Output: " + answer);
                   log.info("Result: " + result);

                   jobResult = BigDecimal.valueOf(Double.valueOf(result));

            } catch (RemoteException re) {
                log.info(re.toString());
            } catch (IOException ioe) {
                log.info(ioe.toString());
            } catch (InterruptedException ie) {
                log.info(ie.toString());
            }
        }
        
    }

    public void validateJobOutput(String outPath, ArrayList expectedResults) {

        try {

            log.info("Validating Job output result!");
            log.log(Level.INFO, "Reading {0}...", outPath);

            Path outputdir = new Path(outPath);

            Path outputFiles[] = FileUtil.stat2Paths(outputdir.getFileSystem(getConfHDFS()).listStatus(outputdir, new OutputLogFilter()));

            int countexpres = 0;

            if (outputFiles.length > 0) {


                for (int ii = 0; ii < outputFiles.length; ii++) {

                    Path file = outputFiles[ii];

                    log.log(Level.INFO, "Reading file {0}...", file);

                    FileSystem hdfs = outputdir.getFileSystem(getConfHDFS());

                    if (hdfs.isFile(file)) {

                        InputStream is = outputdir.getFileSystem(getConfHDFS()).open(file);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                        while (reader.ready()) {
                            Assert.assertTrue(expectedResults.get(countexpres).toString().equals((String) reader.readLine()));
                            countexpres++;
                            log.info("Line " + countexpres + ": " + (String) reader.readLine());
                        }

                        reader.close();
                        is.close();

                    } else {
                        log.log(Level.INFO, "File {0} not found!", file);
                        Assert.fail();
                    }
                }

            } else {
                log.info("No files in " + outPath + "!");
                Assert.fail();
            }


        } catch (IOException ioe) {

            log.info(ioe.toString());

        } catch (InterruptedException ie) {

            log.info(ie.toString());

        }
    }

   

    public class runPiEstimator implements Runnable {

        public void run() {


            try {

                log.info("Starting PiEstimator!");

                PiEstimator pi = new PiEstimator();

                String[] argumentos = {(String) get(-21), (String) get(-22), (String) get(-2), (String) get(-4)};
                pi.run(argumentos);

                jobResult = pi.getResult();
                jobDuration = pi.duration;

            } catch (IOException ioe) {
            } catch (Exception e) {
            }

        } 
    }

    public class runWordCount implements Runnable {

        public void run() {

            try {

                log.info("Starting WordCount!");

                WordCount wc = new WordCount();

              //  String[] argumentos = {(String) get(-23), (String) get(-24), (String) get(-2), (String) get(-4)};
                String[] argumentos = {"/input/","/output/","note","9001"};
                wc.run(argumentos);

            } catch (IOException ioe) {
                log.info(ioe.toString());
            } catch (Exception e) {
                log.info(e.toString());
            }

        }
    }
    }
