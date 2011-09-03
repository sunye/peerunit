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

    @TestStep(order = 1, timeout = 30000, range = "0")
    public void startNameNode() throws IOException, InterruptedException {

        dfsFormatting((String) get(-38));

        nnThread = initNN();

        nnThread.start();
        nnThread.join();


    }

    @TestStep(order = 2, timeout = 10000, range = "*")
    public void startDataNode() throws IOException, InterruptedException {

        System.out.println("startDataNode");
        dnThread = initDN();

        dnThread.start();
        dnThread.join();
        System.out.println("startDataNode - end");

    }

    @TestStep(order = 3, timeout = 10000, range = "0")
    public void startJobTracker() throws Exception {

        jtThread = initJT();

        jtThread.start();
        jtThread.yield();

    }

    @TestStep(order = 4, timeout = 10000, range = "*")
    public void startTaskTracker() throws Exception {

        ttThread = initTT();

        ttThread.start();
        ttThread.yield();

    }

    @TestStep(order = 5, range = "0", timeout = 120000)
    public void jobSubmit() throws Exception, InterruptedException {

        sendJob();

        /*
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
         */


    }

    @TestStep(order = 6, timeout = 1000, range = "0")
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

            String pivalue = (String) get(-20);
            BigDecimal expected;
            expected = BigDecimal.valueOf(Double.valueOf(pivalue));

            //double expected = Double.valueOf(pivalue);
            //log.info("Expected job result: " + expected + "\n Returned job result: " + jobResult);
            System.out.println("expected:" + expected + "  jobResult:" + jobResult
                    + "  duration:" + jobDuration);
            log.info("expected:" + expected + "  jobResult:" + jobResult
                    + "  duration:" + jobDuration);

            Assert.assertTrue(expected.equals(jobResult));

        } else {

            log.info("jobResult is NULL!");

            Assert.fail();

        }


    }

    @TestStep(order = 15, timeout = 30000, range = "*")
    public void stopSlaveServices() throws IOException, InterruptedException {
        log.info("Stopping Datanode...");
        //dn.shutdown();
        dnThread.interrupt(); // takes too long to respond!

        log.info("Stopping TaskTracker...");
        //TTracker.shutdown();
        ttThread.interrupt();
    }

    @TestStep(order = 16, timeout = 30000, range = "0")
    public void stopMasterServices() throws IOException, InterruptedException {
        log.info("Stopping JobTracker...");
        JTracker.stopTracker();
        //jtThread.interrupt();

        log.info("Stopping NameNode...");
        //nn.stop();
        nnThread.interrupt();
    }
}
