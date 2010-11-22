package test;

/**
 * @author albonico  
 */

// My classes
//import examples.PiEstimator;
import load.StartClusterParent;

// PeerUnit classes
import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.parser.AfterClass;

// Java classes
import java.io.IOException;
import java.rmi.RemoteException;

public class TestStartCluster extends StartClusterParent {

    @TestStep(order=1, timeout = 100000, range = "0")
    public void startNameNode() {

    	initNN();

   }

    @TestStep(order=2, timeout=100000, range="1")
    public void startSNameNode() {

	initSNN();

    }

    @TestStep(order=3, timeout = 100000, range = "*")
    public void startDataNode() {

	initDN();

    }

    @TestStep(order=4, timeout = 100000, range = "0")
    public void startJobTracker() {

//    	initJT();

	log.info("Starting JobTracker!");


        try {
                String command = "/home/ppginf/michela/hadoop-0.20.2/bin/start-job.sh";
                final Process process = Runtime.getRuntime().exec(command);
        } catch (Exception e) {

		log.info("Error starting JobTracker:");

                log.warning(e.toString());

        }

    }

    @TestStep(order=5, timeout = 100000, range = "*")
    public void startTaskTracker() {


	log.info("Starting TaskTracker!");

	try {
                String command = "/home/ppginf/michela/hadoop-0.20.2/bin/start-task.sh";
                final Process process = Runtime.getRuntime().exec(command);
        } catch (Exception e) {

		log.info("Error starting TaskTracker:");

                log.warning(e.toString());

        }

//	 initTT();

   }

   @TestStep(order=6, timeout = 440000, range = "0")
   public void sendJob() {

	runPiEstimator piest = new runPiEstimator();

	piest.run();

   }

   @AfterClass(range="*", timeout = 100000)
   public void stopCluster() {


   }

}
