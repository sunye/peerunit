package fr.inria.peerunit;

/**
 * @author albonico  
 */
// My classes
//import examples.PiEstimator;
import java.util.logging.Level;
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
import java.util.logging.Logger;

public class TestJobResult extends TestStartCluster {

    protected static final Logger LOG = Logger.getLogger(TestJobResult.class.getName());

    /**
     * Start at TestSteps 5
     */
    /*
    @TestStep(order=5, timeout = 440000, range = "0")
    public void sendFile() throws Exception, InterruptedException {

    log.info("Putting file!");
    putFileHDFS("/home/michel/workspace-eclipse/albonico/HadoopTest/teste","/input/");

    }
     */
    @TestStep(order = 6, timeout = 120000, range = "0")
    public void jobSubmit() throws Exception, InterruptedException {

        runJob();
        //log.info("Sending job!");
        //sendJob("/home/michel/hadoop-0.20.2/hadoop-0.20.2-examples.jar","wordcount","/input/ /output/");

    }

    @TestStep(order = 7, timeout = 60000, range = "0")
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
            double estimatedresult = Double.valueOf(pivalue);
            log.log(Level.INFO, "Expected:{0} Calculated: {1}", new Object[]{pivalue, jobResult});
            LOG.log(Level.INFO, "Expected: {0}", estimatedresult);
            LOG.log(Level.INFO, "Pi result: {0}", jobResult);
            Assert.assertTrue(estimatedresult == jobResult.doubleValue());

        } else {

            LOG.info("Pi result is NULL!");

            Assert.fail();

        }


    }
}
