package examples;

import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import java.util.logging.Logger;

/**
 * A simple global test case involving dependency and hierarchy
 * 
 * @author jeugenio
 */
public class SimpleGlobalTestCase {

    private static final Logger LOG = Logger.getLogger(SimpleGlobalTestCase.class.getName());

    @TestStep(order = 0, range = "*", timeout = 1000)
    public void a0() {
        System.out.println("a0; range=*; ok");
    }

    @TestStep(order = 1, range = "0", depend = "a0", timeout = 10000)
    public void a1() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println("a1; order=1; range=1; depend=a0; fail");
            Thread.sleep(300);
        }
        Assert.fail("action a1 fail!");
    }

    @TestStep(order = 1, range = "1", depend = "a0", timeout = 10000)
    public void a2() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println("a2; order=1; range=1; depend=a0; ok");
            Thread.sleep(500);
        }
    }

    @TestStep(order = 3, range = "*", depend = "a0,a1,a2", timeout = 1000)
    public void a3() throws InterruptedException {
        System.out.println("a3; range=*; depend=a0,a1,a2; ok");
    }

    @TestStep(order = 4, range = "0", timeout = 1000)
    public void a4() throws InterruptedException {
        System.out.println("a4; range=*; ok");
    }
}
