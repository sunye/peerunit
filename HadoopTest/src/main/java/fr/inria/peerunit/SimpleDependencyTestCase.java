package fr.inria.peerunit;

import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;
import java.util.logging.Logger;

/**
 * Simple dependence test case
 * 
 * @author jeugenio
 */
public class SimpleDependencyTestCase {

    private static final Logger LOG = Logger.getLogger(SimpleDependencyTestCase.class.getName());

    @TestStep(order = 0, range = "*", timeout = 1000)
    public void a0() {
        System.out.println("a0; range=*; ok");
    }

    @TestStep(order = 1, range = "0", depend = "a0", timeout = 1000 )
    public void a1() throws InterruptedException {
        System.out.println("a1; range=1; depend=a0; fail");
        Assert.fail("action a1 fail!");
    }

    @TestStep(order = 2, range = "1", depend = "a0", timeout = 1000)
    public void a2() throws InterruptedException {
        System.out.println("a2; range=1; depend=a0; ok");
    }

    @TestStep(order = 3, range = "*", depend = "a0,a1,a2", timeout = 1000)
    public void a3() throws InterruptedException {
        System.out.println("a3; range=*; depend=a0,a1,a2; ok");
    }

    @TestStep(order = 4, range = "0", timeout = 1000)
    public void a4() throws InterruptedException {
        System.out.println("a4; range=0; ok");
    }
}
