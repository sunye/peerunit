package test;

/**
 * @author albonico  
 */

// My classes
import examples.PiEstimator;
import load.StartClusterParent;

// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.tester.Assert;

// Java classes
import java.io.IOException;
import java.io.FileNotFoundException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import java.util.Arrays;
import java.math.BigDecimal;
import java.util.ArrayList;

//public class TestJobResultWithMutations extends TestStartCluster {
public class TestJobResultWithMutations extends TestStartCluster {

	PiEstimator pi = new PiEstimator();
	
	/**
	 * Start at TestSteps 5
	 */
	 
	@TestStep(order=5, timeout=50000, range="0")
	public void testeReflection()  throws Exception {
	
		String[] arg = {"4", "20"};
		//pi.run(arg);
		pi.test(arg);
		
		/*
		String path = "/home/michel/workspace-eclipse/albonico/HadoopTest/target/mutants/0/";
		String className = "TesteMutation";
		String methodName = "test";
		classLoader(path, className, methodName, null);
		*/
	}
	
	@TestStep(order=5, timeout=50000, range="0")
	public void getPiResult() {
	
    		double estimatedresult = 3.20000000000000000000;
    		Assert.assertTrue(estimatedresult == pi.getResult().doubleValue());
    		
	}
	
	/*
	 @TestStep(order=6, timeout=50000, range="0")
	 public void mutationGeneration() throws FileNotFoundException, IOException {	 
		 
		 runMutation();

	 }
	
	 
    @TestStep(order=7, timeout = 440000, range = "0")
    public void jobSubmit() throws Exception, InterruptedException {
    	
    	for (int i=0; i<mutationOutputList.size(); i++) {
    		
    		log.info("Sending job!");
    	
    		System.out.println("Mutation class: " + mutationOutputList.get(i));
    		//sendJob("/home/michel/hadoop-0.20.2/hadoop-0.20.2-examples.jar","wordcount","/input/ /output/");
    	
    	}
    }
    */
    

    /*
    @TestStep(order=7, timeout = 440000, range = "0")
    public void assertResult() throws Exception, InterruptedException {
    	
    	ArrayList al = new ArrayList();
    	al.add("michel	2");
    	al.add("albonico	1");
    	
    	// Verify output
    	validateJobOutput("/output/", al);
    */
    	/*
    	//Unit Test
    	if (jobResult != null) {
    	
    		double estimatedresult = 3.20000000000000000000;
    		
    		Assert.assertTrue(estimatedresult == jobResult.doubleValue());

    	} else {
    		
    		Assert.fail();
    		
    	}
    	*/
    	
    //}
    

}
