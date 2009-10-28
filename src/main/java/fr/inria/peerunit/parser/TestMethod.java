/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.parser;

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

    public Range range() {
        return range;
    }

    public Method method() {
        return method;
    }

    public int timeout() {
        return timeout;
    }

    protected Range newRange(int place, int from, int to, String str) {
        if (place != -1) {
            return Range.newInstance(place);
        }
        if (from != -1 && to != -1) {
            return Range.newInstance(from, to);
        }
        return Range.fromString(str);
    }

}
