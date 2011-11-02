/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.tester;

import fr.inria.peerunit.base.Range;
import java.lang.reflect.Method;

/**
 *
 * @author sunye
 */
public abstract class TestMethod {
    
    protected Range range;
    protected Method method;
    protected int timeout;
    protected String depend;

    public Range range() {
        return range;
    }

    public Method method() {
        return method;
    }

    public int timeout() {
        return timeout;
    }

    protected Range newRange(String str) {
        return Range.fromString(str);
    }

    public String depend() {
        return depend;
    }

}