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
package fr.inria.peerunit.base;

import fr.inria.peerunit.common.MethodDescription;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author sunye
 */
public class ResultSet implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<SingleResult> results;

    private final MethodDescription method;

    private int errors = 0;
    private int failures = 0;
    private int inconclusives = 0;
    private int passes = 0;

    /**
     * Accumulated value of local execution delay.
     */
    private long accumulatedDelay = 0;
    private long start;
    private long stop;
    
    public ResultSet (MethodDescription md) {
        results = new LinkedList<SingleResult>();
        method = md;
    }

    public void add(ResultSet other) {
        assert method.equals(other.method) : "Adding incompatible ResultSet";

        errors += other.errors;
        failures += other.failures;
        inconclusives += other.inconclusives;
        passes += other.passes;
        accumulatedDelay += other.accumulatedDelay;

        results.addAll(other.results);
    }

    public void add(SingleResult r) {
        assert method.equals(r.getMethodDescription()) : "Adding incompatible SingleResult";

        results.add(r);
        accumulatedDelay += r.getDelay();
        switch(r.getVerdict()) {
            case PASS: 
                passes++;
                break;
            case ERROR:
                errors++;
                break;
            case FAIL:
                failures++;
                break;
            case INCONCLUSIVE:
                inconclusives++;
                break;
        }
    }

    @Override
    public String toString() {
        long average = this.size() == 0 ? 0 : accumulatedDelay/size();
        return String.format("Step: %d. Pass: %d. Fails: %d. Errors: %d. " +
                "Inconclusive: %d.  Time elapsed: %d msec. Average: %d msec. \t Method: %s",
                method.getOrder(), passes, failures, errors, inconclusives,
                getDelay(), average, method.getName());
    }

    public void start() {
        start = System.currentTimeMillis();
    }

    public void stop() {
        stop = System.currentTimeMillis();
    }

    public long getDelay() {
        return stop - start;
    }

    public MethodDescription getMethodDescription() {
        return method;
    }

    public int size() {
        return results.size();
    }

    public int getErrors() {
        return errors;
    }

    public int getfailures() {
        return failures;
    }

    public int getInconclusives() {
        return inconclusives;
    }

    public int getPass() {
        return passes;
    }
}
