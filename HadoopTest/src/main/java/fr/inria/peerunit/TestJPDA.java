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


public class TestJPDA extends AbstractMR {


    // Threads vars to Hadoop manipulation
    protected static Thread ttThread;

   @TestStep(order=1, timeout = 100000, range = "1")
   public void lTester() throws Exception {

	log.info("Starting Lower Tester...");

        lowerTester();

   }


   @TestStep(order=2, timeout = 100000, range = "*")
    public void startTaskTracker() throws Exception {

	 ttThread = initTT();
         
         ttThread.start();
         Thread.sleep(5000);
         ttThread.yield();

    }

}
