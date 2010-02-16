/*
    This file is part of PeerUnit.

    PeerUnit is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PeerUnit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.parser;

import java.lang.reflect.Method;

/**
 *
 * @author sunye
 */
public class TestStepMethod extends TestMethod {

    /**
     *
     */
    private int order;

    /**
     *
     * @param m
     */
    public TestStepMethod(final Method m) {
        TestStep ts = m.getAnnotation(fr.inria.peerunit.parser.TestStep.class);
        timeout = ts.timeout();
        order = ts.order() == -1 ? ts.step() : ts.order();
        method = m;
        range = this.newRange(ts.place(), ts.from(), ts.to(), ts.range());

    }

    /**
     *
     * @return
     */
    public final int order() {
        return order;
    }
}
