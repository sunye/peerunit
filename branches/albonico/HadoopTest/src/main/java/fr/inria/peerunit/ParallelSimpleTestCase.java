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
public class ParallelSimpleTestCase {

    private static final Logger LOG = Logger.getLogger(ParallelTestSimple.class.getName());

    @TestStep(order = 0, range = "0", timeout = 40000)
    public void a0() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println("a0, order=1, range=0");
            Thread.sleep(500);
        }
    }

    @TestStep(order = 1, range = "1", timeout = 50000)
    public void a1() throws InterruptedException {
        for (int a = 0; a < 10; a++) {
            System.out.println("a1, order=1, range=1");
            Thread.sleep(500);
        }
    }

    @TestStep(order = 0, range = "1", timeout = 50000)
    public void a2() throws InterruptedException {
        for (int a = 0; a < 10; a++) {
            System.out.println("a2, order=1, range=1");
            Thread.sleep(800);
        }
    }

    @TestStep(order = 1, range = "0", timeout = 40000)
    public void a3() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            System.out.println("a3, order=1, range=0");
            Thread.sleep(800);
        }
    }
}
