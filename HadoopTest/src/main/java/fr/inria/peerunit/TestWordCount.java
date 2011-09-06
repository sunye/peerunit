package fr.inria.peerunit;

/**
 * @author albonico  
 */
// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;

// Java classes
import java.io.IOException;

public class TestWordCount extends AbstractMR {

    @TestStep(order = 1, timeout = 30000, range = "0")
    public void a0() throws IOException, InterruptedException {
        startMaster();
    }

    @TestStep(order = 2, timeout = 30000, range = "*")
    public void a1() throws IOException, InterruptedException {
        startWorkers();
    }

    @TestStep(order = 3, timeout = 30000, range = "0")
    public void a2() throws IOException, InterruptedException {
        putFileHDFS();
    }

    @TestStep(order = 4, range = "0", timeout = 120000)
    public void a3() throws Exception, InterruptedException {
        sendJob();
    }

    @TestStep(order = 5, timeout = 200000, range = "0")
    public void a4() throws Exception, InterruptedException {
       assertResult();
    }

    @TestStep(order = 6, timeout = 30000, range = "*")
    public void a5() throws IOException, InterruptedException {
        stopWorkers();
    }

    @TestStep(order = 7, timeout = 30000, range = "0")
    public void a6() throws IOException, InterruptedException {
        stopMaster();
    }
}
