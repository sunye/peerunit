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
import org.apache.hadoop.examples.PiEstimator;

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
import java.math.BigDecimal;

public class TestStartCluster extends StartClusterParent {

    public static Thread ttThread;
    public static startTaskTracker tasktracker;

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

    @TestStep(order = 1, timeout = 100000, range = "0")
    public void startNN() throws IOException, InterruptedException {
	NameNode nnode = startNameNode();
    }

    @TestStep(order = 2, timeout = 100000, range = "0")
    public void startJT() throws IOException, InterruptedException {
	startJobTracker();
    }

    @TestStep(order = 3, timeout = 100000, range = "*")
    public void startSlaves() throws IOException, InterruptedException {

	tasktracker = new startTaskTracker();
        ttThread = new Thread( tasktracker );
	ttThread.start();
	ttThread.sleep(5000);
	//TaskTracker tasktracker = startTaskTracker();
	startDataNode();
    
	}

   @TestStep(order = 5, timeout = 100000, range = "0")
   public void runJob() throws IOException, InterruptedException, RemoteException, Exception {

	Configuration config = getConfMR();

	// run PiEstimator
	PiEstimator pi = new PiEstimator();

	pi.setConf(config);

	// pi.configure(job);

	String[] argumentos = {"2","4"};

	pi.run(argumentos);
	

//	BigDecimal piresult = pi.estimate(2,4,job);

   }

   @TestStep(order = 4, timeout = 100000, range = "*")
   public void stopCluster() throws IOException, InterruptedException, RemoteException {


   }

}
