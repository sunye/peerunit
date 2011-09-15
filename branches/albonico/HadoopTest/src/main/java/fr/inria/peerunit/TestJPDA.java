package fr.inria.peerunit;

/**
 * @author albonico  
 */

// PeerUnit classes
import java.util.logging.Logger;

import fr.inria.peerunit.parser.TestStep;


public class TestJPDA extends AbstractMR {
	private static Logger LOG = Logger.getLogger(TestJPDA.class.getName());

    // Threads vars to Hadoop manipulation
    protected  Thread ttThread;

   @TestStep(order=1, timeout = 100000, range = "1")
   public void lTester() throws Exception {

	LOG.info("Starting Lower Tester...");

     //   lowerTester();

   }


   @TestStep(order=2, timeout = 100000, range = "*")
    public void startTaskTracker() throws Exception {

//	 ttThread = initTT();
         
         ttThread.start();
         Thread.sleep(5000);
         Thread.yield();

    }

}
