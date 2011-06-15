/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit;

import fr.inria.peerunit.parser.TestStep;
import fr.inria.peerunit.tester.Assert;

import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class TestSimple extends AbstractMR {

    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(TestSimple.class.getName());
    
    /**
     * Port for RMI Registry
     */
    private static final int PORT = 8282;
    private Registry registry;

    private static int count = 0;
    private static int count2 = 0;

    @TestStep(range = "0", order = 2, timeout = 40000)
    public void firstStep() {

       System.out.println("Hello!!!");
       
    }

    @TestStep(range = "0", order = 3, timeout = 40000)
    public void incrementCount() {
        
        count++;

    }

    @TestStep(range = "0", order = 3, timeout = 40000)
    public void increment2Count() throws InterruptedException {

      //  Assert.fail();

      int a = 2 / 0;

    }
    
    @TestStep(range = "0", order = 4, timeout = 40000, depend = 3)
    public void test() throws InterruptedException {

      System.out.println("Test...");

    }

    @TestStep(range = "0", order = 5, timeout = 40000, depend = 2)
    public void lastStep() throws InterruptedException {

        //int i = 2 / 0;
        //Assert.fail();
        System.out.println("Count is: " + count);
        System.out.println("Count2 is: " + count2);

    }

   
}
    