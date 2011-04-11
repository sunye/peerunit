package fr.inria.peerunit;

/**
 * @author jeugenio
 */
// My classes
//import examples.PiEstimator;
import java.util.logging.Level;
//import load.StartClusterParent;

// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;

// Java classes
public class PiTest extends TestStartCluster {

    /**
     * Start at TestSteps 5
     */
    @TestStep(order = 5, timeout = 440000, range = "0")
    public void jobSubmit() throws Exception, InterruptedException {

        log.info("Running Job!");

        runPiEstimator pi = new runPiEstimator();
        jobThread = new Thread(pi);
        jobThread.start();
        jobThread.join();

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
            double expected = Double.valueOf(pivalue);
            log.log(Level.INFO, "expected:{0}   jobResult: {1}", new Object[]{expected, jobResult.doubleValue()});
            Assert.assertTrue(expected == jobResult.doubleValue());

        } else {

            log.info("Pi result is NULL!");

            Assert.fail();

        }
    }
}
