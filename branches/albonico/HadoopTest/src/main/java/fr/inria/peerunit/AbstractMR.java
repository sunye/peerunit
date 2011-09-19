package fr.inria.peerunit;

/**
 * @author albonico
 *
 */

/*
 * MapReduce Classes
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputLogFilter;

import fr.inria.peerunit.parser.BeforeClass;
import fr.inria.peerunit.parser.SetGlobals;
import fr.inria.peerunit.parser.SetId;
import fr.inria.peerunit.remote.GlobalVariables;
import fr.inria.peerunit.tester.Assert;
import fr.inria.peerunit.util.TesterUtil;

public abstract class AbstractMR {

    // PeerUnit vars
    private static final Logger LOG = Logger.getLogger(AbstractMR.class.getName());
    private int id;
    private GlobalVariables globals;
    private TesterUtil defaults;
    private int sleep;
    private static JobConf job;
    private static BigDecimal jobResult;
    private static double jobDuration;
    private static Process nnProcess;
    private static Process dnProcess;
    private static Process jtProcess;
    private static Process ttProcess;
    private static StartNameNodeByHadoop nnodeHadoop;
    private static Thread nnThread;
    private static Thread dnThread;
    private static Thread jtThread;
    private static Thread ttThread;
    private HadoopJobTrackerWrapper master;
    private HadoopTaskTrackerWrapper taskTracker;
    private HadoopDataNodeWrapper dataNode;

    
    @BeforeClass(range = "*", timeout = 10000)
    public void bc() throws IOException, FileNotFoundException,
            InterruptedException {
        
            String name = (String) get(-5);
        String data = (String) get(-6);
        Configuration config = this.getConfMR();
        Configuration hdfsConf = this.getConfHDFS();
        taskTracker = new HadoopTaskTrackerWrapper(config);
        dataNode = new HadoopDataNodeWrapper(hdfsConf, name, data);
        master = new HadoopJobTrackerWrapper(this);
        
        
        setPeerUnitProperties();
        setHadoopProperties();
    }
    
    
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



    /*
     * PeerUnit Properties
     */
    private void setPeerUnitProperties() throws FileNotFoundException,
            IOException, InterruptedException {
        if (new File("peerunit.properties").exists()) {
            String filename = "peerunit.properties";
            FileInputStream fs = new FileInputStream(filename);
            defaults = new TesterUtil(fs);
        } else {
            defaults = TesterUtil.instance;
        }
        // size = defaults.getObjects();
        sleep = defaults.getSleep();
        // instanceNumber = defaults.getObjects();
    }

    /**
     * Reading Hadoop Properties (hadoop.properties)
     * 
     * @TODO: Why is this method synchronized?
     */
    synchronized public void setHadoopProperties() throws IOException,
            InterruptedException {
        LOG.info("Reading Hadoop properties!");

        Properties properties = new Properties();
        File file = new File("hadoop.properties");
        FileInputStream fis = null;
        fis = new FileInputStream(file);
        properties.load(fis);

        /**
         * JobTracker and NameNode Properties
         */
        String nnaddr = properties.getProperty("hadoop.namenode");
        this.put(-1, nnaddr);
        String jtaddr = properties.getProperty("hadoop.jobtracker");
        this.put(-2, jtaddr);
        String nnport = properties.getProperty("hadoop.namenode.port");
        this.put(-3, nnport);
        String jtport = properties.getProperty("hadoop.jobtracker.port");
        this.put(-4, jtport);

        /**
         * DFS Properties
         */
        String dfsname = properties.getProperty("hadoop.dir.name");
        this.put(-5, dfsname);
        String dfsdata = properties.getProperty("hadoop.dir.data");
        this.put(-6, dfsdata);
        String hadooptmp = properties.getProperty("hadoop.dir.tmp");
        this.put(-7, hadooptmp);
        String dfssnn = properties.getProperty("hadoop.dir.secnn");
        this.put(-8, dfssnn);

        /*
         * Hadoop Properties
         */
        String hadooplog = properties.getProperty("hadoop.dir.log");
        this.put(-9, hadooplog);
        String hadooprep = properties.getProperty("hadoop.dfs.replication");
        this.put(-10, hadooprep);
        String version = properties.getProperty("hadoop.version");
        this.put(-13, version);
        String hadoopdir = properties.getProperty("hadoop.dir.install");
        this.put(-14, hadoopdir);


        /*
         * JVM Properties
         */
        String javaopt = properties.getProperty("hadoop.java.options");
        this.put(-11, javaopt);
        String memtask = properties.getProperty("mapred.child.java.opts");
        this.put(-12, memtask);

        /*
         * Mutants Generation Properties
         */
        String mutantclass = properties.getProperty("mutant.class");
        this.put(-18, mutantclass);
        String mutantoutputdir = properties.getProperty("mutant.output.dir");
        this.put(-19, mutantoutputdir);

        /*
         * Custom PiEstimator Properties
         */
        String pivalue = properties.getProperty("pi.value");
        this.put(-20, pivalue);
        String pinmaps = properties.getProperty("pi.nMaps");
        this.put(-21, pinmaps);
        String pinsamples = properties.getProperty("pi.nSamples");
        this.put(-22, pinsamples);

        /*
         * Custom WordCount Properties
         */
        String inputdir = properties.getProperty("wordcount.input");
        this.put(-23, inputdir);
        String outputdir = properties.getProperty("wordcount.output");
        this.put(-24, outputdir);
        String wfile = properties.getProperty("wordcount.file");
        this.put(-25, wfile);
        String wcsleep = properties.getProperty("wordcount.sleep");
        this.put(-26, wcsleep);

        /*
         * External Classes Execution Properties
         */
        String jobJar = properties.getProperty("job.jar");
        this.put(-15, jobJar);
        String jobClass = properties.getProperty("job.class");
        this.put(-16, jobClass);
        String jobParameters = properties.getProperty("job.parameters");
        this.put(-17, jobParameters);

        /*
         * Lower Tester Properties
         */
        String ltPort = properties.getProperty("lower.tester.port");
        this.put(-30, ltPort);
        String ltClass = properties.getProperty("lower.tester.class");
        this.put(-31, ltClass);
        String ltField = properties.getProperty("lower.tester.field");
        this.put(-32, ltField);

        /*
         * Logging Job Results Properties
         */
        String resultFile = properties.getProperty("job.result.logfile");
        this.put(-33, resultFile);
        String regexChar = properties.getProperty("job.result.regex");
        this.put(-34, regexChar);
        String likeWord = properties.getProperty("job.result.like");
        this.put(-35, likeWord);
        String resultPosition = properties.getProperty("job.result.position");
        this.put(-36, resultPosition);

        /*
         * DFS Formatting Properties
         */
        String scriptFormat = properties.getProperty("hadoop.dir.format.script");
        this.put(-37, scriptFormat);
        String hadooptestDir = properties.getProperty("hadooptest.dir");
        this.put(-38, hadooptestDir);

        /*
         * Expected result file
         */
        String expectedResultFile = properties.getProperty("expected.result.file");
        this.put(-39, expectedResultFile);
        String resultPath = properties.getProperty("result.path");
        this.put(-40, resultPath);
        String assertType = properties.getProperty("job.result.assert.type");
        this.put(-43, assertType);

        /*
         * Input external file
         */
        String externalFile = properties.getProperty("job.input.file");
        this.put(-41, externalFile);
        String inputDir = properties.getProperty("job.input.dir");
        this.put(-42, inputDir);




    }

    /*
     * Getting Configurations to MapReduce Components
     */
    public Configuration getConfMR() throws IOException, InterruptedException {
        LOG.info("Reading MR configuration!");

        Configuration conf = new Configuration();
        String jthost = this.get(-2) + ":" + this.get(-4);
        jthost = (String) jthost;
        String joptions = (String) this.get(-11);
        String memtask = (String) this.get(-12);

        conf.set("mapred.job.tracker", jthost);
        conf.set("mapred.child.java.opts", joptions);
        conf.set("mapred.child.java.opts", memtask);

        return conf;
    }

    /*
     * Getting Configurations to HDFS Components
     */
    public Configuration getConfHDFS() throws IOException, InterruptedException {
        LOG.info("Reading HDFS configuration!");

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

    /*
     * DFS manipulations methods
     */
    private void dfsFormatting(String dirHadoopTest) throws RemoteException,
            IOException, InterruptedException {
        LOG.info("Formatting DFS dir: " + dirHadoopTest + "!");
        String scriptFormat = (String) get(-37) + " " + dirHadoopTest;
        Process formatDFSProcess = Runtime.getRuntime().exec(scriptFormat);
        formatDFSProcess.waitFor();
    }

    protected void putFileHDFS() {
        try {
            LOG.info("Putting file on HDFS!");
            String hadoopdir = (String) get(-14);
            String externalFile = (String) get(-41);
            String inputDir = (String) get(-42);

            String command = hadoopdir + "/bin/hadoop dfs -put " + externalFile
                    + " " + inputDir + "teste";
            LOG.info("Command: " + command);
            Process putProcess = Runtime.getRuntime().exec(command);
            putProcess.waitFor();
        } catch (RemoteException re) {
            LOG.info(re.toString());
        } catch (IOException ioe) {
            LOG.info(ioe.toString());
        } catch (InterruptedException ie) {
            LOG.info(ie.toString());
        }
    }

    /*
     * Sending Jobs
     */
    protected void sendJob() throws InterruptedException, RemoteException {
        /*
        14 - hadoopdir
        15 - jobJar
        16 - jobClass
        17 - jobParameters
        34 - resultRegex
        35 - resultLike
        36 - resultPosition
        43 - assertType	
         */

        String hadoopdir = (String) get(-14);
        String jar = (String) get(-15);
        String job = (String) get(-16);
        // String param = get(-21) + " " + get(-22);
        String param = (String) get(-17);
        String command = hadoopdir + "bin/hadoop jar " + jar + " "
                + job + " " + param;
        String regex = (String) get(-34);

        RunSendJob sjob = new RunSendJob(Integer.valueOf((String) get(-43)), command, regex,
                ((String) get(-35)), Integer.valueOf((String) get(-36)));

        Thread sjThread = new Thread(sjob);
        sjThread.start();
        Thread.sleep(1000);
        sjThread.join();
    }

    private class RunSendJob implements Runnable {

        private final int assertType;
        private final String command;
        private final String regex;
        private final String resultLike;
        private final int resultPosition;

        public RunSendJob(int assertType, String command, String regex, String resultLike,
                int resultPosition) {

            this.assertType = assertType;
            this.command = command;
            this.regex = regex;
            this.resultLike = resultLike;
            this.resultPosition = resultPosition;
        }

        /**
         * @FIXME
         * 
         * This method calls the script "hadoop", which launches a new JVM, 
         * which executes the class RunJar, which unjars the Hadoop job and
         * calls the main() method of the job.
         * Since we already know the job, why don't we call the main() method
         * directly? Am I missing something ?
         */
        public void run() {
            try {

                LOG.info("Running: " + command);
                Process jobProcess = Runtime.getRuntime().exec(command);
                jobProcess.waitFor();
                // If asserting stdout
                if (assertType == 1) {
                    // Getting Result
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(jobProcess.getInputStream()));
                    StringBuffer sb = new StringBuffer();
                    String line;
                    String result = "";
                    String[] lineSplitted;

                    while ((line = br.readLine()) != null) {
                        // Splitting the line

                        if (regex.isEmpty() || regex.equals("\" \"")
                                || regex.equals("")) {
                            lineSplitted = line.split(" ");
                        } else {
                            lineSplitted = line.split(regex);
                        }
                        // Comparing the first line word
                        if (lineSplitted[0].equals(resultLike)) {
                            result = lineSplitted[resultPosition];
                        }
                        // Append the line to String Buffer
                        sb.append(line).append("\n");
                    }

                    String answer = sb.toString();

                    LOG.info("Output: " + answer);
                    LOG.info("Result: " + result);

                    jobResult = BigDecimal.valueOf(Double.valueOf(result));
                }
            } catch (RemoteException re) {
                LOG.info(re.toString());
            } catch (IOException ioe) {
                LOG.info(ioe.toString());
            } catch (InterruptedException ie) {
                LOG.info(ie.toString());
            }
        }
    }

    protected void startMaster() throws RemoteException, IOException,
            InterruptedException {
        // Formatting DFS dir
        dfsFormatting((String) get(-38));
        master.startMaster();
    }

    protected void stopMaster() throws IOException {
        master.stopMaster();
    }

    protected void startWorkers() throws IOException, InterruptedException {
        taskTracker.start();
        dataNode.start();
    }

    protected void stopWorkers() throws IOException {
        taskTracker.stop();
        dataNode.stop();
    }

    /**
     * Starting Hadoop by Hadoop binaries
     * 
     * @TODO: Remove this code. Next time, use the ANT classes to start another
     *        JVM.
     * 
     */
    @Deprecated
    private class StartDataNodeByHadoop implements Runnable {

        public void run() {
            try {
                // Hadoop dir
                String hadoopdir = (String) get(-14);

                String command = "java -Xmx1000m -Dcom.sun.management.jmxremote"
                        + " -Dcom.sun.management.jmxremote"
                        + " -Dhadoop.log.dir="
                        + hadoopdir
                        + "/logs"
                        + " -Dhadoop.log.file=hadoop-jobtracker.log"
                        + " -Dhadoop.home.dir="
                        + hadoopdir
                        + " -Dhadoop.id.str=michel -Dhadoop.root.logger=INFO,DRFA"
                        + " -Djava.library.path="
                        + hadoopdir
                        + "/lib/native/Linux-i386-32"
                        + " -Dhadoop.policy.file=hadoop-policy.xml"
                        + " -classpath "
                        + hadoopdir
                        + "/conf:"
                        + "/usr/lib/jvm/java-6-sun/lib/tools.jar:"
                        + hadoopdir
                        + ":"
                        + hadoopdir
                        + "/hadoop-0.20.2-core.jar:"
                        + hadoopdir
                        + "/lib/commons-cli-1.2.jar:"
                        + hadoopdir
                        + "/lib/commons-codec-1.3.jar:"
                        + hadoopdir
                        + "/lib/commons-el-1.0.jar:"
                        + hadoopdir
                        + "/lib/commons-httpclient-3.0.1.jar:"
                        + hadoopdir
                        + "/lib/commons-logging-1.0.4.jar:"
                        + hadoopdir
                        + "/lib/commons-logging-api-1.0.4.jar:"
                        + hadoopdir
                        + "/lib/commons-net-1.4.1.jar:"
                        + hadoopdir
                        + "/lib/core-3.1.1.jar:"
                        + hadoopdir
                        + "/lib/hsqldb-1.8.0.10.jar:"
                        + hadoopdir
                        + "/lib/jasper-compiler-5.5.12.jar:"
                        + hadoopdir
                        + "/lib/jasper-runtime-5.5.12.jar:"
                        + hadoopdir
                        + "/lib/jets3t-0.6.1.jar:"
                        + hadoopdir
                        + "/lib/jetty-6.1.14.jar:"
                        + hadoopdir
                        + "/lib/jetty-util-6.1.14.jar:"
                        + hadoopdir
                        + "/lib/junit-3.8.1.jar:"
                        + hadoopdir
                        + "/lib/kfs-0.2.2.jar:"
                        + hadoopdir
                        + "/lib/log4j-1.2.15.jar:"
                        + hadoopdir
                        + "/lib/mockito-all-1.8.0.jar:"
                        + hadoopdir
                        + "/lib/oro-2.0.8.jar:"
                        + hadoopdir
                        + "/lib/servlet-api-2.5-6.1.14.jar:"
                        + hadoopdir
                        + "/lib/slf4j-api-1.4.3.jar:"
                        + hadoopdir
                        + "/lib/slf4j-log4j12-1.4.3.jar:"
                        + hadoopdir
                        + "/lib/xmlenc-0.52.jar:"
                        + hadoopdir
                        + "/lib/jsp-2.1/jsp-2.1.jar:"
                        + hadoopdir
                        + "/lib/jsp-2.1/jsp-api-2.1.jar "
                        + "org.apache.hadoop.hdfs.server.datanode.DataNode";
                dnProcess = Runtime.getRuntime().exec(command);
                try {
                    dnProcess.waitFor();
                } catch (InterruptedException ex) {
                    LOG.warning(ex.toString());
                }
            } catch (IOException ioe) {
                LOG.warning(ioe.toString());
            }
        }
    }

    @Deprecated
    private Thread initNNByHadoop() throws IOException, InterruptedException {
        LOG.info("Starting NameNode!");

        nnodeHadoop = new StartNameNodeByHadoop();
        Thread nnT = new Thread(nnodeHadoop);

        return nnT;
    }

    /**
     * 
     * @author sunye
     * @TODO: Remove this code. Next time, use the ANT classes to start another
     *        JVM.
     */
    @Deprecated
    private class StartNameNodeByHadoop implements Runnable {

        public void run() {
            try {
                // Hadoop dir
                String hadoopdir = (String) get(-14);

                String command = "java -Xmx1000m -Dcom.sun.management.jmxremote"
                        + " -Dcom.sun.management.jmxremote"
                        + " -Dhadoop.log.dir="
                        + hadoopdir
                        + "/logs"
                        + " -Dhadoop.log.file=hadoop-jobtracker.log"
                        + " -Dhadoop.home.dir="
                        + hadoopdir
                        + " -Dhadoop.id.str=michel -Dhadoop.root.logger=INFO,DRFA"
                        + " -Djava.library.path="
                        + hadoopdir
                        + "/lib/native/Linux-i386-32"
                        + " -Dhadoop.policy.file=hadoop-policy.xml"
                        + " -classpath "
                        + hadoopdir
                        + "/conf:"
                        + "/usr/lib/jvm/java-6-sun/lib/tools.jar:"
                        + hadoopdir
                        + ":"
                        + hadoopdir
                        + "/hadoop-0.20.2-core.jar:"
                        + hadoopdir
                        + "/lib/commons-cli-1.2.jar:"
                        + hadoopdir
                        + "/lib/commons-codec-1.3.jar:"
                        + hadoopdir
                        + "/lib/commons-el-1.0.jar:"
                        + hadoopdir
                        + "/lib/commons-httpclient-3.0.1.jar:"
                        + hadoopdir
                        + "/lib/commons-logging-1.0.4.jar:"
                        + hadoopdir
                        + "/lib/commons-logging-api-1.0.4.jar:"
                        + hadoopdir
                        + "/lib/commons-net-1.4.1.jar:"
                        + hadoopdir
                        + "/lib/core-3.1.1.jar:"
                        + hadoopdir
                        + "/lib/hsqldb-1.8.0.10.jar:"
                        + hadoopdir
                        + "/lib/jasper-compiler-5.5.12.jar:"
                        + hadoopdir
                        + "/lib/jasper-runtime-5.5.12.jar:"
                        + hadoopdir
                        + "/lib/jets3t-0.6.1.jar:"
                        + hadoopdir
                        + "/lib/jetty-6.1.14.jar:"
                        + hadoopdir
                        + "/lib/jetty-util-6.1.14.jar:"
                        + hadoopdir
                        + "/lib/junit-3.8.1.jar:"
                        + hadoopdir
                        + "/lib/kfs-0.2.2.jar:"
                        + hadoopdir
                        + "/lib/log4j-1.2.15.jar:"
                        + hadoopdir
                        + "/lib/mockito-all-1.8.0.jar:"
                        + hadoopdir
                        + "/lib/oro-2.0.8.jar:"
                        + hadoopdir
                        + "/lib/servlet-api-2.5-6.1.14.jar:"
                        + hadoopdir
                        + "/lib/slf4j-api-1.4.3.jar:"
                        + hadoopdir
                        + "/lib/slf4j-log4j12-1.4.3.jar:"
                        + hadoopdir
                        + "/lib/xmlenc-0.52.jar:"
                        + hadoopdir
                        + "/lib/jsp-2.1/jsp-2.1.jar:"
                        + hadoopdir
                        + "/lib/jsp-2.1/jsp-api-2.1.jar "
                        + "org.apache.hadoop.hdfs.server.namenode.NameNode";
                nnProcess = Runtime.getRuntime().exec(command);
                try {
                    nnProcess.waitFor();
                } catch (InterruptedException ex) {
                    LOG.warning(ex.toString());
                }
            } catch (IOException ioe) {
                LOG.warning(ioe.toString());
            }
        }
    }

    @Deprecated
    private Thread initDNByHadoop() throws IOException, InterruptedException {
        LOG.info("Starting DataNode!");

        StartDataNodeByHadoop datanode = new StartDataNodeByHadoop();
        Thread dnT = new Thread(datanode);

        return dnT;
    }

    /**
     * 
     * @author sunye
     * @TODO: Remove this code. Next time, use the ANT classes to start another
     *        JVM.
     */
    private class StartJobTrackerByHadoop implements Runnable {

        public void run() {
            try {
                LOG.info("Starting JobTracker!");

                String hadoopdir = (String) get(-14);

                String command = "java -Xmx1000m -Dcom.sun.management.jmxremote"
                        + " -Dcom.sun.management.jmxremote"
                        + " -Dhadoop.log.dir="
                        + hadoopdir
                        + "/logs"
                        + " -Dhadoop.log.file=hadoop-jobtracker.log"
                        + " -Dhadoop.home.dir="
                        + hadoopdir
                        + " -Dhadoop.id.str=michel -Dhadoop.root.logger=INFO,DRFA"
                        + " -Djava.library.path="
                        + hadoopdir
                        + "/lib/native/Linux-i386-32"
                        + " -Dhadoop.policy.file=hadoop-policy.xml"
                        + " -classpath "
                        + hadoopdir
                        + "/conf:"
                        + "/usr/lib/jvm/java-6-sun/lib/tools.jar:"
                        + hadoopdir
                        + ":"
                        + hadoopdir
                        + "/hadoop-0.20.2-core.jar:"
                        + hadoopdir
                        + "/lib/commons-cli-1.2.jar:"
                        + hadoopdir
                        + "/lib/commons-codec-1.3.jar:"
                        + hadoopdir
                        + "/lib/commons-el-1.0.jar:"
                        + hadoopdir
                        + "/lib/commons-httpclient-3.0.1.jar:"
                        + hadoopdir
                        + "/lib/commons-logging-1.0.4.jar:"
                        + hadoopdir
                        + "/lib/commons-logging-api-1.0.4.jar:"
                        + hadoopdir
                        + "/lib/commons-net-1.4.1.jar:"
                        + hadoopdir
                        + "/lib/core-3.1.1.jar:"
                        + hadoopdir
                        + "/lib/hsqldb-1.8.0.10.jar:"
                        + hadoopdir
                        + "/lib/jasper-compiler-5.5.12.jar:"
                        + hadoopdir
                        + "/lib/jasper-runtime-5.5.12.jar:"
                        + hadoopdir
                        + "/lib/jets3t-0.6.1.jar:"
                        + hadoopdir
                        + "/lib/jetty-6.1.14.jar:"
                        + hadoopdir
                        + "/lib/jetty-util-6.1.14.jar:"
                        + hadoopdir
                        + "/lib/junit-3.8.1.jar:"
                        + hadoopdir
                        + "/lib/kfs-0.2.2.jar:"
                        + hadoopdir
                        + "/lib/log4j-1.2.15.jar:"
                        + hadoopdir
                        + "/lib/mockito-all-1.8.0.jar:"
                        + hadoopdir
                        + "/lib/oro-2.0.8.jar:"
                        + hadoopdir
                        + "/lib/servlet-api-2.5-6.1.14.jar:"
                        + hadoopdir
                        + "/lib/slf4j-api-1.4.3.jar:"
                        + hadoopdir
                        + "/lib/slf4j-log4j12-1.4.3.jar:"
                        + hadoopdir
                        + "/lib/xmlenc-0.52.jar:"
                        + hadoopdir
                        + "/lib/jsp-2.1/jsp-2.1.jar:"
                        + hadoopdir
                        + "/lib/jsp-2.1/jsp-api-2.1.jar "
                        + "org.apache.hadoop.mapred.JobTracker";

                jtProcess = Runtime.getRuntime().exec(command);
                try {
                    jtProcess.waitFor();
                } catch (InterruptedException ex) {
                    LOG.warning(ex.toString());
                }
            } catch (IOException ioe) {
                LOG.warning(ioe.toString());
            }
        }
    }

    @Deprecated
    private Thread initJTByHadoop() throws IOException, InterruptedException {
        LOG.info("Starting JobTracker!");

        StartJobTrackerByHadoop jtracker = new StartJobTrackerByHadoop();
        Thread jtT = new Thread(jtracker);

        return jtT;
    }

    /**
     * 
     * @author sunye
     * @TODO: Remove this code. Next time, use the ANT classes to start another
     *        JVM.
     */
    @Deprecated
    private class StartTaskTrackerByHadoop implements Runnable {

        public void run() {
            try {

                String hadoopdir = (String) get(-14);

                // String command =
                // "/home/michel/hadoop-0.20.2/bin/start-track.sh";
                String command = "java -Xmx1000m" + " -Dhadoop.log.dir="
                        + hadoopdir
                        + "/logs"
                        + " -Dhadoop.log.file=hadoop-michel-tasktracker-note.log"
                        + " -Dhadoop.home.dir="
                        + hadoopdir
                        + " -Dhadoop.id.str=michel -Dhadoop.root.logger=INFO,DRFA"
                        + " -Djava.library.path=" + hadoopdir
                        + "/lib/native/Linux-i386-32"
                        + " -Dhadoop.policy.file=hadoop-policy.xml"
                        + " -classpath " + hadoopdir + "/conf:"
                        + "/usr/lib/jvm/java-6-sun/lib/tools.jar:" + hadoopdir
                        + ":" + hadoopdir + "/hadoop-0.20.2-core.jar:"
                        + hadoopdir + "/lib/commons-cli-1.2.jar:" + hadoopdir
                        + "/lib/commons-codec-1.3.jar:" + hadoopdir
                        + "/lib/commons-el-1.0.jar:" + hadoopdir
                        + "/lib/commons-httpclient-3.0.1.jar:" + hadoopdir
                        + "/lib/commons-logging-1.0.4.jar:" + hadoopdir
                        + "/lib/commons-logging-api-1.0.4.jar:" + hadoopdir
                        + "/lib/commons-net-1.4.1.jar:" + hadoopdir
                        + "/lib/core-3.1.1.jar:" + hadoopdir
                        + "/lib/hsqldb-1.8.0.10.jar:" + hadoopdir
                        + "/lib/jasper-compiler-5.5.12.jar:" + hadoopdir
                        + "/lib/jasper-runtime-5.5.12.jar:" + hadoopdir
                        + "/lib/jets3t-0.6.1.jar:" + hadoopdir
                        + "/lib/jetty-6.1.14.jar:" + hadoopdir
                        + "/lib/jetty-util-6.1.14.jar:" + hadoopdir
                        + "/lib/junit-3.8.1.jar:" + hadoopdir
                        + "/lib/kfs-0.2.2.jar:" + hadoopdir
                        + "/lib/log4j-1.2.15.jar:" + hadoopdir
                        + "/lib/mockito-all-1.8.0.jar:" + hadoopdir
                        + "/lib/oro-2.0.8.jar:" + hadoopdir
                        + "/lib/servlet-api-2.5-6.1.14.jar:" + hadoopdir
                        + "/lib/slf4j-api-1.4.3.jar:" + hadoopdir
                        + "/lib/slf4j-log4j12-1.4.3.jar:" + hadoopdir
                        + "/lib/xmlenc-0.52.jar:" + hadoopdir
                        + "/lib/jsp-2.1/jsp-2.1.jar:" + hadoopdir
                        + "/lib/jsp-2.1/jsp-api-2.1.jar"
                        + " org.apache.hadoop.mapred.TaskTracker";
                ttProcess = Runtime.getRuntime().exec(command);
                try {
                    ttProcess.waitFor();
                } catch (InterruptedException ex) {
                    LOG.warning(ex.toString());
                }
            } catch (IOException ioe) {
                LOG.warning(ioe.toString());
            }
        }
    }

    @Deprecated
    private Thread initTTByHadoop() throws IOException, InterruptedException {
        LOG.info("Starting TaskTracker!");

        StartTaskTrackerByHadoop ttracker = new StartTaskTrackerByHadoop();
        Thread ttT = new Thread(ttracker);

        return ttT;
    }

    @Deprecated
    protected void startMasterByHadoop() throws RemoteException, IOException,
            InterruptedException {
        // Formatting DFS dir
        dfsFormatting((String) get(-38));

        // NameNode
        nnThread = initNNByHadoop();
        nnThread.start();
        Thread.sleep(sleep);
        Thread.yield();

        // JobTracker
        jtThread = initJTByHadoop();
        jtThread.start();
        Thread.sleep(10000);
        jtThread.join();
    }

    @Deprecated
    protected void startSlavesByHadoop() throws IOException,
            InterruptedException {
        // DataNodes
        dnThread = initDNByHadoop();
        dnThread.start();
        Thread.sleep(10000);
        dnThread.join();

        // TaskTrackers
        ttThread = initTTByHadoop();
        ttThread.start();
        Thread.sleep(10000);
        ttThread.join();
    }

    @Deprecated
    protected void stopSlavesByHadoop() {
        // DataNodes
        LOG.info("Stopping Datanode...");
        dnProcess.destroy();

        // TaskTrackers
        LOG.info("Stopping TaskTracker...");
        ttProcess.destroy();
    }

    @Deprecated
    protected void stopMasterByHadoop() {
        // JobTracker
        LOG.info("Stopping JobTracker...");
        jtProcess.destroy();

        // NameNode
        LOG.info("Stopping NameNode...");
        nnProcess.destroy();
    }

    /*
     * Asserting results
     */
    protected void assertResult() throws RemoteException {
        LOG.info("Asserting job output result!");

        int type = Integer.valueOf((String) get(-43));

        switch (type) {
            case 1:
                validateJobOutputInStdout();
                break;
            case 2:
                validateJobOutputInDir();
                break;
            default:
                validateJobOutputInStdout();
                break;
        }
    }

    private void validateJobOutputInStdout() throws RemoteException {
        if (jobResult != null) {
            String pivalue = (String) get(-20);
            BigDecimal expected;
            expected = BigDecimal.valueOf(Double.valueOf(pivalue));

            System.out.println("expected:" + expected + "  jobResult:"
                    + jobResult + "  duration:" + jobDuration);
            LOG.info("expected:" + expected + "  jobResult:" + jobResult
                    + "  duration:" + jobDuration);
            Assert.assertTrue(expected.equals(jobResult));
        } else {
            LOG.info("jobResult is NULL!");
            Assert.fail();
        }
    }

    private void validateJobOutputInDir() {
        try {
            LOG.info("Reading expected result file...");
            List<String> expectedResults = new ArrayList<String>();
            if (new File((String) get(-39)).canRead()) {
                FileInputStream expectedFile = new FileInputStream(
                        (String) get(-39));
                BufferedReader readerExpected = new BufferedReader(
                        new InputStreamReader(expectedFile));
                String lineExpected = "";
                while ((lineExpected = readerExpected.readLine()) != null) {
                    expectedResults.add(lineExpected);
                    // log.info("Expected: " + lineExpected);
                }
                readerExpected.close();
                expectedFile.close();
            }

            String outPath = (String) get(-40);
            LOG.log(Level.INFO, "Reading {0}...", outPath);
            Path outputdir = new Path(outPath);
            Path outputFiles[] = FileUtil.stat2Paths(outputdir.getFileSystem(getConfHDFS()).listStatus(outputdir, new OutputLogFilter()));
            int countexpres = 0;

            if (outputFiles.length > 0) {
                for (int ii = 0; ii < outputFiles.length; ii++) {
                    Path file = outputFiles[ii];
                    LOG.log(Level.INFO, "Reading file {0}...", file);
                    FileSystem hdfs = outputdir.getFileSystem(getConfHDFS());
                    if (hdfs.isFile(file)) {
                        InputStream is = outputdir.getFileSystem(getConfHDFS()).open(file);
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(is));
                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            Assert.assertTrue(expectedResults.get(countexpres).toString().equals(line));
                            countexpres++;
                            LOG.info("Line " + countexpres + ": " + line);
                        }
                        reader.close();
                        is.close();
                    } else {
                        LOG.log(Level.INFO, "File {0} not found!", file);
                        Assert.fail();
                    }
                }
            } else {
                LOG.info("No files in " + outPath + "!");
                Assert.fail();
            }
        } catch (IOException ioe) {
            LOG.info(ioe.toString());
        } catch (InterruptedException ie) {
            LOG.info(ie.toString());
        }
    }
}
