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


    @TestStep(range = "*", order = 2, timeout = 40000)
    public void firstStep() {

       System.out.println("Hello!!!");
       
    }

    @TestStep(range = "*", order = 3, timeout = 40000)
    public void incrementCount() {
        
        count++;

    }

    @TestStep(range = "*", order = 4, timeout = 40000)
    public void lastStep() {

        System.out.println("Count is: " + count);
        
    }
   
}
