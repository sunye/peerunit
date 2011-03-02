package test;

/**
 * @author albonico  
 */

// My classes
//import examples.PiEstimator;
import load.StartClusterParent;
import util.ThreadUtilities;

// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.parser.AfterClass;

// Java classes
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.math.BigDecimal;

public class TestStartCluster extends StartClusterParent {

	private static Thread JTT;

	@TestStep(order=1, timeout = 10000, range = "0")
    public void startNameNode() throws IOException, InterruptedException {

    	initNN();

    }
	

/*	

    @TestStep(order=2, timeout=100000, range="1")
    public void startSNameNode() throws IOException, InterruptedException {

    	initSNN();

    }
    
 */

    @TestStep(order=2, timeout = 100000, range = "*")
    public void startDataNode() throws IOException, InterruptedException {

    	initDN();

    }

    @TestStep(order=3, timeout = 100000, range = "0")
    public void startJobTracker() throws Exception {
    	
    	initJT();
    	
   } 

    
  //  @TestStep(order=4, timeout = 100000, range = "0,1,3")
   @TestStep(order=4, timeout = 100000, range = "0")
    public void startTaskTracker() throws Exception {

	 	initTT();

    }
    
    /*
  
    @TestStep(order=5, timeout = 440000, range = "0")
    public void sendJob() throws Exception, InterruptedException {

    	runJob();
    	
    	log.info("Job result: " + jobResult);
    	
    }
    
    */
    
    @TestStep(order=6, timeout=10000, range="*")
    public void stopSlaveServices() throws IOException, InterruptedException {
    
    	log.info("Stopping Datanode...");
    	dn.shutdown();
    	dnThread.interrupt();
    	
    	log.info("Stopping TaskTracker...");
    	ttProcess.destroy();
    	ttThread.interrupt();
    	
    }
    	
    @TestStep(order=7, timeout=10000, range="0")
    public void stopMasterServices() throws IOException, InterruptedException {
    	
    	log.info("Stopping JobTracker...");
    	jtProcess.destroy();
    	jtThread.interrupt();
    	
    	log.info("Stopping NameNode...");
    	nn.stop();
    	nnThread.interrupt();
    	
    }
    /*
    @AfterClass(range="*", timeout = 100000)
    public void stopCluster() throws IOException {
    	log.info("End of test case!");
    	
    	//Runtime rt = Runtime.getRuntime();
    	//log.info("Available processes: " + rt.availableProcessors());
    }
    */

}
