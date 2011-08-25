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
package fr.inria.peerunit.bootstrapper;

import fr.inria.peerunit.remote.DistributedTester;

import java.util.Arrays;
import java.util.List;

public class TesterNode {

    private DistributedTester head;
    private DistributedTester[] dependents;

    TesterNode(DistributedTester[] coll) {
        super();
        assert coll.length > 0;
        head = coll[0];
        dependents = new DistributedTester[coll.length - 1];
        System.arraycopy(coll, 1, dependents, 0, coll.length - 1);
    }

    public DistributedTester head() {
        return head;
    }

    public List<DistributedTester> dependents() {
        return Arrays.asList(dependents);
    }
}
