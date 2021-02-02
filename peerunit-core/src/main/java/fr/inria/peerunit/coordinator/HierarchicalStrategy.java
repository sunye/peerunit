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

import java.util.logging.Logger;

/**
 *
 * @author albonico, jeugenio
 */
class HierarchicalStrategy implements CoordinationStrategy {

    private static final Logger LOG = Logger.getLogger(HierarchicalStrategy.class.getName());
    private TesterSet testers;

    public void init(TesterSet ts) {
        testers = ts;
    }

    /**
     * Hierarchical execution of test steps.
     * 
     * @throws InterruptedException
     */
    public void testcaseExecution() throws InterruptedException {
        LOG.entering("HierarchicalStrategy", "testcaseExecution()");

        for (Integer order : testers.getSchedule().orders()) {
            testers.hierarchicalExecute(order);
        }
    }
}
