package fr.inria.peerunit;

/**
 * @author albonico  
 */
// PeerUnit classes

import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPiEstimatorWith20Stopped extends TestPiEstimator {
   
    @TestStep(order = 3, range = "1-20", depend = "a2", timeout = 60000)
    public void a3() {
        System.out.println("a3");
        try {
            Thread.currentThread().sleep(10000);
            System.out.println("a3() after sleep");
            stopWorkers();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimatorWith20Stopped.class.getName()).log(Level.SEVERE,
                    null, e.getStackTrace().toString());
            System.out.println("a3 Exception="+e.toString());
            e.printStackTrace();
            Assert.fail();
        }
    }
}
