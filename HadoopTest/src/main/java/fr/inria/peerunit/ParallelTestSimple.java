/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit;

import fr.inria.peerunit.parser.TestStep;

import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 *
 * @author sunye
 */
public class ParallelTestSimple extends AbstractMR {

    /**
     * Logger
     */
    private static final Logger LOG = Logger.getLogger(ParallelTestSimple.class.getName());
    
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

    @TestStep(order = 1, range = "0", timeout = 40000)
    public void firstStep() throws InterruptedException {

     int a = 2/0;

    // Thread.currentThread().yield();
     
     for (int i=0; i<10; i++) {

        System.out.println("teste1");
        Thread.currentThread().sleep(500);

     }

    }

    @TestStep(order = 1, range = "1", timeout = 50000)
    public void secondStep() throws InterruptedException {

       for (int a=0; a<10; a++) {

            System.out.println("teste2");
            Thread.currentThread().sleep(500);
       }

    }

    @TestStep(order = 1, range = "1", timeout = 50000)
    public void thirdStep() throws InterruptedException {

       for (int a=0; a<10; a++) {

            System.out.println("teste3");
            Thread.currentThread().sleep(500);
       }

    }
   
}
    