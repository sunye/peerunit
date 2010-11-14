package test;

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
import org.apache.hadoop.mapred.JobClient;

import examples.PiEstimator;
import load.StartClusterParent;

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

    @TestStep(order=1, timeout = 100000, range = "0")
    public void startNameNode() throws IOException, InterruptedException {

    	initNN();

   }

    @TestStep(order=2, timeout=100000, range="2")
    public void startSNameNode() throws IOException, InterruptedException {

	initSNN();

    }

    @TestStep(order=3, timeout = 100000, range = "*")
    public void startDataNode() throws IOException, InterruptedException {

	initDN();

    }


    @TestStep(order=4, timeout = 100000, range = "0")
    public void startJobTracker() throws IOException, InterruptedException {

	log.info("Starting JobTracker!");

    //	initJT();

        try {
                String command = "/home/ppginf/michela/hadoop-0.20.2/bin/start-job.sh";
                final Process process = Runtime.getRuntime().exec(command);
        } catch (Exception e) {

		log.warning("Error starting JobTracker:");

                log.warning(e.toString());

        }
    }

    @TestStep(order=5, timeout = 100000, range = "*")
    public void startTaskTracker() throws IOException, InterruptedException {

	log.info("Starting Task Tracker!");

	try {
                String command = "/home/ppginf/michela/hadoop-0.20.2/bin/start-task.sh";
                final Process process = Runtime.getRuntime().exec(command);
        } catch (Exception e) {

		log.warning("Error starting TaskTracker:");

                log.warning(e.toString());

        }

	// initTT();

   }

   @TestStep(order=6, timeout = 440000, range = "0")
   public void sendJob() throws IOException, InterruptedException, RemoteException, Exception {

	runPiEstimator piest = new runPiEstimator();

	piest.run();

   }

   @TestStep(order=7, timeout = 100000, range = "*")
   public void stopCluster() throws IOException, InterruptedException, RemoteException {


   }

}
