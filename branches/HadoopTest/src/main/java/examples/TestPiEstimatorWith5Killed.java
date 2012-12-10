package examples;

/**
 * @author jeugenio
 */

import examples.TestPiEstimator;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPiEstimatorWith5Killed extends TestPiEstimator {
   
    @TestStep(order = 3, range = "1-5", depend = "a2", timeout = 60000)
    public void a3() {
        try {
            //killWorker();
            Logger.getLogger(TestPiEstimatorWith5Killed.class.getName()).log(Level.SEVERE,
                    null, "a3() befor Thread.currentThread().sleep(20000);");
            Thread.currentThread().sleep(20000);
            Logger.getLogger(TestPiEstimatorWith5Killed.class.getName()).log(Level.SEVERE,
                    null, "a3() stopWorker");
            stopWorker();
            Logger.getLogger(TestPiEstimatorWith5Killed.class.getName()).log(Level.SEVERE,
                    null, "a3() befor sleep2");
            Thread.sleep(20000);
            Logger.getLogger(TestPiEstimatorWith5Killed.class.getName()).log(Level.SEVERE,
                    null, "a3() +um killWorker");
            killWorker();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimatorWith5Killed.class.getName()).log(Level.SEVERE,
                    null, e.getStackTrace().toString());
            System.out.println("a3 Exception="+e.toString());
            e.printStackTrace();
            Assert.fail();
        }
    }
}
