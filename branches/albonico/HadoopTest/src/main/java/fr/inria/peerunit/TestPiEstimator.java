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


public class TestPiEstimator extends AbstractMR {


    // Threads vars to Hadoop manipulation
    protected static Thread nnThread;
    protected static Thread dnThread;
    protected static Thread jtThread;
    protected static Thread ttThread;

    @TestStep(order=1, timeout = 150000, range = "0")
    public void startNameNode() throws IOException, InterruptedException {

    	nnThread = initNN();

        nnThread.start();
        nnThread.join();


    }
	

    @TestStep(order=2, timeout = 100000, range = "*")
    public void startDataNode() throws IOException, InterruptedException {

    	dnThread = initDN();

        dnThread.start();
        dnThread.join();

    }

    @TestStep(order=3, timeout = 100000, range = "0")
    public void startJobTracker() throws Exception {
    	
    	jtThread = initJT();
        
        jtThread.start();
        Thread.sleep(5000);
        jtThread.yield();
    	
   } 

   @TestStep(order=4, timeout = 100000, range = "*")
    public void startTaskTracker() throws Exception {

	 ttThread = initTT();
         
         ttThread.start();
         Thread.sleep(5000);
         ttThread.yield();

    }

   public void jobSubmit() throws Exception, InterruptedException {

        try {

                PiEstimator pi = new PiEstimator();

                //String masterAddr = (String) get(-2);
                //String masterPort = (String) get(-4);
                //pi.setCfg(masteraddr, masterport);
                //pi.setCfg(config); (This is correct)

                //String arg1 = (String) get(-21);
                //String arg2 = (String) get(-22);

                String[] argumentos = {(String) get(-21), (String) get(-22), (String) get(-2), (String) get(-4)};
                pi.run(argumentos);

                jobResult = pi.getResult();
                jobDuration = pi.duration;

                //BaileyBorweinPlouffe pi = new BaileyBorweinPlouffe();
                //String[] args = {"1","6","4","/pi"};
                //pi.run(args);



            } catch (IOException ioe) {
            } catch (Exception e) {
            }

    }

    @TestStep(order = 6, timeout = 440000, range = "0")
    public void assertResult() throws Exception, InterruptedException {
        /*
        ArrayList al = new ArrayList();
        al.add("michel	2");
        al.add("albonico	1");

        // Verify output
        validateJobOutput("/output/", al);
         */

        //Unit Test
        if (jobResult != null) {

            //double estimatedresult = 3.20000000000000000000;

            String pivalue = (String) get(-20);
            BigDecimal expected;
            expected = BigDecimal.valueOf(Double.valueOf(pivalue));

            //double expected = Double.valueOf(pivalue);
            log.info("expected:" + expected + "  jobResult:" + jobResult
                    + "  duration:" + jobDuration);
            Assert.assertTrue(expected == jobResult);


        } else {

            log.info("jobResult is NULL!");

            Assert.fail();

        }


    }
   
   @TestStep(order=15, timeout=30000, range="*")
    public void stopSlaveServices() throws IOException, InterruptedException {
    
    	if (dnThread.isAlive()) {
	    	log.info("Stopping Datanode...");
	    	dn.shutdown();
	    	dnThread.interrupt();
    	}
    	
    	if (ttThread.isAlive()) {
		log.info("Stopping TaskTracker...");
		TTracker.shutdown();
		ttThread.interrupt();
    	}

    	
    }
    	
    @TestStep(order=16, timeout=30000, range="0")
    public void stopMasterServices() throws IOException, InterruptedException {
    	
    	if (jtThread.isAlive()) {
	    	log.info("Stopping JobTracker...");
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
    public void ac() throws IOException {
    	log.info("End of test case!");
    	
    }


}
