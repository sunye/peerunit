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
package fr.inria.peerunit.coordinator;

import fr.inria.peerunit.base.ResultSet;
import fr.inria.peerunit.common.MethodDescription;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Eduardo Almeida
 * @version 1.0
 * @since 1.0
 */
class GlobalVerdict {

    private final Map<MethodDescription, ResultSet> results;

    public GlobalVerdict() {
        results = Collections.synchronizedMap(new TreeMap<MethodDescription, ResultSet>());
    }

    @Override
    public String toString() {
        long accumulatedDelay = 0;
        StringBuilder result = new StringBuilder();
        result.append("------------------------------\n");
        result.append("Test Case Verdict: \n");
        for (ResultSet each : results.values()) {
            result.append(each).append("\n");
            accumulatedDelay += each.getDelay();
        }
        result.append("Accumulated Time Elapsed: ").append(accumulatedDelay).append("\n");
        result.append("------------------------------\n");

        return result.toString();
    }

    public void putResult(MethodDescription md, ResultSet rs) {
        results.put(md, rs);
    }

    public ResultSet getResultFor(MethodDescription md) {
        return results.get(md);
    }
}
