package fr.inria.peerunit;

/**
 * @author albonico  
 */

// My classes
//import examples.PiEstimator;
//import load.StartClusterParent;

// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.tester.Assert;

// Java classes
import fr.inria.peerunit.StartClusterParent;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.math.BigDecimal;

public class TestStartCluster extends StartClusterParent {

	
	@TestStep(order=1, timeout = 60000, range = "0")
    public void startNameNode() throws IOException, InterruptedException {

    	initNN();

    }
	

    @TestStep(order=2, timeout = 100000, range = "*")
    public void startDataNode() throws IOException, InterruptedException {

    	initDN();

    }

    @TestStep(order=3, timeout = 100000, range = "0")
    public void startJobTracker() throws Exception {
    	
    	initJT();
    	
   } 

   @TestStep(order=4, timeout = 100000, range = "*")
    public void startTaskTracker() throws Exception {

	 	initTT();

    }
   
   @TestStep(order=15, timeout=10000, range="*")
    public void stopSlaveServices() throws IOException, InterruptedException {
    

       if (ttThread.isAlive()) {
           log.info("Stopping TaskTracker...");
           // ttProcess.destroy();
           TTracker.shutdown();
           ttThread.interrupt();
       }
       if (dnThread.isAlive()) {
           log.info("Stopping Datanode...");
           dn.shutdown();
           dnThread.interrupt();
       }
    }
    	
    @TestStep(order=16, timeout=10000, range="0")
    public void stopMasterServices() throws IOException, InterruptedException {
    	
    	if (jtThread.isAlive()) {
	    	log.info("Stopping JobTracker...");
	    	//jtProcess.destroy();
	    	JTracker.stopTracker();
	    	jtThread.interrupt();
    	}
    	
    	if (nnThread.isAlive()) {
	    	log.info("Stopping NameNode...");
	    	nn.stop();
	    	nnThread.interrupt();
    	}
    }
    
    @AfterClass(range="*", timeout = 100000)
    public void stopCluster() throws IOException {
    	log.info("End of test case!");
    	
    }
    

}
