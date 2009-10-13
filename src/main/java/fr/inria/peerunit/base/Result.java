/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.base;

import fr.inria.peerunit.parser.MethodDescription;
import java.io.Serializable;

/**
 *
 * @author sunye
 */
public class Result implements Serializable {
    private MethodDescription md;
    private int testerId;
    private long start;
    private long stop;

    public Result(int id, MethodDescription md) {
        this.md = md;
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void stop() {
        stop = System.currentTimeMillis();
    }

    public void addError(Throwable t) {

    }

    public void addFailure(AssertionError ae) {
        
    }
    
    public void addTimeout(InterruptedException ie) {

    }
    
    public MethodDescription getMethodDescription() {
        return md;
    }
}
