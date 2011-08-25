/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.tester;


/**
 * @author sunye
 */
public class Failure extends TestException {

    /**
     * Constructs an instance of <code>Failure</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public Failure(String msg) {
        super(msg);
    }
}