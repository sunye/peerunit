package fr.inria.peerunit;

/**
 * @author albonico  
 */

// My classes
//import examples.PiEstimator;
import fr.inria.peerunit.StartClusterParent;

// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.tester.Assert;

// Java classes
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.math.BigDecimal;
import java.util.ArrayList;

public class TestWordCountResult extends TestStartCluster {

	
	/**
	 * Start at TestSteps 5
	 */
	
	 @TestStep(order=5, timeout = 440000, range = "0")
	 public void sendFile() throws Exception, InterruptedException {
	    	
		log.info("Putting file!");
	   	putFileHDFS("/home/michel/workspace-eclipse/albonico/HadoopTest/teste","/input/");
	    
	 }
	
    @TestStep(order=6, timeout = 440000, range = "0")
    public void runWCount() throws Exception, InterruptedException {
    	
    	runJob("wordcount");
    	
    }
    
    
    @TestStep(order=7, timeout = 440000, range = "0")
    public void assertResult() throws Exception, InterruptedException {
    	
    	ArrayList al = new ArrayList();
    	al.add("michel	2");
    	al.add("albonico	1");
    	
    	// Verify output
    	validateJobOutput("/output/", al);    	
    	
    }
    

}
