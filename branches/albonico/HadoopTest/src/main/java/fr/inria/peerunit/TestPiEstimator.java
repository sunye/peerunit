package fr.inria.peerunit;

/**
 * @author albonico  
 */
// PeerUnit classes

import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPiEstimator extends AbstractMR {
   
    @TestStep(order = 1, range = "0", timeout = 60000)
    public void a0() {
        try {
            startMaster();
        } catch (RemoteException ex) {
            Logger.getLogger(TestPiEstimator.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("RemoteException="+ex.toString());
        } catch (IOException ex) {
            Logger.getLogger(TestPiEstimator.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("IOException="+ex.toString());
        } catch (InterruptedException ex) {
            Logger.getLogger(TestPiEstimator.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("InterruptedException="+ex.toString());
        }
        finally {
            System.out.println("finally");
            Assert.fail();
        }

    }

    @TestStep(order = 2, range = "*", depend = "a0", timeout = 120000)
    public void a1() throws IOException, InterruptedException {
        startWorkers();
    }

    @TestStep(order = 3, range = "0", depend = "a1", timeout = 60000)
    public void a2() throws Exception, InterruptedException {
        sendJob();
    }

    @TestStep(order = 3, range = "0", depend = "a2", timeout = 60000)
    public void a3() throws Exception, InterruptedException {
        Thread.currentThread().sleep(10000);
        killWorker();
    }

    @TestStep(order = 4, range = "0", depend = "a2", timeout = 10000)
    public void a4() throws Exception, InterruptedException {
        assertResult();
    }

    @TestStep(order = 5, range = "*", depend = "a1", timeout = 60000)
    public void a5() throws IOException, InterruptedException {
        stopWorkers();
    }
  
    @TestStep(order = 6, range = "0", depend = "a0", timeout = 60000)
    public void a6() throws IOException, InterruptedException {
        stopMaster();
    }
}
