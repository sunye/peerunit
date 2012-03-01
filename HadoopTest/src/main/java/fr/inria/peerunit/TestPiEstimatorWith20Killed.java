package fr.inria.peerunit;

/**
 * @author albonico  
 */
// PeerUnit classes

import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPiEstimatorWith20Killed extends TestPiEstimator {
   
    @TestStep(order = 3, range = "1-20", depend = "a2", timeout = 60000)
    public void a3() {
        try {
            Thread.sleep(10000);
            System.out.println("a3():");
            killWorker();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimatorWith20Killed.class.getName()).log(Level.SEVERE,
                    null, e.getStackTrace().toString());
            System.out.println("a3 Exception="+e.toString());
            e.printStackTrace();
            Assert.fail();
        }
    }
}
