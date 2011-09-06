/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.peerunit;

import fr.inria.peerunit.parser.TestStep;

import java.rmi.registry.Registry;
import java.util.logging.Logger;

/**
 * Parallel Test Simple
 *
 * @author jeugenio
 */
public class SimpleHierarchicalTestCase {

    private static final Logger LOG = Logger.getLogger(SimpleHierarchicalTestCase.class.getName());

    @TestStep(order = 1, range = "1", timeout = 50000)
    public void a1() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println("a1: order=1; range=1; i="+i);
            Thread.sleep(500);
        }
        System.out.println("END a1: order=1; range=1");
    }

    @TestStep(order = 0, range = "1", timeout = 50000)
    public void a2() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println("a2: order=1; range=1; i="+i);
            Thread.sleep(800);
        }
        System.out.println("END a2: order=1; range=1");
    }

    @TestStep(order = 0, range = "0,1", timeout = 40000)
    public void a0() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println("a0: order=1; range=0,1; i="+i);
            Thread.sleep(500);
        }
        System.out.println("END a0: order=1; range=0,1");
    }

    @TestStep(order = 1, range = "0", timeout = 40000)
    public void a3() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println("a3: order=1; range=0; i="+i);
            Thread.sleep(800);
        }
        System.out.println("END a3: order=1; range=0");
    }
}
