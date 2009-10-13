/*
    This file is part of PeerUnit.

    Foobar is free software: you can redistribute it and/or modify
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
