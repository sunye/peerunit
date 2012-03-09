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

public class TestPiEstimatorKillWorker extends AbstractMR {
   
    @TestStep(order = 1, range = "0", timeout = 60000)
    public void startM() {
        System.out.println("a0:");
        try {
            startMaster();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimator.class.getName()).log(Level.SEVERE, 
                    null, e.getStackTrace().toString());
            System.out.println("a0 Exception="+e.toString());
            e.printStackTrace();
            Assert.fail();
        }
    }

    @TestStep(order = 2, range = "*", depend = "a0", timeout = 120000)
    public void startW() throws InterruptedException{
        
        System.out.println("a1:");
        try {
            startWorkers();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimator.class.getName()).log(Level.SEVERE, 
                    null, e.getStackTrace().toString());
            System.out.println("a1 Exception="+e.toString());
            e.printStackTrace();
            Assert.fail();
        }
    }
    
    @TestStep(order=3, range="0", depend="a1", timeout=420000)
    public void putFile() throws InterruptedException {
        System.out.println("putFile:");
        if (assertType == 2) {
            Thread.currentThread().sleep(10000);
            putFileHDFS();
            // PeerUnit is not waiting for putFileHDFS process
       //     Thread.currentThread().sleep(waitForPutFile);
        } else {
            System.out.println("Have not files to be uploaded. Assert Type = " + assertType);
        }  
    }

    @TestStep(order = 4, range = "0", depend = "a1", timeout = 900000)
    public void sendJ() {
        
        System.out.println("a2:");
        
        int i=0;
        try {
            i = System.in.read();
        } catch (IOException ex) {
            Logger.getLogger(TestPiEstimator.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("i="+i);
         
        try {
            sendJob();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimator.class.getName()).log(Level.SEVERE,
                    null, e.getStackTrace().toString());
            System.out.println("a2 Exception="+e.toString());
            e.printStackTrace();
            Assert.fail();
        }
        System.out.println("a2 after sendJob()");
    }
    
 /*   
    @TestStep(order = 4, range = "1", depend = "a1", timeout = 60000)
    public void killTT() throws InterruptedException {
        System.out.println("killTaskTracker():");
        
        Thread.currentThread().sleep(45000);
        
        try {
            killWorker();
        } catch (Exception e) {
            Logger.getLogger(TestPiEstimator.class.getName()).log(Level.SEVERE,
                    null, e.getStackTrace().toString());
            System.out.println("killTaskTracker Exception="+e.toString());
            e.printStackTrace();
            Assert.fail();
        }
        System.out.println("killTaskTracker finished.");
    }
    */
    
    @TestStep(order = 5, range = "0", depend = "a2", timeout = 10000)
    public void assertR() throws Exception, InterruptedException {
        System.out.println("a4.read():");
        //System.out.println(System.in.read());
        assertResult();
    }

    @TestStep(order = 6, range = "*", depend = "a1", timeout = 15000)
    public void stopW() throws IOException, InterruptedException, Exception {
        System.out.println("a5():");
        stopWorkers();
    }
  
    @TestStep(order = 7, range = "0", depend = "a0", timeout = 15000)
    public void stopM() throws IOException, InterruptedException {
        System.out.println("a6():");
        stopMaster();
    }
    
}
