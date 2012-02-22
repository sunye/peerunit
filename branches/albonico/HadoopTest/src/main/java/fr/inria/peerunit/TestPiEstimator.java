package fr.inria.peerunit;

/**
 * @author albonico  
 */
// PeerUnit classes

import fr.inria.peerunit.parser.TestStep;
import java.io.IOException;

public class TestPiEstimator extends AbstractMR {
   
    @TestStep(order = 1, timeout = 30000, range = "0")
    public void a0() throws IOException, InterruptedException {
        startMaster();
    }

    @TestStep(order = 2, timeout = 200000, range = "*")
    public void a1() throws IOException, InterruptedException {
        startWorkers();
    }

    @TestStep(order = 3, range = "0", timeout = 3850000)
    public void a2() throws Exception, InterruptedException {
        sendJob();
    }

    @TestStep(order = 3, range = "1", timeout = 120000)
    public void a3() throws Exception, InterruptedException {
        Thread.currentThread().sleep(10000);
        killWorker();
    }

    @TestStep(order = 4, timeout = 10000, range = "0")
    public void a4() throws Exception, InterruptedException {
        assertResult();
    }

    @TestStep(order = 5, timeout = 30000, range = "*")
    public void a5() throws IOException, InterruptedException {
        stopWorkers();
    }
  
    @TestStep(order = 6, timeout = 30000, range = "0")
    public void a6() throws IOException, InterruptedException {
        stopMaster();
    }
}
