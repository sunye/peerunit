package fr.inria.peerunit;

/**
 * @author Joao Eugenio - jeugenio@inf.ufpr.br
 */

import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import java.io.IOException;
import java.rmi.RemoteException;

public class TestJoao extends TestStartCluster {

    /**
     * Start at TestSteps 5
     */
    @TestStep(order = 5, timeout = 440000, range = "*")
    public void action1() throws RemoteException {
        try {
            Process p = Runtime.getRuntime().exec("../hadoop/hadoop-0.20.2/bin/hadoop jar ../hadoop/hadoop-0.20.0/hadoop-0.20.2-examples.jar");
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
                log.severe(ex.toString());
            }
        } catch (IOException ex) {
            log.severe(ex.toString());
        }
    }

}
