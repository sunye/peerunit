package fr.inria.peerunit;

/**
 * @author albonico  
 */

// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.parser.AfterClass;
import fr.inria.peerunit.tester.Assert;

// Java classes
import java.io.IOException;
import java.math.BigDecimal;


public class TestPiEstimatorByHadoop extends AbstractMR {
    
    // Threads vars to Hadoop manipulation
    protected static Thread nnThread;
    protected static Thread dnThread;
    protected static Thread jtThread;
    protected static Thread ttThread;

    @TestStep(order=1, timeout = 150000, range = "0")
    public void startNameNode() throws IOException, InterruptedException {

    	nnThread = initNNByHadoop();
        nnThread.start();
        Thread.sleep(10000);
        nnThread.join();

    }
	

    @TestStep(order=2, timeout = 100000, range = "*", depend="startNameNode")
    public void startDataNode() throws IOException, InterruptedException {

    	dnThread = initDNByHadoop();
        dnThread.start();
        Thread.sleep(10000);
        dnThread.join();

    }

    @TestStep(order=3, timeout = 120000, range = "0", depend="startNameNode")
    public void startJobTracker() throws Exception {
    	
    	jtThread = initJTByHadoop();
        jtThread.start();
        Thread.sleep(10000);
        jtThread.join();
    	
   } 

   @TestStep(order=4, timeout = 100000, range = "*", depend="startJobTracker")
    public void startTaskTracker() throws Exception {

	 ttThread = initTTByHadoop();
         ttThread.start();
         Thread.sleep(10000);
         ttThread.join();

    }


   @TestStep(order=5, timeout = 400000, range = "0", depend="startJobTracker")
   public void jobSubmit() throws Exception, InterruptedException {


       Thread.sleep(60000);

       String hadoopdir = (String) get(-14);

       String jar = (String) get(-15);
       String job = (String) get(-16);
       String param = (String) get(-17);

       String command = hadoopdir + "/bin/hadoop jar " + jar + " " + job + " " + param;
       log.info("Running " + command + "!" );
       Process jobProcess = Runtime.getRuntime().exec(command);
       jobProcess.waitFor();     

    }

   
   @TestStep(order=15, timeout=30000, range="*")
    public void stopSlaveServices() throws IOException, InterruptedException {
    
	    	log.info("Stopping Datanode...");
                dnProcess.destroy();

		log.info("Stopping TaskTracker...");
                ttProcess.destroy();
    	
    }
    	
    @TestStep(order=16, timeout=30000, range="0")
    public void stopMasterServices() throws IOException, InterruptedException {

	    	log.info("Stopping JobTracker...");
	    	jtProcess.destroy();

	    	log.info("Stopping NameNode...");
                nnProcess.destroy();

    }
    
    @AfterClass(range="*", timeout = 100000)
    public void ac() throws IOException {
    	log.info("End of test case!");
    }


}
