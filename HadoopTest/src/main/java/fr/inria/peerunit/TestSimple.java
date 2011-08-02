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

    private String teste = "teste";

    synchronized public void increment() {
        
        count++;
        
    }

    @TestStep(range = "0", order = 1, timeout = 40000)
    public void firstStep() {

       System.out.println("Hello!!!");
       
    }

    @TestStep(range = "0", order = 2, timeout = 0)
    public void incrementCount() throws InterruptedException {

     // for (int i=0; i<10; i++) {
       // increment();
       // count++;
        System.out.println("teste1");
     //   Thread.currentThread().sleep(500);
     // }


    }

    @TestStep(range = "1", order = 2, timeout = 0)
    public void increment2Count() throws InterruptedException {

        int a = 2 / 0;

      //  Assert.fail();

      //for (int ii=0; ii < 10; ii++) {
       // increment();
       // count2++;
       // System.out.println("teste2" + count2);
       // Thread.currentThread().sleep(500);
      //}

    }
    
    @TestStep(range = "*", order = 3, timeout = 40000, depend = "increment2Count")
    public void test() throws InterruptedException {

      System.out.println("Test...");
      count++;

    }

    @TestStep(range = "0", order = 5, timeout = 40000, depend = "firstStep")
    public void lastStep() throws InterruptedException {

        //int i = 2 / 0;
        //Assert.fail();
        System.out.println("Count is: " + count);
        //System.out.println("Count2 is: " + count2);

    }

   
}
    