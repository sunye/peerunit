package examples;

import br.ufpr.hadooptest.AbstractMR;
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestPiEstimator5KilledDetailed extends AbstractMR {

    @TestStep(order = 1, range = "0", timeout = 10000)
    public void a0() {
        System.out.println("a0: startMaster()");
        try {
            startMaster();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimator5KilledDetailed.class.getName()).log(Level.SEVERE,
                    null, e.getStackTrace().toString());
            System.out.println("a0 Exception=" + e.toString());
            e.printStackTrace();
            Assert.fail();
        }
    }

    @TestStep(order = 2, range = "*", depend = "a0", timeout = 360000)
    public void a1() {
        try {
            startWorkers();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimator5KilledDetailed.class.getName()).log(Level.SEVERE,
                    null, e.getStackTrace().toString());
            System.out.println("a1 Exception=" + e.toString());
            e.printStackTrace();
            Assert.fail();
        }
    }

    @TestStep(order = 3, range = "0", depend = "a1", timeout = 240000)
    public void a2() {
        System.out.println("a0: sendJob()");
        try {
            sendJob();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimator5KilledDetailed.class.getName()).log(Level.SEVERE,
                    null, e.getStackTrace().toString());
            System.out.println("a2 Exception=" + e.toString());
            e.printStackTrace();
            Assert.fail();
        }
        System.out.println("a2 after sendJob()");
    }

    @TestStep(order = 3, range = "1-5", depend = "a2", timeout = 60000)
    public void a3() {
        try {
            Logger.getLogger(TestPiEstimator5KilledDetailed.class.getName()).log(Level.SEVERE,
                    "a3() before Thread.currentThread().sleep(20000);");
            Thread.currentThread().sleep(20000);
            Logger.getLogger(TestPiEstimator5KilledDetailed.class.getName()).log(Level.SEVERE,
                    "a3() stopworker");
            //stopWorker();
            killWorker();
            Logger.getLogger(TestPiEstimator5KilledDetailed.class.getName()).log(Level.SEVERE,
                    "a3() befor Thread.sleep(20000);");
//            Thread.sleep(20000);
            Logger.getLogger(TestPiEstimator5KilledDetailed.class.getName()).log(Level.SEVERE,
                    "a3() killWorker");

            //killWorker();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimatorWith5Killed.class.getName()).log(Level.SEVERE,
                    e.getStackTrace().toString());
            System.out.println("a3 Exception="+e.toString());
            e.printStackTrace();
            Assert.fail();
        }
    }


    @TestStep(order = 4, range = "0", depend = "a2", timeout = 1000)
    public void a4() throws Exception, InterruptedException {
//        System.out.println("a4.read():");
        //System.out.println(System.in.read());
        assertResult();
    }

    @TestStep(order = 5, range = "*", depend = "a1", timeout = 60000)
    public void a5() throws IOException, InterruptedException, Exception {
        stopWorker();
    }

    @TestStep(order = 6, range = "0", depend = "a0", timeout = 60000)
    public void a6() throws IOException, InterruptedException {
        System.out.println("a6(): stopMaster()");
        stopMaster();
    }
}
