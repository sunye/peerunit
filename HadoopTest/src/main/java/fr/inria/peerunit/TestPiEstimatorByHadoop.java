package fr.inria.peerunit;

/**
 * @author albonico  
 */
// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;

// Java classes
import java.io.IOException;

public class TestPiEstimatorByHadoop extends AbstractMR {

    @TestStep(order = 1, timeout = 30000, range = "0")
    public void a1() throws IOException, InterruptedException {
        startMasterByHadoop();
    }

    @TestStep(order = 2, timeout = 30000, range = "*")
    public void a2() throws IOException, InterruptedException {
        startSlavesByHadoop();
    }

    @TestStep(order = 3, timeout = 120000, range = "0")
    public void a3() throws Exception {
        sendJob();
    }

    @TestStep(order = 4, timeout = 10000, range = "0")
    public void a4() throws Exception, InterruptedException {
        assertResult();
    }

    @TestStep(order = 5, timeout = 30000, range = "*")
    public void a5() throws IOException, InterruptedException {
        stopSlavesByHadoop();
    }

    @TestStep(order = 6, timeout = 30000, range = "0")
    public void a6() throws IOException, InterruptedException {
        stopMasterByHadoop();
    }
}