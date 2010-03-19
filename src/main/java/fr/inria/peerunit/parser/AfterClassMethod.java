/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.inria.peerunit.parser;

import java.lang.reflect.Method;

/**
 *
 * @author sunye
 */
public class AfterClassMethod extends TestMethod {

    public  AfterClassMethod(Method m) {
        AfterClass ac = m.getAnnotation(AfterClass.class);
        timeout = ac.timeout();
        method = m;
        range = this.newRange(ac.place(), ac.from(), ac.to(), ac.range());
    }
}
