package fr.inria.peerunit.tester;

import fr.inria.peerunit.base.Range;

import java.lang.reflect.Method;

/**
 * @author sunye
 */
public abstract class TestMethod {

    Range range = null;
    Method method = null;
    int timeout = 0;
    String depend = null;

    public Range range() {
        return range;
    }

    public Method method() {
        return method;
    }

    public int timeout() {
        return timeout;
    }

    Range newRange(String str) {
        return Range.fromString(str);
    }

    public String depend() {
        return depend;
    }
}